package com.wwf.shrimp.application.services.main.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.wwf.shrimp.application.exceptions.ConfigurationException;
import com.wwf.shrimp.application.services.main.ConfigurationService;

/**
 * Simple java property based implementation of the configuration service.
 * 
 * 
 * @author AleaActaEst
 *
 */
public class PropertyConfigurationService implements ConfigurationService {
	/**
	 * THe configuration file name
	 */
	protected final static String BACK_END_CONFIGURATION_FILE = "backend_configuration.properties";
	
	/**
	 * Represents the configuration file needed to get the database details from
	 */
	private Properties prop = new Properties();
    private InputStream configInput = null;
    private boolean initializedFlag = false;
	
	/**
	 * reads the specific configuration named configuration value form the configuration repository
	 * 
	 * @param name - the name of the property, cannot be null
	 * @return - the configuration value as a string or null of not found
	 * @throws ConfigurationException if there was an issue with configuration repository
	 */
	@Override
	public String readConfigurationProperty(String name) throws ConfigurationException {
		String result=null;
				
		// make sure that the service is initialized properly
		if(!initializedFlag) {
			throw new ConfigurationException("Need to initialize/open the service first ");
		}
		try {
			prop.load(configInput);
			// load the data from configuration
			result = prop.getProperty(name);
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new ConfigurationException(
					"Cannot load configuration values from " 
					+ BACK_END_CONFIGURATION_FILE, 
					e1);
		}
		return result;
	}

	@Override
	/**
	 * Open and load the underlying configuration input
	 */
	public void open() throws ConfigurationException {
		
		// 
		try {
			// get the configuration file
			configInput = getClass().getClassLoader().getResourceAsStream(BACK_END_CONFIGURATION_FILE);
			
			prop.load(configInput);
			initializedFlag = true;

			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			initializedFlag = false;
			throw new ConfigurationException(
					"Cannot load configuration file " 
					+ BACK_END_CONFIGURATION_FILE, 
					e1);
			
		}
		
	}

	@Override
	/**
	 * Close the underlying configuration input
	 */
	public void close() throws ConfigurationException {
		if (null != configInput){
	        try{
	        	configInput.close();
	        }
	        catch (Exception e){
	            e.printStackTrace();
	        }
	    }
		initializedFlag = false;
		
	}

}
