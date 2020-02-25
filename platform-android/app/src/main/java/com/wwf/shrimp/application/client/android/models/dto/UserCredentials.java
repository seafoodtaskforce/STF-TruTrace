package com.wwf.shrimp.application.client.android.models.dto;

/**
 * Abstraction of user credentials used to authenticate the user
 * @author AleaActaEst
 *
 */
public class UserCredentials  {
	private String username;
	private SecurityToken token;
	private String requestOrigin;

	/**
	 * @return  - the username
	 */
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * @return the token
	 */
	public SecurityToken getToken() {
		return token;
	}
	/**
	 * @param token the token to set
	 */
	public void setToken(SecurityToken token) {
		this.token = token;
	}
	/**
	 * @return the requestOrigin
	 */
	public String getRequestOrigin() {
		return requestOrigin;
	}
	/**
	 * @param requestOrigin the requestOrigin to set
	 */
	public void setRequestOrigin(String requestOrigin) {
		this.requestOrigin = requestOrigin;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((requestOrigin == null) ? 0 : requestOrigin.hashCode());
		result = prime * result + ((token == null) ? 0 : token.hashCode());
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
		UserCredentials other = (UserCredentials) obj;
		if (token == null) {
			if (other.token != null) {
				return false;
			}
		} else if (!token.equals(other.token)) {
			return false;
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UserCredentials [token=" + token + ", requestOrigin=" + requestOrigin + "]";
	}
	

}
