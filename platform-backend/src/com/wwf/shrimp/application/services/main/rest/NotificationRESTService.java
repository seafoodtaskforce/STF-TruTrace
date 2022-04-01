package com.wwf.shrimp.application.services.main.rest;

import java.io.InputStream;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.wwf.shrimp.application.models.AuditAction;
import com.wwf.shrimp.application.models.AuditEntity;
import com.wwf.shrimp.application.models.AuditUserType;
import com.wwf.shrimp.application.models.Document;
import com.wwf.shrimp.application.models.NotificationData;
import com.wwf.shrimp.application.models.NotificationDataItemType;
import com.wwf.shrimp.application.models.NotificationScope;
import com.wwf.shrimp.application.models.NotificationType;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.search.AuditSearchCriteria;
import com.wwf.shrimp.application.models.search.DocumentSearchCriteria;
import com.wwf.shrimp.application.models.search.NotificationSearchCriteria;
import com.wwf.shrimp.application.models.search.UserSearchCriteria;
import com.wwf.shrimp.application.services.main.BaseRESTService;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.AuditMySQLDao;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.DocumentMySQLDao;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.NotificationMySQLDao;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.UserMySQLDao;
import com.wwf.shrimp.application.utils.DateUtility;
import com.wwf.shrimp.application.utils.RESTUtility;

/**
 * General Notification based RESTful service which will have the functionality management of tag data for documents.  
 * 
 * <TODO> Change the verbs to be fully restful
 * @author AleaActaEst
 *
 */
@Path("/notification")

