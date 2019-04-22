package ftp_client.monitor;

import java.util.Date;
import java.util.List;

public class FileTransactionStatisticsService {
	private static final long MS_IN_SECOND = 1000l;
	private static final long BYTES_IN_KBYTE = 1024l;

	public FileTransactionStatisticInfo getStatistics(MonitorableFileTransaction transaction) {
		DataTransmissionInfo transmissionInfo = transaction.getTransmissionInfo();
		FileTransactionStatisticInfo statistics = new FileTransactionStatisticInfo();
		statistics.setFileName(transaction.getFileName());
		statistics.setFileSize(transaction.getFileSsize());
		statistics.setTotalBytesSent(transmissionInfo.getBytesTransmited());
		statistics.setAverageTransmissionSpeed(getAverageTransmissionSpeed(transmissionInfo));
		statistics.setStatus(transmissionInfo.getStatus());
		statistics.setTotalTransactionTime(getFileTransactionDuration(transaction));
		return statistics;
	}

	public FileTransfersSummary getSummary(List<?extends MonitorableFileTransaction> transactions) {
		FileTransfersSummary summary = new FileTransfersSummary();
		if(transactions.size() > 0) {
			long durationInMs = 0;

			Date minStartTime = null;
			Date maxEndTime = null;
			long averageSpeed = 0l;
			int filesIncludedInCalculation = 0;

			for(MonitorableFileTransaction transaction : transactions) {
				DataTransmissionInfo info = transaction.getTransmissionInfo();
				if(info.hasStartTime() && info.hasEndTime()) {
					averageSpeed += getAverageTransmissionSpeed(info);
					filesIncludedInCalculation++;
				}

				minStartTime = getSmaller(minStartTime, transaction.getTransactionStartTime());
				maxEndTime = getGreater(maxEndTime, transaction.getTransactionEndTime());
			}
			if(minStartTime != null && maxEndTime != null) {
				durationInMs = (maxEndTime.getTime() - minStartTime.getTime());
			}

			double durationInSec = durationInMs / (double)MS_IN_SECOND;
			summary.setTotalTime(durationInSec);

			if(filesIncludedInCalculation > 0){
				summary.setAverageSpeed(averageSpeed / filesIncludedInCalculation); 
			}
		}
		return summary;
	}

	private Date getGreater(Date first, Date second) {
		if(first == null || (second != null && first.before(second))) {
			return second;
		}
		return first;
	}

	private Date getSmaller(Date first, Date second) {
		if(first == null || (second != null && first.before(second))) {
			return second;
		}
		return first;
	}

	private double getFileTransactionDuration(MonitorableFileTransaction transaction) {
		double duration = 0;
		Date startTime = transaction.getTransactionStartTime();
		Date endTime = transaction.getTransactionEndTime();
		if(startTime != null) {
			if(endTime == null) {
				endTime = new Date(System.currentTimeMillis());
			}
			duration = (endTime.getTime() - startTime.getTime()) / (double)(MS_IN_SECOND);
		}
		return duration;
	}

	private Long getAverageTransmissionSpeed(DataTransmissionInfo transmissionInfo) {
		Long averageTransmissionSpeed = 0l;
		if(transmissionInfo.hasStartTime()) {
			Date transmissionStartTime = transmissionInfo.getStartTime();
			Date transmissionEndTime = getTransmissionEndTime(transmissionInfo);
			long transmissionDuration = transmissionEndTime.getTime() - transmissionStartTime.getTime();
			averageTransmissionSpeed = calculateTransmissionAverageSpeed(transmissionDuration, 
					transmissionInfo.getBytesTransmited());
		}
		return averageTransmissionSpeed;
	}


	private Date getTransmissionEndTime(DataTransmissionInfo transmissionInfo) {
		if(transmissionInfo.hasEndTime()) {
			return transmissionInfo.getEndTime();
		}else {
			return new Date(System.currentTimeMillis());
		}
	}

	private long calculateTransmissionAverageSpeed(long transmissionDurationInMilliseconds, long bytesSent) {
		long averageTransmissionSpeed = 0l;
		if(transmissionDurationInMilliseconds == 0)
			transmissionDurationInMilliseconds = 1;
		if(transmissionDurationInMilliseconds < MS_IN_SECOND) {
			averageTransmissionSpeed=  Math.round(getKB(bytesSent) * 
					((double)MS_IN_SECOND /transmissionDurationInMilliseconds));
		}else {
			double durationInSeconds = transmissionDurationInMilliseconds / (double)(MS_IN_SECOND);
			double speedAsdouble = getKB(bytesSent)  / durationInSeconds;
			averageTransmissionSpeed = Math.round(speedAsdouble);
		}

		return averageTransmissionSpeed;
	}

	private double getKB(long bytes) {
		return bytes / (double)BYTES_IN_KBYTE;
	}
}
