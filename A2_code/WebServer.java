

/**
 * WebServer Class
 * 
 */

import java.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
	

public class WebServer extends Thread {
	
	public final int POOL_SIZE = 8;
	public int PORT_NUM;
	public ServerSocket serverSocket;
	private ExecutorService executor; 
	private volatile boolean shutdown = false;
	
	

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
				PORT_NUM = port;
				executor = Executors.newFixedThreadPool(POOL_SIZE);
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
			System.out.println("Error opening server socket " + e);
		}		
		
		while (!shutdown) 
		{
			//Socket clientSocket  = new Socket();
			try 
			{
				try 
				{ 
					//clientSocket = serverSocket.accept();
					executor.execute(new Worker(serverSocket.accept()));
					
				}
				catch (SocketTimeoutException e) { 
					/* do nothing, allows process to check the shutdown flag*/	
					} 
			}
			catch (IOException e) 
			{ 
				System.out.println("Server cannot establish a connection with client " + e);
			}		
		}
		
	}
		
    /**
     * Signals the server to shutdown.
	 *
     */
	public void shutdown() {
		shutdown = true;
		try {
			// do not accept any new tasks
			executor.shutdown();
			// wait 5 seconds for existing tasks to terminate
			if (!executor.awaitTermination(5, TimeUnit.SECONDS)) 
			{
				executor.shutdownNow(); // cancel currently executing tasks
			}
		} 
		catch (InterruptedException e) 
		{
			// cancel currently executing tasks
			executor.shutdownNow();
		}
		
		try {
			serverSocket.close(); 
		}
		catch (IOException e) { 
			System.out.println("Server socket didn't close properly " + e); }
		}

	
	
	
	/**
	 * A simple driver.
	 */
	public static void main(String[] args) {
		int serverPort = 3333;

		// parse command line args
		if (args.length == 1) {
			serverPort = Integer.parseInt(args[0]);
		}
		
		if (args.length >= 2) {
			System.out.println("wrong number of arguments");
			System.out.println("usage: WebServer <port>");
			System.exit(0);
		}
		
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
