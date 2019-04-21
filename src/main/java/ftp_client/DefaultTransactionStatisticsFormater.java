package ftp_client;

public class DefaultTransactionStatisticsFormater implements TransactionStatisticsFormater {

	public String format(FileTransactionStatisticInfo statistics) {
		String result = new String();
		result += "[" + statistics.getFileName() + "]";
		result += statistics.getStatus().name() + "    ";
		result += Long.toString(statistics.getTotalBytesSent()) + "/" +Long.toString(statistics.getFileSize()) + "    ";
		result += Long.toString(statistics.getAverageTransmissionSpeed()) + "  KB/s" + 
				"  TotalTime: " + Double.toString(statistics.getTotalTransactionTime()) + " s";
		return result;
	}

	public String format(FileTransfersSummary summary) {
		String result = new String();
		String avgTransferSpeedAsString = "NA";
		if(summary.getAverageSpeed() > 0) {
			avgTransferSpeedAsString = Long.toString(summary.getAverageSpeed()) + " KB/s";
		}

		result += "TotalTime: " + Double.toString(summary.getTotalTime()) + " s\n";
		result += "AverageTransferSpeed: "  + avgTransferSpeedAsString;
		return result;
	}

}
