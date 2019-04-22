package ftp_client.monitor;

import java.util.List;

public class ConsoleTransactionStatisticsPrinter implements TransactionStatisticsPrinter {
	private TransactionStatisticsFormater formater;
	private List<String> rejectedFiles = null;
	private static final String LINE_BORDER =  "*******************************************";
	private static final String CLEAR_SCREEN_COMMAND = "\033[H\033[2J"; 
	public ConsoleTransactionStatisticsPrinter(TransactionStatisticsFormater formater) {
		this.formater = formater;
	}
	
	public void print(List<FileTransactionStatisticInfo> statistics) {
		System.out.print(CLEAR_SCREEN_COMMAND);
		System.out.println(LINE_BORDER);
		for(FileTransactionStatisticInfo info : statistics) {
			System.out.println(formater.format(info));
		}
		System.out.println(LINE_BORDER);
	}
	
	public void print(FileTransfersSummary summary) {
		System.out.println(LINE_BORDER);
		System.out.println("Summary: ");
		System.out.println(formater.format(summary));
		if(rejectedFiles != null && rejectedFiles.size() > 0) {
			System.out.println();
			for(String fileName : rejectedFiles) {
				System.out.println("[" + fileName + "]" + " could not be read!.");
			}
		}
		System.out.println(LINE_BORDER);
	}
	
	public void registerRejectedFiles(List<String> files) {
		rejectedFiles = files;
	}
}
