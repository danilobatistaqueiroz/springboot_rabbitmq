package br.com.labs.rabbit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

//Normally, active consumers connected to a queue receive messages from it in a round-robin fashion.
//Consumer priorities allow you to ensure that high priority consumers receive messages while they are active

class PriorityTest {
    
    static Connection connection;
    
    @BeforeAll
    static void createConnection() throws IOException, TimeoutException {
        connection = Builder.createAdmConnection();
    }
    
    @Test
    void priority_queue_priority_message() throws IOException, InterruptedException {
        String queue = "minha-queue";
        Channel ch = connection.createChannel();
        ch.queueDelete(queue);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-max-priority", 10);
        ch.queueDeclare(queue, true, false, false, args);
        
        for(int i = 0; i < 10; i++) {
            String msg = i+":minha mensagem";
            ch.basicPublish("",queue, null, msg.getBytes());
        }
        AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
        BasicProperties properties = null;
        properties = propsBuilder.priority(1).build();
        ch.basicPublish("",queue, properties, "minha mensagem prioritária".getBytes());
        
        Thread.sleep(50);
        
        var firstMessage = new Object() {String value = "";}; 
        ch.basicConsume(queue, false, new DefaultConsumer(ch) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println(msg);
                System.out.println(envelope.getDeliveryTag());
                if(firstMessage.value=="")
                    firstMessage.value = msg;
            }
        });
        Thread.sleep(600);
        Assert.assertEquals("minha mensagem prioritária",firstMessage.value);
        
//        String result = Builder.execRabbitmqadmin("""
//        get queue=%s
//        """.formatted(queue),true);
//        System.out.println(result);
        
    }
    
    @Test
    void priority_queue_normal_message() throws IOException, InterruptedException {
        String queue = "minha-queue";
        Channel ch = connection.createChannel();
        ch.queueDelete(queue);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-max-priority", 10);
        ch.queueDeclare(queue, true, false, false, args);
        
        for(int i = 0; i < 10; i++) {
            String msg = i+":minha mensagem";
            ch.basicPublish("",queue, null, msg.getBytes());
        }
        ch.basicPublish("",queue, null, "minha mensagem prioritária".getBytes());
        
        Thread.sleep(50);
        
        var firstMessage = new Object() {String value = "";}; 
        ch.basicConsume(queue, false, new DefaultConsumer(ch) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println(msg);
                System.out.println(envelope.getDeliveryTag());
                if(firstMessage.value=="")
                    firstMessage.value = msg;
            }
        });
        Thread.sleep(600);
        Assert.assertEquals("0:minha mensagem",firstMessage.value);
    }
    
	@Test
	void consumer_priority() throws IOException, InterruptedException {
        String queue = "minha-queue";
        Channel ch = connection.createChannel();
        ch.queueDelete(queue);
        ch.queueDeclare(queue, false, true, false, null);
        ch.basicPublish("",queue, null, "minha mensagem".getBytes());
        
        Thread.sleep(50);
        
	    Map<String, Object> args = new HashMap<String, Object>();
	    args.put("x-priority", 10); //esse consumer terá prioridade sobre outros consumers que não especifiquem x-priority, ou que tenham usado com valores abaixo de 10
        ch.basicConsume(queue, false, args, new DefaultConsumer(ch) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
                System.out.println(new String(body, StandardCharsets.UTF_8));
                System.out.println(envelope.getDeliveryTag());
            }
        });
        Thread.sleep(600);
	}

}
