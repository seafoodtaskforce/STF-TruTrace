package com.wwf.shrimp.application.client.android.models;

import java.util.Date;

/**
 * This is an access token used in the session to use to
 * authenticate the user.
 *
 * @author AleaActaEst
 *
 */
public class SecurityToken {
    private String tokenValue;
    private Date expirationDate;

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

}
