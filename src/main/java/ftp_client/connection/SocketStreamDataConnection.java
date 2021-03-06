package ftp_client.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ftp_client.CommandStreamException;
import ftp_client.monitor.DataStreamMonitor;

public class SocketStreamDataConnection {
	private ConnectionStream connectionStream;
	private DataStreamMonitor monitor = null;
	private long totalBytesSent = 0;
	private long updateBytesThreshold = 1024;
	private long lastSendNotification = 0;

	public SocketStreamDataConnection(ConnectionStream connectionStream) {
		this.connectionStream = connectionStream;
	}

	public void send(InputStream input) throws IOException {
		OutputStream out = connectionStream.getOutputStream();
		int bytesRead = 0;
		byte[] buffer = new byte[8192];
		while((bytesRead = input.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);
			totalBytesSent += bytesRead;
			if(isMonitorRegistered() && isUpdateThresholdReached()) {
				notifyMonitor();
			}
		}
		notifyMonitor();
		connectionStream.close();
	}

	public void close() {
		connectionStream.close();
	}

	public void registerMonitor(DataStreamMonitor monitor) {
		this.monitor = monitor;
	}

	public boolean isConnected() {
		return connectionStream.isConnected();
	}

	public String receive() throws CommandStreamException {
		// TODO Auto-generated method stub
		return null;
	}
	private boolean isUpdateThresholdReached() {
		return (totalBytesSent - lastSendNotification) >= updateBytesThreshold;
	}
	private boolean isMonitorRegistered() {
		return monitor != null;
	}

	private void notifyMonitor() {
		if(isMonitorRegistered()) {
			lastSendNotification = totalBytesSent;
			monitor.totalBytesSent(lastSendNotification);
		}
	}

}
