package ftp_client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketConnectionStream implements ConnectionStream {
	
	Socket socket;
	
	public SocketConnectionStream(Socket socket) {
		this.socket = socket;
	}

	public InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	public void close() {
		
		try {
			socket.close();
		} catch (IOException e) {
		
		}
	}

	public boolean isConnected() {
		return socket.isConnected();
	}

}
