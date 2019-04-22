import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

import ftp_client.monitor.DataTransmissionInfo;
import ftp_client.monitor.FileTransactionStatisticInfo;
import ftp_client.monitor.FileTransactionStatisticsService;
import ftp_client.monitor.FileTransfersSummary;
import ftp_client.monitor.MonitorableFileTransaction;
import ftp_client.monitor.TransferStatus;

public class FileTransactionStatisticsServiceUnitTest {
	FileTransactionStatisticsService statistics = new FileTransactionStatisticsService();
	
	class FakeTransaction implements MonitorableFileTransaction{
		public DataTransmissionInfo info = new DataTransmissionInfo(new Date(), new Date(), 0, TransferStatus.WaitingServerResponse);
		public String fileName = new String("FileName");
		public long fileSize = 0l;
		public Date transactionStartTime = new Date();
		public Date transactionEndTime = new Date();
		
		FakeTransaction(){}
		
		public DataTransmissionInfo getTransmissionInfo() {
			return info;
		}

		public String getFileName() {
			return fileName;
		}

		public long getFileSsize() {
			return fileSize;
		}

		public Date getTransactionStartTime() {
			return transactionStartTime;
		}

		public Date getTransactionEndTime() {
			return transactionEndTime;
		}
		
	}
	
	private static final long MS_IN_SECOND = 1000l;
	private Date addOffsetInSeconds(Date date, int seconds) {
		return new Date(date.getTime() + seconds * MS_IN_SECOND);
	}
	
	private Date addOffsetInMilliseconds(Date date, int milliseconds) {
		return new Date(date.getTime() + milliseconds);
	}
	
	
	private Date now() {
		return new Date(System.currentTimeMillis());
	}
	
	@Test
	public void expectsToRoundUpTransmissionSpeed() {
		FakeTransaction transaction = new FakeTransaction();
		DataTransmissionInfo transmissionInfo = transaction.getTransmissionInfo();
		
		Date currentTime = now();
		
		transmissionInfo.setStartTime(addOffsetInSeconds(currentTime, -5));
		transmissionInfo.setEndTime(addOffsetInSeconds(currentTime, 5));
		transmissionInfo.setBytesTransmited(5120l);
		
		FileTransactionStatisticInfo calculatedStatistics = statistics.getStatistics(transaction);
		Long expectedAVS = 1l;
		assertEquals(expectedAVS, calculatedStatistics.getAverageTransmissionSpeed());
		
	}
	
	@Test
	public void expectsToRoundDownTransmissionSpeed() {
		FakeTransaction transaction = new FakeTransaction();
		DataTransmissionInfo transmissionInfo = transaction.getTransmissionInfo();
		
		Date currentTime = now();
		
		transmissionInfo.setStartTime(addOffsetInSeconds(currentTime, -5));
		transmissionInfo.setEndTime(addOffsetInSeconds(currentTime, 5));
		transmissionInfo.setBytesTransmited(5119l);
		
		FileTransactionStatisticInfo calculatedStatistics = statistics.getStatistics(transaction);
		Long expectedAVS = 0l;
		assertEquals(expectedAVS, calculatedStatistics.getAverageTransmissionSpeed());
		
	}
	
	@Test
	public void expectsToCalculateTransmissionSpeedIfDurationIsLessThanSecond_RoundDown() {
		FakeTransaction transaction = new FakeTransaction();
		DataTransmissionInfo transmissionInfo = transaction.getTransmissionInfo();
		
		Date currentTime = now();
		
		transmissionInfo.setStartTime(currentTime);
		transmissionInfo.setEndTime(addOffsetInMilliseconds(currentTime, 300));
		transmissionInfo.setBytesTransmited(1024);
		
		FileTransactionStatisticInfo calculatedStatistics = statistics.getStatistics(transaction);
		Long expectedAVS = 3l;
		assertEquals(expectedAVS, calculatedStatistics.getAverageTransmissionSpeed());
		
	}
	
	@Test
	public void expectsToCalculateTransmissionSpeedIfDurationIsLessThanSecond_RoundUp() {
		FakeTransaction transaction = new FakeTransaction();
		DataTransmissionInfo transmissionInfo = transaction.getTransmissionInfo();
		
		Date currentTime = now();
		
		transmissionInfo.setStartTime(currentTime);
		transmissionInfo.setEndTime(addOffsetInMilliseconds(currentTime, 300));
		transmissionInfo.setBytesTransmited(1111);
		
		FileTransactionStatisticInfo calculatedStatistics = statistics.getStatistics(transaction);
		Long expectedAVS = 4l;
		assertEquals(expectedAVS, calculatedStatistics.getAverageTransmissionSpeed());
		
	}

	@Test 
	public void expectsToCalculateAverageTransmissionSpeedIfDurationIsZero() {
		FakeTransaction transaction = new FakeTransaction();
		DataTransmissionInfo transmissionInfo = transaction.getTransmissionInfo();
		
		Date currentTime = now();
		
		transmissionInfo.setStartTime(currentTime);
		transmissionInfo.setEndTime(currentTime);
		transmissionInfo.setBytesTransmited(1741);
		
		FileTransactionStatisticInfo calculatedStatistics = statistics.getStatistics(transaction);
		Long expectedAVS = 1700l;
		assertEquals(expectedAVS, calculatedStatistics.getAverageTransmissionSpeed());
	}
	
