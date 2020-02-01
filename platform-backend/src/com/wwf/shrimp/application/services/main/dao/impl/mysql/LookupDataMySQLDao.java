package com.wwf.shrimp.application.services.main.dao.impl.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.cj.api.jdbc.Statement;
import com.wwf.shrimp.application.models.AppResource;
import com.wwf.shrimp.application.models.DocumentType;
import com.wwf.shrimp.application.models.LookupEntity;
import com.wwf.shrimp.application.models.search.LookupDataSearchCriteria;

/**
 * Lookup data DAO implementation
 * 
 * @author argolite
 *
 * @param <T> - the operational entity that this DAO will work with.
 * @param <S> - The specific search criteria for entity <T>
 */
public class LookupDataMySQLDao<T, S> extends BaseMySQLDao<LookupEntity, LookupDataSearchCriteria> {

	/**
	 * Get all the document types.
	 * 
	 * @return - the list of all available document types; empty if none found
	 * @throws Exception - if there were any issues
	 */
	public List<DocumentType> getAllDocumentTypes() throws Exception{
		List<DocumentType> result=null;
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
                .prepareStatement("SELECT id, name, value, color_hex_code, document_designation from document_type");
		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractDocumentTypesFromResult(resultSet);

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result;
				
	}
	
	
	/**
	 * 
	 * @param orgId - the organization id
	 * @return - the list of languages as an 
	 * @throws Exception
	 */
	public List<LookupEntity> getAllAppLanguagesForOrg(long orgId)throws Exception{
		List<LookupEntity> result=null;
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
                .prepareStatement("SELECT A.id, A.name, A.value FROM wwf_shrimp_database_v2.supported_languages A "
                		+"JOIN supported_languages_org_rel B ON A.id = B.language_id "
                		+ "WHERE B.org_id = ?");
		
		// execute the statement 
		preparedSELECTstatement.setLong(1, orgId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractLanguagesFromResult(resultSet);

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result;
				
	}
	
	/**
	 * Create a new Resource
	 * 
	 * @return - the list of all available document types; empty if none found
	 * @throws Exception - if there were any issues
	 */
	public AppResource createAppResource(AppResource appResource) throws Exception{
		AppResource result=null;
		PreparedStatement preparedINSERTstatement;
		long returnId=0;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request

		// create the query
		String insertQuery = " insert into app_resources ("
				+ "resource_key, "
				+ "value, "
				+ "locale, "
				+ "type, "
				+ "sub_type, "
				+ "platform "
				+ ")"
		        + " values (?, ?, ? ,? ,? ,?)";
		// prepare the statement 
		preparedINSERTstatement = conn
                .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
		// initialize data 
		preparedINSERTstatement.setString(1, appResource.getKey());
		preparedINSERTstatement.setString(2, appResource.getValue());
		preparedINSERTstatement.setString(3, appResource.getLocale());
		preparedINSERTstatement.setString(4, appResource.getType());
		preparedINSERTstatement.setString(5, appResource.getSubType());
		preparedINSERTstatement.setString(6, appResource.getPlatform());
		// execute the statement
		preparedINSERTstatement.executeUpdate();
        
        //
        // process the result
		// get the id of the created 
		ResultSet rs = preparedINSERTstatement.getGeneratedKeys();
		if (rs.next()){
			returnId=rs.getLong(1);
		}
		
		// release the connection
		closeConnection(conn);
		
		getLog().info("Create a new App Resource: " + appResource);
		
				
		// return the result
		// return the result
		appResource.setId(returnId);
		getLog().info("CREATE App Resource: " + appResource);
		return result;
		
	}


	/**
	 * Update an existing resource
	 * 
	 * @return - the list of all available document types; empty if none found
	 * @throws Exception - if there were any issues
	 */
	public AppResource updateAppResource(AppResource appResource) throws Exception{
		AppResource result=null;
		PreparedStatement preparedUPDATEStatement;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request

		// create the query
		String updateQuery = " UPDATE app_resources "
				+ "SET value = ? "
				+ "WHERE resource_key = ? AND locale = ?";
		// prepare the statement 
		preparedUPDATEStatement = conn
                .prepareStatement(updateQuery);
		// initialize data 
		preparedUPDATEStatement.setString(1, appResource.getValue());
		preparedUPDATEStatement.setString(2, appResource.getKey());
		preparedUPDATEStatement.setString(3, appResource.getLocale());
		// execute the statement
		preparedUPDATEStatement.executeUpdate();
        
        //
        // process the result
		// get the id of the created 
		
		// release the connection
		closeConnection(conn);
		
		getLog().info("UPDATE an App Resource: " + appResource);
		
				
		// return the result

		getLog().info("UPDATE App Resource: " + appResource);
		return result;
				
	}



	/**
	 * Delete an existing resource
	 * 
	 * @return - no return
	 * @throws Exception - if there were any issues
	 */
	public void deleteAppResource(AppResource appResource) throws Exception{
		PreparedStatement preparedDELETEStatement;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request

		// create the query
		String deleteQuery = " DELETE FROM app_resources "
				+ "WHERE resource_key = ? AND locale = ?";
		// prepare the statement 
		preparedDELETEStatement = conn
                .prepareStatement(deleteQuery);
		// initialize data 
		preparedDELETEStatement.setString(1, appResource.getKey());
		preparedDELETEStatement.setString(2, appResource.getLocale());
		// execute the statement
		preparedDELETEStatement.execute();
        
        //
        // process the result
		// get the id of the created 
		
		// release the connection
		closeConnection(conn);
		
		getLog().info("DELETE an App Resource: " + appResource);
		
				
		// return the result

		getLog().info("DELETE App Resource: " + appResource);
				
	}
	
	/**
	 * Delete an existing resource
	 * 
	 * @return - no return
	 * @throws Exception - if there were any issues
	 */
	public void deleteAppResource(long resourceId) throws Exception{
		PreparedStatement preparedDELETEStatement;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request

		// create the query
		String deleteQuery = " DELETE FROM app_resources "
				+ "WHERE id = ?";
		// prepare the statement 
		preparedDELETEStatement = conn
                .prepareStatement(deleteQuery);
		// initialize data 
		preparedDELETEStatement.setLong(1, resourceId);
		// execute the statement
		preparedDELETEStatement.execute();
        
        //
        // process the result
		// get the id of the created 
		
		// release the connection
		closeConnection(conn);
		
		getLog().info("DELETE an App Resource with id: " + resourceId);
				
	}


	
	/**
	 * Get all the document types.
	 * 
	 * @return - the list of all available document types; empty if none found
	 * @throws Exception - if there were any issues
	 */
	public List<AppResource> getAllAppResources() throws Exception{
		List<AppResource> result=null;
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
                .prepareStatement("SELECT * from app_resources");
		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractAppResourcesFromResult(resultSet);

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result;
				
	}
	

	
	/**
	 * Get all the document types.
	 * 
	 * @return - the list of all available document types; empty if none found
	 * @throws Exception - if there were any issues
	 */
	public boolean doesResourceExist(AppResource appResource) throws Exception{
		boolean result = true;
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
                .prepareStatement("SELECT * from app_resources WHERE resource_key = ? AND locale = ?");
		
		preparedSELECTstatement.setString(1, appResource.getKey());
		preparedSELECTstatement.setString(2, appResource.getLocale());
		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        if(extractAppResourcesFromResult(resultSet).isEmpty()){
        	result = false;
        };

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result;
				
	}
	
	
	
	
	/**
	 * Get all the document types form the result set.
	 * 
	 * @param resultSet - the set to extract from
	 * @return - the extracted and converted data
	 * @throws SQLException - if there were any issues
	 */
	private List<DocumentType> extractDocumentTypesFromResult(ResultSet resultSet) throws SQLException {
		List<DocumentType> documentTypes = new ArrayList<DocumentType>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            String name = resultSet.getString("name");
            String value = resultSet.getString("value");
            String typeColorCode = resultSet.getString("color_hex_code");
            String docDesignation = resultSet.getString("document_designation");
      
            
            // Create a new Document
            DocumentType documentType = new DocumentType();
            
            // populate the entity
            documentType.setId(id);
            documentType.setName(name);
            documentType.setValue(value);
            documentType.setHexColorCode(typeColorCode);
            documentType.setDocumentDesignation(docDesignation);

            documentTypes.add(documentType);
        }
		
		return documentTypes;
	}
	
	
	
