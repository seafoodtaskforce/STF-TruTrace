package com.wwf.shrimp.application.services.main.dao.impl.mysql;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mysql.cj.api.jdbc.Statement;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.wwf.shrimp.application.exceptions.PersistenceException;
import com.wwf.shrimp.application.exceptions.ServiceManagementException;
import com.wwf.shrimp.application.models.DocumentType;
import com.wwf.shrimp.application.models.Group;
import com.wwf.shrimp.application.models.GroupType;
import com.wwf.shrimp.application.models.LookupEntity;
import com.wwf.shrimp.application.models.Organization;
import com.wwf.shrimp.application.models.OrganizationStage;
import com.wwf.shrimp.application.models.PasswordCredentials;
import com.wwf.shrimp.application.models.RESTResponse;
import com.wwf.shrimp.application.models.ResponseErrorData;
import com.wwf.shrimp.application.models.ResponseIssue;
import com.wwf.shrimp.application.models.ResponseMessageData;
import com.wwf.shrimp.application.models.Role;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.UserContact;
import com.wwf.shrimp.application.models.search.LookupDataSearchCriteria;
import com.wwf.shrimp.application.models.search.OrganizationSearchCriteria;
import com.wwf.shrimp.application.models.search.UserSearchCriteria;
import com.wwf.shrimp.application.utils.EmailUtils;

/**
 * The persistence implementation for Organization entities based on the MySQL database
 * @author AleaActaEst
 *
 * @param <T> - the operational entity that this DAO will work with.
 * @param <S> - The specific search criteria for entity <T>
 */
