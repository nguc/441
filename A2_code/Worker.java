import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
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
			String inputLine, outputLine;
			String [] wholeRequest, reqLine;
			//out.println(outputLine); output to client
				
			while ((inputLine = in.readLine()) != null)
			{
				System.out.println("Requested page: " + inputLine);
				wholeRequest = inputLine.split("\r\n");
				reqLine = wholeRequest[0].split(" ");
				// get a status code
				String code = isValidRequest(reqLine);
				if (code.equals("200 OK"))
				{
					
					String fileName = PATHNAME + reqLine[1];
					File file = new File(fileName);
					if(file.exists()) 
					{
						// transfer file over connection
						//System.out.println(goodResponse(file, code));
						outputToClient(goodResponse(file, code));
					}
					else 
					{
						// return error
						code = "404 Not Found";
						outputToClient(errorResponse(code));
						//System.out.println(errorResponse(code) + "\nFile not found");
					}
					
				}
				
				else 
				{
					// send error for bad request (400) or not found (404)
					// Date\n Server\n connection:close
					//System.out.println(errorResponse(code));
					outputToClient(errorResponse(code));
				}
				
				
				// clean up (close sockets, threads, etc.)
				in.close();
				out.close();
				
				
			}
		}
		catch (Exception e){
			
		}
		
	}
	
	public void outputToClient(String response) {
		int count = 0;
		byte[] bytes = response.getBytes(); 
		try 
		{
			OutputStream out = socket.getOutputStream();
			bytes = response.getBytes();
			out.write(bytes);
			out.flush();
		}
		catch (IOException e) {}
		
	}
	
	/**
	 *  Checks if request was sent properly
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
	 * 
	 * @param code
	 * @return
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
	 * 
	 * @param code
	 * @return
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
