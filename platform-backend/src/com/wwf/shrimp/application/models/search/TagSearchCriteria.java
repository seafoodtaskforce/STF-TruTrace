package com.wwf.shrimp.application.models.search;


/**
 * The search criteria used to search for document tags.
 * All elements are treated as "AND" and can be null. * 
 * 
 * @author AleaActaEst
 *
 */
public class TagSearchCriteria extends BaseSearchCriteria {
	
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getTargetType() {
		return targetType;
	}
	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}
	private String tag;
	private String targetType;

}
