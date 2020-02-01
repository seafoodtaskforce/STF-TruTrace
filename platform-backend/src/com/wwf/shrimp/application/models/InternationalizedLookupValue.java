package com.wwf.shrimp.application.models;

/**
 * Value used for internationalization
 * @author user
 *
 */
public class InternationalizedLookupValue extends IdentifiableEntity {
	
	private String languageCode;
	private LookupEntity lookupValue;
	
	
	public LookupEntity getLookupValue() {
		return lookupValue;
	}

	public void setLookupValue(LookupEntity lookupValue) {
		this.lookupValue = lookupValue;
	}
	
	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	  

}
