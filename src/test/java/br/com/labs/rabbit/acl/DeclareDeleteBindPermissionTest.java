package br.com.labs.rabbit.acl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import br.com.labs.rabbit.Builder;

class DeclareDeleteBindPermissionTest {

	static Connection crwConnection;
	static Connection rwConnection;
	static Channel rwChannel;
	static Channel crwChannel;

	public static void setUpUsers() throws IOException, InterruptedException {
		System.out.println("setUp");
		Builder.execRabbitmqctl("""
		add_user directorship Tbry19*ji5Ct3a&avRx
		""");
		Builder.execRabbitmqctl("""
		add_user financial @1ab5JM%0981aklpq%3#
		""");
		Builder.execRabbitmqctl("""
		add_user comercial $Yab*JMb0654ak@pq#&+
		""");
		//Builder.execRabbitmqctl("""
		//set_user_tags financial administrator
		//monitoring management policymaker
		//""");
		Builder.execRabbitmqctl("""
		set_permissions -p / directorship ^minha_.* ^minha_.* ^minha_.*
		""");
		Builder.execRabbitmqctl("""
		set_permissions -p / financial ^xx_.* ^minha_.* ^minha_.*
		""");
		Builder.execRabbitmqctl("""
		set_permissions -p / comercial ^xx_.* ^xx_.* ^minha_.*
		""");
	}

	public static void setDownUsers() throws IOException, InterruptedException {
		System.out.println("setDown");
		Builder.execRabbitmqctl("""
		delete_user directorship
		""");
		Builder.execRabbitmqctl("""
		delete_user financial
		""");
		Builder.execRabbitmqctl("""
		delete_user comercial
		""");
	}

	@BeforeAll
	static void setUp() throws IOException, TimeoutException, InterruptedException {
		setUpUsers();
		crwConnection = Tools.createDirectorshipConnection();
		rwConnection = Tools.createFinancialConnection();
    }
	@AfterAll
	static void setDown() throws IOException, InterruptedException {
		setDownUsers();
	}
	
	
	void cannot_declare() throws IOException {
		final Channel rwChannel1 = rwConnection.createChannel();
		Assertions.assertThrows(IOException.class, () -> {
			rwChannel1.exchangeDeclare("minha_exchange", BuiltinExchangeType.DIRECT, false, true, false, null);
		});
		final Channel rwChannel2 = rwConnection.createChannel();
		Assertions.assertThrows(IOException.class, () -> {
			rwChannel2.queueDeclare("minha_queue", false, true, false, null);
		});
	}
	
	void can_declare() throws IOException {
		final Channel crwChannel1 = crwConnection.createChannel();
		crwChannel1.exchangeDeclare("minha_exchange", BuiltinExchangeType.DIRECT, false, true, false, null);
		crwChannel1.queueDeclare("minha_queue", false, true, false, null);
	}
	
	void cannot_exchange_delete() throws IOException {
		final Channel crwChannel1 = crwConnection.createChannel();
		crwChannel1.exchangeDeclare("minha_exchange", BuiltinExchangeType.DIRECT, false, true, false, null);
		crwChannel1.queueDeclare("minha_queue", false, true, false, null);
		
		final Channel rwChannel1 = rwConnection.createChannel();
		Assertions.assertThrows(IOException.class, () -> {
			rwChannel1.exchangeDelete("minha_exchange");
		});
		final Channel rwChannel2 = rwConnection.createChannel();
		Assertions.assertThrows(IOException.class, () -> {
			rwChannel2.queueDelete("minha_queue");
		});
	}
	void can_exchange_delete() throws IOException {
		final Channel crwChannel1 = crwConnection.createChannel();
		crwChannel1.exchangeDeclare("minha_exchange", BuiltinExchangeType.DIRECT, false, true, false, null);
		crwChannel1.queueDeclare("minha_queue", false, true, false, null);
	
		crwChannel1.exchangeDelete("minha_exchange");
		crwChannel1.queueDelete("minha_queue");		
	}
	
