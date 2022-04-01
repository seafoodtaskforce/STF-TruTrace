package com.wwf.shrimp.application.utils;

import org.apache.commons.codec.digest.DigestUtils;

public class HashingUtils {
	
    /**
     * Create a one-way hash for the input string
     * @param stringToHash - the string to be 1-way hashed
     * @return - the hashed string
     */
    public static String hashStringOneWay(String stringToHash) {
    	String sha256hex = DigestUtils.sha256Hex(stringToHash);
    	return sha256hex;
    }
    
}
