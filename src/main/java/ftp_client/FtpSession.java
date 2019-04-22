package ftp_client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import ftp_client.connection.ConnectionErrorException;
import ftp_client.connection.ControlConnection;
import ftp_client.connection.ControlConnectionFactory;
import ftp_client.connection.DataConnectionFactory;
import ftp_client.connection.SocketStreamDataConnection;
import ftp_client.monitor.DataStreamMonitor;
import ftp_client.monitor.DataTransmissionInfo;
import ftp_client.monitor.MonitorableDataTransmission;
import ftp_client.monitor.TransferStatus;


public class FtpSession implements MonitorableDataTransmission{

	private FtpTransferControlCommands transferControlCommands;
	private ControlConnectionFactory controlConnectionFactory;
	private DataConnectionFactory dataConnectionFactory;
	private ControlConnection controlConnection;
	private AtomicReference<TransferStatus> lastTransmitionStatus = new AtomicReference<TransferStatus>(TransferStatus.Waiting);
	private AtomicLong lastTransmissionBytesTransferred = new AtomicLong(0);
	private AtomicReference<Date> lastTransmissionStartTime = new AtomicReference<Date>();
	private AtomicReference<Date> lastTransmissionEndTime = new AtomicReference<Date>();
	private boolean isConnected = false;
	private boolean isLoggedIn = false;


	public FtpSession(ControlConnectionFactory controlConnectionFactory, DataConnectionFactory dataConnectionFactory) {
		this.controlConnectionFactory = controlConnectionFactory;
		this.dataConnectionFactory = dataConnectionFactory;
	}

	public boolean connect() {
		boolean isConnectSuccessful = false;
		if(!isConnected()) {
			try {
				controlConnection = controlConnectionFactory.createConnection();
				isConnected = acknowledgeConnection();
				isConnectSuccessful = isConnected;
				transferControlCommands = new FtpTransferControlCommands(controlConnection);
			} catch (ConnectionErrorException e) {
				isConnectSuccessful = false;
			}
		}
		return isConnectSuccessful;
	}

	public void close() {
		if(controlConnection != null) {
			controlConnection.close();
		}
	}
	
	public boolean logIn(String username, String password) {
		boolean isLoginSuccessful = false;
		if(isConnected() && !isLoggedIn()) {
			String usernameCommand = FtpHelperTools.createFtpCommand(FtpCommands.USERNAME, username);
			String response = sendCommandRequest(usernameCommand);
			if(FtpHelperTools.isResponseCode(response, FtpResponseCodes.PASS_REQUIRED)) {
				String passwordCommand = FtpHelperTools.createFtpCommand(FtpCommands.PASSWORD , password);
				response = sendCommandRequest(passwordCommand);
			}
			if(FtpHelperTools.isResponseCode(response, FtpResponseCodes.LOGIN_SUCCESSFUL)) {
				isLoggedIn = true;
				isLoginSuccessful = true;
			}
		}
		return isLoginSuccessful;
	}

	public boolean store(String fileName, InputStream inStream) {
		boolean isTransferSuccesfull = false;
		clearPreviousTransmission();
		if(!isConnected() || !isLoggedIn()) {
			updateTransferStatus(TransferStatus.Failed);
			return false;
		}
		SocketStreamDataConnection dataConnection = createDataConnection();
		if(dataConnection == null) {
			updateTransferStatus(TransferStatus.Failed);
			return false;
		}

		subscribeToDataSentUpdates(dataConnection);
		updateTransferStatus(TransferStatus.WaitingServerResponse);
		String storeCommand = FtpHelperTools.createFtpCommand(FtpCommands.STORE, fileName);
		String response = sendCommandRequest(storeCommand);
		if(FtpHelperTools.isResponseCode(response, FtpResponseCodes.READY_TO_SEND_DATA)) {
			updateTransferStatus(TransferStatus.InProgress);
			if(sendToDataConnection(inStream, dataConnection)) {
				updateTransferStatus(TransferStatus.Done);
				isTransferSuccesfull = true;	
			}
		}
		if(!isTransferSuccesfull)
			updateTransferStatus(TransferStatus.Failed);
		
		dataConnection.close();
		return isTransferSuccesfull;
	}

	public DataTransmissionInfo getTransmissionInfo() {
		return new DataTransmissionInfo(lastTransmissionStartTime.get(), 
				lastTransmissionEndTime.get(), lastTransmissionBytesTransferred.get(), 
				lastTransmitionStatus.get());
	}
	
	private void clearPreviousTransmission() {
		lastTransmissionBytesTransferred.set(0);
		lastTransmissionStartTime.set(null);
		lastTransmissionEndTime.set(null);
		lastTransmitionStatus.set(TransferStatus.WaitingServerResponse);
	}

	private boolean sendToDataConnection(InputStream data, SocketStreamDataConnection dataConnection) {
		boolean isTransmissionSuccessful = false;
		setTransmissionStartTime();
		try {
			dataConnection.send(data);
			setTransmissionEndTime();
			if(acknowledgeDataTransfer()){
				isTransmissionSuccessful = true;
			}	
		} catch (IOException e) {
			setTransmissionEndTime();
		}
		return isTransmissionSuccessful;
	}

	private boolean acknowledgeDataTransfer() {
		String response = controlConnection.receive();
		if(FtpHelperTools.isResponseCode(response, FtpResponseCodes.DATA_TRANSFER_SUCCESSFUL)){
			return true;
		}
		return false;

	}

	private boolean acknowledgeConnection() {
		String response = controlConnection.receive();
		return FtpHelperTools.isResponseCode(response, FtpResponseCodes.CONNECTION_ACKNOWLEDGEMENT);
	}

	private String sendCommandRequest(String command) {
		controlConnection.send(command);
		return controlConnection.receive();
	}

	private boolean isConnected() {
		return isConnected;
	}

	private boolean isLoggedIn() {
		return isLoggedIn;
	}

	private SocketStreamDataConnection createDataConnection() {
		SocketStreamDataConnection newConnection = null;
		try {
			newConnection = dataConnectionFactory.createConnection(transferControlCommands);

		} catch (ConnectionErrorException e) {

		}
		return newConnection;
	}

	private void subscribeToDataSentUpdates(SocketStreamDataConnection dataConnection) {
		dataConnection.registerMonitor(new DataStreamMonitor() {

			public void totalBytesSent(long bytes) {
				updateBytesTransfered(bytes);
			}

			public void totalBytesReceived(long bytes) {
			}
		});
	}

	private void setTransmissionStartTime() {
		lastTransmissionStartTime.set(new Date(System.currentTimeMillis()));
	}

	private void setTransmissionEndTime() {
		lastTransmissionEndTime.set(new Date(System.currentTimeMillis()));
	}

	private void updateTransferStatus(TransferStatus newStatus) {
		lastTransmitionStatus.set(newStatus);
	}
	
	private void updateBytesTransfered(long newValue) {
		lastTransmissionBytesTransferred.set(newValue);
	}
}
