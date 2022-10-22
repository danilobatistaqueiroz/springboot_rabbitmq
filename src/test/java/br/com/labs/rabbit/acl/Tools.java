package br.com.labs.rabbit.acl;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Tools {
	static Connection createDirectorshipConnection() throws IOException, TimeoutException {
		ConnectionFactory cf = new ConnectionFactory();
		cf.setHost("localhost");
		cf.setUsername("directorship");
		cf.setPassword("Tbry19*ji5Ct3a&avRx");
		return cf.newConnection();
	}
	static Connection createFinancialConnection() throws IOException, TimeoutException {
		ConnectionFactory cf = new ConnectionFactory();
		cf.setHost("localhost");
		cf.setUsername("financial");
		cf.setPassword("@1ab5JM%0981aklpq%3#");
		return cf.newConnection();
	}
	static Connection createComercialConnection() throws IOException, TimeoutException {
		ConnectionFactory cf = new ConnectionFactory();
		cf.setHost("localhost");
		cf.setUsername("comercial");
		cf.setPassword("$Yab*JMb0654ak@pq#&+");
		return cf.newConnection();
	}
}
