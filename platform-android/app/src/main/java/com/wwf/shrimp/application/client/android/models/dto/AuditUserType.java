package com.wwf.shrimp.application.client.android.models.dto;

/**
 * THe types of audit sources which at this point could be either a user or the system itself.
 * An example would be a login from a user or a PUSH notification from a system
 * @author user
 *
 */
public enum AuditUserType {
    SYSTEM("SYSTEM"),
    USER("USER");


    /**
     * The action being audited
     */
    private final String userType;

    AuditUserType(String userType) {
        this.userType = userType;
    }

    public boolean equalsUserType(String otherUserType) {
        // (otherAction == null) check is not needed because action.equals(null) returns false
        return userType.equals(otherUserType);
    }

    public String toString() {
        return this.userType;
    }
}
