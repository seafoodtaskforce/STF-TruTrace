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
	 * @param seed - an optional seed to be used in creating the token
	 * @return - the generated security token
	 * @throws ServiceManagementException if there were any issues with the generation process
	 */
	public SecurityToken generateUniqueToken(String... seed) throws ServiceManagementException {
		
		// Check optional seed
		String optionalSeed = seed.length > 0 ? seed[0] : null;
		
		// create the security token instance
		SecurityToken token = new SecurityToken();
		token.setSeed(optionalSeed);
		token.setExpirationDate(null);
		token.setTokenValue(UUID.randomUUID().toString());
		
		return token; 

	}

	@Override
	/**
	 * Generate a unique security string.
	 * @param expiryDate - the expiry date for the token
	 * @param seed - an optional seed to be used in creating the token
	 * @return - the generated security token
	 * @throws ServiceManagementException if there were any issues with the generation process.
	 *  - if the provided token is in the past this exception will be thrown
	 */
	public SecurityToken generateUniqueToken(Date expiryDate, String... seed) throws ServiceManagementException {
		
		// Check optional seed
		String optionalSeed = seed.length > 0 ? seed[0] : null;
		
		// create the security token instance
		SecurityToken token = new SecurityToken();
		token.setSeed(optionalSeed);
		token.setExpirationDate(expiryDate);
		token.setTokenValue(UUID.randomUUID().toString());
		
		return token; 
	}

}
