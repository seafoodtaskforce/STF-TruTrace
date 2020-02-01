package com.wwf.shrimp.application.services.main.dao.impl.mysql;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import com.mysql.cj.api.jdbc.Statement;
import com.wwf.shrimp.application.exceptions.EntityNotFoundException;
import com.wwf.shrimp.application.exceptions.PersistenceException;
import com.wwf.shrimp.application.models.DocumentType;
import com.wwf.shrimp.application.models.Group;
import com.wwf.shrimp.application.models.GroupType;
import com.wwf.shrimp.application.models.LookupEntity;
import com.wwf.shrimp.application.models.Organization;
import com.wwf.shrimp.application.models.PasswordCredentials;
import com.wwf.shrimp.application.models.Role;
import com.wwf.shrimp.application.models.SecurityToken;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.UserContact;
import com.wwf.shrimp.application.models.UserCredentials;
import com.wwf.shrimp.application.models.search.LookupDataSearchCriteria;
import com.wwf.shrimp.application.models.search.OrganizationSearchCriteria;
import com.wwf.shrimp.application.models.search.UserSearchCriteria;
import com.wwf.shrimp.application.utils.DateUtility;

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
		long returnId=0;
		
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
			String userName =  entity.getCredentials().getUsername();
			String password = "123";
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
		
		return entity;
		
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
				preparedUPDATEStatement.setString(1, entity.getName());
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
		
		getLog().info("UPDATED User Data for user " + entity.getName());
		
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
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		try {
			preparedSELECTstatement = conn
			        .prepareStatement("SELECT a.id, a.name, "
									+ "b.email_address, b.cell_number, b.first_name, b.last_name, b.nick_name, b.line_id, "
									+ "d.name AS organization_name, d.description As organization_description, d.id AS organization_id, "
									+ "e.group_id, gadr.doc_type_ids AS allowed_doc_types, "
									+ "g.name AS group_name, h.id AS group_type_id, h.name AS group_type, h.color_hex_code AS group_type_color, "
									+ " h.order_index, j.name as role, j.id as role_id  "
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
			        				+ "WHERE organization_id=? ");
			
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
									+ "b.email_address, b.cell_number, b.first_name, b.last_name, b.nick_name, b.line_id, "
									+ "d.name AS organization_name, d.description As organization_description, d.id AS organization_id, "
									+ "e.group_id, gadr.doc_type_ids AS allowed_doc_types, "
									+ "g.name AS group_name, h.id AS group_type_id, h.name AS group_type, h.color_hex_code AS group_type_color, "
									+ " h.order_index, j.name as role, j.id as role_id "
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
			        				+ "b.email_address, b.cell_number, b.first_name, b.last_name, b.id AS contact_id "
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
						+ "b.email_address, b.cell_number, b.first_name, b.last_name, b.nick_name, b.line_id, "
						+ "d.name AS organization_name, d.description As organization_description, d.id AS organization_id, "
						+ "e.group_id, gadr.doc_type_ids AS allowed_doc_types, "
						+ "g.name AS group_name, h.id AS group_type_id, h.name AS group_type, h.color_hex_code AS group_type_color, "
						+ " h.order_index, j.name as role, j.id as role_id   "
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
	        
	        //
	        // process the result
	        List<User> users = extractUsersFromResult(resultSet);
	        if(users.size() > 0){
	        	result = users.get(0);
	        }else{
	        	// 
	        	// check for a super admin
	        	preparedSELECTstatement = conn
	    				.prepareStatement("SELECT a.id, a.name, "
	    						+ "b.email_address, b.cell_number, b.first_name, b.last_name, b.nick_name, b.line_id, "
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
	    	        users = extractLightUsersFromResult(resultSet);
	    	        if(users.size() > 0){
	    	        	result = users.get(0);
	    	        }
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
						+ "b.email_address, b.cell_number, b.first_name, b.last_name, b.nick_name, b.line_id, "
						+ "d.name AS organization_name, d.description As organization_description, d.id AS organization_id, "
						+ "e.group_id, gadr.doc_type_ids AS allowed_doc_types, "
						+ "g.name AS group_name, h.id AS group_type_id, h.name AS group_type, h.color_hex_code AS group_type_color, "
						+ " h.order_index, j.name as role, j.id as role_id  "
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
				+ "user_id "
				+ ")"
		        + " values (?,?,?,?,?)";
		
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

			// execute the statement 
			preparedINSERTstatement.setString(1, emailAddress);
			preparedINSERTstatement.setString(2,cellNumber);
			preparedINSERTstatement.setString(3,firstName);
			preparedINSERTstatement.setString(4,lastName);
			preparedINSERTstatement.setLong(5,user.getId());
			
			
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
				+ "line_id = ? "
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
			preparedUPDATEStatement.setLong(7, user.getId());
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
	
	
	
	/**************************************************************************************
	 * Private helper methods
	 */
		
	
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
            String organizationName = resultSet.getString("organization_name");
            String organizationDescription = resultSet.getString("organization_description");
            String groupName = resultSet.getString("group_name");
            String groupTypeName = resultSet.getString("group_type");
            String groupTypeColorHexCode = resultSet.getString("group_type_color");
            int groupTypeOrderIndex = resultSet.getInt("order_index");
            String allowedDocTypeIds = resultSet.getString("allowed_doc_types");
            
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

}
