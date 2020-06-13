package com.wwf.shrimp.application.client.android.models.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapping of a resource entity to permissions and required indicator.
 * 
 * @author AleaActaEst
 *
 * @param <T> - The specific resource being mapped
 */
public class MappedResourceEntity<T> extends IdentifiableEntity {

	private T entity;
	private boolean isRequired=false;
	private List<Permission> permissions = new ArrayList<Permission>();
	
	/**
	 * @return the entity
	 */
	public T getEntity() {
		return entity;
	}
	/**
	 * @param entity the entity to set
	 */
	public void setEntity(T entity) {
		this.entity = entity;
	}
	/**
	 * @return the isRequired
	 */
	public boolean isRequired() {
		return isRequired;
	}
	/**
	 * @param isRequired the isRequired to set
	 */
	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}
	/**
	 * @return the permissions
	 */
	public List<Permission> getPermissions() {
		return permissions;
	}
	/**
	 * @param permissions the permissions to set
	 */
	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
	}
}