public class OrganizationMySQLDao <T, S> extends BaseMySQLDao<Organization, OrganizationSearchCriteria>{
	private LookupDataMySQLDao<LookupEntity, LookupDataSearchCriteria> lookupService = new LookupDataMySQLDao<LookupEntity, LookupDataSearchCriteria>();
	
	
	/**
	 * Get all the organizations on a global scope.
	 * 
	 * @return - a list of all the organizations; empty if none found
	 * 
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public List<Organization> getAllOrganizations() throws Exception{
		List<Organization> organizationResult=null;
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
                .prepareStatement("SELECT * "
                				+ "FROM organization ");

		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        organizationResult = extractOrganizationsFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return organizationResult;
	}
	
	
	/**
	 * Get all the organization Headers (Stages)
	 * 
	 * @return - a list of all the Organization Stages; empty if none found
	 * 
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public List<OrganizationStage> getAllGroupStages(long orgId) throws Exception{
		List<OrganizationStage> organizationResult=null;
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
                .prepareStatement("SELECT * "
                				+ "FROM group_data_type "
                				+ "WHERE org_id=? "
                				+ "ORDER BY order_index ASC");
		// insert the variable
		preparedSELECTstatement.setLong(1, orgId);
		// execute the statement 
		resultSet = preparedSELECTstatement.executeQuery();
		
        //
        // process the result
        organizationResult = extractOrganizationStagesFromResult(resultSet);
        
       

		// release the connection
		closeConnection(conn);
				
		// return the result
		return organizationResult;
	}
	
	
	/**
	 * Get all the organization Headers (Stages)
	 * 
	 * @return - a list of all the Organization Stages; empty if none found
	 * 
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public List<OrganizationStage> getStageByName(String stageName) throws Exception{
		List<OrganizationStage> organizationResult=null;
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
                .prepareStatement("SELECT * "
                				+ "FROM group_data_type "
                				+ "WHERE upper(value)=? ");
		// insert the variable
		preparedSELECTstatement.setString(1, stageName.toUpperCase());
		// execute the statement 
		resultSet = preparedSELECTstatement.executeQuery();
		
        //
        // process the result
        organizationResult = extractOrganizationStagesFromResult(resultSet);
        

		// release the connection
		closeConnection(conn);
				
		// return the result
		return organizationResult;
	}
	

	/**
	 * 
	 * @param userName - the non-null username to get the organizations for 
	 * @return
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public List<Organization> getAllOrganizations(String userName) throws Exception{
		List<Organization> organizationResult=null;
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
                .prepareStatement("SELECT * "
                				+ "FROM organization ");

		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        organizationResult = extractOrganizationsFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return organizationResult;
	}
	
	public boolean doesOrganizationExist(String orgName, String orgBusinessNumber, long orgId)  throws Exception {
		boolean result = false;
		List<Group> organizationResult=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		orgName = orgName.toUpperCase();
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT a.id, a.name AS organization_name, a.description AS organization_description, a.group_data_type_id, "
                		+ "a.business_id_number, a.legal_business_name, a.business_address, a.gps_location, a.email_address, a.verified, "
                		+ "b.name AS organization_type, b.order_index, b.org_id "
                		+ "FROM group_data a "
                		+ "JOIN group_data_type b on b.id = a.group_data_type_id "
                		+ "WHERE business_id_number = ? OR "
                		+ "upper(legal_business_name) = ? ");

		// insert the variable
		preparedSELECTstatement.setString(1, orgBusinessNumber);
		preparedSELECTstatement.setString(2, orgName);
		// execute the statement 	
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        organizationResult = extractGroupOrganizationsFromResult(resultSet);

		// release the connection
		closeConnection(conn);
		
		if(organizationResult.size() > 0) {
			result = true;
		} else {
			result = false;
		}
		return result;
		
	}
	
	

	/**
	 * 
	 * @param oName
	 * @return
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public Organization getOrganizationByName(String oName) throws Exception {
		Organization result=null;
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
		                .prepareStatement("SELECT * "
		                				+ "FROM organization "
		                				+ "WHERE name=?");
		// insert the variable
		preparedSELECTstatement.setString(1, oName);
		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractOrganizationFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
				
	}
	
	/**
	 * 
	 * @param orgId
	 * @return
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public Organization getOrganizationById(long orgId) throws Exception {
		Organization result=null;
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
		                .prepareStatement("SELECT * "
		                				+ "FROM organization "
		                				+ "WHERE id=?");
		// insert the variable
		preparedSELECTstatement.setLong(1, orgId);
		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractOrganizationFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
				
	}
	
	/**
	 * Fetch the hierarchy of groups based on the input organization.
	 * 
	 * @param orgId - the organization id
	 * @return - group tree based on the input id
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public List<Group> getGroupTreeByOrgId(long orgId) throws Exception{
		List<Group> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// get first all the 
		
		// create the statement to get all JOINS of the different tree elements
		preparedSELECTstatement = conn
                .prepareStatement("SELECT a.parent_org_id, a.group_id, b.name, b.description, c.parent_group_id, c.child_group_id "
                				+ "FROM organization_group_rel_tree a "
                				+ "JOIN group_data b ON a.group_id=b.id "
                				+ "JOIN group_group_rel_tree c ON a.group_id=c.parent_group_id "
                				+ "WHERE a.parent_org_id = ? "
                		);

		// execute the statement 
		preparedSELECTstatement.setLong(1, orgId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //F
        // process the result
        result = extractTreeGroupsFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
	}
	
	/**
	 * Get all the available groups for the organization
	 * 
	 * @param orgId - the organization id
	 * @return - the list of matching groups; empty if none found
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public List<Group> getAllGroupsByOrgId(long orgId) throws Exception{
		List<Group> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
	
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// get first all the 
		
		// create the statement to get all JOINS of the different tree elements
		preparedSELECTstatement = conn
                .prepareStatement("SELECT a.parent_org_id, a.group_id, "
                				+ "b.name, b.description, b.business_id_number, b.legal_business_name, b.business_address, b.gps_location, b.email_address, "
                				+ "c.value as group_type, c.name as group_value, c.id AS group_type_id, "
                				+ "c.color_hex_code as group_type_color, c.order_index AS group_type_order_index, gadr.doc_type_ids "
                				+ "FROM organization_group_rel_tree a "
                				+ "JOIN group_data b ON a.group_id=b.id "
                				+ "JOIN group_data_type c ON b.group_data_type_id=c.id "
                				+ "JOIN group_allowed_doctype_rel gadr ON gadr.parent_group_type_id = c.id "
                				+ "WHERE a.parent_org_id = ? "
                		);

		// execute the statement 
		preparedSELECTstatement.setLong(1, orgId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractGroupsFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
	}
	
	
	/**
	 * Get all the available groups for the given name
	 * 
	 * @param name - the name of the group/organization
	 * @return - the list of matching groups; empty if none found
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public List<Group> getAllGroupsByName(String name) throws Exception {
		List<Group> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
	
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// get first all the 
		
		// create the statement to get all JOINS of the different tree elements
		preparedSELECTstatement = conn
                .prepareStatement("SELECT a.parent_org_id, a.group_id, "
                				+ "b.name, b.description, b.business_id_number, b.legal_business_name, b.business_address, b.gps_location, b.email_address, "
                				+ "c.value as group_type, c.id AS group_type_id, "
                				+ "c.color_hex_code as group_type_color, c.order_index AS group_type_order_index, gadr.doc_type_ids "
                				+ "FROM organization_group_rel_tree a "
                				+ "JOIN group_data b ON a.group_id=b.id "
                				+ "JOIN group_data_type c ON b.group_data_type_id=c.id "
                				+ "JOIN group_allowed_doctype_rel gadr ON gadr.parent_group_type_id = c.id "
                				+ "WHERE b.name = ? "
                		);

		// execute the statement 
		preparedSELECTstatement.setString(1, name);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractGroupsFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
	}

	
	/**
	 * Get all the group types
	 * 
	 * @return - the list of groups types
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public List<GroupType> getAllGroupTypes(long organizationId) throws Exception{
		List<GroupType> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// get first all the 
		
		// create the statement to get all JOINS of the different tree elements
		preparedSELECTstatement = conn
                .prepareStatement("SELECT DISTINCT c.value as group_type, c.name as group_key, c.id AS group_type_id, "
                		+ "c.color_hex_code as group_type_color, c.order_index AS group_type_order_index, c.associated_groups, "
                		+ "gadr.doc_type_ids AS allowed_doc_types "
                		+ "FROM group_data_type c "
                		+ "LEFT JOIN group_allowed_doctype_rel gadr ON gadr.parent_group_type_id = c.id "
                		+ "WHERE c.org_id = ?"

                		);

		// execute the statement 
		preparedSELECTstatement.setLong(1, organizationId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractAllGroupTypesFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
	}
	
	/**
	 * get all the groups
	 * 
	 * @return - the list of all groups
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public List<Group> getAllOrganizationGroups(long organizationId, boolean sparseFlag) throws Exception{
		List<Group> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// get first all the 
		
		// create the statement to get all JOINS of the different tree elements
		preparedSELECTstatement = conn
                .prepareStatement("SELECT a.id, a.name AS organization_name, a.description AS organization_description, a.group_data_type_id, "
                		+ "a.business_id_number, a.legal_business_name, a.business_address, a.gps_location, a.email_address, a.verified, "
                		+ "b.name AS organization_type, b.order_index, b.org_id "
                		+ "FROM group_data a "
                		+ "JOIN group_data_type b on b.id = a.group_data_type_id "
                		+ "WHERE b.org_id = ?"
                		);
		
		preparedSELECTstatement.setLong(1, organizationId);

		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractGroupOrganizationsFromResult(resultSet);


		// release the connection
		closeConnection(conn);
		// return the result
		return result;
	}
	
	/**
	 * get all the groups of the given type for the organization
	 * 
	 * @param groupType
	 * @return - the list of all matching groups
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public List<Group> getAllOrganizationGroups(long organizationId, String groupType) throws Exception{
		List<Group> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// get first all the 
		
		// create the statement to get all JOINS of the different tree elements
		preparedSELECTstatement = conn
                .prepareStatement("SELECT a.id, a.name AS organization_name, a.description AS organization_description, a.group_data_type_id, a.email_address, a.verified, "
                		+ "b.name AS organization_type, b.order_index, b.org_id "
                		+ "FROM group_data a "
                		+ "JOIN group_data_type b on b.id = a.group_data_type_id "
                		+ "WHERE b.name = ? AND b.org_id = ? "
                		);

		// execute the statement 
		preparedSELECTstatement.setString(1, groupType);
		preparedSELECTstatement.setLong(2, organizationId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractGroupOrganizationsFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
	}
	
	/**
	 * get all the groups of the given type for the organization
	 * 
	 * @param groupType
	 * @return - the list of all matching groups
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public Group getOrganizationGroupByName(long organizationId, String groupName) throws Exception{
		Group result = null;
		List<Group> tempResult = null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// get first all the 
		
		// create the statement to get all JOINS of the different tree elements
		preparedSELECTstatement = conn
                .prepareStatement("SELECT a.id, a.name AS organization_name, a.description AS organization_description, a.group_data_type_id, "
                		+ "a.business_id_number, a.legal_business_name, a.business_address, a.gps_location, a.email_address, a.verified, "
                		+ "b.name AS organization_type, b.order_index, b.org_id "
                		 + "FROM group_data a " 
                		 + "JOIN group_data_type b on b.id = a.group_data_type_id " 
                		 + "WHERE b.org_id = ? AND a.name = ?"
                		);

		// execute the statement 
		preparedSELECTstatement.setLong(1, organizationId);
		preparedSELECTstatement.setString(2, groupName);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        tempResult = extractGroupOrganizationsFromResult(resultSet);
        if(tempResult.size() > 0){
        	result = tempResult.get(0);
        }

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
	}
	
	
	/**
	 * Get all available group types
	 * 
	 * @return - list of all group types
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public List<GroupType> getAllGroupDataTypes() throws Exception{
		List<GroupType> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// get first all the 
		
		// create the statement to get all JOINS of the different tree elements
		preparedSELECTstatement = conn
                .prepareStatement("SELECT * FROM group_data_type"
                		);

		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractGroupsTypesFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
	}
	
	
	/**
	 * Get all available group types
	 * 
	 * @return - list of all group types
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public List<GroupType> getGroupDataTypeById(long orgTypeId) throws Exception{
		List<GroupType> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// get first all the 
		
		// create the statement to get all JOINS of the different tree elements
		preparedSELECTstatement = conn
                .prepareStatement("SELECT * FROM group_data_type WHERE id = ? ");
                		

		// execute the statement 
		preparedSELECTstatement.setLong(1, orgTypeId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractGroupDataTypesFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
	}
	
	
	/**
	 * Create a new Stages based on the input.
	 * 
	 * @param groupOrganization - the instance of Organization to persist.
	 * @return - the persisted Organization instance with a unique id set.
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public List<OrganizationStage> createOrganizationStages(List<OrganizationStage> orgStages) throws Exception{
		PreparedStatement preparedINSERTstatement;
		PreparedStatement preparedUPDATEstatement;
		PreparedStatement preparedDELETEStatement;
		PreparedStatement preparedSELECTAllStageIDsStatement;
		long returnId=0;
		long orgID = 0;
		List<Long> allStageIDs = new ArrayList<Long>();
		List<Long> stageIDsToDelete = new ArrayList<Long>();
		
		// look up the user by name which should be unique
		//
		orgID = orgStages.get(0).getOrgID();
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement to get all JOINS of the different tree elements
		String insertQuery = " insert into group_data_type ("
				+ "name, "
				+ "value, "
				+ "color_hex_code, "
				+ "order_index, "
				+ "org_id "
				+ ")"
		        + " values (?,?,?, ?, ?)";
		
		String updateQuery = " UPDATE group_data_type "
				+ " SET name = ?, "
				+ " value = ?, "
				+ " color_hex_code = ?, "
				+ " order_index = ? " 
		        + " WHERE id = ?";		
		
		String getAllIStageIDsQuery = " SELECT id FROM group_data_type WHERE org_id=? ";
		
		String deleteQueryStart = " DELETE FROM group_data_type WHERE org_id=? "
				+ " AND id IN (";
		String deleteQueryEnd = ")";
		String deleteQuery = null;
		
		String deleteAllowedDocsQueryStart = " DELETE FROM group_allowed_doctype_rel WHERE "
				+ " parent_group_type_id IN (";
		String deleteAllowedDocsQueryEnd = ")";
		String deleteAllowedDocsQuery = null;
		
		/**
		 * Get all IDs to delete
		 */
		
