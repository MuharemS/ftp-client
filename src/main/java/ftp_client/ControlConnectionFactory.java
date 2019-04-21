package ftp_client;

public interface ControlConnectionFactory {
	public ControlConnection createConnection() throws ConnectionErrorException;
}
