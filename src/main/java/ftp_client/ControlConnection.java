package ftp_client;

public interface ControlConnection {
	
	public boolean isConnected();
	public void close();
	
	public boolean send(String command) throws CommandStreamException;
	public String receive() throws CommandStreamException;
}
