package com.wwf.shrimp.application.services.main.rest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
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
import com.wwf.shrimp.application.utils.SingletonMapGlobal;

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
	
	//
	// Token based security service
	private SecurityService securityService =  new TokenBasedSecurityService();
	//
	// Diagnostics
	private SingletonMapGlobal DIAGNOSTIC_MAP = SingletonMapGlobal.getInstance();
	
		
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
	public Response authenticate(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("") @HeaderParam("origin") String origin,
			@DefaultValue("") @HeaderParam("device-id") String deviceID) {
		
		
		// final String DIAGNOSTIC_KEY = "SecurityRESTService.authenticate";
		final String DIAGNOSTIC_KEY =  DIAGNOSTIC_MAP.getDiagnosticKey();
		PasswordCredentials credentials = null;
		User user=null;
		Status httpResponseStatus = null;
		String responseMessage=null;
		List<String> diagnostics = new ArrayList<String>();
		DIAGNOSTIC_MAP.clearDiagnostics(DIAGNOSTIC_KEY);
		
		DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY, "<REST><Info> <IP Address>: - " + DIAGNOSTIC_MAP.getIPAddress());
		DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY, "<REST><Info> <External IP ADdress>: - " + DIAGNOSTIC_MAP.getExternalIP());
		DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY, "<REST><Info> <Host>: - " + DIAGNOSTIC_MAP.getIPAddressHost());
		
				
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
			DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY, "<REST><Info> Credentials from client: - " + credentials.toString());
			
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
				DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
						, "<REST><Error> User is *NOT* authenticated (not found): - " + credentials.getUsername());
				
			}else if(user.getCredentials().getToken() == null){
				getLog().info("User is *NOT* authenticated (bad credentials): - " + credentials.getUsername());
				httpResponseStatus = Status.UNAUTHORIZED;
				responseMessage = "Wrong credentials.";
				user = initBlankUser((UserCredentials) credentials);
				DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
						, "<REST><Error> User is *NOT* authenticated (bad credentials): - " + credentials.getUsername());
								
			}else if(!user.getContactInfo().isActivated() || !user.getContactInfo().isVerified() ) {
				getLog().info("User is NOT activated/verified: - " + credentials.getUsername());
				httpResponseStatus = Status.UNAUTHORIZED;
				responseMessage = "Not activated/verified";
				DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
						, "<REST><Error> User is *NOT* authenticated (verification/activation failure): - " + credentials.getUsername());
				
			}else{
				getLog().info("User is authenticated: - " + credentials.getUsername());
				httpResponseStatus = Status.OK;
				responseMessage = "Ok";
				DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
						, "<REST><Info> User is authenticated: - " + credentials.getUsername());
				
			}
			

		} catch (Exception e) {
			getLog().error("Error Parsing: - " + e);
			DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
					, "<REST><Error> Error Parsing: - " + e.getMessage());
		}
		
		//
		// Add diagnostics
		diagnostics = DIAGNOSTIC_MAP.getDiagnostics(DIAGNOSTIC_KEY);
		user.getDiagnostic().addAll(0,diagnostics);
		DIAGNOSTIC_MAP.clearDiagnostics(DIAGNOSTIC_KEY);

		
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
		user.setDiagnostic(new ArrayList<String>());
		
		return user;
		
	}
	
}
