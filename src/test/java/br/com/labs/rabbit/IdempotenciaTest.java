package br.com.labs.rabbit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

class IdempotenciaTest {

    static Connection connection;
    static String queue = "minha-queue";
    
    @BeforeAll
    static void setUp() throws IOException, TimeoutException, InterruptedException {
        connection = Builder.createAdmConnection();
    }
    
    @Test
    void delivery_tag() throws IOException, InterruptedException {
        
        Channel ch = connection.createChannel();
        ch.queueDelete(queue);
        ch.queueDeclare(queue, false, false, false, null);
        
        AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
        BasicProperties props =  propsBuilder.messageId("1").build();
        
        ch.basicPublish("",queue, props, "minha mensagem".getBytes());
        Thread.sleep(150);
        
        ch.basicConsume(queue, false, "a-consumer-tag", new DefaultConsumer(ch) {
            @Override
            public void handleDelivery(String consumerTag,Envelope envelope,BasicProperties properties,byte[] body) throws IOException {
                String s = new String(body, StandardCharsets.UTF_8);
                System.out.println("recebido: " + s);
                long deliveryTag = envelope.getDeliveryTag();
                System.out.println(deliveryTag);
                System.out.println(properties);
                if(deliveryTag == 1) {
                    ch.basicNack(deliveryTag,false,true);
                } else {

                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    System.out.println("erro no sleep");
                }
            }
        });
        
        Thread.sleep(1500);
        ch.queueDelete(queue);
    }
    
    @Test
    void message_id() throws IOException, InterruptedException {
        
        Channel ch = connection.createChannel();
        ch.queueDelete(queue);
        ch.queueDeclare(queue, false, false, false, null);
        
        AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
        BasicProperties props =  propsBuilder.messageId("1").build();
        
        ch.basicPublish("",queue, props, "minha mensagem".getBytes());
        Thread.sleep(150);
        
        ch.basicConsume(queue, false, "a-consumer-tag", new DefaultConsumer(ch) {
            @Override
            public void handleDelivery(String consumerTag,Envelope envelope,BasicProperties properties,byte[] body) throws IOException {
                String s = new String(body, StandardCharsets.UTF_8);
                System.out.println("recebido: " + s);
                long deliveryTag = envelope.getDeliveryTag();
                System.out.println(deliveryTag);
                System.out.println(properties);
                System.out.println(properties.getMessageId());
                if(properties.getMessageId().equals("1")) {
                    System.out.println("nada");
                } else {
                    System.out.println("ack");
                    ch.basicAck(deliveryTag,false);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    System.out.println("erro no sleep");
                }
            }
        });
        
        Thread.sleep(1500);
        ch.queueDelete(queue);
    }

}
