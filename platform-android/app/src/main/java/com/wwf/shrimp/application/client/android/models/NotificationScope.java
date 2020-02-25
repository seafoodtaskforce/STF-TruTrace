package com.wwf.shrimp.application.client.android.models;

/**
 * Notification scope enumeration
 */
public enum NotificationScope {
    INDIVIDUAL("INDIVIDUAL"),
    GROUP("GROUP"),
    SYSTEM("SYSTEM");


    /**
     * THe notification type
     */
    private final String notificationScope;

    NotificationScope(String notificationScope) {
        this.notificationScope = notificationScope;
    }

    public boolean equalsUserType(String otherNotificationScope) {
        // (otherAction == null) check is not needed because action.equals(null) returns false
        return notificationScope.equals(otherNotificationScope);
    }

    public String toString() {
        return this.notificationScope;
    }

}
