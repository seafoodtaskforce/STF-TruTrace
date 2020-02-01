package com.wwf.shrimp.application.models;

/**
 * A supplementary piece of data to a document which has to do with the 
 * submission and acceptance cycle.
 * It is a note that is attached (textual) to a document.
 * It can contain formatting information as well as additional tags or 
 * specific data about the reasons for document rejection or acceptance.
 * 
 * @author AleaActaEst
 *
 */
public class NoteData extends IdentifiableEntity {
	
	@Override
	public String toString() {
		return "NoteData [note=" + note + ", creationTimestamp=" + creationTimestamp + ", owner=" + owner + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((creationTimestamp == null) ? 0 : creationTimestamp.hashCode());
		result = prime * result + ((note == null) ? 0 : note.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NoteData other = (NoteData) obj;
		if (creationTimestamp == null) {
			if (other.creationTimestamp != null)
				return false;
		} else if (!creationTimestamp.equals(other.creationTimestamp))
			return false;
		if (note == null) {
			if (other.note != null)
				return false;
		} else if (!note.equals(other.note))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getCreationTimestamp() {
		return creationTimestamp;
	}
	public void setCreationTimestamp(String creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	private String note;
	private String creationTimestamp;
	private String owner;	

}
