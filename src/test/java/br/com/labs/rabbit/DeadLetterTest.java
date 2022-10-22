package br.com.labs.rabbit;

import java.io.IOException;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

class DeadLetterTest {

	static Connection connection;
	static String queue = "minha-queue";
	static String queueDLX = "minha-dlx-queue";
	static String exchange = "minha-exchange";
	static String exchangeDLX = "minha-dlx-exchange";
	
	/*******************************************************************
	 * 
	 * não é possível aplicar dead-letter em quorum queues
	 * 
	 ********************************************************************/
	
	@BeforeAll
	static void setUp() throws IOException, TimeoutException, InterruptedException {
		connection = Builder.createAdmConnection();
    }

	@Test
	void dead_letter_message_ttl() throws IOException, InterruptedException {
		Builder.execRabbitmqctl("""
		set_policy DLX .* {"dead-letter-exchange":"%s"} --apply-to queues
		""".formatted(exchangeDLX));
		
        Channel ch = connection.createChannel();
        
        ch.queueDelete(queue);
        ch.queueDelete(queueDLX);
        ch.exchangeDelete(exchange);
        ch.exchangeDelete(exchangeDLX);
        
        ch.queueDeclare(queue, false, false, false, null);
        ch.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT,false,false,false,null);
        ch.exchangeDeclare(exchangeDLX, BuiltinExchangeType.DIRECT,false,false,false,null);
        ch.queueDeclare(queueDLX, false, false, false, null);
        ch.queueBind(queueDLX, exchangeDLX, "tests");
        ch.queueBind(queue, exchange, "tests");

        ch.confirmSelect();
        var ack = new Object(){ String value = ""; };
        ConfirmCallback okConfirms = (sequenceNumber, multiple) -> {
        	ack.value = "ack";
        };
        ConfirmCallback errConfirms = (sequenceNumber, multiple) -> {
        	ack.value = "nack";
        };
        ch.addConfirmListener(okConfirms, errConfirms);

        AMQP.BasicProperties messageProperties = setTTLPerMessage(100);
       
        String body = String.valueOf("test_dead_letter_message_ttl");
        ch.basicPublish(exchange,"tests", messageProperties, body.getBytes());
        
        Thread.sleep(1000);
        
        long qtdmessagesDLX = ch.messageCount(queueDLX);
        Assert.assertEquals(1, qtdmessagesDLX);
        long qtdmessages = ch.messageCount(queue);
        Assert.assertEquals(0, qtdmessages);
        Assert.assertEquals("ack",ack.value);
        
