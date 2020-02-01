package com.wwf.shrimp.application.models;

/**
 * Base class for all entities that have an id
 * 
 * @author AleaActaEst
 *
 */
public class IdentifiableEntity {
	


	/**
	 * The unique id for this entity, it is assumed that any id <= 0 is not valid and will be treated as empty
	 */
	private long id;
	
	/**
	 * Keeps track of a soft-delete functionality for filtering deleted entities.
	 */
	private boolean deleted = false;

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IdentifiableEntity [id=" + id + "]";
	}
	
	/**
	 * Check if the entity is defined through its id
	 * @param entity
	 * @return
	 */
	public static boolean isDefined(IdentifiableEntity entity){
		if(entity.getId() <=0){
			return false;
		}else{
			return true;
		}
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IdentifiableEntity other = (IdentifiableEntity) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}
}
