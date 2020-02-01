package com.wwf.shrimp.application.models;

import java.util.Comparator;

/**
 * Abstraction of user credentials used to authenticate the user.
 * 
 * (It is not abstract due to GSON limitations)
 * @author AleaActaEst
 *
 */
public class UserCredentials  implements Comparator<UserCredentials>, Comparable<UserCredentials> {
	
	private String username;
	private SecurityToken token;
	private String requestOrigin;
	
	/**
	 * @return the token
	 */
	public SecurityToken getToken() {
		return token;
	}
	
	/**
	 * @return  - the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the requestOrigin
	 */
	public String getRequestOrigin() {
		return requestOrigin;
	}
	
	/**
	 * @param token the token to set
	 */
	public void setToken(SecurityToken token) {
		this.token = token;
	}
	
	public void setUsername(String username) {
		this.username = username;
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

	@Override
	public int compareTo(UserCredentials o) {
		SecurityToken token1 = this.getToken();
    	SecurityToken token2 = o.getToken();
		
		if(token1.getExpirationDate().before(token2.getExpirationDate())){
        	return -1;
        }
        if(token1.getExpirationDate().after(token2.getExpirationDate())){
        	return -1;
        }
        return 0;
	}

	@Override
	public int compare(UserCredentials o1, UserCredentials o2) {
		UserCredentials object1 = (UserCredentials) o1;
    	UserCredentials object2 = (UserCredentials) o2;
    	SecurityToken token1 = object1.getToken();
    	SecurityToken token2 = object2.getToken();
    	
        if(token1.getExpirationDate().before(token2.getExpirationDate())){
        	return -1;
        }
        if(token1.getExpirationDate().after(token2.getExpirationDate())){
        	return -1;
        }
        return 0;
	}
	

}
