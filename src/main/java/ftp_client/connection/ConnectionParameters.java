package ftp_client.connection;

public class ConnectionParameters {
	public String hostname;
	public int port;
	
	public ConnectionParameters() {
		hostname = new String();
		port = 0;
	}
	
	public ConnectionParameters(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}

}
