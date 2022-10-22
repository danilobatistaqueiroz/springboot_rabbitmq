package br.com.labs.rabbit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

class ExchangeTypesTest {

    static Connection connection;
    static String queue1 = "minha-queue1";
    static String queue2 = "minha-queue2";
    static String queue3 = "minha-queue3";
    static String queue4 = "minha-queue4";
    static String queue5 = "minha-queue5";
    static String queue6 = "minha-queue6";
    static String queue7 = "minha-queue7";
    
    @BeforeAll
    static void setUp() throws IOException, TimeoutException, InterruptedException {
        connection = Builder.createAdmConnection();
    }
    
    @Test
    void direct_exchange() throws IOException, InterruptedException {
        Channel ch = connection.createChannel();
        
        String directExchange = "minha-direct-exchange";
        
        ch.queueDelete(queue1);
        ch.queueDelete(queue2);
        ch.queueDelete(queue3);
        ch.queueDelete(queue4);
        ch.exchangeDelete(directExchange);

        ch.queueDeclare(queue1, false, false, false, null);
        ch.queueDeclare(queue2, false, false, false, null);
        ch.queueDeclare(queue3, false, false, false, null);
        ch.queueDeclare(queue4, false, false, false, null);
        ch.exchangeDeclare(directExchange, BuiltinExchangeType.DIRECT, false, false, false, null);
        ch.queueBind(queue1, directExchange, "tests");
        ch.queueBind(queue2, directExchange, "abc");
        ch.queueBind(queue3, directExchange, "tests");
        ch.queueBind(queue4, directExchange, "xyz");

        ch.basicPublish(directExchange, "tests", null, "minha mensagem:tests".getBytes());
        ch.basicPublish(directExchange, "abc", null, "minha mensagem:abc".getBytes());
        ch.basicPublish(directExchange, "xyz", null, "minha mensagem:xyz".getBytes());
        ch.basicPublish(directExchange, "1abc", null, "minha mensagem:1abc".getBytes());
        ch.basicPublish(directExchange, "xyz1", null, "minha mensagem:xyz1".getBytes());

        Thread.sleep(500);

        CancelCallback cancelCallback = (consumerTag) -> {
        };
        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: %s mensagem: '%s'\n", queue1, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue1, false, deliverCallback1, cancelCallback);
        
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: %s mensagem: '%s'\n", queue2, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue2, false, deliverCallback2, cancelCallback);

        DeliverCallback deliverCallback3 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: %s mensagem: '%s'\n", queue3, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue3, false, deliverCallback3, cancelCallback);
        
        DeliverCallback deliverCallback4 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: %s mensagem: '%s'\n", queue4, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue4, false, deliverCallback4, cancelCallback);
        
        Thread.sleep(1500);
        ch.queueDelete(queue1);
        ch.queueDelete(queue2);
        ch.queueDelete(queue3);
        ch.queueDelete(queue4);
        ch.exchangeDelete(directExchange);
    }

    @Test
    void fanout_exchange() throws IOException, InterruptedException {
        Channel ch = connection.createChannel();
        
        String fanoutExchange = "minha-fanout-exchange";

        ch.queueDelete(queue1);
        ch.queueDelete(queue2);
        ch.queueDelete(queue3);
        ch.queueDelete(queue4);
        ch.exchangeDelete(fanoutExchange);

        ch.queueDeclare(queue1, false, false, false, null);
        ch.queueDeclare(queue2, false, false, false, null);
        ch.queueDeclare(queue3, false, false, false, null);
        ch.queueDeclare(queue4, false, false, false, null);
        ch.exchangeDeclare(fanoutExchange, BuiltinExchangeType.FANOUT, false, false, false, null);
        ch.queueBind(queue1, fanoutExchange, "tests");
        ch.queueBind(queue2, fanoutExchange, "abc");
        ch.queueBind(queue3, fanoutExchange, "tests");
        ch.queueBind(queue4, fanoutExchange, "xyz");

        ch.basicPublish(fanoutExchange, "tests", null, "minha mensagem:tests".getBytes());
        ch.basicPublish(fanoutExchange, "abc", null, "minha mensagem:abc".getBytes());
        ch.basicPublish(fanoutExchange, "xyz", null, "minha mensagem:xyz".getBytes());
        ch.basicPublish(fanoutExchange, "1abc", null, "minha mensagem:1abc".getBytes());
        ch.basicPublish(fanoutExchange, "xyz1", null, "minha mensagem:xyz1".getBytes());
        
        Thread.sleep(500);

        CancelCallback cancelCallback = (consumerTag) -> {
        };
        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: %s mensagem: '%s'\n", queue1, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue1, false, deliverCallback1, cancelCallback);
        
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: %s mensagem: '%s'\n", queue2, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue2, false, deliverCallback2, cancelCallback);

        DeliverCallback deliverCallback3 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: %s mensagem: '%s'\n", queue3, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue3, false, deliverCallback3, cancelCallback);
        
        DeliverCallback deliverCallback4 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: %s mensagem: '%s'\n", queue4, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue4, false, deliverCallback4, cancelCallback);
        
        Thread.sleep(1500);
        ch.queueDelete(queue1);
        ch.queueDelete(queue2);
        ch.queueDelete(queue3);
        ch.queueDelete(queue4);
        ch.exchangeDelete(fanoutExchange);
    }
    
