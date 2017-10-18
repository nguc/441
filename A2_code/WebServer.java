

/**
 * WebServer Class
 * 
 */

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
	

public class WebServer extends Thread {
	
	
	public int PORT_NUM;
	
	private volatile boolean shutdown = false;
	private ServerSocket serverSocket;
	
	
	
	
	//int POOL_SIZE = 8;
	//ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);

    /**
     * Default constructor to initialize the web server
     * 
     * @param port 	The server port at which the web server listens > 1024
     * 
     */
	public WebServer(int port) {
		// initialization
		try 
		{
			if (port > 1024 && port < 65536 ) 
			{
				// Initialize and open a server socket that listens to port number given
				PORT_NUM = port;
				System.out.println("Started server on port " + port);
			} 
		}
		catch (Exception e) { System.out.println("Error opening socket: " + e);}	
	}

	
    /**
     * The main loop of the web server
     *   Opens a server socket at the specified server port
	 *   Remains in listening mode until shutdown signal
	 * 
     */
	public void run() {

		try
		{
			this.serverSocket = new ServerSocket(PORT_NUM);
			serverSocket.setSoTimeout(1000);
		}
		catch (IOException e)
		{
			System.out.println("Error opening server socket");
		}		
		
		while (!shutdown) 
		{
			Socket clientSocket = new Socket();
			try 
			{
			// accept new connection
				try { clientSocket = serverSocket.accept();}
				catch (SocketTimeoutException e){} 
			// create worker thread to handle new connection
			Thread thread = new Thread((new Worker(clientSocket)));
			thread.start();
			}
			catch (IOException e) 
			{ 
				// do nothing, allows process to check the shutdown flag	
			}		
		}
			
			
	}
		
	
	
	
    /**
     * Signals the server to shutdown.
	 *
     */
	public void shutdown() {
		shutdown = true;
		try
		{
			this.serverSocket.close();
		}
		catch (Exception e){
			
		}
		
	}

	
	
	
	/**
	 * A simple driver.
	 */
	public static void main(String[] args) {
		int serverPort = 2227;

		// parse command line args
		if (args.length == 1) {
			serverPort = Integer.parseInt(args[0]);
		}
		
		if (args.length >= 2) {
			System.out.println("wrong number of arguments");
			System.out.println("usage: WebServer <port>");
			System.exit(0);
		}
		
		//System.out.println("starting the server on port " + serverPort);
		
		WebServer server = new WebServer(serverPort);
		
		server.start();
		System.out.println("server started. Type \"quit\" to stop");
		System.out.println(".....................................");

		Scanner keyboard = new Scanner(System.in);
		while ( !keyboard.next().equals("quit") );
		Thread t1 = server;
		System.out.println();
		System.out.println("shutting down the server...");
		server.shutdown();
		System.out.println("server stopped");
	}
	
}
