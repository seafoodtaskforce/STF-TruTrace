package com.wwf.shrimp.application.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Data specific to error messages
 * @author AleaActaEst
 *
 */
public class ResponseErrorData extends IdentifiableEntity {
	
	private String header;          			// the header for the error message
	private String mediaType;                 	// media type for the response
	private List<ResponseIssue> issues; 		// the actual textual issues itemized for the request
	
	
	/**
	 * Default constructor
	 */
	public ResponseErrorData() {

		this.issues = new ArrayList<ResponseIssue>();
	}
	
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public String getMediaType() {
		return mediaType;
	}
	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}
	public List<ResponseIssue> getIssues() {
		return issues;
	}
	public void setIssues(List<ResponseIssue> errorData) {
		this.issues = errorData;
	}
	
	
}
