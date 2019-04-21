package ftp_client;

public interface TransferControlCommands {
	
	public ConnectionParameters passivePort() throws ConnectionErrorException;
	public boolean activePort(ConnectionParameters params);
	public boolean transferMode(FtpTransferMode mode);
}
