

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
	String METHOD = "GET"; 
	String PROTOCOL = "HTTP/1.1";
	String CONNECTION_CLOSED = "Connection: close\r\n";
	
	private volatile boolean shutdown = false;
	private ServerSocket serverSocket;
	
	PrintWriter out;
	BufferedReader in;
	
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
			clientSocket = serverSocket.accept();
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
						// send server response + "Connection:close" at end
						// tranfer file over connection
						// close connection
						System.out.println(errorResponse(code));
					}
					else 
					{
						// return error
						code = "404 Not Found";
					}
				}
				
				
				// send error for bad request (400) or not found (404)
				// Date\n Server\n connection:close
				System.out.println(errorResponse(code));
				
				// clean up (close sockets, threads, etc.)
				in.close();
				out.close();
				serverSocket.close();
				
			}
		}
		catch (IOException e) 
		{ 
			// do nothing, allows process to check the shutdown flag	
		}	
		

		
		
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
	
	public String goodResponse(String code) {
		String response = 
				METHOD + " " + code + "\r\n" + 
				getDate() +
				getServer() + 
				// last-mod
				// content-length
				// content-type
				CONNECTION_CLOSED;
		return response;
	}
	
	/**
	 * 
	 * @param code
	 * @return
	 */
	public String errorResponse(String code) {
		String response = METHOD + " " + code + "\r\n" + 
						  getDate() + 
						  getServer() + 
						  CONNECTION_CLOSED;
		return response;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getDate () {
		String current = "";
		DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss zzz");
		Date date = new Date();
		current =  dateFormat.format(date);
		return current + "\r\n";
	}
	
	/**
	 * Code taken from stack overflow: 
	 * https://stackoverflow.com/questions/7883542/getting-the-computer-name-in-java
	 * @return
	 */
	public String getServer() {
		String hostname = "";
		try
		{
		    InetAddress addr;
		    addr = InetAddress.getLocalHost();
		    hostname = addr.getHostName();
		}
		catch (UnknownHostException ex)
		{
		    System.out.println("Hostname can not be resolved");
		}
		return hostname + "\r\n";
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
