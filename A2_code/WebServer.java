

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
import java.net.SocketException;
import java.net.SocketTimeoutException;
	

public class WebServer extends Thread {
	String METHOD = "GET"; 
	String PROTOCOL = "HTTP/1.1";
	int POOL_SIZE = 8;
	ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
	private volatile boolean shutdown = false;
	private ServerSocket serverSocket;
	PrintWriter out;
	BufferedReader in;

    /**
     * Default constructor to initialize the web server
     * 
     * @param port 	The server port at which the web server listens > 1024
     * 
     */
	public WebServer(int port) {
		// initialization
		if (port > 1024 && port < 65536 ) 
		{
			try 
			{
				// Initialize and open a server socket that listens to port number given
				this.serverSocket = new ServerSocket(port);
				System.out.println("Started server on port " + port);
			} 
			catch (IOException e) { System.out.println("Error opening socket: " + e);}
				
		}	
	}

	
    /**
     * The main loop of the web server
     *   Opens a server socket at the specified server port
	 *   Remains in listening mode until shutdown signal
	 * 
     */
	public void run() {
		
		Socket clientSocket = null;
		try 
		{
			serverSocket.setSoTimeout(1000);
			
			while (!shutdown) 
			{
				// accept new connection
				clientSocket = serverSocket.accept();
				// create worker thread to handle new connection
				//Thread thread = new WebServer(clientSocket.getPort());
				//thread.start();
			}
			
			out = new PrintWriter (clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String inputLine, outputLine = "Hello";
			String [] wholeRequest, reqLine;
			out.println(outputLine);
				
			while ((inputLine = in.readLine()) != null)
			{
				wholeRequest = inputLine.split("\r\n");
				reqLine = wholeRequest[0].split(" ");
				// get a status code
				String code = isValidRequest(reqLine);
				if (code.equals("200 OK"))
				{
					// check if have the file
					File file = new File(reqLine[1]);
					if(file.isDirectory()) 
					{
						// tranfer file over connection
						// close connection
					}
					else 
					{
						// return error
						code = "404 Not Found";
					}
				}
				else
				{
					// send error for bad request (400)
					System.out.println(code);
				}
				
			}
			
			
		}
		catch (IOException e) 
		{ 
			// do nothing, allows process to check the shutdown flag	
		}	
		// clean up (close sockets, threads, etc.)
		try {
			in.close();
			out.close();
			serverSocket.close();
		} catch (IOException e) { e.printStackTrace(); }
		
		
	}
	
	
	
    /**
     * Signals the server to shutdown.
	 *
     */
	public void shutdown() {
		shutdown = true;
	}

	/**
	 *  Checks if request was sent properly
	 */
	public String isValidRequest(String [] reqLine) {
		String code = "200 OK";
		
		if (reqLine.length != 3)
			code = "400 Bad Request";
		else if (!reqLine[0].equals(METHOD))
			code = "400 Bad Request";
		else if (!reqLine[2].equals(PROTOCOL))
			code = "400 Bad Request";
		
		return code;
	}
	
	
	
	
	/**
	 * A simple driver.
	 */
	public static void main(String[] args) {
		int serverPort = 2226;

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
