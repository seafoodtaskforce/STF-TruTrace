package com.wwf.shrimp.application.utils;

import javax.ws.rs.ext.Provider;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/**
 * CORS filter for testing under the same domain server
 * NOTE: should be removed from configuration when final deployment is made
 * @author argolite
 *
 */
@Provider
public class CORSFilter  implements ContainerResponseFilter {

	
	   
	   /**
	    * filter method for adding specific header data to a response for an input request.
		 * @param requestContext - the intercepted request
		 * @param cres - the CORS modified response with a modified header.
		 * @return - the modified response
	    */
	   @Override
	   public ContainerResponse filter(final ContainerRequest requestContext,
	                      final ContainerResponse cres)  {
	      cres.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
	      cres.getHttpHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, user-name");
	      cres.getHttpHeaders().add("Access-Control-Allow-Credentials", "true");
	      cres.getHttpHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
	      cres.getHttpHeaders().add("Access-Control-Max-Age", "1209600");
	      return cres;
	   }

	}
