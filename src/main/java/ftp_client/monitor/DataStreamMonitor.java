package ftp_client.monitor;

public interface DataStreamMonitor {
	public void totalBytesSent(long bytes);
	public void totalBytesReceived(long bytes);
}
