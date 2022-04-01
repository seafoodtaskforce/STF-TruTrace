package com.wwf.shrimp.application.models;

/**
 * 
 * @author AleaActaEst
 * 
 * This is the actual field data as related to a specific document.
 * for JSON we are avoiding any circular dependency so the Document will aggregate this entity
 * but we will retain the id of the document for any quick read outs if necessary.
 *
 */
public class DynamicFieldData  extends IdentifiableEntity {
	
	private long dynamicFieldDefinitionId;
	private long parentResourceId;
	private String fieldDisplayNameValue;
	private String data;
	
	
	public long getDynamicFieldDefinitionId() {
		return dynamicFieldDefinitionId;
	}
	public void setDynamicFieldDefinitionId(long dynamicFieldDefinitionId) {
		this.dynamicFieldDefinitionId = dynamicFieldDefinitionId;
	}
	public long getParentResourceId() {
		return parentResourceId;
	}
	public void setParentResourceId(long parentResourceId) {
		this.parentResourceId = parentResourceId;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
	
	public String getFieldDisplayNameValue() {
		return fieldDisplayNameValue;
	}
	public void setFieldDisplayNameValue(String fieldDisplayNameValue) {
		this.fieldDisplayNameValue = fieldDisplayNameValue;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + (int) (dynamicFieldDefinitionId ^ (dynamicFieldDefinitionId >>> 32));
		result = prime * result + (int) (parentResourceId ^ (parentResourceId >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DynamicFieldData other = (DynamicFieldData) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (dynamicFieldDefinitionId != other.dynamicFieldDefinitionId)
			return false;
		if (parentResourceId != other.parentResourceId)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "DynamicFieldData [dynamicFieldDefinitionId=" + dynamicFieldDefinitionId + ", parentResourceId="
				+ parentResourceId + ", data=" + data + "]";
	}
}
