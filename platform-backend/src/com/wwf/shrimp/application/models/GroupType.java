package com.wwf.shrimp.application.models;

import java.util.ArrayList;
import java.util.List;

/**
 * THe group type used to determine the generic aspect of a group.
 * It comes with a color code (if needed) and and order within other group types
 * @author AleaActaEst
 *
 */
public class GroupType extends LookupEntity {
	
	private String hexColorCode;
	private int orderIndex;
	private List<DocumentType> allowedDocTypes = new ArrayList<DocumentType>();
	private long matrixId;
	private long[] associatedStageIds;

	/**
	 * @return the orderIndex
	 */
	public int getOrderIndex() {
		return orderIndex;
	}

	/**
	 * @param orderIndex the orderIndex to set
	 */
	public void setOrderIndex(int orderIndex) {
		this.orderIndex = orderIndex;
	}

	public String getHexColorCode() {
		return hexColorCode;
	}

	public void setHexColorCode(String hexColorCode) {
		this.hexColorCode = hexColorCode;
	}
	
	public List<DocumentType> getAllowedDocTypes() {
		return allowedDocTypes;
	}

	public void setAllowedDocTypes(List<DocumentType> allowedDocTypes) {
		this.allowedDocTypes = allowedDocTypes;
	}

	public long getMatrixId() {
		return matrixId;
	}

	public void setMatrixId(long matrixId) {
		this.matrixId = matrixId;
	}

	public long[] getAssociatedStageIds() {
		return associatedStageIds;
	}

	public void setAssociatedStageIds(long[] associatedStageIds) {
		this.associatedStageIds = associatedStageIds;
	}
}
