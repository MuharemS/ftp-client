package ftp_client.monitor;

public interface TransactionStatisticsFormater {
	public String format(FileTransactionStatisticInfo statistics);

	public String format(FileTransfersSummary summary);
}
