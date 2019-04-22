package ftp_client;

import java.io.File;
import org.apache.commons.cli.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ftp_client.UserCredidentials;
import ftp_client.connection.ConnectionParameters;
import ftp_client.connection.ControlConnectionFactory;
import ftp_client.connection.DataConnectionFactory;
import ftp_client.connection.PassiveFtpDataConnectionFactory;
import ftp_client.connection.SocketControlConnectionFactory;
import ftp_client.monitor.ConsoleTransactionStatisticsPrinter;
import ftp_client.monitor.DataTransferMonitoringService;
import ftp_client.monitor.DefaultTransactionStatisticsFormater;
import ftp_client.monitor.FileTransactionStatisticsService;

public class FtpClient {
	private static String username = "user";
	private static String password = "pass";
	private static String host = "localhost";
	private static int PORT = 21; 

	private static String[] requestedFilesnames;
	private static Map<String, File> filesForUpload;
	private static List<String> rejectedFiles = new ArrayList<String>();

	final private static String OPTION_USERNAME = "u";
	final private static String OPTION_PASSWORD = "p";
	final private static String OPTION_SERVER = "server";
	final private static String OPTION_FILES = "files";

	final private static String FILENAME_SEPARATOR = ";";


	public static void main(String[] args) throws IOException, InterruptedException {

		try {
			readArguments(args); 
		}catch(ParseException e) {
			System.exit(1);
		}

		filesForUpload = getValidFiles();
		if(filesForUpload.size() == 0) {
			System.out.println("Nothing for upload. Stopping the client.");
			System.exit(1);
		} 
		ControlConnectionFactory controlConnectionFactory = new SocketControlConnectionFactory(new ConnectionParameters(host, PORT));
		DataConnectionFactory dataConnectionFactory = new PassiveFtpDataConnectionFactory();

		if(!validateConnection(controlConnectionFactory, dataConnectionFactory)) {
			System.exit(1);
		}

		List<FileUploader> workers = new ArrayList<FileUploader>();
		ExecutorService executor = Executors.newFixedThreadPool(2);
		for(Map.Entry<String, File> entry: filesForUpload.entrySet()) {
			FileUploader worker = new FileUploader(entry.getValue(), new UserCredidentials(username, password),controlConnectionFactory, dataConnectionFactory );
			workers.add(worker);
			executor.execute(worker);
		}

		DefaultTransactionStatisticsFormater formater = new DefaultTransactionStatisticsFormater();
		ConsoleTransactionStatisticsPrinter printer = new ConsoleTransactionStatisticsPrinter(formater);
		printer.registerRejectedFiles(rejectedFiles);
		FileTransactionStatisticsService statisticsService = new FileTransactionStatisticsService();
		DataTransferMonitoringService monitorService = new DataTransferMonitoringService(workers, statisticsService, printer);
		Thread monitorThread = new Thread(monitorService);
		monitorThread.start();

		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.DAYS);


		Thread.sleep(1000);
		monitorService.stop();
		monitorThread.join();


		return;

	}

	private static Map<String, File> getValidFiles(){
		Map<String,File> files = new HashMap<String,File>();
		for(String filename : requestedFilesnames) {
			try {
				if(!files.containsKey(filename))
					files.put(filename, getFileIfExists(filename));
			}catch(IllegalArgumentException e){
				rejectedFiles.add(filename);
				System.out.println("Could not read the file. " + e.getMessage() + " Skipping the file!");
			}
		}
		return files;
	}

	private static File getFileIfExists(String filename) {
		File file = new File(filename);
		if(!file.exists()) {
			throw new IllegalArgumentException(filename + " does not exist!");
		}
		if(file.isDirectory()) {
			throw new IllegalArgumentException(filename + " is directory");
		}
		return file;
	}

	private static void readArguments(String[] args) throws ParseException {
		Options options = new Options();

		Option user = new Option(OPTION_USERNAME, true, "Defines username. [Default = user]");
		user.setRequired(false);
		options.addOption(user);

		Option passwotd = new Option(OPTION_PASSWORD, true, "Defines password. [Default = pass]");
		passwotd.setRequired(false);
		options.addOption(passwotd);

		Option server = new Option(OPTION_SERVER, true, "IP addres of ftp server. [Default = localhost]");
		server.setRequired(false);
		options.addOption(server);

		Option files = new Option(OPTION_FILES, true, "Filenames for upload. [Required]");
		files.setRequired(true);
		options.addOption(files);

		CommandLineParser clParser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = clParser.parse(options, args);
		}catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("FTPClient", options);

			throw e;
		}

		if(cmd.hasOption(OPTION_USERNAME)) {
			username = cmd.getOptionValue(OPTION_USERNAME);
		}
		if(cmd.hasOption(OPTION_PASSWORD)) {
			password = cmd.getOptionValue(OPTION_PASSWORD);
		}
		if(cmd.hasOption(OPTION_SERVER)) {
			host = cmd.getOptionValue(OPTION_SERVER);
		}

		requestedFilesnames = cmd.getOptionValue(OPTION_FILES).split(FILENAME_SEPARATOR);
	}

	private static boolean validateConnection(ControlConnectionFactory controlConnectionFactory, DataConnectionFactory dataConnectionFactory) {
		FtpSession tempSession = new FtpSession(controlConnectionFactory, dataConnectionFactory);
		if(tempSession.connect()) {
			if(tempSession.logIn(username, password)) {
				return true;
			}else {
				System.out.println("Could not login!");
			}
		}else {
			System.out.println("Could not connect!");
		}
		return false;
	}



}
