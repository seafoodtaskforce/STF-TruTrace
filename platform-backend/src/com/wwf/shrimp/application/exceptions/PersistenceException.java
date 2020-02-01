package com.wwf.shrimp.application.exceptions;

/**
 * General exception for persistence.
 * @author AleaActaEst
 *
 */
public class PersistenceException extends ServiceManagementException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6795808550099183369L;

	public PersistenceException(String message) {
		super(message);
	}
	
	public PersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

}
