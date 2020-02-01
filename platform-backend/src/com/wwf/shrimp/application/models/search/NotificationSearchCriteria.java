package com.wwf.shrimp.application.models.search;

import com.wwf.shrimp.application.models.AuditEntity;
import com.wwf.shrimp.application.models.NotificationType;
import com.wwf.shrimp.application.models.User;

/**
 *  * The search criteria used to search for notifications.
 * All elements are treated as "AND" and can be null.
 * 
 * @author AleaActaEst
 *
 */
public class NotificationSearchCriteria extends BaseSearchCriteria {

	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public NotificationType getNotificationType() {
		return notificationType;
	}
	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}
	public AuditEntity getAuditData() {
		return auditData;
	}
	public void setAuditData(AuditEntity auditData) {
		this.auditData = auditData;
	}
	public String getNotificationTimeStamp() {
		return notificationTimeStamp;
	}
	public void setNotificationTimeStamp(String notificationTimeStamp) {
		this.notificationTimeStamp = notificationTimeStamp;
	}
	public String getCreationTimeStamp() {
		return creationTimeStamp;
	}
	public void setCreationTimeStamp(String creationTimeStamp) {
		this.creationTimeStamp = creationTimeStamp;
	}
	private User user;
	private NotificationType notificationType;
	private AuditEntity auditData;
	private String notificationTimeStamp;
	private String creationTimeStamp;
	
}
