package br.com.labs.rabbit;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

//One of the main goals of lazy queues is to be able to support very long queues (many millions of messages).
class LazyTest {

    static Connection connection;
    static String queue = "lazy-queue";
    
    @BeforeAll
    static void setUp() throws IOException, TimeoutException, InterruptedException {
        connection = Builder.createAdmConnection();
    }
    
    @Test
    void load_queue() throws IOException, InterruptedException {
        Builder.execRabbitmqctl("""
        clear_policy Lazy
        """);
        
        Channel ch = connection.createChannel();
        ch.queueDelete(queue);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-expires", 90000);
        ch.queueDeclare(queue, false, false, false, args);
        
        long start = System.nanoTime();
        for(int i = 1; i <= 600000; i++) {
            String msg = "minha mensagem %d".formatted(i);
            ch.basicPublish("",queue, null, msg.getBytes());
        }
        Thread.sleep(150);
        //visualizar no Grafana o uso de memória

        long end = System.nanoTime();
        System.out.format("ClassicQueue - Tempo decorrido %d ms%n", Duration.ofNanos(end - start).toMillis());
        
    }
    
	@Test
	void load_lazy_queue() throws IOException, InterruptedException {
        Builder.execRabbitmqctl("""
        set_policy Lazy ^%s$ {"queue-mode":"lazy"} --apply-to queues
        """.formatted(queue));
	    
        Channel ch = connection.createChannel();
        ch.queueDelete(queue);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-expires", 90000);
        ch.queueDeclare(queue, false, false, false, args);
        
        long start = System.nanoTime();
        for(int i = 1; i <= 600000; i++) {
            String msg = "minha mensagem %d".formatted(i);
            ch.basicPublish("",queue, null, msg.getBytes());
        }
        Thread.sleep(150);
        //visualizar no Grafana o uso de memória

        long end = System.nanoTime();
        System.out.format("LazyQueue - Tempo decorrido %d ms%n", Duration.ofNanos(end - start).toMillis());
        
	}
	
    @Test
    void load_quorum_lazy_queue() throws IOException, InterruptedException {
        Builder.execRabbitmqctl("""
        set_policy Lazy ^%s$ {"queue-mode":"lazy"} --apply-to queues
        """.formatted(queue));
        
        Channel ch = connection.createChannel();
        ch.queueDelete(queue);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-expires", 90000);
        args.put("x-queue-type", "quorum");
        ch.queueDeclare(queue, true, false, false, args);
        
        long start = System.nanoTime();
        for(int i = 1; i <= 600000; i++) {
            String msg = "minha mensagem %d".formatted(i);
            ch.basicPublish("",queue, null, msg.getBytes());
        }
        Thread.sleep(150);
        //visualizar no Grafana o uso de memória

        long end = System.nanoTime();
        System.out.format("Quorum LazyQueue - Tempo decorrido %d ms%n", Duration.ofNanos(end - start).toMillis());
        
    }
}
