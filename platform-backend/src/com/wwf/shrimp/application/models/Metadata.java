package com.wwf.shrimp.application.models;

/**
 * The specific metadata being captured. Example could be GPS coordinates where
 *  a document was created.
 *  
 *  Metadata captures something about the data itself.
 *  So for example for a document the Metatdata could be GPS which would
 *  provide information as to where this document was created/submitted/etc...
 *
 *  This is a DataEntity which will capture data name, value, type as well as 
 *  whether the value is needed or not.
 * @author AleaActaEst
 *
 */
public class Metadata extends DataEntity {
	private MetadataType type;

	/**
	 * @return the type
	 */
	public MetadataType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(MetadataType type) {
		this.type = type;
	}

}
