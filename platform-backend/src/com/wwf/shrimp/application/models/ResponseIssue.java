package com.wwf.shrimp.application.models;

public class ResponseIssue extends IdentifiableEntity {
	/**
     * Static global variables
     */
    public static String ISSUE_SEVERITY_WARNING = "WARNING";
    public static String ISSUE_SEVERITY_ERROR = "ERROR";
    public static String ISSUE_SEVERITY_FATAL_ERROR = "FATAL";
    
    private String lineNumber;			// line number if the input was a file
    private String columnNumber;		// column number of the input had columns 1-based
    private String columnName;          // column name if names have been designated
    private String atCharLocation;      //  specific location of the 1st character of the issue 1-based
    private String issue;               // the actual issue representation as a string
    private String severity;            // severity if the issue as noted in the static variables above
    private String rawMessage;			// the raw message which is the original data for the issue
    
	public String getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}
	public String getColumnNumber() {
		return columnNumber;
	}
	public void setColumnNumber(String columnNumber) {
		this.columnNumber = columnNumber;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getAtCharLocation() {
		return atCharLocation;
	}
	public void setAtCharLocation(String atCharLocation) {
		this.atCharLocation = atCharLocation;
	}
	public String getIssue() {
		return issue;
	}
	public void setIssue(String issue) {
		this.issue = issue;
	}
	public String getSeverity() {
		return severity;
	}
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	public String getRawMessage() {
		return rawMessage;
	}
	public void setRawMessage(String rawMessage) {
		this.rawMessage = rawMessage;
	}

    
}
