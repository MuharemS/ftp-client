package ftp_client.connection;

import ftp_client.TransferControlCommands;

public interface DataConnectionFactory {
	
	public SocketStreamDataConnection createConnection(TransferControlCommands commands) throws ConnectionErrorException;
}
