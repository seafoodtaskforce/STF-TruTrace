package com.wwf.shrimp.application.services.main.rest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.wwf.shrimp.application.models.Group;
import com.wwf.shrimp.application.models.GroupType;
import com.wwf.shrimp.application.models.Organization;
import com.wwf.shrimp.application.models.OrganizationStage;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.search.OrganizationSearchCriteria;
import com.wwf.shrimp.application.models.search.UserSearchCriteria;
import com.wwf.shrimp.application.services.main.BaseRESTService;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.OrganizationMySQLDao;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.UserMySQLDao;
import com.wwf.shrimp.application.utils.RESTUtility;


/**
 * Simple RESTful organization and group based services
 * 
 * <TODO> Change the verbs to be fully restful 
 * @author argolite
 *
 */
@Path("/organization")
public class OrganizationRESTService extends BaseRESTService {
	private OrganizationMySQLDao<Organization, OrganizationSearchCriteria> organizationService = new OrganizationMySQLDao<Organization, OrganizationSearchCriteria>();
	private UserMySQLDao<User, UserSearchCriteria> userService = new UserMySQLDao<User, UserSearchCriteria>();
	
	
	@POST
	@Path("/creategrouporganization")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will create a new group organization in the database
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the group organization entity with all additional data embedded
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated user.
	 *     2. Error String if there was an issue
	 */
	public Response create(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		
		Group newGroup = null;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		organizationService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
			
			//
			// get the parser ready
			Gson gson = new GsonBuilder()
		            .setDateFormat("YYYY-MM-DD HH:MM:SS")
		            .create();
			
			// parse the JSON input into the specific class
			// newDocument = gson.fromJson(reader, Document.class);
			newGroup = gson.fromJson(reader, Group.class);
			System.out.println(newGroup.getName());
			System.out.println("New Group Organization Creation: " + newGroup);
			
			//
			// create the new user
			newGroup = organizationService.createOrganizationGroup(newGroup);

		} catch (Exception e) {
			getLog().error("Error Creating a new organization group: - " + e);
		}

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(newGroup)).build();
	}

	
	@POST
	@Path("/createstages")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will create a new group organization in the database
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the group organization entity with all additional data embedded
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated user.
	 *     2. Error String if there was an issue
	 */
	public Response createStages(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @HeaderParam("org-id") long orgID){
		
		List<OrganizationStage> newStages = null;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		organizationService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
			
			//
			// get the parser ready
			Gson gson = new GsonBuilder()
		            .setDateFormat("YYYY-MM-DD HH:MM:SS")
		            .create();
			
			// parse the JSON input into the specific class
			// newDocument = gson.fromJson(reader, Document.class);
			Type listType = new TypeToken<List<OrganizationStage>>() {}.getType();

			newStages = new Gson().fromJson(reader, listType);
			getLog().info("New Organization Stages Creation: " + newStages);
			
			//
			// Check for updates and new creations
			
			//
			// remove old stages
			//organizationService.deleteOrganizationStages(orgID);
			for(int i=0; i< newStages.size(); i++){
				newStages.get(i).setOrgID(orgID);
			}
			
			//
			// create the new stages
			newStages = organizationService.createOrganizationStages(newStages);

		} catch (Exception e) {
			getLog().error("Error Creating a new organization stages: - " + e);
		}

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(newStages)).build();
	}

	/**
	 * Fetch all organizations
	 * @param userName
	 * @return
	 */
	@GET
	@Path("/fetchall")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchAllOrganizations(
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		
		// results
		List<Organization> allOrganizations=null;
		
		getLog().info("HEADER user-name: " + userName);
		
		//
		// Initialize service
		organizationService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			// get all the organizations
			allOrganizations = organizationService.getAllOrganizations();
			// for each fetch the groups
			for(int i=0; i < allOrganizations.size(); i++){
				// currently empty will be added later
				// <TODO> Add the ability to fetch groups 
				
			}

		} catch (Exception e) {
			getLog().error("Error Fetching Organizations: - " + e.getStackTrace());
		}
		
		getLog().debug("Fetch all organizations result: " + allOrganizations);
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(allOrganizations)).build();
	}
	
	@GET
	@Path("/fetchlinked")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchOrganizationLinkedGroups(
			@DefaultValue("0") @QueryParam("org_id") long orgID
																) {
		
		// results
		Organization organization=null;
		
		// intermediate data
		List<Group> flatOrgGroup = null;
		List<Group> treeOrgGroup = null;
				
				
		//
		// Initialize service
		organizationService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			// fetch the data
			organization = organizationService.getOrganizationById(orgID);
			flatOrgGroup = organizationService.getAllGroupsByOrgId(orgID);
			treeOrgGroup = organizationService.getGroupTreeByOrgId(orgID);
			
			//
			// process the request and assemble the tree
			// map of parents to list of their rows
			Map<Long, List<Group>> parentMap = new HashMap<Long, List<Group>>();
			Map<Long, Group> flatGroupMap = new HashMap<Long, Group>();
			
			
			// find the list of rows for the parent, create it if it doesn't exist
			
			// process empty rows to be added for all groups under this org
			for(int i=0; i < flatOrgGroup.size(); i++){
				List<Group> rows = parentMap.get(flatOrgGroup.get(i).getId());
	            if (rows == null) {
	                rows = new ArrayList<Group>();
	                parentMap.put(flatOrgGroup.get(i).getId(), rows);
	            }
	            // add row to the list for its parent
	            rows.add(flatOrgGroup.get(i));
	            flatGroupMap.put(flatOrgGroup.get(i).getId(), flatOrgGroup.get(i));
			}
			
			// process the tree hierarchy
			for(int i=0; i < treeOrgGroup.size(); i++){
				List<Group> rows = parentMap.get(treeOrgGroup.get(i).getParentId());
				rows.add(flatGroupMap.get(treeOrgGroup.get(i).getChildId()));
			}
            
		} catch (Exception e) {
			getLog().error("Error Fetching Linked Organizations: - " + e.getStackTrace());

		}
		
		// add the data to organization
		organization.setSubGroups(treeOrgGroup);
		
		
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(organization)).build();
	}
	
	@GET
	@Path("/fetchtree")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchOrganizationTreeGroup(
			@DefaultValue("0") @QueryParam("org_id") long orgID
																) {
		
		// results
		Organization organization=null;
		
		// intermediate data
		List<Group> flatOrgGroup = null;
		List<Group> treeOrgGroup = null;
				
				
		//
		// Initialize service
		organizationService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			// fetch the data
			organization = organizationService.getOrganizationById(orgID);
			flatOrgGroup = organizationService.getAllGroupsByOrgId(orgID);
			treeOrgGroup = organizationService.getGroupTreeByOrgId(orgID);
			
			//
			// process the request and assemble the tree
			// map of parents to list of their rows
			Map<Long, List<Group>> parentMap = new HashMap<Long, List<Group>>();
			Map<Long, Group> flatGroupMap = new HashMap<Long, Group>();
			
			
			// find the list of rows for the parent, create it if it doesn't exist
			
			//
			// process empty rows to be added for all groups under this org
			// This will create a table of all organizations without linking them
			for(int i=0; i < flatOrgGroup.size(); i++){
				List<Group> rows = parentMap.get(flatOrgGroup.get(i).getId());
	            if (rows == null) {
	                rows = new ArrayList<Group>();
	                parentMap.put(flatOrgGroup.get(i).getId(), rows);
	            }
	            // add row to the list for its parent
	            rows.add(flatOrgGroup.get(i));
	            flatGroupMap.put(flatOrgGroup.get(i).getId(), flatOrgGroup.get(i));
			}
			
			// process the tree hierarchy by linking the elements from the 
			// flat group
			for(int i=0; i < treeOrgGroup.size(); i++){
				Group group = treeOrgGroup.get(i);
				long childIndex = group.getChildId();
				// add the data
				List<Group> subGroups = group.getSubGroups();
				if(subGroups == null){
					subGroups = new ArrayList<Group>();
				}
				subGroups.add(flatGroupMap.get(childIndex));
			}
			
		} catch (Exception e) {
			getLog().error("Error Fetching Organization Tree Groups: - " + e.getStackTrace());

		}
		
		// add the data to organization
		organization.setSubGroups(treeOrgGroup);
		
		
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(organization)).build();
	}
	
	
	/**
	 * Fetch the flat representation of the groups under the organization.
	 * THis view does not create a hierarchy but rather lists all the groups that are attached to this organization.
	 * 
	 * @param orgID - the organization id to get the groups for
	 * @return - the list of groups under this organization
	 */
	@GET
	@Path("/fetchflat")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchFlatOrganization(
			@DefaultValue("0") @QueryParam("org_id") long orgID
																) {
		
		// results
		Organization organization=null;
		
		// intermediate data
		List<Group> flatOrgGroup = null;
		

				
				
		//
		// Initialize service
		organizationService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			// fetch the data
			organization = organizationService.getOrganizationById(orgID);
			flatOrgGroup = organizationService.getAllGroupsByOrgId(orgID);
			
			
		} catch (Exception e) {
			getLog().error("Error Fetching Flat Organization representation: - " + e.getStackTrace());
		}
		
		// add the data to organization
		organization.setSubGroups(flatOrgGroup);
		
		
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(organization)).build();
	}
	
	/**
	 * Fetch the groups in this organization in this 
	 * This is the actual; headers of the stages
	 * 
	 * @param orgID - the organization id to get the groups for
	 * @return - the list of groups under this organization
	 */
	@GET
	@Path("/fetchstages")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchOrganizationStages(
			@DefaultValue("0") @QueryParam("org_id") long orgID
																) {
		
		// results
		List<OrganizationStage>  stages = null;

		//
		// Initialize service
		organizationService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			// fetch the data
			stages = organizationService.getAllGroupStages(orgID);
			
			
		} catch (Exception e) {
			getLog().error("Error Fetching Stage Data: - " + e);
		}
		
		
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(stages)).build();
	}
	
	
	/**
	 * Fetch all organization types
	 * @param userName
	 * @return
	 */
	@GET
	@Path("/fetchallgrouptypes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchAllGroupTypes(
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		
		// results
		List<GroupType> allGroupTypes=null;
		
		getLog().info("HEADER user-name: " + userName);
		
		//
		// Initialize service
		organizationService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			//
			// get the user data
			User user = userService.getUserByName(userName);
		
			// get all the organizations
			allGroupTypes = organizationService.getAllGroupTypes(user.getUserOrganizations().get(0).getId());


		} catch (Exception e) {
			getLog().error("Error Fetching Group Types: - " + e.getStackTrace());
		}
		
		getLog().debug("Fetch all organizations result: " + allGroupTypes);
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(allGroupTypes)).build();
	}
	
	/**
	 * Fetch all organization types
	 * @param userName
	 * @return
	 */
	@GET
	@Path("/fetchallgrouporganizations")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchAllGroupOrganizations(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("") @HeaderParam("group-type") String groupType) {
		
		// results
		List<Group> allGroupOrganizations=null;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("HEADER group-type: " + groupType);
		
		//
		// Initialize service
		organizationService.init();
		userService.init();
		
		
		
		/**
		 * Process the request
		 */
		try {
			//
			// get the user data
			User user = userService.getUserByName(userName);
		
			// get all the organizations
			if(groupType.isEmpty()){
				allGroupOrganizations = organizationService.getAllOrganizationGroups(user.getUserOrganizations().get(0).getId());
			}else{
				allGroupOrganizations = organizationService.getAllOrganizationGroups(user.getUserOrganizations().get(0).getId(), groupType);
			}
			


		} catch (Exception e) {
			getLog().error("Error Fetching Group Organizations: - " + e);
		}
		
		getLog().debug("Fetch all organization groups result: " + allGroupOrganizations);
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(allGroupOrganizations)).build();
	}
	
	@POST
	@Path("/allowedDocs")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * THis method will insert/update the profile image for the user
	 * @param incomingData - the user data with the profile image to set
	 * @return - Simple text response with success of the insertion
	 */
	public Response updateGroupAssignedDocs(
			@DefaultValue("") @HeaderParam("user-name") String username,
			@DefaultValue("0") @HeaderParam("groupId") long parentGroupId,
			@DefaultValue("") @HeaderParam("allowedDocs") String allowedDocs) {
		String result = "/user/profileimage com.wwf.shrimp.application [UPDATE ALLOWED DOCS - SUCCESS]";

		//
		// Initialize service
		organizationService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			// get the request reader ready 
			getLog().info("allowed Docs Update: " + username + " " + parentGroupId + " " + allowedDocs);

			organizationService.updateGroupAssignedDocs(parentGroupId, allowedDocs);
			

		} catch (Exception e) {
			getLog().error("Error Updating Allowed Docs: - " + e.getStackTrace()); 
			
		}

		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(result).build();
	}
	
}
