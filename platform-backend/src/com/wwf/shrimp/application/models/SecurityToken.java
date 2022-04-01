package com.wwf.shrimp.application.models;

import java.util.Date;

/**
 * This is an access token used in the session to use to 
 * authenticate the user.
 * 
 * @author AleaActaEst
 *
 */
public class SecurityToken {
	/**
	 * The token value. Should be unique and relatively random
	 */
	private String tokenValue;
	/**
	 * THe expiration date of the token
	 */
	private Date expirationDate;
	
	/**
	 * Optional seed for the token
	 */
	private String seed;
	
	/**
	 * Flag for valid/invalid token
	 */
	private boolean invalidated = false;
	
	/**
	 * @return the invalidated
	 */
	public boolean isInvalidated() {
		return invalidated;
	}
	/**
	 * @param invalidated the invalidated to set
	 */
	public void setInvalidated(boolean invalidated) {
		this.invalidated = invalidated;
	}
	/**
	 * @return the tokenValue
	 */
	public String getTokenValue() {
		return tokenValue;
	}
	/**
	 * @param tokenValue the tokenValue to set
	 */
	public void setTokenValue(String tokenValue) {
		this.tokenValue = tokenValue;
	}
	/**
	 * @return the expirationDate
	 */
	public Date getExpirationDate() {
		return expirationDate;
	}
	/**
	 * @param expirationDate the expirationDate to set
	 */
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}
	
	
	/**
	 * Get the seed used for this token
	 * @return the seed for this token and null if none was used
	 */
	public String getSeed() {
		return seed;
	}
	
	/**
	 * Set the seed for this token. Can be null
	 * @param seed - the seed used to create this token. Can be null
	 */
	public void setSeed(String seed) {
		this.seed = seed;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tokenValue == null) ? 0 : tokenValue.hashCode());
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
		SecurityToken other = (SecurityToken) obj;
		if (tokenValue == null) {
			if (other.tokenValue != null) {
				return false;
			}
		} else if (!tokenValue.equals(other.tokenValue)) {
			return false;
		}
		return true;
	}
	
}
