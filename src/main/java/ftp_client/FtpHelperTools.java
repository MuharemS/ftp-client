package ftp_client;

public class FtpHelperTools {
	
	public static boolean isResponseWithCode(String response, String code) {
		return response.startsWith(code);
	}
}
