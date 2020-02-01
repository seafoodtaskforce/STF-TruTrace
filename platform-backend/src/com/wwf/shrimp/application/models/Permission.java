package com.wwf.shrimp.application.models;

import java.util.Date;


/**
 * Individual permission datum which holds information about type, scope, and expiry date.
 * @author AleaActaEst
 *
 */
public class Permission extends IdentifiableEntity {
	
	private PermissionType type;
	private Date expiryDate;
	private PermissionScopeType scope;
	/**
	 * @return the type
	 */
	public PermissionType getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(PermissionType type) {
		this.type = type;
	}
	/**
	 * @return the expiryDate
	 */
	public Date getExpiryDate() {
		return expiryDate;
	}
	/**
	 * @param expiryDate the expiryDate to set
	 */
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
	/**
	 * @return the scope
	 */
	public PermissionScopeType getScope() {
		return scope;
	}
	/**
	 * @param scope the scope to set
	 */
	public void setScope(PermissionScopeType scope) {
		this.scope = scope;
	}
	
	
	

}
