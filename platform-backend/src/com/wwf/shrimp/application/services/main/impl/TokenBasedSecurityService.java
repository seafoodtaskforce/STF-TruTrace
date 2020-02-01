package com.wwf.shrimp.application.services.main.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import com.wwf.shrimp.application.exceptions.AuthenticationException;
import com.wwf.shrimp.application.exceptions.PersistenceException;
import com.wwf.shrimp.application.exceptions.ServiceManagementException;
import com.wwf.shrimp.application.models.AppResource;
import com.wwf.shrimp.application.models.LookupEntity;
import com.wwf.shrimp.application.models.SecurityToken;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.UserCredentials;
import com.wwf.shrimp.application.models.search.LookupDataSearchCriteria;
import com.wwf.shrimp.application.models.search.UserSearchCriteria;
import com.wwf.shrimp.application.services.main.Authenticator;
import com.wwf.shrimp.application.services.main.BaseSecurityService;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.LookupDataMySQLDao;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.UserMySQLDao;
import com.wwf.shrimp.application.services.worker.SecurityTokenGeneratorService;
import com.wwf.shrimp.application.services.worker.impl.SecurityTokenUUIDGeneratorService;

/**
 * Token Based SecurityService implementation which checks user's
 * authentication credentials and session credentials against a database 
 * token.
 * @author argolite
 *
 */
public class TokenBasedSecurityService extends BaseSecurityService {
	
	/**
	 * default expiry for the token
	 */
	public final static int TOKEN_EXPIRY_INTERVAL_SECONDS = 3600;
	
	/**
	 * expected length of the token
	 */
	private final static int TOKEN_EXPECTED_LENGTH = 36;
	
	// Token generator for authentication
	private SecurityTokenGeneratorService tokenGenerator;
	private UserMySQLDao<User, UserSearchCriteria> userService = new UserMySQLDao<User, UserSearchCriteria>();
	private LookupDataMySQLDao<LookupEntity, LookupDataSearchCriteria> lookupService = new LookupDataMySQLDao<LookupEntity, LookupDataSearchCriteria>();

