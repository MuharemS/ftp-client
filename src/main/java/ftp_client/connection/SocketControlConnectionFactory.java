package ftp_client.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketControlConnectionFactory implements ControlConnectionFactory{
	private String host;
	private int port;
	private int connectTimeoutInMs = 3000; 
	public SocketControlConnectionFactory(ConnectionParameters parameters) {
		this.host = parameters.hostname;
		this.port = parameters.port;
	}
	
	public ControlConnection createConnection() throws ConnectionErrorException {
		ControlConnection connection;
		try {
			Socket socket;
			socket = new Socket();
			socket.connect(new InetSocketAddress(host, port), connectTimeoutInMs);
			SocketConnectionStream connectionStream = new SocketConnectionStream(socket);
			connection = new FtpStreamControlConenction(connectionStream);
		} catch (UnknownHostException e) {
			throw new ConnectionErrorException(e.getMessage());
		} catch (IOException e) {
			throw new ConnectionErrorException(e.getMessage());
		}
		return connection;
	}

}
