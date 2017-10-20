import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Worker implements Runnable{
	public final String METHOD = "GET"; 
	public final String PROTOCOL = "HTTP/1.0";
	public final String PROTOCOL_v11 = "HTTP/1.1";
	public final String CONNECTION_CLOSED = "Connection: close\r\n";
	public final String PATHNAME = System.getProperty("user.dir");
	public Socket cSocket;
	
	
	// Constructor: takes in the socket connected to the client 
	public Worker(Socket socket) {
		cSocket = socket;
	}
	
	/**
	 * Parses the request line, then sends the appropriate response. 
	 * Will also send the file data if request is OK and file exists in current directory
	 */
	@Override
	public void run() {
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
			String inputLine;
			String [] wholeRequest, reqLine;
				
			while ((inputLine = in.readLine()) != null)
			{
				System.out.println("Requested page: " + inputLine);
				wholeRequest = inputLine.split("\r\n");
				reqLine = wholeRequest[0].split(" ");
				
				String code = isValidRequest(reqLine);		// get a status code
				
				if (code.equals("200 OK"))
				{	
					String fileName = PATHNAME + reqLine[1];
					File file = new File(fileName);
					
					if(file.exists()) 
					{
						// transfer header and file over connection
						outputToClient(goodResponse(file, code));
						fileOutputToClient(file);
					}
					else 
					{
						code = "404 Not Found";
						outputToClient(errorResponse(code));
					}
				}
				else 
				{
					// output an error message
					outputToClient(errorResponse(code));
				}
			}
			// clean up
			in.close();
				
		}
		catch (Exception e)
		{
			System.out.println("Can't open a reader" + e);
		}
	}
	
	
	/**
	 * Sends the byte data from file through the socket to the client 
	 * @param file the file we are reading from
	 */
	public void fileOutputToClient(File file) {
		try 
		{
			InputStream inS = new FileInputStream(file);
			OutputStream outS = cSocket.getOutputStream();
			byte[] bytes = new byte[8*1024];
			int count;
			
			while((count = inS.read(bytes)) > 0) 
			{
				outS.write(bytes,  0 , count);
				outS.flush();
			}
			
			outS.close();
			inS.close();
		}
		catch (IOException e) 
		{
			System.out.println("Can't get socket input/output stream" + e);
		}
		
		
	}

	/**
	 * Sends the response header to the client
	 * @param response a string containing the appropriate response header
	 */
	public void outputToClient(String response) {
		int count = 0;
		byte[] bytes = response.getBytes(); 
		try 
		{
			OutputStream out = cSocket.getOutputStream();
			bytes = response.getBytes();
			out.write(bytes);
			out.flush();
			out.close();
		}
		catch (IOException e) { System.out.println(" Can't get socket output stream");}
	
		
	}
	
	/**
	 *  Checks if the request is properly formatted
	 *  @Param reqLine a String array containing the tokens of the request header line
	 *  @return code is the status code of the request
	 */
	public String isValidRequest(String [] reqLine) {
		String code = "200 OK";
		
		/*for(int i = 0; i < reqLine.length; i++) {
			System.out.println("Request tokens[" + i + "]" + reqLine[i]);
		}*/
		
		if (reqLine.length != 3)
			{ code = "400 Bad Request"; System.out.println("not enough info");}
		
		else if (!reqLine[0].equals(METHOD))
			{ code = "400 Bad Request"; System.out.println("did not match method"); }
		else if (!reqLine[2].equals(PROTOCOL) &&  !reqLine[2].equals(PROTOCOL_v11))
			{ code = "200 OK"; System.out.println("did not match protocol"); }
		return code;
	}
	
	/**
	 * Constructs a response header for a good request
	 * @param:
	 *  file the file request by the client
	 *  code the status code of the request (200)
	 * @return response a string containing the full response header
	 */
	public String goodResponse(File file, String code) {
		DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss zzz");
		String response = "";
		try 
		{
		  response = 
				PROTOCOL + " " + code + "\r\n" + 
				"Date: " + getDate() + 
				"Server: " + getServer() + 
				"Last-Modified: " + dateFormat.format(file.lastModified()) + "\r\n" + 
				"Content-Length: " + file.length() + "\r\n" + 
				"Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n" + 
				CONNECTION_CLOSED + 
				"\r\n";
		}
		catch (IOException e) { System.out.println("Error forming response header " + e); }
		return response;
	}
	
	/**
	 * Constructs the response header for a request that had an error
	 * @param code the status code of the error (400 or 404)
	 * @return response a string with the full response header
	 */
	public String errorResponse(String code) {
		String response = PROTOCOL + " " + code + "\r\n" + 
						  "Date: " + getDate() + 
						  "Server: " + getServer() + 
						  CONNECTION_CLOSED +
						  "\r\n";
		return response;
	}
	
	/**
	 * Gets the current local date and time 
	 * @return current a formatted string of the current date and time
	 */
	public String getDate () {
		String current = "";
		DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss zzz");
		Date date = new Date();
		current =  dateFormat.format(date);
		return current + "\r\n";
	}
	
	/**
	 * Gets the name of the host computer
	 * Code taken from stack overflow: 
	 * https://stackoverflow.com/questions/7883542/getting-the-computer-name-in-java
	 * @return hostname the name of the computer that is running the server
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