	@Test
	public void expectsToSetAverageTransmissionSpeedToZeroIfStartTimeAndEndTimeIsUnavailable() {
		FakeTransaction transaction = new FakeTransaction();
		DataTransmissionInfo transmissionInfo = transaction.getTransmissionInfo();
		
		transmissionInfo.setStartTime(null);
		transmissionInfo.setEndTime(null);
		transmissionInfo.setBytesTransmited(1111);
		
		FileTransactionStatisticInfo calculatedStatistics = statistics.getStatistics(transaction);
		Long expectedAVS = 0l;
		assertEquals(expectedAVS, calculatedStatistics.getAverageTransmissionSpeed());
		
	}
	
	@Test
	public void expectsToCalculateAverageTransmissionSpeedIfEndTimeIsNotAvailable() {
		FakeTransaction transaction = new FakeTransaction();
		DataTransmissionInfo transmissionInfo = transaction.getTransmissionInfo();
		
		Date currentTime = now();
		
		Date startTime = addOffsetInSeconds(currentTime, -10);
		transmissionInfo.setStartTime(startTime);
		transmissionInfo.setEndTime(null);
		transmissionInfo.setBytesTransmited(10000);
		Date beforeEndTime = now();
		FileTransactionStatisticInfo calculatedStatistics = statistics.getStatistics(transaction);
		Date endTime = now();
		double durationMax = endTime.getTime() - startTime.getTime();
		durationMax = durationMax / MS_IN_SECOND;
		double durationMin = beforeEndTime.getTime() - startTime.getTime();
		durationMin = durationMin / MS_IN_SECOND;
		long expectedAverageSpeedTopLimit = Math.round((10000l / 1024.0) / durationMax); 
		long expectedAverageSpeedBottomLimit = Math.round((10000l / 1024.0) / durationMin); 
		
		assertTrue(calculatedStatistics.getAverageTransmissionSpeed() <= expectedAverageSpeedTopLimit && 
				calculatedStatistics.getAverageTransmissionSpeed() >= expectedAverageSpeedBottomLimit);
		
	}
	
	private final Double PRECISION = 0.0001;
	boolean areEqual(Double first, Double second) {
		return Math.abs(first - second) < PRECISION; 
	}
	
	@Test
	public void expectsToCalculadteTransactionDurationInSummary() {
		FakeTransaction transaction1 = new FakeTransaction();
		FakeTransaction transaction2 = new FakeTransaction();
		FakeTransaction transaction3 = new FakeTransaction();
		Date startTime = addOffsetInSeconds(now(), -5);
		transaction1.transactionStartTime = null;
		transaction1.transactionEndTime = addOffsetInMilliseconds(startTime, 15500);
		transaction2.transactionStartTime = addOffsetInSeconds(startTime, 3);
		transaction2.transactionEndTime = addOffsetInSeconds(startTime, 13);
		transaction3.transactionStartTime = startTime;
		transaction3.transactionEndTime = null;
		
		ArrayList<FakeTransaction> transactions = new ArrayList<FakeTransaction>();
		transactions.add(transaction1);
		transactions.add(transaction2);
		transactions.add(transaction3);
		
		FileTransfersSummary summary = statistics.getSummary(transactions);
		assertTrue(areEqual(summary.getTotalTime(), 12.5));
	}
	
	@Test
	public void expectsToReturnZeroDurationAndSpeedIfListIsEmpty() {
		
		ArrayList<FakeTransaction> transactions = new ArrayList<FakeTransaction>();
		
		FileTransfersSummary summary = statistics.getSummary(transactions);
		assertTrue(areEqual(summary.getTotalTime(), 0.0));
		assertEquals(0, summary.getAverageSpeed());
	}
	
	@Test
	public void expectsToReturnZeroAverageSpeedIfTransmissionDurationsAreMisssing() {
		
		FakeTransaction transaction1 = new FakeTransaction();
		FakeTransaction transaction2 = new FakeTransaction();
		FakeTransaction transaction3 = new FakeTransaction();
		Date startTime = addOffsetInSeconds(now(), -5);
		transaction1.transactionStartTime = null;
		transaction1.transactionEndTime = addOffsetInMilliseconds(startTime, 15500);
		transaction1.getTransmissionInfo().setEndTime(null);
		transaction2.transactionStartTime = addOffsetInSeconds(startTime, 3);
		transaction2.transactionEndTime = addOffsetInSeconds(startTime, 13);
		transaction2.getTransmissionInfo().setEndTime(null);
		transaction3.transactionStartTime = startTime;
		transaction3.transactionEndTime = null;
		transaction3.getTransmissionInfo().setEndTime(null);
		
		ArrayList<FakeTransaction> transactions = new ArrayList<FakeTransaction>();
		transactions.add(transaction1);
		transactions.add(transaction2);
		transactions.add(transaction3);
		
		FileTransfersSummary summary = statistics.getSummary(transactions);
		assertTrue(areEqual(summary.getTotalTime(), 12.5));
		assertEquals(0, summary.getAverageSpeed());
	}
	

}
