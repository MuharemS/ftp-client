package ftp_client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataTransferMonitoringService implements Runnable {

	private List<?extends MonitorableFileTransaction> monitors;
	int updateRate = 500;
	AtomicBoolean running = new AtomicBoolean(true);
	FileTransactionStatisticsService statisticsService;
	TransactionStatisticsPrinter printer;
	public DataTransferMonitoringService(List<? extends MonitorableFileTransaction> transfers, FileTransactionStatisticsService statistics,TransactionStatisticsPrinter printer){
		this.monitors = transfers;
		this.statisticsService = statistics;
		this.printer = printer;
	}

	public void run() {
		while(running.get()) {
			ArrayList<FileTransactionStatisticInfo> statistics = new ArrayList<FileTransactionStatisticInfo>();
			for(MonitorableFileTransaction transaction : monitors) {
				DataTransmissionInfo info = transaction.getTransmissionInfo();
				String startTime = (info.hasStartTime())?info.getStartTime().toString():"null";
				String endTime = (info.hasEndTime())?info.getEndTime().toString():"null";
				statistics.add( statisticsService.getStatistics(transaction));
			}
			printer.print(statistics);
			try {
				Thread.sleep(updateRate);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		FileTransfersSummary summary = statisticsService.getSummary(monitors);
		printer.print(summary);

	}

	public void stop() {
		running.set(false);
	}

}
