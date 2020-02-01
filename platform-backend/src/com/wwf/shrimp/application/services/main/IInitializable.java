package com.wwf.shrimp.application.services.main;

import com.wwf.shrimp.application.exceptions.ConfigurationException;


/**
 * Initialization interface which will allow for any client to check 
 * if something needs to be initialized.
 * 
 * @author AleaActaEst
 *
 */
public interface IInitializable {
	/**
	 * Simple initialization method which should be called before the 
	 * business methods are invoked.
	 * 
	 * @throws ConfigurationException - if the instance cannot be 
	 * properly configured
	 */
	public void init() throws ConfigurationException;

}
