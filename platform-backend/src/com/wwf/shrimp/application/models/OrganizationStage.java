package com.wwf.shrimp.application.models;

/**
 * @author user
 *
 */
public class OrganizationStage extends LookupEntity {
	
	private int orderIndex;
	private long orgID;
	private String colorHexCode;
	
	public int getOrderIndex() {
		return orderIndex;
	}
	public void setOrderIndex(int orderIndex) {
		this.orderIndex = orderIndex;
	}
	public long getOrgID() {
		return orgID;
	}
	public void setOrgID(long orgID) {
		this.orgID = orgID;
	}
	public String getColorHexCode() {
		return colorHexCode;
	}
	public void setColorHexCode(String colorHexCode) {
		this.colorHexCode = colorHexCode;
	}

}
