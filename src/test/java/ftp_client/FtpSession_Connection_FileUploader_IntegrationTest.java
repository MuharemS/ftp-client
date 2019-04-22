package ftp_client;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class FtpSession_Connection_FileUploader_IntegrationTest {

	private FakeConenctionStream controlStream;
	private FakeConenctionStream dataStream;
	private FakeControlConnectionFactory controllFactory ;
	private FakePassivePortDataConnectionFactory dataFactory ;
	private FtpSession session;
	public static File testFile = new File("fakedFile.bin");
	public static long numberOfCharacters = 7890;



	private class FakeConenctionStream implements ConnectionStream{
		private ByteArrayInputStream inStream;
		private ByteArrayOutputStream outStream;
		private Queue<String> responseQueue = new LinkedList<String>();
		private boolean isClosed = false;

		FakeConenctionStream (){

			this.inStream = new ByteArrayInputStream(new byte[1]);
			initializeOutStream();
		}

		public void initializeOutStream() {
			this.outStream = new ByteArrayOutputStream(2048);
		}

		public InputStream getInputStream() throws IOException {
			String response = new String();
			if(!responseQueue.isEmpty()) {
				response = responseQueue.remove();
			}
			return new ByteArrayInputStream(response.getBytes());
		}

		public OutputStream getOutputStream() throws IOException {
			return outStream;
		}

		public void close() {
			try {
				inStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				outStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			isClosed = true;
		}
		
		public boolean isClosed() { return isClosed; }
		
		public String getSentData() {
			String data = outStream.toString();
			initializeOutStream();
			return data;
		}

		public void clearSentData() {
			initializeOutStream();
		}

		public void prepareStream(String content) {
			inStream = new ByteArrayInputStream(content.getBytes());
		}

		public boolean isConnected() {
			return true;
		}

		public void addResponseToQueue(String response) {
			responseQueue.add(response);
		}
	}

	private class FakeControlConnectionFactory implements ControlConnectionFactory{
		private ConnectionStream stream;
		private boolean rejectConnection = false;
		
		public FakeControlConnectionFactory(ConnectionStream connectionStream) {
			this.stream = connectionStream;
		}

		public ControlConnection createConnection() throws ConnectionErrorException {
			if(rejectConnection) {
				throw new ConnectionErrorException("Could not create connection.");
			}
			try {
				return new FtpStreamControlConenction(this.stream);
			} catch (IOException e) {
				throw new ConnectionErrorException();
			}
		}
		
		public void disableConnection() {
			rejectConnection = true;
		}
		
		public void enableConnection() {
			rejectConnection = false;
		}
	}


	private class FakePassivePortDataConnectionFactory implements DataConnectionFactory{
		private ConnectionStream stream;
		private ConnectionParameters calculatedParams;
		public FakePassivePortDataConnectionFactory(ConnectionStream connectionStream) {
			this.stream = connectionStream;
		}
		public SocketStreamDataConnection createConnection(TransferControlCommands commands)
				throws ConnectionErrorException {
			calculatedParams = commands.passivePort();
			return new SocketStreamDataConnection(stream);
		}

	}

	public void prepareConnectionAck() {
		controlStream.addResponseToQueue(FtpResponseCodes.CONNECTION_ACKNOWLEDGEMENT + " Connection accepted\r\n");
	}

	public void simulateLogin() {
		controlStream.addResponseToQueue(FtpResponseCodes.CONNECTION_ACKNOWLEDGEMENT + " Connection accepted\r\n");
		String response1 = FtpResponseCodes.PASS_REQUIRED + " Enter password\r\n";
		String response2 = FtpResponseCodes.LOGIN_SUCCESSFUL + " Login successful\r\n";
		controlStream.addResponseToQueue(response1);
		controlStream.addResponseToQueue(response2);
		session.connect();
		session.logIn("fake", "fake");
		controlStream.clearSentData();

	}

	@BeforeClass
	public static void beforeClass() {
		try {
			testFile.createNewFile();
			FileWriter writer = new FileWriter(testFile);
			for(int i = 0; i < numberOfCharacters;++i) {
				writer.write((char)i);
			}
		} catch (IOException e) {
		}
	}
	
	@AfterClass
	public static void afterClass() {
		testFile.delete();
	}
	
	@Before
	public void before() throws Exception {
		controlStream = new FakeConenctionStream();
		dataStream = new FakeConenctionStream();
		controllFactory = new FakeControlConnectionFactory(controlStream);
		dataFactory = new FakePassivePortDataConnectionFactory(dataStream);
		session = new FtpSession(controllFactory, dataFactory);
	}

	@Rule
	public Timeout globaTimeout = new Timeout(10000);

	@Test
	public void connectionSuccessfulTest() {
		controlStream.addResponseToQueue(FtpResponseCodes.CONNECTION_ACKNOWLEDGEMENT + " Connection accepted\r\n");
		boolean isConnected = session.connect();
		assertTrue(isConnected);
	}
	@Test
	public void connectioUnsuccessfulTest_badResponseFromServer() {
		controlStream.addResponseToQueue("123 WrongCode\r\n");
		boolean isConnected = session.connect();
		assertFalse(isConnected);
	}

	@Test
	public void expectsToFailLoginIfSessionIsNotConnected(){
		boolean isLoggedIn = session.logIn("username", "password");
		assertFalse(isLoggedIn);
	}

	@Test
	public void expectsSuccessfullLoginIfPasswordIsNotRequired() {
		prepareConnectionAck();
		session.connect();
		controlStream.addResponseToQueue(FtpResponseCodes.LOGIN_SUCCESSFUL + " Login successful");
		boolean isLoggedIn = session.logIn("username", "password");
		String requestSent = controlStream.getSentData();
		String expectedRequest = new String(FtpCommands.USERNAME + " username\r\n");
		assertEquals(expectedRequest, requestSent);
		assertTrue(isLoggedIn);
	}

	@Test
	public void expectsSuccessfullLoginIfPasswordIsRequired() {
		prepareConnectionAck();
		session.connect();
		String response1 = FtpResponseCodes.PASS_REQUIRED + " Enter password\r\n";
		String response2 = FtpResponseCodes.LOGIN_SUCCESSFUL + " Login successful\r\n";
		controlStream.addResponseToQueue(response1);
		controlStream.addResponseToQueue(response2);

		boolean isLoggedIn = session.logIn("username", "password");

		String requestSent = controlStream.getSentData();
		String expectedDataSent = new String(FtpCommands.USERNAME + " username\r\n");
		expectedDataSent += new String(FtpCommands.PASSWORD + " password\r\n");
		assertEquals(expectedDataSent, requestSent);
		assertTrue(isLoggedIn);
	}

	@Test
	public void expectsToSuccesfullyStoreData() {
		simulateLogin();
		controlStream.addResponseToQueue(FtpResponseCodes.ENTERING_PASSIVE_MODE + " entering passive connection (192,168,10,14,134,55)\r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.READY_TO_SEND_DATA + " \r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.DATA_TRANSFER_SUCCESSFUL + " \r\n");
		String fakeFileContent = "Fake text. FileContent: !!! ";
		ByteArrayInputStream fakeFileStream = new ByteArrayInputStream(fakeFileContent.getBytes());

		boolean isFileSuccefullyStored = session.store("FakeFile.txt", fakeFileStream);
		assertTrue(isFileSuccefullyStored);
		assertEquals(fakeFileContent, dataStream.getSentData());
		assertEquals(dataFactory.calculatedParams.hostname, "192.168.10.14");
		assertEquals(dataFactory.calculatedParams.port, 134*256+55);
		assertEquals(fakeFileContent.getBytes().length, session.getTransmissionInfo().getBytesTransmited());
		assertTrue(session.getTransmissionInfo().hasEndTime());
		assertTrue(session.getTransmissionInfo().hasStartTime());
	}
	
	@Test
	public void expectsToSuccesfullyStoreDataMultipleTimes() {
		simulateLogin();
		controlStream.addResponseToQueue(FtpResponseCodes.ENTERING_PASSIVE_MODE + " entering passive connection (192,168,10,14,134,55)\r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.READY_TO_SEND_DATA + " \r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.DATA_TRANSFER_SUCCESSFUL + " \r\n");
		String fakeFileContent = "Fake text. FileContent: !!! ";
		ByteArrayInputStream fakeFileStream = new ByteArrayInputStream(fakeFileContent.getBytes());

		boolean isFileSuccefullyStored = session.store("FakeFile.txt", fakeFileStream);
		assertTrue(isFileSuccefullyStored);
		assertEquals(fakeFileContent, dataStream.getSentData());
		assertEquals(dataFactory.calculatedParams.hostname, "192.168.10.14");
		assertEquals(dataFactory.calculatedParams.port, 134*256+55);
		assertEquals(fakeFileContent.getBytes().length, session.getTransmissionInfo().getBytesTransmited());
		assertTrue(session.getTransmissionInfo().hasEndTime());
		assertTrue(session.getTransmissionInfo().hasStartTime());
		
		
		fakeFileContent = "Another file! Another content! Same functionality";
		fakeFileStream = new ByteArrayInputStream(fakeFileContent.getBytes());
		
		controlStream.addResponseToQueue(FtpResponseCodes.ENTERING_PASSIVE_MODE + " entering passive connection (192,168,10,14,134,55)\r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.READY_TO_SEND_DATA + " \r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.DATA_TRANSFER_SUCCESSFUL + " \r\n");
		
		isFileSuccefullyStored = session.store("FakeFile.txt", fakeFileStream);
		assertTrue(isFileSuccefullyStored);
		assertEquals(fakeFileContent, dataStream.getSentData());
		assertEquals(dataFactory.calculatedParams.hostname, "192.168.10.14");
		assertEquals(dataFactory.calculatedParams.port, 134*256+55);
		assertEquals(fakeFileContent.getBytes().length, session.getTransmissionInfo().getBytesTransmited());
		assertTrue(session.getTransmissionInfo().hasEndTime());
		assertTrue(session.getTransmissionInfo().hasStartTime());
	}
	
	@Test
	public void expectsFailStoringDataIfConnectionInfoIsWrong() {
		simulateLogin();
		controlStream.addResponseToQueue(FtpResponseCodes.ENTERING_PASSIVE_MODE + " entering passive connection (192,168,10,14134,55)\r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.READY_TO_SEND_DATA + " \r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.DATA_TRANSFER_SUCCESSFUL + " \r\n");
		String fakeFileContent = "Fake text. FileContent: !!! ";
		ByteArrayInputStream fakeFileStream = new ByteArrayInputStream(fakeFileContent.getBytes());

		boolean isFileSuccefullyStored = session.store("FakeFile.txt", fakeFileStream);
		assertFalse(isFileSuccefullyStored);
		assertEquals(0, session.getTransmissionInfo().getBytesTransmited());
		assertFalse(session.getTransmissionInfo().hasEndTime());
		assertFalse(session.getTransmissionInfo().hasStartTime());
		assertEquals(TransferStatus.Failed, session.getTransmissionInfo().getStatus());
	}

	@Test
	public void expectsToSuccesfullySendFileByFileUploader() {
		
		controlStream.addResponseToQueue(FtpResponseCodes.CONNECTION_ACKNOWLEDGEMENT + " Connection accepted\r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.PASS_REQUIRED + " Enter password\r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.LOGIN_SUCCESSFUL + " Login successful\r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.ENTERING_PASSIVE_MODE + " entering passive connection (192,168,10,14,134,55)\r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.READY_TO_SEND_DATA + " \r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.DATA_TRANSFER_SUCCESSFUL + " \r\n");

		FileUploader uploader = new FileUploader(testFile, new UserCredidentials("fake", "fake"), controllFactory, dataFactory);
		uploader.run();

		assertEquals(testFile.length(), uploader.getTransmissionInfo().getBytesTransmited());
		assertEquals(TransferStatus.Done, uploader.getTransmissionInfo().getStatus());
		assertTrue(controlStream.isClosed());
		assertTrue(dataStream.isClosed());
		assertTrue(uploader.getTransmissionInfo().hasEndTime());
		assertTrue(uploader.getTransmissionInfo().hasStartTime());
	}
	
	@Test
	public void expectsToFailTransferIfCouldNotConnect() {

		controlStream.addResponseToQueue(FtpResponseCodes.CONNECTION_ACKNOWLEDGEMENT + " Connection accepted\r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.LOGIN_SUCCESSFUL + " Login successful\r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.ENTERING_PASSIVE_MODE + " entering passive connection (192,168,10,14,134,55)\r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.READY_TO_SEND_DATA + " \r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.DATA_TRANSFER_SUCCESSFUL + " \r\n");
		
		FileUploader uploader = new FileUploader(testFile, new UserCredidentials("fake", "fake"), controllFactory, dataFactory);
		controllFactory.disableConnection();
		uploader.run();

		assertEquals(0l, uploader.getTransmissionInfo().getBytesTransmited());
		assertEquals(TransferStatus.Waiting, uploader.getTransmissionInfo().getStatus());
		assertFalse(uploader.getTransmissionInfo().hasEndTime());
		assertFalse(uploader.getTransmissionInfo().hasStartTime());
	}
	
	@Test
	public void expectsToFailTransferIfCouldAcknowlegeConnection() {

		controlStream.addResponseToQueue("989" + " Unknown response code\r\n");
		controlStream.addResponseToQueue(FtpResponseCodes.LOGIN_SUCCESSFUL + " Login successful\r\n");
		;
		
		FileUploader uploader = new FileUploader(testFile, new UserCredidentials("fake", "fake"), controllFactory, dataFactory);
		uploader.run();

		assertEquals(0l, uploader.getTransmissionInfo().getBytesTransmited());
		assertEquals(TransferStatus.Waiting, uploader.getTransmissionInfo().getStatus());
		assertFalse(uploader.getTransmissionInfo().hasEndTime());
		assertFalse(uploader.getTransmissionInfo().hasStartTime());
	}
	
	@Test
	public void expectsToFailTransferIfCouldNotLogin() {

		controlStream.addResponseToQueue(FtpResponseCodes.CONNECTION_ACKNOWLEDGEMENT + " Connection accepted\r\n");
		controlStream.addResponseToQueue("989" + " Unknown response code\r\n");
		
		FileUploader uploader = new FileUploader(testFile, new UserCredidentials("fake", "fake"), controllFactory, dataFactory);
		uploader.run();

		assertEquals(0l, uploader.getTransmissionInfo().getBytesTransmited());
		assertEquals(TransferStatus.Waiting, uploader.getTransmissionInfo().getStatus());
		assertFalse(uploader.getTransmissionInfo().hasEndTime());
		assertFalse(uploader.getTransmissionInfo().hasStartTime());

	}
}
