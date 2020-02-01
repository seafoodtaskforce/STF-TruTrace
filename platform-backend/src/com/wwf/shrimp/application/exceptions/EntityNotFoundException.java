package com.wwf.shrimp.application.exceptions;

/**
 * The top-level exception for not found entity in the data store
 * @author AleaActaEst
 *
 */
public class EntityNotFoundException extends PersistenceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 163898325828268454L;

	public EntityNotFoundException(String message) {
		super(message);
	}
	
	public EntityNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
