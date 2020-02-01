package com.wwf.shrimp.application.models;

/**
 * 
 * @author argolite
 * 
 * The type of notification. It can be RECURRING which would be repeating until it is stopped
 * It could be ONE_TIME which is a singular occurrence notification and a REQUIRES_ACK which 
 * would only be turned off once it has been acknowledged.
 *
 */
public enum NotificationType {
	RECURRING("RECURRING"), 
	ONE_TIME("ONE_TIME"),
	REQUIRES_ACK("REQUIRES_ACK");

	
	/**
	 * THe notification type 
	 */
	private final String notificationType;
	
	NotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
	
	public boolean equalsUserType(String otherNotificationType) {
        // (otherAction == null) check is not needed because action.equals(null) returns false 
        return notificationType.equals(otherNotificationType);
    }

    public String toString() {
       return this.notificationType;
    }

}
