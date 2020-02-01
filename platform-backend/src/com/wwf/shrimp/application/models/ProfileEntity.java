package com.wwf.shrimp.application.models;

import java.util.List;

/**
 * This is a top-level abstracted notion of a Profile entity.
 * It defines  a name and description for the entity as well as 
 * what resources it is associated with.
 *
 * Currently we can associate templates and specific documents with a profile.
 * @author AleaActaEst
 *
 */
public abstract class ProfileEntity extends IdentifiableEntity {
	
	private String name;
	private String description;
	private List<MappedResourceEntity<DocumentTemplate>> resourceTemplates;
	private List<MappedResourceEntity<Document>> resources;
	
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
	 * @return the resourceTemplates
	 */
	public List<MappedResourceEntity<DocumentTemplate>> getResourceTemplates() {
		return resourceTemplates;
	}
	/**
	 * @param resourceTemplates the resourceTemplates to set
	 */
	public void setResourceTemplates(List<MappedResourceEntity<DocumentTemplate>> resourceTemplates) {
		this.resourceTemplates = resourceTemplates;
	}
	/**
	 * @return the resources
	 */
	public List<MappedResourceEntity<Document>> getResources() {
		return resources;
	}
	/**
	 * @param resources the resources to set
	 */
	public void setResources(List<MappedResourceEntity<Document>> resources) {
		this.resources = resources;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ProfileEntity [name=" + name + ", description=" + description + ", resourceTemplates="
				+ resourceTemplates + ", resources=" + resources + "]";
	}

}
