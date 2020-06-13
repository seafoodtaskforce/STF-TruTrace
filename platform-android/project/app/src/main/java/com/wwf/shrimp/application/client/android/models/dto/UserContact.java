package com.wwf.shrimp.application.client.android.models.dto;

/**
 * User Contact Information.
 * 
 * @author AleaActaEst
 *
 */
public class UserContact extends IdentifiableEntity {

	private String emailAddress;
	private String cellNumber;
	private String firstName;
	private String lastName;
	private String nickName;
	private String lineId;
	
	/**
	 * @return the emailAddress
	 */
	public String getEmailAddress() {
		return emailAddress;
	}
	/**
	 * @param emailAddress the emailAddress to set
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	/**
	 * @return the cellNumber
	 */
	public String getCellNumber() {
		return cellNumber;
	}
	/**
	 * @param cellNumber the cellNumber to set
	 */
	public void setCellNumber(String cellNumber) {
		this.cellNumber = cellNumber;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}



	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getLineId() {
		return lineId;
	}

	public void setLineId(String lineId) {
		this.lineId = lineId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UserContact [emailAddress=" + emailAddress + ", cellNumber=" + cellNumber + "]";
	}
}
