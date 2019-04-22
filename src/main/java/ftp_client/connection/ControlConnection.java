package ftp_client.connection;

import ftp_client.CommandStreamException;

public interface ControlConnection {
	
	public boolean isConnected();
	public void close();
	
	public boolean send(String command) throws CommandStreamException;
	public String receive() throws CommandStreamException;
}
