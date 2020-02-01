package com.wwf.shrimp.application.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-Level (root) of a hierarchy of profiles that 
 * would make up an organization.
 * It will contain 1 or more groups and will have a number 
 * of roles which will be automatically provided to the 
 * lower level groups.
 * 
 * For example if we provide a role of "Submitter" to the 
 * Organization then each group will inherit this role and 
 * its permissions.
 * 
 * @author AleaActaEst
 *
 */
public class Organization extends ProfileEntity {
	/**
	 * A hierarchy of groups.
	 * An organization must have at least one group 
	 * with at least one user to be valid.
	 */
	private List<Group> subGroups = new ArrayList<Group>();
	
	/**
	 * A list of users at the level of the organization.
	 * These users would have their roles/permissions transcend 
	 * all the groups and in fact would have their permissions 
	 * apply to any and all sub-groups in this organization
	 */
	private List<User> users = new ArrayList<User>();
	
	/**
	 * @return the subGroups
	 */
	public List<Group> getSubGroups() {
		return subGroups;
	}
	/**
	 * @param subGroups the subGroups to set
	 */
	public void setSubGroups(List<Group> subGroups) {
		this.subGroups = subGroups;
	}
	/**
	 * @return the users
	 */
	public List<User> getUsers() {
		return users;
	}
	/**
	 * @param users the users to set
	 */
	public void setUsers(List<User> users) {
		this.users = users;
	}
}