    @Test
    void topic_exchange() throws IOException, InterruptedException {
        Channel ch = connection.createChannel();

        String topicExchange = "minha-topic-exchange";
        
        ch.queueDelete(queue1);
        ch.queueDelete(queue2);
        ch.queueDelete(queue3);
        ch.queueDelete(queue4);
        ch.exchangeDelete(topicExchange);

        ch.queueDeclare(queue1, false, false, false, null);
        ch.queueDeclare(queue2, false, false, false, null);
        ch.queueDeclare(queue3, false, false, false, null);
        ch.queueDeclare(queue4, false, false, false, null);
        ch.exchangeDeclare(topicExchange, BuiltinExchangeType.TOPIC, false, false, false, null);
        ch.queueBind(queue1, topicExchange, "tests");
        ch.queueBind(queue2, topicExchange, "*.abc");
        ch.queueBind(queue3, topicExchange, "tests.*");
        ch.queueBind(queue4, topicExchange, "xyz.#");

        ch.basicPublish(topicExchange, "tests.abc", null, "minha mensagem:tests.abc".getBytes());
        ch.basicPublish(topicExchange, "tests.ccc", null, "minha mensagem:tests.ccc".getBytes());
        ch.basicPublish(topicExchange, "aaa.abc", null, "minha mensagem:aaa.abc".getBytes());
        ch.basicPublish(topicExchange, "xyz", null, "minha mensagem:xyz".getBytes());
        ch.basicPublish(topicExchange, "abc", null, "minha mensagem:abc".getBytes());
        ch.basicPublish(topicExchange, "xyz.1", null, "minha mensagem:xyz.1".getBytes());
        
        Thread.sleep(500);

        CancelCallback cancelCallback = (consumerTag) -> {
        };
        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: %s mensagem: '%s'\n", queue1, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue1, false, deliverCallback1, cancelCallback);
        
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: %s mensagem: '%s'\n", queue2, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue2, false, deliverCallback2, cancelCallback);

        DeliverCallback deliverCallback3 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: %s mensagem: '%s'\n", queue3, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue3, false, deliverCallback3, cancelCallback);
        
        DeliverCallback deliverCallback4 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: %s mensagem: '%s'\n", queue4, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue4, false, deliverCallback4, cancelCallback);
        
        Thread.sleep(1500);
        ch.queueDelete(queue1);
        ch.queueDelete(queue2);
        ch.queueDelete(queue3);
        ch.queueDelete(queue4);
        ch.exchangeDelete(topicExchange);
    }
    
