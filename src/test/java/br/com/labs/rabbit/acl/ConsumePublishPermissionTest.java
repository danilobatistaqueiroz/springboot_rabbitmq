package br.com.labs.rabbit.acl;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;

import br.com.labs.rabbit.Builder;

class ConsumePublishPermissionTest {
	
	@Test
	void cannot_publish_message() throws InterruptedException, TimeoutException, IOException {
		Builder.execRabbitmqctl("""
		rabbitmqctl delete_user readeruser
		""");
		Thread.sleep(50);
		Builder.execRabbitmqctl("""
		add_user readeruser $Yab*JMb0654ak@pq#&+
		""");
		Builder.execRabbitmqctl("""
		set_permissions -p / readeruser ^$ ^$ ^minha_queue$
		""");
		
		Thread.sleep(100);
		
		ConnectionFactory cfreader = new ConnectionFactory();
		cfreader.setHost("localhost");
		cfreader.setUsername("readeruser");
		cfreader.setPassword("$Yab*JMb0654ak@pq#&+");
		Connection readerConnection = cfreader.newConnection();
		Channel readerChannel = readerConnection.createChannel();
		
		Channel configurerChannel = createConfigurerUser();
		configurerChannel.exchangeDeclare("minha_exchange", BuiltinExchangeType.DIRECT, false, false, false, null);
		configurerChannel.queueDelete("minha_queue");
		configurerChannel.queueDeclare("minha_queue", false, false, true, null);
		configurerChannel.queueBind("minha_queue", "minha_exchange", "abc");
		
		String sended = "minha mensagem";
		readerChannel.basicPublish("minha_exchange", "abc", null, sended.getBytes());
		Thread.sleep(100);
		Assert.assertEquals("fechou por que não tem permissão de publicar",false, readerChannel.isOpen());
		
		readerChannel = readerConnection.openChannel().get();

		long qtdmessages = readerChannel.messageCount("minha_queue");
		Assert.assertEquals(0, qtdmessages);
		
		Builder.execRabbitmqctl("""
		delete_user readeruser
		""");
		configurerChannel.queueDelete("minha_queue");
		configurerChannel.exchangeDelete("minha_exchange");
		Builder.execRabbitmqctl("""
		delete_user configureruser
		""");
	}
	
	@Test
	void can_publish_message() throws InterruptedException, TimeoutException, IOException {
		Builder.execRabbitmqctl("""
		rabbitmqctl delete_user writeruser
		""");
		Thread.sleep(50);
		Builder.execRabbitmqctl("""
		add_user writeruser $Yab*JMb0654ak@pq#&+
		""");
		Builder.execRabbitmqctl("""
		set_permissions -p / writeruser ^$ ^minha_exchange.*$ ^$
		""");
		
		Thread.sleep(100);
		
		ConnectionFactory cfwriter = new ConnectionFactory();
		cfwriter.setHost("localhost");
		cfwriter.setUsername("writeruser");
		cfwriter.setPassword("$Yab*JMb0654ak@pq#&+");
		Connection writerConnection = cfwriter.newConnection();
		Channel writerChannel = writerConnection.createChannel();
		
		Channel configurerChannel = createConfigurerUser();
		configurerChannel.exchangeDeclare("minha_exchange", BuiltinExchangeType.DIRECT, false, false, false, null);
		configurerChannel.queueDelete("minha_queue");
		configurerChannel.queueDeclare("minha_queue", false, false, false, null);
		configurerChannel.queueBind("minha_queue", "minha_exchange", "abc");
		
		String sended = "minha mensagem";
		writerChannel.basicPublish("minha_exchange", "abc", null, sended.getBytes());
		
		long qtdmessages = writerChannel.messageCount("minha_queue");
		Assert.assertEquals(1, qtdmessages);
		
		Builder.execRabbitmqctl("""
		delete_user writeruser
		""");
		configurerChannel.queueDelete("minha_queue");
		configurerChannel.exchangeDelete("minha_exchange");
		Builder.execRabbitmqctl("""
		delete_user configureruser
		""");
	}
	
