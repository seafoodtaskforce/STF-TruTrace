package com.wwf.shrimp.application.services.main.rest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sun.jersey.multipart.FormDataParam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.wwf.shrimp.application.models.AuditEntity;
import com.wwf.shrimp.application.models.IdentifiableEntity;
import com.wwf.shrimp.application.models.PasswordCredentials;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.search.UserSearchCriteria;
import com.wwf.shrimp.application.services.main.BaseRESTService;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.UserMySQLDao;
import com.wwf.shrimp.application.utils.RESTUtility;

/**
 * This is a collection of USer based RESTFul service options.
 * It will cover the ability to fetch user data as well as user related data.
 * 
 * <TODO> Change the verbs to be fully restful
 * @author AleaActaEst
 *
 */
@Path("/user")
public class UserRESTService extends BaseRESTService {
	private UserMySQLDao<User, UserSearchCriteria> userService = new UserMySQLDao<User, UserSearchCriteria>();
	
	
	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will create a new document in the database
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the User entity with all additional data embedded
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated user.
	 *     2. Error String if there was an issue
	 */
	public Response create(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("true") @HeaderParam("sparse") boolean sparseFlag) {
		
		User newUser = null;
		IdentifiableEntity id = null;
		AuditEntity newAuditEntity = new AuditEntity();
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
			
			//
			// get the parser ready
			Gson gson = new GsonBuilder()
		            .setDateFormat("YYYY-MM-DD HH:MM:SS")
		            .create();
			
			// parse the JSON input into the specific class
			// newDocument = gson.fromJson(reader, Document.class);
			newUser = gson.fromJson(reader, User.class);
			System.out.println(newUser.getName());
			System.out.println("New User Creation: " + newUser);
			
			//
			// create the new user
			newUser = userService.create(newUser);

		} catch (Exception e) {
			getLog().error("Error Creating a new user: - " + e);
		}

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(newUser)).build();
	}
	
	@POST
	@Path("/update")
	@Produces("application/json; charset=UTF-8")
	@Consumes("application/json; charset=UTF-8")
	/**
	 * This method will create a new document in the database
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the User entity with all additional data embedded
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated user.
	 *     2. Error String if there was an issue
	 */
	public Response update(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("true") @HeaderParam("sparse") boolean sparseFlag) {
		
		User user = null;
		IdentifiableEntity id = null;
		AuditEntity newAuditEntity = new AuditEntity();
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
			
			//
			// get the parser ready
			Gson gson = new GsonBuilder()
					.disableHtmlEscaping()
		            .setDateFormat("YYYY-MM-DD HH:MM:SS")
		            .create();
			
			// parse the JSON input into the specific class
			// newDocument = gson.fromJson(reader, Document.class);
			user = gson.fromJson(reader, User.class);
			System.out.println(user.getName());
			System.out.println("Existing User update: " + user);
			
			//
			// create the new user
			user = userService.update(user);

		} catch (Exception e) {
			getLog().error("Error Updating an existing user: - " + e);
		}

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(user)).build();
	}
	
	@POST
	@Path("/updatecredentials")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will create a new document in the database
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the User entity with all additional data embedded
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated user.
	 *     2. Error String if there was an issue
	 */
	public Response updateCredentials(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		
		PasswordCredentials credentials = null;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
			
			//
			// get the parser ready
			Gson gson = new GsonBuilder()
		            .setDateFormat("YYYY-MM-DD HH:MM:SS")
		            .create();
			
			// parse the JSON input into the specific class
			// newDocument = gson.fromJson(reader, Document.class);
			credentials = gson.fromJson(reader, PasswordCredentials.class);
			System.out.println("Existing User Credentials update: " + credentials.getUsername());
			
			//
			// UPDATE
			userService.updateCredentials(credentials);

		} catch (Exception e) {
			getLog().error("Error Updating Credentials: - " + e);
		}

		// return HTTP response 200 in case of success
		return Response.status(200).entity("Credentials Update Successful").build();
	}
	
	/**
	 * This service method will get all users in the system 
	 * @param userName - the user requesting this data.
	 * @return - a list of users that the requesting user can see.
	 */
	@GET
	@Path("/fetchall")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllUsers(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("false") @HeaderParam("sparse") boolean sparseFlag,
			@DefaultValue("normal") @HeaderParam("user-type") String userType) {
		// results
		List<User> allUsers=null;
		User user = null;
		
		getLog().info("Got a request : Fetch All Users - " + userName);
		getLog().info("Request HEADER <user-name:> " + userName);
		
		//
		// Initialize service
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			if(userType.equals("normal")){
				user = userService.getUserByName(userName);
				if(sparseFlag){
					allUsers= userService.getAllUsersSparse(user);
				}else{
					allUsers = userService.getAllUsers(user); 
				}
			}else{
				allUsers = userService.getAllUsers();
			}

		} catch (Exception e) {
			getLog().error("Error Fetching Users: - " + e);
		}
		
		getLog().debug("Fetch All Users result: " + allUsers);
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(allUsers)).build();
	}
	
	@GET
	@Path("/profileimage")
	@Produces({"image/png", "image/jpeg", "image/gif"})
	public Response fetchProfileImage(
			@DefaultValue("0") @QueryParam("user_id") long userID,
			@DefaultValue("") @QueryParam("user_name") String username
																) {
		
		InputStream binaryStream=null;
		
		//
		// Initialize service
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			if(userID == 0){
				User user = userService.getUserByName(username);
				userID = user.getId();
			}
			binaryStream = userService.getUserProfileImage(userID);


		} catch (Exception e) {
			getLog().error("Error Fetching Profile Image Page: - " + e);
		}
		
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(binaryStream).build();
	}
	
	@POST
	@Path("/profileimage")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	/**
	 * THis method will insert/update the profile image for the user
	 * @param incomingData - the user data with the profile image to set
	 * @return - Simple text response with success of the insertion
	 */
	public Response updateProfileImage(
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail,
			@FormDataParam("userName") String username) {
		String result = "/user/profileimage com.wwf.shrimp.application [UPDATE PROFILE IMAGE - SUCCESS]";

		//
		// Initialize service
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			// get the request reader ready 
			getLog().info("New Profile Image Insertion: " + username + " " + fileDetail.getFileName());

			if(uploadedInputStream != null){
				userService.updateUserProfileImage(uploadedInputStream, username);
			}
			

		} catch (Exception e) {
			getLog().error("Error Updating Profile pic: - " + e.getStackTrace());
			
		}

		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(result).build();
	}
	
	@POST
	@Path("/profileimage2")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * THis method will insert/update the profile image for the user
	 * @param incomingData - the user data with the profile image to set
	 * @return - Simple text response with success of the insertion
	 */
	public Response updateProfileImage2(InputStream incomingData,
				@DefaultValue("0") @QueryParam("user_id") long userID
																		) {
		String result = "/user/profileimage2 com.wwf.shrimp.application [UPDATE PROFILE IMAGE - SUCCESS]";
		String profileBase64Image = null;

		//
		// Initialize service
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
			
			//
			// get the parser ready
			Gson gson = new GsonBuilder()
		            .create();
			
			// parse the JSON input into the specific class
			// newDocument = gson.fromJson(reader, Document.class);
			profileBase64Image = gson.fromJson(reader, String.class);
			System.out.println("New Profile Image Insertion: " + userID);
			if(profileBase64Image != null){
				// userService.updateUserProfileImage(profileBase64Image, userID);
			}

		} catch (Exception e) {
			getLog().error("Error Updating Profile pic: - " + e.getStackTrace());
			
		}

		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(result).build();
	}
	
	
	
	
}
