package com.wwf.shrimp.application.models;

/**
 * Represents a lookup entity which holds a pair of name:value data.
 * 
 * 1. This would be used to look up shorthand values for a longer named data, 
 * or to lookup data based on a fixed name for which the value could change.
 * 
 * 2. It could be used to look up specific values based on shorthands such as 
 * for example states of the USA or countries.
 * 
 * 3. It can be used to get a list of available values which could then be 
 * used for some further processing.
 * 
 * You will need at each derived lookup value entity for specifics (for example 
 * see MetadataType and DocuemntType and their differences)
 * 
 * @author AleaActaEst
 *
 */
public class LookupEntity extends IdentifiableEntity {
	

	private String name;
	private String value;
	private boolean resource;
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	public boolean isResource() {
		return resource;
	}
	public void setResource(boolean resource) {
		this.resource = resource;
	}

}