	/**
	 * Get all the document types form the result set.
	 * 
	 * @param resultSet - the set to extract from
	 * @return - the extracted and converted data
	 * @throws SQLException - if there were any issues
	 */
	private List<LookupEntity> extractLanguagesFromResult(ResultSet resultSet) throws SQLException {
		List<LookupEntity> languageLookupEntities = new ArrayList<LookupEntity>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            String name = resultSet.getString("name");
            String value = resultSet.getString("value");
      
            
            // Create a new Document
            LookupEntity languageData = new LookupEntity();
            
            // populate the entity
            languageData.setId(id);
            languageData.setName(name);
            languageData.setValue(value);

            languageLookupEntities.add(languageData);
        }
		
		return languageLookupEntities;
	}
	
	/**
	 * Get all the document types form the result set.
	 * 
	 * @param resultSet - the set to extract from
	 * @return - the extracted and converted data
	 * @throws SQLException - if there were any issues
	 */
	private List<AppResource> extractAppResourcesFromResult(ResultSet resultSet) throws SQLException {
		List<AppResource> appResources = new ArrayList<AppResource>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            String locale = resultSet.getString("locale");
            String value = resultSet.getString("value");
            String key = resultSet.getString("resource_key");
            String type = resultSet.getString("type");
            String subType = resultSet.getString("sub_type");
      
            
            // Create a new app resource
            AppResource resource = new AppResource();
            
            // populate the entity
            resource.setId(id);
            resource.setLocale(locale);
            resource.setValue(value);
            resource.setKey(key);
            resource.setType(type);
            resource.setSubType(subType);

            appResources.add(resource);
        }
		
		return appResources;
	}
}
