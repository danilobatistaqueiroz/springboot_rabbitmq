package br.com.labs.rabbit;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

class Cancel {
    int value = 0;
}
class ConsumerCancelTest {

    static Connection connection;
    static String queue = "minha-queue";
    
    static Cancel cancels = new Cancel();
    
    @BeforeAll
    static void setUp() throws IOException, TimeoutException, InterruptedException {
        connection = Builder.createAdmConnection();
        cancels = new Cancel();
    }
    @AfterAll
    static void setDown() {
        Assert.assertEquals(1, cancels.value);
    }
    
	@Test
	void broker_canceling() throws IOException, InterruptedException, TimeoutException {
        Channel ch = connection.createChannel();
        
        ch.queueDelete(queue);
        ch.queueDeclare(queue,false,false,false,null);
        
        ch.basicPublish("",queue, null, "minha mensagem".getBytes());
        
        var deliveries = new Object() { int value = 0; };
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            deliveries.value++;
            try {Thread.sleep(50);} catch (InterruptedException e) {}
            System.out.println("o consumidor recebeu do broker a mensagem");
        };
        CancelCallback cancelCallback = (consumerTag) -> {
            cancels.value++;
            System.out.println("o broker cancelou o canal:"+consumerTag);
            //se aumentar o time para Thread.sleep para 600000 e entrar no management UI e deletar a queue, será disparado esse callback
            //esse callback também será chamado após o método ser encerrado
        };
        
        ch.basicConsume(queue, false, "consumer-tag", deliverCallback, cancelCallback);
        //ch.basicCancel("consumer-tag");
        ch.basicPublish("",queue, null, "minha mensagem".getBytes());

        Thread.sleep(1000);
        Assert.assertEquals(2, deliveries.value);
        Assert.assertEquals(0, cancels.value);
        ch.queueDelete(queue);
	}
	
    @Test
    void client_canceling() throws IOException, InterruptedException {
        Channel ch = connection.createChannel();
        
        ch.queueDelete(queue);
        ch.queueDeclare(queue,false,false,false,null);
        
        ch.basicPublish("",queue, null, "minha mensagem".getBytes());
        
        var deliveries = new Object() { int value = 0; };
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {Thread.sleep(50);} catch (InterruptedException e) {}
            System.out.println("o consumidor recebeu do broker a mensagem");
            deliveries.value++;
        };
        var cancels = new Object() { int value = 0; };
        CancelCallback cancelCallback = (consumerTag) -> {
            System.out.println("o broker cancelou o canal do consumer:"+consumerTag);
            cancels.value++;
        };
        
        ch.basicConsume(queue, false, "consumer-tag-cancel", deliverCallback, cancelCallback);
        ch.basicCancel("consumer-tag-cancel");
        ch.basicPublish("",queue, null, "minha mensagem".getBytes());

        Thread.sleep(1000);
        Assert.assertEquals(1, deliveries.value);
        Assert.assertEquals(0, cancels.value);
        ch.queueDelete(queue);
    }

}
