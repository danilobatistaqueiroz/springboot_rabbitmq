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
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ReturnListener;

class MandatoryMessageTest {

	static Connection connection;

	@BeforeAll
	static void setUp() throws IOException, TimeoutException {
		connection = Builder.createAdmConnection();
	}

	@Test
	void mandatory_message_route_altered() throws IOException, InterruptedException {

		Channel ch = connection.createChannel();

		String queue = "queue_mandatory";
		String exchange = "exchange";

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("x-expires", 15000); // queue será dropada após 15 segundos

		ch.queueDeclare(queue, true, false, false, args);
		ch.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT);
		ch.queueBind(queue, exchange, "tests");

		ch.confirmSelect();
		var ack = new Object() {
			String value = "";
		};
		ConfirmCallback okConfirms = (sequenceNumber, multiple) -> {
			ack.value = "broker ack";
		};
		ConfirmCallback errConfirms = (sequenceNumber, multiple) -> {
			ack.value = "broker nack";
		};
		ch.addConfirmListener(okConfirms, errConfirms);

		String body = String.valueOf("test_mandatory_message");
		Boolean mandatory = true;
		ch.addReturnListener(new ReturnListener() {
			@Override
			public void handleReturn(int replyCode, String replyText, String exchange, String routingKey,
					AMQP.BasicProperties basicProperties, byte[] body) throws IOException {
				System.out.println(replyCode);
				System.out.println(replyText);
				System.out.println(exchange);
				System.out.println(routingKey);
				System.out.println(new String(body, StandardCharsets.UTF_8));
				ch.basicPublish(exchange, "tests", basicProperties, body);
			}
		});
		ch.basicPublish(exchange, "123", mandatory, null, body.getBytes());

		Thread.sleep(1000);
		
        long msgs = ch.messageCount(queue);
        Assert.assertEquals("a mensagem foi publicada com a rota corrigida", 1, msgs);

		Assert.assertEquals("broker ack", ack.value);
	}
	
    @Test
    void mandatory_message() throws IOException, InterruptedException {

        Channel ch = connection.createChannel();

        String queue = "queue_mandatory";
        String exchange = "exchange";

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-expires", 15000); // queue será dropada após 15 segundos

        ch.queueDeclare(queue, true, false, false, args);
        ch.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT);
        ch.queueBind(queue, exchange, "tests");

        ch.confirmSelect();
        var ack = new Object() {
            String value = "";
        };
        ConfirmCallback okConfirms = (sequenceNumber, multiple) -> {
            ack.value = "broker ack";
        };
        ConfirmCallback errConfirms = (sequenceNumber, multiple) -> {
            ack.value = "broker nack";
        };
        ch.addConfirmListener(okConfirms, errConfirms);

        String body = String.valueOf("test_mandatory_message");
        Boolean mandatory = true;
        ch.addReturnListener(new ReturnListener() {
            @Override
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey,
                    AMQP.BasicProperties basicProperties, byte[] body) throws IOException {
                System.out.println(replyCode);
                System.out.println(replyText);
                System.out.println(exchange);
                System.out.println(routingKey);
                System.out.println(new String(body, StandardCharsets.UTF_8));
            }
        });
        ch.basicPublish(exchange, "123", mandatory, null, body.getBytes());

        Thread.sleep(1000);
        
        long msgs = ch.messageCount(queue);
        Assert.assertEquals("a mensagem foi perdida", 0, msgs);

        Assert.assertEquals("broker ack", ack.value);
    }


}