	/**
	 * Login the user by generating a unique token to be stored for 
	 * the duration of the session. 
	 * 
	 * @param credentials - The credentials for this user
	 * @return The User entity with specific data allowed for this user.
	 * @throws AuthenticationException 
	 *     - the credentials cannot be authenticated
	 *     - the credentials are missing (null, empty etc...)
	 */
	@Override
	public User login(UserCredentials credentials) throws AuthenticationException {
		User user = null;
		List<AppResource> allResources=null;
		SecurityToken token = new SecurityToken();
		Authenticator authenticator = new PasswordAuthenticator();
		UserCredentials returnCredentials = new UserCredentials();
		
		// initialize the user service
		userService.init();
		lookupService.init();
		
		//
		// initialize the return credentials
		returnCredentials.setUsername(credentials.getUsername());
		returnCredentials.setRequestOrigin(credentials.getRequestOrigin());
		
		//
		// Find the user in the data store and verify their credentials
		try {
			user = userService.getUserByName(credentials.getUsername()) ;

		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException(
					"User cannot be logged in. Cannot get find user in persistence: " 
					+ credentials.getUsername(), 
					e);
		}
		
		//
		// authenticate the user
		if(user != null && authenticator.isAuthenticated(credentials)){
			getLog().info("User is authenticated: - " + user.toString());
			//
			// token generation
			tokenGenerator = new SecurityTokenUUIDGeneratorService();
			// get the expire date
			Date expiryDate = new Date();
			expiryDate = DateUtils.addSeconds(expiryDate, TOKEN_EXPIRY_INTERVAL_SECONDS);
			try {
				token = tokenGenerator.generateUniqueToken(expiryDate);
				returnCredentials.setToken(token);
				user.setCredentials(returnCredentials);
				//
				// store the token in persistence;
				userService.writeUserToken(returnCredentials);
				//
				// finally get the resources
				allResources = lookupService.getAllAppResources();
				user.setAppResources(allResources);
				
			} catch (ServiceManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else{
			getLog().info("User is *NOT* authenticated: - " + returnCredentials.getUsername());
		}
		
		//
		// return the proper result to the user
		return user;
		
	}
	
	/**
	 * Log an existing user out. 
	 * Here we find the user and invalidate the last token in their history.
	 * 
	 * @param user - The user to logout; only the username is needed.
	 * @return - nothing to return
	 * @throws AuthenticationException 
	 *     - the user cannot be found
	 *     - the credentials are missing (null, empty etc...)
	 */
	@Override
	public void logout(User user) throws AuthenticationException {
		User foundUser = null;
		List<UserCredentials> credentials = null;
		
		
		// initialize the user service
		userService.init();

		//
		// Find the user in the database
		try {
			foundUser = userService.getUserByName(user.getName());
			if(foundUser == null){
				getLog().debug("Logout operation is ignored. Cannot get find user in persistence: " 
						+ user.getName());
				throw new AuthenticationException(
						"Logout operation is ignored. Cannot get find user in persistence: " 
						+ user.getName());
			}
		} catch (PersistenceException e1) {
			e1.printStackTrace();
			getLog().error("Logout operation cannot be performed: " + user.getName());
			throw new AuthenticationException(
					"Logout operation cannot be performed: " 
					+ user.getName(),
					e1);
		}

		
		
		//
		// If found then test the provided token against the token in the database
		try {
			credentials = userService.fetchUserTokens(user.getName());
		} catch (PersistenceException e1) {
			e1.printStackTrace();
			getLog().error("Logout operation cannot be performed: " + user.getName());
			throw new AuthenticationException(
					"Logout operation cannot be performed: " 
					+ user.getName(),
					e1);
		}
		
		//
		// Expire the token if found otherwise throw an exception
		if(credentials != null && !credentials.isEmpty()){
			try {
				userService.invalidateUserToken(credentials.get(0));
			} catch (PersistenceException e) {
				e.printStackTrace();
				getLog().error("Logout operation is ignored. Failed to get token for the user: " + user.getName());
				throw new AuthenticationException(
						"Logout operation is ignored. Failed to get token for the user:  " 
						+ user.getName());
			} 
		}
		
		
	}

	@Override
	/**
	 * Test if the session has been authenticated for the given user credentials
	 * 
	 * @param userCredentials - The credentials for this user
	 * @return true if the credentials are verified and still active; false otherwise
	 */
	public boolean isSessionAuthenticated(UserCredentials userCredentials) throws AuthenticationException {
		User foundUser = null;
		List<UserCredentials> persistedTokenData = null;
		
		
		// initialize the user service
		userService.init();

		//
		// Find the user in the database
		try {
			foundUser = userService.getUserByName(userCredentials.getUsername());
			if(foundUser == null){
				getLog().debug("Session Authentication Failed. Cannot get find user in persistence: " + userCredentials.getUsername());
				throw new AuthenticationException(
						"Session Authentication Failed. Cannot get find user in persistence: " 
						+ userCredentials.getUsername());
			}
		} catch (PersistenceException e1) {
			e1.printStackTrace();
			getLog().error("Session Authentication Failed: " + userCredentials.getUsername());
			throw new AuthenticationException(
					"Session Authentication Failed: " 
					+ userCredentials.getUsername(),
					e1);
		}

		
		
		//
		// If found then test the provided token against the token in the database
		try {
			persistedTokenData = userService.fetchUserTokens(userCredentials.getUsername());
		} catch (PersistenceException e1) {
			e1.printStackTrace();
			getLog().error("Session Authentication Failed: " + userCredentials.getUsername());
			throw new AuthenticationException(
					"Session Authentication Failed: " 
					+ userCredentials.getUsername(),
					e1);
		}
		
		//
		// Test the token if found and still valid otherwise faile the request
		getLog().info("Testing for session token for the user");
		if(persistedTokenData != null && !persistedTokenData.isEmpty()){
			for(int i=0; i< persistedTokenData.size(); i++){
				// Compare the tokens
				// <TODO> later this will need to be changed to DB based check
				String tokenValue = persistedTokenData.get(i).getToken().getTokenValue();
				boolean isValid = !persistedTokenData.get(i).getToken().isInvalidated();
				Date tokenExpirationDate =  persistedTokenData.get(i).getToken().getExpirationDate();
				
				if(tokenValue.equals(userCredentials.getToken().getTokenValue())){
					if(isValid){
						if(tokenExpirationDate.compareTo(new Date()) > 0 ){
							getLog().debug("Token was found and verified");
							return true;
						}
					}
				}
			}
		}
		
		// no token was found in session for this user
		getLog().debug("Token was *NOT* found");
		return false;
	}
	
	/**
	 * Get the expected length of a token
	 * @return - the expected length
	 */
	protected int expectedTokenLength(){
		return TOKEN_EXPECTED_LENGTH;
	}

}
