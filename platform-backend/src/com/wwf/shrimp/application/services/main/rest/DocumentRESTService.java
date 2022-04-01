package com.wwf.shrimp.application.services.main.rest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVWriter;
import com.sun.jersey.core.header.ContentDisposition;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;
import com.wwf.shrimp.application.exceptions.PersistenceException;
import com.wwf.shrimp.application.models.AuditAction;
import com.wwf.shrimp.application.models.AuditEntity;
import com.wwf.shrimp.application.models.Document;
import com.wwf.shrimp.application.models.DocumentCollection;
import com.wwf.shrimp.application.models.DocumentOCRMatchingData;
import com.wwf.shrimp.application.models.DocumentPage;
import com.wwf.shrimp.application.models.DocumentType;
import com.wwf.shrimp.application.models.DynamicFieldData;
import com.wwf.shrimp.application.models.DynamicFieldDefinition;
import com.wwf.shrimp.application.models.IdentifiableEntity;
import com.wwf.shrimp.application.models.NoteData;
import com.wwf.shrimp.application.models.NotificationData;
import com.wwf.shrimp.application.models.NotificationType;
import com.wwf.shrimp.application.models.Role;
import com.wwf.shrimp.application.models.Screening;
import com.wwf.shrimp.application.models.TagData;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.search.AuditSearchCriteria;
import com.wwf.shrimp.application.models.search.BaseSearchCriteria;
import com.wwf.shrimp.application.models.search.DocumentSearchCriteria;
import com.wwf.shrimp.application.models.search.NotificationSearchCriteria;
import com.wwf.shrimp.application.models.search.SearchResult;
import com.wwf.shrimp.application.models.search.UserSearchCriteria;
import com.wwf.shrimp.application.services.main.BaseRESTService;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.AuditMySQLDao;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.DocumentMySQLDao;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.NotificationMySQLDao;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.UserMySQLDao;
import com.wwf.shrimp.application.utils.DataSparseHelper;
import com.wwf.shrimp.application.utils.DateUtility;
import com.wwf.shrimp.application.utils.RESTUtility;

/**
 * A collection of RESTful services for document management.
 * 
 * <TODO> Change the verbs to be fully restful 
 * @author AleaActaEst
 *
 */

@Path("/document")
public class DocumentRESTService extends BaseRESTService {
	/**
	 * Services used by the implementation
	 */
	private DocumentMySQLDao<Document, DocumentSearchCriteria> documentService = new DocumentMySQLDao<Document, DocumentSearchCriteria>();
	private UserMySQLDao<User, UserSearchCriteria> userService = new UserMySQLDao<User, UserSearchCriteria>();
	private AuditMySQLDao<AuditEntity, AuditSearchCriteria> auditService = new AuditMySQLDao<AuditEntity, AuditSearchCriteria>();
	private NotificationMySQLDao<NotificationData, NotificationSearchCriteria> notificationService = new NotificationMySQLDao<NotificationData, NotificationSearchCriteria>();
	/**
	 * Global data
	 */
	public static final String CUSTOM_TAG_PREFIX = "CUSTOM: ";
	public static final String DOC_INFO_DATA_FORMATTING_DELIMITER = "~@@@~"; 
	public static final String DOC_INFO_DATA_FORMATTING_DELIMITER_SPLITTER = "~;~"; 
	

	@GET
	@Path("/fetchall")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all the documents
	 * @param userName - the user who is asking for these documents
	 * @return - all the documents that this user has access to
	 */
	public Response fetchAllDocuments(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("") @HeaderParam("doc-type") String docType) {
		// results
		List<Document> allDocuments = new ArrayList<Document>();
		User user = null;
		
		Status httpResponseStatus = Status.OK;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("HEADER doc-type: " + docType);
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			allDocuments = documentService.getAllDocuments(user, docType, false); 

		} catch (Exception e) {
			getLog().error("Error Fetching Documents: - " + e);
			httpResponseStatus = Status.NOT_FOUND;
		}
		
