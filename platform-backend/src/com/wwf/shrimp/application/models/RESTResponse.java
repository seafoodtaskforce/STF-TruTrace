package com.wwf.shrimp.application.models;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author AleaActaEst
 * 
 * Main Response class for holding response data for RESTful responses.
 * The Object is made of two parts:
 * 	1. THe data load which is the actual response data for the given request
 *  2. Messages that describe something about the response 
 */
public class RESTResponse {
	
	public static String ERROR_MESSAGE_CSV_TEMPLATE = "line: [_line_number] has issue: [_issue] at column: [_column_line] - [_column_name]";
	
	private Object data;
	private List<ResponseErrorData> errorData = new ArrayList<ResponseErrorData>();     // errors
	private List<ResponseMessageData> messageData = new ArrayList<ResponseMessageData>();

	
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	 
	 public List<ResponseErrorData> getErrorData() {
		return errorData;
	}
	public void setErrorData(List<ResponseErrorData> errorData) {
		this.errorData = errorData;
	}
	public List<ResponseMessageData> getMessageData() {
		return messageData;
	}
	public void setMessageData(List<ResponseMessageData> messageData) {
		this.messageData = messageData;
	}
	
	public static String createCVSErrorMessage(int lineNumber, String issue, int column, String columnName){
		 String result = ERROR_MESSAGE_CSV_TEMPLATE;
		 
		 result = result.replaceFirst("_line_number", String.valueOf(lineNumber));
		 result = result.replaceFirst("_issue", issue);
		 result = result.replaceFirst("_column_line", String.valueOf(column));
		 result = result.replaceFirst("_column_name", String.valueOf(columnName));
		 return result;
	 }
}
