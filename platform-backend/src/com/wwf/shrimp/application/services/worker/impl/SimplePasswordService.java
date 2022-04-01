/**
 * 
 */
package com.wwf.shrimp.application.services.worker.impl;

import com.wwf.shrimp.application.exceptions.ServiceManagementException;
import com.wwf.shrimp.application.services.worker.SecurityPasswordService;
import com.wwf.shrimp.application.utils.PasswordGenerator;

/**
 * Simple implementation of the password generation service.
 * This implementation generates a new password.
 * @author AleaActaEst
 *
 */
public class SimplePasswordService implements SecurityPasswordService {

	/**
	 * Generate a new password for the user.
	 * @param seed - an optional seed to be used in creating the password
	 * @return - the generated new password
	 * @throws ServiceManagementException if there were any issues with the generation process
	 */
	@Override
	public String generateNewPassword(String... seed) throws ServiceManagementException {
		return PasswordGenerator.generateStrongPassword();
	}

	/* (non-Javadoc)
	 * @see com.wwf.shrimp.application.services.worker.SecurityPasswordService#testPasswordStrength(java.lang.String)
	 */
	@Override
	public int testPasswordStrength(String password) throws ServiceManagementException {
		// TODO Auto-generated method stub
		return 0;
	}

}
