package com.wwf.shrimp.application.client.android.models.dto;

/**
 * Defines a data entity for a given resource.
 * This is a generic definition that defines the name, the specific
 *  value, its data type as well as weather it is required in that 
 *  entity or not.
 *  
 *  Think of it as simple definitions of columns in a data base.
 * @author AleaActaEst
 *
 */
public abstract class DataEntity {
	private String datumName;
	private Object value;
	private String dataType;
	private boolean isrequired = false;
	/**
	 * @return the datumName
	 */
	public String getDatumName() {
		return datumName;
	}
	/**
	 * @param datumName the datumName to set
	 */
	public void setDatumName(String datumName) {
		this.datumName = datumName;
	}
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}
	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	/**
	 * @return the isrequired
	 */
	public boolean isIsrequired() {
		return isrequired;
	}
	/**
	 * @param isrequired the isrequired to set
	 */
	public void setIsrequired(boolean isrequired) {
		this.isrequired = isrequired;
	}
	
	
}
