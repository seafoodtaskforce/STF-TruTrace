package com.wwf.shrimp.application.services.main.rest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import com.wwf.shrimp.application.exceptions.PersistenceException;
import com.wwf.shrimp.application.models.AuditEntity;
import com.wwf.shrimp.application.models.Document;
import com.wwf.shrimp.application.models.DocumentType;
import com.wwf.shrimp.application.models.IdentifiableEntity;
import com.wwf.shrimp.application.models.LookupEntity;
import com.wwf.shrimp.application.models.Organization;
import com.wwf.shrimp.application.models.PasswordCredentials;
import com.wwf.shrimp.application.models.RESTResponse;
import com.wwf.shrimp.application.models.ResponseErrorData;
import com.wwf.shrimp.application.models.ResponseMessageData;
import com.wwf.shrimp.application.models.TagData;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.search.LookupDataSearchCriteria;
import com.wwf.shrimp.application.models.search.OrganizationSearchCriteria;
import com.wwf.shrimp.application.models.search.UserSearchCriteria;
import com.wwf.shrimp.application.services.main.BaseRESTService;
import com.wwf.shrimp.application.services.main.ConfigurationService;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.LookupDataMySQLDao;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.OrganizationMySQLDao;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.UserMySQLDao;
import com.wwf.shrimp.application.services.main.impl.PropertyConfigurationService;
import com.wwf.shrimp.application.services.worker.impl.SimplePasswordService;
import com.wwf.shrimp.application.utils.CSVUtils;
import com.wwf.shrimp.application.utils.EmailUtils;
import com.wwf.shrimp.application.utils.RESTUtility;
import com.wwf.shrimp.application.utils.SingletonMapGlobal;

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
	private LookupDataMySQLDao<LookupEntity, LookupDataSearchCriteria> lookupService = new LookupDataMySQLDao<LookupEntity, LookupDataSearchCriteria>();
	private UserMySQLDao<User, UserSearchCriteria> userService = new UserMySQLDao<User, UserSearchCriteria>();
	private OrganizationMySQLDao<Organization, OrganizationSearchCriteria> organizationService = new OrganizationMySQLDao<Organization, OrganizationSearchCriteria>();
	private ConfigurationService configService = new PropertyConfigurationService();
	//
	// Diagnostics
	private SingletonMapGlobal DIAGNOSTIC_MAP = SingletonMapGlobal.getInstance();
	
	
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
		int minUserNameLength;
		Status httpResponseStatus = Status.OK;
		String responseMessage = "";
		
		
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		this.configService.open();
		userService.init();
		
		//
    	// extract configuration data first
    	minUserNameLength = Integer.parseInt(this.configService.readConfigurationProperty("user.name.min.length"));
    	String activationRESTLink = this.configService.readConfigurationProperty("system.url.server.path")
    			+ this.configService.readConfigurationProperty("system.url.server.path.activate.account");
		
		
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
			
			if(!userService.isObjectValid(newUser, true)){
				httpResponseStatus = Status.BAD_REQUEST;
				responseMessage = getResponseMessage(ERROR_CODE_USER_INVALID_OBJECT);
				
			}else if(newUser.getName().length() < minUserNameLength){
				httpResponseStatus = Status.BAD_REQUEST;
				responseMessage = getResponseMessage(ERROR_CODE_USER_NAME_LENGTH);
			}else{
				//
				// create the new user
				newUser = userService.create(newUser);
				httpResponseStatus = Status.OK;
				responseMessage = RESTUtility.getJSON(newUser);
			}

		} catch (Exception e) {
			getLog().error("Error Creating a new user: - " + e);
			httpResponseStatus = Status.INTERNAL_SERVER_ERROR;
			responseMessage = getResponseMessage(ERROR_CODE_DEFAULT_SERVER);
		}

		// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(responseMessage).build();
	}
	
	
	
	@POST
	@Path("/register")
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
	public Response register(InputStream incomingData,
			@DefaultValue("") @HeaderParam("group-name") String organizationName) {
		
		User newUser = null;
		int minUserNameLength;
		Status httpResponseStatus = Status.OK;
		String responseMessage = "";
		
		
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		this.configService.open();
		userService.init();
		
		//
    	// extract configuration data first
    	minUserNameLength = Integer.parseInt(this.configService.readConfigurationProperty("user.name.min.length"));
	
		
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
			System.out.println("New User Creation <register>: " + newUser);
			
			if(!userService.isObjectValid(newUser, false)){
				httpResponseStatus = Status.BAD_REQUEST;
				responseMessage = getResponseMessage(ERROR_CODE_USER_INVALID_OBJECT);
				
			}else if(newUser.getName().length() < minUserNameLength){
				httpResponseStatus = Status.BAD_REQUEST;
				responseMessage = getResponseMessage(ERROR_CODE_USER_NAME_LENGTH);
			}else{
				//
				// create the new user
				responseMessage = userService.register(newUser, organizationName);
				httpResponseStatus = Status.OK;
				responseMessage = RESTUtility.getJSON(responseMessage);
			}

		} catch (Exception e) {
			getLog().error("Error registering a new user: - " + e);
			httpResponseStatus = Status.INTERNAL_SERVER_ERROR;
			responseMessage = getResponseMessage(ERROR_CODE_DEFAULT_SERVER);
		}

		// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(responseMessage).build();
	}
	
	
	
	
	@GET
	@Path("/activate")
	@Consumes("application/json; charset=UTF-8")
	@Produces("text/html; charset=UTF-8")
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
	public Response activate(
			@DefaultValue("") @QueryParam("user_name") String userName
			) {
		
		Status httpResponseStatus = Status.OK;
		String emailBody = "";
		String htmlResponse = "";
				
		//
		// Preconditions
		if(userName.isEmpty()) {
			httpResponseStatus = Status.FORBIDDEN;
			// return HTTP response 200 in case of success
			return Response.status(httpResponseStatus).entity(RESTUtility.getJSON("Activation is Forbidden")).build();
		}
		
		//
		// Configure
		//
		// Initialize services
		configService.open();
		

		
		
		//
		// Extract the object to be written to the database:
		User gestureUser = new User();
		
		
		getLog().info("Activating user: - " + userName);
		
		//
		// Initialize service
		userService.init();
		lookupService.init();
		configService.open();
		
		// 
		// Initialize other data
		try {
			gestureUser = userService.getUserByName(userName);
		} catch (PersistenceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			gestureUser.getContactInfo().setActivated(true);
			userService.updateUserContact(gestureUser);
			
			//
			// get the resources from the db
			String loginPath = "";
			
			try {
				//
				// get the email resource
				emailBody = lookupService.getAppResource("system.email.template.activation.success").get(0).getValue();
				emailBody=emailBody.replace("${username}",gestureUser.getContactInfo().getFirstName());
				emailBody=emailBody.replace("${login_name}",gestureUser.getName());
				emailBody = emailBody.replace("${login_page_link}",loginPath);
				//
				// get the html page response
				loginPath = this.configService.readConfigurationProperty("system.url.server.path.login.account");
				htmlResponse = lookupService.getAppResource("system.email.response.activation.success.template.html").get(0).getValue();
				htmlResponse = htmlResponse.replace("${username}",gestureUser.getContactInfo().getFirstName());
				
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//
			// send the activation email
			EmailUtils.sendUserWelcomeEmail(gestureUser
					, "TruTrace - Account Has Been Succesfuly Activated"
					, emailBody);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//
		// Send the confirmation email
		
		// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(htmlResponse).build();
	}
	
	
	@POST
	@Path("/passwordreset")
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
	public Response resetPassword(
			@DefaultValue("") @QueryParam("user_name") String userName
			) {
		
		Status httpResponseStatus = Status.OK;
		
		//
		// Extract the object to be written to the database:
		User gestureUser = new User();
		
		
		getLog().info("Activating user: - " + userName);
		
		//
		// Initialize service
		userService.init();
		lookupService.init();
		
		// 
		// Initialize other data
		try {
			gestureUser = userService.getUserByName(userName);
		} catch (PersistenceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			//
			// get reset password for the user
			SimplePasswordService passwordService = new SimplePasswordService();
			String newPassword = passwordService.generateNewPassword();
			
			//
			// create set of credentials to be updated
			PasswordCredentials credentials = new PasswordCredentials();
			credentials.setUsername(gestureUser.getName());
			credentials.setPassword(newPassword);
			
			//
			// update the credentials in the backend
			userService.updateCredentials(credentials);
			
			//
			// get the resources from the db
			String emailBody = "";
			try {
				emailBody = lookupService.getAppResource("system.email.template.password.reset").get(0).getValue();
				emailBody=emailBody.replace("${username}",gestureUser.getContactInfo().getFirstName());
				emailBody=emailBody.replace("${login_name}",gestureUser.getName());
				emailBody=emailBody.replace("${password}",newPassword);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//
			// send the activation email
			EmailUtils.sendUserPasswordResetEmail(gestureUser
					, "TruTrace - You have succesfully reset your password."
					, emailBody);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//
		// Send the confirmation email
		
		// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(RESTUtility.getJSON("Password Has Been Reset")).build();
	}
	
	@POST
	@Path("/updateprofile")
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
	public Response updateProfile(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("true") @HeaderParam("sparse") boolean sparseFlag) {
		
		User user = null;
		Status httpResponseStatus = Status.OK;
		String responseMessage = "";
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		configService.open();
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
			System.out.println("Existing User update: " + user);
			
			if(!userService.isObjectValid(user.getContactInfo())){
				httpResponseStatus = Status.BAD_REQUEST;
				responseMessage = getResponseMessage(ERROR_CODE_USER_INVALID_OBJECT);
				
			}else{
				//
				// update the user
				user = userService.updateProfile(user);
				httpResponseStatus = Status.OK;
				responseMessage = RESTUtility.getJSON(user);
			}
			
		} catch (Exception e) {
			getLog().error("Error Updating an existing user profile: - " + e);
			httpResponseStatus = Status.INTERNAL_SERVER_ERROR;
			responseMessage = getResponseMessage(ERROR_CODE_DEFAULT_SERVER);
		}

		// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(responseMessage).build();
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
		int minUserNameLength;
		int minPasswordLength;
		Status httpResponseStatus = Status.OK;
		String responseMessage = "";
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		configService.open();
		userService.init();
		
		//
    	// extract configuration data first
    	minUserNameLength = Integer.parseInt(this.configService.readConfigurationProperty("user.name.min.length"));
    	minPasswordLength = Integer.parseInt(this.configService.readConfigurationProperty("user.password.min.length"));
		
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
			
			if(!userService.isObjectValid(user, true)){
				httpResponseStatus = Status.BAD_REQUEST;
				responseMessage = getResponseMessage(ERROR_CODE_USER_INVALID_OBJECT);
				
			}else if(user.getName().length() < minUserNameLength){
				httpResponseStatus = Status.BAD_REQUEST;
				responseMessage = getResponseMessage(ERROR_CODE_USER_NAME_LENGTH);
			}else if(user.getCredentials() instanceof PasswordCredentials && 
					((PasswordCredentials)user.getCredentials()).getPassword().length() < minPasswordLength){
				httpResponseStatus = Status.BAD_REQUEST;
				responseMessage = getResponseMessage(ERROR_CODE_USER_PASSWORD_LENGTH);
			}else{
				//
				// update the user
				user = userService.update(user);
				httpResponseStatus = Status.OK;
				responseMessage = RESTUtility.getJSON(user);
			}
			
		} catch (Exception e) {
			getLog().error("Error Updating an existing user: - " + e);
			httpResponseStatus = Status.INTERNAL_SERVER_ERROR;
			responseMessage = getResponseMessage(ERROR_CODE_DEFAULT_SERVER);
		}

		// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(responseMessage).build();
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
		int minPasswordLength;
		Status httpResponseStatus = Status.OK;
		String responseMessage = "";
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		configService.open();
		userService.init();
		
		//
    	// extract configuration data first
    	minPasswordLength = Integer.parseInt(this.configService.readConfigurationProperty("user.password.min.length"));
		
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
			
			if(!userService.isObjectValid(credentials)){
				httpResponseStatus = Status.BAD_REQUEST;
				responseMessage = getResponseMessage(ERROR_CODE_USER_INVALID_OBJECT);
				
			}else if(credentials.getPassword().length() < minPasswordLength){
				httpResponseStatus = Status.BAD_REQUEST;
				responseMessage = getResponseMessage(ERROR_CODE_USER_PASSWORD_LENGTH);
			}else{
				//
				// update the user
				userService.updateCredentials(credentials);
				httpResponseStatus = Status.OK;
				responseMessage = getResponseMessage(SUCCESS_CODE_USER_UPDATE);
			}
		} catch (Exception e) {
			getLog().error("Error Updating Credentials: - " + e);
			httpResponseStatus = Status.INTERNAL_SERVER_ERROR;
			responseMessage = getResponseMessage(ERROR_CODE_DEFAULT_SERVER);
		}

		// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(responseMessage).build();
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
	
	
	
	@POST
	@Path("/batchupload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Returns text response to caller containing uploaded file location
	 * 
	 * @return error response in case of missing parameters an internal
	 *         exception or success response if file has been stored
	 *         successfully
	 */
	public Response uploadUserBatchFile(
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail,
			@FormDataParam("userName") String username,
			@FormDataParam("orgName") String orgName){
		//
		// Response to the caller
		RESTResponse result = new RESTResponse();
		
		List<User> batchUsers=null;
		byte [] bDocImportCSV = new byte [0];
		Object verificationResponse = null;
		
		//
		// check if all form parameters are provided
		if (uploadedInputStream == null || fileDetail == null)
			return Response.status(400).entity("Invalid form data").build();
		
		//
		// initialize services
		userService.init();
		
		//
		// Get the CVS file in bytes
		bDocImportCSV = CSVUtils.getCSVBytesFromInputStream(uploadedInputStream);
		
		//
		// Verify that the file is legitimate
		try {
			verificationResponse = userService.verifyBatchUserCSVFile(bDocImportCSV, username);
		} catch (PersistenceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//
		// Check if we will process the data
		if(verificationResponse instanceof ResponseErrorData){
			 result.getErrorData().add((ResponseErrorData)verificationResponse);
			 
		 } else {
			 result.getMessageData().add((ResponseMessageData)verificationResponse);
			 try {
					//
					// Parse the user records
					batchUsers = userService.importBatchUserCSVFile(bDocImportCSV, username, orgName);
					result.setData(batchUsers);
				} catch (PersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
		 }
		 

		getLog().info("Batch Processing if USer Input: - " + "Success");
		return Response.status(200)
				.entity(RESTUtility.getJSON(result)).build();
	}

}