	Channel createConfigurerUser() throws IOException, InterruptedException, TimeoutException {
		Builder.execRabbitmqctl("""
		add_user configureruser $Yab*JMb0654ak@pq#&+
		""");
		Builder.execRabbitmqctl("""
		set_permissions -p / configureruser ^minha_.*$ ^minha_queue$ ^minha_exchange$
		""");
		ConnectionFactory cfconfigurer = new ConnectionFactory();
		cfconfigurer.setHost("localhost");
		cfconfigurer.setUsername("configureruser");
		cfconfigurer.setPassword("$Yab*JMb0654ak@pq#&+");
		Connection configurerConnection = cfconfigurer.newConnection();
		Channel configurerChannel = configurerConnection.createChannel();
		return configurerChannel;
	}

	@Test
	void cannot_consume_message() throws IOException, InterruptedException, TimeoutException {
		Builder.execRabbitmqctl("""
		rabbitmqctl delete_user writeruser
		""");
		Builder.execRabbitmqctl("""
		rabbitmqctl delete_user readeruser
		""");
		Builder.execRabbitmqctl("""
		rabbitmqctl delete_user readerqueue
		""");
		Thread.sleep(50);
		Builder.execRabbitmqctl("""
		add_user writeruser $Yab*JMb0654ak@pq#&+
		""");
		Builder.execRabbitmqctl("""
		set_permissions -p / writeruser ^$ ^minha_exchange$ ^$
		""");
		
		Builder.execRabbitmqctl("""
		add_user readeruser $Yab*JMb0654ak@pq#&+
		""");
		Builder.execRabbitmqctl("""
		set_permissions -p / readeruser ^$ ^$ ^minha_exchange$
		""");
		
		Builder.execRabbitmqctl("""
		add_user readerqueue $Yab*JMb0654ak@pq#&+
		""");
		Builder.execRabbitmqctl("""
		set_permissions -p / readerqueue ^$ ^$ ^minha_queue$
		""");
		
		Thread.sleep(100);
		
		ConnectionFactory cfwriter = new ConnectionFactory();
		cfwriter.setHost("localhost");
		cfwriter.setUsername("writeruser");
		cfwriter.setPassword("$Yab*JMb0654ak@pq#&+");
		Connection writerConnection = cfwriter.newConnection();
		final Channel writerChannel = writerConnection.createChannel();
		
		ConnectionFactory cfreader = new ConnectionFactory();
		cfreader.setHost("localhost");
		cfreader.setUsername("readeruser");
		cfreader.setPassword("$Yab*JMb0654ak@pq#&+");
		Connection readerConnection = cfreader.newConnection();
		final Channel readerChannel = readerConnection.createChannel();
		
		ConnectionFactory cfqueue = new ConnectionFactory();
		cfqueue.setHost("localhost");
		cfqueue.setUsername("readerqueue");
		cfqueue.setPassword("$Yab*JMb0654ak@pq#&+");
		Connection queueConnection = cfqueue.newConnection();
		final Channel queueChannel = queueConnection.createChannel();
		
		Channel configurerChannel = createConfigurerUser();
		configurerChannel.exchangeDeclare("minha_exchange", BuiltinExchangeType.DIRECT, false, true, false, null);
		configurerChannel.queueDelete("minha_queue");
		configurerChannel.queueDeclare("minha_queue", false, false, true, null);
		configurerChannel.queueBind("minha_queue", "minha_exchange", "abc");
		
		String sended = "minha mensagem";
		writerChannel.basicPublish("minha_exchange", "abc", null, sended.getBytes());
		Thread.sleep(200);
		queueChannel.basicConsume("minha_queue", true, "a-consumer-tag", new DefaultConsumer(queueChannel) {});
		Assertions.assertThrows(IOException.class, () -> {
			readerChannel.basicConsume("minha_queue", true, "a-consumer-tag", new DefaultConsumer(queueChannel) {});
		}, "não tem permissão de leitura na queue");

		Builder.execRabbitmqctl("""
		delete_user writeruser
		""");
		Builder.execRabbitmqctl("""
		delete_user readeruser
		""");
		Builder.execRabbitmqctl("""
		delete_user readerqueue
		""");
		configurerChannel.queueDelete("minha_queue");
		configurerChannel.exchangeDelete("minha_exchange");
		Builder.execRabbitmqctl("""
		delete_user configureruser
		""");
	}

}
