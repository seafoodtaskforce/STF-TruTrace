package com.wwf.shrimp.application.services.main.dao.impl.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.mysql.cj.api.jdbc.Statement;
import com.wwf.shrimp.application.exceptions.ConfigurationException;
import com.wwf.shrimp.application.exceptions.PersistenceException;
import com.wwf.shrimp.application.exceptions.ServiceManagementException;
import com.wwf.shrimp.application.models.AuditEntity;
import com.wwf.shrimp.application.models.Document;
import com.wwf.shrimp.application.models.LookupEntity;
import com.wwf.shrimp.application.models.TagData;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.search.AuditSearchCriteria;
import com.wwf.shrimp.application.models.search.DocumentSearchCriteria;
import com.wwf.shrimp.application.models.search.TagSearchCriteria;
import com.wwf.shrimp.application.models.search.UserSearchCriteria;

/**
 * The persistence implementation for TagData entities based on the MySQL database
 * @author AleaActaEst
 *
 * @param <T> - the operational entity that this DAO will work with.
 * @param <S> - The specific search criteria for entity <T>
 */
public class TagMySQLDao<T, S> extends BaseMySQLDao<TagData, TagSearchCriteria>{
	
	private static final String CUSTOM_TAG_PREFIX = "CUSTOM: ";
	private UserMySQLDao<User, UserSearchCriteria> userService = new UserMySQLDao<User, UserSearchCriteria>();
		
