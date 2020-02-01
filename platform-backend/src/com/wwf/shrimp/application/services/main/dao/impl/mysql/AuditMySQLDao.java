package com.wwf.shrimp.application.services.main.dao.impl.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mysql.cj.api.jdbc.Statement;
import com.wwf.shrimp.application.exceptions.PersistenceException;
import com.wwf.shrimp.application.exceptions.ServiceManagementException;
import com.wwf.shrimp.application.models.AuditAction;
import com.wwf.shrimp.application.models.AuditEntity;
import com.wwf.shrimp.application.models.AuditUserType;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.search.AuditSearchCriteria;

/**
 * The persistence implementation for Audit entities based on the MySQL database
 * @author AleaActaEst
 *
 * @param <T> - the operational audit entity that this DAO will work with.
 * @param <S> - The specific search criteria for audit entity <T>
 */
public class AuditMySQLDao<T, S> extends BaseMySQLDao<AuditEntity, AuditSearchCriteria> {
	
	public AuditEntity create(AuditEntity newAuditEntity) throws PersistenceException, IllegalArgumentException { 
		PreparedStatement preparedINSERTstatement;
		long returnId=0;
		
		// get the connection
		Connection conn = openConnection();
		
	
		// create the query
		String insertQuery = " insert into audit_data ("
				+ "actor_name, "
				+ "actor_type, "
				+ "action, "
				+ "item_type, "
				+ "item_id, "
				+ "timestamp "
				+ ")"
		        + " values (?,?,?,?,?,?)";
		
		// create the statement
		try {
			preparedINSERTstatement = conn
			        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			
			// execute the statement 
			preparedINSERTstatement.setString(1, newAuditEntity.getActor().getName());
			preparedINSERTstatement.setString(2, newAuditEntity.getUserType().toString());
			preparedINSERTstatement.setString(3, newAuditEntity.getAction().toString());
			preparedINSERTstatement.setString(4, newAuditEntity.getItemType());
			preparedINSERTstatement.setString(5, newAuditEntity.getItemId());
			preparedINSERTstatement.setString(6, newAuditEntity.getTimestamp());
			
			
			preparedINSERTstatement.executeUpdate();
			
			// get the id of the created 
			ResultSet rs = preparedINSERTstatement.getGeneratedKeys();
			if (rs.next()){
				returnId=rs.getLong(1);
			}
		} catch (SQLException e) {
			// re-throw
			throw new PersistenceException(e.getMessage());
		}

		// release the connection
		closeConnection(conn);
		
		// return the result
		newAuditEntity.setId(returnId);
		getLog().info("CREATE AUDIT: " + newAuditEntity);
		
		//
		// Process any notification creation
		// TODO
		
		return newAuditEntity;
	}
	
	
	/**
	 * Get the list of all available audit entities.
	 * @return - the list of audit entities
	 * @throws Exception - the exception thrown if
	 * 			- there were any issues with fetching the requested data
	 */
	public List<AuditEntity> getAllAuditEntities() throws PersistenceException {
		List<AuditEntity> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		try {
			preparedSELECTstatement = conn
			        .prepareStatement("SELECT * from audit_data");
			
			// execute the statement 
	        resultSet = preparedSELECTstatement.executeQuery();
		} catch (SQLException e) {
			// re-throw
			throw new PersistenceException(e.getMessage());
		}
		
		
        
        //
        // process the result
        result = extractAuditEntitiesFromResult(resultSet);

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result;
				
	}
	
	/**
	 * Fetch the AuditEntity by specific audit entity id.
	 * @param id - the id of the entity
	 * @return - the found instance of the entity or null if not found
	 * @throws Exception
	 */
	public AuditEntity getAuditEntityById(long id) throws PersistenceException {
		List<AuditEntity> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		try {
			preparedSELECTstatement = conn
			        .prepareStatement("SELECT * from audit_data WHERE id=?");
			
			// execute the statement 
			preparedSELECTstatement.setLong(1, id);
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        //
	        // process the result
	        result = extractAuditEntitiesFromResult(resultSet);
	        
		} catch (SQLException e) {
			// re-throw
			throw new PersistenceException(e.getMessage());
		}

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result.get(0);
				
	}
	
	/**
	 * Fetch the AuditEntity by specific item id which is a cross-reference to the audit entity by the 
	 * if of the item that it audits
	 * @param itemId - the id of the item that was audited
	 * @param itemType - the specific item type
	 * @param action - the action of the audit
	 * @return - the matched audit entity if found and null otherwise
	 * @throws PersistenceException - if there were any issues with this transaction
	 */
	public AuditEntity getAuditEntityByItemId(String itemId, String itemType, String action) throws PersistenceException {
		List<AuditEntity> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		try {
			preparedSELECTstatement = conn
			        .prepareStatement("SELECT * from audit_data WHERE "
			           					+ " item_id=? AND "
			        					+ " item_type=? AND "
			        					+ " action=?"
			        		);
			
			// execute the statement 
			preparedSELECTstatement.setString(1, itemId);
			preparedSELECTstatement.setString(2, itemType);
			preparedSELECTstatement.setString(3, action);
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        //
	        // process the result
	        result = extractAuditEntitiesFromResult(resultSet);
		} catch (SQLException e) {
			// re-throw
			throw new PersistenceException(e.getMessage());
		}
		
		

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result.get(0);
				
	}
	
	
	
	/**********************************************************************
	 * Private helper methods
	 */

	
	/**
	 * Helper method to convert audit row data into an entity based data representation.
	 * @param resultSet - the result set of the SQL Query with record data
	 * @return - the list of converted data entities or an empty list if none were found
	 * @throws PersistenceException - if there was an issue  with the operation
	 */
	private List<AuditEntity> extractAuditEntitiesFromResult(ResultSet resultSet) throws PersistenceException {
		List<AuditEntity> auditEntities= new ArrayList<AuditEntity>();
		
		// process the extractions - there will only be one user
		//
		try {
			while (resultSet.next()) {
				// extract the data
				long id = resultSet.getLong("id");
			    String actorName = resultSet.getString("actor_name");
			    String actorType = resultSet.getString("actor_type");
			    String action = resultSet.getString("action");
			    String itemType = resultSet.getString("item_type");
			    String itemId = resultSet.getString("item_id");
			    String field = resultSet.getString("field");
			    String previousValue = resultSet.getString("previous_value");
			    String newValue = resultSet.getString("new_value");
			    String timestamp = resultSet.getString("timestamp");
			    
			    // additional data
			    User user = new User();
			    user.setName(actorName);
			    
			    // Create a new audit
			    AuditEntity audit = new AuditEntity();
			    
			    
			    // populate the audit entity
			    audit.setId(id);
			    audit.setActor(user);
			    audit.setUserType(AuditUserType.valueOf(actorType.toUpperCase(Locale.ENGLISH)));
			    audit.setAction(AuditAction.valueOf(action.toUpperCase(Locale.ENGLISH)));
			    audit.setItemType(itemType);
			    audit.setItemId(itemId);
			    audit.setFieldName(field);
			    audit.setPrevValue(previousValue);
			    audit.setNewValue(newValue);
			    audit.setTimestamp(timestamp);
			    
			    // add to the list
			    auditEntities.add(audit);
			}
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
		
		return auditEntities;
	}
	
}
