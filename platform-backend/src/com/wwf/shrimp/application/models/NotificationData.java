package com.wwf.shrimp.application.models;


/**
 * Details about a notification. Specifically who created it, 
 * its timestamp, its type etc...
 * 
 * @author AleaActaEst
 *
 */
public class NotificationData extends IdentifiableEntity{
	
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
	public String getCreationTimestamp() {
		return creationTimestamp;
	}
	public void setCreationTimestamp(String creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}
	public String getNotificationTimestamp() {
		return notificationTimestamp;
	}
	public void setNotificationTimestamp(String notificationTimestamp) {
		this.notificationTimestamp = notificationTimestamp;
	}
	public AuditEntity getAuditData() {
		return auditData;
	}
	public void setAuditData(AuditEntity auditData) {
		this.auditData = auditData;
	}
	public String getNotificationText() {
		return notificationText;
	}
	public void setNotificationText(String notificationText) {
		this.notificationText = notificationText;
	}
	public String getNotificationDescription() {
		return notificationDescription;
	}
	public void setNotificationDescription(String notificationDescription) {
		this.notificationDescription = notificationDescription;
	}
	
	private User user;
	private NotificationType notificationType;
	private String creationTimestamp;
	private String notificationTimestamp;
	private AuditEntity auditData;
	private String notificationText;
	private String notificationDescription;
}
