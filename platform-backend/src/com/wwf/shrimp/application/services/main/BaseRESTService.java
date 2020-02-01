package com.wwf.shrimp.application.services.main;

import org.apache.log4j.Logger;

/**
 * Base service for RESTFul Services
 * 
 * @author argolite
 *
 */
public abstract class BaseRESTService {
	
	/**
	 * Represents the Logger used to perform logging.
	 */
	private Logger log = Logger.getLogger(getClass().getName());
	
	/**
	 * @return the log
	 */
	protected Logger getLog() {
		return log;
	}

}
