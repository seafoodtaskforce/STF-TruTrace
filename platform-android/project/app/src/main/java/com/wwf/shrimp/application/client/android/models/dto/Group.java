package com.wwf.shrimp.application.client.android.models.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * The group hierarchy will hold either 
 * subgroups or users or both.
 *
 * It will contain 1 or more groups (and 1 or more users) 
 * and will have a number of roles which will be automatically 
 * provided to the lower level groups and users as well as this level.
 * In other words roles (and their permissions) are inherited to all
 * sub-levels
 * 
 * For example if we provide a role of "Submitter" to the Group 
 * then each sub-group and user will inherit this.
 *
 * NOTE that a group may contain 0 roles.
 * 
 * @author AleaActaEst
 *
 */
public class Group extends ProfileEntity {
	private long parentId = 0;
	private long childId = 0;
	private long organizationId = 0;
	private GroupType groupType;

	private List<Group> subGroups = new ArrayList<Group>();
	private List<User> users = new ArrayList<User>();
	private List<Role> roles = new ArrayList<Role>();
	private List<DocumentType> allowedDocTypes = new ArrayList<DocumentType>();
	
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

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public long getChildId() {
		return childId;
	}

	public void setChildId(long childId) {
		this.childId = childId;
	}

	public long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(long organizationId) {
		this.organizationId = organizationId;
	}

	public GroupType getGroupType() {
		return groupType;
	}

	public void setGroupType(GroupType groupType) {
		this.groupType = groupType;
	}

	public List<DocumentType> getAllowedDocTypes() {
		return allowedDocTypes;
	}

	public void setAllowedDocTypes(List<DocumentType> allowedDocTypes) {
		this.allowedDocTypes = allowedDocTypes;
	}
}
