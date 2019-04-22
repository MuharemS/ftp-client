package ftp_client.monitor;

import java.util.Date;

public class DataTransmissionInfo {
	private Date transmissionStartTime = null;
	private Date transmissionEndTime = null;
	private long totalBytesTransmited = 0;
	private TransferStatus status = TransferStatus.WaitingServerResponse;
	
	public DataTransmissionInfo() {}
	
	public DataTransmissionInfo(Date startTime, Date endTime, long bytesTransmited, TransferStatus status){
		this.transmissionStartTime = startTime;
		this.transmissionEndTime = endTime;
		this.totalBytesTransmited = bytesTransmited;
		this.status = status;
	}
	
	public void setStartTime(Date startTime) {
		this.transmissionStartTime = startTime;
	}
	public void setEndTime(Date endTime) {
		this.transmissionEndTime = endTime;
	}
	public void setStatus(TransferStatus status) {
		this.status = status;
	}
	public void setBytesTransmited(long bytesTransmited) {
		this.totalBytesTransmited = bytesTransmited;
	}
	
	public Date getStartTime() {
		return this.transmissionStartTime;
	}
	public Date getEndTime() {
		return this.transmissionEndTime;
	}
	public TransferStatus getStatus() {
		return this.status;
	}
	public long getBytesTransmited() {
		return this.totalBytesTransmited;
	}
	
	public boolean hasStartTime() {
		return this.transmissionStartTime != null;
	}
	
	public boolean hasEndTime() {
		return this.transmissionEndTime != null;
	}
}
