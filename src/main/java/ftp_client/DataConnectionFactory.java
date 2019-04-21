package ftp_client;

public interface DataConnectionFactory {
	
	public SocketStreamDataConnection createConnection(TransferControlCommands commands) throws ConnectionErrorException;
}
