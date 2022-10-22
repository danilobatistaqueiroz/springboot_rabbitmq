package br.com.labs.rabbit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

class DeliveryLimitTest {

	static Connection connection;
	
	/*****************************
	 * criando uma quorum queue para esse cenário é possível aplicar uma regra de delivery-limit
	 */
	@BeforeAll
	static void createConnection() throws IOException, TimeoutException {
		connection = Builder.createAdmConnection();
	}

	/*** ***************************
     * é possível aplicar policy para quorum queues
     * rabbitmqctl set_policy delivery-limit "^q_max_requeue$" '{"delivery-limit":3}' --apply-to queues
	 */
	@Test
	void message_quorumqueue_delivery_limit() throws IOException, InterruptedException {

		Channel ch = connection.createChannel();

		String queue = "qquorum1";
		String exchange = "exchange3";

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("x-queue-type", "quorum");
		args.put("x-expires", 15000);
		args.put("x-delivery-limit",5);

		ch.queueDeclare(queue, true, false, false, args);
		ch.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, false, true, null);
		ch.queueBind(queue, exchange, "tests");

		ch.confirmSelect();
		var ack = new Object() {
			String value = "";
		};
		ConfirmCallback okConfirms = (sequenceNumber, multiple) -> {
		    System.out.println("ack");
			ack.value = "ack";
		};
		ConfirmCallback errConfirms = (sequenceNumber, multiple) -> {
		    System.out.println("nack");
			ack.value = "nack";
		};
		ch.addConfirmListener(okConfirms, errConfirms);

		String body = String.valueOf("msg test_mandatory_message");
		ch.basicPublish(exchange, "tests", false, null, body.getBytes());

		consume(ch, queue);

		Thread.sleep(2000);

		Assert.assertEquals("ack", ack.value);
	}
	
    @Test
    void message_redelivery() throws IOException, InterruptedException {

        Channel ch = connection.createChannel();

        String queue = "minha-queue-classica";
        String exchange = "exchange3";

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-expires", 15000);

        ch.queueDeclare(queue, false, false, false, args);
        ch.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, false, true, null);
        ch.queueBind(queue, exchange, "tests");

        ch.confirmSelect();
        var ack = new Object() {
            String value = "";
        };
        ConfirmCallback okConfirms = (sequenceNumber, multiple) -> {
            System.out.println("ack");
            ack.value = "ack";
        };
        ConfirmCallback errConfirms = (sequenceNumber, multiple) -> {
            System.out.println("nack");
            ack.value = "nack";
        };
        ch.addConfirmListener(okConfirms, errConfirms);

        String body = String.valueOf("msg test_mandatory_message");
        ch.basicPublish(exchange, "tests", false, null, body.getBytes());

        consume(ch, queue);

        //vai ficar reenviando, tendo redelivery até acabar o sleep e o metodo encerrar
        Thread.sleep(1000);

        Assert.assertEquals("ack", ack.value);
    }

	void consume(Channel ch, String queue) throws IOException {
		boolean autoAck = false;
		ch.basicConsume(queue, autoAck, "a-consumer-tag", new DefaultConsumer(ch) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String s = new String(body, StandardCharsets.UTF_8);
				System.out.println("recebido: " + s);
				long deliveryTag = envelope.getDeliveryTag();
				boolean multiple = true, requeue = true;
				ch.basicNack(deliveryTag, multiple, requeue);
				try {
					Thread.sleep(10);
				} catch (InterruptedException ex) {
					System.out.println("erro no sleep");
				}
			}
		});
	}

}
