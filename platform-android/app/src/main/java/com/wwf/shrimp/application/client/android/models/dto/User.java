package com.wwf.shrimp.application.client.android.models.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * The leaf of any organization.
 * 
 * Users inherit the group's roles but can be assigned additional 
 * individual roles as well.
 * 
 * @author AleaActaEst
 *
 */
public class User extends ProfileEntity {
	private UserCredentials credentials;
	private List<Role> roles = new ArrayList<Role>();
	private UserContact contactInfo=new UserContact();
	private String base64ProfileImageData;
	private List<Group> userGroups= new ArrayList<Group>();
	private List<Organization> userOrganizations= new ArrayList<Organization>();
	// resources for this user
	private List<AppResource> appResources = new ArrayList<AppResource>();

	public String getBase64ProfileImageData() {
		return base64ProfileImageData;
	}

	public void setBase64ProfileImageData(String base64ProfileImageData) {
		this.base64ProfileImageData = base64ProfileImageData;
	}

	/**
	 * @return the credentials
	 */
	public UserCredentials getCredentials() {
		return credentials;
	}
	/**
	 * @param credentials the credentials to set
	 */
	public void setCredentials(UserCredentials credentials) {
		this.credentials = credentials;
	}
	/**
	 * @return the roles
	 */
	public List<Role> getRoles() {
		return roles;
	}
	/**
	 * @param roles the roles to set
	 */
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	/**
	 * @return the contactInfo
	 */
	public UserContact getContactInfo() {
		return contactInfo;
	}
	/**
	 * @param contactInfo the contactInfo to set
	 */

	/**
	 * @return the userGroups
	 */
	public List<Group> getUserGroups() {
		return userGroups;
	}
	/**
	 * @param userGroups the userGroups to set
	 */
	public void setUserGroups(List<Group> userGroups) {
		this.userGroups = userGroups;
	}
	/**
	 * @return the userOrganizations
	 */
	public List<Organization> getUserOrganizations() {
		return userOrganizations;
	}
	/**
	 * @param userOrganizations the userOrganizations to set
	 */
	public void setUserOrganizations(List<Organization> userOrganizations) {
		this.userOrganizations = userOrganizations;
	}

	public List<AppResource> getAppResources() {
		return appResources;
	}

	public void setAppResources(List<AppResource> appResources) {
		this.appResources = appResources;
	}

	public void setContactInfo(UserContact contactInfo) {
		this.contactInfo = contactInfo;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "User [ [credentials=" + credentials + ", roles=" + roles + ", contactInfo=" + contactInfo + ", getId()="
				+ getId() + "]"
				+ super.toString();
	}
}
