package ftp_client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


public class FtpSession implements MonitorableDataTransmission{

	private FtpTransfercommandsImpl transactionParametersCommands = new FtpTransfercommandsImpl();
	private ControlConnectionFactory controlConnectionFactory = null;
	private DataConnectionFactory dataConnectionFactory;
	private ControlConnection controlConnection;
	private AtomicReference<TransferStatus> lastTransmitionStatus = new AtomicReference<TransferStatus>(TransferStatus.Waiting);
	private AtomicLong lastTransmissionBytesTransferred = new AtomicLong(0);
	private AtomicReference<Date> lastTransmissionStartTime = new AtomicReference<Date>();
	private AtomicReference<Date> lastTransmissionEndTime = new AtomicReference<Date>();
	private boolean isConnected = false;
	private boolean isLoggedIn = false;


	public DataTransmissionInfo getTransmissionInfo() {
		return new DataTransmissionInfo(lastTransmissionStartTime.get(), 
				lastTransmissionEndTime.get(), lastTransmissionBytesTransferred.get(), 
				lastTransmitionStatus.get());
	}

	public FtpSession(ControlConnectionFactory controlConnectionFactory, DataConnectionFactory dataConnectionFactory) {
		this.controlConnectionFactory = controlConnectionFactory;
		this.dataConnectionFactory = dataConnectionFactory;
	}

	public void close() {
		if(controlConnection != null) {
			controlConnection.close();
		}
	}

	public boolean connect() {
		if(!isConnected()) {
			try {
				controlConnection = controlConnectionFactory.createConnection();
			} catch (ConnectionErrorException e) {
				return false;
			}
			isConnected = acknowledgeConnection();
		}
		return isConnected();
	}

	public boolean logIn(String username, String password) {
		if(!isConnected()) {
			return false;
		}
		if(!isLoggedIn()) {
			String response = request(FtpCommands.USERNAME+ " " + username);
			if(checkCode(response, FtpResponseCodes.PASS_REQUIRED)) {
				response = request(FtpCommands.PASSWORD + " " + password);
			}
			if(checkCode(response, FtpResponseCodes.LOGIN_SUCCESSFUL)) {
				isLoggedIn = true;
				return true;
			}
		}
		return false;
	}

	public boolean store(String fileName, InputStream inStream) {
		clearPreviousTransmission();
		if(!isLoggedIn()) {
			updateTransferStatus(TransferStatus.Failed);
			return false;
		}
		SocketStreamDataConnection dataConnection = createDataConnection();
		if(dataConnection == null) {
			updateTransferStatus(TransferStatus.Failed);
			return false;
		}

		boolean isTransferSuccesfull = false;
		subscribeToDataSentUpdates(dataConnection);
		updateTransferStatus(TransferStatus.WaitingServerResponse);
		String response = request(FtpCommands.STORE + " " + fileName);
		if(checkCode(response, FtpResponseCodes.READY_TO_SEND_DATA)) {
			updateTransferStatus(TransferStatus.InProgress);
			if(sendToDataConnection(inStream, dataConnection)) {
				if(acknowledgeDataTransfer()){
					updateTransferStatus(TransferStatus.Done);
					isTransferSuccesfull = true;
				}		
			}
		}
		if(!isTransferSuccesfull)
			updateTransferStatus(TransferStatus.Failed);

		dataConnection.close();
		return isTransferSuccesfull;
	}

	private boolean sendToDataConnection(InputStream data, SocketStreamDataConnection dataConnection) {
		boolean isTransmissionSuccessful = false;
		try {
			setTransmissionStartTime();
			dataConnection.send(data);
			isTransmissionSuccessful = true;
		} catch (IOException e) {
		}
		setTransmissionEndTime();
		return isTransmissionSuccessful;
	}

	private void clearPreviousTransmission() {
		lastTransmissionBytesTransferred.set(0);
		lastTransmissionStartTime.set(null);
		lastTransmissionEndTime.set(null);
		lastTransmitionStatus.set(TransferStatus.WaitingServerResponse);
	}

	private boolean acknowledgeDataTransfer() {
		String response = controlConnection.receive();
		if(checkCode(response, FtpResponseCodes.DATA_TRANSFER_SUCCESSFUL)){
			return true;
		}
		return false;

	}

	private boolean acknowledgeConnection() {
		String response = controlConnection.receive();
		return checkCode(response, FtpResponseCodes.CONNECTION_ACKNOWLEDGEMENT);
	}

	private String request(String command) {
		controlConnection.send(command);
		return controlConnection.receive();
	}

	private boolean checkCode(String response, String code) {
		return response.startsWith(code);
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
			newConnection = dataConnectionFactory.createConnection(transactionParametersCommands);

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

	private class FtpTransfercommandsImpl implements TransferControlCommands{

		public ConnectionParameters passivePort() {
			String response = request(FtpCommands.PASSIVE);
			if(checkCode(response, FtpResponseCodes.ENTERING_PASSIVE_MODE)){
				return calculateAddres(response);
			}else {
				throw new ConnectionErrorException("Could not establish passive connection.");
			}
		}

		public boolean activePort(ConnectionParameters parameters) {
			// TODO Auto-generated method stub
			return true;
		}

		public boolean transferMode(FtpTransferMode mode) {
			// TODO Auto-generated method stub
			return true;
		}

		private ConnectionParameters calculateAddres(String response) {
			response = response.substring(3);
			response = extractAddressInformation(response);
			String[] stringValues = response.split(",");
			if(stringValues.length < 6) {
				throw new ConnectionErrorException("Received invalid response from server!");
			}
			String host = stringValues[0] + "."+ stringValues[1]+ "." + stringValues[2]+ "." + stringValues[3];

			Integer portMultiplier = Integer.parseInt(stringValues[4]);
			Integer portAdd = Integer.parseInt(stringValues[5]);
			Integer port = portMultiplier * 256 + portAdd;
			return new ConnectionParameters(host, port);
		}

		private String extractAddressInformation(String response) {
			return response.replaceAll("[^0-9,]", "");
		}

	};

}
