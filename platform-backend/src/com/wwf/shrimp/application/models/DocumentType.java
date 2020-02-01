package com.wwf.shrimp.application.models;


/**
 * Document type which would define the document's
 * capabilities and interpretation.
 * The name will be the specific Document type name 
 * (for example "Vessel MCPD" and the value will be the name 
 * of the image to represent the Document.
 * 
 * @author AleaActaEst
 *
 */
public class DocumentType extends LookupEntity {
	
	public static String DESIGNATION_PASSTHROUGH = "Passthrough";
	public static String DESIGNATION_PROFILE = "Profile";
	
	
	/**
	 * the color coding for the type of the document
	 */
	private String hexColorCode;
	
	/**
	 * The actual document type
	 */
	private String documentDesignation;
	
	/**
	 * @return the documentDesignation
	 */
	public String getDocumentDesignation() {
		return documentDesignation;
	}

	/**
	 * @param documentDesignation the documentDesignation to set
	 */
	public void setDocumentDesignation(String documentDesignation) {
		this.documentDesignation = documentDesignation;
	}



	@Override
	public String toString() {
		return "DocumentType [hexColorCode=" + hexColorCode + ", getName()=" + getName() + ", getValue()=" + getValue()
				+ ", getId()=" + getId() + ", toString()=" + super.toString() + ", getClass()=" + getClass()
				+ ", hashCode()=" + hashCode() + "]";
	}

	

	/**
	 * Simple getter for the color value
	 * @return the current color value as hex RGB
	 */
	public String getHexColorCode() {
		return hexColorCode;
	}

	/**
	 * SImple setter for the color value
	 * @param hexColorCode the hex code for the color RGB
	 */
	public void setHexColorCode(String hexColorCode) {
		this.hexColorCode = hexColorCode;
	}

}
