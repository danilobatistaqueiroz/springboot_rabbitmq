package br.com.labs.rabbit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

class ConsumerConfirmsTest {
    
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
	    ch.queueDeclare(queue, false, false, false, null);
        ch.basicPublish("", queue, null, "minha mensagem".getBytes());
        DeliverCallback deliverCallback = (consumer_tag, delivery) -> {
            System.out.println("first"+delivery.getEnvelope().getDeliveryTag());
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        String consumerTag = ch.basicConsume(queue, false, deliverCallback, cancelCallback -> { });
        ch.basicCancel(consumerTag);
        Thread.sleep(1200);
        long msgs = ch.messageCount(queue);
        Assert.assertEquals(0, msgs);
        ch.basicPublish("", queue, null, "minha mensagem".getBytes());
        DeliverCallback deliverCallback2 = (consumer_tag, delivery) -> {
            System.out.println(delivery.getEnvelope().getDeliveryTag());
            if(Long.valueOf(delivery.getEnvelope().getDeliveryTag())<=2L){
                ch.basicNack(delivery.getEnvelope().getDeliveryTag(),false,true);
                //ch.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
            }
        };
        consumerTag = ch.basicConsume(queue, false, deliverCallback2, cancelCallback -> { });
        Thread.sleep(200);
        msgs = ch.messageCount(queue);
        Assert.assertEquals(0, msgs);
	}

}
