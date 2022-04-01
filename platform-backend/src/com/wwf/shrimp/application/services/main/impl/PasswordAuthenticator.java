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
import com.wwf.shrimp.application.services.main.ConfigurationService;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.UserMySQLDao;
import com.wwf.shrimp.application.utils.HashingUtils;
import com.wwf.shrimp.application.utils.SingletonMapGlobal;

/**
 * Simple password authenticator based on username and password pair
 *  
 * @author argolite
 *
 */
public class PasswordAuthenticator implements Authenticator {
	
	// Configuration Service
    ConfigurationService configService = new PropertyConfigurationService();
	//
	// Diagnostics
	private SingletonMapGlobal DIAGNOSTIC_MAP = SingletonMapGlobal.getInstance();
	

	
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
		final String DIAGNOSTIC_KEY =  DIAGNOSTIC_MAP.getDiagnosticKey();
		
		DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
				, "<BACK-Password><Info> Using Password Authenticator");
		
		//
		// Check the type of credentials
		if(PasswordCredentials.class.isInstance( credentials )){
			// cast the credentials
			passwordCredentials = (PasswordCredentials) credentials;
			DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
					, "<BACK-Password><Info> Using Password Authenticator <verified>");
			DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
					, "<BACK-Password><Info> Using Password Authenticator <verified>: " + passwordCredentials);
			
			//
			// process the actual data
			
			// precondition
			if((passwordCredentials.getPassword() == null 
					|| passwordCredentials.getPassword().isEmpty())
					&& passwordCredentials.getToken() == null){
				DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
						, "<BACK-Password><Error> Credentials are is missing. Expecting Credentials for " 
						+ credentials.getUsername());
				throw new AuthenticationException(
						"Credentials are is missing. Expecting Credentials for " 
						+ credentials.getUsername());
			}
			
			/**
			 * Check if password and username have been passed along
			 */
			if(passwordCredentials.getPassword() != null 
					&& !passwordCredentials.getPassword().isEmpty()){
				
				DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
						, "<BACK-Password><Info> Looking for <password match> for user: " 
						+ credentials.getUsername());
				//
				// check if the user and password matches
				userService.init();
				//
				// check that the password matches
				try {
					userPassword = userService.fetchUserPassword(credentials.getUsername());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
							, "<BACK-Password><ERROR> Exception when Looking for <password match> " 
							+ e.getMessage());
					throw new AuthenticationException(
							"User password failed for user " 
							+ credentials.getUsername(), 
							e);
				}
				
				//
				// are we hashing the passwords
				this.configService.open();
				if(Boolean.parseBoolean(this.configService.readConfigurationProperty("security.psw.hash"))){
					DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
							, "<BACK-Password><Info> Password is *IS* Hashed ");
					String hashedPassword = HashingUtils.hashStringOneWay(passwordCredentials.getPassword());
					if(hashedPassword.equals(userPassword)){
						result = true;
						return result;
					}
				}else{
					DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
							, "<BACK-Password><Info> Password *NOT* Hashed ");
					if(passwordCredentials.getPassword().equals(userPassword)){
						result = true;
						return result;
					}
				}
			}
			/**
			 * Check if token has been passed along
			 */
			if(passwordCredentials.getToken() != null && passwordCredentials.getUsername() != null ){
				//
				// compare the token provided to the one stored for the user
				try {
					DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
							, "<BACK-Password><Info> Looking for <token> for user: " 
							+ credentials.getUsername());
					List<UserCredentials> tokens = userService.fetchUserTokens(passwordCredentials.getUsername());
					Iterator<UserCredentials> iter = tokens.iterator();
					while(iter.hasNext()){
						UserCredentials token = iter.next();
						if(token.getToken().getTokenValue().equals(passwordCredentials.getToken().getTokenValue())){
							result = true;
							return result;
						}
					}
					DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
							, "<BACK-Password><Info> <token> was *NOT* found for user: " 
							+ credentials.getUsername());
				} catch (PersistenceException e) {
					// TODO Auto-generated catch block
					DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
							, "<BACK-Password><ERROR> exception thrown: " + e.getMessage());
					e.printStackTrace();
				}
			}

		//
		// Wrong type of credentials provided
		}else{
			DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
					, "<BACK-Password><ERROR> exception thrown: Incorrect type of credentials. Expecting PasswordCredentials for " 
							+ credentials.getUsername());
			throw new AuthenticationException(
					"Incorrect type of credentials. Expecting PasswordCredentials for " 
					+ credentials.getUsername());
		}
		
		
		return result;
	}

}
