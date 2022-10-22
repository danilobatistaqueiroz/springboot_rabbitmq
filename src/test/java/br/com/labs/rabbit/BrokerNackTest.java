package br.com.labs.rabbit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

class BrokerNackTest {
	
	static Connection connection;
	static String queue = "queue-minha";
	static String exchange = "minha-exchange";
	
	@BeforeAll
	static void setUp() throws IOException, TimeoutException, InterruptedException {
		connection = Builder.createAdmConnection();
    }
	
	@Test
	void nack_length_limit_bytes() throws IOException, InterruptedException {
		Builder.execRabbitmqctl("""
		set_policy MaxLen ^%s$ {"max-length-bytes":1048,"overflow":"reject-publish"} --apply-to queues
		""".formatted(queue));
		
        Channel ch = connection.createChannel();

        ch.queueDelete(queue);
        ch.queueDeclare(queue, false, true, false, null);
        List<String> ack = new ArrayList<String>();
        
	    ch.confirmSelect();
        ch.addConfirmListener(
		    (seq,mult) -> {
		    	ack.add("ack");
		    }, 
		    (seq,mult) -> {
		    	ack.add("nack");
		    }
        );
        for(int i = 1; i <= 3; i++) {
	        ch.basicPublish("",queue, null, """
	        		   	minha mensagem enorme {contem bastante conteudo para estourar o limite em bytes mais rapido,
	        			vamos continuar com mais texto para a mensagem preencher bastante espaço no servidor
	        			vamos continuar com mais texto para a mensagem preencher bastante espaço no servidor
	        			vamos continuar com mais texto para a mensagem preencher bastante espaço no servidor
	        			vamos continuar com mais texto para a mensagem preencher bastante espaço no servidor
	    	""".getBytes());
	        Thread.sleep(150);
        }
        Thread.sleep(2000);

        Assert.assertEquals("ack, ack, nack",ack.toString().replace("[", "").replace("]", ""));
        
		Builder.execRabbitmqctl("""
		clear_policy MaxLen
		""");
	}
	
	@Test
	void nack_length_limit() throws IOException, InterruptedException {
		Builder.execRabbitmqctl("""
		set_policy MaxLen ^%s$ {"max-length":5,"overflow":"reject-publish"} --apply-to queues
		""".formatted(queue));
		
        Channel ch = connection.createChannel();

        ch.queueDelete(queue);
        ch.queueDeclare(queue, false, true, false, null);
        List<String> ack = new ArrayList<String>();
        
	    ch.confirmSelect();
        ch.addConfirmListener(
		    (seq,mult) -> {
		    	System.out.println(mult);
		    	ack.add("ack");
		    }, 
		    (seq,mult) -> {
		    	ack.add("nack");
		    }
        );
        for(int i = 1; i <= 6; i++) {
	        ch.basicPublish("",queue, null, "minha mensagem".getBytes());
	        Thread.sleep(150);
        }
        Thread.sleep(2000);

        Assert.assertEquals("ack, ack, ack, ack, ack, nack",ack.toString().replace("[", "").replace("]", ""));
        
		Builder.execRabbitmqctl("""
		clear_policy MaxLen
		""");
	}
	
	@Test
	void multiple_ack() throws IOException, InterruptedException {
		
        Channel ch = connection.createChannel();

        ch.queueDelete(queue);
        ch.queueDeclare(queue, false, true, false, null);
        List<String> multiples = new ArrayList<String>();
        
	    ch.confirmSelect();
        ch.addConfirmListener(
		    (seq,mult) -> {
		    	multiples.add(String.valueOf(mult));
		    }, 
		    (seq,mult) -> {
		    	multiples.add("nack");
		    }
        );
        for(int i = 1; i <= 6; i++) {
	        ch.basicPublish("",queue, null, "minha mensagem".getBytes());
	        Thread.sleep(150);
        }
        Thread.sleep(150);
        Assert.assertEquals("false, false, false, false, false, false",multiples.toString().replace("[", "").replace("]", ""));
        multiples.clear();
        
        for(int i = 1; i <= 6; i++) {
	        ch.basicPublish("",queue, null, "minha mensagem".getBytes());
        }
        Thread.sleep(150);
        //se a sequencia de basicPublish for rapida demais, o Broker responde uma vez só com multiple=true
        Assert.assertTrue(multiples.contains("true"));
        
		Builder.execRabbitmqctl("""
		clear_policy MaxLen
		""");
	}

}
