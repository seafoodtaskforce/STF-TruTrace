package com.wwf.shrimp.application.services.main.dao.impl.mysql;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.mysql.cj.api.jdbc.Statement;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.wwf.shrimp.application.exceptions.EntityNotFoundException;
import com.wwf.shrimp.application.exceptions.PersistenceException;
import com.wwf.shrimp.application.models.DocumentType;
import com.wwf.shrimp.application.models.Group;
import com.wwf.shrimp.application.models.GroupType;
import com.wwf.shrimp.application.models.LookupEntity;
import com.wwf.shrimp.application.models.Organization;
import com.wwf.shrimp.application.models.PasswordCredentials;
import com.wwf.shrimp.application.models.RESTResponse;
import com.wwf.shrimp.application.models.ResponseErrorData;
import com.wwf.shrimp.application.models.ResponseIssue;
import com.wwf.shrimp.application.models.ResponseMessageData;
import com.wwf.shrimp.application.models.Role;
import com.wwf.shrimp.application.models.SecurityToken;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.UserContact;
import com.wwf.shrimp.application.models.UserCredentials;
import com.wwf.shrimp.application.models.search.LookupDataSearchCriteria;
import com.wwf.shrimp.application.models.search.OrganizationSearchCriteria;
import com.wwf.shrimp.application.models.search.UserSearchCriteria;
import com.wwf.shrimp.application.services.main.ConfigurationService;
import com.wwf.shrimp.application.services.main.impl.PropertyConfigurationService;
import com.wwf.shrimp.application.utils.CSVUtils;
import com.wwf.shrimp.application.utils.DateUtility;
import com.wwf.shrimp.application.utils.EmailUtils;
import com.wwf.shrimp.application.utils.HashingUtils;
import com.wwf.shrimp.application.utils.SingletonMapGlobal;

/**
 * The persistence implementation for User entities based on the MySQL database
 * @author AleaActaEst
 *
 * @param <T> - the operational entity that this DAO will work with.
 * @param <S> - The specific search criteria for entity <T>
 */
public class UserMySQLDao<T, S> extends BaseMySQLDao<User, UserSearchCriteria>{
	
	private OrganizationMySQLDao<Organization, OrganizationSearchCriteria> organizationService = new OrganizationMySQLDao<Organization, OrganizationSearchCriteria>();
	private LookupDataMySQLDao<LookupEntity, LookupDataSearchCriteria> lookupService = new LookupDataMySQLDao<LookupEntity, LookupDataSearchCriteria>();
    private ConfigurationService configService = new PropertyConfigurationService();
    //
	// Diagnostics
	private SingletonMapGlobal DIAGNOSTIC_MAP = SingletonMapGlobal.getInstance();
	
