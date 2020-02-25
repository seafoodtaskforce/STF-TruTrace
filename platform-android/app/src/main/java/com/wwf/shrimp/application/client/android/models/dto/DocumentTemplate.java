package com.wwf.shrimp.application.client.android.models.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * A document template which is basically a generalization 
 * of a document type.
 * 
 * For example an MCPD document could be a template as it captures 
 * a specific document type and some information about such a document. 
 * Information such as min and/or max number of expected pages, etc...
 * 
 * @author AleaActaEst
 *
 */
public class DocumentTemplate extends IdentifiableEntity implements IResource {
	private String name;
	private String description;
	private int minPages;
	private int maxPages;
	private List<DataEntity> data = new ArrayList<DataEntity>();
	private DocumentType type;
	private int versionMajor;
	private int versionMinor;
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the minPages
	 */
	public int getMinPages() {
		return minPages;
	}
	/**
	 * @param minPages the minPages to set
	 */
	public void setMinPages(int minPages) {
		this.minPages = minPages;
	}
	/**
	 * @return the maxPages
	 */
	public int getMaxPages() {
		return maxPages;
	}
	/**
	 * @param maxPages the maxPages to set
	 */
	public void setMaxPages(int maxPages) {
		this.maxPages = maxPages;
	}
	/**
	 * @return the data
	 */
	public List<DataEntity> getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(List<DataEntity> data) {
		this.data = data;
	}
	/**
	 * @return the type
	 */
	public DocumentType getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(DocumentType type) {
		this.type = type;
	}
	/**
	 * @return the versionMajor
	 */
	public int getVersionMajor() {
		return versionMajor;
	}
	/**
	 * @param versionMajor the versionMajor to set
	 */
	public void setVersionMajor(int versionMajor) {
		this.versionMajor = versionMajor;
	}
	/**
	 * @return the versionMinor
	 */
	public int getVersionMinor() {
		return versionMinor;
	}
	/**
	 * @param versionMinor the versionMinor to set
	 */
	public void setVersionMinor(int versionMinor) {
		this.versionMinor = versionMinor;
	}
}
