package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{
	private ArrayList<ConnectionHandler> connections;
	private ServerSocket server;
	private boolean done;
	private ExecutorService pool;
	public Server() {
		connections = new ArrayList<>();
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			
			 server = new ServerSocket(9999);
			 pool = Executors.newCachedThreadPool();
			 while(!done) {
			Socket client = server.accept();
			ConnectionHandler handler = new ConnectionHandler(client);
			connections.add(handler);
			pool.execute(handler);
			 } 
		}
			 catch (Exception e) {
			// TODO Auto-generated catch block
		   // TODO: handle
				 shutdown();
		}
	}
	public void broadcast(String message) {
		for(ConnectionHandler ch : connections) {
			if(ch!=null) {
				ch.sendMessage(message);
			}
		}
	}
	public void shutdown()  {
		done = true;
		try {
		if(!server.isClosed()) {
			server.close();
		}
		for(ConnectionHandler ch: connections) {
			ch.shutdown();
		}
		}
		catch(IOException e) {
			//ignore
		}
		
	}
	class ConnectionHandler implements Runnable{
		private Socket client;
		private BufferedReader in;
		private PrintWriter out;
		private String nickname;
		public ConnectionHandler(Socket client) {
			this.client = client;  
		}
		public void shutdown() {
			// TODO Auto-generated method stub
			try {
				in.close();
				out.close();
			if(!client.isClosed()) {
				client.close();
				}
			}
			catch(IOException e) {
				//ignore
			}
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				out = new PrintWriter(client.getOutputStream(),true);
				in  = new BufferedReader(new InputStreamReader(client.getInputStream()));
				out.println("Enter your name: ");
				nickname = in.readLine();
				System.out.println(nickname+" connected");
				broadcast(nickname+" joined the chat");
				String message;
				while((message = in.readLine()) !=null) {
					if(message.startsWith("/nick ")) {
						String[] messageSplit = message.split(" ", 2);
						if(messageSplit.length == 2) {
							broadcast(nickname+" changed thier username to "+messageSplit[1]);
							System.out.println(nickname+" changed thier username to "+messageSplit[1]);
							nickname = messageSplit[1];
							out.println("Successfully changed username to "+nickname);
						}
						else {
							out.println("No username is provided!");
						}
					}
					else if(message.startsWith("/quit")) {
						//TODO: quit
						broadcast(nickname+" left the chat");
						shutdown();
					}
					else {
						broadcast(nickname+": "+message);
					}
				}
			}
			catch(IOException e) {
				shutdown();
			}
		}
		public void sendMessage(String message) {
			out.println(message);
		}
		
	}
		public static void main(String[] args) {
			// TODO Auto-generated method stub
			Client server = new Client();
			server.run();
		}
	
}
