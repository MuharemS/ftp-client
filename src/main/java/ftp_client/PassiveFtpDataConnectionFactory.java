package ftp_client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PassiveFtpDataConnectionFactory implements DataConnectionFactory {

	public SocketStreamDataConnection createConnection(TransferControlCommands commands) throws ConnectionErrorException {
		ConnectionParameters destination = commands.passivePort();
			
		Socket dataSocket = new Socket();
		InetSocketAddress address = new InetSocketAddress(destination.hostname, destination.port);
		try {
			dataSocket.connect(address);
		} catch (IOException e) {
			try {
				dataSocket.close();
			} catch (IOException e1) {
				
				e1.printStackTrace();
			}
			throw new ConnectionErrorException(e.getMessage());
		}
		SocketConnectionStream stream = new SocketConnectionStream(dataSocket);
		SocketStreamDataConnection dataSession = new SocketStreamDataConnection(stream);
		return dataSession;
	}
}
