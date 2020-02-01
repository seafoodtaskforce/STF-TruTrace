package com.wwf.shrimp.application.models;

/**
 * Tag strings associated with the document.
 * Tags will always be case insensitive.
 * 
 * @author AleaActaEst
 *
 */
public class TagData extends IdentifiableEntity {


	@Override
	public String toString() {
		return "TagData [text=" + text + ", customPrefix=" + customPrefix + ", organizationId=" + organizationId
				+ ", custom=" + custom + ", owner=" + owner + "]";
	}

	private String text;
	private String customPrefix;
	private long organizationId; 
	private boolean custom = false;
	private String owner = null;
	
	public String getCustomPrefix() {
		return customPrefix;
	}

	public void setCustomPrefix(String customPrefix) {
		this.customPrefix = customPrefix;
	}

	public long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(long organizationId) {
		this.organizationId = organizationId;
	}
	
	public boolean isCustom() {
		return custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
}
