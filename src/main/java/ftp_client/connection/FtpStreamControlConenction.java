package ftp_client.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class FtpStreamControlConenction implements ControlConnection {
	private ConnectionStream stream;
	private final static String LINE_BREAK = new String("\r\n");
	
	public FtpStreamControlConenction(ConnectionStream stream) throws IOException{
		this.stream = stream;
	}

	public boolean isConnected() {
		return stream.isConnected();
	}

	public void close() {
		stream.close();
	}

	public boolean send(String command) {
		PrintWriter out;
		try {
			out = new PrintWriter(stream.getOutputStream());
		} catch (IOException e) {
			return false;
		}
		if(!command.endsWith(LINE_BREAK))
			command += LINE_BREAK;
		out.write(command);
		out.flush();

		return true;
	}

	public String receive() {
		String response;
		try {
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(stream.getInputStream()));
			response = waitForServerResponse(inputReader);
			closeReader(inputReader);
		} catch (IOException e1) {
			response =  new String();
		}
		return response;
	}

	private String waitForServerResponse(BufferedReader reader) throws IOException {
		String response = new String();
		String singleLine = new String();
		while(singleLine != null && !isEndOfResponse(singleLine)) {
			singleLine = reader.readLine();
			System.out.println("Line" + singleLine);
			if(singleLine != null)
				response += singleLine;
		}
		return response;
	}

	private boolean isEndOfResponse(String line) {
		if(line != null && line.length() > 3) {
			if( startsWithDigits(line) &&
					Character.isWhitespace(line.charAt(3))) {
				return true;
			}
		}
		return false;
	}

	private boolean startsWithDigits(String content) {
		return Character.isDigit(content.charAt(0)) &&
				Character.isDigit(content.charAt(1)) &&
				Character.isDigit(content.charAt(2));
	}

	private void closeReader(BufferedReader reader) {
		try {
			reader.close();
		}catch(IOException e) {
			// Add log line
		}
	}

}