	/**
	 * create a new tag in the system
	 * 
	 * @param newTag - the new entity
	 * @return - the tag entity with a unique id embedded
	 * @throws PersistenceException if there was an issue with persistence
	 * @throws IllegalArgumentException if the tag instance is invalid
	 */
	public TagData create(TagData newTag) throws PersistenceException, IllegalArgumentException { 
		PreparedStatement preparedINSERTstatement = null;
		long returnId=0;
		String insertQuery = null;
		
		// get the connection
		Connection conn = openConnection();
		// set the defaults
		if(newTag.getCustomPrefix() == null || newTag.getCustomPrefix().isEmpty()){
			newTag.setCustomPrefix("OTHER");
		}
		// check if exists for both CUSTOM prefix and without
		try {
			if(doesTagExist(newTag)){
				getLog().info("TAG Creation: <duplicate> " + newTag.getText());
				return null;
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(newTag.isCustom()){
			newTag.setText(CUSTOM_TAG_PREFIX + newTag.getText());
		}
		// check if exists for both CUSTOM prefix and without
		try {
			if(doesTagExist(newTag)){
				getLog().info("TAG Creation: <duplicate> " + newTag.getText());
				return null;
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// create the statement
		try {		
	
			// create the query
			if(newTag.getOrganizationId() == 0){
				insertQuery = " insert into tag_data ("
						+ "tag_text, "
						+ "custom, "
						+ "owner "
						+ ")"
				        + " values (?, ?, ?)";
				
				preparedINSERTstatement = conn
				        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
				
				// execute the statement 
				preparedINSERTstatement.setString(1, newTag.getText());
				preparedINSERTstatement.setBoolean(2, newTag.isCustom());
				preparedINSERTstatement.setString(3, newTag.getOwner());
				
			}
			if(!newTag.getCustomPrefix().isEmpty() && newTag.getOrganizationId() != 0){
				insertQuery = " insert into tag_data ("
						+ "tag_text, "
						+ "custom_tag_prefix, "
						+ "organization_id, "	
						+ "custom, "
						+ "owner "
						+ ")"
				        + " values (?, ? , ?, ?, ?)";
				
				preparedINSERTstatement = conn
				        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
				
				// execute the statement 
				preparedINSERTstatement.setString(1, newTag.getText());
				preparedINSERTstatement.setString(2, newTag.getCustomPrefix());
				preparedINSERTstatement.setLong(3, newTag.getOrganizationId());
				preparedINSERTstatement.setBoolean(4, newTag.isCustom());
				preparedINSERTstatement.setString(5, newTag.getOwner());
			}
			preparedINSERTstatement.executeUpdate();
			
			
			
			
			// get the id of the created 
			ResultSet rs = preparedINSERTstatement.getGeneratedKeys();
			if (rs.next()){
				returnId=rs.getLong(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// release the connection
		closeConnection(conn);
		
		// return the result
		newTag.setId(returnId);
		if(newTag.isCustom()){
			newTag.setText(newTag.getCustomPrefix() + " " + newTag.getText());
		}
		getLog().info("CREATE TAG: " + newTag);
		
		return newTag;
	}
	
	/**
	 * Attach the provided tags in a batch mode
	 * 
	 * @param tags - the list of 0 or more tags to attach to the given document
	 * @param docId - the id of the doc to attach the tag(s) to
	 * @throws Exception - the exception to throw if:
	 * 		- there was an issue with attachment
	 */
	public void attach(List<TagData> tags, long docId) throws Exception { 
		
		// 
		// process the request
				
		// delete previous tags
		deleteAttachedTags(docId);
		
		// create the new tags
		for(int i=0; i< tags.size(); i++){
			if(tags.get(i).getId() == 0){
				// First try to find the tag by its text
				TagData tempTag = getTagFromName(tags.get(i).getText(), tags.get(i).getCustomPrefix(), tags.get(i).getId());
				if(tempTag != null){
					tags.set(i, tempTag);
				}else{
					tags.get(i).setId(create(tags.get(i)).getId());
				}
				
			}
			createDocumentTag(docId, tags.get(i).getId());
			
		}
		getLog().info("TAG Attachements for: " + docId);
		
	}
	
	/**
	 * Get the list of all available tags.
	 * 
	 * @return - the list of tags
	 * @throws Exception - the exception thrown if
	 * 			- there were any issues with fetching the requested data
	 */
	public List<TagData> getAllTags(String userName) throws Exception{
		List<TagData> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		long organizationId = 1;
		User user = null;
		
		//
		// Initialize services
		userService.init();
		
		
		
		// look up the user by name which should be unique
		//
		if(userName != null && !userName.isEmpty()){
			user = userService.getUserByName(userName);
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
		preparedSELECTstatement = conn
                .prepareStatement("SELECT * from tag_data WHERE "
                				+ "organization_id = ?");
		
		//
		// Prepare the query
		preparedSELECTstatement.setLong(1, organizationId);
		
		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractTagsFromResult(resultSet);

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result;
				
	}
	
	/**
	 * Test if the tag exists in the system
	 * 
	 * @param tag - the tag to look up
	 * @return - true if the tag exists and false if it does not
	 * @throws Exception
	 * 			- if there were any issues with executing this request
	 */
	public boolean doesTagExist(TagData tag) throws Exception {
		boolean result = false;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT * from tag_data " 
					+ "WHERE tag_text = ? "
					+ "AND custom_tag_prefix = ? "
					+ "AND organization_id = ? "
					+ "AND owner = ?"
                );
		
		// execute the statement 
		preparedSELECTstatement.setString(1, tag.getText());
		preparedSELECTstatement.setString(2, tag.getCustomPrefix());
		preparedSELECTstatement.setLong(3, tag.getOrganizationId());
		preparedSELECTstatement.setString(4, tag.getOwner());
		
		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        while(resultSet.next()){
        	result = true;
        }

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result;
				
	}
	
	/**
	 * Check if the tag exists
	 * 
	 * @param tagId - the id to check
	 * @return - true if it exists; false otherwise
	 * @throws Exception
	 */
	public boolean doesTagById(long tagId) throws Exception {
		boolean result = false;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT * from tag_data " 
					+ "WHERE id = ? "
                );
		
		// execute the statement 
		preparedSELECTstatement.setLong(1, tagId);
		
		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        while(resultSet.next()){
        	result = true;
        }

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result;
				
	}
	
	/**
	 * Get all the tags for a given document id
	 * 
	 * @param docId - the doc id to fetch the tags for
	 * @return - a list of tags or empty list if not found
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public List<TagData> getAllTagsByDocId(long docId) throws Exception{
		List<TagData> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT tag_data.id, tag_data.tag_text, tag_data.custom_tag_prefix, tag_data.organization_id, tag_data.custom, tag_data.owner "
                		+ "FROM tag_data " 
                		+ "INNER JOIN document_tag_data ON tag_data.id=document_tag_data.tag_data_id "
                		+ "WHERE document_tag_data.document_id = ? ");
		// execute the statement 
		preparedSELECTstatement.setLong(1, docId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractDocumentTagsFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
				
	}
	
	/**
	 * 
	 * @param docSyncId
	 * @return
	 * @throws Exception
	 */
	public List<TagData> getAllTagsByDocSyncId(String docSyncId) throws Exception{
		List<TagData> result=null;
		
		long docId = getDocIdBySyncId(docSyncId);
		result = getAllTagsByDocId(docId);
	
		// return the result
		return result;
	}
	
	
	/**
	 * Gets the document id for the document sync id provided.
	 * 
	 * @param syncId -  the sync id of the document
	 * @return - the id of the document for this sync id; 0 if not found
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public long getDocIdBySyncId(String syncId) throws Exception {
		long documentId=0;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT id from document WHERE sync_id=?");
		preparedSELECTstatement.setString(1, syncId);
		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        while (resultSet.next()) {
			// extract the data
        	documentId = resultSet.getLong("id");
        }

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return documentId;
	}
	
	/**
	 * Delete any of the attached tags form this document.
	 * NOTE: this only removes the associations it does not delete the actual tags.
	 * 
	 * @param docId - the document id to detach the tags from
	 * @throws ServiceManagementException
	 * 			- if there were any issues with the request
	 */
	public void deleteAttachedTags(long docId) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		
		// create the query
		String deleteQuery = " DELETE FROM document_tag_data WHERE document_id = ?";
		
		// create the statement
		try {
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			
			// execute the statement 
			preparedDELETEStatement.setLong(1, docId);
			preparedDELETEStatement.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting tags for document with id=" + docId, e);
		}

		// release the connection
		closeConnection(conn);
	}
	
	/**
	 * fetch all the custom tags prefix list
	 * 
	 * @return - the list of prefixes or empty if none were found
	 * @throws ServiceManagementException
	 * 			- if there were any issues with the request
	 */
	public List<LookupEntity> getTagCustomPrefixList() throws ServiceManagementException{
		List<LookupEntity> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		// create the statement
		try {		
			// create the statement
			preparedSELECTstatement = conn
	                .prepareStatement("SELECT * from tag_prefix_lu");
			
			// execute the statement 
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        //
	        // process the result
	        result = extractTagCustomPrefixesFromResult(resultSet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while fetching lookup values for Tag Prefoxes", e);
		}
		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result;
	}
	
	/**
	 * Fetch the tag 
	 * 
	 * @return - the list of prefixes or empty if none were found
	 * @throws ServiceManagementException
	 * 			- if there were any issues with the request
	 */
	public TagData getTagFromName(String name, String prefix, long orgId) throws ServiceManagementException{
		TagData result = null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// pre-process
		if(name.contains(prefix)){
			name = name.replace(prefix, "");
		}
		if(!name.contains(CUSTOM_TAG_PREFIX)){
			name = CUSTOM_TAG_PREFIX + name;
		}
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		// create the statement
		try {		
			// create the statement
			preparedSELECTstatement = conn
	                .prepareStatement("SELECT * from tag_data "
	                		+ " WHERE tag_text = ? AND custom_tag_prefix = ? AND organization_id = ?");
			
			// execute the statement 
			preparedSELECTstatement.setString(1, name);
			preparedSELECTstatement.setString(2, prefix);
			preparedSELECTstatement.setLong(3, orgId);
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        //
	        // process the result
	        List<TagData> tags = extractTagsFromResult(resultSet);
	        if(tags!= null && tags.size() > 0){
	        	result = tags.get(0);
	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while fetching Tag by name", e);
		}
		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result;
	}
	
	
	
	/**********************************************************************************
	 * Private helper methods
	 */
	
	/**
	 * Helper method to extract tags
	 * 
	 * @param resultSet - the SQL result Set to extract data from
	 * @return - the list of extracted tags or empty of none were found
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	private List<TagData> extractTagsFromResult(ResultSet resultSet) throws Exception {
		List<TagData> tags= new ArrayList<TagData>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            String tagText = resultSet.getString("tag_text");
            String tagCustomPrefix = resultSet.getString("custom_tag_prefix");
            long tagOrganizationId = resultSet.getLong("organization_id");
            boolean isCustom = resultSet.getBoolean("custom");
            String owner = resultSet.getString("owner");
            
            
            // Create a new Document
            TagData tag = new TagData();
            
            
            // populate the Document entity
            tag.setId(id);
            if(isCustom){
            	tag.setText(tagCustomPrefix + " " + tagText);
            }else{
            	tag.setText(tagText);
            }
            tag.setCustomPrefix(tagCustomPrefix);
            tag.setOrganizationId(tagOrganizationId);
            tag.setCustom(isCustom);
            tag.setOwner(owner);


            tags.add(tag);
        }
		
		return tags;
	}
	
	/**
	 * Helper method to extract custom prefixes for tags
	 * 
	 * @param resultSet - the result set for a given SQL query
	 * @return - the list of mapped look up values for the prefixes
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	private List<LookupEntity> extractTagCustomPrefixesFromResult(ResultSet resultSet) throws Exception {
		List<LookupEntity> tagPrefixes= new ArrayList<LookupEntity>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            String name = resultSet.getString("name");
            String value = resultSet.getString("value");
           
            
            // Create a new Document
            LookupEntity tagPrefix = new LookupEntity();
            
            
            // populate the Document entity
            tagPrefix.setId(id);
            tagPrefix.setValue(value);
            tagPrefix.setName(name);


            tagPrefixes.add(tagPrefix);
        }
		
		return tagPrefixes;
	}
	
	/**
	 * Helper method to extract Tag data from Result data
	 * 
	 * @param resultSet - the result set for a given SQL query
	 * @return - the extracted and mapped Tag data as a list or empty if no data exists
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	private List<TagData> extractDocumentTagsFromResult(ResultSet resultSet) throws Exception {
		List<TagData> tags= new ArrayList<TagData>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            String tagText = resultSet.getString("tag_text");
            String tagCustomPrefix = resultSet.getString("custom_tag_prefix");
            long tagOrganizationId = resultSet.getLong("organization_id");
            boolean isCustom = resultSet.getBoolean("custom");
            String owner = resultSet.getString("owner");
            
            // Create a new Document
            TagData tag = new TagData();
            
            
            // populate the Document entity
            // populate the Document entity
            tag.setId(id);
            if(isCustom){
            	tag.setText(tagCustomPrefix + " " + tagText);
            }else{
            	tag.setText(tagText);
            }
            tag.setCustomPrefix(tagCustomPrefix);
            tag.setOrganizationId(tagOrganizationId);
            tag.setCustom(isCustom);
            tag.setOwner(owner);


            tags.add(tag);
        }
		
		return tags;
	}
	
	/**
	 * Helper method used to create a tag to document association.
	 * 
	 * @param documentID -  the document to tag
	 * @param tagId - the specific tag to use
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	private void createDocumentTag(long documentID, long tagId) throws Exception {
		PreparedStatement preparedINSERTstatement;
		
		//
		// Initialize services
		userService.init();
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request

		// create the query
		String insertQuery = " insert into document_tag_data ("
				+ "document_id, "
				+ "tag_data_id"
				+ ")"
		        + " values (?, ?)";
		
		// create the statement
		preparedINSERTstatement = conn
                .prepareStatement(insertQuery);
		// execute the statement 
		preparedINSERTstatement.setLong(1, documentID);
		preparedINSERTstatement.setLong(2, tagId);
		preparedINSERTstatement.executeUpdate();
		
		// release the connection
		closeConnection(conn);
		
		
		
		getLog().info("Attached a Tag to Document: " + documentID + " " + tagId);
		
	}
	


}
