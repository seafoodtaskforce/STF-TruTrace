package com.wwf.shrimp.application.client.android.models.dto;


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

    @Override
    public String toString() {
        return "DocumentType [hexColorCode=" + hexColorCode + ", getName()=" + getName() + ", getValue()=" + getValue()
                + ", getId()=" + getId() + ", toString()=" + super.toString() + ", getClass()=" + getClass()
                + ", hashCode()=" + hashCode() + "]";
    }

    private String hexColorCode;
    private String documentDesignation;

    public String getHexColorCode() {
        return hexColorCode;
    }

    public void setHexColorCode(String hexColorCode) {
        this.hexColorCode = hexColorCode;
    }

    public String getDocumentDesignation() {
        return documentDesignation;
    }

    public void setDocumentDesignation(String documentDesignation) {
        this.documentDesignation = documentDesignation;
    }
}
