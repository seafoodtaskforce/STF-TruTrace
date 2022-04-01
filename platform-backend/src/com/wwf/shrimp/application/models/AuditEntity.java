package com.wwf.shrimp.application.models;

/**
 * THe audit data for any audit happening in the system.
 * @author AkeaActEst
 *
 */
public class AuditEntity extends IdentifiableEntity {
	
	/**
	 * @return the actor
	 */
	public User getActor() {
		return actor;
	}
	/**
	 * @param actor the actor to set
	 */
	public void setActor(User actor) {
		this.actor = actor;
	}
	/**
	 * @return the userTYpe
	 */
	public AuditUserType getUserType() {
		return userType;
	}
	/**
	 * @param userTYpe the userTYpe to set
	 */
	public void setUserType(AuditUserType userType) {
		this.userType = userType;
	}
	/**
	 * @return the action
	 */
	public AuditAction getAction() {
		return action;
	}
	/**
	 * @param action the action to set
	 */
	public void setAction(AuditAction action) {
		this.action = action;
	}
	/**
	 * @return the itemType
	 */
	public String getItemType() {
		return itemType;
	}
	/**
	 * @param itemType the itemType to set
	 */
	public void setItemType(String itemType) {
		this.itemType = itemType;
	}
	/**
	 * @return the itemId
	 */
	public String getItemId() {
		return itemId;
	}
	/**
	 * @param itemId the itemId to set
	 */
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}
	/**
	 * @param fieldName the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	/**
	 * @return the prevValue
	 */
	public String getPrevValue() {
		return prevValue;
	}
	/**
	 * @param prevValue the prevValue to set
	 */
	public void setPrevValue(String prevValue) {
		this.prevValue = prevValue;
	}
	/**
	 * @return the newValue
	 */
	public String getNewValue() {
		return newValue;
	}
	/**
	 * @param newValue the newValue to set
	 */
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	/**
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	private User actor;
	private AuditUserType userType;
	private AuditAction action;
	private String itemType;
	private String itemId;
	private String fieldName;
	private String prevValue;
	private String newValue;
	private String timestamp;


}
