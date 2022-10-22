package br.com.labs.rabbit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

class ConsumerNackTest {
    
    /***
     * Press Ctrl+Shift+↑ (moves cursor to current method declaration),
     * press Alt+Shift+x (or d for debug) then press t (hotkey for "Run JUnit Test"),
     * check test result,
     * press Alt+← to get back to the line of code you were before.
    ***/

	static Connection connection;
	static String queue = "queue-minha";
	static String exchange = "minha-exchange";
	
	@BeforeAll
	static void setUp() throws IOException, TimeoutException, InterruptedException {
		connection = Builder.createAdmConnection();
    }
	
	@Test
	void requeue() throws IOException, InterruptedException {
		
        Channel ch = connection.createChannel();

        ch.queueDelete(queue);
        ch.queueDeclare(queue, false, false, false, null);
        
        ch.basicPublish("",queue, null, "minha mensagem".getBytes());
        Thread.sleep(150);
        
        var totalDeliveries = new Object() { long value = 0; };
        
        ch.basicConsume(queue, false, "a-consumer-tag", new DefaultConsumer(ch) {
        	@Override
            public void handleDelivery(String consumerTag,Envelope envelope,BasicProperties properties,byte[] body) throws IOException {
                String s = new String(body, StandardCharsets.UTF_8);
                System.out.println("recebido: " + s);
                long deliveryTag = envelope.getDeliveryTag();
                System.out.println(deliveryTag);
                System.out.println(properties);
                totalDeliveries.value = deliveryTag;
                if (deliveryTag<2) //inicia em 1 e vai incrementando a cada handle, enquanto o método executar basicNack com requeue
                    ch.basicNack(deliveryTag, true, true); //esse handler vai ficar pegando a mensagem novamente enquanto estiver rodando
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    System.out.println("erro no sleep");
                }
            }
        });
        
        Thread.sleep(1000);
        Assert.assertEquals(2, totalDeliveries.value);

	}
	
    @Test
    void redelivery_limit() throws IOException, InterruptedException {
        
        Channel ch = connection.createChannel();

        ch.queueDelete(queue);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-queue-type", "quorum");
        args.put("x-expires", 150000);
        args.put("x-delivery-limit",10);
        //args.put("quorum_cluster_size", 3);
        ch.queueDeclare(queue, true, false, false, args);
        
        ch.basicPublish("",queue, null, "minha mensagem".getBytes());
        Thread.sleep(150);
        
        var totalDeliveries = new Object() { long value = 0; };
        
        ch.basicConsume(queue, false, "a-consumer-tag", new DefaultConsumer(ch) {
            @Override
            public void handleDelivery(String consumerTag,Envelope envelope,BasicProperties properties,byte[] body) throws IOException {
                String s = new String(body, StandardCharsets.UTF_8);
                System.out.println("recebido: " + s);
                long deliveryTag = envelope.getDeliveryTag();
                System.out.println(deliveryTag);
                System.out.println(properties);
                totalDeliveries.value = deliveryTag;
                ch.basicNack(deliveryTag, true, true);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    System.out.println("erro no sleep");
                }
            }
        });
        
        Thread.sleep(1500);
        Assert.assertEquals(11, totalDeliveries.value);

    }

}
