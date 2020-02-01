package com.wwf.shrimp.application.exceptions;

/**
 * Top-level exception for authentication
 * @author AleaActaEst
 *
 */
public class AuthenticationException extends ServiceManagementException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3373157691516689052L;

	public AuthenticationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public AuthenticationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
