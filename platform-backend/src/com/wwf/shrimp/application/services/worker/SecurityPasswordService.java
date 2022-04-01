package com.wwf.shrimp.application.services.worker;

import com.wwf.shrimp.application.exceptions.ServiceManagementException;

/**
 * This is a contract for security token generation.
 * It will allow for different ways of generating authentication
 * tokens.
 * 
 * @author AleaActaEst
 *
 */
public interface SecurityPasswordService {
	/**
	 * Generate a new password for the user.
	 * @param seed - an optional seed to be used in creating the password
	 * @return - the generated new password
	 * @throws ServiceManagementException if there were any issues with the generation process
	 */
	public String generateNewPassword(String... seed) throws ServiceManagementException ;
	
	/**
	 * Check how strong the password is.
	 * @param password - the password to be tested
	 * @return - the strength measurement as a value from 1 to 10 with 1-3 being week, 
	 * 			 4-7 being medium, 8-9 being string, and 10 being very strong.
	 * @throws ServiceManagementException if there were any issues with the generation process.
	 */
	public int testPasswordStrength(String password) throws ServiceManagementException;
}
