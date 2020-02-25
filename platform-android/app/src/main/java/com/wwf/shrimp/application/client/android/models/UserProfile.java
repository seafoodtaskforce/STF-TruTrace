package com.wwf.shrimp.application.client.android.models;

import com.wwf.shrimp.application.client.android.models.dto.IdentifiableEntity;

import java.util.Date;

/**
 * User Profile Data
 *
 * @author AleaActaEst
 */
public class UserProfile extends IdentifiableEntity{

    //
    // member variables
    private String username;
    private String password;
    private String role;
    private String macAddress;
    private String email;
    private Date lastLogin;
    private String fullName;
    private SecurityToken authToken;

    //
    // setters and getters
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public SecurityToken getAuthToken() {
        return authToken;
    }
    public void setAuthToken(SecurityToken authToken) {
        this.authToken = authToken;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getMacAddress() {
        return macAddress;
    }
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", email='" + email + '\'' +
                ", lastLogin=" + lastLogin +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}

