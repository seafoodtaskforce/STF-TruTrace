package com.wwf.shrimp.application.client.android.models.dto;

/**
 * The specific action being audited
 * @author AleaActaEst
 *
 */
public enum AuditAction {
    DOCUMENT_READ("DOCUMENT_READ"),
    DOCUMENT_CREATE("DOCUMENT_CREATE"),
    DOCUMENT_EDIT("DOCUMENT_EDIT"),
    DOCUMENT_DELETE("DOCUMENT_DELETE"),
    DOCUMENT_USER_TAG("DOCUMENT_USER_TAG"),
    DOCUMENT_SUBMIT("DOCUMENT_SUBMIT"),
    DOCUMENT_REJECT("DOCUMENT_REJECT"),
    DOCUMENT_ACCEPT("DOCUMENT_ACCEPT"),
    DOCUMENT_RESUBMIT("DOCUMENT_RESUBMIT"),
    DOCUMENT_PENDING("DOCUMENT_PENDING"),
    USER_LOGIN("USER_LOGIN"),
    USER_LOGOUT("USER_LOGOUT"),
    NOTIFICATION_SYSTEM("NOTIFICATION_SYSTEM"),
    NOTIFICATION_INDIVIDUAL("NOTIFICATION_INDIVIDUAL"),
    NOTIFICATION_GROUP("NOTIFICATION_GROUP");

    /**
     * THe action being audited
     */
    private final String action;

    AuditAction(String action) {
        this.action = action;
    }

    public boolean equalsAction(String otherAction) {
        // (otherAction == null) check is not needed because action.equals(null) returns false
        return action.equals(otherAction);
    }

    public String toString() {
        return this.action;
    }
}

