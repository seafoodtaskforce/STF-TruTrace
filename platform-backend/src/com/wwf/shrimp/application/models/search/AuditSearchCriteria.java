package com.wwf.shrimp.application.models.search;

import com.wwf.shrimp.application.models.AuditAction;
import com.wwf.shrimp.application.models.AuditUserType;
import com.wwf.shrimp.application.models.User;

/**
 * The search criteria used to search for audit entries.
 * All elements are treated as "AND" and can be null.
 * 
 * @author argolite
 *
 */
public class AuditSearchCriteria extends BaseSearchCriteria {
	
	public User getActor() {
		return actor;
	}
	public void setActor(User actor) {
		this.actor = actor;
	}
	public AuditUserType getUserTYpe() {
		return userTYpe;
	}
	public void setUserTYpe(AuditUserType userTYpe) {
		this.userTYpe = userTYpe;
	}
	public AuditAction getAction() {
		return action;
	}
	public void setAction(AuditAction action) {
		this.action = action;
	}
	private User actor;
	private AuditUserType userTYpe;
	private AuditAction action;

}
