package com.wwf.shrimp.application.services.worker;

import java.util.Date;

import com.wwf.shrimp.application.exceptions.ServiceManagementException;
import com.wwf.shrimp.application.models.SecurityToken;

/**
 * This is a contract for security token generation.
 * It will allow for different ways of generating authentication
 * tokens.
 * 
 * @author AleaActaEst
 *
 */
public interface SecurityTokenGeneratorService {
	/**
	 * Generate a unique token string with no expiry constraints.
	 * @param seed - an optional seed to be used in creating the token
	 * @return - the generated security token
	 * @throws ServiceManagementException if there were any issues with the generation process
	 */
	public SecurityToken generateUniqueToken(String... seed) throws ServiceManagementException ;
	
	/**
	 * Generate a unique security string.
	 * @param expiryDate - the expiry date for the token
	 * @param seed - an optional seed to be used in creating the token
	 * @return - the generated security token
	 * @throws ServiceManagementException if there were any issues with the generation process.
	 *  - if the provided token is in the past this exception will be thrown
	 */
	public SecurityToken generateUniqueToken(Date expiryDate, String... seed) throws ServiceManagementException;
}
