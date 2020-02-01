package com.wwf.shrimp.application.exceptions;

/**
 * THe top-level exception for authorization.
 * @author AleaActaEst
 *
 */
public class AuthorizationException extends ServiceManagementException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5501117194004891343L;

	public AuthorizationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public AuthorizationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