	/**
	 * This method will create a new entity within the storage for this entity and 
	 * will assign a new unique ID for it
	 * 
	 * @param entity - the entity to create
	 * @return - the created entity with a unique id set
	 *
	 * @throws IllegalArgumentException - if entity is null or not valid 
	 * @throws PersistenceException - if any other error occurred during operation
	 */
	public User create(User entity) throws PersistenceException, IllegalArgumentException{
		PreparedStatement preparedINSERTstatement;
		String defaultPassword;
		boolean sendActivationEmail = false;
		long returnId=0;
		
		//
    	// extract configuration data first
    	this.configService.open();
		lookupService.init();
    	
    	
    	defaultPassword = this.configService.readConfigurationProperty("user.password.default");
    	if(Boolean.parseBoolean(this.configService.readConfigurationProperty("security.psw.hash"))){
    		defaultPassword = HashingUtils.hashStringOneWay(defaultPassword);
		}
    	sendActivationEmail = Boolean.parseBoolean(this.configService.readConfigurationProperty("user.registration.must.activate"));
		//
    	// extract configuration data first
    	String activationRESTLink = this.configService.readConfigurationProperty("system.url.server.path")
    			+ this.configService.readConfigurationProperty("system.url.server.path.activate.account");
		
		// get the connection
		Connection conn = openConnection();
		
	
		// create the query for user
		String insertQuery = " insert into user ("
				+ "name, "
				+ "password "
				+ ")"
		        + " values (?,?)";
		
		//
		// create the user data
		try {
			preparedINSERTstatement = conn
			        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			
			// get the data
			String userName =  entity.getCredentials().getUsername().toLowerCase();
			String password = defaultPassword;
			if(entity.getCredentials() instanceof PasswordCredentials){
				password = ((PasswordCredentials)entity.getCredentials()).getPassword();
			}
			// execute the statement 
			preparedINSERTstatement.setString(1, userName);
			preparedINSERTstatement.setString(2,password);
			
			
			preparedINSERTstatement.executeUpdate();
			
			// get the id of the created 
			ResultSet rs = preparedINSERTstatement.getGeneratedKeys();
			if (rs.next()){
				returnId=rs.getLong(1);
				entity.setId(returnId);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//
		// Add user contact data 
		entity.getContactInfo().setActivated(false);
		entity.getContactInfo().setVerified(true);
		createUserContact(entity);
		
		//
		// Add user role
		createUserRole(entity);
		//
		// Add user group
		createUserGroup(entity);
		
		//
		// Add User organization
		createUserOrganization(entity);
		
		//
		// Add Group relation
		createUserOrganizationGroupLink(entity);
		

		// release the connection
		closeConnection(conn);
		
		//
		// Process any notification creation
		if(sendActivationEmail){
			//
			// get the resources from the db
			String emailBody = "";
			try {
				emailBody = lookupService.getAppResource("system.email.template.activation.body").get(0).getValue();
				emailBody=emailBody.replace("${username}",entity.getContactInfo().getFirstName());
				//String serverRootUrl = this.configService.readConfigurationProperty("system.server.rest.root.url");
				//String url = this.configService.readConfigurationProperty("system.url.server.path.activate.account");
				// url = serverRootUrl + url + entity.getName();
				emailBody=emailBody.replace("${activation_link}",activationRESTLink);
				emailBody=emailBody.replace("${login_name}",entity.getCredentials().getUsername());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//
			// send the activation email
			EmailUtils.sendActivationEmail(entity
					, "TruTrace - Account Activation"
					, emailBody);

			getLog().info("Email Body " + emailBody);
		}

		return entity;
		
	}
	
	/**
	 * This method will create a new entity within the storage for this entity and 
	 * will assign a new unique ID for it
	 * 
	 * @param entity - the entity to create
	 * @return - the created entity with a unique id set
	 *
	 * @throws IllegalArgumentException - if entity is null or not valid 
	 * @throws PersistenceException - if any other error occurred during operation
	 */
	public String register(User registeredUser, String orgName) throws PersistenceException, IllegalArgumentException{
		PreparedStatement preparedINSERTstatement;
		long returnId=0;
		List<User> orgAdmins = new ArrayList<User>();
		List<Group> regOrgs = new ArrayList<Group>();
		String response = "";
		
		//
    	// extract configuration data first
    	this.configService.open();
		lookupService.init();
		organizationService.init();
		
		//
		// Preconditions
		//
		//
		
		//
		// get the orgAdmins
		try {
			regOrgs = organizationService.getAllGroupsByName(orgName);
			orgAdmins = getOrgAdminsForGroup(regOrgs.get(0));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(!isUsernameUnique(registeredUser.getName())){
			response = "username " + "'" + registeredUser.getName() + "' is already taken. Please try a different name";
			return response;
		}
		if(!isEmailUnique(registeredUser.getContactInfo().getEmailAddress())){
			response = "email address " + "'" + registeredUser.getContactInfo().getEmailAddress() + "' is already registered.";
			return response;
		}
		if(regOrgs == null || regOrgs.size() == 0){
			response = "The organization " + "'" + orgName + "' does not exist in this system.";
			return response;
		}
		if(orgAdmins == null || orgAdmins.size() == 0){
			response = "The organization " + "'" + orgName + "' does not have an assigned administrator.";
			return response;
		}
		
		

		// get the connection
		Connection conn = openConnection();
		
	
		// create the query for user
		String insertQuery = " insert into user ("
				+ "name, "
				+ "password "
				+ ")"
		        + " values (?,?)";
		
		//
		// create the user data
		try {
			preparedINSERTstatement = conn
			        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			
			// get the data
			String userName =  registeredUser.getCredentials().getUsername().toLowerCase();
			String password = this.configService.readConfigurationProperty("user.password.default");
			if(registeredUser.getCredentials() instanceof PasswordCredentials){
				password = ((PasswordCredentials)registeredUser.getCredentials()).getPassword();
			}
			// execute the statement 
			preparedINSERTstatement.setString(1, userName);
			preparedINSERTstatement.setString(2,password);
			
			
			preparedINSERTstatement.executeUpdate();
			
			// get the id of the created 
			ResultSet rs = preparedINSERTstatement.getGeneratedKeys();
			if (rs.next()){
				returnId=rs.getLong(1);
				registeredUser.setId(returnId);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		//
		// Add user contact data 
		registeredUser.getContactInfo().setActivated(false);
		registeredUser.getContactInfo().setVerified(false);
		createUserContact(registeredUser);
		
		//
		// Add user role
		Role role = new Role();
		role.setId(3);
		role.setName("General");
		role.setValue("General");
		registeredUser.getRoles().add(0,  role);

		createUserRole(registeredUser);
		//
		// Add user group
		
		registeredUser.setUserGroups(regOrgs);
		createUserGroup(registeredUser);
		
		//
		// Add User organization
		createUserOrganization(registeredUser);
		
		//
		// Add Group relation
		createUserOrganizationGroupLink(registeredUser);
		

		// release the connection
		closeConnection(conn);
		
		//
		// Process any notification creation
		
		//
		// get the resources from the db
		for(int i=0; i<orgAdmins.size(); i++) {
			String emailBody = "";
			try {
				emailBody = lookupService.getAppResource("system.email.template.registration.alert.org.admin.body").get(0).getValue();
				emailBody=emailBody.replace("${username}",orgAdmins.get(i).getName());
				emailBody=emailBody.replace("${orgname}",orgName);
				emailBody=emailBody.replace("${new_username}",registeredUser.getName());
				emailBody=emailBody.replace("${user_firstname}",registeredUser.getContactInfo().getFirstName());
				emailBody=emailBody.replace("${user_lastname}",registeredUser.getContactInfo().getLastName());
				emailBody=emailBody.replace("${user_email}",registeredUser.getContactInfo().getEmailAddress());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//
			// send the registration alert to the  email
			EmailUtils.sendNewUserRegistrationAlertEmail(orgAdmins.get(i)
						, "TruTrace - New Registration Request"
						, emailBody);
			getLog().info("Email Body " + emailBody);
			
		}
		
		//
		//
		response = "Success!";

		return response;
		
	}
	
	
	
	
	@Override
	/**
	 * Update the user entity
	 * 
	 * @param entity - the entity to update
	 * @return - the updated entity
	 *
	 * @throws IllegalArgumentException - if entity is null or not valid 
	 * @throws PersistenceException - if any other error occurred during operation
	 * @throws EntityNotFoundException - if the entity to update does not exist
	 */
	public User update(User entity) throws IllegalArgumentException, PersistenceException, EntityNotFoundException {
		PreparedStatement preparedUPDATEStatement = null;
		boolean sendActivationEmail = false;
		
		// get the connection
		Connection conn = openConnection();
		String updateQuery = null;
		
		// 
		// process the request
		try {
			//
			// create the query
			if(entity.getCredentials() instanceof PasswordCredentials){
				updateQuery = " UPDATE user "
						+ "set name = ?, password = ? "
						+ "WHERE id = ? ";
				// create the statement
				preparedUPDATEStatement = conn
		                .prepareStatement(updateQuery);
				// execute the statement
				preparedUPDATEStatement.setString(1, entity.getName().toLowerCase());
				preparedUPDATEStatement.setLong(3, entity.getId());
				preparedUPDATEStatement.setString(3, ((PasswordCredentials)entity.getCredentials()).getPassword());
			}
			if(entity.getCredentials() instanceof UserCredentials){
				updateQuery = " UPDATE user "
						+ "set name = ? "
						+ "WHERE id = ? ";
				// create the statement
				preparedUPDATEStatement = conn
		                .prepareStatement(updateQuery);
				// execute the statement
				preparedUPDATEStatement.setString(1, entity.getName());
				preparedUPDATEStatement.setLong(2, entity.getId());
			}
			
			preparedUPDATEStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//
		// Add user contact data 
		updateUserContact(entity);
		
		// release the connection
		closeConnection(conn);
		
		//
		// Activation Logic
		//
		//
		sendActivationEmail = Boolean.parseBoolean(this.configService.readConfigurationProperty("user.registration.must.activate"));
		//
    	// extract configuration data first
    	String activationRESTLink = this.configService.readConfigurationProperty("system.url.server.path")
    			+ this.configService.readConfigurationProperty("system.url.server.path.activate.account");
		
    	if(sendActivationEmail && entity.getContactInfo().isActivated() == false &&  entity.getContactInfo().isVerified() == true){
			//
			// get the resources from the db
			String emailBody = "";
			try {
				emailBody = lookupService.getAppResource("system.email.template.activation.body").get(0).getValue();
				emailBody=emailBody.replace("${username}",entity.getContactInfo().getFirstName());
				//String serverRootUrl = this.configService.readConfigurationProperty("system.server.rest.root.url");
				//String url = this.configService.readConfigurationProperty("system.url.server.path.activate.account");
				// url = serverRootUrl + url + entity.getName();
				emailBody=emailBody.replace("${activation_link}",activationRESTLink);
				emailBody=emailBody.replace("${login_name}",entity.getCredentials().getUsername());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//
			// send the activation email
			EmailUtils.sendActivationEmail(entity
					, "TruTrace - Account Activation"
					, emailBody);

			getLog().info("Email Body " + emailBody);
		}
		
		
		getLog().info("UPDATED User Data for user " + entity.getName());
		
		// return data
		return entity;
	}

	
	/**
	 * Update the user entity
	 * 
	 * @param entity - the entity to update
	 * @return - the updated entity
	 *
	 * @throws IllegalArgumentException - if entity is null or not valid 
	 * @throws PersistenceException - if any other error occurred during operation
	 * @throws EntityNotFoundException - if the entity to update does not exist
	 */
	public User updateProfile(User entity) throws IllegalArgumentException, PersistenceException, EntityNotFoundException {
		
		// get the connection
		Connection conn = openConnection();
		
		//
		// Add user contact data 
		updateUserContact(entity);
		
		// release the connection
		closeConnection(conn);
		
		getLog().info("UPDATED User Profile Data for user <id> " + entity.getId());
		
		// return data
		return entity;
	}
	
	/**
	 * Update the user credentials
	 * 
	 * @param entity - the entity to update
	 * 
	 * @throws IllegalArgumentException - if entity is null or not valid 
	 * @throws PersistenceException - if any other error occurred during operation
	 * @throws EntityNotFoundException - if the entity to update does not exist
	 */
	public void updateCredentials(PasswordCredentials entity) throws IllegalArgumentException, PersistenceException, EntityNotFoundException {
		PreparedStatement preparedUPDATEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		String updateQuery = null;
		
		User user = getUserByName(entity.getUsername());
		this.configService.open();
    	if(Boolean.parseBoolean(this.configService.readConfigurationProperty("security.psw.hash"))){
    		entity.setPassword(HashingUtils.hashStringOneWay(entity.getPassword()));
		}
		
		// 
		// process the request
		try {
			//
			// create the query
				updateQuery = " UPDATE user "
						+ "set name = ?, password = ? "
						+ "WHERE id = ? ";
				// create the statement
				preparedUPDATEStatement = conn
		                .prepareStatement(updateQuery);
				// execute the statement
				preparedUPDATEStatement.setString(1, entity.getUsername());
				preparedUPDATEStatement.setString(2, (entity.getPassword()));
				preparedUPDATEStatement.setLong(3, user.getId());

			
			preparedUPDATEStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// release the connection
		closeConnection(conn);
		
		getLog().info("UPDATED User Credentials for user " + user.getName());
		
	}
	
	



	/**
	 * Get all the users in persistence that this user has access to 
	 * @param user - the user on whose behalf this request is made
	 * @return - the list of users for this request
	 * @throws PersistenceException
	 * 		- if there were any issues with the request processing
	 */
	public List<User> getAllUsers(User user) throws PersistenceException {
		List<User> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		long organizationId;
		
		// look up the user by name which should be unique
		//
		if(user != null){
			organizationId = user.getUserOrganizations().get(0).getId();
		}else{
			// use default organization
			organizationId = 1;
			
		}
		
		//
		// Prep any services
		lookupService.init();
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		
		// create the statement
		try {
			preparedSELECTstatement = conn
			        .prepareStatement("SELECT a.id, a.name, "
									+ "b.email_address, b.cell_number, b.first_name, b.last_name, b.nick_name, b.line_id, b.activated, b.verified, "
									+ "d.name AS organization_name, d.description As organization_description, d.id AS organization_id, "
									+ "e.group_id, gadr.doc_type_ids AS allowed_doc_types, "
									+ "g.name AS group_name, h.id AS group_type_id, h.name AS group_type, h.color_hex_code AS group_type_color, "
									+ " h.associated_groups, h.order_index, j.name as role, j.id as role_id  "
									+ "FROM user a "
									+ "JOIN user_contact b ON a.id = b.user_id "
									+ "JOIN user_organization c ON a.id=c.user_id "
									+ "JOIN organization d ON c.organization_id=d.id "
									+ "JOIN organization_group_rel_tree e ON d.id = e.parent_org_id "
									+ "JOIN user_group f ON a.id = f.user_id AND f.group_id = e.group_id "
									+ "JOIN group_data g ON e.group_id = g.id "
									+ "JOIN group_data_type h ON h.id = g.group_data_type_id "
									+ "JOIN group_allowed_doctype_rel gadr ON gadr.parent_group_type_id = h.id "
									+ "JOIN user_role i on i.user_id = a.id "
									+ "JOIN role j on i.role_id = j.id "
			        				+ "WHERE organization_id=?");
			
			// execute the statement 
			preparedSELECTstatement.setLong(1, organizationId);
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        //
	        // process the result
	        result = extractUsersFromResult(resultSet);
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
	}
	
	/**
	 * Get all the users in persistence (All users total) 
	 * @param user - the user on whose behalf this request is made
	 * @return - the list of users for this request
	 * @throws PersistenceException
	 * 		- if there were any issues with the request processing
	 */
	public List<User> getAllUsers() throws PersistenceException {
		List<User> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
				
	
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		try {
			preparedSELECTstatement = conn
			        .prepareStatement("SELECT a.id, a.name, "
									+ "b.email_address, b.cell_number, b.first_name, b.last_name, b.nick_name, b.line_id, b.activated, b.verified, "
									+ "d.name AS organization_name, d.description As organization_description, d.id AS organization_id, "
									+ "e.group_id, gadr.doc_type_ids AS allowed_doc_types, "
									+ "g.name AS group_name, h.id AS group_type_id, h.name AS group_type, h.color_hex_code AS group_type_color, "
									+ "h.associated_groups, h.order_index, j.name as role, j.id as role_id "
									+ "FROM user a "
									+ "JOIN user_contact b ON a.id = b.user_id "
									+ "JOIN user_organization c ON a.id=c.user_id "
									+ "JOIN organization d ON c.organization_id=d.id "
									+ "JOIN organization_group_rel_tree e ON d.id = e.parent_org_id "
									+ "JOIN user_group f ON a.id = f.user_id AND f.group_id = e.group_id "
									+ "JOIN group_data g ON e.group_id = g.id "
									+ "JOIN group_data_type h ON h.id = g.group_data_type_id "
									+ "JOIN group_allowed_doctype_rel gadr ON gadr.parent_group_type_id = h.id "
									+ "JOIN user_role i on i.user_id = a.id "
									+ "JOIN role j on i.role_id = j.id "
			        				);
			
			// execute the statement 
			resultSet = preparedSELECTstatement.executeQuery();
	        
	        //
	        // process the result
	        result = extractUsersFromResult(resultSet);
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
	}
	
	/**
	 * Get all the users in persistence that this user has access to in a sparse model
	 * @param user - the user on whose behalf this request is made
	 * @return - the list of users for this request (sparse data)
	 * @throws PersistenceException
	 * 		- if there were any issues with the request processing
	 */
	public List<User> getAllUsersSparse(User user) throws PersistenceException {
		List<User> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		long organizationId;
		
		// look up the user by name which should be unique
		//
		if(user != null){
			organizationId = user.getUserOrganizations().get(0).getId();
		}else{
			// use default organization
			organizationId = 1;
			
		}
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		try {
			preparedSELECTstatement = conn
			        .prepareStatement("SELECT a.id, a.name, "
			        				+ "b.email_address, b.cell_number, b.first_name, b.last_name, b.id AS contact_id, b.activated, b.verified "
			        				+ "FROM user a "
			        				+ "JOIN user_contact b ON a.id = b.user_id");
			
			// execute the statement 
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        //
	        // process the result
	        result = extractSparseUsersFromResult(resultSet);
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
	}
	
	/**
	 * Get the organization for the input user
	 * 
	 * @param username - the user for whom we are getting the organization
	 * @return - the organization that this user is associated with
	 * @throws PersistenceException
	 * 		- if there were any issues with the request processing 
	 */
	public Organization getOrganizationByUser(String username) throws PersistenceException {
		Organization result = null;
		
		// TODO implement 
		
		return result;
		
	}
	
	/**
	 * Simple password fetcher for the user.
	 * 
	 * @param username - the username to test against
	 * @return - true if the password matches; false otherwise
	 * @throws Exception
	 * 		- if there were any issues with fetching the data
	 * 
	 * <TODO> Eventually this should be a 1-way hashed operation
	 */
	public String fetchUserPassword(String username) throws Exception {
		String password = null;
		PreparedStatement preparedSELECTStatement;
		ResultSet resultSet = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// create the statement
		preparedSELECTStatement = conn
                .prepareStatement("SELECT password "
					+ "FROM user "
                	+ "WHERE name=?");
		
		// insert the variable
		preparedSELECTStatement.setString(1, username);
		
		// execute the statement 
        resultSet = preparedSELECTStatement.executeQuery();
		
        while (resultSet.next()) {
			// extract the data
        	password = resultSet.getString("password");
        }

		// release the connection
		closeConnection(conn);
		
		return password;
		
	}
	

	/**
	 * Get specific user by their username
	 * @param username - the username (case sensitive)
	 * @return - the found User entity
	 * @throws PersistenceException
	 * 		- if there were any issues with the request processing
	 */
	public User getUserByName(String username) throws PersistenceException {
		User result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		final String DIAGNOSTIC_KEY =  DIAGNOSTIC_MAP.getDiagnosticKey();
		
		// look up the user by name which should be unique
		//
		
		DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
				, "<BACK-UserDAO><Info> Looking for User: <<" + username + ">>");
		
		
		DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
				, "<BACK-UserDAO><Info> Opening Connection");
		// get the connection
		Connection conn = openConnection();
		if(conn == null){
			DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
					, "<BACK-UserDAO><WARN> <Success> Connection *NOT* Open");
		}else{
			DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
					, "<BACK-UserDAO><Info> <Success> Connection Open");
		}
		
		
		// 
		// process the request
		
		// create the statement
		try {
			preparedSELECTstatement = conn
				.prepareStatement("SELECT a.id, a.name, "
						+ "b.email_address, b.cell_number, b.first_name, b.last_name, b.nick_name, b.line_id, b.activated, b.verified, "
						+ "d.name AS organization_name, d.description As organization_description, d.id AS organization_id, "
						+ "e.group_id, gadr.doc_type_ids AS allowed_doc_types, "
						+ "g.name AS group_name, h.id AS group_type_id, h.name AS group_type, h.color_hex_code AS group_type_color, "
						+ "h.associated_groups, h.order_index, j.name as role, j.id as role_id   "
						+ "FROM user a "
						+ "JOIN user_contact b ON a.id = b.user_id "
						+ "JOIN user_organization c ON a.id=c.user_id "
						+ "JOIN organization d ON c.organization_id=d.id "
						+ "JOIN organization_group_rel_tree e ON d.id = e.parent_org_id "
						+ "JOIN user_group f ON a.id = f.user_id AND f.group_id = e.group_id "
						+ "JOIN group_data g ON e.group_id = g.id "
						+ "JOIN group_data_type h ON h.id = g.group_data_type_id "
						+ "JOIN group_allowed_doctype_rel gadr ON gadr.parent_group_type_id = h.id "
						+ "JOIN user_role i on i.user_id = a.id "
						+ "JOIN role j on i.role_id = j.id "
						+ "WHERE  a.name = ?");
			
			// insert the variable
			preparedSELECTstatement.setString(1, username);
			// execute the statement 
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
					, "<BACK-UserDAO><Info> Extracting User Info - STEP 1");
	        //
	        // process the result
	        List<User> users = extractUsersFromResult(resultSet);
	        DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
					, "<BACK-UserDAO><Info> Extracting User Info - STEP 1 DONE");
	        if(users.size() > 0){
	        	DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
						, "<BACK-UserDAO><Info> Extracting User Info - STEP 1 USER *FOUND*");
	        	result = users.get(0);
	        }else{
	        	DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
						, "<BACK-UserDAO><Info> Extracting User Info - STEP 1 USER *NOT* Found");
	        	DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
						, "<BACK-UserDAO><Info> Preparing fro Step 2...");
	        	// 
	        	// check for a super admin
	        	preparedSELECTstatement = conn
	    				.prepareStatement("SELECT a.id, a.name, "
	    						+ "b.email_address, b.cell_number, b.first_name, b.last_name, b.nick_name, b.line_id, b.activated, b.verified, "
	    						+ "d.name AS organization_name, d.description As organization_description, d.id AS organization_id, "
	    						+ "j.name as role, j.id as role_id   "
	    						+ "FROM user a "
	    						+ "JOIN user_contact b ON a.id = b.user_id "
	    						+ "JOIN user_organization c ON a.id=c.user_id "
	    						+ "JOIN organization d ON c.organization_id=d.id "
	    						+ "JOIN user_role i on i.user_id = a.id "
	    						+ "JOIN role j on i.role_id = j.id "
	    						+ "WHERE  a.name = ?");
	    			
	    			// insert the variable
	    			preparedSELECTstatement.setString(1, username);
	    			// execute the statement 
	    	        resultSet = preparedSELECTstatement.executeQuery();
	    	        
	    	        //
	    	        // process the result
	    	        DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
	    					, "<BACK-UserDAO><Info> Extracting User Info - STEP 2");
	    	        users = extractLightUsersFromResult(resultSet);
	    	        DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
	    					, "<BACK-UserDAO><Info> Extracting User Info - STEP 2 DONE");
	    	        if(users.size() > 0){
	    	        	DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
	    						, "<BACK-UserDAO><Info> Extracting User Info - STEP 2 USER *FOUND*");
	    	        	result = users.get(0);
	    	        }else{
	    	        	DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
	    						, "<BACK-UserDAO><Info> Extracting User Info - STEP 2 USER *NOT* Found");
	    	        }
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
					, "<BACK-UserDAO><ERROR> SQL Exception: " + e.getMessage());
			DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
					, "<BACK-UserDAO><ERROR> SQL Exception <ERROR CODE>: " + e.getErrorCode());
			DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
					, "<BACK-UserDAO><ERROR> SQL Exception <TRACE>: " + e.toString());
			DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
					, "<BACK-UserDAO><ERROR> SQL Exception <TRACE>: " + ExceptionUtils.getStackTrace(e));
			
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
					, "<BACK-UserDAO><ERROR> Top Exception: " + e.getMessage());
			DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
					, "<BACK-UserDAO><ERROR> Top Exception <TRACE>: " + e.toString());
			DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
					, "<BACK-UserDAO><ERROR> Top Exception <TRACE>: " + ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}


		// release the connection
		closeConnection(conn);
		DIAGNOSTIC_MAP.addDiagnostic(DIAGNOSTIC_KEY
				, "<BACK-UserDAO><Info> <Success> Connection Closed");
				
		// return the result
		return result;
				
	}
	
	
	/**
	 * Get specific user by their id
	 * @param id - the user id
	 * @return - the found User entity
	 * @throws PersistenceException
	 * 		- if there were any issues with the request processing
	 */
	public User getUserByUserId(long id) throws PersistenceException {
		User result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		try {
			preparedSELECTstatement = conn
				.prepareStatement("SELECT a.id, a.name, "
						+ "b.email_address, b.cell_number, b.first_name, b.last_name, b.nick_name, b.line_id, b.activated, b.verified, "
						+ "d.name AS organization_name, d.description As organization_description, d.id AS organization_id, "
						+ "e.group_id, gadr.doc_type_ids AS allowed_doc_types, "
						+ "g.name AS group_name, h.id AS group_type_id, h.name AS group_type, h.color_hex_code AS group_type_color, "
						+ "h.associated_groups, h.order_index, j.name as role, j.id as role_id  "
						+ "FROM user a "
						+ "JOIN user_contact b ON a.id = b.user_id "
						+ "JOIN user_organization c ON a.id=c.user_id "
						+ "JOIN organization d ON c.organization_id=d.id "
						+ "JOIN organization_group_rel_tree e ON d.id = e.parent_org_id "
						+ "JOIN user_group f ON a.id = f.user_id AND f.group_id = e.group_id "
						+ "JOIN group_data g ON e.group_id = g.id "
						+ "JOIN group_data_type h ON h.id = g.group_data_type_id "
						+ "JOIN group_allowed_doctype_rel gadr ON gadr.parent_group_type_id = h.id "
						+ "JOIN user_role i on i.user_id = a.id "
						+ "JOIN role j on i.role_id = j.id "
						+ "WHERE  a.id = ?");
			
			// insert the variable
			preparedSELECTstatement.setLong(1, id);
			// execute the statement 
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        //
	        // process the result
	        List<User> users = extractUsersFromResult(resultSet);
	        if(users.size() > 0){
	        	result = users.get(0);
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
				
	}
	
	/**
	 * Get the profile image for the user
	 * @param userId - the id of the user
	 * @return - the bytes representing the image for the profile or null of there is not one
	 * @throws PersistenceException
	 * 		- if there were any issues in processing this request
	 */
	public InputStream getUserProfileImage(long userId) throws PersistenceException {
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		InputStream binaryStream=null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		try {
			preparedSELECTstatement = conn
			        .prepareStatement("SELECT profile_image from user WHERE id=?");
			
			// execute the statement 
			preparedSELECTstatement.setLong(1, userId);
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        while (resultSet.next()) {
				// extract the data
	        	binaryStream = resultSet.getBinaryStream("profile_image");
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        

		// release the connection
		closeConnection(conn);
				
		// return the result
		return binaryStream;
				
	}
	
	/**
	 * Update the profile image for the user
	 * @param profileImage - the image data
	 * @param username - the user for whom the update is done
	 * @throws PersistenceException
	 * 		- if there were any issues in processing this request
	 */
	public void updateUserProfileImage(InputStream profileImage, String  username) throws PersistenceException {
		BufferedImage bImageFromConvert = null;
		
		PreparedStatement preparedUPDATEStatement;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		try {
			bImageFromConvert = ImageIO.read(profileImage);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write( bImageFromConvert, "png", baos );
			baos.flush();
			byte[] profileImageBytes = baos.toByteArray();
			baos.close();
			
			// create the query
			String updateQuery = " UPDATE user "
					+ "set profile_image = ? "
					+ "WHERE name = ? ";
			
			// create the statement
			preparedUPDATEStatement = conn
	                .prepareStatement(updateQuery);
			// execute the statement
			preparedUPDATEStatement.setBlob(1, new javax.sql.rowset.serial.SerialBlob(profileImageBytes));
			preparedUPDATEStatement.setString(2, username);
			preparedUPDATEStatement.executeUpdate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// release the connection
		closeConnection(conn);
		
		getLog().info("UPDATED PROFILE IMAGE for user " + username);
		
	}
	
	/**
	 * Create a new user token in the database with the specific expiry date.
	 * 
	 * @param credentials - the user credentials
	 * @throws PersistenceException 
	 * 		- if there were any issues with the request/processing
	 */
	public void writeUserToken(UserCredentials credentials) throws PersistenceException {
		PreparedStatement preparedINSERTStatement;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the query
		String insertQuery = " insert into security_token ("
				+ "user_id, "
				+ "token_value, "
				+ "expiration_date"
				+ ")"
		        + " values (?, ?, ?)";
		
		// create the statement
		try {
			preparedINSERTStatement = conn
			        .prepareStatement(insertQuery);
			
			// execute the statement 
			preparedINSERTStatement.setLong(1, getUserByName(credentials.getUsername()).getId());
			preparedINSERTStatement.setString(2, credentials.getToken().getTokenValue());
			preparedINSERTStatement.setTimestamp(3, DateUtility.convertToSQLTimestamp(credentials.getToken().getExpirationDate()));
			
			preparedINSERTStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new PersistenceException(
					"Cannot write session token to persistence. " 
					+ credentials.getUsername(), 
					e);
		}
		
		
		// release the connection
		closeConnection(conn);
		
	}
	
	/**
	 * Get the credentials for this user in the data base
	 * This will return a list of all the tokens for this user sorted by the latest first
	 * There is no differentiation on invalidated tokens
	 * 
	 * @param username - the username that we want the credentials for
	 * @return - the list (possibly empty) of all the credentials for this user
	 * @throws PersistenceException
	 * 		- if there were any issues
	 */
	public List<UserCredentials> fetchUserTokens(String username) throws PersistenceException {
		List<UserCredentials> resultCredentials = new ArrayList<UserCredentials>();
		
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		try {
			preparedSELECTstatement = conn
			        .prepareStatement("SELECT * from security_token WHERE user_id=?");
			
			//
			// get the user id
			User user = getUserByName(username);
			// check that the user exists
			if(user == null){
				throw new PersistenceException(
						"User does not exist: " 
						+ username);
			}
			
			// execute the statement 
			preparedSELECTstatement.setLong(1, user.getId());
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        while (resultSet.next()) {
				// extract the data
	        	UserCredentials credentials = new UserCredentials();
	        	SecurityToken token = new SecurityToken();
	        	token.setTokenValue(resultSet.getString("token_value"));
	        	token.setExpirationDate(DateUtility.convertFromSQLTimestamp(resultSet.getTimestamp("expiration_date")));
	        	token.setInvalidated(resultSet.getBoolean("invalidated"));
	        	credentials.setToken(token);
	        	credentials.setUsername(username);
	        	
	        	//
	        	// add the data to the list
	        	resultCredentials.add(credentials);
	        	
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//
		// Sort the list
		Collections.sort(resultCredentials);
		
		// release the connection
		closeConnection(conn);
		
		return resultCredentials;
	}
	
	/**
	 * Get the credentials for this user in the data base
	 * This will return a list of all the tokens for this user sorted by the latest first
	 * There is no differentiation on invalidated tokens
	 * THis is an overloaded version of the method to limit number of returned tokens
	 * 
	 * @param username - the username that we want the credentials for
	 * @return - the list (possibly empty) of all the credentials for this user
	 * @throws PersistenceException
	 * 		- if there were any issues
	 * 		- if the number of tokens being requested is <= 0 
	 */
	public List<UserCredentials> fetchUserTokens(String username, int maxTokens) throws PersistenceException {
		List<UserCredentials> resultCredentials = new ArrayList<UserCredentials>();
		
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		try {
			preparedSELECTstatement = conn
			        .prepareStatement("SELECT * from security_token WHERE user_id=? LIMIT ?");
			
			//
			// get the user id
			User user = getUserByName(username);
			// check that the user exists
			if(user == null){
				throw new PersistenceException(
						"User does not exist: " 
						+ username);
			}
			
			// execute the statement 
			preparedSELECTstatement.setLong(1, user.getId());
			preparedSELECTstatement.setInt(2, maxTokens);
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        while (resultSet.next()) {
				// extract the data
	        	UserCredentials credentials = new UserCredentials();
	        	SecurityToken token = new SecurityToken();
	        	token.setTokenValue(resultSet.getString("token_value"));
	        	token.setExpirationDate(DateUtility.convertFromSQLTimestamp(resultSet.getTimestamp("expiration_date")));
	        	token.setInvalidated(resultSet.getBoolean("invalidated"));
	        	credentials.setToken(token);
	        	credentials.setUsername(username);
	        	
	        	//
	        	// add the data to the list
	        	resultCredentials.add(credentials);
	        	
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//
		// Sort the list
		Collections.sort(resultCredentials);
		
		// release the connection
		closeConnection(conn);
		
		return resultCredentials;
		
	}
	
	/**
	 * This will invalidate the token regardless of its expiry.
	 * @param credentials - the user credentials
	 * @throws PersistenceException
	 * 		- if there were any issues with the request/processing
	 */
	public void invalidateUserToken(UserCredentials credentials) throws PersistenceException {
		// create the java mysql update preparedstatement
	    String query = "UPDATE security_token set invalidated = 1 WHERE token_value = ?";
	    
	    //
	    // Check for input data
	    if(credentials == null 
	    		|| credentials.getToken() == null
	    		|| credentials.getToken().getTokenValue() == null){
	    	// do nothing
	    	return;
	    }
	    
	    // get the connection
	 	Connection conn = openConnection();
	    
	    PreparedStatement preparedUPDATEstatement;
		try {
			preparedUPDATEstatement = conn.prepareStatement(query);
			preparedUPDATEstatement.setString(1, credentials.getToken().getTokenValue());
		    
		    // execute the java preparedstatement
		    preparedUPDATEstatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new PersistenceException(
					"Cannot invalidate the session token: " 
							+ credentials.getUsername(), 
					e);
		}
	    
		// release the connection
		closeConnection(conn);  
		
	}
	
	/**
	 * This will create user contact data.
	 * @param user - the user
	 * @throws PersistenceException
	 * 		- if there were any issues with the request/processing
	 */
	public User createUserContact(User user) throws PersistenceException {
		PreparedStatement preparedINSERTstatement;
		long returnId=0;
		
		// get the connection
		Connection conn = openConnection();
		
	
		// create the query for user
		String insertQuery = " insert into user_contact ("
				+ "email_address, "
				+ "cell_number, "
				+ "first_name, "
				+ "last_name, "
				+ "user_id, "
				+ "activated, "
				+ "verified"
				+ ")"
		        + " values (?,?,?,?,?,?,?)";
		
		//
		// create the user data
		try {
			preparedINSERTstatement = conn
			        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			
			// get the data
			String emailAddress =  user.getContactInfo().getEmailAddress();
			String cellNumber = user.getContactInfo().getCellNumber();
			String firstName = user.getContactInfo().getFirstName();
			String lastName = user.getContactInfo().getLastName();
			boolean isActivated = user.getContactInfo().isActivated();
			boolean isVerified = user.getContactInfo().isVerified();

			// execute the statement 
			preparedINSERTstatement.setString(1, emailAddress);
			preparedINSERTstatement.setString(2,cellNumber);
			preparedINSERTstatement.setString(3,firstName);
			preparedINSERTstatement.setString(4,lastName);
			preparedINSERTstatement.setLong(5,user.getId());
			preparedINSERTstatement.setBoolean(6,isActivated);
			preparedINSERTstatement.setBoolean(7,isVerified);
			
			
			preparedINSERTstatement.executeUpdate();
			
			// get the id of the created 
			ResultSet rs = preparedINSERTstatement.getGeneratedKeys();
			if (rs.next()){
				returnId=rs.getLong(1);
				user.getContactInfo().setId(returnId);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		// release the connection
		closeConnection(conn);
		

		return user;
		
	}
	
	/**
	 * This will create the user role data
	 * @param user - the user data
	 * @throws PersistenceException
	 * 		- if there were any issues with the request/processing
	 */
	public User createUserRole(User user) throws PersistenceException {
		PreparedStatement preparedINSERTstatement;
		
		// get the connection
		Connection conn = openConnection();
			
		// create the query for user
		String insertQuery = " insert into user_role ("
				+ "user_id, "
				+ "role_id "
				+ ")"
		        + " values (?,?)";
		
		//
		// create the user data
		try {
			preparedINSERTstatement = conn
			        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			
			// get the data
			long userId =  user.getId();
			long roleId = user.getRoles().get(0).getId();

			// execute the statement 
			preparedINSERTstatement.setLong(1, userId);
			preparedINSERTstatement.setLong(2, roleId);
			
			preparedINSERTstatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// release the connection
		closeConnection(conn);
		
		return user;
	}
	
	/**
	 * This will create the user to group association
	 * @param user - the user data
	 * @throws PersistenceException
	 * 		- if there were any issues with the request/processing
	 */
	public User createUserGroup(User user) throws PersistenceException {
		PreparedStatement preparedINSERTstatement;
		
		// get the connection
		Connection conn = openConnection();
			
		// create the query for user
		String insertQuery = " insert into user_group ("
				+ "user_id, "
				+ "group_id "
				+ ")"
		        + " values (?,?)";
		
		//
		// create the user data
		try {
			preparedINSERTstatement = conn
			        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			
			// get the data
			long userId =  user.getId();
			long groupId = user.getUserGroups().get(0).getId();

			// execute the statement 
			preparedINSERTstatement.setLong(1, userId);
			preparedINSERTstatement.setLong(2, groupId);
			
			preparedINSERTstatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// release the connection
		closeConnection(conn);
		
		return user;
	}
	
	/**
	 * This will associate the user with an organization
	 * @param user - the user data
	 * @throws PersistenceException
	 * 		- if there were any issues with the request/processing
	 */
	public User createUserOrganization(User user) throws PersistenceException {
		PreparedStatement preparedINSERTstatement;
		
		// get the connection
		Connection conn = openConnection();
			
		// create the query for user
		String insertQuery = " insert into user_organization ("
				+ "user_id, "
				+ "organization_id "
				+ ")"
		        + " values (?,?)";
		
		//
		// create the user data
		try {
			preparedINSERTstatement = conn
			        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			
			// get the data
			long userId =  user.getId();
			long organizationId = user.getUserGroups().get(0).getOrganizationId();

			// execute the statement 
			preparedINSERTstatement.setLong(1, userId);
			preparedINSERTstatement.setLong(2, organizationId);
			
			preparedINSERTstatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// release the connection
		closeConnection(conn);
		
		return user;
	}
	
	/**
	 * This will link the organization and group data for this user
	 * @param user - the user data
	 * @throws PersistenceException
	 * 		- if there were any issues with the request/processing
	 */
	public User createUserOrganizationGroupLink(User user) throws PersistenceException {
		PreparedStatement preparedINSERTstatement;
		
		// get the connection
		Connection conn = openConnection();
			
		// create the query for user
		String insertQuery = " insert into organization_group_rel_tree ("
				+ "parent_org_id, "
				+ "group_id "
				+ ")"
		        + " values (?,?)";
		
		//
		// create the user data
		try {
			preparedINSERTstatement = conn
			        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			
			// get the data
			long parentOrgId =  user.getUserGroups().get(0).getOrganizationId();
			long groupId = user.getUserGroups().get(0).getId();

			// execute the statement 
			preparedINSERTstatement.setLong(1, parentOrgId);
			preparedINSERTstatement.setLong(2, groupId);
			
			preparedINSERTstatement.executeUpdate();
		} catch(java.sql.SQLIntegrityConstraintViolationException e){
			//
			// dismiss we do not worry about this one
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// release the connection
		closeConnection(conn);
		
		return user;
	}
	
	/**
	 * This will update the user contact data
	 * @param user - the user data
	 * @throws PersistenceException
	 * 		- if there were any issues with the request/processing
	 */
	public void updateUserContact(User user) throws PersistenceException {
		PreparedStatement preparedUPDATEStatement;
		
		// get the connection
		Connection conn = openConnection();
		
	
		// create the query for user
		// create the query
		String updateQuery = " UPDATE user_contact "
				+ "set email_address = ?, "
				+ "cell_number = ?, "
				+ "first_name = ?, "
				+ "last_name = ?, "
				+ "nick_name = ?, "
				+ "line_id = ?, "
				+ "activated = ?, "
				+ "verified = ? "
				+ "WHERE user_id = ?";
		
		//
		// create the user data
		try {
			
			// get the data
			String emailAddress =  user.getContactInfo().getEmailAddress();
			String cellNumber = user.getContactInfo().getCellNumber();
			String firstName = user.getContactInfo().getFirstName();
			String lastName = user.getContactInfo().getLastName();
			String nickName = user.getContactInfo().getNickName();
			String lineId = user.getContactInfo().getLineId();
			boolean activated = user.getContactInfo().isActivated();
			boolean verified = user.getContactInfo().isVerified();
			
			// create the statement
			preparedUPDATEStatement = conn
	                .prepareStatement(updateQuery);
			// execute the statement
			preparedUPDATEStatement.setString(1, emailAddress);
			preparedUPDATEStatement.setString(2, cellNumber);
			preparedUPDATEStatement.setString(3, firstName);
			preparedUPDATEStatement.setString(4, lastName);
			preparedUPDATEStatement.setString(5, nickName);
			preparedUPDATEStatement.setString(6, lineId);
			preparedUPDATEStatement.setBoolean(7, activated);
			preparedUPDATEStatement.setBoolean(8, verified);
			preparedUPDATEStatement.setLong(9, user.getId());
			preparedUPDATEStatement.executeUpdate();

			
			//execute
			preparedUPDATEStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		// release the connection
		closeConnection(conn);
		
	}
	
	
	/**
	 * Checks of the given email address is unique in the system
	 * @param emailAddress - the email address to check
	 * @return - true if it is unique and false otherwise
	 * @throws PersistenceException - if there was an issue with persistence
	 */
	public boolean isEmailUnique(String emailAddress) throws PersistenceException {
		boolean result = true;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		try {
			preparedSELECTstatement = conn
			        .prepareStatement("SELECT * from user_contact WHERE email_address=?");
			
		
			// execute the statement 
			preparedSELECTstatement.setString(1, emailAddress);
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        while (resultSet.next()) {
				// extract the data
	        	result = false;
	        	
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Checks of the given username is unique in the system
	 * @param username - the username address to check
	 * @return - true if it is unique and false otherwise
	 * @throws PersistenceException - if there was an issue with persistence
	 */
	public boolean isUsernameUnique(String username) throws PersistenceException {
		boolean result = true;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		try {
			preparedSELECTstatement = conn
			        .prepareStatement("SELECT * from user WHERE name=?");
			
		
			// execute the statement 
			preparedSELECTstatement.setString(1, username);
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        while (resultSet.next()) {
				// extract the data
	        	result = false;
	        	
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}


	/**
	 * Pre and Post condition for PasswordCredentials Object validity
	 * @param credentials - the PasswordCredentials object to test
	 * @return - true if the object is valid and false otherwise
	 */
	public boolean isObjectValid(PasswordCredentials credentials) {
		boolean result = true;
		
		//
		// Ensure that the required data is present
		if(credentials.getUsername() == null || credentials.getUsername().isEmpty()){
			return false;
		}
		if(credentials.getPassword() == null || credentials.getPassword().isEmpty()){
			return false;
		}
		return result;
	}
	
	/**
	 * Pre and Post condition for User Object validity
	 * @param user - the User object to test
	 * @return - true if the object is valid and false otherwise
	 */
	public boolean isObjectValid(User user, boolean strict) {
		boolean result = true;
		
		//
		// Ensure that the required data is present
		if(user.getName() == null || user.getName().isEmpty()){
			return false;
		}
		if(user.getContactInfo().getEmailAddress() == null || user.getContactInfo().getEmailAddress().isEmpty()){
			return false;
		}
		if(user.getContactInfo().getFirstName() == null || user.getContactInfo().getFirstName().isEmpty()){
			return false;
		}
		if(user.getContactInfo().getLastName() == null || user.getContactInfo().getLastName().isEmpty()){
			return false;
		}
		if(user.getCredentials().getUsername() == null || user.getCredentials().getUsername().isEmpty()){
			return false;
		}
		if(strict) {
			if(user.getRoles() == null || user.getRoles().isEmpty()){
				return false;
			}
			if(user.getRoles().get(0).getId() <= 0){
				return false;
			}
			if(user.getUserGroups() == null || user.getUserGroups().isEmpty()){
				return false;
			}
			if(user.getUserGroups().get(0).getId() <= 0 || user.getUserGroups().get(0).getOrganizationId() <= 0){
				return false;
			}
		}
		
		
		return result;
	}
	
	/**
	 * Pre and Post condition for User Object validity
	 * @param user - the User object to test
	 * @return - true if the object is valid and false otherwise
	 */
	public boolean isObjectValid(UserContact userContact) {
		boolean result = true;
		
		//
		// Ensure that the required data is present and valid

		// check the email address format
		if(!EmailUtils.isValid(userContact.getEmailAddress())){
			return false;	// invalid email address format
		}
		
		// 
		if(userContact.getEmailAddress() == null || userContact.getEmailAddress().isEmpty()){
			return false;
		}
		if(userContact.getFirstName() == null || userContact.getFirstName().isEmpty()){
			return false;
		}
		if(userContact.getLastName() == null || userContact.getLastName().isEmpty()){
			return false;
		}
		
		return result;
	}
	
	/**
	 * Update the profile image for the user
	 * @param profileImage - the image data
	 * @param username - the user for whom the update is done
	 * @throws PersistenceException
	 * 		- if there were any issues in processing this request
	 */
	public List<User> importBatchUserCSVFile(byte [] bDocImportCSV, String username, String orgName) throws PersistenceException {
		List<User> result = new ArrayList<User>();
		List<GroupType> allGroupTypes=null;
		Map<String, GroupType> mappedGroupTypes = new HashMap<String, GroupType>();
		
		organizationService.init();
	    
		//
		// get the bytes of the file
	    try {
	          
				// get the reader for the CSV file
				CSVReader csvReader = new CSVReaderBuilder(
				    new InputStreamReader(
				        new ByteArrayInputStream(bDocImportCSV)))
				  			.withSkipLines(1) 
				  			.build();
				
				// fetch all user roles
				List<Role> roles = fetchUserRoles();
				 //
				// get the user data
				User user = getUserByName(username);
				 // get all the organizations
				allGroupTypes = organizationService.getAllGroupTypes(user.getUserOrganizations().get(0).getId());
				// create a map based on the group data names
				for(int i=0; i<allGroupTypes.size(); i++){
					mappedGroupTypes.put(allGroupTypes.get(i).getName(), allGroupTypes.get(i));
				}
				// Process Organization Name
            	Group groupOrg = new Group();
            	groupOrg = organizationService.getOrganizationGroupByName(
            			user.getUserOrganizations().get(0).getId(), 
            			orgName.toLowerCase()
            	);
            	
				
				//
				// Fetch the CVS File contents
				List<String[]> allData = csvReader.readAll(); 
	    
	          //
	          // Process the CSV File contents
	          for (String[] row : allData) { 
	        	  // process the user information column by column
                  User parsedUser = new User(); 
                  // add to the user
              	  parsedUser.getUserGroups().add(groupOrg);
                  UserContact parsedUserContact = new UserContact();
	        	  int column = 0;	// column counter
	              for (String cell : row) { 
	                  System.out.print(cell + "\t"); 

	                  switch(column++) {
		                  case 0:
		                    // Process First Name
		                	parsedUserContact.setFirstName(cell);
		                    break;
		                    
		                  case 1:
		                	// Process Last Name
		                	parsedUserContact.setLastName(cell);
		                    break;
		                    
		                  case 2:
		                	// Process username
		                	parsedUser.getCredentials().setUsername(cell);
		                    break;
		                    
		                  case 3:
		                	// Process email address
		                	parsedUserContact.setEmailAddress(cell);
		                    break;
		                    
		                  case 4:
		                	// Process Cell Number
		                	parsedUserContact.setCellNumber(cell);
		                    break;
		                    
		                  case 5:
		                	// Process User Type (i.e. role)
		                	Iterator<Role> it = roles.iterator();
		                	while(it.hasNext()){
		                		Role tempRole = it.next();
		                		if(tempRole.getValue().toLowerCase().equals(cell.toLowerCase())){
		                			// found it
		                			List<Role> rolesList  = new ArrayList<Role>();
		                			rolesList.add(tempRole);
		                			parsedUser.setRoles(rolesList);
		                			// exit loop
		                			break;
		                		}
		                	}
		                    break;
	
		                  default:
		                    // Nothing to do
	                } // end switch
	                  
	              } // for loop per row
	              
	              //
	              // add the rest of the data
	              parsedUserContact.setVerified(true);
	              parsedUser.setContactInfo(parsedUserContact);
	              
	              //
	              // create the user
	              parsedUser = create(parsedUser);
	              
	              result.add(parsedUser);
	              System.out.println(); 
	          } // for loop total
	          
	          
	    } catch (Exception e) { 
	        e.printStackTrace();
	    }
		
		return result;
		
	}
	

	
	/**
	 * Update the profile image for the user
	 * @param profileImage - the image data
	 * @param username - the user for whom the update is done
	 * @throws PersistenceException
	 * 		- if there were any issues in processing this request
	 */
	public List<Group> importBatchOrgCSVFile(byte [] bDocImportCSV, String username,  String stageId) throws PersistenceException {
		List<Group> result = new ArrayList<Group>();
		List<GroupType> allGroupTypes=null;
		Map<String, GroupType> mappedGroupTypes = new HashMap<String, GroupType>();
		
		organizationService.init();
	    
		//
		// get the bytes of the file
	    try {
	          
				// get the reader for the CSV file
				CSVReader csvReader = new CSVReaderBuilder(
				    new InputStreamReader(
				        new ByteArrayInputStream(bDocImportCSV)))
				  			.withSkipLines(1) 
				  			.build();

				 //
				// get the user data
				User user = getUserByName(username);
				 // get all the organizations
				allGroupTypes = organizationService.getAllGroupTypes(user.getUserOrganizations().get(0).getId());
				// create a map based on the group data names
				for(int i=0; i<allGroupTypes.size(); i++){
					mappedGroupTypes.put(allGroupTypes.get(i).getName(), allGroupTypes.get(i));
				}
            	//groupOrg = organizationService.getOrganizationGroupByName(
            	//		user.getUserOrganizations().get(0).getId(), 
            			//orgName.toLowerCase()
            	//);
            	
				
				//
				// Fetch the CVS File contents
				List<String[]> allData = csvReader.readAll(); 
	    
	          //
	          // Process the CSV File contents
	          for (String[] row : allData) { 
	        	  // process the user information column by column
                  Group parsedGroup = new Group();
                  User parsedUser = new User();
                  // add to the user
                      UserContact parsedUserContact = new UserContact();
	        	  int column = 0;	// column counter
	              for (String cell : row) { 
	                  System.out.print(cell + "\t"); 

	                  switch(column++) {
		                  case 0:
		                    // Process Display Name
		                	parsedGroup.setName(cell);
		                    break;
		                    
		                  case 1:
		                	// Process Legal Business Name
		                	parsedGroup.setLegalBusinessName(cell);
		                    break;
		                    
		                  case 2:
		                	// Process Business Number
		                	parsedGroup.setBusinessIDNumber(cell);
		                    break;
		                    
		                  case 3:
		                	// Process Business Address
		                	parsedGroup.setBusinessAddress(cell);
		                    break;
		                    
		                  case 4:
		                	// Process GPS Location
			                parsedGroup.setGpsCoordinates(cell);
		                    break;
		                    
		                  case 5:
		                	// Process Email Address
				            parsedGroup.setEmailAddress(cell);	
				            parsedUserContact.setEmailAddress(cell);
		                    break;
		                    
		                  case 6:
			                	// Process First Name
					            parsedUserContact.setFirstName(cell);
			                    break;
			                    
		                  case 7:
			                	// Process Last Name
					            parsedUserContact.setLastName(cell);
			                    break;
	
		                  default:
		                    // Nothing to do
	                } // end switch
	                
	                  
	              } // for loop per row
	              GroupType groupType = organizationService.getGroupDataTypeById(Long.parseLong(stageId)).get(0);
	              parsedGroup.setGroupType(groupType);
	              parsedGroup.setOrganizationId(groupType.getMatrixId());
	              //
	              // set it as verified
	              parsedGroup.setVerified(true);
                  //
                  // create the entry
                  organizationService.createOrganizationGroup(parsedGroup);
                  //
                  // create the user who is the admin for this new group
	              
	              //
	              // add the rest of the data
	              parsedUserContact.setVerified(true);
	              parsedUser.setName(parsedUserContact.getEmailAddress().split("@")[0]);
	              parsedUser.getCredentials().setUsername(parsedUserContact.getEmailAddress().split("@")[0]);
	              parsedUser.setContactInfo(parsedUserContact);
	              parsedUser.getUserGroups().add(parsedGroup);
	              // set role fororg admin
	              Role orgAdminRole = new Role();
	              orgAdminRole.setId(7); 
	              orgAdminRole.setName(Role.ROLE_NAME_ORG_ADMIN);
	              parsedUser.getRoles().add(orgAdminRole);
	              
	              //
	              // create the user
	              parsedUser = create(parsedUser);
	              
	              //result.add(parsedUser);
	              System.out.println(); 
	          } // for loop total
	          
	          
	    } catch (Exception e) { 
	        e.printStackTrace();
	    }
		
		return result;
		
	}
	
	
	
	/**
	 * Update the profile image for the user
	 * @param profileImage - the image data
	 * @param username - the user for whom the update is done
	 * @throws PersistenceException
	 * 		- if there were any issues in processing this request
	 */
	public Group importOrgRegistration(Group group,long stageId, long orgId) throws PersistenceException {
		List<GroupType> allGroupTypes=null;
		Map<String, GroupType> mappedGroupTypes = new HashMap<String, GroupType>();
		User user = new User();
		UserContact userContact = new UserContact();
		
		organizationService.init();
	    
		//
		// get the bytes of the file
	    try {

			 // get all the organizations
			allGroupTypes = organizationService.getAllGroupTypes(orgId);
			// create a map based on the group data names
			for(int i=0; i<allGroupTypes.size(); i++){
				mappedGroupTypes.put(allGroupTypes.get(i).getName(), allGroupTypes.get(i));
			}

			GroupType groupType = organizationService.getGroupDataTypeById(stageId).get(0);
			group.setGroupType(groupType);
			group.setOrganizationId(groupType.getMatrixId());
			//
			// set it as *NOT* verified
			group.setVerified(false);
			//
			// create the entry
			organizationService.createOrganizationGroup(group);
			//
			// create the user who is the admin for this new group
			user = group.getUsers().get(0);
			//
			// add the rest of the data
			user.getContactInfo().setVerified(false);
			user.setName(user.getContactInfo().getEmailAddress().split("@")[0]);
			user.getCredentials().setUsername(user.getContactInfo().getEmailAddress().split("@")[0]);
			user.getUserGroups().add(group);
			// set role fororg admin
			Role orgAdminRole = new Role();
			orgAdminRole.setId(7); 
			orgAdminRole.setName(Role.ROLE_NAME_ORG_ADMIN);
			user.getRoles().add(orgAdminRole);
			  
			//
			// create the user
			user = create(user);
	          
	          
	    } catch (Exception e) { 
	        e.printStackTrace();
	    }
		
		return group;
		
	}
	
	
	
	/**
	 * Update the profile image for the user
	 * @param profileImage - the image data
	 * @param username - the user for whom the update is done
	 * @throws PersistenceException
	 * 		- if there were any issues in processing this request
	 */
	public Object verifyBatchUserCSVFile(byte [] bDocImportCSV, String username) throws PersistenceException {
		List<String> result = new ArrayList<String>();
		ResponseErrorData errorData = new ResponseErrorData();
		ResponseMessageData messageData = new ResponseMessageData();
		boolean errorsFlag = false;
		
		
		organizationService.init();
	    
		//
		// get the bytes of the file
	    try {
	          
				// get the reader for the CSV file
				CSVReader csvReader = new CSVReaderBuilder(
				    new InputStreamReader(
				        new ByteArrayInputStream(bDocImportCSV)))
				  			.withSkipLines(1) 
				  			.build();
				
				// fetch all user roles
				List<Role> roles = fetchUserRoles();
				 //
				// get the user data
				User user = getUserByName(username);
				
				//
				// Fetch the CVS File contents
				List<String[]> allData = csvReader.readAll(); 
	    
	          //
	          // Process the CSV File contents
				
			  // line numbers
			  int lineNumber = 1;	
	          for (String[] row : allData) { 
	        	  int column = 0;								// column counter
	        	  
	              for (String cell : row) { 
	                  System.out.print(cell + "\t"); 
	                  
	                  switch(column++) {
                  		case 0:
                  			//
		                    // Process First Name
                  			//
		                	if(cell.isEmpty()){
		                		// Add empty issue
		                		result.add(
	                				RESTResponse.createCVSErrorMessage(
                						lineNumber, 
                						"First Name should not be empty", 
                						column + 1, 
                						"First Name"));
		                		errorsFlag = true;
		                		// set the issue
		                		ResponseIssue issue = new ResponseIssue();
		                		issue.setLineNumber(String.valueOf(lineNumber));
		                		issue.setColumnNumber(String.valueOf(column + 1));
		                		issue.setColumnName("First Name");
		                		issue.setIssue("First Name should not be empty");
		                		issue.setRawMessage(result.get(result.size()-1));
		                		issue.setSeverity(ResponseIssue.ISSUE_SEVERITY_FATAL_ERROR);
		                		errorData.getIssues().add(issue);
		                	}
		                    break;
		                    
                  		case 1:
                  			//
		                	// Process Last Name
                  			//
							if(cell.isEmpty()){
								// Add empty issue
								result.add(
									RESTResponse.createCVSErrorMessage(
										lineNumber, 
										"Last Name should not be empty", 
										column + 1,
										"Last Name"));
								errorsFlag = true;
								// set the issue
		                		ResponseIssue issue = new ResponseIssue();
		                		issue.setLineNumber(String.valueOf(lineNumber));
		                		issue.setColumnNumber(String.valueOf(column + 1));
		                		issue.setColumnName("Last Name");
		                		issue.setIssue("Last Name should not be empty");
		                		issue.setRawMessage(result.get(result.size()-1));
		                		issue.setSeverity(ResponseIssue.ISSUE_SEVERITY_FATAL_ERROR);
		                		errorData.getIssues().add(issue);
							}
		                    break;
		                    
                  		case 2:
                  			//
							// Process username
                  			//
                  			
                  			// check for empty username
							if(cell.isEmpty()){
								// Add empty issue
								result.add(
									RESTResponse.createCVSErrorMessage(
										lineNumber, 
										"username should not be empty", 
										column + 1,
										"username"));
								errorsFlag = true;
								// set the issue
		                		ResponseIssue issue = new ResponseIssue();
		                		issue.setLineNumber(String.valueOf(lineNumber));
		                		issue.setColumnNumber(String.valueOf(column + 1));
		                		issue.setColumnName("username");
		                		issue.setIssue("username should not be empty");
		                		issue.setRawMessage(result.get(result.size()-1));
		                		issue.setSeverity(ResponseIssue.ISSUE_SEVERITY_FATAL_ERROR);
		                		errorData.getIssues().add(issue);
							}
							
							// check uniqueness
							if(!cell.isEmpty() && !isUsernameUnique(cell)){
								// Add unique issue
								result.add(
									RESTResponse.createCVSErrorMessage(
										lineNumber, 
										"username must be unique. " + cell + " is already taken.", 
										column + 1,
										"username"));
								errorsFlag = true;
								// set the issue
		                		ResponseIssue issue = new ResponseIssue();
		                		issue.setLineNumber(String.valueOf(lineNumber));
		                		issue.setColumnNumber(String.valueOf(column + 1));
		                		issue.setColumnName("username");
		                		issue.setIssue("username must be unique. " + cell + " is already taken.");
		                		issue.setRawMessage(result.get(result.size()-1));
		                		issue.setSeverity(ResponseIssue.ISSUE_SEVERITY_FATAL_ERROR);
		                		errorData.getIssues().add(issue);
							}
		                    break;
		                    
                  		case 3:
                  			//
							// Process email address
                  			//
                  			
                  			// check for empty email address
							if(cell.isEmpty()){
								// Add empty issue
								result.add(
									RESTResponse.createCVSErrorMessage(
										lineNumber, 
										"email should not be empty", 
										column + 1,
										"email"));
								errorsFlag = true;
								// set the issue
		                		ResponseIssue issue = new ResponseIssue();
		                		issue.setLineNumber(String.valueOf(lineNumber));
		                		issue.setColumnNumber(String.valueOf(column + 1));
		                		issue.setColumnName("email");
		                		issue.setIssue("email should not be empty.");
		                		issue.setRawMessage(result.get(result.size()-1));
		                		issue.setSeverity(ResponseIssue.ISSUE_SEVERITY_FATAL_ERROR);
		                		errorData.getIssues().add(issue);
							}
							
							// check uniqueness
							if(!cell.isEmpty() && !isEmailUnique(cell)){
								// Add unique issue
								result.add(
									RESTResponse.createCVSErrorMessage(
										lineNumber, 
										"email address must be unique. " + cell + " is already taken.", 
										column + 1,
										"email"));
								errorsFlag = true;
								// set the issue
		                		ResponseIssue issue = new ResponseIssue();
		                		issue.setLineNumber(String.valueOf(lineNumber));
		                		issue.setColumnNumber(String.valueOf(column + 1));
		                		issue.setColumnName("email");
		                		issue.setIssue("email address must be unique. " + cell + " is already taken.");
		                		issue.setRawMessage(result.get(result.size()-1));
		                		issue.setSeverity(ResponseIssue.ISSUE_SEVERITY_FATAL_ERROR);
		                		errorData.getIssues().add(issue);
							}
							
							// check email formatting
							if(!cell.isEmpty() && isEmailUnique(cell)){
								// Add formatting issue
								if(!EmailUtils.isValid(cell)) {
									String errorMessage = "Email format issue. " + cell + " is *NOT* a valid email address.";
									result.add(
											
											RESTResponse.createCVSErrorMessage(
												lineNumber, 
												errorMessage, 
												column + 1,
												"email"));
										errorsFlag = true;
										// set the issue
				                		ResponseIssue issue = new ResponseIssue();
				                		issue.setLineNumber(String.valueOf(lineNumber));
				                		issue.setColumnNumber(String.valueOf(column + 1));
				                		issue.setColumnName("email");
				                		issue.setIssue(errorMessage);
				                		issue.setRawMessage(result.get(result.size()-1));
				                		issue.setSeverity(ResponseIssue.ISSUE_SEVERITY_FATAL_ERROR);
				                		errorData.getIssues().add(issue);
								}
								
							}
		                    break;
		                    
		                  case 4:
		                	// Process Cell Number
		                	// Do nothing
		                    break;
		                    
		                  case 5:
							// Process User Type (i.e. role)
							if(cell.isEmpty()){
								// Add empty issue
								result.add(
									RESTResponse.createCVSErrorMessage(
										lineNumber, 
										"user type should not be empty", 
										column + 1,
										"user type"));
								errorsFlag = true;
								// set the issue
		                		ResponseIssue issue = new ResponseIssue();
		                		issue.setLineNumber(String.valueOf(lineNumber));
		                		issue.setColumnNumber(String.valueOf(column + 1));
		                		issue.setColumnName("user type");
		                		issue.setIssue("user type should not be empty.");
		                		issue.setRawMessage(result.get(result.size()-1));
		                		issue.setSeverity(ResponseIssue.ISSUE_SEVERITY_FATAL_ERROR);
		                		errorData.getIssues().add(issue);
							}		                	
		                	  
		                	Iterator<Role> it = roles.iterator();
		                	boolean roleFoundFlag = false;
		                	while(it.hasNext()){
		                		Role tempRole = it.next();
		                		if(tempRole.getValue().toLowerCase().equals(cell.toLowerCase())){
		                			// found it
		                			roleFoundFlag = true;
		                			// exit loop
		                			break;
		                		}
		                	}
		                	if(!roleFoundFlag){
		                		// Add a not found issue
								result.add(
									RESTResponse.createCVSErrorMessage(
										lineNumber, 
										"user type: " + cell + " is invalid (does not exist)", 
										column + 1,
										"user type"));
								errorsFlag = true;
								// set the issue
		                		ResponseIssue issue = new ResponseIssue();
		                		issue.setLineNumber(String.valueOf(lineNumber));
		                		issue.setColumnNumber(String.valueOf(column + 1));
		                		issue.setColumnName("user type");
		                		issue.setIssue("user type: " + cell + " is invalid (does not exist)");
		                		issue.setRawMessage(result.get(result.size()-1));
		                		issue.setSeverity(ResponseIssue.ISSUE_SEVERITY_FATAL_ERROR);
		                		errorData.getIssues().add(issue);
		                	}
		                    break;
	
		                  default:
		                    // Nothing to do
	                } // end switch
	                  
	              } // for loop per row
	              lineNumber++;
	          } // for loop total
	          
	          
	    } catch (Exception e) { 
	        e.printStackTrace();
	    }
	    
	    //
	    // Process the results
	    if(errorsFlag) {
	    	return errorData;
	    }else{
	    	messageData.getMessages().add("There are no issues with the input file.");
	    	return messageData;
	    }

	}

	
	/**
	 * Get all the organizations on a global scope.
	 * 
	 * @return - a list of all the organizations; empty if none found
	 * 
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public List<User> getOrgAdminsForGroup(Group group) throws Exception{
		List<User> result=new ArrayList<User>();
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT ug.group_id, u.id AS user_id, u.name AS user_name, r.name FROM user_group as ug  "
                		+ "JOIN user as u ON ug.user_id = u.id "
                		+ "JOIN user_role as ur ON ur.user_id = u.id "
                		+ "JOIN role as r ON r.id = ur.role_id "
                		+ "WHERE ug.group_id = ? AND r.name = ?");

		// execute the statement 
		preparedSELECTstatement.setLong(1, group.getId());
		preparedSELECTstatement.setString(2, Role.ROLE_NAME_ORG_ADMIN);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractOrgAdminsFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
	}
	
	
	/**
	 * Get the credentials for this user in the data base
	 * This will return a list of all the tokens for this user sorted by the latest first
	 * There is no differentiation on invalidated tokens
	 * 
	 * @param username - the username that we want the credentials for
	 * @return - the list (possibly empty) of all the credentials for this user
	 * @throws PersistenceException
	 * 		- if there were any issues
	 */
	public List<Role> fetchUserRoles() throws PersistenceException {
		List<Role> roles = new ArrayList<Role>();
		
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		try {
			preparedSELECTstatement = conn
			        .prepareStatement("SELECT * from role");
			
			// execute the statement 
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        while (resultSet.next()) {
				// extract the data
	        	Role role = new Role();
	        	role.setId(resultSet.getLong("id"));
	        	role.setName(resultSet.getString("name"));
	        	role.setValue(resultSet.getString("value"));
        	
	        	//
	        	// add the data to the list
	        	roles.add(role);
	        	
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// release the connection
		closeConnection(conn);
		
		return roles;
	}
	
	
		
	
	/**************************************************************************************
	 * Private helper methods
	 */
	
	
	
	private boolean isEmailInUse(String emailAddress, Long ownerId) throws PersistenceException {
		boolean result = false;
		User user = null;
		PreparedStatement preparedSELECTStatement;
		ResultSet resultSet = null;
				
	
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		try {
			// create the statement
			preparedSELECTStatement = conn
	                .prepareStatement("SELECT * "
						+ "FROM user_contact "
	                	+ "WHERE LOWER(email_address) = LOWER(?");
			
			// insert the variable
			preparedSELECTStatement.setString(1, emailAddress);
			
			// execute the statement 
	        resultSet = preparedSELECTStatement.executeQuery();
			
	        while (resultSet.next()) {
				// extract the data
	        	result = true;
	        }
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
	}
		
	
	/**
	 * Simple conversion data routine from ResultSet to User list 
	 * @param resultSet - the input result set from an SQL Operation
	 * @return - the extracted User list data
	 * @throws Exception
	 * 		- if there were any issues in processing this request
	 */
	private List<User> extractUsersFromResult(ResultSet resultSet) throws Exception {
		List<User> result = new ArrayList<User>();
		organizationService.init();
		lookupService.init();
	
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			User user = new User();
			PasswordCredentials credentials = new PasswordCredentials();
			UserContact contact = new UserContact();
			Organization organization = new Organization(); 
			Group group = new Group();
			GroupType groupType = new GroupType();
			List<DocumentType> allowedDocTypes = new ArrayList<DocumentType>();
			List<Role> roles = new ArrayList<Role>();
			
			
			// extract the data
			//
			
			// extract main ids
			long userId = resultSet.getLong("id");
			long organizationId = resultSet.getLong("organization_id");
			long groupTypeId = resultSet.getLong("group_type_id");
			long groupId = resultSet.getLong("group_id");
					
			// extract all the other data
            String username = resultSet.getString("name");
            String email = resultSet.getString("email_address");
            String cellNumber = resultSet.getString("cell_number");
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");
            String nickName = resultSet.getString("nick_name");
            String lineId = resultSet.getString("line_id");
            boolean activated = resultSet.getBoolean("activated");
            boolean verified = resultSet.getBoolean("verified");
            String organizationName = resultSet.getString("organization_name");
            String organizationDescription = resultSet.getString("organization_description");
            String groupName = resultSet.getString("group_name");
            String groupTypeName = resultSet.getString("group_type");
            String groupTypeColorHexCode = resultSet.getString("group_type_color");
            int groupTypeOrderIndex = resultSet.getInt("order_index");
            String allowedDocTypeIds = resultSet.getString("allowed_doc_types");
            String associatedGroupsUnparsed = resultSet.getString("associated_groups");
            
            // pre-process any data
            // 
            String[] associatedGroupsParsed = new String[0]; 
            long[] associatedGroupIds = new long[0];
            if(associatedGroupsUnparsed != null && !associatedGroupsUnparsed.isEmpty()) {
            	associatedGroupsParsed = associatedGroupsUnparsed.split(",");
            	associatedGroupIds = new long[associatedGroupsParsed.length];
            	for(int i=0; i<associatedGroupsParsed.length; i++ ){
            		associatedGroupIds[i] = Long.parseLong(associatedGroupsParsed[i]);
            	}
            }
            
            // extract roles
            String roleName = resultSet.getString("role");
            long roleId = resultSet.getLong("role_id");
            
            
            //create the data elements
            //
            
            // Prep the user and other data with null fields
            user.setId(userId);
         	user.setCredentials(credentials);
         	user.setContactInfo(contact);
         	
         	// populate the User entity
            credentials.setUsername(username);
            contact.setEmailAddress(email);
            contact.setCellNumber(cellNumber);
            contact.setFirstName(firstName);
            contact.setLastName(lastName);
            contact.setNickName(nickName);
            contact.setLineId(lineId);
            contact.setActivated(activated);
            contact.setVerified(verified);
            
            
            user.setId(userId);
            user.setName(username);
            user.setCredentials(credentials);
            user.setContactInfo(contact);
            
            // role
            Role role = new Role();
            role.setName(roleName);
            role.setValue(roleName);
            role.setId(roleId);
            roles.add(role);
            user.setRoles(roles);
            
            // Populate group type
            groupType.setId(groupTypeId);
            groupType.setHexColorCode(groupTypeColorHexCode);
            groupType.setName(groupTypeName);
            groupType.setOrderIndex(groupTypeOrderIndex);
            groupType.setAssociatedStageIds(associatedGroupIds);
            
            // populate the allowed doc types for this user
            //
            if(allowedDocTypeIds != null && !allowedDocTypeIds.isEmpty()){
            	String[] ids = allowedDocTypeIds.split(",");
            	List<DocumentType> allDocTypes = lookupService.getAllDocumentTypes();
            	for(int i=0; i< ids.length; i++){
            		long docTypeId = Long.parseLong(ids[i]);
            		for(int j=0; j< allDocTypes.size(); j++){
	            		if(allDocTypes.get(j).getId() == docTypeId){
	            			allowedDocTypes.add(allDocTypes.get(j));
	            		}
            		}
            	}
            }
            
            //
            
            // populate the group
            group.setId(groupId);
            group.setName(groupName);
            group.setOrganizationId(organizationId);
            group.setGroupType(groupType);
            group.setAllowedDocTypes(allowedDocTypes);
            
            
            // populate the organization
            organization.setId(organizationId);
            organization.setDescription(organizationDescription);
            organization.setName(organizationName);
            
            //
            // Check the user entry if it already exists
            if(result.contains(user)){
            	// the user exists
            	int userIndex = result.indexOf(user);
            	user = result.get(userIndex);
            }else{
            	// add the user to the list
                result.add(user);
            }
            //
        	// check if the organization has already been set
        	if(user.getUserOrganizations().contains(organization)){
        		//
        		// check if it contains the group
        		int organizationIndex = user.getUserOrganizations().indexOf(organization);
        		organization = user.getUserOrganizations().get(organizationIndex);
        	}else{
        		// add the organization
        		user.getUserOrganizations().add(organization);
        		// add the flat setting for the groups in this organization
        		organization.setSubGroups(organizationService.getAllGroupsByOrgId(organization.getId()));
        		
        	}
        	
    		// 
    		// check if it contains the group
    		if(user.getUserGroups().contains(group)){
    			// do nothing it is already contained
    		}else{
    			// add the group
    			user.getUserGroups().add(group);
    		}
        }
		
		return result;
	}
	
	
	
	/**
	 * Simple conversion data routine from ResultSet to User list 
	 * @param resultSet - the input result set from an SQL Operation
	 * @return - the extracted User list data
	 * @throws Exception
	 * 		- if there were any issues in processing this request
	 */
	private List<User> extractLightUsersFromResult(ResultSet resultSet) throws Exception {
		List<User> result = new ArrayList<User>();
		organizationService.init();
		lookupService.init();
	
		// process the extractions - there will only be one user
		//
		
		//
		// get the languages for the user
		
		while (resultSet.next()) {
			User user = new User();
			PasswordCredentials credentials = new PasswordCredentials();
			UserContact contact = new UserContact();
			Organization organization = new Organization(); 
			Group group = new Group();
			GroupType groupType = new GroupType();
			List<DocumentType> allowedDocTypes = new ArrayList<DocumentType>();
			List<Role> roles = new ArrayList<Role>();
			
			
			// extract the data
			//
			
			// extract main ids
			long userId = resultSet.getLong("id");
			long organizationId = resultSet.getLong("organization_id");
					
			// extract all the other data
            String username = resultSet.getString("name");
            String email = resultSet.getString("email_address");
            String cellNumber = resultSet.getString("cell_number");
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");
            boolean activated = resultSet.getBoolean("activated");
            boolean verified = resultSet.getBoolean("verified");
            
            String nickName = resultSet.getString("nick_name");
            String lineId = resultSet.getString("line_id");
            String organizationName = resultSet.getString("organization_name");
            String organizationDescription = resultSet.getString("organization_description");
            
            // extract roles
            String roleName = resultSet.getString("role");
            long roleId = resultSet.getLong("role_id");
            
            
            //create the data elements
            //
            
            // Prep the user and other data with null fields
            user.setId(userId);
         	user.setCredentials(credentials);
         	user.setContactInfo(contact);
         	
         	// populate the User entity
            credentials.setUsername(username);
            contact.setEmailAddress(email);
            contact.setCellNumber(cellNumber);
            contact.setFirstName(firstName);
            contact.setLastName(lastName);
            contact.setNickName(nickName);
            contact.setLineId(lineId);
            contact.setActivated(activated);
            contact.setVerified(verified);

            user.setId(userId);
            user.setName(username);
            user.setCredentials(credentials);
            user.setContactInfo(contact);
            
            // role
            Role role = new Role();
            role.setName(roleName);
            role.setValue(roleName);
            role.setId(roleId);
            roles.add(role);
            user.setRoles(roles);
            
           
            //
            
            // populate the group
            group.setOrganizationId(organizationId);
            group.setGroupType(groupType);
            group.setAllowedDocTypes(allowedDocTypes);
            
            
            // populate the organization
            organization.setId(organizationId);
            organization.setDescription(organizationDescription);
            organization.setName(organizationName);
            
            //
            // Check the user entry if it already exists
            if(result.contains(user)){
            	// the user exists
            	int userIndex = result.indexOf(user);
            	user = result.get(userIndex);
            }else{
            	// add the user to the list
                result.add(user);
            }
            //
        	// check if the organization has already been set
        	if(user.getUserOrganizations().contains(organization)){
        		//
        		// check if it contains the group
        		int organizationIndex = user.getUserOrganizations().indexOf(organization);
        		organization = user.getUserOrganizations().get(organizationIndex);
        	}else{
        		// add the organization
        		user.getUserOrganizations().add(organization);
        		// add the flat setting for the groups in this organization
        		organization.setSubGroups(organizationService.getAllGroupsByOrgId(organization.getId()));
        		
        	}
        	
    		// 
    		// check if it contains the group
    		if(user.getUserGroups().contains(group)){
    			// do nothing it is already contained
    		}else{
    			// add the group
    			user.getUserGroups().add(group);
    		}
        }
		
		return result;
	}
	

	/**
	 * Simple conversion data routine from ResultSet to User list 
	 * @param resultSet - the input result set from amn SQL Operation
	 * @return - the extracted User list data
	 * @throws Exception
	 * 		- if there were any issues in processing this request
	 */
	private List<User> extractSparseUsersFromResult(ResultSet resultSet) throws Exception {
		List<User> result = new ArrayList<User>();
		organizationService.init();
		lookupService.init();
	
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			User user = new User();
			PasswordCredentials credentials = new PasswordCredentials();
			UserContact contact = new UserContact();
			
			
			// extract the data
			//
			
			// extract main ids
			long userId = resultSet.getLong("id");
					
			// extract all the other data
            String username = resultSet.getString("name");
            String email = resultSet.getString("email_address");
            String cellNumber = resultSet.getString("cell_number");
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");
            boolean activated = resultSet.getBoolean("activated");
            boolean verified = resultSet.getBoolean("verified");
            long contactId = resultSet.getLong("contact_id");
            
            //create the data elements
            //
            
            // Prep the user and other data with null fields
            user.setId(userId);
         	user.setCredentials(credentials);
         	user.setContactInfo(contact);
         	
         	// populate the User entity
            credentials.setUsername(username);
            contact.setEmailAddress(email);
            contact.setCellNumber(cellNumber);
            contact.setFirstName(firstName);
            contact.setLastName(lastName);
            contact.setId(contactId);
            contact.setActivated(activated);
            contact.setVerified(verified);

            user.setId(userId);
            user.setName(username);
            user.setCredentials(credentials);
            user.setContactInfo(contact);
            
            
            //
            // Check the user entry if it already exists
            if(result.contains(user)){
            	// the user exists
            	int userIndex = result.indexOf(user);
            	user = result.get(userIndex);
            }else{
            	// add the user to the list
                result.add(user);
            }
        }
		return result;
	}
	
	/**
	 * Extract the Organization data from the SQL based result set
	 * 
	 * @param resultSet - the input representation of the records fetched through the initial SQL
	 * @return
	 * @throws SQLException
	 * 			- if there were any issues with the request
	 */
	private List<User> extractOrgAdminsFromResult(ResultSet resultSet) throws SQLException {
		List<User> result = new ArrayList<User>();
		

		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			User user = null;
			
			// extract the data
			long id = resultSet.getLong("user_id");
            
            // populate the User entity
			try {
				user = getUserByUserId(id);
			} catch (PersistenceException | IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            // add the user to the list
			if(user != null) {
				result.add(user);
			}
        }
		return result;
	}
	

}
