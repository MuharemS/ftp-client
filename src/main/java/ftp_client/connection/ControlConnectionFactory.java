package ftp_client.connection;

public interface ControlConnectionFactory {
	public ControlConnection createConnection() throws ConnectionErrorException;
}
