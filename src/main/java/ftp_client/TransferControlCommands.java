package ftp_client;

import ftp_client.connection.ConnectionErrorException;
import ftp_client.connection.ConnectionParameters;

public interface TransferControlCommands {
	
	public ConnectionParameters passivePort() throws ConnectionErrorException;
	public boolean activePort(ConnectionParameters params);
	public boolean transferMode(FtpTransferMode mode);
}
