package ftp_client.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ConnectionStream {
	public InputStream getInputStream() throws IOException;
	public OutputStream getOutputStream() throws IOException;
	void close();
	boolean isConnected();
}