    @Test
    void header_exchange() throws IOException, InterruptedException {
        //excelente tutorial:
        //https://codedestine.com/rabbitmq-headers-exchange/
        //header exchange ignora routing key
        //x-match:any significa que qualquer um (algum) dos parametros se bater com os da mensagem, ela será roteada
        //x-match:all significa que se (somente no caso de) todos os parametros baterem é que a mensagem será roteada
        Channel ch = connection.createChannel();
        
        String headerExchange = "minha-head-exchange";

        ch.queueDelete(queue1);
        ch.queueDelete(queue2);
        ch.queueDelete(queue3);
        ch.queueDelete(queue4);
        ch.queueDelete(queue5);
        ch.queueDelete(queue6);
        ch.queueDelete(queue7);
        ch.exchangeDelete(headerExchange);

        ch.queueDeclare(queue1, false, false, false, null);
        ch.queueDeclare(queue2, false, false, false, null);
        ch.queueDeclare(queue3, false, false, false, null);
        ch.queueDeclare(queue4, false, false, false, null);
        ch.queueDeclare(queue5, false, false, false, null);
        ch.queueDeclare(queue6, false, false, false, null);
        ch.queueDeclare(queue7, false, false, false, null);
        
        ch.exchangeDeclare(headerExchange, BuiltinExchangeType.HEADERS, false, false, false, null);
        
        Map<String,Object> map = new HashMap<String,Object>();   
        map.put("x-match","any");
        map.put("First","A");
        map.put("Fourth","D");
        ch.queueBind(queue1, headerExchange, "", map);
        System.out.println("queue: '%s' com headers: %s".formatted(queue1,map));
        
        map = new HashMap<String,Object>();   
        map.put("x-match","any");
        map.put("Fourth","D");
        map.put("Third","C");
        ch.queueBind(queue2, headerExchange, "", map);
        System.out.println("queue: '%s' com headers: %s".formatted(queue2,map));
        
        map = new HashMap<String,Object>();   
        map.put("x-match","all");
        map.put("First","A");
        map.put("Third","C");
        ch.queueBind(queue3, headerExchange, "", map);
        System.out.println("queue: '%s' com headers: %s".formatted(queue3,map));
        
        map = new HashMap<String,Object>();   
        map.put("x-match","all");
        map.put("First","A");
        map.put("Third","C");
        map.put("Fourth","D");
        ch.queueBind(queue4, headerExchange, "", map);
        System.out.println("queue: '%s' com headers: %s".formatted(queue4,map));
        
        map = new HashMap<String,Object>();   
        map.put("x-match","all");
        map.put("First","A");
        ch.queueBind(queue5, headerExchange, "", map);
        System.out.println("queue: '%s' com headers: %s".formatted(queue5,map));
        
        map = new HashMap<String,Object>();   
        map.put("x-match","any");
        map.put("First","A");
        ch.queueBind(queue6, headerExchange, "", map);
        System.out.println("queue: '%s' com headers: %s".formatted(queue6,map));
        
        map = new HashMap<String,Object>();   
        map.put("x-match","all");
        map.put("First","A");
        map.put("Second","B");
        map.put("Third","C");
        map.put("Fourth","D");
        ch.queueBind(queue7, headerExchange, "", map);
        System.out.println("queue: '%s' com headers: %s".formatted(queue7,map));
        
        BasicProperties props = new BasicProperties();
        map = new HashMap<String,Object>(); 
        map.put("First","A");
        map.put("Fourth","D");
        props = props.builder().headers(map).build();
        String msg = "minha mensagem:AD";
        ch.basicPublish(headerExchange, "", props, msg.getBytes());
        System.out.println("mensagem: '%s' enviada com properties:%s".formatted(msg,map));
        
        props = new BasicProperties();
        map = new HashMap<String,Object>(); 
        map.put("Third","C");
        props = props.builder().headers(map).build();
        msg = "minha mensagem:C";
        ch.basicPublish(headerExchange, "", props, msg.getBytes());
        System.out.println("mensagem: '%s' enviada com properties:%s".formatted(msg,map));
        
        map = new HashMap<String,Object>();
        props = new BasicProperties();
        map.put("First","A");
        map.put("Third","C");
        props = props.builder().headers(map).build();
        msg = "minha mensagem:AC";
        ch.basicPublish(headerExchange, "", props, msg.getBytes());
        System.out.println("mensagem: '%s' enviada com properties:%s".formatted(msg,map));
        
        map = new HashMap<String,Object>();
        props = new BasicProperties();
        map.put("First","A");
        map.put("Third","C");
        map.put("Fourth","D");
        props = props.builder().headers(map).build();
        msg = "minha mensagem:ACD";
        ch.basicPublish(headerExchange, "", props, msg.getBytes());
        System.out.println("mensagem: '%s' enviada com properties:%s".formatted(msg,map));
        
        map = new HashMap<String,Object>();
        props = new BasicProperties();
        map.put("First","A");
        map.put("Second","B");
        map.put("Third","C");
        map.put("Fourth","D");
        props = props.builder().headers(map).build();
        msg = "minha mensagem:ABCD";
        ch.basicPublish(headerExchange, "", props, msg.getBytes());
        System.out.println("mensagem: '%s' enviada com properties:%s".formatted(msg,map));
        
        map = new HashMap<String,Object>();
        props = new BasicProperties();
        map.put("Second","B");
        props = props.builder().headers(map).build();
        msg = "minha mensagem:B";
        ch.basicPublish(headerExchange, "", props, msg.getBytes());
        System.out.println("mensagem: '%s' enviada com properties:%s".formatted(msg,map));
        
        
        Thread.sleep(500);

        CancelCallback cancelCallback = (consumerTag) -> {
        };
        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: '%s' mensagem: '%s'\n", queue1, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue1, false, deliverCallback1, cancelCallback);
        
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: '%s' mensagem: '%s'\n", queue2, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue2, false, deliverCallback2, cancelCallback);

        DeliverCallback deliverCallback3 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: '%s' mensagem: '%s'\n", queue3, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue3, false, deliverCallback3, cancelCallback);
        
        DeliverCallback deliverCallback4 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: '%s' mensagem: '%s'\n", queue4, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue4, false, deliverCallback4, cancelCallback);
        
        DeliverCallback deliverCallback5 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: '%s' mensagem: '%s'\n", queue5, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue5, false, deliverCallback5, cancelCallback);
        
        DeliverCallback deliverCallback6 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: '%s' mensagem: '%s'\n", queue6, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue6, false, deliverCallback6, cancelCallback);
        
        DeliverCallback deliverCallback7 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.format("consumindo queue: '%s' mensagem: '%s'\n", queue7, message);
            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        ch.basicConsume(queue7, false, deliverCallback7, cancelCallback);
        
        Thread.sleep(1500);
        ch.queueDelete(queue1);
        ch.queueDelete(queue2);
        ch.queueDelete(queue3);
        ch.queueDelete(queue4);
        ch.queueDelete(queue5);
        ch.queueDelete(queue6);
        ch.queueDelete(queue7);
        ch.exchangeDelete(headerExchange);
    }
    
}
