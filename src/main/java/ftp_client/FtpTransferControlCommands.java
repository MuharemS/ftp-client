package ftp_client;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class FtpTransferControlCommands  implements TransferControlCommands{
	private ControlConnection controlConnection;
	
	FtpTransferControlCommands(ControlConnection controlConnection){
		this.controlConnection = controlConnection;
	}
	
	public ConnectionParameters passivePort() {
		controlConnection.send(FtpCommands.PASSIVE);
		String response = controlConnection.receive();
		if(FtpHelperTools.isResponseWithCode(response, FtpResponseCodes.ENTERING_PASSIVE_MODE)){
			return calculateAddres(response);
		}else {
			throw new ConnectionErrorException("Could not establish passive connection.");
		}
	}

	public boolean activePort(ConnectionParameters parameters) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean transferMode(FtpTransferMode mode) {
		// TODO Auto-generated method stub
		return false;
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
	
}
