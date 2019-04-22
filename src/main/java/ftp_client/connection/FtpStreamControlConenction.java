package ftp_client.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class FtpStreamControlConenction implements ControlConnection {
	private ConnectionStream stream;
	private BufferedReader inputReader;
	private PrintWriter out;
	private final static String LINE_BREAK = new String("\r\n");
	
	public FtpStreamControlConenction(ConnectionStream stream) throws IOException{
		this.stream = stream;
		this.inputReader = new BufferedReader(new InputStreamReader(stream.getInputStream()));
		this.out = new PrintWriter(stream.getOutputStream());
	}

	public boolean isConnected() {
		return stream.isConnected();
	}

	public void close() {
		stream.close();
	}

	public boolean send(String command) {
		if(!command.endsWith(LINE_BREAK))
			command += LINE_BREAK;
		out.write(command);
		out.flush();

		return true;
	}

	public String receive() {
		String response;
		try {
			response = waitForServerResponse(inputReader);
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
}
