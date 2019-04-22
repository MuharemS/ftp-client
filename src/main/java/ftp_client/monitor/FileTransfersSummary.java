package ftp_client.monitor;

public class FileTransfersSummary {
	private double totalTime = 0;
	private long averageSpeed = 0l;

	public FileTransfersSummary() {}
	public FileTransfersSummary(double totalTime, long averageSpeed) {
		this.totalTime = totalTime;
		this.averageSpeed = averageSpeed;
	}
	public double getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(double totalTime) {
		this.totalTime = totalTime;
	}
	public long getAverageSpeed() {
		return averageSpeed;
	}
	public void setAverageSpeed(long averageSpeed) {
		this.averageSpeed = averageSpeed;
	}
}