	@Test
	void ae_declare() throws IOException, InterruptedException, TimeoutException {

		Builder.execRabbitmqctl("""
		add_user specialuser $Yab*JMb0654ak@pq#&+
		""");
		Builder.execRabbitmqctl("""
		set_permissions -p / specialuser ^(minha_exchange|minha_ae_exch|minha_queue|minha_ae_queue)$ ^(minha_ae_exch|minha_queue|minha_ae_queue)$ ^(minha_exchange|minha_ae_exch)$
		""");
		
		Thread.sleep(100);
		
		ConnectionFactory cf = new ConnectionFactory();
		cf.setHost("localhost");
		cf.setUsername("specialuser");
		cf.setPassword("$Yab*JMb0654ak@pq#&+");
		Connection suConnection = cf.newConnection();
		final Channel suChannel = suConnection.createChannel();
		
		// para criar uma alternate exchange, é necessário as seguintes permissões:
		//w = ae_exchange, ae_queue, queue
		//r = exchange, ae_exchange
		//c = exchange, ae_exchange, queue, ae_queue
		
		//bind exige permissão para leitura na origem e escrita no destino
		//toda entidade declarada exige permissão de configuração
		//a propriedade alternate-exchange exige direito de leitura para a exchange origem e escrita para a alternate exchange
		suChannel.exchangeDeclare("minha_ae_exch", BuiltinExchangeType.FANOUT, false, true, false, null);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("alternate-exchange", "minha_ae_exch");
		suChannel.exchangeDeclare("minha_exchange", "direct", false, true, false, args);
		suChannel.queueDeclare("minha_queue", false, true, true, null);
		suChannel.queueBind("minha_queue", "minha_exchange", "tests");
		suChannel.queueDeclare("minha_ae_queue", false, true, true, null);

		final Channel suChannel3 = suConnection.createChannel();
		suChannel3.queueBind("minha_ae_queue", "minha_ae_exch", "");
		
		Builder.execRabbitmqctl("""
		delete_user specialuser
		""");
	}
	
	@Test
	void dlx_declare() throws IOException, InterruptedException, TimeoutException {
		Builder.execRabbitmqctl("""
		add_user specialuser $Yab*JMb0654ak@pq#&+
		""");
		Builder.execRabbitmqctl("""
		set_permissions -p / specialuser ^(minha_exchange|minha_dlx_exch|minha_queue|minha_dlx_queue)$ ^(minha_dlx_exch|minha_queue|minha_dlx_queue)$ ^(minha_exchange|minha_queue|minha_dlx_exch)$
		""");
		
		//para criar uma dead letter exchange com dead letter queue:
		//precisa de permissão de configuração nas 4 entidades
		//permissão de leitura na queue que receberá o atributo e permissão de escrita na dead letter exchange informada no atributo
		//ao usar o queueBind -> permissão de escrita na dead letter queue, e leitura na dead letter exchange
		//ao usar o queueBind -> leitura na exchange e escrita na queue
		
		Thread.sleep(300);
		
		ConnectionFactory cf = new ConnectionFactory();
		cf.setHost("localhost");
		cf.setUsername("specialuser");
		cf.setPassword("$Yab*JMb0654ak@pq#&+");
		Connection suConnection = cf.newConnection();
		final Channel suChannel = suConnection.createChannel();
        
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("x-dead-letter-exchange", "minha_dlx_exch");
        suChannel.queueDeclare("minha_queue", false, true, true, args);
        boolean durable = false, autoDelete = true, internal = false; //durable exchange survive broker restart //autoDelete after all queues and exchanges binded are unbinded is deleted // internal only accept bind to another exchange
        suChannel.exchangeDeclare("minha_exchange", BuiltinExchangeType.DIRECT,durable,autoDelete,internal,null);
        suChannel.exchangeDeclare("minha_dlx_exch", BuiltinExchangeType.DIRECT,durable,autoDelete,internal,null);
        suChannel.queueDeclare("minha_dlx_queue", false, true, true, null);
        suChannel.queueBind("minha_dlx_queue", "minha_dlx_exch", "tests");
        suChannel.queueBind("minha_queue", "minha_exchange", "tests");

		Builder.execRabbitmqctl("""
		delete_user specialuser
		""");
	}
	
}
