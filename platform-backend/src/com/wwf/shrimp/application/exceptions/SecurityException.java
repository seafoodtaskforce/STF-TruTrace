package com.wwf.shrimp.application.exceptions;

/**
 * General exception for security issues
 * @author AleaActaEst
 *
 */
public class SecurityException extends ServiceManagementException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5359121407610445776L;

	public SecurityException(String message) {
		super(message);
	}
	
	public SecurityException(String message, Throwable cause) {
		super(message, cause);
	}

}
