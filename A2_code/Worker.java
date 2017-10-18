import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Worker implements Runnable{
	public final String METHOD = "GET"; 
	public final String PROTOCOL = "HTTP/1.1";
	public final String CONNECTION_CLOSED = "Connection: close\r\n";
	public Socket socket;
	
	
	public Worker(Socket socket) {
		this.socket = socket;
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try
		{
			PrintWriter out = new PrintWriter (socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String inputLine, outputLine = "Hello";
			String [] wholeRequest, reqLine;
			out.println(outputLine);
				
			while ((inputLine = in.readLine()) != null)
			{
				System.out.println(inputLine);
				wholeRequest = inputLine.split("\r\n");
				reqLine = wholeRequest[0].split(" ");
				// get a status code
				String code = isValidRequest(reqLine);
				if (code.equals("200 OK"))
				{
					// check if have the file
					System.out.println("file: " + reqLine[1].substring(1));
					File file = new File(//current directory + reqLine[1]);
					if(file.exists()) 
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
				
				
			}
		}
		catch (Exception e){
			
		}
		
	}
	
	
	/**
	 *  Checks if request was sent properly
	 */
	public String isValidRequest(String [] reqLine) {
		String code = "200 OK";
		
		if (reqLine.length != 3)
			code = "400 Bad Request";
		else if (!reqLine[0].equals(METHOD) || !reqLine[2].equals(PROTOCOL))
			code = "400 Bad Request";
		
		return code;
	}
	
	public String goodResponse(String code) {
		String response = 
				PROTOCOL + " " + code + "\r\n" + 
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
		String response = PROTOCOL + " " + code + "\r\n" + 
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

}
