package com.wwf.shrimp.application.services.main.rest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wwf.shrimp.application.models.IdentifiableEntity;
import com.wwf.shrimp.application.models.LookupEntity;
import com.wwf.shrimp.application.models.TagData;
import com.wwf.shrimp.application.models.search.TagSearchCriteria;
import com.wwf.shrimp.application.services.main.BaseRESTService;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.TagMySQLDao;
import com.wwf.shrimp.application.utils.RESTUtility;

/**
 * General Tag based RESTful service which will have the functionality management of tag data for documents.  
 * 
 * <TODO> Change the verbs to be fully restful
 * @author AleaActaEst
 *
 */
@Path("/tag")
public class TagRESTService extends BaseRESTService {
	private TagMySQLDao<TagData, TagSearchCriteria> tagService = new TagMySQLDao<TagData, TagSearchCriteria>();
	
	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will create a new tag in the database.
	 * If the creator also wants top be the owner then only they will be able to see the tag
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the TagData entity with a new tag to be created
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated TagData entity.
	 *     2. Error String if there was an issue
	 */
	public Response create(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("false") @HeaderParam("owner") boolean isOwner) {
		TagData newTag = null;
		IdentifiableEntity id = null;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize service
		tagService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
			
			//
			// get the parser ready

			
			
			Gson gson = new GsonBuilder()
		            .create();
			
			// parse the JSON input into the specific class
			newTag = gson.fromJson(reader, TagData.class);
			getLog().info("New Tag Creation: " + newTag);
			
			newTag = tagService.create(newTag);
			
			if(newTag != null){
				id = new IdentifiableEntity();
				id.setId(newTag.getId());
			}


		} catch (Exception e) {
			getLog().error("Error Creating a new Tag: - " + e);
		}

		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(newTag)).build();
	}

	
	@GET
	@Path("/fetchall")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchAllTags(
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		// results
		List<TagData> allTags=null;
		
		getLog().info("HEADER user-name: " + userName);
		
		//
		// Initialize service
		tagService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			allTags = tagService.getAllTags(userName);

		} catch (Exception e) {
			getLog().error("Error Fetching Tags: - " + e.getStackTrace());
		}
		
		getLog().debug("Fetch all tags result: " + allTags);
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(allTags)).build();
	}
	
	@GET
	@Path("/fetchbydocumentid")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchTagByDocument(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @QueryParam("doc_id") long docID) {
		// results
		List<TagData> allTags=null;
		
		getLog().info("HEADER user-name: " + userName);
		
		//
		// Initialize service
		tagService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			allTags = tagService.getAllTagsByDocId(docID);

		} catch (Exception e) {
			getLog().error("Error Fetching Tags for Doc: - " + e.getStackTrace());
		}
		
		getLog().debug("Fetch tag by doc id result: " + allTags);
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(allTags)).build();
	}
	

	@GET
	@Path("/fetchbydocumentsyncid")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchTagByDocumentSyncId(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @QueryParam("doc_id") String syncId) {
		// results
		List<TagData> allTags=null;
		
		getLog().info("HEADER user-name: " + userName);
		
		//
		// Initialize service
		tagService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			allTags = tagService.getAllTagsByDocSyncId(syncId);

		} catch (Exception e) {
			getLog().error("Error Fetching Tags for Doc: - " + e);
		}
		
		
		getLog().debug("Fetch tag by sync id result: " + allTags);
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(allTags)).build();
	}
	
	
	 
	@GET
	@Path("/fetchcustomprefixlist")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchTagPrefixList(
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		// results
		List<LookupEntity> allTagPrefixes=null;
		
		getLog().info("HEADER user-name: " + userName);
		
		//
		// Initialize service
		tagService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			allTagPrefixes = tagService.getTagCustomPrefixList();

		} catch (Exception e) {
			getLog().error("Error Fetching Tag custom prefix list: - " + e.getStackTrace());
		}
		
		
		getLog().debug("Fetch tag custom prefix list: " + allTagPrefixes);
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(allTagPrefixes)).build();
	}
	
	@POST
	@Path("/attach")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will create a new document in the database
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the Document entity with a DocumentPage embedded
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated document.
	 *     2. Error String if there was an issue
	 */
	public Response attach(InputStream incomingData, 
			@DefaultValue("0") @QueryParam("doc_id") long docId) {
		String result = "/tag/attach com.wwf.shrimp.application [CREATE NEW TAG ATTACHEMENTS]";
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize service
		tagService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
			
			
			// parse the JSON input into the specific class
			Type listType = new TypeToken<ArrayList<TagData>>(){}.getType();
			List<TagData> tags = new Gson().fromJson(reader, listType);
			
			tagService.attach(tags, docId);


		} catch (Exception e) {
			getLog().error("Error attaching tags: - " + e.getStackTrace());
			
		}

		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(result).build();
	}
	
	@POST
	@Path("/attachbysync")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will create a new document in the database
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the Document entity with a DocumentPage embedded
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated document.
	 *     2. Error String if there was an issue
	 */
	public Response attachBySyncId(InputStream incomingData, 
			@DefaultValue("0") @QueryParam("doc_id") String syncId) {
		String result = "/tag/attach com.wwf.shrimp.application [CREATE NEW TAG ATTACHEMENTS]";
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize service
		tagService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
			
			// parse the JSON input into the specific class
			Type listType = new TypeToken<ArrayList<TagData>>(){}.getType();
			List<TagData> tags = new Gson().fromJson(reader, listType);
			
			// fetch the proper id
			long docId = tagService.getDocIdBySyncId(syncId);
			getLog().info("Attaching Tags For Sync DocId :" + syncId);
			
			tagService.attach(tags, docId);


		} catch (Exception e) {
			getLog().error("Error attaching tags: - ", e);
			
		}

		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(result).build();
	}

}
