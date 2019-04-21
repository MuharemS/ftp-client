package ftp_client;

public class CommandStreamException extends RuntimeException {

	public CommandStreamException() {
		super();
	}

	public CommandStreamException(String message) {
		super(message);
	}

	public CommandStreamException(Throwable cause) {
		super(cause);
	}

	public CommandStreamException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandStreamException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
