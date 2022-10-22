package br.com.labs.rabbit;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

class ExchangeToExchangeTest {

    static Connection connection;
    static String queue = "minha-queue";
    static String sourceExchange = "source-exchange";
    static String destinationExchange = "destination-exchange";
    
    @BeforeAll
    static void setUp() throws IOException, TimeoutException, InterruptedException {
        connection = Builder.createAdmConnection();
    }
    
	@Test
	void test() throws IOException, InterruptedException {
        Channel ch = connection.createChannel();
        ch.queueDelete(queue);
        ch.exchangeDelete(sourceExchange);
        ch.exchangeDelete(destinationExchange);
        ch.queueDeclare(queue, false, false, false, null);
        ch.exchangeDeclare(destinationExchange, BuiltinExchangeType.DIRECT, false, false, null);
        ch.exchangeDeclare(sourceExchange, BuiltinExchangeType.DIRECT, false, false, null);
        ch.queueBind(queue, destinationExchange, "tests");
	    ch.exchangeBind(destinationExchange, sourceExchange, "tests");
	    ch.basicPublish(sourceExchange,"tests", null, "minha mensagem".getBytes());
	    Thread.sleep(100);
        long qtdmessages = ch.messageCount(queue);
        Assert.assertEquals(1, qtdmessages);
        ch.queueDelete(queue);
        ch.exchangeDelete(sourceExchange);
        ch.exchangeDelete(destinationExchange);
	}

}
