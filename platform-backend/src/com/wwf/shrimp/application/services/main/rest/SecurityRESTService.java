package com.wwf.shrimp.application.services.main.rest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import com.wwf.shrimp.application.models.PasswordCredentials;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.UserCredentials;
import com.wwf.shrimp.application.services.main.BaseRESTService;
import com.wwf.shrimp.application.services.main.SecurityService;
import com.wwf.shrimp.application.services.main.impl.TokenBasedSecurityService;
import com.wwf.shrimp.application.utils.RESTUtility;

/**
 * General security RESTful service which will have the functionality for authentication
 * and authorization functionality.  
 * 
 * <TODO> Change the verbs to be fully restful and go over the RESTFul designation
 * @author AleaActaEst
 *
 */
@Path("/security")
public class SecurityRESTService extends BaseRESTService {
	
	public final static int TOKEN_EXPIRY_INTERVAL_SECONDS = 3600;
	
	//
	// Token based security service
	private SecurityService securityService =  new TokenBasedSecurityService();
		
	@POST
	@Path("/authenticate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * This is the authentication method which will accept the user's credentials
	 * and will then verify them against the data store and will either generate an auth token
	 * @param incomingData which is expected to hold the 
	 * request data with user-name and password
	 * @return the response which will contain either the response 
	 * with the authentication token or an error code:
	 *     1. SecurityToken - if no issues
	 *     2. Error String if there was an issue
	 */
	public Response authenticate(InputStream incomingData) {

		PasswordCredentials credentials = null;
		User user=null;
		Status httpResponseStatus = null;
		String responseMessage=null;
		
				
		/**
		 * Process the request
		 */
		try {
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
			
			//
			// get the parser ready
			Gson gson = new Gson();
			// parse the JSON input into the specific class
			credentials = gson.fromJson(reader, PasswordCredentials.class);
			getLog().info("Credentials from client: - " + credentials.toString());
			
			//
			// Test that the user indeed exists in the database
			user = securityService.login(credentials);
			
			/**
			 * If the user has not been rejected we create authentication token
			 */
			if(user == null){
				getLog().info("User is *NOT* authenticated (not found): - " + credentials.getUsername());
				httpResponseStatus = Status.UNAUTHORIZED;
				responseMessage = "Wrong credentials.";
				user = initBlankUser((UserCredentials) credentials);
				
			}else if(user.getCredentials().getToken() == null){
				getLog().info("User is *NOT* authenticated (bad credentials): - " + credentials.getUsername());
				httpResponseStatus = Status.UNAUTHORIZED;
				responseMessage = "Wrong credentials.";
				user = initBlankUser((UserCredentials) credentials);
				
			}else{
				getLog().info("User is authenticated: - " + user.toString());
				httpResponseStatus = Status.OK;
				responseMessage = "Ok";
				
			}

		} catch (Exception e) {
			getLog().error("Error Parsing: - " + e);
		}

		
		// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(RESTUtility.getJSON(user)).build();
	}
	
	
	@POST
	@Path("/logout")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This is the authentication method which will accept the user's credentials
	 * and will then verify them against the data store and will either generate an auth token
	 * @param incomingData which is expected to hold the 
	 * request data with user-name and password
	 * @return the response which will contain either the response 
	 * with the authentication token or an error code:
	 *     1. SecurityToken - if no issues
	 *     2. Error String if there was an issue
	 */
	public Response logout(InputStream incomingData) {
		UserCredentials credentials = null;
		User user=null;
		Status httpResponseStatus;
		String responseMessage="";
				
		/**
		 * Process the request
		 */
		try {
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
			
			//
			// get the parser ready
			Gson gson = new Gson();
			// parse the JSON input into the specific class
			credentials = gson.fromJson(reader, UserCredentials.class);
			getLog().info("Logout Credentials from client: - " + credentials.toString());
			
			user = new User();
			user.setName(credentials.getUsername());
			user.setCredentials(credentials);
			
			//
			// Test that the user indeed exists in the database
			securityService.logout(user);
			httpResponseStatus = Status.OK;
			responseMessage = "Ok";
			
		} catch (Exception e) {
			getLog().error("Error Parsing: - " + e);
			// we ignore any errors for the server response
			httpResponseStatus = Status.FORBIDDEN;
			responseMessage = "Logout was rejected";
		}
		// System.out.println("Data Received: " + RESTUtility.getJSON(credentials));

		// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(responseMessage).build();
	}
	
	
	/**************************************************************************************
	 * Private helper methods
	 */
	
	/**
	 * Helper method for creating a blank user as a return data.
	 * 
	 * @param credentials - the credentials to initialize the data with 
	 * @return - the initialized blank user
	 */
	private User initBlankUser(UserCredentials credentials){
		User user = new User();
		user.getCredentials().setRequestOrigin(credentials.getRequestOrigin());
		user.getCredentials().setUsername(credentials.getUsername());
		
		return user;
		
	}
	
}
