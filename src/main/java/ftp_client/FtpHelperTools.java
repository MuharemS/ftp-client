package ftp_client;

public class FtpHelperTools {
	private static final String WHITESPACE = " ";
	public static boolean isResponseCode(String response, String code) {
		return response.startsWith(code);
	}
	
	public static String createFtpCommand(String command, String parameter) {
		return command + WHITESPACE + parameter;
	}
}