		//
		// get all existing IDs
		preparedSELECTAllStageIDsStatement = conn
		        .prepareStatement(getAllIStageIDsQuery);
		preparedSELECTAllStageIDsStatement.setLong(1, orgID);
		ResultSet resultSet = preparedSELECTAllStageIDsStatement.executeQuery();
		allStageIDs = extractStageIDsFromResult(resultSet);
		stageIDsToDelete = new ArrayList<Long>(allStageIDs);
		//
		// create a list of IDs to delete
		for(int i= 0; i< orgStages.size(); i++){
			if(orgStages.get(i).getId() != 0){
				stageIDsToDelete.remove(new Long(orgStages.get(i).getId()));
			}
		}

		//
		// Create the delete queries
		for(int i= 0; i< stageIDsToDelete.size(); i++){
				deleteQueryStart += (stageIDsToDelete.get(i) + ",");
				deleteAllowedDocsQueryStart += (stageIDsToDelete.get(i) + ",");
				
		}
		//
		// Execute deletion of specific elements
		if(stageIDsToDelete.size() > 0){
			
			//
			// delete the extra stage records
			deleteQueryStart = deleteQueryStart.substring(0,deleteQueryStart.length() - 1);
			deleteQuery = deleteQueryStart + deleteQueryEnd;
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			preparedDELETEStatement.setLong(1, orgID);
			preparedDELETEStatement.execute();
			
			//
			// delete the extra allowed docs records
			deleteAllowedDocsQueryStart = deleteAllowedDocsQueryStart.substring(0,deleteAllowedDocsQueryStart.length() - 1);
			deleteAllowedDocsQuery = deleteAllowedDocsQueryStart + deleteAllowedDocsQueryEnd;
			preparedDELETEStatement = conn
			        .prepareStatement(deleteAllowedDocsQuery);
			preparedDELETEStatement.execute();
			

		}
		
		