public class NotificationRESTService extends BaseRESTService {
	/**
	 * Services used by the implementation
	 */
	private NotificationMySQLDao<NotificationData, NotificationSearchCriteria> notificationService = new NotificationMySQLDao<NotificationData, NotificationSearchCriteria>();
	private UserMySQLDao<User, UserSearchCriteria> userService = new UserMySQLDao<User, UserSearchCriteria>();
	private AuditMySQLDao<AuditEntity, AuditSearchCriteria> auditService = new AuditMySQLDao<AuditEntity, AuditSearchCriteria>();
	private DocumentMySQLDao<Document, DocumentSearchCriteria> documentService = new DocumentMySQLDao<Document, DocumentSearchCriteria>();
	
	
	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will create a new notification in the database
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the Notification entity. Currently this is expected to be empty
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated audit record.
	 *     2. Error String if there was an issue
	 */
	public Response create(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("SYSTEM") @HeaderParam("notification-scope") String notificationScope,
			@DefaultValue("") @HeaderParam("notification-text") String notificationText,
			@DefaultValue("") @HeaderParam("notification-description") String notificationDescription,
			@DefaultValue("") @HeaderParam("user-target") String userTarget,
			@DefaultValue("") @HeaderParam("group-target") String groupTarget) {
		
		AuditEntity auditEntity = null;
		User gestureUser = new User();
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		userService.init();
		
		//
		// Process any notification creation
		
		
		// get all the group members for this user to whom the notifications should be sent
		try {
			gestureUser = userService.getUserByName(userName);
			
			if(notificationScope.equals(NotificationScope.SYSTEM.toString())){
				getLog().info("Creating <SYSTEM> notifications for user: " + userName);
				auditEntity = processSystemNotificationsCreation(notificationScope, notificationText, notificationDescription, gestureUser);
				
			}else if(notificationScope.equals(NotificationScope.INDIVIDUAL.toString())){
				getLog().info("Creating <INDIVIDUAL> notifications for user: " + userName);
				auditEntity = processIndividualNotificationsCreation(notificationScope, notificationText, notificationDescription, gestureUser, userTarget);
			}else if(notificationScope.equals(NotificationScope.GROUP.toString())){
				getLog().info("Creating <GROUP> notifications for user: " + userName);
				auditEntity = processGroupNotificationsCreation(notificationScope, notificationText, notificationDescription, gestureUser, groupTarget);
			}
			
		}catch (Exception e) {
			getLog().error("Error Creating notifications for user: " +userName + " " + e);
		}
		

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(auditEntity)).build();
	}
	
	@GET
	@Path("/fetchall")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all the outstanding notification per input user
	 * @param userName - the unique name of the user for whom the notification are requested
	 * @return - the list of notifications or empty if non found
	 */
	public Response fetchAllNotifications(
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		// results
		List<NotificationData> allNotifications=null;
		User user = null;
		
		getLog().info("HEADER user-name: " + userName);
		
		//
		// Initialize service
		notificationService.init();
		userService.init();
		auditService.init();
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			user = userService.getUserByName(userName);
			allNotifications = notificationService.getOutstandingNotificationEntitiesByUserId(user.getId());
			// extract full audit data items
			for(int i=0; i< allNotifications.size(); i++){
				AuditEntity audit = null;
				audit = auditService.getAuditEntityById(allNotifications.get(i).getAuditData().getId());
				allNotifications.get(i).setAuditData(audit);
				// set the item data

				//
			    // check if the entity is a document
			    if(NotificationDataItemType.DOCUMENT.equalsNotificationDataItem(audit.getItemType())){
			    	// We have a document ad we will get the item and pack it into the audit data
			    	List<Document> docs = documentService.getDocumentById(documentService.getDocIdBySyncId(audit.getItemId()));
			    	if(docs != null && !docs.isEmpty()) {
			    		allNotifications.get(i).setItem(docs.get(0));
			    	}
			    }
			}
			

		} catch (Exception e) {
			getLog().error("Error Fetching Notifications: - " + e);
		}
		
		getLog().debug("Fetch all Notifications result: " + allNotifications);
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(allNotifications)).build();
	}
	
	@GET
	@Path("/fetchallfiltered")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch notifications through simple filtered criteria
	 * @param userName - the user name for whom we are getting the notifications
	 * @param historyFlag - if true we get all historical records; if false we only get outstanding records.
	 * @param showAll - overriding flag which fetches all notification records regardless of the other flags
	 * @param organizationID - the organization id if we want notifications per organization rather than buy user
	 * @return
	 */
	public Response fetchAllNotificationsFiltered(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("true") @HeaderParam("history") boolean historyFlag,
			@DefaultValue("true") @HeaderParam("show_all") boolean showAll,
			@DefaultValue("") @HeaderParam("organization_id") String organizationID) {
		// results
		List<NotificationData> allNotifications=null;
		User user = null;
		List<User> allUsers = null;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("HEADER history: " + historyFlag);
		getLog().info("HEADER show_all: " + showAll);
		
		//
		// Initialize service
		notificationService.init();
		userService.init();
		auditService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			// 
			// get all the users for the organization
			user = userService.getUserByName(userName);
			allUsers = userService.getAllUsers(user);
			
			allNotifications = notificationService.getAllNotificationEntitiesByUserList(allUsers);
			// extract full audit data items
			for(int i=0; i< allNotifications.size(); i++){
				AuditEntity audit = null;
				User targetUser = userService.getUserByUserId(allNotifications.get(i).getUser().getId());
				audit = auditService.getAuditEntityById(allNotifications.get(i).getAuditData().getId());
				allNotifications.get(i).setAuditData(audit);
				allNotifications.get(i).setUser(targetUser);
			}
			

		} catch (Exception e) {
			getLog().error("Error Fetching Notifications: - " + e);
		}
		
		getLog().debug("Fetch all Filtered Notifications result: " + allNotifications);
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(allNotifications)).build();
	}
	
	/*****************************************************************************************************
	 * Private Methods
	 */
	
	/**
	 * Process the gesture for a system notification creation 
	 * @param notificationScope - the scope of the notification
	 * @param notificationText - the notification text
	 * @param user - the user on whose behalf the change made
	 * @throws Exception - if there were any issues
	 */
	private AuditEntity processSystemNotificationsCreation(String notificationScope, String notificationText, String notificationDescription, User gestureUser) throws Exception{
		AuditEntity auditEntity = null;
		String itemId = UUID.randomUUID().toString();
		
		// init
		notificationService.init();
		auditService.init();
		userService.init();
		
		//
		// create the audit
		auditEntity = auditRequest(AuditUserType.USER  , AuditAction.NOTIFICATION_INDIVIDUAL, gestureUser, itemId);
		
		//
		// find all the users in the system
		List<User> systemUsers = userService.getAllUsers(gestureUser);
		// remove target
		systemUsers.remove(gestureUser);
		
		for(int i = 0; i< systemUsers.size(); i++){
			// get user
			User target = systemUsers.get(i);
			
			//
			// create the notification
			NotificationData notification = new NotificationData();
			notification.setUser(target);
			notification.setNotificationType(NotificationType.ONE_TIME);
			notification.setAuditData(auditEntity);
			notification.setNotificationText(notificationText);
			notification.setNotificationDescription(notificationDescription);
			notification.setCreationTimestamp(DateUtility.simpleDateFormat(DateUtility.getCurrentDateTime(), DateUtility.FORMAT_DATE_AND_TIME));
			notificationService.create(notification);
			
		}
		
		
		return auditEntity;
	}
	
	/**
	 * Process the gesture for individual notification creation
	 * @param notificationScope - the scope of the notification
	 * @param notificationText - the notification text
	 * @param user - the user on whose behalf the change made
	 * @throws Exception - if there were any issues
	 */
	private AuditEntity processIndividualNotificationsCreation(String notificationScope, String notificationText, String notificationDescription, User gestureUser, String userTarget) throws Exception{
		AuditEntity auditEntity = null;
		String itemId = UUID.randomUUID().toString();
		
		// init
		notificationService.init();
		auditService.init();
		userService.init();
		// get user
		User target = userService.getUserByName(userTarget);
		
		//
		// create the audit
		auditEntity = auditRequest(AuditUserType.USER  , AuditAction.NOTIFICATION_INDIVIDUAL, gestureUser, itemId);
		
		//
		// create the notification
		NotificationData notification = new NotificationData();
		notification.setUser(target);
		notification.setNotificationType(NotificationType.ONE_TIME);
		notification.setAuditData(auditEntity);
		notification.setNotificationText(notificationText);
		notification.setNotificationDescription(notificationDescription);
		notification.setCreationTimestamp(DateUtility.simpleDateFormat(DateUtility.getCurrentDateTime(), DateUtility.FORMAT_DATE_AND_TIME));
		notificationService.create(notification);
		
		return auditEntity;
	}
	
	/**
	 * Process the gesture on a group notification 
	 * @param notificationScope - the scope of the notification
	 * @param notificationText - the notification text
	 * @param user - the user on whose behalf the change made
	 * @throws Exception - if there were any issues
	 */
	private AuditEntity processGroupNotificationsCreation(String notificationScope, String notificationText, String notificationDescription, User gestureUser, String groupTarget) throws Exception{
		AuditEntity auditEntity = null;
		String itemId = UUID.randomUUID().toString();
		
		// init
		notificationService.init();
		auditService.init();
		userService.init();
		
		//
		// create the audit
		auditEntity = auditRequest(AuditUserType.USER  , AuditAction.NOTIFICATION_INDIVIDUAL, gestureUser, itemId);
		
		//
		// find all the users in the system
		List<User> systemUsers = userService.getAllUsers(gestureUser);
		// remove target
		systemUsers.remove(gestureUser);
		
		for(int i = 0; i< systemUsers.size(); i++){
			// get user
			User target = systemUsers.get(i);
			if(target.getUserGroups().get(0).getName().equals(groupTarget)){
				//
				// create the notification
				NotificationData notification = new NotificationData();
				notification.setUser(target);
				notification.setNotificationType(NotificationType.ONE_TIME);
				notification.setAuditData(auditEntity);
				notification.setNotificationText(notificationText);
				notification.setNotificationDescription(notificationDescription);
				notification.setCreationTimestamp(DateUtility.simpleDateFormat(DateUtility.getCurrentDateTime(), DateUtility.FORMAT_DATE_AND_TIME));
				notificationService.create(notification);
			}
		}
		
		
		return auditEntity;
	}
	
	/**
	 * Helper method to audit a given action by the user
	 * @param userType - the user type
	 * @param action - the type of action 
	 * @param actor - the specific user/actor who did the gesture
	 * @param itemId - the it of the given item which was being audited
	 * @return - the entity representing the persisted audit
	 * @throws Exception - any exception that was thrown if there were issues
	 */
	private AuditEntity auditRequest(AuditUserType userType, AuditAction action, User actor, String itemId) throws Exception {
		AuditEntity newAuditEntity = new AuditEntity();
		auditService.init();
		
		// actor name
		newAuditEntity.setActor(actor);
		// user type
		newAuditEntity.setUserType(userType);
		// action
		newAuditEntity.setAction(action);
		// item type
		newAuditEntity.setItemType("Notification");
		// item id
		newAuditEntity.setItemId(itemId);
		// creation time stamp
		newAuditEntity.setTimestamp(DateUtility.simpleDateFormat(DateUtility.getCurrentDateTime(), DateUtility.FORMAT_DATE_AND_TIME));
		
		// execute
		newAuditEntity = auditService.create(newAuditEntity);
		
		// return results
		return newAuditEntity;
	}
	
	
}
