package ftp_client;

public interface TransactionStatisticsFormater {
	public String format(FileTransactionStatisticInfo statistics);

	public String format(FileTransfersSummary summary);
}
