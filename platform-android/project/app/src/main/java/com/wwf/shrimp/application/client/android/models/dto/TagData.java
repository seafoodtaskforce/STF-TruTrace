package com.wwf.shrimp.application.client.android.models.dto;

/**
 * Tag strings associated with the document.
 * Tags will always be case insensitive.
 * 
 * @author AleaActaEst
 *
 */
public class TagData extends IdentifiableEntity {
	private String text;
	private boolean custom = false;
	private String customPrefix;
	private long organizationId;

	/**
	 * @return the text
	 */
	public String getText() {
		//if(text != null && isCustom() && !text.contains(customPrefix)){
		//	text = customPrefix + " " + text;
		//}
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	public boolean isCustom() {
		return custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}

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
}