		Builder.execRabbitmqctl("""
		clear_policy DLX
		""");
        ch.queueDelete(queue);
        ch.queueDelete(queueDLX);
        ch.exchangeDelete(exchange);
        ch.exchangeDelete(exchangeDLX);
	}
	
	AMQP.BasicProperties setTTLPerMessage(Integer expiration) {
        AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
        return propsBuilder.expiration(expiration.toString()).build();
	}
	
	@Test
	void dead_letter_length_limit() throws IOException, InterruptedException {
		Builder.execRabbitmqctl("""
		set_policy DLX ^%s$ {"dead-letter-exchange":"%s","max-length":5,"overflow":"drop-head"} --apply-to queues
		""".formatted(queue,exchangeDLX),true);
		
        Channel ch = connection.createChannel();
        
        ch.queueDelete(queue);
        ch.queueDelete(queueDLX);
        ch.exchangeDelete(exchange);
        ch.exchangeDelete(exchangeDLX);
        
        ch.queueDeclare(queue,false,false,false,null);
        ch.exchangeDeclare(exchange,BuiltinExchangeType.DIRECT,false,false,false,null);
        ch.queueBind(queue,exchange,"tests");
        ch.queueDeclare(queueDLX,false,false,false,null);
        ch.exchangeDeclare(exchangeDLX,BuiltinExchangeType.DIRECT,false,false,false,null);
        ch.queueBind(queueDLX,exchangeDLX,"tests");

        ch.confirmSelect();
        ConcurrentNavigableMap<Long, String> msgs = new ConcurrentSkipListMap<>();
        ConcurrentNavigableMap<String, String> acks = new ConcurrentSkipListMap<>();
        
        ConfirmCallback okConfirms = (seq, multiple) -> {
        	String body = msgs.get(seq);
        	acks.put(body,"ack");
        };
        ConfirmCallback errConfirms = (seq, multiple) -> {
        	String body = msgs.get(seq);
        	acks.put(body,"nack");
            System.err.format(
            		"Message with body %s has been nack-ed. Sequence number: %d, multiple: %b%n",
                    body, seq, multiple
            );
        };
        ch.addConfirmListener(okConfirms, errConfirms);
        
        for(int i = 0; i < 6; i++) {
        	String msg = "minha mensagem %s".formatted(i);
        	msgs.put(ch.getNextPublishSeqNo(),msg);
        	Thread.sleep(100);
        	ch.basicPublish(exchange,"tests", null, msg.getBytes());
        }
        Thread.sleep(500);
        
        long qtdmessagesDLX = ch.messageCount(queueDLX);
        Assert.assertEquals(1, qtdmessagesDLX);
        long qtdmessages = ch.messageCount(queue);
        Assert.assertEquals(5, qtdmessages);
        Assert.assertEquals("ack, ack, ack, ack, ack, ack",acks.values().toString().replace("[","").replace("]", ""));
        
		Builder.execRabbitmqctl("""
		clear_policy DLX
		""");
        ch.queueDelete(queue);
        ch.queueDelete(queueDLX);
        ch.exchangeDelete(exchange);
        ch.exchangeDelete(exchangeDLX);
	}
	
	@Test
	void dead_letter_nack_consumer() throws IOException, InterruptedException {
        Builder.execRabbitmqctl("""
        set_policy DLX ^%s$ {"dead-letter-exchange":"%s"} --apply-to queues
        """.formatted(queue,exchangeDLX));
        
        Channel ch = connection.createChannel();
        
        ch.queueDelete(queue);
        ch.queueDelete(queueDLX);
        ch.exchangeDelete(exchange);
        ch.exchangeDelete(exchangeDLX);
        
        ch.queueDeclare(queue,false,false,false,null);
        ch.exchangeDeclare(exchange,BuiltinExchangeType.DIRECT,false,false,false,null);
        ch.queueBind(queue,exchange,"tests");
        ch.queueDeclare(queueDLX,false,false,false,null);
        ch.exchangeDeclare(exchangeDLX,BuiltinExchangeType.DIRECT,false,false,false,null);
        ch.queueBind(queueDLX,exchangeDLX,"tests");

        ch.confirmSelect();
        ConcurrentNavigableMap<Long, String> msgs = new ConcurrentSkipListMap<>();
        
        ConfirmCallback okConfirms = (seq, multiple) -> {
            String body = msgs.get(seq);
            System.out.println("confirmado o recebimento pelo broker da mensagem de texto: '%s' e sequencial: %d ".formatted(body,seq));
        };
        ConfirmCallback errConfirms = (seq, multiple) -> {
            String body = msgs.get(seq);
            System.err.format("mensagem com texto '%s' foi rejeitada pelo broker. sequencial: %d", body, seq);
        };
        ch.addConfirmListener(okConfirms, errConfirms);
        
        String msg = "minha mensagem";
        msgs.put(ch.getNextPublishSeqNo(),msg);
        ch.basicPublish(exchange,"tests", null, msg.getBytes());

        Thread.sleep(500);
        
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("o consumidor recebeu do broker a mensagem: '%s'\n",message);
            ch.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
        };
        CancelCallback cancelCallback = (consumerTag) -> {
            
        };
        ch.basicConsume(queue, false, deliverCallback, cancelCallback);
        
// ou usando handles        
//        ch.basicConsume(queue, false, "a-consumer-tag", new DefaultConsumer(ch) {
//            @Override
//            public void handleDelivery(String consumerTag,Envelope envelope,BasicProperties properties,byte[] body) throws IOException {
//                String message = new String(body, "UTF-8");
//                System.out.format("o consumidor recebeu do broker a mensagem: '%s'",message);
//                ch.basicNack(envelope.getDeliveryTag(), false, false);
//            }
//            @Override
//            public void handleCancel(String consumerTag) {
//            }
//        });
        
        Thread.sleep(300);
        
        long qtdmessagesDLX = ch.messageCount(queueDLX);
        Assert.assertEquals(1, qtdmessagesDLX);
        long qtdmessages = ch.messageCount(queue);
        Assert.assertEquals(0, qtdmessages);
        
        Builder.execRabbitmqctl("""
        clear_policy DLX
        """);
        ch.queueDelete(queue);
        ch.queueDelete(queueDLX);
        ch.exchangeDelete(exchange);
        ch.exchangeDelete(exchangeDLX);
	}
	
	void dead_letter_queue_ttl() {
	    /** configuração para ttl por queue usando argumentos **/
	    //args.put("x-message-ttl", 1000);
	    /** ******************************* **/
	    /** também é possível aplicar ttl na queue usando policy **/
	    // rabbitmqctl set_policy expiry ".*" '{"expires":1800000}' --apply-to queues
	    // essa policy coloca 30 minutos de ttl para todas as queues
	    /** **************************************************** **/
	}
}
