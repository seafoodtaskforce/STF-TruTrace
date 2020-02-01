package com.wwf.shrimp.application.services.main.impl;

import java.util.Iterator;
import java.util.List;

import com.wwf.shrimp.application.exceptions.AuthenticationException;
import com.wwf.shrimp.application.exceptions.PersistenceException;
import com.wwf.shrimp.application.models.PasswordCredentials;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.UserCredentials;
import com.wwf.shrimp.application.models.search.UserSearchCriteria;
import com.wwf.shrimp.application.services.main.Authenticator;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.UserMySQLDao;

/**
 * Simple password authenticator based on username and password pair
 *  
 * @author argolite
 *
 */
public class PasswordAuthenticator implements Authenticator {

	
	@Override
	/**
	 * Check if the credentials are valid
	 * 
	 * 
	 */
	public boolean isAuthenticated(UserCredentials credentials) throws AuthenticationException {
		boolean result = false;
		PasswordCredentials passwordCredentials = null;
		String userPassword = null;
		UserMySQLDao<User, UserSearchCriteria> userService = new UserMySQLDao<User, UserSearchCriteria>();
		
		//
		// Check the type of credentials
		if(PasswordCredentials.class.isInstance( credentials )){
			// cast the credentials
			passwordCredentials = (PasswordCredentials) credentials;
			
			//
			// process the actual data
			
			// precondition
			if((passwordCredentials.getPassword() == null 
					|| passwordCredentials.getPassword().isEmpty())
					&& passwordCredentials.getToken() == null){
				throw new AuthenticationException(
						"Credentials are is missing. Expecting Credentials for " 
						+ credentials.getUsername());
			}
			
			/**
			 * Check if password and username have been passed along
			 */
			if(passwordCredentials.getPassword() != null 
					&& !passwordCredentials.getPassword().isEmpty()){
				//
				// check if the user and password matches
				userService.init();
				//
				// check that the password matches
				try {
					userPassword = userService.fetchUserPassword(credentials.getUsername());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new AuthenticationException(
							"User password failed for user " 
							+ credentials.getUsername(), 
							e);
				}
				if(passwordCredentials.getPassword().equals(userPassword)){
					result = true;
					return result;
				}
			}
			/**
			 * Check if token has been passed along
			 */
			if(passwordCredentials.getToken() != null && passwordCredentials.getUsername() != null ){
				//
				// compare the token provided to the one stored for the user
				try {
					List<UserCredentials> tokens = userService.fetchUserTokens(passwordCredentials.getUsername());
					Iterator<UserCredentials> iter = tokens.iterator();
					while(iter.hasNext()){
						UserCredentials token = iter.next();
						if(token.equals(passwordCredentials.getToken())){
							result = true;
							return result;
						}
					}
				} catch (PersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		//
		// Wrong type of credentials provided
		}else{
			throw new AuthenticationException(
					"Incorrect type of credentials. Expecting PasswordCredentials for " 
					+ credentials.getUsername());
		}
		
		
		return result;
	}

}
