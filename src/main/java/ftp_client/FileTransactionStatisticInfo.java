package ftp_client;

public class FileTransactionStatisticInfo {
	private double totalTransactionTime = 0l;
	private Long averageTransmissionSpeed = null;
	private Long fileSize = 0l;
	private Long totalBytesSent = 0l;
	private String fileName = new String();
	private TransferStatus status = TransferStatus.WaitingServerResponse;

	public Long getAverageTransmissionSpeed() {
		return averageTransmissionSpeed;
	}
	public void setAverageTransmissionSpeed(Long averageTransmissionSpeed) {
		this.averageTransmissionSpeed = averageTransmissionSpeed;
	}
	public double getTotalTransactionTime() {
		return totalTransactionTime;
	}
	public void setTotalTransactionTime(double totalTransactionTime) {
		this.totalTransactionTime = totalTransactionTime;
	}
	public Long getFileSize() {
		return fileSize;
	}
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
	public Long getTotalBytesSent() {
		return totalBytesSent;
	}
	public void setTotalBytesSent(Long totalBytesSent) {
		this.totalBytesSent = totalBytesSent;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public TransferStatus getStatus() {
		return status;
	}
	public void setStatus(TransferStatus status) {
		this.status = status;
	}
}