		// execute the statement 
		//
		// create the user data
		try {
			for(int i= 0; i< orgStages.size(); i++){
				
				if(orgStages.get(i).getId() <= 0){
					getLog().info("Creating a new Stage: " + orgStages.get(i).getName());
					preparedINSERTstatement = conn
					        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
					
					// get the data
					String name =  orgStages.get(i).getName();
					String value =  orgStages.get(i).getValue();
					String colorHexCode =  orgStages.get(i).getColorHexCode();
					int ordIndex = i;
					long orgId = orgStages.get(i).getOrgID();
					
					// execute the statement 
					preparedINSERTstatement.setString(1, name);
					preparedINSERTstatement.setString(2,value);
					preparedINSERTstatement.setString(3,colorHexCode);
					preparedINSERTstatement.setInt(4,ordIndex);
					preparedINSERTstatement.setLong(5,orgId);
					preparedINSERTstatement.executeUpdate();
					
					// get the id of the created 
					ResultSet rs = preparedINSERTstatement.getGeneratedKeys();
					if (rs.next()){
						returnId=rs.getLong(1);
						orgStages.get(i).setId(returnId);
						// create relationship to allowed docs for this
						createGroupAssignedDocs(orgStages.get(i).getId(), "");
					}
				}else{
					getLog().info("Updating a Stage: " + orgStages.get(i).getName());
					preparedUPDATEstatement = conn
					        .prepareStatement(updateQuery);
					
					// get the data
					String name =  orgStages.get(i).getName();
					String value =  orgStages.get(i).getValue();
					String colorHexCode =  orgStages.get(i).getColorHexCode();
					int ordIndex = i;
					long id = orgStages.get(i).getId();
					
					// execute the statement 
					preparedUPDATEstatement.setString(1, name);
					preparedUPDATEstatement.setString(2,value);
					preparedUPDATEstatement.setString(3,colorHexCode);
					preparedUPDATEstatement.setInt(4,ordIndex);
					preparedUPDATEstatement.setLong(5,id);
					preparedUPDATEstatement.executeUpdate();
					
				}
				
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// release the connection
		closeConnection(conn);
				
		// return the result
		return orgStages;
	}
	
	
	
	/**
	 * Delete all the stages for a given document
	 * 
	 * @param orgId - the organization id for which to remove the stages
	 * @return - nothing
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public void deleteOrganizationStages(long orgId) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		
		// create the query
		String deleteQuery = " DELETE FROM group_data_type WHERE org_id = ?";
		
		// create the statement
		try {
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			
			// execute the statement 
			preparedDELETEStatement.setLong(1, orgId);
			preparedDELETEStatement.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting a stages list with id=" + orgId, e);
		}

		// release the connection
		closeConnection(conn);
	}
	
	
	
	/**
	 * Create a new Organization group based on the input.
	 * 
	 * @param groupOrganization - the instance of Organization to persist.
	 * @return - the persisted Organization instance with a unique id set.
	 * @throws Exception
	 * 			- if there were any issues with the request
	 */
	public Group createOrganizationGroup(Group groupOrganization) throws Exception{
		PreparedStatement preparedINSERTstatement;
		long returnId=0;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// get first all the 
		
		// create the statement to get all JOINS of the different tree elements
		String insertQuery = " insert into group_data ("
				+ "name, "
				+ "description, "
				+ "group_data_type_id, "
				+ "business_id_number, "
				+ "legal_business_name, "
				+ "business_address, "
				+ "gps_location, "
				+ "email_address, "
				+ "verified "
				+ ")"
		        + " values (?,?,?,?,?,?,?,?,?)";

		// execute the statement 
		//
		// create the user data
		try {
			preparedINSERTstatement = conn
			        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			
			// get the data
			String groupOrganizationName =  groupOrganization.getName();
			String groupOrganizationDescription =  groupOrganization.getDescription();
			long groupOrganizationTypeId = groupOrganization.getGroupType().getId();
			String businessIDNumber =  groupOrganization.getBusinessIDNumber();
			String legalBusinessName =  groupOrganization.getLegalBusinessName();
			String businessAddress =  groupOrganization.getBusinessAddress();
			String gpsCoordinates =  groupOrganization.getGpsCoordinates();
			String emailAddress =  groupOrganization.getEmailAddress();
			boolean verified =  groupOrganization.isVerified();
			
			// execute the statement 
			preparedINSERTstatement.setString(1, groupOrganizationName);
			preparedINSERTstatement.setString(2,groupOrganizationDescription);
			preparedINSERTstatement.setLong(3,groupOrganizationTypeId);
			preparedINSERTstatement.setString(4,businessIDNumber);
			preparedINSERTstatement.setString(5,legalBusinessName);
			preparedINSERTstatement.setString(6,businessAddress);
			preparedINSERTstatement.setString(7,gpsCoordinates);
			preparedINSERTstatement.setString(8,emailAddress);
			preparedINSERTstatement.setBoolean(9,verified);
			
			
			
			preparedINSERTstatement.executeUpdate();
			
			// get the id of the created 
			ResultSet rs = preparedINSERTstatement.getGeneratedKeys();
			if (rs.next()){
				returnId=rs.getLong(1);
				groupOrganization.setId(returnId);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// release the connection
		closeConnection(conn);
				
		// return the result
		return groupOrganization;
	}
	
	/**
	 * This will update the list of allowed docs.
	 * 
	 * @param credentials - the user credentials
	 * @throws PersistenceException
	 * 		- if there were any issues with the request/processing
	 */
	public void updateGroupAssignedDocs(long parentGroupId, String allowedDocs )  throws PersistenceException {
		PreparedStatement preparedUPDATEStatement;
		
		// get the connection
		Connection conn = openConnection();
		
	
		// create the query for user
		// create the query
		String updateQuery = " UPDATE group_allowed_doctype_rel  "
				+ "set doc_type_ids = ? "
				+ "WHERE parent_group_type_id = ?";
		
		//
		// create the user data
		try {
			
			// get the data
			// create the statement
			preparedUPDATEStatement = conn
	                .prepareStatement(updateQuery);
			// execute the statement

			preparedUPDATEStatement.setString(1, allowedDocs);
			preparedUPDATEStatement.setLong(2, parentGroupId);
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
	 * This will update the specific group organization data
	 * 
	 * @param credentials - the user credentials
	 * @throws PersistenceException
	 * 		- if there were any issues with the request/processing
	 */
	public void updateOrganizationGroup(Group groupOrganization)  throws PersistenceException {
		PreparedStatement preparedUPDATEStatement;
		
		// get the connection
		Connection conn = openConnection();
		
	
		// create the query for user
		// create the query
		String updateQuery = " UPDATE group_data  "
				+ "set name = ?, "
				+ "description = ?, "
				+ "group_data_type_id = ?, "
				+ "business_id_number = ?, "
				+ "legal_business_name = ?, "
				+ "business_address = ?, "
				+ "gps_location = ?,  "
				+ "email_address = ?, "
				+ "verified = ? "
				+ "WHERE id = ?";
		
		//
		// create the user data
		try {
			
			// get the data
			// create the statement
			preparedUPDATEStatement = conn
	                .prepareStatement(updateQuery);
			// execute the statement

			preparedUPDATEStatement.setString(1, groupOrganization.getName());
			preparedUPDATEStatement.setString(2, groupOrganization.getDescription());
			preparedUPDATEStatement.setLong(3, groupOrganization.getGroupType().getId());
			preparedUPDATEStatement.setString(4, groupOrganization.getBusinessIDNumber());
			preparedUPDATEStatement.setString(5, groupOrganization.getLegalBusinessName());
			preparedUPDATEStatement.setString(6, groupOrganization.getBusinessAddress());
			preparedUPDATEStatement.setString(7, groupOrganization.getGpsCoordinates());
			preparedUPDATEStatement.setString(8, groupOrganization.getEmailAddress());
			preparedUPDATEStatement.setBoolean(9, groupOrganization.isVerified());
			
			preparedUPDATEStatement.setLong(10, groupOrganization.getId());
			
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
	 * This will create the list of allowed docs.
	 * 
	 * @param credentials - the user credentials
	 * @throws PersistenceException
	 * 		- if there were any issues with the request/processing
	 */
	public void createGroupAssignedDocs(long parentGroupId, String allowedDocs )  throws PersistenceException {
		PreparedStatement preparedINSERTStatement;
		
		// get the connection
		Connection conn = openConnection();
		
	
		// create the query for user
		// create the query
		String insertQuery = " INSERT INTO group_allowed_doctype_rel  ("
				+ "parent_group_type_id, "
				+ "doc_type_ids "
				+ ")"
		        + " values (?,?)";
		
		// execute the statement 
		//
		// create the user data
		try {
			preparedINSERTStatement = conn
			        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			
			// get the data
			long parentStageId =  parentGroupId;
			String allowedDocPermissions =  allowedDocs;
			
			// execute the statement 
			preparedINSERTStatement.setLong(1, parentStageId);
			preparedINSERTStatement.setString(2,allowedDocPermissions);
			
			
			preparedINSERTStatement.executeUpdate();
			getLog().debug("Creating Allowed Docs Stage Entry for Stage: " + parentStageId);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			getLog().error("Error Creating new Allowed Doc for Stages: - " + e);
		}
		

		// release the connection
		closeConnection(conn);
	}
	
	/**
	 * Update the profile image for the user
	 * @param profileImage - the image data
	 * @param username - the user for whom the update is done
	 * @throws PersistenceException
	 * 		- if there were any issues in processing this request
	 */
	public Object verifyBatchOrgCSVFile(byte [] bDocImportCSV, String username) throws PersistenceException {
		List<String> result = new ArrayList<String>();
		ResponseErrorData errorData = new ResponseErrorData();
		ResponseMessageData messageData = new ResponseMessageData();
		boolean errorsFlag = false;
	    
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
		                    // Process Display Name
                  			//
		                	if(cell.isEmpty()){
		                		// Add empty issue
		                		result.add(
	                				RESTResponse.createCVSErrorMessage(
                						lineNumber, 
                						"Display Name should not be empty", 
                						column + 1, 
                						"Display Name	"));
		                		errorsFlag = true;
		                		// set the issue
		                		ResponseIssue issue = new ResponseIssue();
		                		issue.setLineNumber(String.valueOf(lineNumber));
		                		issue.setColumnNumber(String.valueOf(column + 1));
		                		issue.setColumnName("Display Name");
		                		issue.setIssue("Display Name should not be empty");
		                		issue.setRawMessage(result.get(result.size()-1));
		                		issue.setSeverity(ResponseIssue.ISSUE_SEVERITY_FATAL_ERROR);
		                		errorData.getIssues().add(issue);
		                	}
		                    break;
		                    
                  		case 1:
                  			//
		                	// Process Legal Business Name
                  			//
							if(cell.isEmpty()){
								// Add empty issue
								result.add(
									RESTResponse.createCVSErrorMessage(
										lineNumber, 
										"Legal Business Name should not be empty", 
										column + 1,
										"Legal Business Name"));
								errorsFlag = true;
								// set the issue
		                		ResponseIssue issue = new ResponseIssue();
		                		issue.setLineNumber(String.valueOf(lineNumber));
		                		issue.setColumnNumber(String.valueOf(column + 1));
		                		issue.setColumnName("Legal Business Name");
		                		issue.setIssue("Legal Business Name should not be empty");
		                		issue.setRawMessage(result.get(result.size()-1));
		                		issue.setSeverity(ResponseIssue.ISSUE_SEVERITY_FATAL_ERROR);
		                		errorData.getIssues().add(issue);
							}
		                    break;
		                    
                  		case 2:
                  			//
							// Process Business Number
                  			//
                  			
                  			// check for empty Business Number
							if(cell.isEmpty()){
								// Add empty issue
								result.add(
									RESTResponse.createCVSErrorMessage(
										lineNumber, 
										"Business Number should not be empty", 
										column + 1,
										"Business Number"));
								errorsFlag = true;
								// set the issue
		                		ResponseIssue issue = new ResponseIssue();
		                		issue.setLineNumber(String.valueOf(lineNumber));
		                		issue.setColumnNumber(String.valueOf(column + 1));
		                		issue.setColumnName("Business Number");
		                		issue.setIssue("Business Number should not be empty");
		                		issue.setRawMessage(result.get(result.size()-1));
		                		issue.setSeverity(ResponseIssue.ISSUE_SEVERITY_FATAL_ERROR);
		                		errorData.getIssues().add(issue);
							}
							
							// check uniqueness
							if(!cell.isEmpty() && !isBusinessNumberUnique(cell)){
								// Add unique issue
								result.add(
									RESTResponse.createCVSErrorMessage(
										lineNumber, 
										"Business Number must be unique. " + cell + " is already taken.", 
										column + 1,
										"Business Number"));
								errorsFlag = true;
								// set the issue
		                		ResponseIssue issue = new ResponseIssue();
		                		issue.setLineNumber(String.valueOf(lineNumber));
		                		issue.setColumnNumber(String.valueOf(column + 1));
		                		issue.setColumnName("Business Number");
		                		issue.setIssue("Business Number must be unique. " + cell + " is already taken.");
		                		issue.setRawMessage(result.get(result.size()-1));
		                		issue.setSeverity(ResponseIssue.ISSUE_SEVERITY_FATAL_ERROR);
		                		errorData.getIssues().add(issue);
							}
		                    break;
		                    
                  		case 3:
                  			//
							// Process Business Address
                  			//
                  			
                  			// check for empty email address
							if(cell.isEmpty()){
								// Add empty issue
								result.add(
									RESTResponse.createCVSErrorMessage(
										lineNumber, 
										"Business Address should not be empty", 
										column + 1,
										"Business Address"));
								errorsFlag = true;
								// set the issue
		                		ResponseIssue issue = new ResponseIssue();
		                		issue.setLineNumber(String.valueOf(lineNumber));
		                		issue.setColumnNumber(String.valueOf(column + 1));
		                		issue.setColumnName("Business Address");
		                		issue.setIssue("Business Address should not be empty.");
		                		issue.setRawMessage(result.get(result.size()-1));
		                		issue.setSeverity(ResponseIssue.ISSUE_SEVERITY_FATAL_ERROR);
		                		errorData.getIssues().add(issue);
							}
		                    break;
		                    
		                  case 4:
		                	//
							// Process GPS Location
		                	//  
							if(cell.isEmpty()){
								// Add empty issue
								result.add(
									RESTResponse.createCVSErrorMessage(
										lineNumber, 
										"GPS Location is empty", 
										column + 1,
										"GPS Location"));
								errorsFlag = true;
								// set the issue
		                		ResponseIssue issue = new ResponseIssue();
		                		issue.setLineNumber(String.valueOf(lineNumber));
		                		issue.setColumnNumber(String.valueOf(column + 1));
		                		issue.setColumnName("GPS Location");
		                		issue.setIssue("GPS Location is empty.");
		                		issue.setRawMessage(result.get(result.size()-1));
		                		issue.setSeverity(ResponseIssue.ISSUE_SEVERITY_WARNING);
		                		errorData.getIssues().add(issue);
							}		                
		                    break;
		                    
		                  case 5:
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
			                    
		                  case 6:
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
			                    
	                  		case 7:
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

	
	
	
	
	
	
	
	/**************************************************************************************
	 * Private helper methods
	 */
	
	/**
	 * Extract Organization data from SQL result set
	 * 
	 * @param resultSet - the input representation of the records fetched through the initial SQL
	 * @return - a list of notifications; empty of there were none
	 * @throws SQLException
	 * 			- if there were any issues with the request
	 */
	private List<Group> extractGroupOrganizationsFromResult(ResultSet resultSet) throws SQLException {
		List<Group> result = new ArrayList<Group>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            String name = resultSet.getString("organization_name");
            long organizationId = resultSet.getLong("org_id");
            String description = resultSet.getString("organization_description");
            
            long organizationTypeId = resultSet.getLong("group_data_type_id");
            String organizationType = resultSet.getString("organization_type");
            int organizationTypeOrderIndex = resultSet.getInt("order_index");
            
            String businessIDNumber = resultSet.getString("business_id_number");
			String legalBusinessName = resultSet.getString("legal_business_name");
			String businessAddress = resultSet.getString("business_address");
			String gpsCoordinates = resultSet.getString("gps_location");
			String emailAddress = resultSet.getString("email_address");
			boolean verified = resultSet.getBoolean("verified");
            
            // populate the Group entity
            Group org = new Group();
            org.setId(id);
            org.setName(name);
            org.setDescription(description);
            org.setOrganizationId(organizationId);
            org.setBusinessIDNumber(businessIDNumber); 
            org.setLegalBusinessName(legalBusinessName);
            org.setBusinessAddress(businessAddress);
            org.setGpsCoordinates(gpsCoordinates);
            org.setEmailAddress(emailAddress);
            org.setVerified(verified);
            
            
            GroupType orgType = new GroupType();
            orgType.setId(organizationTypeId);
            orgType.setName(organizationType);
            orgType.setOrderIndex(organizationTypeOrderIndex);
            org.setGroupType(orgType);
            
            result.add(org);
        }
		
		return result;
	}
	
	/**
	 * Extract Organization data from SQL result set
	 * 
	 * @param resultSet - the input representation of the records fetched through the initial SQL
	 * @return - a list of organizations empty of there were none
	 * @throws SQLException
	 * 			- if there were any issues with the request
	 */
	private Organization extractOrganizationFromResult(ResultSet resultSet) throws SQLException {
		Organization organization = new Organization();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            String name = resultSet.getString("name");
            String description = resultSet.getString("description");
            
            // populate the User entity
            organization.setName(name);
            organization.setId(id);
            organization.setDescription(description);
        }
		
		return organization;
	}
	
	private List<Long> extractStageIDsFromResult(ResultSet resultSet) throws SQLException {
		List<Long> ids = new ArrayList<Long>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            
            // populate the User entity
			ids.add(new Long(id));
        }
		
		return ids;
	}
	
	/**
	 * Extract the Organization data from the SQL based result set
	 * 
	 * @param resultSet - the input representation of the records fetched through the initial SQL
	 * @return
	 * @throws SQLException
	 * 			- if there were any issues with the request
	 */
	private List<Organization> extractOrganizationsFromResult(ResultSet resultSet) throws SQLException {
		List<Organization> result = new ArrayList<Organization>();
	
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			Organization organization = new Organization();
			
			// extract the data
			long id = resultSet.getLong("id");
            String name = resultSet.getString("name");
            String description = resultSet.getString("description");
            
            // populate the User entity
            organization.setName(name);
            organization.setId(id);
            organization.setDescription(description);
            
            // add the user to the list
            result.add(organization);
        }
		return result;
	}
	
	/**
	 * Extract the group data as a tree (i.e. with all linked nodes)
	 * 
	 * @param resultSet - the input representation of the records fetched through the initial SQL
	 * @return -  a list of groups; empty of none found
	 * @throws SQLException
	 * 			- if there were any issues with the request
	 */
	private List<Group> extractTreeGroupsFromResult(ResultSet resultSet) throws SQLException {
		List<Group> result = new ArrayList<Group>();
	
		// process the groups
		//
		
		
		// extract the main data about the orgnization
		while (resultSet.next()) {
			Group group = new Group();
			
			// extract the data
			long id = resultSet.getLong("group_id");
            String name = resultSet.getString("name");
            String description = resultSet.getString("description");
            long organizationId = resultSet.getLong("parent_org_id");
            long groupParentId = resultSet.getLong("parent_group_id");
            long groupChildId = resultSet.getLong("child_group_id");
            
            // populate the User entity
            group.setName(name);
            group.setId(id);
            group.setDescription(description);
            group.setParentId(groupParentId);
            group.setOrganizationId(organizationId);
            group.setChildId(groupChildId);
            
 
            // add the user to the list
            result.add(group);
        }
		return result;
	}
	
	
	
	/**
	 * Extract the flat group data, all related to the input group type id
	 * 
	 * @param resultSet - the db set to parse out
	 * @return - the list of group data
	 * @throws SQLException
	 * 			- if there were any issues with the request
	 */
	private List<Group> extractGroupsFromResult(ResultSet resultSet) throws SQLException {
		List<Group> result = new ArrayList<Group>();
		
		lookupService.init();
	
		// process the groups
		//
		
		// extract the main data about the organization
		while (resultSet.next()) {
			Group group = new Group();
			GroupType groupType = new GroupType();
			List<DocumentType> allowedDocTypes = new ArrayList<DocumentType>();
			
			// extract the data
			long organizationId = resultSet.getLong("parent_org_id");
			long groupId = resultSet.getLong("group_id");
            String name = resultSet.getString("name");
            String description = resultSet.getString("description");
            String groupTypeName = resultSet.getString("group_type");
            String groupTypeValue = resultSet.getString("group_value");
            String groupTypeColor = resultSet.getString("group_type_color");
            long groupTypeId= resultSet.getLong("group_type_id");
            int groupTypeOrderIndex = resultSet.getInt("group_type_order_index");
            String allowedDocTypeIds = resultSet.getString("doc_type_ids");
            String businessIDNumber = resultSet.getString("business_id_number");
			String legalBusinessName = resultSet.getString("legal_business_name");
			String businessAddress = resultSet.getString("business_address");
			String gpsCoordinates = resultSet.getString("gps_location");
			String emailAddress = resultSet.getString("email_address");
            
            // populate the allowed doc types for this user
            //
            if(allowedDocTypeIds != null && !allowedDocTypeIds.isEmpty()){
            	String[] ids = allowedDocTypeIds.split(",");
            	try {
					List<DocumentType> allDocTypes = lookupService.getAllDocumentTypes();
					for(int i=0; i< ids.length; i++){
	            		long docTypeId = Long.parseLong(ids[i]);
	            		for(int j=0; j< allDocTypes.size(); j++){
		            		if(allDocTypes.get(j).getId() == docTypeId){
		            			allowedDocTypes.add(allDocTypes.get(j));
		            		}
	            		}
	            	}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            
            // populate the Group entity
            //
            
            // populate group type
            groupType.setId(groupTypeId);
            groupType.setName(groupTypeName);
            groupType.setValue(groupTypeValue);
            groupType.setHexColorCode(groupTypeColor);
            groupType.setOrderIndex(groupTypeOrderIndex);
            
            // set the group data
            group.setOrganizationId(organizationId);
            group.setId(groupId);
            group.setName(name);
            group.setDescription(description);
            group.setGroupType(groupType);
            group.setAllowedDocTypes(allowedDocTypes);
            group.setBusinessIDNumber(businessIDNumber); 
            group.setLegalBusinessName(legalBusinessName);
            group.setBusinessAddress(businessAddress);
            group.setGpsCoordinates(gpsCoordinates);
            group.setEmailAddress(emailAddress);

            // add the user to the list
            result.add(group);
        }
		return result;
	}
	
	/**
	 * Extract the flat group data, all related to the input organization id
	 * 
	 * @param resultSet - the db set to parse out
	 * @return - the list of group data
	 * @throws SQLException
	 * 			- if there were any issues with the request
	 */
	private List<GroupType> extractGroupsTypesFromResult(ResultSet resultSet) throws SQLException {
		List<GroupType> result = new ArrayList<GroupType>();
	
		// process the groups
		//
		
		// extract the main data about the organization
		while (resultSet.next()) {
			GroupType groupType = new GroupType();
			
			// extract the data
            String groupTypeName = resultSet.getString("group_type");
            String groupTypeColor = resultSet.getString("group_type_color");
            long groupTypeId= resultSet.getLong("group_type_id");
            int groupTypeOrderIndex = resultSet.getInt("group_type_order_index");
            
            // populate the Group entity
            //
            
            // populate group type
            groupType.setId(groupTypeId);
            groupType.setName(groupTypeName);
            groupType.setHexColorCode(groupTypeColor);
            groupType.setOrderIndex(groupTypeOrderIndex);
            
            // add the user to the list
            result.add(groupType);
        }
		return result;
	}
	
	
	/**
	 * Extract the flat group data, all related to the input organization id
	 * 
	 * @param resultSet - the db set to parse out
	 * @return - the list of group data
	 * @throws SQLException
	 * 			- if there were any issues with the request
	 */
	private List<GroupType> extractGroupDataTypesFromResult(ResultSet resultSet) throws SQLException {
		List<GroupType> result = new ArrayList<GroupType>();
	
		// process the groups
		//
		
		// extract the main data about the organization
		while (resultSet.next()) {
			GroupType groupType = new GroupType();
			
			// extract the data
            String groupTypeName = resultSet.getString("name");
            String groupTypeColor = resultSet.getString("color_hex_code");
            long groupTypeId= resultSet.getLong("id");
            long ordId= resultSet.getLong("org_id");
            int groupTypeOrderIndex = resultSet.getInt("order_index");
            String associatedGroupsUnparsed = resultSet.getString("associated_groups");
            
            // pre-process any data
            // 
            String[] associatedGroupsParsed = null; 
            long[] associatedGroupIds = null;
            if(associatedGroupsUnparsed != null && !associatedGroupsUnparsed.isEmpty()) {
            	associatedGroupsParsed = associatedGroupsUnparsed.split(",");
            	associatedGroupIds = new long[associatedGroupsParsed.length];
            	for(int i=0; i<associatedGroupsParsed.length; i++ ){
            		associatedGroupIds[i] = Long.parseLong(associatedGroupsParsed[i]);
            	}
            }
            
            
            // populate the Group entity
            //
            
            // populate group type
            groupType.setId(groupTypeId);
            groupType.setName(groupTypeName);
            groupType.setHexColorCode(groupTypeColor);
            groupType.setOrderIndex(groupTypeOrderIndex);
            groupType.setMatrixId(ordId);
            groupType.setAssociatedStageIds(associatedGroupIds);
            
            // add the user to the list
            result.add(groupType);
        }
		return result;
	}
	
	/**
	 * Extract the flat group data, all related to the input organization id
	 * 
	 * @param resultSet - the db set to parse out
	 * @return - the list of group data
	 * @throws SQLException
	 * 			- if there were any issues with the request
	 */
	private List<GroupType> extractGroupTypesFromResult(ResultSet resultSet) throws SQLException {
		List<GroupType> result = new ArrayList<GroupType>();
	
		// process the groups
		//
		
		// extract the main data about the organization
		while (resultSet.next()) {
			GroupType groupType = new GroupType();
			
			// extract the data
            String groupTypeName = resultSet.getString("group_type");
            String groupTypeColor = resultSet.getString("group_type_color");
            long groupTypeId= resultSet.getLong("group_type_id");
            int groupTypeOrderIndex = resultSet.getInt("group_type_order_index");
            
            // populate the Group entity
            //
            
            // populate group type
            groupType.setId(groupTypeId);
            groupType.setName(groupTypeName);
            groupType.setHexColorCode(groupTypeColor);
            groupType.setOrderIndex(groupTypeOrderIndex);
            
            // add the user to the list
            result.add(groupType);
        }
		return result;
	}
	
	/**
	 * Extract the expanded group type data, all related to the input group type id
	 * 
	 * @param resultSet - the db set to parse out
	 * @return - the list of group data
	 * @throws Exception 
	 * 			- if there were any issues with the request
	 */
	private List<GroupType> extractAllGroupTypesFromResult(ResultSet resultSet) throws Exception {
		List<GroupType> result = new ArrayList<GroupType>();
		lookupService.init();
	
		// process the groups
		//
		
		// extract the main data about the organization
		while (resultSet.next()) {
			GroupType groupType = new GroupType();
			
			String groupTypeKey = resultSet.getString("group_key");
			String groupTypeName = resultSet.getString("group_type");
            String groupTypeColor = resultSet.getString("group_type_color");
            long groupTypeId= resultSet.getLong("group_type_id");
            int groupTypeOrderIndex = resultSet.getInt("group_type_order_index");
            String allowedDocTypeIds = resultSet.getString("allowed_doc_types");
            List<DocumentType> allowedDocTypes = new ArrayList<DocumentType>();
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
            
            // populate the Group entity
            //
            
            // populate group type
            groupType.setId(groupTypeId);
            groupType.setName(groupTypeName);
            groupType.setValue(groupTypeKey);
            groupType.setHexColorCode(groupTypeColor);
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
            
            groupType.setAllowedDocTypes(allowedDocTypes);
            
            // add the user to the list
            result.add(groupType);
        }
		return result;
	}
	
	
	
	/**
	 * Extract the expanded group type data, all related to the input group type id
	 * 
	 * @param resultSet - the db set to parse out
	 * @return - the list of group data
	 * @throws Exception 
	 * 			- if there were any issues with the request
	 */
	private List<OrganizationStage> extractOrganizationStagesFromResult(ResultSet resultSet) throws Exception {
		List<OrganizationStage> result = new ArrayList<OrganizationStage>();
		// process the groups
		//
		
		// extract the main data about the organization
		while (resultSet.next()) {
			OrganizationStage organizationStage = new OrganizationStage();

			long id = resultSet.getLong("id");
			String stageName = resultSet.getString("name");
			String stageValue = resultSet.getString("value");
			String stageColorHexValue = resultSet.getString("color_hex_code");
			int orderIndex = resultSet.getInt("order_index");
			long organizationId = resultSet.getInt("org_id");
            
            // Populate the entity
            //
            
            // populate Stage Data
            organizationStage.setId(id);
            organizationStage.setName(stageName);
            organizationStage.setValue(stageValue);
            organizationStage.setColorHexCode(stageColorHexValue);
            organizationStage.setOrderIndex(orderIndex);
            organizationStage.setOrgID(organizationId);
            
            // add the user to the list
            result.add(organizationStage);
        }
		return result;
	}
	
	
	
	/**
	 * Checks of the given business number is unique in the system
	 * @param businessNumber - the business Number  to check
	 * @return - true if it is unique and false otherwise
	 * @throws PersistenceException - if there was an issue with persistence
	 */
	public boolean isBusinessNumberUnique(String businessNumber) throws PersistenceException {
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
			        .prepareStatement("SELECT * from group_data WHERE business_id_number=?");
			
		
			// execute the statement 
			preparedSELECTstatement.setString(1, businessNumber);
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

}
