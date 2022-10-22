package br.com.labs.rabbit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Builder {
	
	public static Connection createAdmConnection() throws IOException, TimeoutException {
        ConnectionFactory cf = new ConnectionFactory();
        cf.setHost("localhost");
        cf.setUsername("rabbit_admin");
        cf.setPassword(".123-321.");
        return cf.newConnection();
    }
	
	public static Map<String, Object> queueExpires() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-expires", 4000); //é possível expirar uma queue após um tempo sem uso, sem um client conectar, após a expiração, a queue é dropada
        return args;
	}
	
	public static String execRabbitmqctl(String command) throws IOException, InterruptedException {
		return execRabbitmqctl(command,false);
	}
	
	public static String execRabbitmqctl(String command, Boolean debug) throws IOException, InterruptedException {
	    String fullcommand = "docker exec rabbitmq2 %s %s".formatted("rabbitmqctl",command);
	    return execRabbitmqExec(fullcommand, debug);
	}
	
    public static String execRabbitmqadmin(String command) throws IOException, InterruptedException {
        return execRabbitmqadmin(command,false);
    }
	
    public static String execRabbitmqadmin(String command, Boolean debug) throws IOException, InterruptedException {
        String fullcommand = "docker exec rabbitmq2 %s %s".formatted("rabbitmqadmin",command);
        return execRabbitmqExec(fullcommand, debug);
    }
	
	public static String execRabbitmqExec(String fullcommand, Boolean debug) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(fullcommand);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String str = "";
        String line = "";
        while ((line = reader.readLine()) != null) {
            str += line;
        }
        if (debug) {
        	System.out.println(fullcommand);
        	System.out.println(str);
	        BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
	        line = "";
	        str = "";
	        while ((line = error.readLine()) != null) {
	            str += line;
	        }
	        System.err.println(str);
	        int exitCode = process.waitFor();
	        System.out.println("\nExited with error code : " + exitCode);
        }
      	return str;
	}
	
}
