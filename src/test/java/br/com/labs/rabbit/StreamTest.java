package br.com.labs.rabbit;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import com.rabbitmq.stream.*;
import com.rabbitmq.stream.StreamCreator.LeaderLocator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

class StreamTest {

    //para streams funcionar é necessário ativar o plugin:
    //rabbitmq-plugins enable rabbitmq_stream
    //é melhor usar a biblioteca especifica para streams: com.rabbitmq.stream-client
    //a porta para o protocolo de streams é 5552
    //as portas 5672, 5671 são para AMQP 0-9-1 e AMQP 1.0 com TLS e sem
    
/* 
docker run -it --rm --name rabbitmq_streams2 -p 5552:5552 -p 5672:5672 -p 15672:15672 \
-e RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS='-rabbitmq_stream advertised_host localhost' \
rabbitmq:3.10-management

docker run -it --rm --name rabbitmq_streams3 -p 5553:5552 -p 5673:5672 -p 15673:15672 \
-e RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS='-rabbitmq_stream advertised_host localhost' \
rabbitmq:3.10-management
      
docker exec rabbitmq rabbitmq-plugins enable rabbitmq_stream
      
pom.xml
<dependencies>
  <dependency>
    <groupId>com.rabbitmq</groupId>
    <artifactId>stream-client</artifactId>
    <version>0.8.0</version>
  </dependency>
</dependencies>


rabbitmq-streams add_replica [-p <vhost>] <stream-name> <node>

rabbitmq-streams delete_replica [-p <vhost>] <stream-name> <node>

The replication status of a stream can be queried using the following command:
rabbitmq-streams stream_status [-p <vhost>] <stream-name>

All nodes in a RabbitMQ cluster are equal peers: there are no special nodes in RabbitMQ core. 

*/
    
    static Connection connection;
    
    static final String PEDIDOS_STREAM = "financeiro.faturamento.pedidos";

    @BeforeAll
    static void setUp() throws IOException, TimeoutException {
        connection = Builder.createAdmConnection();
    }
    
    @Test
    void creating_environment_with_all_defaults() {
        Environment environment = Environment.builder().build();
        environment.close();
    }
    
    @Test
    void creating_an_environment_with_several_uris() {
        Environment environment = Environment.builder()
            .uris(Arrays.asList(                     
                    "rabbitmq-stream://host1:5552",
                    "rabbitmq-stream://host2:5552",
                    "rabbitmq-stream://host3:5552")
            )
            .build();
    }
    
    @Test
    void publishing_using_rabbitmq_stream_protocol() throws InterruptedException {
//        Environment environment = Environment.builder().build();
//        Address entryPoint = new Address("localhost", 5672);  // <1>
        Environment environment = Environment.builder().uri("rabbitmq-stream://rabbit_admin:.123-321.@localhost:5552/%2f").build();
////            .host(entryPoint.host())
////            .port(entryPoint.port())
////            .addressResolver(address -> entryPoint)
////            .username("rabbit_admin")
////            .password(".123-321.")

        String stream = PEDIDOS_STREAM;
        environment.streamCreator().maxAge(Duration.ofMinutes(1L)).stream(stream).create();
        
        int messageCount = 10000;
        CountDownLatch publishConfirmLatch = new CountDownLatch(messageCount);
        Producer producer = environment.producerBuilder().stream(stream).build();
        IntStream.range(0, messageCount)
                .forEach(i -> producer.send(  
                        producer.messageBuilder()                    
                            .addData(String.valueOf(i).getBytes())   
                            .build(),                                
                        confirmationStatus -> publishConfirmLatch.countDown()  
                ));
        publishConfirmLatch.await(10, TimeUnit.SECONDS);  
        producer.close();  
        System.out.printf("Published %,d messages%n", messageCount);
        //environment.deleteStream(PEDIDOS_STREAM);  
        environment.close(); 
    }
    
    @Test
    void consuming_using_stream_protocol() throws InterruptedException {
        Environment environment = Environment.builder().uri("rabbitmq-stream://rabbit_admin:.123-321.@localhost:5552/%2f").build();
        environment.streamCreator().maxAge(Duration.ofMinutes(1L)).stream(PEDIDOS_STREAM).create();
        System.out.println("Starting consuming...");
        int messageCount = 10000;
        AtomicLong sum = new AtomicLong(0);
        CountDownLatch consumeLatch = new CountDownLatch(messageCount);
        Consumer consumer = environment.consumerBuilder()  
                .stream(PEDIDOS_STREAM)
                .offset(OffsetSpecification.first()) 
                .messageHandler((offset, message) -> {  
                    sum.addAndGet(Long.parseLong(new String(message.getBodyAsBinary())));  
                    consumeLatch.countDown();  
                })
                .build();

        consumeLatch.await(10, TimeUnit.SECONDS);  

        System.out.println("Sum: " + sum.get());  

        consumer.close();  
        environment.deleteStream(PEDIDOS_STREAM);  
        environment.close(); 
    }

    @Test
    void publishing_using_rabbittmq_amqp_client() throws IOException, InterruptedException {
        Channel ch = connection.createChannel();
        String queue = "stream-queue";
        String exchange = "minha-exchange";
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-queue-type", "stream");
        arguments.put("x-max-length-bytes", 2_000_000_000); // maximum stream size: 2GB Default: not set.
        arguments.put("x-max-age", "7D"); // valid units: Y, M, D, h, m, s Default: not set.
        arguments.put("x-stream-max-segment-size-bytes", 100_000_000); // size of segment files: 100 MB
        arguments.put("x-initial-cluster-size", 2);
        ch.queueDelete(queue);
        ch.queueDeclare(queue, true, // durable
                false, false, // not exclusive, not auto-delete
                arguments);
        ch.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, false, false, false, null);
        ch.queueBind(queue, exchange, "tests");

        ch.confirmSelect();

        ConfirmCallback okConfirms = (sequenceNumber, multiple) -> {
            System.out.println("broker ack");
        };
        ConfirmCallback errConfirms = (sequenceNumber, multiple) -> {
            System.out.println("broker nack");
        };
        ch.addConfirmListener(okConfirms, errConfirms);

        for(int i = 0; i < 10; i++)
            ch.basicPublish(exchange, "tests", null, (i+":minha mensagem").getBytes());

        Thread.sleep(500);

        DeliverCallback deliverCallback = (consumerTag, message) -> {
            String msg = new String(message.getBody(), "UTF-8");
            System.out.format("o consumidor recebeu do broker: '%s'\n", msg);
            ch.basicNack(message.getEnvelope().getDeliveryTag(), false, false);
        };
        CancelCallback cancelCallback = (consumerTag) -> {
            System.out.println("consumer cancelado");
        };
        ch.basicQos(3);
        //Collections.singletonMap("x-stream-offset", "first")
        //Collections.singletonMap("x-stream-offset", "last")
        //Date timestamp = new Date(System.currentTimeMillis() - 60 * 60 * 1_000)
        //Collections.singletonMap("x-stream-offset", timestamp)
        ch.basicConsume(queue, false, Collections.singletonMap("x-stream-offset", 6), deliverCallback, cancelCallback);
        
        Thread.sleep(500);
        //ch.queueDelete(queue);
        //ch.exchangeDelete(exchange);
    }

}
