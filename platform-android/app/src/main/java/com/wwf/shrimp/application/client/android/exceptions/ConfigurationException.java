package com.wwf.shrimp.application.client.android.exceptions;

import 	java.lang.RuntimeException;


/**
 * This exception will be thrown to indicate any
 * configuration error.
 *
 * @author AleaActaEst
 *
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(String message) {
        super(message);
    }
}
