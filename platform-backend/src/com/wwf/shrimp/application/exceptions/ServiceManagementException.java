package com.wwf.shrimp.application.exceptions;

/**
 * This is the base class for all exceptions thrown by 
 * back-end services.
 * 
 * @author AleaActaEst
 *
 */
public class ServiceManagementException extends Exception {

	public ServiceManagementException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceManagementException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2315662291724979057L;

}
