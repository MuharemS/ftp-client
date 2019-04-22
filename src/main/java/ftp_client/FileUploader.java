package ftp_client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import ftp_client.connection.ControlConnectionFactory;
import ftp_client.connection.DataConnectionFactory;
import ftp_client.monitor.DataTransmissionInfo;
import ftp_client.monitor.MonitorableFileTransaction;

public class FileUploader implements Runnable, MonitorableFileTransaction{
	private File fileForUpload;
	private UserCredidentials credidentials;
	private ControlConnectionFactory controlConFactory;
	private DataConnectionFactory dataConFactory;
	private FtpSession commandSession;
	private AtomicReference<Date> transactionStartTime = new AtomicReference<Date>();
	private AtomicReference<Date> transactionEndTime = new AtomicReference<Date>();

	public FileUploader(File file, UserCredidentials credidentials, ControlConnectionFactory controlConnectionFactory, DataConnectionFactory dataConnectionFactory) {
		this.fileForUpload = file;
		this.credidentials = credidentials;
		this.controlConFactory = controlConnectionFactory;
		this.dataConFactory = dataConnectionFactory;
		this.commandSession = new FtpSession(controlConFactory, dataConFactory);
	}

	public void run() {

		FileInputStream inStream;
		try {
			inStream = new FileInputStream(fileForUpload);
		} catch (FileNotFoundException e) {
			// Should not happen
			// Log could not load file, return
			return;
		}

		if(commandSession.connect()) {
			if(commandSession.logIn(credidentials.username, credidentials.password)) {
				transactionStartTime.set(new Date(System.currentTimeMillis()));
				commandSession.store(fileForUpload.getName(), inStream);
				transactionEndTime.set(new Date(System.currentTimeMillis()));

			}
		}
		commandSession.close();
	}

	
	public String getFileName() {
		return fileForUpload.getName();
	}

	public long getFileSsize() {
		return fileForUpload.length();
	}

	public Date getTransactionStartTime() {
		return transactionStartTime.get();
	}

	public Date getTransactionEndTime() {
		return transactionEndTime.get();
	}
	
	public DataTransmissionInfo getTransmissionInfo() {
		return commandSession.getTransmissionInfo();
	}

}
