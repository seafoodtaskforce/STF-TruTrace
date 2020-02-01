package com.wwf.shrimp.application.models;

/**
 * The scope of a notification.
 * 
 * - INDIVIDUAL: means that the notification is sent to a single individual.
 * - GROUP: is sent to a group @see Group 
 * - SYSTEM: is a system wide notification
 * 
 * @author argolite
 *
 */
public enum NotificationScope {
	INDIVIDUAL("INDIVIDUAL"), 
	GROUP("GROUP"),
	SYSTEM("SYSTEM");

	
	/**
	 * THe notification scope 
	 */
	private final String notificationScope;
	
	NotificationScope(String notificationScope) {
        this.notificationScope = notificationScope;
    }
	
	public boolean equalsNotificationScope(String otherNotificationScope) {
        // (otherAction == null) check is not needed because action.equals(null) returns false 
        return notificationScope.equals(otherNotificationScope);
    }

    public String toString() {
       return this.notificationScope;
    }

}
