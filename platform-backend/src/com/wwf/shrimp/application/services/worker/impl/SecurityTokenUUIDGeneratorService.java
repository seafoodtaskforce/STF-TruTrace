package com.wwf.shrimp.application.services.worker.impl;

import java.util.Date;
import java.util.UUID;

import com.wwf.shrimp.application.exceptions.ServiceManagementException;
import com.wwf.shrimp.application.models.SecurityToken;
import com.wwf.shrimp.application.services.worker.SecurityTokenGeneratorService;

/**
 * Implementation of the SecurityTokenGeneratorService contract interface to 
 * generate the security token based on a UUID protocol.
 *  
 * @author AleaActaEst
 *
 */
public class SecurityTokenUUIDGeneratorService implements SecurityTokenGeneratorService {

	@Override
	/**
	 * Generate a unique token string with no expiry constraints.
	 * @return - the generated security token
	 * @throws ServiceManagementException if there were any issues with the generation process
	 */
	public SecurityToken generateUniqueToken() throws ServiceManagementException {
		
		// create the security token instance
		SecurityToken token = new SecurityToken();
		token.setExpirationDate(null);
		token.setTokenValue(UUID.randomUUID().toString());
		
		return token; 

	}

	@Override
	/**
	 * Generate a unique security string.
	 * @param expiryDate - the expiry date for the token
	 * @return - the generated security token
	 * @throws ServiceManagementException if there were any issues with the generation process.
	 *  - if the provided token is in the past this exception will be thrown
	 */
	public SecurityToken generateUniqueToken(Date expiryDate) throws ServiceManagementException {
		
		// create the security token instance
		SecurityToken token = new SecurityToken();
		token.setExpirationDate(expiryDate);
		token.setTokenValue(UUID.randomUUID().toString());
		
		return token; 
	}

}
