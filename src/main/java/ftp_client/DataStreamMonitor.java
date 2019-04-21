package ftp_client;

public interface DataStreamMonitor {
	public void totalBytesSent(long bytes);
	public void totalBytesReceived(long bytes);
}
