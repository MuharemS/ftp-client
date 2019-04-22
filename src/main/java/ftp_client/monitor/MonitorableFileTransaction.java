package ftp_client.monitor;

import java.util.Date;

public interface MonitorableFileTransaction extends MonitorableDataTransmission {
	public String getFileName();
	public long getFileSsize();
	public Date getTransactionStartTime();
	public Date getTransactionEndTime();
}