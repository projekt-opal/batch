package org.dice_research.opal.batch.configuration;

/**
 * Runtime exception thrown on configuration errors.
 *
 * @author Adrian Wilke
 */
public class CfgException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CfgException() {
		super();
	}

	public CfgException(String message) {
		super(message);
	}

	public CfgException(String message, Throwable cause) {
		super(message, cause);
	}

	public CfgException(Throwable cause) {
		super(cause);
	}

	protected CfgException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}