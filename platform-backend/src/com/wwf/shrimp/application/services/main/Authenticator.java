package com.wwf.shrimp.application.services.main;

import com.wwf.shrimp.application.exceptions.AuthenticationException;
import com.wwf.shrimp.application.models.UserCredentials;

/**
 * This interface will define the contract for authenticating input credentials
 * @author argolite
 * 
 */
public interface Authenticator {
	
	/**
	 * Test if the specific credentials can be authenticated
	 * @param credentials - the credentials to authenticate
	 * @return - true if authenticated; false otherwise
	 * @throws AuthenticationException
	 * 		- if there were any issues with the authentication process
	 */
	public boolean isAuthenticated(UserCredentials credentials) throws AuthenticationException;

}
