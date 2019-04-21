package ftp_client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketControlConnectionFactory implements ControlConnectionFactory{
	private String host;
	private int port;
	public SocketControlConnectionFactory(ConnectionParameters parameters) {
		this.host = parameters.hostname;
		this.port = parameters.port;
	}
	
	public ControlConnection createConnection() throws ConnectionErrorException {
		ControlConnection connection;
		try {
			Socket socket;
			socket = new Socket(host, port);
			SocketConnectionStream connectionStream = new SocketConnectionStream(socket);
			connection = new SocketControlConnection(connectionStream);
		} catch (UnknownHostException e) {
			throw new ConnectionErrorException(e.getMessage());
		} catch (IOException e) {
			throw new ConnectionErrorException(e.getMessage());
		}
		return connection;
	}

}
