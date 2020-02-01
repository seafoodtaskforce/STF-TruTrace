package com.wwf.shrimp.application.exceptions;

/**
 * This exception will be thrown to indicate any 
 * configuration error.
 * 
 * @author AleaActaEst
 *
 */
public class ConfigurationException extends RuntimeException {

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigurationException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7806978452046998316L;

}
