package com.wwf.shrimp.application.models;

/**
 * 
 * @author argolite
 * 
 * The type of data item for this notification. It can be null (i.e. no data item)
 * or a specific item of data that could be further extracted from the notification.
 *
 */
public enum NotificationDataItemType {
	DOCUMENT("Document");

	
	/**
	 * THe notification type 
	 */
	private final String notificationDataItemType;
	
	NotificationDataItemType(String notificationDataItemType) {
        this.notificationDataItemType = notificationDataItemType;
    }
	
	public boolean equalsNotificationDataItem(String otherNotificationDataItemType) {
        // (otherAction == null) check is not needed because action.equals(null) returns false 
        return notificationDataItemType.equals(otherNotificationDataItemType);
    }

    public String toString() {
       return this.notificationDataItemType;
    }
}

