package com.wwf.shrimp.application.models;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author AleaActaEst
 *
 */
public class ResponseMessageData extends IdentifiableEntity {
	private List<String> messages = new ArrayList<String>();

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
	
	
}