		getLog().debug("FETCH ALL: - Result" + allDocuments);
				// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(RESTUtility.getJSON(allDocuments)).build();
	}
	
	@GET
	@Path("/fetchtracedataexport_v2")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all the documents
	 * @param userName - the user who is asking for these documents
	 * @return - all the documents that this user has access to
	 */
	public Response fetchTraceDataExport_v2(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("") @HeaderParam("doc-type") String docType) {
		// results
		List<Document> allDocuments = new ArrayList<Document>();
		List<Document> traceDocuments = new ArrayList<Document>();
		List<Document> tempTraceDocuments = new ArrayList<Document>();
		List<Document> allDocumentsWithoutDuplicates = new ArrayList<Document>();
		User user = null;
		
		Status httpResponseStatus = Status.OK;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("HEADER doc-type: " + docType);
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
		
		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			
			//
			// Preconditions
			if(!user.getRoles().get(0).getName().equals(Role.ROLE_NAME_MATRIX_ADMIN)){
				httpResponseStatus = Status.UNAUTHORIZED;
				return Response.status(httpResponseStatus).entity(RESTUtility.getJSON(allDocuments)).build();
			}
			
			//
			// swap role temporarily
			user.getRoles().get(0).setName(Role.ROLE_NAME_GENERAL_USER);
			
			allDocuments = documentService.getAllDocuments_v2(user, docType, false); 
			
			//
			// extract all trace documents
			ListIterator<Document> iter = allDocuments.listIterator();
			
			// create the list of all linked docs
			while(iter.hasNext()){
				Document iterDoc = iter.next();
				//if(iterDoc.getAttachedDocuments().size() > 0){
				//	allLinkedDocs.addAll(iterDoc.getAttachedDocuments());
				//}
				if(iterDoc.getLinkedDocuments().size() > 0){
					tempTraceDocuments = documentService.getRecursiveDocumentById(iterDoc.getId());
					if(tempTraceDocuments.size() > 0){
						traceDocuments.addAll(tempTraceDocuments);
					}
				}
			}
			
			//
			// reset role
			user.getRoles().get(0).setName(Role.ROLE_NAME_MATRIX_ADMIN);
			//
			// consolidate all documents
			allDocuments.addAll(traceDocuments);
			//
			// remove dupliucates
			allDocumentsWithoutDuplicates = new ArrayList<>(
				      new HashSet<>(allDocuments));
			

		} catch (Exception e) {
			getLog().error("Error Fetching Documents: - " + e);
			httpResponseStatus = Status.NOT_FOUND;
		}
		
		getLog().debug("FETCH ALL: - Result" + allDocuments);
				// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(RESTUtility.getJSON(allDocumentsWithoutDuplicates)).build();
	}
	
	
	@GET
	@Path("/fetchall_v2")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all the documents
	 * @param userName - the user who is asking for these documents
	 * @return - all the documents that this user has access to
	 */
	public Response fetchAllDocuments_v2(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("") @HeaderParam("doc-type") String docType) {
		// results
		List<Document> allDocuments = new ArrayList<Document>();
		User user = null;
		
		Status httpResponseStatus = Status.OK;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("HEADER doc-type: " + docType);
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			allDocuments = documentService.getAllDocuments_v2(user, docType, false); 

		} catch (Exception e) {
			getLog().error("Error Fetching Documents: - " + e);
			httpResponseStatus = Status.NOT_FOUND;
		}
		
		getLog().debug("FETCH ALL: - Result" + allDocuments);
				// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(RESTUtility.getJSON(allDocuments)).build();
	}
	
	@GET
	@Path("/fetchallrecursive")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all the documents
	 * @param userName - the user who is asking for these documents
	 * @return - all the documents that this user has access to
	 */
	public Response fetchAllDocumentsRecursive(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @HeaderParam("doc-id") long docId) {
		// results
		List<Document> allDocuments=null;
		User user = null;
		Status httpResponseStatus = Status.OK;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("HEADER doc-id: " + docId);
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			allDocuments = documentService.getRecursiveDocumentById(docId); 

		} catch (Exception e) {
			getLog().error("Error Fetching Documents: - " + e);
			httpResponseStatus = Status.NOT_FOUND;
		}
		
		getLog().debug("FETCH ALL RECURSIVE: - Result" + allDocuments);
				// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(RESTUtility.getJSON(allDocuments)).build();
	}
	
	@GET
	@Path("/fetchalldocstolink")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all the documents
	 * @param userName - the user who is asking for these documents
	 * @return - all the documents that this user has access to
	 */
	public Response fetchAllDocumentsLinkList(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("") @HeaderParam("doc-type") String docType,
			@DefaultValue("") @HeaderParam("doc_id") String syncId,
			@DefaultValue("") @HeaderParam("custom_tag") String customTag) {
		// results
		List<Document> allDocuments=new ArrayList<Document>();
		User user = null;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("HEADER doc-type: " + docType);
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			// extract only the documents that the user can access
			allDocuments = documentService.getAllDocsToLink(user, docType, customTag, syncId);


		} catch (Exception e) {
			getLog().error("Error Fetching Linked Documents: - " + e);
		}
		
		getLog().debug("FETCH ALL Linked Docs: - Result" + allDocuments);
				// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(allDocuments)).build();
	}
	
	
	
	@GET
	@Path("/fetchalldocstolink_v2")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all the documents
	 * @param userName - the user who is asking for these documents
	 * @return - all the documents that this user has access to
	 */
	public Response fetchAllDocumentsLinkList_v2(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("") @HeaderParam("doc-type") String docType,
			@DefaultValue("") @HeaderParam("doc_id") String syncId,
			@DefaultValue("") @HeaderParam("custom_tag") String customTag) {
		// results
		List<Document> allDocuments=new ArrayList<Document>();
		User user = null;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("HEADER doc-type: " + docType);
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			// extract only the documents that the user can access
			allDocuments = documentService.getAllDocsToLink_v2(user, docType, customTag, syncId);


		} catch (Exception e) {
			getLog().error("Error Fetching Linked Documents: - " + e);
		}
		
		getLog().debug("FETCH ALL Linked Docs: - Result" + allDocuments);
				// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(allDocuments)).build();
	}
	
	@GET
	@Path("/fetchalldocstoattach")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all the documents
	 * @param userName - the user who is asking for these documents
	 * @return - all the documents that this user has access to
	 */
	public Response fetchAllDocumentsAttachList(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("") @HeaderParam("doc-type") String docType,
			@DefaultValue("") @HeaderParam("doc_id") String syncId,
			@DefaultValue("") @HeaderParam("custom_tag") String customTag) {
		// results
		List<Document> allDocuments=null;
		User user = null;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("HEADER doc-type: " + docType);
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			// extract only the documents that the user can access
			allDocuments = documentService.getAllDocsToAttach(user, docType, customTag, syncId);


		} catch (Exception e) {
			getLog().error("Error Fetching Backing Documents: - " + e);
		}
		
		getLog().debug("FETCH ALL Backing Docs: - Result" + allDocuments);
				// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(allDocuments)).build();
	}	
	

	
	@GET
	@Path("/fetchalldocstoattach_v2")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all the documents
	 * @param userName - the user who is asking for these documents
	 * @return - all the documents that this user has access to
	 */
	public Response fetchAllDocumentsAttachList_v2(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("") @HeaderParam("doc-type") String docType,
			@DefaultValue("") @HeaderParam("doc_id") String syncId,
			@DefaultValue("") @HeaderParam("custom_tag") String customTag) {
		// results
		List<Document> allDocuments=null;
		User user = null;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("HEADER doc-type: " + docType);
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			// extract only the documents that the user can access
			allDocuments = documentService.getAllDocsToAttach_v2(user, docType, customTag, syncId);


		} catch (Exception e) {
			getLog().error("Error Fetching Backing Documents: - " + e);
		}
		
		getLog().debug("FETCH ALL Backing Docs: - Result" + allDocuments);
				// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(allDocuments)).build();
	}	
	
	
	@GET
	@Path("/fetchattachdoccollection")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all the documents
	 * @param userName - the user who is asking for these documents
	 * @return - all the documents that this user has access to
	 */
	public Response fetchAllDocumentsAttachCollection(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @QueryParam("doc_id") String syncId){
		// results
		List<Document> allDocuments=null;
		List<Document> allAttachedDocs = new ArrayList<Document>();
		DocumentCollection resultDocs = new DocumentCollection();
		User user = null;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("QUERY doc_id: " + syncId);
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			// extract only the documents that the user can access
			allDocuments = documentService.getAllDocsToAttach(user, "", "", syncId);
			if(!syncId.equals("0")){
				allAttachedDocs = documentService.getAllAttachedDocsByDocSyncId(syncId);
			}
			resultDocs.setAllDocsToAttach(allDocuments);
			resultDocs.setAttachedDocs(allAttachedDocs);


		} catch (Exception e) {
			getLog().error("Error Fetching Backing Documents Collection: - " + e);
		}
		
		getLog().debug("FETCH ALL Backing Docs Collection: - Result" + allDocuments);
				// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(resultDocs)).build();
	}	
	
	
	@GET
	@Path("/fetchattachdoccollection_v2")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all the documents
	 * @param userName - the user who is asking for these documents
	 * @return - all the documents that this user has access to
	 */
	public Response fetchAllDocumentsAttachCollection_v2(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @QueryParam("doc_id") String syncId){
		// results
		List<Document> allDocuments=null;
		List<Document> allAttachedDocs = new ArrayList<Document>();
		DocumentCollection resultDocs = new DocumentCollection();
		User user = null;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("QUERY doc_id: " + syncId);
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			// extract only the documents that the user can access
			allDocuments = documentService.getAllDocsToAttach_v2(user, "", "", syncId);
			if(!syncId.equals("0")){
				allAttachedDocs = documentService.getAllAttachedDocsByDocSyncId(syncId);
			}
			resultDocs.setAllDocsToAttach(allDocuments);
			resultDocs.setAttachedDocs(allAttachedDocs);


		} catch (Exception e) {
			getLog().error("Error Fetching Backing Documents Collection: - " + e);
		}
		
		getLog().debug("FETCH ALL Backing Docs Collection: - Result" + allDocuments);
				// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(resultDocs)).build();
	}	
	
	
	
	@GET
	@Path("/fetchlinkdoccollection")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all the documents
	 * @param userName - the user who is asking for these documents
	 * @return - all the documents that this user has access to
	 */
	public Response fetchAllDocumentsLinkCollection(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @QueryParam("doc_id") String syncId){
		// results
		List<Document> allDocuments=null;
		List<Document> allLinkedDocs = new ArrayList<Document>();
		DocumentCollection resultDocs = new DocumentCollection();
		User user = null;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("QUERY doc_id: " + syncId);
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			// extract only the documents that the user can access
			allDocuments = documentService.getAllDocsToLink(user, "", "", syncId);
			if(!syncId.equals("0")){
				allLinkedDocs = documentService.getAllLinkedDocsByDocSyncId(syncId);
			}
			resultDocs.setAllDocsToLink(allDocuments);
			resultDocs.setLinkedDocs(allLinkedDocs);


		} catch (Exception e) {
			getLog().error("Error Fetching Linking Documents Collection: - " + e);
		}
		
		getLog().debug("FETCH ALL Linking Docs Collection: - Result" + allDocuments);
				// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(resultDocs)).build();
	}	
	
	
	@GET
	@Path("/fetchlinkdoccollection_v2")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all the documents
	 * @param userName - the user who is asking for these documents
	 * @return - all the documents that this user has access to
	 */
	public Response fetchAllDocumentsLinkCollection_v2(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @QueryParam("doc_id") String syncId){
		// results
		List<Document> allDocuments=null;
		List<Document> allLinkedDocs = new ArrayList<Document>();
		DocumentCollection resultDocs = new DocumentCollection();
		User user = null;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("QUERY doc_id: " + syncId);
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			// extract only the documents that the user can access
			allDocuments = documentService.getAllDocsToLink_v2(user, "", "", syncId);
			if(!syncId.equals("0")){
				allLinkedDocs = documentService.getAllLinkedDocsByDocSyncId(syncId);
			}
			resultDocs.setAllDocsToLink(allDocuments);
			resultDocs.setLinkedDocs(allLinkedDocs);


		} catch (Exception e) {
			getLog().error("Error Fetching Linking Documents Collection: - " + e);
		}
		
		getLog().debug("FETCH ALL Linking Docs Collection: - Result" + allDocuments);
				// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(resultDocs)).build();
	}	
	
	
	
	@Path("/files")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFiles(@DefaultValue("") @FormDataParam("tags") String tags,
			@FormDataParam("files") List<FormDataBodyPart> bodyParts,
			@FormDataParam("files") FormDataContentDisposition fileDispositions,
			@FormDataParam("userName") String username, 
			@FormDataParam("creationDate") String creationDate,
			@FormDataParam("updationDate") String updationDate,
			@FormDataParam("docTypeName") String docTypeName, 
			@FormDataParam("docTypeId") long docTypeId, 
			@FormDataParam("docTypeHexColorCode") String docTypeHexColorCode,
			@FormDataParam("docRecipients") String docRecipients,
			@FormDataParam("docLinkedDocs") String docLinkedDocs,
			@FormDataParam("docBackingDocs") String docBackingDocs,
			@FormDataParam("docImportDocTags") String docImportDocTags,
			@FormDataParam("docImportDocStatus") String docImportDocStatus,
			@FormDataParam("docInfoDynamicData") String docInfoDynamicData) {
		
		
		Document responseDoc=null;
		if(docImportDocStatus == null){
			docImportDocStatus=Document.STATUS_DRAFT;
		}
		
		//
		// initialize services
		documentService.init();
		userService.init();
		
		getLog().info("Recipients for the upload: - " + docRecipients);
		getLog().info("Upload <status>: - " + docImportDocStatus);
		
		getLog().info("[Upload Doc Update] <recipients> - " + docRecipients);
		getLog().info("[Upload Doc Update] <status>: - " + docImportDocStatus);
		getLog().info("[Upload Doc Update] <userName>: - " + username);
		getLog().info("[Upload Doc Update] <creationDate>: - " + creationDate);
		getLog().info("[Upload Doc Update] <docTypeName>: - " + docTypeName);
		getLog().info("[Upload Doc Update] <docTypeId>: - " + docTypeId);
		getLog().info("[Upload Doc Update] <docLinkedDocs>: - " + docLinkedDocs);
		getLog().info("[Upload Doc Update] <docBackingDocs>: - " + docBackingDocs);
		getLog().info("[Upload Doc Update] <docImportDocTags>: - " + docImportDocTags);
		getLog().info("[Upload Doc Update] <docInfoDynamicData>: - " + docInfoDynamicData);
		getLog().info("[Upload Doc Update] <docTypeHexColorCode>: - " + docTypeHexColorCode);
		
		//
		// initialize Document Data
		Document doc = null;
		User user = null;
				
		try {
			doc = new Document();
			// set the creation/updation timestamp
			doc.setCreationTimestamp(creationDate);
			doc.setUpdationTimestamp(updationDate);
			// set the user data
			user = userService.getUserByName(username);
			doc.setOwner(user.getName());
			doc.setOwnerId(user.getId());
			long organizationId  = 0;
			long groupId  = 0;
			// set the group data
			if(user.getUserOrganizations().size() > 0) {
	            organizationId = user.getUserOrganizations().get(0).getId();
	            groupId = user.getUserGroups().get(0).getId();
	        }
			doc.setOrganizationId(organizationId);
			doc.setGroupId(groupId);
			
			// doc type data
			List<DocumentType> docTypes = documentService.getAllDocumentTypes();
			
			DocumentType docType = new DocumentType();
			docType.setId(docTypeId);
			docType.setName(docTypeName);
			docType.setValue(docTypeName);
			docType.setHexColorCode(docTypeHexColorCode);
			docType = docTypes.get(docTypes.indexOf(docType));
			doc.setType(docType);
			doc.setDocumentType(docTypeName);
			doc.setTypeHEXColor(docTypeHexColorCode);
			
			// set the sync id
			doc.setSyncID(UUID.randomUUID().toString());
			// Status
			doc.setStatus(docImportDocStatus);
			
			//
			// get recipients
			List<User> recipients = new ArrayList<User>();
			if(!docRecipients.trim().isEmpty()){
				String[] recipientIds = docRecipients.split(",");
				for(int i=0; i< recipientIds.length; i++){
					long id = Long.parseLong(recipientIds[i]);
					User recipient = new User();
					recipient.setId(id);
					recipients.add(recipient);
				}
				doc.setToRecipients(recipients);
			}
			
			//
			// get doc data
			List<DynamicFieldData> dynamicDocData = new ArrayList<DynamicFieldData>();
			if(!docInfoDynamicData.trim().isEmpty()){
				String[] dynamicDocDataElements = docInfoDynamicData.split(DocumentRESTService.DOC_INFO_DATA_FORMATTING_DELIMITER_SPLITTER);
				for(int i=0; i< dynamicDocDataElements.length; i++){
					// extract the sub-elements
					String[] dataElement = dynamicDocDataElements[i].split(DocumentRESTService.DOC_INFO_DATA_FORMATTING_DELIMITER);
					
					long id = Long.parseLong(dataElement[0]);
					String data = dataElement[1];
					DynamicFieldData docInfoData = new DynamicFieldData();
					docInfoData.setDynamicFieldDefinitionId(id);
					docInfoData.setData(data);
					dynamicDocData.add(docInfoData);
				}
				doc.setDynamicFieldData(dynamicDocData);
			}
			
			//
			// get linked docs
			List<Document> linkedDocs = new ArrayList<Document>();
			if(!docLinkedDocs.trim().isEmpty()){
				String[] linkedDocsIds = docLinkedDocs.split(",");
				for(int i=0; i< linkedDocsIds.length; i++){
					long id = Long.parseLong(linkedDocsIds[i]);
					Document linkedDoc = new Document();
					linkedDoc.setId(id);
					linkedDocs.add(linkedDoc);
				}
				doc.setLinkedDocuments(linkedDocs);
			}
			
			//
			// get backing docs
			List<Document> backingDocs = new ArrayList<Document>();
			if(!docBackingDocs.trim().isEmpty()){
				String[] backingDocsIds = docBackingDocs.split(",");
				for(int i=0; i< backingDocsIds.length; i++){
					long id = Long.parseLong(backingDocsIds[i]);
					Document backingDoc = new Document();
					backingDoc.setId(id);
					backingDocs.add(backingDoc);
				}
				doc.setAttachedDocuments(backingDocs);
			}
			
			//
			// get tags
			List<TagData> docTags = new ArrayList<TagData>();
			if(!docImportDocTags.trim().isEmpty()){
				String[] docTagsIds = docImportDocTags.split(DocumentRESTService.DOC_INFO_DATA_FORMATTING_DELIMITER_SPLITTER);
				for(int i=0; i< docTagsIds.length; i++){
					long id = Long.parseLong(docTagsIds[i]);
					TagData docTag = new TagData();
					docTag.setId(id);
					docTags.add(docTag);
				}
				doc.setTags(docTags);
			}	
			
		} catch (PersistenceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i < bodyParts.size(); i++) {
			/*
			 * Casting FormDataBodyPart to BodyPartEntity, which can give us
			 * InputStream for uploaded file
			 */
			BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyParts.get(i).getEntity();
			//
			// process
			try {
				List<DocumentPage> pages = documentService.extractPDFDocumentPages(bodyPartEntity.getInputStream());
				// recollate the pages
				for(int index = 0; index < pages.size(); index++){
					pages.get(index).setPageNumber(doc.getPages().size() + (index+1));
				}
				doc.addPages(pages);
			} catch (PersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		//
		// create the document
		try {
			responseDoc = documentService.create(doc);
		} catch (PersistenceException | IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		// get the sparse version of the doc
		Document newDoc  = null;
		try {
			newDoc = documentService.getDocumentById(responseDoc.getId()).get(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getLog().info("CreatedPDF-Based Doc: - " + newDoc);
		return Response.status(200)
				.entity(RESTUtility.getJSON(newDoc)).build();
		
	}
	
	
	@Path("/files")
	@PUT
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadUpdatedFiles(
			@FormDataParam("docId") long docId,
			@DefaultValue("") @FormDataParam("tags") String tags,
			@FormDataParam("files") List<FormDataBodyPart> bodyParts,
			@FormDataParam("files") FormDataContentDisposition fileDispositions,
			@FormDataParam("userName") String username, 
			@FormDataParam("creationDate") String creationDate,
			@FormDataParam("updationDate") String updationDate,
			@FormDataParam("docTypeName") String docTypeName, 
			@FormDataParam("docTypeId") long docTypeId, 
			@FormDataParam("docTypeHexColorCode") String docTypeHexColorCode,
			@FormDataParam("docRecipients") String docRecipients,
			@FormDataParam("docLinkedDocs") String docLinkedDocs,
			@FormDataParam("docBackingDocs") String docBackingDocs,
			@FormDataParam("docImportDocTags") String docImportDocTags,
			@FormDataParam("docExistingPageData") String docExistingPageData,
			@FormDataParam("docImportDocStatus") String docImportDocStatus,
			@FormDataParam("docInfoDynamicData") String docInfoDynamicData) {

		Document responseDoc=null;
		if(docImportDocStatus == null){
			docImportDocStatus=Document.STATUS_DRAFT;
		}
		
		//
		// initialize services
		documentService.init();
		userService.init();
		
		getLog().info("[Upload Doc Update] <recipients> - " + docRecipients);
		getLog().info("[Upload Doc Update] <status>: - " + docImportDocStatus);
		getLog().info("[Upload Doc Update] <userName>: - " + username);
		getLog().info("[Upload Doc Update] <creationDate>: - " + creationDate);
		getLog().info("[Upload Doc Update] <updationDate>: - " + updationDate);
		getLog().info("[Upload Doc Update] <docTypeName>: - " + docTypeName);
		getLog().info("[Upload Doc Update] <docTypeId>: - " + docTypeId);
		getLog().info("[Upload Doc Update] <docExistingPageData>: - " + docExistingPageData);
		getLog().info("[Upload Doc Update] <docLinkedDocs>: - " + docLinkedDocs);
		getLog().info("[Upload Doc Update] <docBackingDocs>: - " + docBackingDocs);
		getLog().info("[Upload Doc Update] <docImportDocTags>: - " + docImportDocTags);
		getLog().info("[Upload Doc Update] <docInfoDynamicData>: - " + docInfoDynamicData);
		getLog().info("[Upload Doc Update] <docTypeHexColorCode>: - " + docTypeHexColorCode);
		
		//
		// initialize Document Data
		Document doc = null;
		User user = null;
		List<DocumentPage> existingPages = null;
		
		try {
			doc = new Document();
			doc.setId(docId);
			// set the creation timestamp
			doc.setCreationTimestamp(creationDate);
			doc.setUpdationTimestamp(updationDate);
			// set the user data
			user = userService.getUserByName(username);
			doc.setOwner(user.getName());
			long organizationId  = 0;
			long groupId  = 0;
			// set the group data
			if(user.getUserOrganizations().size() > 0) {
	            organizationId = user.getUserOrganizations().get(0).getId();
	            groupId = user.getUserGroups().get(0).getId();
	        }
			doc.setOrganizationId(organizationId);
			doc.setGroupId(groupId);
			
			// doc type data
			List<DocumentType> docTypes = documentService.getAllDocumentTypes();
			
			DocumentType docType = new DocumentType();
			docType.setId(docTypeId);
			docType.setName(docTypeName);
			docType.setValue(docTypeName);
			docType.setHexColorCode(docTypeHexColorCode);
			docType = docTypes.get(docTypes.indexOf(docType));
			doc.setType(docType);
			doc.setDocumentType(docTypeName);
			doc.setTypeHEXColor(docTypeHexColorCode);
			
			// set the sync id
			doc.setSyncID(UUID.randomUUID().toString());
			// Status
			doc.setStatus(docImportDocStatus);
			
			//
			// get recipients
			List<User> recipients = new ArrayList<User>();
			if(!docRecipients.trim().isEmpty()){
				String[] recipientIds = docRecipients.split(",");
				for(int i=0; i< recipientIds.length; i++){
					long id = Long.parseLong(recipientIds[i]);
					User recipient = new User();
					recipient.setId(id);
					recipients.add(recipient);
				}
				doc.setToRecipients(recipients);
			}
			
			// 
			// get Pages
			existingPages = new ArrayList<DocumentPage>();
			if(docExistingPageData != null){
				if(!docExistingPageData.trim().isEmpty()){
					String[] pageIds = docExistingPageData.split(",");
					for(int i=0; i< pageIds.length; i++){
						long id = Long.parseLong(pageIds[i]);
						DocumentPage page = new DocumentPage();
						page.setId(id);
						page.setPageNumber(i+1);
						existingPages.add(page);
					}
				}
			}
			
			
			//
			// get doc data
			List<DynamicFieldData> dynamicDocData = new ArrayList<DynamicFieldData>();
			if(!docInfoDynamicData.trim().isEmpty()){
				String[] dynamicDocDataElements = docInfoDynamicData.split(DocumentRESTService.DOC_INFO_DATA_FORMATTING_DELIMITER_SPLITTER);
				for(int i=0; i< dynamicDocDataElements.length; i++){
					// extract the sub-elements
					String[] dataElement = dynamicDocDataElements[i].split(DocumentRESTService.DOC_INFO_DATA_FORMATTING_DELIMITER);
					
					long id = Long.parseLong(dataElement[0]);
					String data = dataElement[1];
					DynamicFieldData docInfoData = new DynamicFieldData();
					docInfoData.setDynamicFieldDefinitionId(id);
					docInfoData.setData(data);
					dynamicDocData.add(docInfoData);
				}
				doc.setDynamicFieldData(dynamicDocData);
			}
			
			//
			// get linked docs
			List<Document> linkedDocs = new ArrayList<Document>();
			if(!docLinkedDocs.trim().isEmpty()){
				String[] linkedDocsIds = docLinkedDocs.split(",");
				for(int i=0; i< linkedDocsIds.length; i++){
					long id = Long.parseLong(linkedDocsIds[i]);
					Document linkedDoc = new Document();
					linkedDoc.setId(id);
					linkedDocs.add(linkedDoc);
				}
				doc.setLinkedDocuments(linkedDocs);
			}
			
			//
			// get backing docs
			List<Document> backingDocs = new ArrayList<Document>();
			if(!docBackingDocs.trim().isEmpty()){
				String[] backingDocsIds = docBackingDocs.split(",");
				for(int i=0; i< backingDocsIds.length; i++){
					long id = Long.parseLong(backingDocsIds[i]);
					Document backingDoc = new Document();
					backingDoc.setId(id);
					backingDocs.add(backingDoc);
				}
				doc.setAttachedDocuments(backingDocs);
			}
			
			//
			// get tags
			List<TagData> docTags = new ArrayList<TagData>();
			if(!docImportDocTags.trim().isEmpty()){
				String[] docTagsIds = docImportDocTags.split(",");
				for(int i=0; i< docTagsIds.length; i++){
					long id = Long.parseLong(docTagsIds[i]);
					TagData docTag = new TagData();
					docTag.setId(id);
					docTags.add(docTag);
				}
				doc.setTags(docTags);
			}	
			
		} catch (PersistenceException e1) {
			// TODO Auto-generated catch block
			getLog().error("[Upload Doc Update] Error Creating Doc Data: " + e1);
			getLog().error("[Upload Doc Update] Error Creating Doc Data: <stack-trace> : " + ExceptionUtils.getStackTrace(e1));
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			getLog().error("[Upload Doc Update] Error Creating Doc Data: " + e);
			getLog().error("[Upload Doc Update] Error Creating Doc Data: <stack-trace> : " + ExceptionUtils.getStackTrace(e));
		}
		if(bodyParts != null){
			for (int i = 0; i < bodyParts.size(); i++) {
				/*
				 * Casting FormDataBodyPart to BodyPartEntity, which can give us
				 * InputStream for uploaded file
				 */
				BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyParts.get(i).getEntity();
				//
				// process
				try {
					List<DocumentPage> pages = documentService.extractPDFDocumentPages(bodyPartEntity.getInputStream());
					// re-collate the pages
					for(int index = 0; index < pages.size(); index++){
						pages.get(index).setPageNumber(doc.getPages().size() + (index+1));
					}
					doc.addPages(pages);
				} catch (PersistenceException e) {
					// TODO Auto-generated catch block
					getLog().error("[Upload Doc Update] Error Extracting PDF: " + e);
					getLog().error("[Upload Doc Update] Error Extracting PDF: <stack-trace> : " + ExceptionUtils.getStackTrace(e));
				}
			}
		}
		
		
		//
		// get the current version of the document
		Document oldDoc = null;
		try {
			
			oldDoc = documentService.getDocumentById(docId).get(0);
			//
			// transfer any old pages to the new doc
			documentService.deleteOmmitedPages(oldDoc,existingPages);
			
			//
			// re-collate all the pages in the new doc
			if(oldDoc.getPages().size() > 0){
				for(int i=0; i<doc.getPages().size(); i++){
					doc.getPages().get(i).setPageNumber(doc.getPages().get(i).getPageNumber() + oldDoc.getPages().size());
				}
				for(int i=0; i<oldDoc.getPages().size(); i++){
					doc.getPages().add(0, oldDoc.getPages().get(i));
				}
			}

		} catch (PersistenceException | IllegalArgumentException e2) {
			// TODO Auto-generated catch block
			getLog().error("[Upload Doc Update] Error Syncing Update Pages: " + e2);
			getLog().error("[Upload Doc Update] Error Syncing Update Pages: <stack-trace> : " + ExceptionUtils.getStackTrace(e2));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			getLog().error("[Upload Doc Update] Error Syncing Update Pages: " + e);
			getLog().error("[Upload Doc Update] Error Syncing Update Pages: <stack-trace> : " + ExceptionUtils.getStackTrace(e));
		}
		
		//
		// get the old page data from the old doc
		

		//
		// create the document
		try {
			//
			// reset session id
			doc.setSyncID(oldDoc.getSyncID()); 
			getLog().info("Upload <doc data>: - " + doc);
			responseDoc = documentService.update(doc);
		} catch (PersistenceException | IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// get the sparse version of the doc
		Document newDoc  = null;
		try {
			newDoc = documentService.getDocumentById(responseDoc.getId()).get(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getLog().info("Updated PDF-Based Doc: - " + newDoc);
		return Response.status(200)
				.entity(RESTUtility.getJSON(newDoc)).build();
		
	}
	
	
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	/**
	 * Returns text response to caller containing uploaded file location
	 * 
	 * @return error response in case of missing parameters an internal
	 *         exception or success response if file has been stored
	 *         successfully
	 */
	public Response uploadFile(
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail,
			@FormDataParam("userName") String username, 
			@FormDataParam("creationDate") String creationDate,
			@FormDataParam("updationDate") String updationDate,
			@FormDataParam("docTypeName") String docTypeName, 
			@FormDataParam("docTypeId") long docTypeId, 
			@FormDataParam("docTypeHexColorCode") String docTypeHexColorCode,
			@FormDataParam("docRecipients") String docRecipients,
			@FormDataParam("docLinkedDocs") String docLinkedDocs,
			@FormDataParam("docBackingDocs") String docBackingDocs,
			@FormDataParam("docImportDocTags") String docImportDocTags,
			@FormDataParam("docImportDocStatus") String docImportDocStatus)	{
		
		Document responseDoc=null;
		
		//
		// check if all form parameters are provided
		if (uploadedInputStream == null || fileDetail == null)
			return Response.status(400).entity("Invalid form data").build();
		if(docImportDocStatus == null){
			docImportDocStatus=Document.STATUS_DRAFT;
		}
		
		//
		// initialize services
		documentService.init();
		userService.init();
		
		getLog().info("Recipients for the upload: - " + docRecipients);
		getLog().info("Upload <status>: - " + docImportDocStatus);
		
		//
		// initialize Document Data
		Document doc = null;
		User user = null;
		
		try {
			doc = new Document();
			// set the creation timestamp
			doc.setCreationTimestamp(creationDate);
			doc.setUpdationTimestamp(updationDate);
			// set the user data
			user = userService.getUserByName(username);
			doc.setOwner(user.getName());
			long organizationId  = 0;
			long groupId  = 0;
			// set the group data
			if(user.getUserOrganizations().size() > 0) {
	            organizationId = user.getUserOrganizations().get(0).getId();
	            groupId = user.getUserGroups().get(0).getId();
	        }
			doc.setOrganizationId(organizationId);
			doc.setGroupId(groupId);
			
			// doc type data
			List<DocumentType> docTypes = documentService.getAllDocumentTypes();
			
			DocumentType docType = new DocumentType();
			docType.setId(docTypeId);
			docType.setName(docTypeName);
			docType.setValue(docTypeName);
			docType.setHexColorCode(docTypeHexColorCode);
			docType = docTypes.get(docTypes.indexOf(docType));
			doc.setType(docType);
			doc.setDocumentType(docTypeName);
			doc.setTypeHEXColor(docTypeHexColorCode);
			
			// set the sync id
			doc.setSyncID(UUID.randomUUID().toString());
			// Status
			doc.setStatus(docImportDocStatus);
			
			//
			// get recipients
			List<User> recipients = new ArrayList<User>();
			if(!docRecipients.trim().isEmpty()){
				String[] recipientIds = docRecipients.split(",");
				for(int i=0; i< recipientIds.length; i++){
					long id = Long.parseLong(recipientIds[i]);
					User recipient = new User();
					recipient.setId(id);
					recipients.add(recipient);
				}
				doc.setToRecipients(recipients);
			}
			
			//
			// get linked docs
			List<Document> linkedDocs = new ArrayList<Document>();
			if(!docLinkedDocs.trim().isEmpty()){
				String[] linkedDocsIds = docLinkedDocs.split(",");
				for(int i=0; i< linkedDocsIds.length; i++){
					long id = Long.parseLong(linkedDocsIds[i]);
					Document linkedDoc = new Document();
					linkedDoc.setId(id);
					linkedDocs.add(linkedDoc);
				}
				doc.setLinkedDocuments(linkedDocs);
			}
			
			//
			// get backing docs
			List<Document> backingDocs = new ArrayList<Document>();
			if(!docBackingDocs.trim().isEmpty()){
				String[] backingDocsIds = docBackingDocs.split(",");
				for(int i=0; i< backingDocsIds.length; i++){
					long id = Long.parseLong(backingDocsIds[i]);
					Document backingDoc = new Document();
					backingDoc.setId(id);
					backingDocs.add(backingDoc);
				}
				doc.setAttachedDocuments(backingDocs);
			}
			
			//
			// get bactags
			List<TagData> docTags = new ArrayList<TagData>();
			if(!docImportDocTags.trim().isEmpty()){
				String[] docTagsIds = docImportDocTags.split(",");
				for(int i=0; i< docTagsIds.length; i++){
					long id = Long.parseLong(docTagsIds[i]);
					TagData docTag = new TagData();
					docTag.setId(id);
					docTags.add(docTag);
				}
				doc.setTags(docTags);
			}	
			
			
			
		} catch (PersistenceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//
		// process
		try {
			responseDoc = documentService.importPDFDocFile(uploadedInputStream , doc);
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

/*
		
		try {
			// import the PDF data
			// saveToFile(uploadedInputStream, uploadedFileLocation);
			// String uploadedFileLocation = UPLOAD_FOLDER + fileDetail.getFileName();
			fileDetail.getFileName();
		} catch (IOException e) {
			return Response.status(500).entity("Can not save file").build();
		}
		
		
		*/
		
		// get the sparse version of the doc
		Document newDoc  = null;
		try {
			newDoc = documentService.getDocumentById(responseDoc.getId()).get(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getLog().info("CreatedPDF-Based Doc: - " + newDoc);
		return Response.status(200)
				.entity(RESTUtility.getJSON(newDoc)).build();
		
		
	}
	

	@GET
	@Path("/docbyid")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch a document for the given document id.
	 * @param docID - the id of the document to fetch
	 * @param recursive - a flag signifying if we also fetch all the attached and linked documents with this document.
	 * @return - the list of matching documents (it will be a single doc).
	 */
	public Response fetchDocumentById(
			@DefaultValue("0") @QueryParam("doc_id") long docID,
			@DefaultValue("false") @QueryParam("recursive") String recursive
																) {
		
		List<Document> allDocuments=null;
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			if(recursive.equals("true")){
				allDocuments = documentService.getRecursiveDocumentById(docID);
				
			}else{
				allDocuments = documentService.getDocumentById(docID);
			}
			

		} catch (Exception e) {
			getLog().error("Error Fetching Document By Id: - " + e);
		}
		
		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(allDocuments)).build();
	}
	


	@GET
	@Path("/traceexport")
	@Produces("application/pdf")
	/**
	 * Fetch a document for the given document id.
	 * @param docID - the id of the document to fetch
	 * @param recursive - a flag signifying if we also fetch all the attached and linked documents with this document.
	 * @return - the list of matching documents (it will be a single doc).
	 */
	public Response fetchTraceDocumentExportById(
			@DefaultValue("0") @QueryParam("doc_id") long docID) {
		
		PDDocument pdfDoc = null;
		InputStream binaryStream=null;
		String filename = "trace.document." + DateUtility.simpleDateFormat(new Date(), DateUtility.FORMAT_DATE_AND_TIME) + ".pdf";
		ContentDisposition contentDisposition = ContentDisposition.type("attachment")
			    .fileName(filename).creationDate(new Date()).build();
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
				pdfDoc = documentService.generateTracePDFDocument(docID);
				//
				// fetch the document
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				pdfDoc.save(out);
				// pdfDoc.save("C:/PdfBox_Examples/my_doc.pdf");
				
				binaryStream =  new ByteArrayInputStream(out.toByteArray());
				pdfDoc.close();
				
				

		} catch (Exception e) {
			getLog().error("Error Fetching Docuemnt Trace PDF By ID: - " + e);
		}
		
		
		
		// return HTTP response 200 in case of success
		return Response.status(200).entity(binaryStream).header("Content-Disposition",contentDisposition).build();
	}
	
	


	@GET
	@Path("/tracegpsexport")
	@Produces("application/csv")
	/**
	 * Fetch a dump of gps data for the given trace
	 * @param docID - the id of the document to fetch
	 * @param recursive - a flag signifying if we also fetch all the attached and linked documents with this document.
	 * @return - the list of matching documents (it will be a single doc).
	 */
	public Response fetchTraceDocumentGPSExportById(
			@DefaultValue("0") @QueryParam("doc_id") long docID) {
		
		InputStream binaryStream=null;
		String filename = "trace.gps." + DateUtility.simpleDateFormat(new Date(), DateUtility.FORMAT_DATE_AND_TIME) + ".csv";
		ContentDisposition contentDisposition = ContentDisposition.type("attachment")
			    .fileName(filename).creationDate(new Date()).build();
		List<String[]> csvFormattedData = null;
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
				csvFormattedData = documentService.generateTraceGPSData(docID);
				// create FileWriter object with file as parameter 
				ByteArrayOutputStream out = new ByteArrayOutputStream(); 
				OutputStreamWriter streamWriter = new OutputStreamWriter(out);
		  
		        // create CSVWriter object filewriter object as parameter 
		        CSVWriter writer = new CSVWriter(streamWriter);
		        writer.writeAll(csvFormattedData);
				
				//string fullString = String.Join(String.Empty, list.ToArray());
		        streamWriter.flush();

		        out.toByteArray();
				// pdfDoc.save("C:/PdfBox_Examples/my_doc.pdf");
				
				binaryStream =  new ByteArrayInputStream(out.toByteArray());
				streamWriter.close();
				
				

		} catch (Exception e) {
			getLog().error("Error Fetching Docuemnt Trace GPS Data By ID: - " + e);
		}
		
		
		
		// return HTTP response 200 in case of success
		return Response.status(200).entity(binaryStream).header("Content-Disposition",contentDisposition).build();
	}
	

	

	
	@GET
	@Path("/docbyidlist")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Get the documents by a list of ids (bulk operation)
	 * @param docIdList - the list of ids to fetch
	 * @return - the list of docuemnts that match the ids.
	 */
	public Response fetchDocumentsByIdList(
			@DefaultValue("0") @QueryParam("doc_ids") String docIdList
																) {
		String[] idList = docIdList.split(",");
		List<Document> allDocuments=null;
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		long[] idListAsLong = new long[idList.length];
		for(int i=0; i< idListAsLong.length; i++){
			idListAsLong[i] = Long.parseLong(idList[i]);
		}
		
		try {
		
			allDocuments = documentService.getDocumentsByIdList(idListAsLong);

		} catch (Exception e) {
			getLog().error("Error Fetching Document By Id: - " + e.getStackTrace());
		}
		
		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(allDocuments)).build();
	}
	

	@GET
	@Path("/alltypes")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Get the list of all the document types.
	 * @param userName - the name of teh user requesting the list
	 * @return - the list of document types
	 */
	public Response fetchAllDocumentTypes(
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		// results
		List<DocumentType> allDocumentTypes=null;
		
		getLog().info("HEADER user-name: " + userName);
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			allDocumentTypes = documentService.getAllDocumentTypes(); 

		} catch (Exception e) {
			getLog().error("Error Fetching Documents: - " + e.getStackTrace());
		}
		
		getLog().debug("FETCH ALL TYPES: - Result" + allDocumentTypes);
		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(allDocumentTypes)).build();
	}
	
	@GET
	@Path("/allscreenings")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Get the list of all the document types.
	 * @param userName - the name of teh user requesting the list
	 * @return - the list of document types
	 */
	public Response fetchAllScreenings(
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		// results
		List<Screening> allScreenings=null;
		
		getLog().info("HEADER user-name: " + userName);
		
		//
		// Initialize service
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			User user = userService.getUserByName(userName);
			allScreenings = documentService.fetchScreenings(user.getId());

		} catch (Exception e) {
			getLog().error("Error Fetching Screenings: - " + e.getStackTrace());
		}
		
		getLog().debug("FETCH ALL Screenings: - Result" + allScreenings);
		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(allScreenings)).build();
	}
	
	@GET
	@Path("/allscreeningstotal")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Get the list of all the document types.
	 * @param userName - the name of teh user requesting the list
	 * @return - the list of document types
	 */
	public Response fetchAllScreeningsTotal(
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		// results
		List<Screening> allScreenings=null;
		
		getLog().info("HEADER user-name: " + userName);
		
		//
		// Initialize service
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			allScreenings = documentService.fetchAllScreenings();

		} catch (Exception e) {
			getLog().error("Error Fetching Screenings: - " + e.getStackTrace());
		}
		
		getLog().debug("FETCH ALL Screenings: - Result" + allScreenings);
		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(allScreenings)).build();
	}

	@GET
	@Path("/page")
	@Produces({"image/png", "image/jpeg", "image/gif"})
	/**
	 * Fetch a specific document page
	 * @param userName - the user requesting this page
	 * @param docID - the id of the document to which this page belongs
	 * @return - the image/page requested
	 */
	public Response fetchPage(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @QueryParam("doc_id") long docID
																) {
		
		InputStream binaryStream=null;
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			binaryStream = documentService.getDocumentPage(docID);

		} catch (Exception e) {
			getLog().error("Error Fetching Document Page: - " + e.getStackTrace());
		}
		
		// return HTTP response 200 in case of success
		return Response.status(200).entity(binaryStream).build();
	}
	

	@GET
	@Path("/pagethumbnail")
	@Produces({"image/png", "image/jpeg", "image/gif"})
	/**
	 * Get the thumb-nail for the given document page
	 * @param userName - the user requesting the call
	 * @param docID - the id of the document to get the thumb-nail image of.
	 * @return - the thumb-nail as a stream
	 */
	public Response fetchPageThumbnail(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @QueryParam("doc_id") long docID
																) {
		
		InputStream binaryStream=null;
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			binaryStream = documentService.getDocumentPageThumbnail(docID);
		} catch (Exception e) {
			getLog().error("Error Fetching Document Page: - " + e.getStackTrace());
		}
		
		try {
			if(binaryStream == null){
				documentService.updatePageThumbnail(
						documentService.generateThumbnail(documentService.getDocumentPage(docID)),
						docID	
				);
				binaryStream = documentService.getDocumentPageThumbnail(docID);
			}
		} catch (Exception e) {
			getLog().error("Error Fetching Document Thumbnail from re-generated update: - " + e);

		}
		
		// return HTTP response 200 in case of success
		return Response.status(200).entity(binaryStream).build();
	}
	
	@GET
	@Path("/wasread")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Check if a give document was read by the user
	 * @param userName - the user requesting the data 
	 * @param docID - the id of the document to check
	 * @param username - the name of the user to check (if they read)
	 * @return - true if the user has read the document and false otherwise.
	 */
	public Response wasRead(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @QueryParam("doc_id") String docID,
			@DefaultValue("") @QueryParam("user_name") String username
			) {
		boolean result = false;
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			result = documentService.wasDocumentRead(docID, username); 

		} catch (Exception e) {
			getLog().error("Error Fetching Document Read Data: - " + e.getStackTrace());
		}
		
		System.out.println("Was Docuemnt Read: - Result " + docID + " " + username);
		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(result)).build();
	}
	
	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Simple document search service (All criteria are joined by an AND)
	 * @param documentTypeId - the document type
	 * @param owner - the owner's name
	 * @param dateFrom - date from
	 * @param dateTo - date to 
	 * @param userName - the user requesting this list
	 * @return - List of documents matching the criteria
	 */
	public Response searchDocuments(
			@DefaultValue("0") @QueryParam("docType") long documentTypeId,
			@DefaultValue("") @QueryParam("owner") String owner,
			@DefaultValue("") @QueryParam("dfrom") String dateFrom,
			@DefaultValue("") @QueryParam("dto") String dateTo,
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		
		//
		// results
		List<Document> allDocuments=new ArrayList<Document>(); ;
		SearchResult<Document> searchResult = new  SearchResult<Document>();
		User user = null;
		
		//
		// inputs
		DocumentSearchCriteria searchCriteria = new DocumentSearchCriteria();

		//
		// Initialize service
		documentService.init();
		userService.init();

		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			if(!dateFrom.isEmpty()){
				searchCriteria.setDateFrom(DateUtility.simpleDateFormat(dateFrom, DateUtility.FORMAT_DATE_ONLY));
			}
			if(!dateTo.isEmpty()){
				searchCriteria.setDateTo(DateUtility.simpleDateFormat(dateTo, DateUtility.FORMAT_DATE_ONLY));
			}
			// document type
			DocumentType docType = new DocumentType();
			docType.setId(documentTypeId);
			searchCriteria.setDocType(docType);
			searchCriteria.setUserName(owner);
		
			getLog().info("SEARCH DOCUMENTS: - Criteria - " + searchCriteria);
			
			// search for the results
			allDocuments = documentService.searchDocuments(searchCriteria, user);

		} catch (Exception e) {
			System.out.println("Error Searching Documents: - " + e);
		}
		
		//
		// prepare results
		searchResult.setList(allDocuments);
		if(searchCriteria.getPageSize() == BaseSearchCriteria.NO_PAGING){
			searchResult.setTotalPages(BaseSearchCriteria.NO_PAGING);
		}else{
			searchResult.setTotalPages(
					(int) Math.ceil(
							allDocuments.size()/searchCriteria.getPageSize())
			);
		}

		
		getLog().debug("SEARCH DOCUMENTS: - Result - " + searchResult);
		Type searchType = new TypeToken<SearchResult<Document>>(){}.getType();
		
		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(searchResult, searchType)).build();
	}
	
	@GET
	@Path("/fetchbydocumentsyncid")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all linked documents by the sync id
	 * @param userName - the user requesting this
	 * @param syncId - the sync id for the document
	 * @return
	 */
	public Response fetchLinkedDocsByDocumentSyncId(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @QueryParam("doc_id") String syncId) {
		// results
		List<Document> allLinkedDocs=null;
		
		System.out.println("[fetchbydocumentsyncid] HEADER user-name: " + userName);
		System.out.println("[fetchbydocumentsyncid] Sync Id : " + syncId);
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			allLinkedDocs = documentService.getAllLinkedDocsByDocSyncId(syncId);

		} catch (Exception e) {
			getLog().error("Error Fetching Linked Docs for Doc: - " + e.getStackTrace());
		}
		
		getLog().debug("FETCH Linked Docs Sync Id: - Result" + allLinkedDocs);
		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(allLinkedDocs)).build();
	}
	
	@GET
	@Path("/fetchattachedbydocumentsyncid")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Get attached documents by sync id
	 * @param userName - the user requesting this
	 * @param syncId - the sync id for the document
	 * @return - the list of attached documents.
	 */
	public Response fetchAttachedDocsByDocumentSyncId(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @QueryParam("doc_id") String syncId) {
		// results
		List<Document> allAttachedDocs=null;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("The sybc id used: " + syncId);
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			allAttachedDocs = documentService.getAllAttachedDocsByDocSyncId(syncId);

		} catch (Exception e) {
			getLog().error("Error Fetching Attached Docs for Doc: - " + e.getStackTrace());
		}
		
		
		getLog().debug("FETCH Attached Docs Sync Id: - Result" + allAttachedDocs);
		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(allAttachedDocs)).build();
	}
	

	@GET
	@Path("/fetchrecipientsforuser")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Get attached documents by sync id
	 * @param userName - the user requesting this
	 * @param syncId - the sync id for the document
	 * @return - the list of attached documents.
	 */
	public Response fetchRecipientsForUser(
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		// results
		List<User> allRecipients=null;
		User user = null;
		
		
		getLog().info("HEADER user-name: " + userName);
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			allRecipients = documentService.fetchRecipientsForUser(user);

		} catch (Exception e) {
			getLog().error("Error Fetching Recipients for User: - " + e.getStackTrace());
		}
		
		
		getLog().debug("FETCH Recipients for User: - Result" + allRecipients);
		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(allRecipients)).build();
	}
	
	
	
	
	@GET
	@Path("/fetchrecipientsbydocumentsyncid")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Get attached documents by sync id
	 * @param userName - the user requesting this
	 * @param syncId - the sync id for the document
	 * @return - the list of attached documents.
	 */
	public Response fetchRecipientsByDocumentSyncId(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @QueryParam("doc_id") String syncId) {
		// results
		List<User> allRecipients=null;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("The sync id used: " + syncId);
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
			// fetch the proper id
			long docId = documentService.getDocIdBySyncId(syncId);
			
			allRecipients = documentService.fetchDocumentRecipients(docId);

		} catch (Exception e) {
			getLog().error("Error Fetching Recipients for Doc: - " + e.getStackTrace());
		}
		
		
		getLog().debug("FETCH Recipients Sync Id: - Result" + allRecipients);
		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(allRecipients)).build();
	}
	
	@POST
	@Path("/linkbysync")
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
	public Response linkDocumentBySyncId(InputStream incomingData, 
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @QueryParam("doc_id") String syncId) {

		String result = "/document/linkbysync com.wwf.shrimp.application [CREATE NEW DOC LINKS]";
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
			
			getLog().debug("[POST] Linking Docs For Sync DocId :" + syncId);
			
			
			// parse the JSON input into the specific class
			Type listType = new TypeToken<ArrayList<Document>>(){}.getType();
			List<Document> docs = new Gson().fromJson(reader, listType);
			
			// fetch the proper id
			long docId = documentService.getDocIdBySyncId(syncId);
			getLog().debug("[POST] Linking Docs For DocId :" + docId);
			getLog().debug("[POST] Linking Docs For Sync DocId :" + syncId);
			
			documentService.createDocumentLinks(docs, docId);


		} catch (Exception e) {
			getLog().error("Error linking docs: - " + e.getStackTrace());
			
		}

		// return HTTP response 200 in case of success
		return Response.status(200).entity(result).build();
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
	public Response attachDocumentBySyncId(InputStream incomingData, 
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @QueryParam("doc_id") String syncId) {

		String result = "/document/attachbysync com.wwf.shrimp.application [CREATE NEW DOC ATTACHMENTS]";
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
			
			getLog().debug("[POST] Attaching Docs For Sync DocId :" + syncId);
			
			
			// parse the JSON input into the specific class
			Type listType = new TypeToken<ArrayList<Document>>(){}.getType();
			List<Document> docs = new Gson().fromJson(reader, listType);
			
			// fetch the proper id
			long docId = documentService.getDocIdBySyncId(syncId);
			getLog().debug("[POST] Attaching Docs For DocId :" + docId);
			getLog().debug("[POST] Attaching Docs For Sync DocId :" + docs);
			
			documentService.createDocumentAttachments(docs, docId);


		} catch (Exception e) {
			getLog().error("Error attaching docs: - " + e.getStackTrace());
			
		}

		// return HTTP response 200 in case of success
		return Response.status(200).entity(result).build();
	}
	
	@POST
	@Path("/addrecipientsbysync")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will attach `TO` recipients by sync id
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the Document entity with a DocumentPage embedded
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated document.
	 *     2. Error String if there was an issue
	 */
	public Response attachRecipientsBySyncId(InputStream incomingData, 
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @QueryParam("doc_id") String syncId) {

		String result = "/document/recipientsbysync com.wwf.shrimp.application [CREATE NEW RECIPIENTS]";
		AuditEntity newAuditEntity = new AuditEntity();
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
			
			getLog().debug("[POST] Attaching Recipients For Sync DocId :" + syncId);
			
			
			// parse the JSON input into the specific class
			Type listType = new TypeToken<ArrayList<User>>(){}.getType();
			List<User> recipients = new Gson().fromJson(reader, listType);
			
			// fetch the proper id
			long docId = documentService.getDocIdBySyncId(syncId);
			getLog().debug("[POST] Attaching Recipients For Sync DocId :" + recipients);
			
			documentService.createDocumentRecipients(recipients, docId);


		} catch (Exception e) {
			getLog().error("Error attaching recipients: - " + e.getStackTrace());
			
		}
		
		

		// return HTTP response 200 in case of success
		return Response.status(200).entity(result).build();
	}
	
	@POST
	@Path("/pageimage")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * THis method will insert/update the profile image for the user
	 * This is used by external client (like the iOS) to create pages individually through a form submission.
	 * @param incomingData - the user data with the profile image to set
	 * @return - Simple text response with success of the insertion
	 */
	public Response createDocumentPage(
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail,
			@FormDataParam("userName") String username,
			@FormDataParam("sessionId") String sessionId,
			@FormDataParam("pageNumber") int pageNumber) {
		String result = "/document/pageimage com.wwf.shrimp.application [CREATE PAGE IMAGE - SUCCESS]";
		
		DocumentPage page = new DocumentPage();

		
		//
		// Initialize services
		documentService.init();
		
		//
		// Init page data
		page.setPageNumber(pageNumber);
		
		
		/**
		 * Process the request
		 */
		try {
			
			long docId = documentService.getDocIdBySyncId(sessionId);
			getLog().info("New Page Insertion Into Doc with ID: " + username + " " + docId);

			if(uploadedInputStream != null){
				page = documentService.createDocumentPageFromFormData(uploadedInputStream, page, docId);
			}
			

		} catch (Exception e) {
			getLog().error("Error Updating Profile pic: - " + e.getStackTrace());
			
		}

		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(page)).build();
	}
	
	@PUT
	@Path("/pageimage")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * THis method will insert/update the profile image for the user
	 * This is used by external client (like the iOS) to update pages individually through a form submission.
	 * @param incomingData - the user data with the profile image to set
	 * @return - Simple text response with success of the insertion
	 */
	public Response updateDocumentPage(
			@FormDataParam("pageId") long pageId,
			@FormDataParam("userName") String username,
			@FormDataParam("sessionId") String sessionId,
			@FormDataParam("pageNumber") int pageNumber) {
		String result = "/document/pageimage com.wwf.shrimp.application [UPDATE PAGE IMAGE - SUCCESS]";
		
		DocumentPage page = new DocumentPage();

		
		//
		// Initialize services
		documentService.init();
		
		//
		// Init page data
		page.setPageNumber(pageNumber);
		page.setId(pageId);
		
		
		/**
		 * Process the request
		 */
		try {
			
			long docId = documentService.getDocIdBySyncId(sessionId);
			getLog().info("New Page Insertion Into Doc with ID: " + username + " " + docId);

			page = documentService.updateDocumentPageFromFormData(page);

			

		} catch (Exception e) {
			getLog().error("Error Updating Page data - " + e.getStackTrace());
			
		}

		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(RESTUtility.getJSON(page)).build();
	}
	
	@POST
	@Path("/createnopages")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will create a new document in the database without any pages
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the Document entity with a DocumentPage embedded
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated document.
	 *     2. Error String if there was an issue
	 */
	public Response createNoPages(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("false") @HeaderParam("sparse") boolean sparseFlag) {
		
		Document newDocument = null;
		IdentifiableEntity id = null;
		User user = new User();
		User gestureUser = new User();
		Status httpResponseStatus = Status.OK;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
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
			getLog().info("Parsing the Document form JSON payload");
			newDocument = gson.fromJson(reader, Document.class);
			getLog().info("DONE - Parsing the Document form JSON payload");
			getLog().info("DONE - Parsed Document: " + newDocument);
			
			//
			// extract some data
			user = userService.getUserByName(newDocument.getOwner());
			gestureUser = userService.getUserByName(userName);
			//
			// Add user id to the data
			newDocument.setOwnerId(user.getId());
			
			getLog().info("Creating new Document...");
			newDocument = documentService.create(newDocument, false);
			
			id = new IdentifiableEntity();
			id.setId(newDocument.getId());

		} catch (Exception e) {
			getLog().error("Error Creating a new document: - " + e);
			httpResponseStatus = Status.INTERNAL_SERVER_ERROR;
		}
		
	
		// sparsify
		if(sparseFlag == true && newDocument != null){
			newDocument = DataSparseHelper.sparsify(newDocument);
		}
		
		//
		// Process any notification creation
		
		// get all the group members for this user to whom the notifications should be sent
		if(newDocument != null){
			try {
				processDocumentNotifications(newDocument, gestureUser);
				
			}catch (Exception e) {
				getLog().error("Error Creating notifications for Document-Creation: " + e);
			}
		}
		

		// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(RESTUtility.getJSON(newDocument)).build();
	}
	
	@POST
	@Path("/create")
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
	public Response create(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("false") @HeaderParam("sparse") boolean sparseFlag) {
		
		Document newDocument = null;
		IdentifiableEntity id = null;
		User user = new User();
		User gestureUser = new User();
		Status httpResponseStatus = Status.OK;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
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
			newDocument = gson.fromJson(reader, Document.class);
			System.out.println(newDocument.getCreationTimestamp());
			System.out.println("New Document Creation: " + newDocument);
			
			//
			// extract some data
			user = userService.getUserByName(newDocument.getOwner());
			gestureUser = userService.getUserByName(userName);
			//
			// populate additional doc data
			
			//
			// User
			newDocument.setOwnerId(user.getId());
			
			newDocument = documentService.create(newDocument);
			
			id = new IdentifiableEntity();
			id.setId(newDocument.getId());

		} catch (Exception e) {
			getLog().error("Error Creating a new document: - " + e);
			httpResponseStatus = Status.INTERNAL_SERVER_ERROR;
		}
		
	
		// sparsify
		if(sparseFlag == true){
			newDocument = DataSparseHelper.sparsify(newDocument);
		}
		
		//
		// Process any notification creation
		
		// get all the group members for this user to whom the notifications should be sent
		try {
			processDocumentNotifications(newDocument, gestureUser);
			
		}catch (Exception e) {
			getLog().error("Error Creating notifications for Document-Creation: " + e);
		}
		

		// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(RESTUtility.getJSON(newDocument)).build();
	}
	
	
	@POST
	@Path("/createocrmatchdata")
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
	public Response createOCRMatchData(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("false") @HeaderParam("sparse") boolean sparseFlag) {
		
		DocumentOCRMatchingData newOCRMatchDataItem = null;
		IdentifiableEntity id = null;
		Status httpResponseStatus = Status.OK;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
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
			newOCRMatchDataItem = gson.fromJson(reader, DocumentOCRMatchingData.class);
			System.out.println("New OCR Match Data Item Creation: " + newOCRMatchDataItem);
			
			//
			// extract some data
			
			newOCRMatchDataItem = documentService.createDocumentOCRMatchingData(newOCRMatchDataItem);
			
			id = new IdentifiableEntity();
			id.setId(newOCRMatchDataItem.getId());

		} catch (Exception e) {
			getLog().error("Error Creating a new OCR Match Data Item: - " + e);
			httpResponseStatus = Status.INTERNAL_SERVER_ERROR;
		}
		

		// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(RESTUtility.getJSON(newOCRMatchDataItem)).build();
	}
	
	@PUT
	@Path("/updateocrmatchdata")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will update an existing document in the database
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the Document entity with a DocumentPage embedded
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated document.
	 *     2. Error String if there was an issue
	 */
	public Response updateOCRMatchData(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		
		DocumentOCRMatchingData updatedOCRMatchDataItem = null;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
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
			updatedOCRMatchDataItem = gson.fromJson(reader, DocumentOCRMatchingData.class);
			
			//
			// process the request
			
			// create a new doc type
			updatedOCRMatchDataItem = documentService.updateDocumentOCRMatchingData(updatedOCRMatchDataItem);


		} catch (Exception e) {
			getLog().error("Error Updating an existing OCR match type type: - " + e);
		}
		

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(updatedOCRMatchDataItem)).build();
	}
	
	@GET
	@Path("/allocrmatchdata")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Get the list of all the document types.
	 * @param userName - the name of teh user requesting the list
	 * @return - the list of document types
	 */
	public Response fetchAllOCRMatchData(
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		// results
		List<DocumentOCRMatchingData> allOCRMatchData=null;
		
		getLog().info("HEADER user-name: " + userName);
		
		//
		// Initialize service
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			allOCRMatchData = documentService.getDocumentOCRMatchingDataList();

		} catch (Exception e) {
			getLog().error("Error Fetching OCR Match Data List: - " + e.getStackTrace());
		}
		
		getLog().debug("FETCH ALL OCR Match Data: - Result" + allOCRMatchData);
		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(allOCRMatchData)).build();
	}
	
	@DELETE
	@Path("/delete/ocrmatchdata/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	/**
	 * This will mark a document as deleted
	 * @param userName - the user requesting the operation
	 * @param documentId - the id of the document to delete
	 * @return - request response
	 */
	public Response deleteOCRMatchData(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@PathParam("id") long ocrMatchDataItemId) {
		
		String result = "Delete of OCR Match Data with id= " + ocrMatchDataItemId 
							+ " <SUCCESS>";
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			documentService.deleteDocumentOCRMatchingData(ocrMatchDataItemId);

		} catch (Exception e) {
			getLog().error("Error Deleting OCR Match Data with - " + e.getStackTrace());
		}
		
		System.out.println("DELETE OCR Match Data with id=" + ocrMatchDataItemId);
		// return HTTP response 200 in case of success
		return Response.status(200).entity(result).build();
	}
	
	
	
	
	
	
	@POST
	@Path("/createfielddefinition")
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
	public Response createFieldDefinition(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		
		DynamicFieldDefinition newDefinition = null;
		IdentifiableEntity id = null;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
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
			newDefinition = gson.fromJson(reader, DynamicFieldDefinition.class);
			
			//
			// process the request
			
			// create a new doc type
			newDefinition = documentService.createDynamicFieldDefinition(newDefinition);
			
			id = new IdentifiableEntity();
			id.setId(newDefinition.getId());

		} catch (Exception e) {
			getLog().error("Error Creating a new field defintion: - " + e);
		}
		

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(newDefinition)).build();
	}
	
	
	@POST
	@Path("/createscreening")
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
	public Response createScreening(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		
		Screening newScreening = null;
		IdentifiableEntity id = null;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
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
			newScreening = gson.fromJson(reader, Screening.class);
			User user = userService.getUserByName(userName);
			
			//
			// process the request
			
			// create a new doc type
			newScreening = documentService.createScreening(newScreening, user.getId());
			
			id = new IdentifiableEntity();
			id.setId(newScreening.getId());

		} catch (Exception e) {
			getLog().error("Error Creating a new screening: - " + e);
		}
		

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(newScreening)).build();
	}
	

	@PUT
	@Path("/updatefielddefinition")
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
	public Response updateFieldDefinition(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		
		DynamicFieldDefinition newDefinition = null;
		IdentifiableEntity id = null;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
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
			newDefinition = gson.fromJson(reader, DynamicFieldDefinition.class);
			
			//
			// process the request
			
			// create a new doc type
			documentService.updateDynamicFieldDefinition(newDefinition);

		} catch (Exception e) {
			getLog().error("Error Creating a new field defintion: - " + e);
		}
		

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(newDefinition)).build();
	}
	
	
	
	@POST
	@Path("/createdoctype")
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
	public Response createDocType(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		
		DocumentType newDocumentType = null;
		IdentifiableEntity id = null;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
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
			newDocumentType = gson.fromJson(reader, DocumentType.class);
			
			//
			// process the request
			
			// create a new doc type
			newDocumentType = documentService.createDocType(newDocumentType);
			
			id = new IdentifiableEntity();
			id.setId(newDocumentType.getId());

		} catch (Exception e) {
			getLog().error("Error Creating a new document type: - " + e);
		}
		

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(newDocumentType)).build();
	}
	

	
	@PUT
	@Path("/updatedoctype")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will update an existing document in the database
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the Document entity with a DocumentPage embedded
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated document.
	 *     2. Error String if there was an issue
	 */
	public Response updateDocType(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		
		DocumentType updatedDocumentType = null;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
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
			updatedDocumentType = gson.fromJson(reader, DocumentType.class);
			
			//
			// process the request
			
			// create a new doc type
			updatedDocumentType = documentService.updateDocType(updatedDocumentType);


		} catch (Exception e) {
			getLog().error("Error Updating an existing document type: - " + e);
		}
		

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(updatedDocumentType)).build();
	}
	
	
	
	
	@POST
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will update an existing document in the database
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the Document entity with a DocumentPage embedded
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated document.
	 *     2. Error String if there was an issue
	 */
	public Response update(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("false") @HeaderParam("sparse") boolean sparseFlag) {
		
		Document newDocument = null;
		IdentifiableEntity id = null;
		User user = new User();
		User gestureUser = new User();
		AuditEntity newAuditEntity = new AuditEntity();
		Status httpResponseStatus = Status.OK;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
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
			newDocument = gson.fromJson(reader, Document.class);
			System.out.println(newDocument.getCreationTimestamp());
			System.out.println("Document Updation: " + newDocument);
			
			//
			// extract some data
			user = userService.getUserByName(newDocument.getOwner());
			gestureUser = userService.getUserByName(userName);
			
			newDocument = documentService.update(newDocument);

		} catch (Exception e) {
			getLog().error("Error Updating a document: - " + e);
			httpResponseStatus = Status.INTERNAL_SERVER_ERROR;
		}
		
	
		// sparsify
		if(sparseFlag == true){
			newDocument = DataSparseHelper.sparsify(newDocument);
		}
		
		//
		// Process any notification update
		
		try {
			processDocumentNotifications(newDocument, gestureUser);
			
		}catch (Exception e) {
			getLog().error("Error Creating notifications for Document-Creation: " + e);
		}
		

		// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(RESTUtility.getJSON(newDocument)).build();

		
	}
	
	@POST
	@Path("/updatenopages")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will update an existing document in the database
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the Document entity with a DocumentPage embedded
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated document.
	 *     2. Error String if there was an issue
	 */
	public Response updateNoPages(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("false") @HeaderParam("sparse") boolean sparseFlag) {
		
		Document newDocument = null;
		IdentifiableEntity id = null;
		User user = new User();
		User gestureUser = new User();
		AuditEntity newAuditEntity = new AuditEntity();
		Status httpResponseStatus = Status.OK;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
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
			newDocument = gson.fromJson(reader, Document.class);
			System.out.println(newDocument.getCreationTimestamp());
			System.out.println("Document Updation: " + newDocument);
			
			//
			// extract some data
			user = userService.getUserByName(newDocument.getOwner());
			gestureUser = userService.getUserByName(userName);
			
			newDocument = documentService.update(newDocument, false);

		} catch (Exception e) {
			getLog().error("Error Updating a document: - " + e);
			httpResponseStatus = Status.INTERNAL_SERVER_ERROR;
		}
		
	
		// sparsify
		if(sparseFlag == true){
			newDocument = DataSparseHelper.sparsify(newDocument);
		}
		
		//
		// Process any notification update
		
		try {
			processDocumentNotifications(newDocument, gestureUser);
			
		}catch (Exception e) {
			getLog().error("Error Creating notifications for Document-Creation: " + e);
		}
		

		// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(RESTUtility.getJSON(newDocument)).build();

		
	}
	
	
	@POST
	@Path("/addnotes")
	@Consumes(MediaType.APPLICATION_JSON + "; charset=utf-8")
	/**
	 * This method will create a new document note in the database
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the NoteData entity
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated NoteData.
	 *     2. Error String if there was an issue
	 */
	public Response createDocumentNotes(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("0") @HeaderParam("doc-id") long docId) {
		String result = "/document/addnotes com.wwf.shrimp.application [CREATE DOCUMENT NOTES OK]";
		
		NoteData newDocumentNote = null;
		List<NoteData> newDocumentNotes = new ArrayList<NoteData>();
		String sessionID = null;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize services
		documentService.init();
		
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
			
			// get teh doc session id
			sessionID  = documentService.getSyncIDByDocId(docId);
			
			// parse the JSON input into the specific class
			// newDocument = gson.fromJson(reader, Document.class);
			newDocumentNote = gson.fromJson(reader, NoteData.class);
			getLog().info(newDocumentNote.getOwner());
			getLog().info("New Document Note: " + newDocumentNote.getNote());
			
			//
			// extract some data
			newDocumentNotes.add(newDocumentNote);
			documentService.createDocumentNotes(newDocumentNotes, docId);
			
			//
			// Change the status
			documentService.updateDocumentStatus(Document.STATUS_REJECTED, sessionID, userName);

		} catch (Exception e) {
			getLog().error("Error Creating a new document note: - " + e);
		}
		
		

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(newDocumentNote)).build();
	}
	
	@DELETE
	@Path("/delete/{syncId}")
	@Produces(MediaType.TEXT_PLAIN)
	/**
	 * This will mark a document as deleted
	 * @param userName - the user requesting the operation
	 * @param documentId - the id of the document to delete
	 * @return - request response
	 */
	public Response deleteDocument(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@PathParam("syncId") String documentId) {
		
		String result = "Delete of document id= " + documentId 
							+ " <SUCCESS>";
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			documentService.delete(documentId);; 

		} catch (Exception e) {
			getLog().error("Error Deleting Document - " + e.getStackTrace());
		}
		
		System.out.println("DELETE Document with syncID=" + documentId);
		// return HTTP response 200 in case of success
		return Response.status(200).entity(result).build();
	}
	
	
	
	@DELETE
	@Path("/delete/pages/{syncId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	/**
	 * This will mark a document'spages as deleted
	 * @param incomingData-  the incoming list of ids for the pages to be removed 
	 * @param userName - the user requesting the operation
	 * @param documentId - the id of the document's pages to delete
	 * @return - request response
	 */
	public Response deleteDocumentPages(InputStream incomingData, 
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@PathParam("syncId") String documentId) {
		
		String result = "Delete of document pages for doc id: " + documentId 
							+ " <SUCCESS>";
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
						
			getLog().debug("[DELETE] Removing Pages For doc id :" + documentId);
			
			
			// parse the JSON input into the specific class
			long[] docPagesIds = new Gson().fromJson(reader, long[].class);
		
			documentService.deleteDocumentPages(docPagesIds);

		} catch (Exception e) {
			getLog().error("Error Deleting Document - " + e);
		}
		
		System.out.println("DELETE Document with syncID=" + documentId);
		// return HTTP response 200 in case of success
		return Response.status(200).entity(result).build();
	}
	
	@DELETE
	@Path("/delete/dynamicfielddefinition/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	/**
	 * This will mark a document as deleted
	 * @param userName - the user requesting the operation
	 * @param documentId - the id of the document to delete
	 * @return - request response
	 */
	public Response deleteDynamicFieldDefinition(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@PathParam("id") long dynamicFieldDefinitionId) {
		
		String result = "Delete of dynamic field definition id= " + dynamicFieldDefinitionId 
							+ " <SUCCESS>";
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
		
			documentService.deleteDynamicFieldDefinition(dynamicFieldDefinitionId);

		} catch (Exception e) {
			getLog().error("Error Deleting Dynamic Field Definition - " + e.getStackTrace());
		}
		
		System.out.println("DELETE dynamic field definition with id=" + dynamicFieldDefinitionId);
		// return HTTP response 200 in case of success
		return Response.status(200).entity(result).build();
	}
	
	
	@POST
	@Path("/markread")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will mark a document as READ for the given document and the user who read the document.
	 *  
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated document.
	 *     2. Error String if there was an issue
	 */
	public Response markAsRead(
			@DefaultValue("0") @QueryParam("doc_id") String docID,
			@DefaultValue("") @QueryParam("user_name") String userName
			) {
		AuditEntity newAuditEntity = null;
		IdentifiableEntity id = null;
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			newAuditEntity = new AuditEntity();
			// actor name
			User user = new User();
			user.setName(userName);
			newAuditEntity.setActor(user);
			// action
			newAuditEntity.setAction(AuditAction.DOCUMENT_READ);
			// item type
			newAuditEntity.setItemType("Document");
			// item id
			newAuditEntity.setItemId(docID);
			
			newAuditEntity = documentService.setDocumentAuditAsRead(newAuditEntity);
			
			id = new IdentifiableEntity();
			id.setId(newAuditEntity.getId());

		} catch (Exception e) {
			getLog().error("Error Creating a new audit record: - " + e.getStackTrace());
			
		}

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(id)).build();
	}
	
	
	@POST
	@Path("/status")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will update the status of an existing document in the database
	 *  
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The OK status for the request
	 *     2. Error String if there was an issue
	 */
	public Response setStatus(
			@DefaultValue("0") @QueryParam("doc_id") String docSessionID,
			@DefaultValue("") @QueryParam("user_name") String userName,
			@DefaultValue("") @QueryParam("action_timestamp") String actionTimestamp,
			@DefaultValue("") @QueryParam("doc_status") String status
			) {
		
		//
		// Extract the object to be written to the database:
		User gestureUser = new User();
		Document currDoc = null;
		
		
		
		getLog().info("Seting document status: - " + docSessionID 
				+ " " + userName + " " + status);
		
		//
		// Initialize service
		documentService.init();
		userService.init();
		
		// 
		// Initialize other data
		try {
			gestureUser = userService.getUserByName(userName);
		} catch (PersistenceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			currDoc = documentService.getDocumentById(documentService.getDocIdBySyncId(docSessionID)).get(0);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/**
		 * Process the request
		 */
		try {
			if(actionTimestamp.contentEquals("")) {
				currDoc = documentService.updateDocumentStatus(status, docSessionID, userName);
			} else {
				currDoc = documentService.updateDocumentStatus(status, docSessionID, userName, actionTimestamp);
			}
			

		} catch (Exception e) {
			getLog().error("Error setting document status: - " + e.getStackTrace());
			
		}
		
		//
		// Process any notification update
		
		try {
			processDocumentNotifications(currDoc, gestureUser);
			
		}catch (Exception e) {
			getLog().error("Error Creating notifications for Document-Creation: " + e);
		}
		
		

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(currDoc.getUpdationServerTimestamp())).build();
	}
	
	@POST
	@Path("/gpslocation")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will update the status of an existing document in the database
	 *  
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The OK status for the request
	 *     2. Error String if there was an issue
	 */
	public Response setGPSLocation(
			@DefaultValue("0") @QueryParam("doc_id") String docSessionID,
			@DefaultValue("") @QueryParam("gps_location") String gpsLocation) {
		
		//
		// Extract the object to be written to the database:
		User gestureUser = new User();
		Document currDoc = null;
		
		
		getLog().info("Seting document gps location: - " + docSessionID 
				+ " " + gpsLocation );
		
		//
		// Initialize service
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			documentService.setDocumentGPSLocation(docSessionID, gpsLocation);

		} catch (Exception e) {
			getLog().error("Error setting document gps: - " + e.getStackTrace());
			
		}
		


		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON("GPS Added")).build();
	}
	
	@POST
	@Path("/recall")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will update the status of an existing document in the database
	 *  
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The OK status for the request
	 *     2. Error String if there was an issue
	 */
	public Response recallDocument(
			@DefaultValue("0") @QueryParam("doc_id") String docSessionID,
			@DefaultValue("") @QueryParam("user_name") String userName
			) {
		
		//
		// Extract the object to be written to the database:
		User gestureUser = new User();
		Document currDoc = null;
		String status="";
		
		
		//
		// Initialize service
		documentService.init();
		userService.init();
		
		// 
		// Initialize other data
		try {
			gestureUser = userService.getUserByName(userName);
		} catch (PersistenceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			currDoc = documentService.getDocumentById(documentService.getDocIdBySyncId(docSessionID)).get(0);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/**
		 * Process the request
		 */
		if(currDoc.getStatus().equals(Document.STATUS_SUBMITTED)){
			status = Document.STATUS_DRAFT;
		}else if(currDoc.getStatus().equals(Document.STATUS_RESUBMITTED)){
			status = Document.STATUS_DRAFT;
		}
		try {
			if(!status.isEmpty()){
				currDoc = documentService.updateDocumentStatus(status, docSessionID, userName);
			}

		} catch (Exception e) {
			getLog().error("Error recalling document: - " + e.getStackTrace());
			
		}
		
		//
		// Process any notification update
		
		try {
			// processDocumentNotifications(currDoc, gestureUser);
			
		}catch (Exception e) {
			getLog().error("Error Creating notifications for Document-Creation: " + e);
		}

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON("Status Set")).build();
	}
	
	
	@POST
	@Path("/pages")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * This method will add new pages to an existing document
	 *  
	 * @param incomingData - the incoming data will hold a JSON string representing
	 * the instance of the Document entity with a DocumentPage embedded
	 * @return the response which will contain either the OK response
	 * or an error response.
	 *     1. The PK of the generated document.
	 *     2. Error String if there was an issue
	 */
	public Response addDocPages(InputStream incomingData,
			@DefaultValue("0") @QueryParam("doc_id") long docID,
			@DefaultValue("") @QueryParam("user_name") String userName
			) {
		IdentifiableEntity id = new IdentifiableEntity();
		
		List<DocumentPage> sparseResultPages = new ArrayList<DocumentPage>();
		
		//
		// Extract the object to be written to the database:
		
		//
		// Initialize service
		documentService.init();
		
		/**
		 * Process the request
		 */
		try {
			
			// get the request reader ready 
			BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
			
			getLog().debug("[POST] Adding new Pages For doc id :" + docID);
			
			
			// parse the JSON input into the specific class
			Type listType = new TypeToken<ArrayList<DocumentPage>>(){}.getType();
			List<DocumentPage> docPages = new Gson().fromJson(reader, listType);
			
			for(int page = 0; page < docPages.size(); page++){
				//get the page
				DocumentPage sparsePage = documentService.createDocumentPages(docPages.get(page), docID);
				// sparsify it
				sparsePage.setBase64ImageData(null);
				sparsePage.setData(null);
				sparsePage.setImagePage(null);
				sparsePage.setPageData(null);
				sparsePage.setPageDataType(null);
				sparsePage.setPageDataURL(null);
				sparseResultPages.add(sparsePage);
			}
			id.setId(docID);

		} catch (Exception e) {
			getLog().error("Error adding pages: - " + e);
			
		}

		// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(sparseResultPages)).build();
	}
	
	@GET
	@Path("/fetchallbytag")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all the documents that have this given tag
	 * @param userName - the user who is asking for these documents
	 * @param docType - the optional document type
	 * @param customTag - the required tag for which to search
	 * 
	 * @return - all the documents that this user has access to
	 */
	public Response fetchAllDocumentsByTag(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@DefaultValue("") @HeaderParam("doc-type") String docType,
			@DefaultValue("") @HeaderParam("custom_tag") String customTag) {
		// results
		List<Document> allDocuments=null;
		User user = null;
		
		getLog().info("HEADER user-name: " + userName);
		getLog().info("HEADER doc-type: " + docType);
		getLog().info("HEADER custom_tag: " + customTag);
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			allDocuments = documentService.getAllTaggedDocs(user, docType, customTag); 

		} catch (Exception e) {
			getLog().error("Error Fetching Tagged Documents: - " + e.getMessage());
		}
		
		getLog().debug("FETCH ALL: - Result" + allDocuments);
				// return HTTP response 200 in case of success
		return Response.status(200).entity(RESTUtility.getJSON(allDocuments)).build();
	}
	
	@GET
	@Path("/fetchallfielddefinitions")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Fetch all the dynamic field definitions accessible to this user
	 * @param userName - the user who is asking for these definitions
	 * @return - all the dynamic field definitions that this user has access to
	 */
	public Response fetchAllFieldDefinitions(
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		// results
		List<DynamicFieldDefinition> allDynamicFieldDefinitions = new ArrayList<DynamicFieldDefinition>();
		User user = null;
		
		Status httpResponseStatus = Status.OK;
		
		getLog().info("HEADER user-name: " + userName);
		
		//
		// Initialize services
		documentService.init();
		userService.init();
		
		/**
		 * Process the request
		 */
		try {
			user = userService.getUserByName(userName);
			allDynamicFieldDefinitions = documentService.getDynamicFieldDefinitionsByOrgId(user.getUserOrganizations().get(0).getId());

		} catch (Exception e) {
			getLog().error("Error Fetching Dynamic Field Definitions: - " + e);
			httpResponseStatus = Status.NOT_FOUND;
		}
		
		getLog().debug("FETCH ALL: - Result" + allDynamicFieldDefinitions);
				// return HTTP response 200 in case of success
		return Response.status(httpResponseStatus).entity(RESTUtility.getJSON(allDynamicFieldDefinitions)).build();
	}
	
	
	

	/*****************************************************************************************************
	 * Private Methods
	 */
	
	/**
	 * Process the gesture on a document 
	 * @param doc - the document for which we will create the notifications
	 * @param user - the user on whose behalf the change made
	 * @throws Exception - if there were any issues
	 */
	private void processDocumentNotifications(Document doc, User gestureUser) throws Exception{
		List<User> allUsers = new ArrayList<User>();
		// get all recipients
		allUsers = doc.getToRecipients();
		
		// initialize
		notificationService.init();
		auditService.init();
		
		
		//
		// Owner only 
		if(doc.getStatus().equals(Document.STATUS_ACCEPTED)
				|| doc.getStatus().equals(Document.STATUS_REJECTED)){
					User owner = userService.getUserByName(doc.getOwner());
					List<NotificationData> notifications = notificationService.getAllNotificationEntitiesByUserIdAndAuditItemId(owner.getId(), doc.getSyncID());
					if(notifications == null || notifications.size() == 0){
						AuditAction auditAction = null;
						if(doc.getStatus().equals(Document.STATUS_REJECTED)){
							auditAction = AuditAction.DOCUMENT_REJECT; 
						}
						if(doc.getStatus().equals(Document.STATUS_ACCEPTED)){
							auditAction = AuditAction.DOCUMENT_ACCEPT; 
						}
						// create a new notification
						AuditEntity auditEntity = auditService.getAuditEntityByItemId(doc.getSyncID(), "Document", auditAction.toString());
						NotificationData notification = new NotificationData();
						notification.setUser(owner);
						notification.setNotificationType(NotificationType.ONE_TIME);
						notification.setAuditData(auditEntity);
						notification.setCreationTimestamp(DateUtility.simpleDateFormat(DateUtility.getCurrentDateTime(), DateUtility.FORMAT_DATE_AND_TIME));
						notificationService.create(notification);
					}
		}
		
		//
		// Recipients Only
		if(doc.getStatus().equals(Document.STATUS_SUBMITTED)
				|| doc.getStatus().equals(Document.STATUS_RESUBMITTED)){

			for(int i=0; i<allUsers.size(); i++){
				List<NotificationData> notifications = notificationService.getAllNotificationEntitiesByUserIdAndAuditItemId(allUsers.get(i).getId(), doc.getSyncID());
				if(notifications == null || notifications.size() == 0){
					AuditAction auditAction = null;
					if(doc.getStatus().equals(Document.STATUS_SUBMITTED)){
						auditAction = AuditAction.DOCUMENT_SUBMIT; 
					}
					if(doc.getStatus().equals(Document.STATUS_RESUBMITTED)){
						auditAction = AuditAction.DOCUMENT_RESUBMIT; 
					}
					// create a new notification
					AuditEntity auditEntity = auditService.getAuditEntityByItemId(doc.getSyncID(), "Document", auditAction.toString());
					NotificationData notification = new NotificationData();
					notification.setUser(allUsers.get(i));
					notification.setNotificationType(NotificationType.ONE_TIME);
					notification.setAuditData(auditEntity);
					notification.setCreationTimestamp(DateUtility.simpleDateFormat(DateUtility.getCurrentDateTime(), DateUtility.FORMAT_DATE_AND_TIME));
					notificationService.create(notification);
				}
			}
		}
	}
	
	/**
	 * check if the document is tagged
	 * @param doc - the document in question
	 * @param tag - the specific tag 
	 * @return - true if the document is tagged and false if it is not
	 */
	private boolean containsTag(Document doc, String tag){
		boolean result = false;
		
		for(int i =0; i<doc.getTags().size(); i++){
			if(doc.getTags().get(i).getText().equals(tag)){
				return true;
			}
		}
		
		return result;
		
	}

}
