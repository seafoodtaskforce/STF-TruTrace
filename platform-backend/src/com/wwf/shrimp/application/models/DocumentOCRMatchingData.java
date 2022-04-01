package com.wwf.shrimp.application.models;

/**
 * Class which collects data about possible OCR matching
 * This is organized per document type
 * @author AleaActaEst
 *
 */
public class DocumentOCRMatchingData extends IdentifiableEntity {
	
	public static String EXACT_MATCH_TYPE = "EXACT";
	public static String PREFIX_MATCH_TYPE = "PREFIX";
	public static String IN_STRING_MATCH_TYPE = "ANYWHERE_IN_STRING";
	public static String REGEX_MATCH_TYPE = "REGEX";
	
	private String ocrMatchText;
	private DocumentType docType ;
	private String matchType;
	
	public String getOcrMatchText() {
		return ocrMatchText;
	}
	public void setOcrMatchText(String ocrMatchText) {
		this.ocrMatchText = ocrMatchText;
	}
	public DocumentType getDocType() {
		return docType;
	}
	public void setDocType(DocumentType docType) {
		this.docType = docType;
	}
	public String getMatchType() {
		return matchType;
	}
	public void setMatchType(String matchType) {
		this.matchType = matchType;
	}
	
	
	
	

}
