package com.wwf.shrimp.application.services.main.rest.filters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.sun.jersey.core.util.Base64;
import com.sun.jersey.core.util.ReaderWriter;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.wwf.shrimp.application.exceptions.AuthenticationException;
import com.wwf.shrimp.application.models.SecurityToken;
import com.wwf.shrimp.application.models.UserCredentials;
import com.wwf.shrimp.application.services.main.ConfigurationService;
import com.wwf.shrimp.application.services.main.SecurityService;
import com.wwf.shrimp.application.services.main.impl.PropertyConfigurationService;
import com.wwf.shrimp.application.services.main.impl.TokenBasedSecurityService;
import com.sun.jersey.oauth.signature.OAuthParameters;

/**
 * Intercepting filter at the level before the services are called for REST
 * THis is used to launch other specific services such as the ability to authenticate and authorize or audit
 * specific data gestures.
 * 
 * <TODO> Eventually should add handlers for the different service end-points as well as abstract
 * <TODO> filtering, logging, and error handling a bit further.
 * @author argolite
 *
 */
public class SecurityInterceptingFilter implements ContainerRequestFilter {
	/**
	 * Represents the Logger used to perform logging.
	 */
	private Logger log = Logger.getLogger(getClass().getName());
	
	// Token generator for authentication
	private SecurityService securityService =  new TokenBasedSecurityService();
	
	// Configuration Service
    ConfigurationService configService = new PropertyConfigurationService();

    @Override
    public ContainerRequest filter(ContainerRequest request) {
    	boolean checkTokenHeader = true;
    	
    	//
    	// extract configuration data first
    	this.configService.open();
    	checkTokenHeader = Boolean.parseBoolean(this.configService.readConfigurationProperty("security.token.enabled"));
    	
    	// log the entry data
    	//
    	log.info("<Security Filter Intercept>");
    	log.info("Verb " + request.getMethod());
    	log.info("Path " + request.getPath());
    	log.info("Parameters " + request.getQueryParameters());
    	log.info("Headers " + request.getHeaderValue(OAuthParameters.AUTHORIZATION_HEADER));
    	log.info("Process Header Token? <" + checkTokenHeader + ">");
    	
    	// 
    	// Process Request Data
    	if(checkTokenHeader){
    		request = processHeader(request);
    	}

    	// Extract the underlying entity if it exists
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = request.getEntityInputStream();
        final StringBuilder b = new StringBuilder();
        try {
            if (in.available() > 0) {
                ReaderWriter.writeTo(in, out);

                byte[] requestEntity = out.toByteArray();
                printEntity(b, requestEntity);

                request.setEntityInputStream(new ByteArrayInputStream(requestEntity));
            }
            
        } catch (IOException ex) {
        	//
        	// log the issue
        	log.error("Security Filter has failed for request: "
            		+ request.getPath());
        	log.info("<Security Filter Intercept End - Exception>");
        	throw generateResponseException(Response.Status.UNAUTHORIZED, 
        			"Security Filter has failed for request: " + request.getPath());
        }
        
        log.info("<Security Filter Intercept End>");
        return request;

    }
    
    /****************************************************************************************************************
     * Helper Methods
     */

    /**
     * Helper method for entity creation from JSON input
     * @param builder - the string builder
     * @param entity - the entity being printed
     * @throws IOException
     * 		- if there was an issue with parsing the bytes to create the output.
     */
    private void printEntity(StringBuilder builder, byte[] entity) throws IOException {
        if (entity.length == 0)
            return;
        builder.append(new String(entity)).append("\n");
        log.debug("Intercepted Entity: " + builder.toString());
    }
    
    /**
     * Create a web exception and map the error HTTP status codes
     * @param status - the status we want to be output
     * @param message - the message we want to be output
     * @return - the WebApplicationException with the input data
     */
    private WebApplicationException generateResponseException(Status status, String message){
            ResponseBuilder builder = null;
            String response = message;
            builder = Response.status(status).entity(response);
            return new WebApplicationException(builder.build());
    }
    
    private ContainerRequest processHeader(ContainerRequest request){
    	UserCredentials credentials = new UserCredentials();
    	SecurityToken token = new SecurityToken();
    	
    	//
    	// extract and de-construct the header data
    	String auth = request.getHeaderValue(OAuthParameters.AUTHORIZATION_HEADER);
    	
    	/**
    	 * Handle un-authorised access
    	 */

    	//
    	// Check for authentication
    	if(request.getPath().equals(SecurityService.SECURITY_AUTH_LOGIN_REQUEST_URL)){
    		//
    		// log the user in
    		
    		//
    		// <TODO> for now just pass through
    		
    		
    	//
        // check for anonymous access	
    	} else if(request.getPath().startsWith(SecurityService.SERVER_REQUEST_URL_PREFIX)){
    		//
    		// This is allowed for anonymous users
    		
    		//
    		// <TODO> for now just pass through but will be based on permissions and ANONYMOUS role
    		
    	//
    	// Handler the other situations for all other requests
    	} else {
    		//
    		// check if the user has a valid session token
    		
    		// did we get the header at all
    		if (auth == null){
    			//
    			// reject the request
    			log.info("<Security Filter Intercept End - Exception>");
    			throw generateResponseException(Response.Status.UNAUTHORIZED, 
    					"Security Filter has failed for request: " + request.getPath());
    			
    		}
    	        
    		//
    		// Strip the request and extract the data
    		if (auth.startsWith("Basic ") || auth.startsWith("basic ")) {
    	        auth = auth.replaceFirst("[Bb]asic ", "");
    	        String userColonPass = Base64.base64Decode(auth);
    	        log.info("Header Stripped " + userColonPass);
    	        String[] headerUserData = userColonPass.split(":");
    	        
    	        // get username and token
    	        token.setTokenValue(headerUserData[1]);
    	        credentials.setUsername(headerUserData[0]);
    	        credentials.setToken(token);
    	            	        
    	        try {
					if (!securityService.isSessionAuthenticated(credentials)){
						log.info("<Security Filter Intercept End - Exception>");
		    			throw generateResponseException(Response.Status.UNAUTHORIZED, 
		    					"User has no session token " + request.getPath());
					}
				} catch (AuthenticationException e) {
					e.printStackTrace();
					log.info("<Security Filter Intercept End - Exception>");
					throw generateResponseException(Response.Status.UNAUTHORIZED, 
							"User's session cannot be authenticated: " + request.getPath());
				}
    	        //    throw unauthorized;
    	    } else {
    	        // fail on unrecognized auth type
    	    	//
    	    	log.info("<Security Filter Intercept End - Exception>");
    	    	throw generateResponseException(Response.Status.UNAUTHORIZED, 
    	    			"Security Filter has failed for unknown header: " + request.getPath());
    	    }
    		
    		//
    		// <TODO> NOTE that later we will allow some anonymous access so this will be modified 
    	}
    	
    	return request;
    }
    	
}

