package br.com.labs.rabbit;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

class AlternateExchangeTest {

	static Connection connection;
	
	@BeforeAll
	static void createConnection() throws IOException, TimeoutException {
        connection = Builder.createAdmConnection();
    }
    
	@Test
	void test_send_alternate_exchange() throws IOException, InterruptedException {
        String queue = "minha-queue";
        String exchange = "minha-exchange";
        String queueAE = "minha-AE-queue";
        String exchangeAE = "minha-AE-exchange";
        
		Builder.execRabbitmqctl("""
		set_policy AE .* {"alternate-exchange":"%s"} --apply-to exchanges
		""".formatted(exchangeAE));
        Channel ch = connection.createChannel();

        ch.queueDelete(queue);
        ch.queueDelete(queueAE);
        ch.exchangeDelete(exchange);
        ch.exchangeDelete(exchangeAE);
        ch.queueDeclare(queue, false, true, false, null);
        ch.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, false, true, false, null);
        ch.exchangeDeclare(exchangeAE, BuiltinExchangeType.FANOUT, false, true, false, null);
        ch.queueDeclare(queueAE, false, true, false, null);
        ch.queueBind(queueAE, exchangeAE, "");
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

        String body = String.valueOf("msg teste 1");
        /** a rota não existe, a exchange tendo uma policy de alternate-exchange, a mensagem será redirecionada **/ 
        ch.basicPublish(exchange,"xyz", null, body.getBytes());
        
        long qtdMessagesQueue = ch.messageCount(queue);
        long qtdMessagesAE = ch.messageCount(queueAE);
        Assert.assertEquals(0, qtdMessagesQueue);
        Assert.assertEquals(1, qtdMessagesAE);
        
        Thread.sleep(100);

        Assert.assertEquals("ack",ack.value);

        ch.queueDelete(queue);
        ch.queueDelete(queueAE);
		Builder.execRabbitmqctl("""
		clear_policy AE
		""");
	}

}
