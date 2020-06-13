package com.wwf.shrimp.application.client.android.services;

/**
 * Created by AleaActaEst on 09/11/2017.
 */

import com.wwf.shrimp.application.client.android.exceptions.ConfigurationException;
import com.wwf.shrimp.application.client.android.models.ConfigurationData;

/**
 * Contract for configuration. This is basically just a read only contract.
 *
 * @author AleActaEst
 *
 */
public interface ConfigurationService {

    /**
     * reads the specific configuration named configuration value form the configuration repository
     * @param name - the name of the property, cannot be null
     * @return - the configuration value as a string or null of not found
     * @throws ConfigurationException
     *      - if there was an issue with configuration repository
     */
    public String readConfigurationProperty(String name) throws ConfigurationException;

    /**
     * Read all of the properties form the configuration persistence and put them in the return
     * object.
     * @return - the configuration data read from configuration
     * @throws ConfigurationException
     *      - if there was an issue with configuration repository
     */
    public ConfigurationData readAllProperties() throws ConfigurationException;

    /**
     * Configuration session should be open before it can be used
     *
     * @throws ConfigurationException
     *      - if there was an issue
     */
    public void open() throws ConfigurationException;

    /**
     * Configuration session should be closed after it is not needed
     *
     * @throws ConfigurationException
     *      - if there was an issue
     */
    public void close() throws ConfigurationException;

}