package com.wwf.shrimp.application.services.main;

import com.wwf.shrimp.application.exceptions.AuthenticationException;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.UserCredentials;

/**
 * Basic interface for a security service which will be used to authenticate and authorize users
 * and eventually groups.
 * 
 * @author AleaActaEst
 *
 */
public interface SecurityService {
	/**
	 * Specific URL mappings
	 */
	public static final String SECURITY_AUTH_LOGIN_REQUEST_URL = "security/authenticate";
	
	/**
	 * Prefix URL mappings
	 */
	public static final String SERVER_REQUEST_URL_PREFIX = "server/";
	public static final String SECURITY_REQUEST_URL_PREFIX = "security/";
	public static final String DOCUMENT_REQUEST_URL_PREFIX = "document/";
	public static final String ORGANIZATION_REQUEST_URL_PREFIX = "organization/";
	public static final String TAG_REQUEST_URL_PREFIX = "tag/";
	public static final String USER_REQUEST_URL_PREFIX = "user/";
	
	
	
	/**
	 * Logs in a user by verifying their credentials.
	 * 
	 * @param credentials - The credentials for this user
	 * @return The User entity with specific data allowed for this user.
	 * @throws AuthenticationException 
	 *     - the credentials cannot be authenticated
	 *     - the credentials are missing (null, empty etc...)
	 */
	public User login(UserCredentials credentials) throws AuthenticationException;
	
	/**
	 * Logs out the user by finding their security token in the data storage and expiring it
	 * 
	 * @param user - the user to be logged out
	 * @throws AuthenticationException
	 * 		- the credentials cannot be verified.
	 */
	public void logout(User user) throws AuthenticationException;
	
	/**
	 * Check if the given user is authenticated.
	 * This is usually done by checking some sort of a token in the back-end.
	 * NOTE: this is not to see if the user is a real user, this is to check if 
	 * the user is *currently*  (as in: they have a current authentication session) 
	 * 
	 * @param user - the user to be checked
	 * @return - true if the user is currently in session; false otherwise.
	 * @throws AuthenticationException
	 */
	public boolean isSessionAuthenticated(UserCredentials credentials) throws AuthenticationException;
	
	

}
