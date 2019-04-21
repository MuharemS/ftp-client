package ftp_client;

public class ConnectionErrorException extends RuntimeException {

	public ConnectionErrorException() {
	}

	public ConnectionErrorException(String message) {
		super(message);
	}

	public ConnectionErrorException(Throwable cause) {
		super(cause);
	}

	public ConnectionErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectionErrorException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
