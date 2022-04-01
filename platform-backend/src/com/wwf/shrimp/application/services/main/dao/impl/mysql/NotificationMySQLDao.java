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
import com.wwf.shrimp.application.models.AuditEntity;
import com.wwf.shrimp.application.models.AuditUserType;
import com.wwf.shrimp.application.models.Document;
import com.wwf.shrimp.application.models.NotificationData;
import com.wwf.shrimp.application.models.NotificationDataItemType;
import com.wwf.shrimp.application.models.NotificationType;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.search.DocumentSearchCriteria;
import com.wwf.shrimp.application.models.search.NotificationSearchCriteria;
import com.wwf.shrimp.application.utils.DateUtility;

/**
 * Notification data DAO implementation
 * 
 * @author argolite
 *
 * @param <T> - the operational entity that this DAO will work with.
 * @param <S> - The specific search criteria for entity <T>
 */
public class NotificationMySQLDao<T, S> extends BaseMySQLDao<NotificationData, NotificationSearchCriteria>  {
	
	/**
	 * Create a new notification instance
	 * 
	 * @param userId - the user.
	 * @return - 
	 * @throws PersistenceException - the exception thrown if
	 * 			- there were any issues with fetching the requested data
	 * @throws IllegalArgumentException - the exception thrown if
	 * 			- illegal data was passed in
	 */
	public NotificationData create(NotificationData newNotification) throws PersistenceException, IllegalArgumentException { 
		PreparedStatement preparedINSERTstatement;
		long returnId=0;
		
		// get the connection
		Connection conn = openConnection();
		
	
		// create the query
		String insertQuery = " insert into notification ("
				+ "userid, "
				+ "notification_type, "
				+ "creation_timestamp, "
				+ "notification_timestamp, "
				+ "auditId,"
				+ "text,"
				+ "description "
				+ ")"
		        + " values (?,?,?,?,?,?,?)";
		
		// create the statement
		try {
			preparedINSERTstatement = conn
			        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			
			// execute the statement 
			preparedINSERTstatement.setLong(1, newNotification.getUser().getId());
			preparedINSERTstatement.setString(2, newNotification.getNotificationType().toString());
			preparedINSERTstatement.setString(3, newNotification.getCreationTimestamp());
			preparedINSERTstatement.setString(4, newNotification.getNotificationTimestamp());
			preparedINSERTstatement.setLong(5, newNotification.getAuditData().getId());
			preparedINSERTstatement.setString(6, newNotification.getNotificationText());
			preparedINSERTstatement.setString(7, newNotification.getNotificationDescription());
			
			
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
		newNotification.setId(returnId);
		getLog().info("CREATE NOTIFICATION: " + newNotification);
		
		return newNotification;
	}
	
	/**
	 * Get the list of all available notification entities.
	 * 
	 * @return - the list of notification entities
	 * @throws Exception - the exception thrown if
	 * 			- there were any issues with fetching the requested data
	 */
	public List<NotificationData> getAllNotificationEntities() throws Exception{
		List<NotificationData> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT * from notification");
		
		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractNotificationEntitiesFromResult(resultSet);

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result;
				
	}
	
	/**
	 * Get the list of all available notification entities for the user ID.
	 * 
	 * @param userId - the user.
	 * @return - the list of notification entities; empty of none found
	 * @throws Exception - the exception thrown if
	 * 			- there were any issues with fetching the requested data
	 */
	public List<NotificationData> getAllNotificationEntitiesByUserId(long userId) throws Exception{
		List<NotificationData> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT * from notification "
                		+ "WHERE userid = ?");
		
		// execute the statement 
		preparedSELECTstatement.setLong(1, userId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractNotificationEntitiesFromResult(resultSet);

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result;
				
	}
	
	/**
	 * Get the list of all available notification entities for the user ID list.
	 * This is a batch operation
	 * 
	 * @param userList - the list of users to fetch against.
	 * @return - the list of notification entities
	 * @throws Exception - the exception thrown if
	 * 			- there were any issues with fetching the requested data
	 */
	public List<NotificationData> getAllNotificationEntitiesByUserList(List<User> userList) throws Exception{
		List<NotificationData> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		StringBuilder builder = new StringBuilder();

		for( int i = 0 ; i < userList.size(); i++ ) {
		    builder.append("?,");
		}

		String stmt = "SELECT * from notification "
        		+ "WHERE userid in (" 
        		+ builder.deleteCharAt( builder.length() -1 ).toString() + ")";
		getLog().info("Statement to Build Notifications by User List: " + stmt);
		preparedSELECTstatement = conn
                .prepareStatement(stmt);
		
		// execute the statement 
		int index = 1;
		for( Object o : userList ) {
			preparedSELECTstatement.setLong(  index++, ((User)o).getId() ); 
		}
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractNotificationEntitiesFromResult(resultSet);

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result;
				
	}
	
	/**
	 * Get the list of all available notification entities for the user ID.
	 * 
	 * @param userId - the user.
	 * @return - the list of notification entities
	 * @throws Exception - the exception thrown if
	 * 			- there were any issues with fetching the requested data
	 */
	public List<NotificationData> getAllNotificationEntitiesByUserIdAndAuditItemId(long userId, String itemId) throws Exception{
		List<NotificationData> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT n.id, n.userid, n.auditId, n.notification_type, n.creation_timestamp, n.notification_timestamp "
                		+ "from notification n "
                		+ "JOIN audit_data ad ON ad.id = n.auditId "
                		+ "WHERE n.userid = ? AND ad.item_id = ?");
		
		// execute the statement 
		preparedSELECTstatement.setLong(1, userId);
		preparedSELECTstatement.setString(2, itemId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractNotificationEntitiesFromResult(resultSet);

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result;
				
	}
	
	/**
	 * Get the list of all available notification entities for the user ID.
	 * It will also ACK those entities when the request was made.
	 * 
	 * @param userId - the user 
	 * @return - the list of notification entities
	 * @throws Exception - the exception thrown if
	 * 			- there were any issues with fetching the requested data
	 */
	public List<NotificationData> getOutstandingNotificationEntitiesByUserId(long userId) throws Exception {
		List<NotificationData> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT * from notification "
                		+ "WHERE userid = ? "
                		+ "AND notification_timestamp IS NULL");
		
		// execute the statement 
		preparedSELECTstatement.setLong(1, userId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractNotificationEntitiesFromResult(resultSet);

		// release the connection
		closeConnection(conn);
		
		// Ack those notifications as well
		ackNotificationEntitiesByUserId(userId);
		
				
		// return the result
		return result;
				
	}
	
	
	
	/**********************************************************************
	 * Private helper methods
	 */
	
	/**
	 * Acknowledge the receiving of a notification.
	 * 
	 * @param userId - the user who acknowledged
	 * @throws Exception - if there were any issues
	 */
	private void ackNotificationEntitiesByUserId(long userId) throws Exception {
		PreparedStatement preparedUPDATEStatement;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedUPDATEStatement = conn
                .prepareStatement("UPDATE notification "
                		+ "SET notification_timestamp = ? "
                		+ "WHERE userid = ? "
                		+ "AND notification_timestamp IS NULL");
		
		// execute the statement 
		preparedUPDATEStatement.setLong(2, userId);
		preparedUPDATEStatement.setString(1, DateUtility.simpleDateFormat(DateUtility.getCurrentDateTime(), DateUtility.FORMAT_DATE_AND_TIME));
		// execute the java preparedstatement
		preparedUPDATEStatement.executeUpdate();
        

		// release the connection
		closeConnection(conn);
		
	}

	/**
	 * Extract Notification data from SQL result set
	 * 
	 * @param resultSet - the results set with notification data
	 * @return - a list of notifications; empty of there were none
	 * @throws Exception - if there were issues
	 */
	private List<NotificationData> extractNotificationEntitiesFromResult(ResultSet resultSet) throws Exception {
		List<NotificationData> notificationEntities= new ArrayList<NotificationData>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            long userId = resultSet.getLong("userid");
            String notificationType = resultSet.getString("notification_type");
            String creationTimestamp = resultSet.getString("creation_timestamp");
            String notificationTimestamp = resultSet.getString("notification_timestamp");
            String notificationText = resultSet.getString("text");
            String notificationDescription= resultSet.getString("description");
            long auditId = resultSet.getLong("auditId");
            
            // additional data
            User user = new User();
            user.setId(userId);
            AuditEntity auditData = new AuditEntity();
            auditData.setId(auditId);

            // Create a new notification
            NotificationData notification = new NotificationData();
            
            
            // populate the notification entity
            notification.setId(id);
            notification.setUser(user);
            notification.setNotificationType(NotificationType.valueOf(notificationType.toUpperCase(Locale.ENGLISH)));
            // AuditUserType.valueOf(actorType.toUpperCase(Locale.ENGLISH)
            notification.setCreationTimestamp(creationTimestamp);
            notification.setNotificationTimestamp(notificationTimestamp);
            notification.setAuditData(auditData);
            notification.setNotificationText(notificationText);
            notification.setNotificationDescription(notificationDescription);
              
            // add to the list
            notificationEntities.add(notification);
        }
		
		return notificationEntities;
	}


}
