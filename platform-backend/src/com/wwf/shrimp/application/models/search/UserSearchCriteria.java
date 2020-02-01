package com.wwf.shrimp.application.models.search;

import com.wwf.shrimp.application.models.Role;

/**
 * The search criteria used to search for users.
 * All elements are treated as "AND" and can be null.
 * 
 * @author AleaActaEst
 *
 */
public class UserSearchCriteria extends BaseSearchCriteria {
	
	/**
	 * username to search by
	 */
	private String userName;
	/**
	 * Specific role to search by
	 */
	private Role role;
	/**
	 * Search by organization id
	 */
	private long organizationId;
	/**
	 * Search by group id
	 */
	private long groupId;
	
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public long getOrganizationId() {
		return organizationId;
	}
	public void setOrganizationId(long organizationId) {
		this.organizationId = organizationId;
	}
	public long getGroupId() {
		return groupId;
	}
	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

}
