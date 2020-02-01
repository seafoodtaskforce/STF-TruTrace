package com.wwf.shrimp.application.models;

import java.util.ArrayList;
import java.util.List;

public class DocumentCollection {
	
	private List<Document> allDocsToAttach = new ArrayList<Document>();
	private List<Document> attachedDocs = new ArrayList<Document>();;
	private List<Document> allDocsToLink = new ArrayList<Document>();;
	private List<Document> linkedDocs = new ArrayList<Document>();
	public List<Document> getAllDocsToAttach() {
		return allDocsToAttach;
	}
	public void setAllDocsToAttach(List<Document> allDocsToAttach) {
		this.allDocsToAttach = allDocsToAttach;
	}
	public List<Document> getAttachedDocs() {
		return attachedDocs;
	}
	public void setAttachedDocs(List<Document> attachedDocs) {
		this.attachedDocs = attachedDocs;
	}
	public List<Document> getAllDocsToLink() {
		return allDocsToLink;
	}
	public void setAllDocsToLink(List<Document> allDocsToLink) {
		this.allDocsToLink = allDocsToLink;
	}
	public List<Document> getLinkedDocs() {
		return linkedDocs;
	}
	public void setLinkedDocs(List<Document> linkedDocs) {
		this.linkedDocs = linkedDocs;
	};

}
