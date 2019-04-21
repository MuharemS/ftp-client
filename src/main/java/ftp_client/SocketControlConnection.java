package ftp_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class SocketControlConnection implements ControlConnection {
	private ConnectionStream stream;

	SocketControlConnection(ConnectionStream stream) throws IOException{
		this.stream = stream;
	}

	public boolean send(String command) {
		PrintWriter out;
		try {
			out = new PrintWriter(stream.getOutputStream());
		} catch (IOException e) {
			return false;
		}
		command += new String("\r\n");
		out.write(command);
		out.flush();

		return true;
	}


	public String receive() {
		String response = new String();
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(stream.getInputStream()));
		} catch (IOException e1) {
			return response;
		}
		String responseLine = new String();
		try {

			responseLine = in.readLine();
			if(responseLine != null) {
				response = responseLine;
			}
			while(responseLine != null && !endOfResponse(responseLine)) {
				System.out.flush();
				response += responseLine;
				responseLine = in.readLine();
			}
		} catch (IOException e) {
			response = new String();
		}
		return response;
	}

	boolean endOfResponse(String line) {
		if(line != null && line.length() > 3) {
			if(Character.isDigit(line.charAt(0)) &&
					Character.isDigit(line.charAt(1)) &&
					Character.isDigit(line.charAt(2)) &&
					Character.isWhitespace(line.charAt(3))) {
				return true;
			}
		}
		return false;
	}

	public boolean isConnected() {
		return stream.isConnected();
	}


	public void close() {
		stream.close();
	}

}
