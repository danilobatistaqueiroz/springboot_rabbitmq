package br.com.labs.rabbit;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

class ConsumerTagDeliveryTagTest {

    static Connection connection;
    static Connection connection2;
    
    @BeforeAll
    static void setUp() throws IOException, TimeoutException {
        connection = Builder.createAdmConnection();
    }
    
    @Test
    void test() throws IOException, InterruptedException {
        Channel ch = connection.createChannel();
        String queue = "minha-queue";
        ch.queueDelete(queue);
        ch.queueDeclare(queue, false, true, false, null);
        ch.basicPublish("", queue, null, "minha mensagem".getBytes());
        DeliverCallback deliverCallback = (consumer_tag, delivery) -> {
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            System.out.println(consumer_tag);
            Assert.assertEquals("meu-consumer", consumer_tag);
        };
        String consumerTag = "meu-consumer";
        String consumerTagRetorno = ch.basicConsume(queue, true, consumerTag, deliverCallback, cancelCallback -> { });
        Thread.sleep(1200);
        Assert.assertEquals(consumerTag, consumerTagRetorno);
    }

}
