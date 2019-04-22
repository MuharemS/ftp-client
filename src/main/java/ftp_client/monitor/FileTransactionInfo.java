package ftp_client.monitor;

import java.util.Date;

public class FileTransactionInfo {
	private String fileName = new String();
	private long fileSsize = 0l;
	private Date transactionStartTime = new Date();
	private Date transactionEndTime = new Date();
	private DataTransmissionInfo transmissionInfo = new DataTransmissionInfo();
	
	public FileTransactionInfo() {
	}
	
	public FileTransactionInfo(String fileName, long fileSsize, Date transactionStartTime, Date transactionEndTime,
			DataTransmissionInfo transmitionInfo) {
		this.fileName = fileName;
		this.fileSsize = fileSsize;
		this.transactionStartTime = transactionStartTime;
		this.transactionEndTime = transactionEndTime;
		this.transmissionInfo = transmitionInfo;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getFileSsize() {
		return fileSsize;
	}

	public void setFileSsize(long fileSsize) {
		this.fileSsize = fileSsize;
	}

	public Date getTransactionStartTime() {
		return transactionStartTime;
	}

	public void setTransactionStartTime(Date transactionStartTime) {
		this.transactionStartTime = transactionStartTime;
	}

	public Date getTransactionEndTime() {
		return transactionEndTime;
	}

	public void setTransactionEndTime(Date transactionEndTime) {
		this.transactionEndTime = transactionEndTime;
	}

	public DataTransmissionInfo getTransmissionInfo() {
		return transmissionInfo;
	}

	public void setTransmissionInfo(DataTransmissionInfo transmitionInfo) {
		this.transmissionInfo = transmitionInfo;
	}

	

}
