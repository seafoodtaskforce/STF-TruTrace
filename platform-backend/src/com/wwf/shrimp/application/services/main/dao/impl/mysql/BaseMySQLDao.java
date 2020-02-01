package com.wwf.shrimp.application.services.main.dao.impl.mysql;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.wwf.shrimp.application.exceptions.ConfigurationException;
import com.wwf.shrimp.application.exceptions.EntityNotFoundException;
import com.wwf.shrimp.application.exceptions.PersistenceException;
import com.wwf.shrimp.application.models.IdentifiableEntity;
import com.wwf.shrimp.application.models.search.SearchResult;
import com.wwf.shrimp.application.services.main.ConfigurationService;
import com.wwf.shrimp.application.services.main.dao.GenericDAO;
import com.wwf.shrimp.application.services.main.impl.PropertyConfigurationService;
import javax.sql.DataSource;

/**
 * The generic Base DAO implementation for MySQL based DAOs
 * Implements the contract from GenericDAO<T extends IdentifiableEntity,S>
 * @author AleaActaEst
 *
 * @param <T> - the operational entity that this DAO will work with.
 * @param <S> - The specific search criteria for entity <T>
 */
public abstract class BaseMySQLDao<T extends IdentifiableEntity, S> implements GenericDAO<T, S> {
	
	@Override
	public T get(long id) throws PersistenceException, EntityNotFoundException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public T create(T entity) throws PersistenceException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public T update(T entity) throws IllegalArgumentException, PersistenceException, EntityNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public T delete(long id) throws IllegalArgumentException, PersistenceException, EntityNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public SearchResult<T> search(S criteria) throws IllegalArgumentException, PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * THe configuration file name
	 */
	protected final static String BACK_END_CONFIGURATION_FILE = "backend_configuration.properties";
	
	/**
	 * Represents the Logger used to perform logging.
	 */
	private Logger log = Logger.getLogger(getClass().getName());
	
	/**
	 * Represents the configuration file needed to get the database details from
	 */
	Properties prop = new Properties();
    InputStream configInput = null;
    // Configuration Service
    ConfigurationService configService = new PropertyConfigurationService();
	
	
	/**
	 * Connectivity data for MySQL
	 */
	private String username;
	private String password;
	private String jdbcDriverClassName;
	private String dbURL;
		
	
	@Override
	public void init() throws ConfigurationException {
		// get the configuration file
		configInput = getClass().getClassLoader().getResourceAsStream(BACK_END_CONFIGURATION_FILE);
		
		
		// load the configuration file
		try {
			configService.open();
			// load the data from configuration
			jdbcDriverClassName = configService.readConfigurationProperty("jdbc.driver");
			dbURL = configService.readConfigurationProperty("jdbc.url");
			username = configService.readConfigurationProperty("jdbc.username");
			password = 	configService.readConfigurationProperty("jdbc.password");
			// JNDI Data
			Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/Backend_DATA");
			
			
		} catch (ConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new ConfigurationException(
					"Failed to retrieve configuration data", 
					e1);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			configService.close();
		}

		/**
		 * Execute the processing logic
		 */
		try{
	        // Register JDBC driver
			Class.forName(jdbcDriverClassName).newInstance();
	    }catch(Exception e) {
	    	//
		    //Handle errors for JDBC
		    	
		    // log the issue
	    	log.error("[ERROR] " + e);
	    	
	    	// do the rest
	        e.printStackTrace();
	        throw new ConfigurationException("Cannot create driver for MySQL", e);
	        
	    }finally{
	    	// nothing to do here
		}

	}
	
	
	/**
	 * Get a new connection
	 * 
	 * @return the connection
	 * @throws PersistenceException - if the connection could not be established
	 */
	protected Connection openConnection() throws PersistenceException{
		
		Connection conn = null;
		
		// Execute the processing logic
		//
		try{
			// JNDI Data
			Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            DataSource dataSource = (DataSource) envContext.lookup("jdbc/Backend_DATA");
			
			
			// create a connection 
            conn = dataSource.getConnection();
            
			// conn = DriverManager.getConnection(dbURL,username,password);
		}catch(SQLException se){
		    //
		    //Handle errors for JDBC
		    	
		    // log the issue
			log.error("[ERROR] " + se);
		    	
		    // do the rest
		    se.printStackTrace();
		    throw new PersistenceException("Cannot create connection for MySQL", se);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			// nothing to do here
		}
		
		// return the result
	    return conn;
	}
	
	/**
	 * Release a connection
	 * 
	 * @param conn
	 * @throws PersistenceException
	 */
	protected void closeConnection(Connection conn) throws PersistenceException{
		
		// Execute the processing logic
		//
		try{
	        if(conn != null)
	        	conn.close();
		}catch(SQLException se){
	    	//
	        //Handle errors for JDBC
	    	
			// log the issue
			log.error("[ERROR] " + se);
	    	
	    	// do the rest
	        se.printStackTrace();
	        throw new PersistenceException("Cannot close connection for MySQL", se);
		}finally{
			// nothing to do here
		}

	}
	/**
	 * @return the log
	 */
	protected Logger getLog() {
		return log;
	}

	
	
}
