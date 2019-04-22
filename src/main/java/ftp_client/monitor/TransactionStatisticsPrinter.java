package ftp_client.monitor;

import java.util.List;

public interface TransactionStatisticsPrinter {
	public void print(List<FileTransactionStatisticInfo> statistics);
	public void print(FileTransfersSummary summary);
}
