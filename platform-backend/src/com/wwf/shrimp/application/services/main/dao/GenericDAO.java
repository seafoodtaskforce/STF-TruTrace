package com.wwf.shrimp.application.services.main.dao;

import com.wwf.shrimp.application.exceptions.ConfigurationException;
import com.wwf.shrimp.application.exceptions.EntityNotFoundException;
import com.wwf.shrimp.application.exceptions.PersistenceException;
import com.wwf.shrimp.application.models.IdentifiableEntity;
import com.wwf.shrimp.application.models.search.SearchResult;
import com.wwf.shrimp.application.services.main.IInitializable;

/**
 * The generic DAO definition.
 * Will serve as a base interface for CRUD operations and services.
 * 
 * All implementations should be thread-safe
 * @author AleaActaEst
 *
 * @param <T> - the operational entity that this DAO will work with.
 * @param <S> - The specific search criteria for entity <T>
 */
public interface GenericDAO<T extends IdentifiableEntity, S> extends IInitializable{
	
	/**
	 * This method is used to retrieve an entity <T>
	 * 
	 * @param id - the ID of the entity to retrieve
	 * @return - the matching entity
	 * @throws IllegalArgumentException - if id is not positive  
	 * @throws PersistenceException - if there was a generic persistence issue
	 * @throws EntityNotFoundException - if the entity does not exist
	 */
	public T get(long id) throws PersistenceException
										, EntityNotFoundException
										, IllegalArgumentException;
	

	/**
	 * This method will create a new entity within the storage for this entity and 
	 * will assign a new unique ID for it
	 * @param entity - the entity to create
	 * @return - the created entity with a unique id set
	 *
	 * @throws IllegalArgumentException - if entity is null or not valid 
	 * @throws PersistenceException - if any other error occurred during operation
	 */
	public T create(T entity) throws PersistenceException, IllegalArgumentException;
	
	
	/**
	 * This method is used to update an existing entity
	 * 
	 * @param entity - the entity to update
	 * @return - the updated entity (unchanged form the passed in one)
	 * @throws IllegalArgumentException - if entity is null or not valid
	 * @throws PersistenceException - if any other error occurred during operation
	 * @throws EntityNotFoundException - if the entity does not exist.
	 */
	public T update(T entity) throws IllegalArgumentException, PersistenceException, EntityNotFoundException;
	
	/**
	 * This method is used to delete an existing entity
	 * 
	 * @param id - the ID of the entity to delete
	 * @return - the removed entity
	 * @throws IllegalArgumentException - if id is not positive
	 * @throws PersistenceException - if any other error occurred during operation
	 * @throws EntityNotFoundException - if the entity does not exist.
	 */
	public T delete(long id) throws IllegalArgumentException, PersistenceException, EntityNotFoundException;
	
	/**
	 * This method is used to search for entities by search criteria.
	 * The search is paged.	
	 * 
	 * @param criteria - the search criteria/filter with paging fields of type <S>
	 * @return - the search result with entity of type <T>
	 * @throws IllegalArgumentException - if attributeName or attributeValue are empty or null
	 * 									, if page size is not positive or page number is negative.
	 * @throws PersistenceException - if any other error occurred during operation
	 */
	public SearchResult<T> search(S criteria) throws IllegalArgumentException, PersistenceException;
	
	
	/**
	 * Simple initialization method which should be called before the 
	 * business methods are invoked.
	 * 
	 * @throws ConfigurationException - if the instance is not properly configured
	 */
	public void init() throws ConfigurationException;

}
