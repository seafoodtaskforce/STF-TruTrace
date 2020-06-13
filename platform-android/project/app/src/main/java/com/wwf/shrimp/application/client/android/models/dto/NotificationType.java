package com.wwf.shrimp.application.client.android.models.dto;

/**
 * Created by user on 21/03/2018.
 */

public enum NotificationType {
    RECURRING("RECURRING"),
    ONE_TIME("ONE_TIME"),
    REQUIRES_ACK("REQUIRES_ACK");


    /**
     * The notification type
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
