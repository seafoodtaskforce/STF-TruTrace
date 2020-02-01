package com.wwf.shrimp.application.services.main.dao.impl.mysql;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.vandeseer.easytable.TableDrawer;
import org.vandeseer.easytable.settings.HorizontalAlignment;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.TextCell;

import com.mysql.cj.api.jdbc.Statement;
import com.wwf.shrimp.application.exceptions.EntityNotFoundException;
import com.wwf.shrimp.application.exceptions.PersistenceException;
import com.wwf.shrimp.application.exceptions.ServiceManagementException;
import com.wwf.shrimp.application.models.AuditAction;
import com.wwf.shrimp.application.models.AuditEntity;
import com.wwf.shrimp.application.models.AuditUserType;
import com.wwf.shrimp.application.models.Document;
import com.wwf.shrimp.application.models.DocumentPage;
import com.wwf.shrimp.application.models.DocumentType;
import com.wwf.shrimp.application.models.GroupType;
import com.wwf.shrimp.application.models.IdentifiableEntity;
import com.wwf.shrimp.application.models.NoteData;
import com.wwf.shrimp.application.models.Organization;
import com.wwf.shrimp.application.models.Role;
import com.wwf.shrimp.application.models.TagData;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.search.AuditSearchCriteria;
import com.wwf.shrimp.application.models.search.DocumentSearchCriteria;
import com.wwf.shrimp.application.models.search.OrganizationSearchCriteria;
import com.wwf.shrimp.application.models.search.TagSearchCriteria;
import com.wwf.shrimp.application.models.search.UserSearchCriteria;
import com.wwf.shrimp.application.utils.DateUtility;
import com.wwf.shrimp.application.utils.FileUtils;
import com.wwf.shrimp.application.utils.RESTUtility;

import net.coobird.thumbnailator.Thumbnails;


/**
 * The persistence implementation for Document entities based on the MySQL database
 * @author AleaActaEst
 *
 * @param <T> - the operational entity that this DAO will work with.
 * @param <S> - The specific search criteria for entity <T>
 */
public class DocumentMySQLDao<T, S> extends BaseMySQLDao<Document, DocumentSearchCriteria>{

	/**
	 * Services used by the implementation
	 */
	private AuditMySQLDao<AuditEntity, AuditSearchCriteria> auditService = new AuditMySQLDao<AuditEntity, AuditSearchCriteria>();
	private UserMySQLDao<User, UserSearchCriteria> userService = new UserMySQLDao<User, UserSearchCriteria>();
	private TagMySQLDao<TagData, TagSearchCriteria> tagService = new TagMySQLDao<TagData, TagSearchCriteria>();
	private OrganizationMySQLDao<Organization, OrganizationSearchCriteria> groupService = new OrganizationMySQLDao<Organization, OrganizationSearchCriteria> ();
	
	public  static final String CUSTOM_TAG_PREFIX = "CUSTOM: ";
	
	
	/**
	 * Get all the documents in the data base.
	 * @return -  a list of documents that were found.
	 * @throws Exception - if there was an issue
	 */
	public List<Document> getAllDocuments(User user, String docType, boolean permissionsOverride) throws Exception{
		List<Document> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		String userName = null;
		long organizationId = 1;
		getLog().debug("FETCH ALL: <Service In>");
		
		// look up the user by name which should be unique
		//
		if(user != null){
			userName = user.getCredentials().getUsername();
			organizationId = user.getUserOrganizations().get(0).getId();
		}else{
			// use default organization
			organizationId = 1;
			
		}
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		getLog().debug("FETCH ALL: <Service Create Statement>");
		
		if(!docType.isEmpty()){
			preparedSELECTstatement = conn
					.prepareStatement("SELECT d.id, d.doc_status, d.username, d.creation_timestamp, d.document_type, d.type_hex_color, "
							+ "d.document_type_id, d.sync_id, d.organization_id, d.group_id, gd.name AS group_name, "
							+ "gdt.name AS group_type_name, gdt.order_index, dt.name, dt.value, "
							+ "dt.color_hex_code, dt.document_designation, dt.delete_flag "
							+ "FROM document d "
							+ "JOIN document_type dt ON d.document_type_id = dt.id "
							+ "JOIN group_data gd ON gd.id = d.group_id "
							+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id "
							+ "WHERE organization_id = ? AND dt.document_designation = ? ");
			
			// execute the statement 
			preparedSELECTstatement.setLong(1, organizationId);
			preparedSELECTstatement.setString(2, docType);
		}else{
			preparedSELECTstatement = conn
					.prepareStatement("SELECT d.id, d.doc_status, d.username, d.creation_timestamp, d.document_type, d.type_hex_color, "
							+ "d.document_type_id, d.sync_id, d.organization_id, d.group_id, "
							+ "gd.name AS group_name, gdt.name AS group_type_name, gdt.order_index, "
							+ "dt.name, dt.value, dt.color_hex_code, dt.document_designation, dt.delete_flag "
							+ "FROM document d "
							+ "JOIN document_type dt ON d.document_type_id = dt.id "
							+ "JOIN group_data gd ON gd.id = d.group_id "
							+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id "
							+ "WHERE organization_id = ?");
			// execute the statement 
			preparedSELECTstatement.setLong(1, organizationId);
		}
		

		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        getLog().debug("FETCH ALL: <Service Create Statement DONE>");
        
        //
        // process the result
        getLog().debug("FETCH ALL: <Service Extracting Data>");
        result = extractDocumentFromResult(resultSet, user,docType, permissionsOverride);
        getLog().debug("FETCH ALL: <Service Extracting Data DONE>");

		// release the connection
		closeConnection(conn);
		
		getLog().debug("FETCH ALL: <Service Check READ>");
		//
		// Check if the user has read the document
		//for(int i=0; i<result.size(); i++){
		//	boolean wasRead = wasDocumentRead(result.get(i).getSyncID(),userName );
		//	result.get(i).setCurrentUserRead(wasRead);
		//}
		getLog().debug("FETCH ALL: <Service Check READ DONE>");
		
		//////////////////////////////////////////////////////////////////////////////
		// check any permissions issues
		
		getLog().debug("FETCH ALL: <Service Check Permissions>");
		//
		// 1 up and 1 down permissions for the document
		/**
		ListIterator<Document> iter = result.listIterator();
		while(iter.hasNext()){
			Document iterDoc = iter.next();
		    if(!hasPermissions(user, iterDoc, permissionsOverride)){
		        iter.remove();
		        continue;
		    }
		    if(!docType.isEmpty() && !iterDoc.getType().getDocumentDesignation().equals(docType)){
		    	iter.remove();
		    }
		}
		*/
		
		getLog().debug("FETCH ALL: <Service Check Permissions Done>");
		getLog().debug("FETCH ALL: <Service Out>");		
		// return the result
		return result;
				
	}
	
	

	
	/**
	 * Get all the documents in the data base.
	 * @return -  a list of documents that were found.
	 * @throws Exception - if there was an issue
	 */
	public List<Document> getDocumentById(long docId) throws Exception{
		List<Document> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
				.prepareStatement("SELECT d.id, d.doc_status, d.username, d.creation_timestamp, d.document_type, d.type_hex_color, "
									+ "d.document_type_id, d.sync_id, d.organization_id, d.group_id, "
									+ "gd.name AS group_name, gdt.name AS group_type_name, gdt.order_index, "
									+ "dt.name, dt.value, dt.color_hex_code, dt.document_designation, dt.delete_flag "
									+ "FROM document d "
									+ "JOIN document_type dt ON d.document_type_id = dt.id "
									+ "JOIN group_data gd ON gd.id = d.group_id "
									+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id "
									+ "WHERE d.id = ? ");
                				
		// execute the statement 
		preparedSELECTstatement.setLong(1, docId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractDocumentFromResult(resultSet, null, null, false);

		// release the connection
		closeConnection(conn);
		
		// return the result
		return result;
				
	}
	
	
	/**
	 * Get all the documents in the data base.
	 * @return -  a list of documents that were found.
	 * @throws Exception - if there was an issue
	 */
	public List<Document> getRecursiveDocumentById(long docId) throws Exception{
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		List<Document> seenList = new ArrayList<Document>();
		Stack<Long> processStack = new Stack<Long>();
		
		processStack.push(docId);

		// 
		// process the request
		
		while(!processStack.isEmpty()){
			// get the connection
			Connection conn = openConnection();
			
			Document resultDocument=null;
			long idToProcess = processStack.pop();
			getLog().info("POP of ID " + idToProcess + " --> " + processStack);
			// create the statement
			preparedSELECTstatement = conn
					.prepareStatement("SELECT d.id, d.doc_status, d.username, d.creation_timestamp, d.document_type, d.type_hex_color, "
										+ "d.document_type_id, d.sync_id, d.organization_id, d.group_id, "
										+ "gd.name AS group_name, gdt.name AS group_type_name, gdt.order_index, "
										+ "dt.name, dt.value, dt.color_hex_code, dt.document_designation, dt.delete_flag "
										+ "FROM document d "
										+ "JOIN document_type dt ON d.document_type_id = dt.id "
										+ "JOIN group_data gd ON gd.id = d.group_id "
										+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id "
										+ "WHERE d.id = ? ");
			
			
			// execute the statement 
			preparedSELECTstatement.setLong(1, idToProcess);
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        //
	        // process the result
	        resultDocument = extractDocumentFromResult(resultSet, null, null, false).get(0);
	        seenList.add(resultDocument);
	        for(int i=0; i<resultDocument.getLinkedDocuments().size(); i++){
	        	if(!seenList.contains(resultDocument.getLinkedDocuments().get(i))){
	        		processStack.push(resultDocument.getLinkedDocuments().get(i).getId());
	        	}
	        }
	        for(int i=0; i<resultDocument.getAttachedDocuments().size(); i++){
	        	if(!seenList.contains(resultDocument.getAttachedDocuments().get(i))){
	        		processStack.push(resultDocument.getAttachedDocuments().get(i).getId());
	        	}
	        }

			// release the connection
			closeConnection(conn);
		}
		getLog().info("RECURSIVE TRACEABILITY --> " + seenList);
				
		// return the result
		return seenList;
				
	}
	
	/**
	 * Get all the documents in the data base.
	 * 
	 * @return -  a list of documents that were found.
	 * @throws Exception - if there was an issue
	 */
	public List<Document> getDocumentsByIdList(long[] docIdList) throws Exception{
		List<Document> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the list of Ids
		String idlist ="(";
		for(int i=0; i<docIdList.length; i++ ){
			idlist += "?,";
		}
		idlist = idlist.substring(0, idlist.length()-1);
		idlist += ")";
		getLog().info("Document Id List: " + idlist);
				
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT id, status, username, creation_timestamp, document_type, type_hex_color, document_type_id, "
                				+ "sync_id, organization_id, group_id, "
                				+ "dt.name, dt.value, dt.color_hex_code, dt.document_designation, dt.delete_flag "
                				+ "FROM document d "
                				+ "JOIN document_type dt ON d.document_type_id = dt.id "
                				+ "WHERE id IN " 
                				+ idlist);
		// execute the statement 
		for(int i=0; i<docIdList.length; i++ ){
			preparedSELECTstatement.setLong(i+1, docIdList[i]);
		}
		
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractDocumentFromResult(resultSet, null, null, false);

		// release the connection
		closeConnection(conn);
		
		// return the result
		return result;
				
	}
	
	
	
	/**
	 * Get all available document types
	 * 
	 * @return - list of the types or empty if there were none
	 * @throws Exception - if there was an issue
	 */
	public List<DocumentType> getAllDocumentTypes() throws Exception{
		List<DocumentType> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT id, name, value, color_hex_code, document_designation from document_type " +
                					"where delete_flag=0");
		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractDocumentTypesFromResult(resultSet);

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return result;
				
	}
	
	/**
	 * Link the input documents to the parent doc.
	 * 
	 * @param docs - the documents to be linked
	 * @param docId - the id of the parent doc
	 * @throws Exception - if there was an issue
	 */
	public void createDocumentLinks(List<Document> docs, long docId) throws Exception { 
		
		// 
		// process the request
				
		// delete previous tags
		deleteLinkedDocuments(docId);
		
		// create the new tags
		for(int i=0; i< docs.size(); i++){
			System.out.println("Creating document link for: " + docId + " --> " + docs.get(i).getId());
			createDocumentLink(docId, docs.get(i).getId());
		}
		getLog().info("Document Links for: " + docId);
		
	}
	
	/**
	 * Attach backup documents to the parent doc
	 * 
	 * @param docs - the backup docs to attach
	 * @param docId - the id of the parent doc
	 * @throws Exception - if there was an issue
	 */
	public void createDocumentAttachments(List<Document> docs, long docId) throws Exception { 
		
		// 
		// process the request
				
		// delete previous tags
		deleteAttachedDocuments(docId);
		
		// create the new tags
		for(int i=0; i< docs.size(); i++){
			System.out.println("Creating document link for: " + docId + " --> " + docs.get(i).getId());
			createDocumentAttachment(docId, docs.get(i).getId());
		}
		getLog().info("Document Links for: " + docId);
		
	}
	
	/**
	 * Attach notes to a given document
	 * 
	 * @param notes - the notes to attach
	 * @param docId - the id of the target doc
	 * @throws Exception - if there was an issue
	 */
	public void createDocumentNotes(List<NoteData> notes, long docId) throws Exception { 
		
		// 
		// process the request
				
		// delete previous tags
		deleteDocumentNotes(docId);
		
		// create the new notes
		for(int i=0; i< notes.size(); i++){
			System.out.println("Creating document note for: " + docId + " --> " + notes.get(i).getId());
			createDocumentNote(docId, notes.get(i), i);
		}
		getLog().info("Document Notes for: " + docId);
		
	}
	
	/**
	 * Attach document recipients to the given document
	 * 
	 * @param recipients - list of recipients
	 * @param docId - the id of the doc that is the target
	 * @throws Exception - if there was an issue
	 */
	public void createDocumentRecipients(List<User> recipients, long docId) throws Exception { 
		
		// 
		// process the request
		
		// first delete previous ones
		deleteDocumentRecipients(docId);
				
		
		// create the new tags
		for(int i=0; i< recipients.size(); i++){
			System.out.println("Creating document recipient for: " + docId + " --> " + recipients.get(i).getId());
			createDocumentRecipient(docId, recipients.get(i).getId());
		}
		getLog().info("Document Links for: " + docId);
		
	}
	
	
	/**
	 * Delete the attached docs
	 * 
	 * @param docId - the id of the document to purge the attachments from 
	 * @throws ServiceManagementException - if there was an issue
	 */
	public void deleteAttachedDocuments(long docId) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		
		// create the query
		String deleteQuery = " DELETE FROM document_attachment_data WHERE parent_doc_id = ?";
		
		// create the statement
		try {
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			
			// execute the statement 
			preparedDELETEStatement.setLong(1, docId);
			preparedDELETEStatement.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting docuemnt attachments for document with id=" + docId, e);
		}

		// release the connection
		closeConnection(conn);
	}
	
	/**
	 * Remove the given document page form the system
	 * 
	 * @param pageId - the unique id of the page to remove
	 * @throws ServiceManagementException - if there was an issue
	 */
	public void deleteDocumentPage(long pageId) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		
		// create the query
		String deleteQuery = " DELETE FROM document_page WHERE id = ?";
		
		// create the statement
		try {
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			
			// execute the statement 
			preparedDELETEStatement.setLong(1, pageId);
			preparedDELETEStatement.execute();
			//
			// Re-allign the pages by ordinal <TODO>
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting docuemnt page with id=" + pageId, e);
		}

		// release the connection
		closeConnection(conn);
	}
	
	
	/**
	 * Remove the given document page form the system
	 * 
	 * @param pageId - the unique id of the page to remove
	 * @throws ServiceManagementException - if there was an issue
	 */
	public void deleteDocumentPages(long[] pageIds) throws ServiceManagementException {
		
		for(int i=0; i< pageIds.length; i++){
			deleteDocumentPage(pageIds[i]);
		}
	}
	
	
	
	
	/**
	 * Remove all the document pages form the system
	 * 
	 * @param docId - the unique id of the doc to which the pages are attached
	 * @throws ServiceManagementException - if there was an issue
	 */
	public void deleteAllDocumentPages(long docId) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		
		// create the query
		String deleteQuery = " DELETE FROM document_page WHERE document_id = ?";
		
		// create the statement
		try {
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			
			// execute the statement 
			preparedDELETEStatement.setLong(1, docId);
			preparedDELETEStatement.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting docuemnt pages for a doc with id= " + docId, e);
		}

		// release the connection
		closeConnection(conn);
	}
	
	/**
	 * Remove all the attached documents from the system
	 * 
	 * @param docId - the unique id of the doc to which the pages are attached
	 * @throws ServiceManagementException - if there was an issue
	 */
	public void deleteAllDocumentAttachedDocs(long docId) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		
		// create the query
		String deleteQuery = " DELETE FROM document_attachment_data WHERE parent_doc_id = ? OR attached_doc_id = ?";
		
		// create the statement
		try {
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			
			// execute the statement 
			preparedDELETEStatement.setLong(1, docId);
			preparedDELETEStatement.setLong(2, docId);
			preparedDELETEStatement.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting attached documents for a doc with id= " + docId, e);
		}

		// release the connection
		closeConnection(conn);
	}	
		

	
	
	/**
	 * Remove all the attached documents from the system
	 * 
	 * @param docId - the unique id of the doc to which the pages are attached
	 * @throws ServiceManagementException - if there was an issue
	 */
	public void deleteAllDocumentLinkedDocs(long docId) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		
		// create the query
		String deleteQuery = " DELETE FROM document_link_data WHERE parent_doc_id = ? OR linked_doc_id = ?";
		
		// create the statement
		try {
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			
			// execute the statement 
			preparedDELETEStatement.setLong(1, docId);
			preparedDELETEStatement.setLong(2, docId);
			preparedDELETEStatement.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting attached documents for a doc with id= " + docId, e);
		}

		// release the connection
		closeConnection(conn);
	}	
	
	
	/**
	 * Remove all the attached documents from the system
	 * 
	 * @param docId - the unique id of the doc to which the pages are attached
	 * @throws ServiceManagementException - if there was an issue
	 */
	public void deleteAllDocumentTags(long docId) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		
		// create the query
		String deleteQuery = " DELETE FROM document_tag_data WHERE document_id = ?";
		
		// create the statement
		try {
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			
			// execute the statement 
			preparedDELETEStatement.setLong(1, docId);
			preparedDELETEStatement.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting attached documents for a doc with id= " + docId, e);
		}

		// release the connection
		closeConnection(conn);
	}	
	
	
	
	
	/**
	 * Remove all the attached documents from the system
	 * 
	 * @param docId - the unique id of the doc to which the pages are attached
	 * @throws ServiceManagementException - if there was an issue
	 */
	public void deleteAllDocumentNotes(long docId) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		
		// create the query
		String deleteQuery = " DELETE FROM document_notes_data WHERE parent_doc_id = ?";
		
		// create the statement
		try {
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			
			// execute the statement 
			preparedDELETEStatement.setLong(1, docId);
			preparedDELETEStatement.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting attached documents for a doc with id= " + docId, e);
		}

		// release the connection
		closeConnection(conn);
	}	
	

	
	/**
	 * Remove the given document pages form the system
	 * 
	 * @param pageId - the unique id of the page to remove
	 * @throws ServiceManagementException - if there was an issue
	 */
	public void updateDocumentPageCollation(long docId) throws ServiceManagementException {
		PreparedStatement preparedSELECTStatement = null;
		ResultSet resultSet = null;
		
		// get the connection
		Connection conn = openConnection();
		
		
		// create the query
		String selectQuery = " SELECT FROM document_page WHERE id = ?";
		
		// create the statement
		try {
			preparedSELECTStatement = conn
			        .prepareStatement(selectQuery);
			
			// execute the statement 
			preparedSELECTStatement.setLong(1, docId);
			resultSet = preparedSELECTStatement.executeQuery();
			
			//
	        // process the result
	        extractSparseDocumentPagesFromResult(resultSet);

			// release the connection
			closeConnection(conn);
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while collating document with id= " + docId, e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new ServiceManagementException("Error while collating document with id= " + docId, e);
		}

		// release the connection
		closeConnection(conn);
	}
	
	/**
	 * Delete all the notes for e a given document
	 * 
	 * @param docId - the id of the target doc
	 * @throws ServiceManagementException - if there was an issue
	 */
	public void deleteDocumentNotes(long docId) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		
		// create the query
		String deleteQuery = " DELETE FROM document_notes_data WHERE parent_doc_id = ?";
		
		// create the statement
		try {
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			
			// execute the statement 
			preparedDELETEStatement.setLong(1, docId);
			preparedDELETEStatement.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting document notes for document with id=" + docId, e);
		}

		// release the connection
		closeConnection(conn);
	}	

	/**
	 * Remove all linked documents from a target doc. 
	 * If there are no linked docs to remove do nothing.
	 * 
	 * @param docId - the id of the target doc
	 * @throws ServiceManagementException - if there was an issue
	 */
	public void deleteLinkedDocuments(long docId) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		
		// create the query
		String deleteQuery = " DELETE FROM document_link_data WHERE parent_doc_id = ?";
		
		// create the statement
		try {
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			
			// execute the statement 
			preparedDELETEStatement.setLong(1, docId);
			preparedDELETEStatement.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting docuemnt links for document with id=" + docId, e);
		}

		// release the connection
		closeConnection(conn);
	}
	
	/**
	 * Remove all recipients attached to this document.
	 * If there are no recipients attached then do nothing.
	 * 
	 * @param docId - the target doc id
	 * @throws ServiceManagementException - if there was an issue
	 */
	public void deleteDocumentRecipients(long docId) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		
		// create the query
		String deleteQuery = " DELETE FROM document_recipient_data WHERE parent_doc_id = ?";
		
		// create the statement
		try {
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			
			// execute the statement 
			preparedDELETEStatement.setLong(1, docId);
			preparedDELETEStatement.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting document recipients for document with id=" + docId, e);
		}

		// release the connection
		closeConnection(conn);
	}
	
	/**
	 * Attach a backup document.
	 * 
	 * @param documentID - the id of the target document
	 * @param attachedDocumentId - the id of the document being attached to the parent.
	 * @throws Exception - if there was an issue
	 */
	private void createDocumentAttachment(long documentID, long attachedDocumentId) throws Exception {
		PreparedStatement preparedINSERTstatement;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request

		// create the query
		String insertQuery = " insert into document_attachment_data ("
				+ "parent_doc_id, "
				+ "attached_doc_id"
				+ ")"
		        + " values (?, ?)";
		
		// create the statement
		preparedINSERTstatement = conn
                .prepareStatement(insertQuery);
		// execute the statement 
		preparedINSERTstatement.setLong(1, documentID);
		preparedINSERTstatement.setLong(2, attachedDocumentId);
		preparedINSERTstatement.executeUpdate();
		
		// release the connection
		closeConnection(conn);
		
		getLog().info("Attached to a Document: " + documentID + " " + attachedDocumentId);
		
	}
	
	/**
	 * Attach a document recipient to a target document
	 * 
	 * @param documentID - the target document id
	 * @param userId - the user being attached as recipient
	 * @throws Exception - if there was an issue
	 */
	private void createDocumentRecipient(long documentID, long userId) throws Exception {
		PreparedStatement preparedINSERTstatement;
		User user = new User();
		AuditEntity auditEntity = new AuditEntity();
		List<Document> docs = null;
		
		//
		// create support services
		
		//
		// Initialize services
		userService.init();
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request

		// create the query
		String insertQuery = " insert into document_recipient_data ("
				+ "parent_doc_id, "
				+ "to_user_id"
				+ ")"
		        + " values (?, ?)";
		
		// create the statement
		preparedINSERTstatement = conn
                .prepareStatement(insertQuery);
		// execute the statement 
		preparedINSERTstatement.setLong(1, documentID);
		preparedINSERTstatement.setLong(2, userId);
		preparedINSERTstatement.executeUpdate();
		
		// release the connection
		closeConnection(conn);
		
	
		getLog().info("Added a Recipient to a Document: " + documentID + " " + userId);
		
	}
	
	/**
	 * Create a document note and attach it
	 * 
	 * @param documentID
	 * @param note
	 * @param ordinal
	 * @throws Exception - if there was an issue
	 */
	private void createDocumentNote(long documentID, NoteData note, int ordinal) throws Exception {
		PreparedStatement preparedINSERTstatement;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request

		// create the query
		String insertQuery = " insert into document_notes_data ("
				+ "parent_doc_id, ordinal, note_data, creator_user "
				+ ")"
		        + " values (?, ?, ?, ?)";
		
		// create the statement
		preparedINSERTstatement = conn
                .prepareStatement(insertQuery);
		// execute the statement 
		preparedINSERTstatement.setLong(1, documentID);
		preparedINSERTstatement.setInt(2, ordinal);
		preparedINSERTstatement.setString(3, note.getNote());
		preparedINSERTstatement.setString(4, note.getOwner());
		preparedINSERTstatement.executeUpdate();
		
		// release the connection
		closeConnection(conn);
		
		getLog().info("Note for Document: " + documentID + " " + note);
		
	}
	
	/**
	 * Link a document to the target doc
	 * 
	 * @param documentID
	 * @param linkedDocumentId
	 * @throws Exception - if there was an issue
	 */
	private void createDocumentLink(long documentID, long linkedDocumentId) throws Exception {
		PreparedStatement preparedINSERTstatement;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request

		// create the query
		String insertQuery = " insert into document_link_data ("
				+ "parent_doc_id, "
				+ "linked_doc_id"
				+ ")"
		        + " values (?, ?)";
		
		// create the statement
		preparedINSERTstatement = conn
                .prepareStatement(insertQuery);
		// execute the statement 
		preparedINSERTstatement.setLong(1, documentID);
		preparedINSERTstatement.setLong(2, linkedDocumentId);
		preparedINSERTstatement.executeUpdate();
		
		// release the connection
		closeConnection(conn);
		
		getLog().info("Linked to a Document: " + documentID + " " + linkedDocumentId);
		
	}
	
	/**
	 * Search through documents given the input criteria
	 * 
	 * @param searchCriteria
	 * @param user
	 * @return
	 * @throws Exception - if there was an issue
	 */
	public List<Document> searchDocuments(DocumentSearchCriteria searchCriteria, User user) throws Exception{
		List<Document> result=null;
		PreparedStatement preparedSEARCHstatement;
		ResultSet resultSet = null;
		String userName = null;
		long organizationId = 1;
		
		// look up the user by name which should be unique
		//
		if(user != null){
			userName = user.getCredentials().getUsername();
			organizationId = user.getUserOrganizations().get(0).getId();
		}else{
			// use default organization
			organizationId = 1;
			
		}
		
		//
		// Strings used to capture data
		// String SELECT_QUERY_PREFIX = "SELECT id, username, creation_timestamp, document_type, type_hex_color, document_type_id, sync_id, organization_id, group_id from document ";
		
		String SELECT_QUERY_PREFIX = "SELECT d.id, d.doc_status, d.username, d.creation_timestamp, d.document_type, d.type_hex_color, "
				+ "d.document_type_id, d.sync_id, d.organization_id, d.group_id, gd.name AS group_name, "
				+ "gdt.name AS group_type_name, gdt.order_index, "
				+ "dt.name, dt.value, dt.color_hex_code, dt.document_designation, dt.delete_flag "
				+ "FROM document d "
				+ "JOIN document_type dt ON d.document_type_id = dt.id "
				+ "JOIN group_data gd ON gd.id = d.group_id "
				+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id ";
		
		String DOCUMENT_TYPE_SQL = "document_type_id = ?";
		String USERNAME_SQL = "username = ?";
		String DATE_FROM_SQL = "creation_timestamp >= ?";
		String DATE_TO_SQL = "creation_timestamp <= ?";
		String ORGANIZATION_ID_SQL = "organization_id = ?";
		
		// 
		// create the WHERE Part of the query
		String WHERE_QUERY = "";
		List<String> WHERE_QUERY_ELEMENTS = new ArrayList<String>();
		WHERE_QUERY_ELEMENTS.add(ORGANIZATION_ID_SQL);
		if(IdentifiableEntity.isDefined(searchCriteria.getDocType())){
			WHERE_QUERY_ELEMENTS.add(DOCUMENT_TYPE_SQL);
		}
		if(!searchCriteria.getUserName().isEmpty() && searchCriteria.getUserName() != null){
			WHERE_QUERY_ELEMENTS.add(USERNAME_SQL);
		}
		if(searchCriteria.getDateFrom() != null){
			WHERE_QUERY_ELEMENTS.add(DATE_FROM_SQL);
		}
		if(searchCriteria.getDateTo() != null){
			WHERE_QUERY_ELEMENTS.add(DATE_TO_SQL);
		}
		
		//
		// Combine the WHERE part of the query into a preparedSEARCHstatement
		for(int i=0; i < WHERE_QUERY_ELEMENTS.size(); i++){
			if(i==0){
				WHERE_QUERY = " WHERE " + WHERE_QUERY_ELEMENTS.get(i);
			}else{
				WHERE_QUERY = WHERE_QUERY + " AND " + WHERE_QUERY_ELEMENTS.get(i);
			}
		}
		getLog().info("SEARCH DOCUMENTS DAO: - WHERE QUERY - " + WHERE_QUERY);
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
	
		// create the statement for the search with AND elements
		preparedSEARCHstatement = conn
                .prepareStatement(SELECT_QUERY_PREFIX 
                		+ WHERE_QUERY);
		
		// add the data into the statement
		for(int i=0; i < WHERE_QUERY_ELEMENTS.size(); i++){
			int statementIndex = i+1;
			// document type
			if(WHERE_QUERY_ELEMENTS.get(i).equals(DOCUMENT_TYPE_SQL)){
				preparedSEARCHstatement.setLong(statementIndex
						, searchCriteria.getDocType().getId());
			}
			// username
			if(WHERE_QUERY_ELEMENTS.get(i).equals(USERNAME_SQL)){
				preparedSEARCHstatement.setString(statementIndex
						, searchCriteria.getUserName());
			}
			// date from 
			if(WHERE_QUERY_ELEMENTS.get(i).equals(DATE_FROM_SQL)){
				preparedSEARCHstatement.setString(statementIndex
						, DateUtility.simpleDateFormat(searchCriteria.getDateFrom(), DateUtility.FORMAT_DATE_ONLY));
			}
			// date to 
			if(WHERE_QUERY_ELEMENTS.get(i).equals(DATE_TO_SQL)){
				preparedSEARCHstatement.setString(statementIndex
						, DateUtility.simpleDateFormat(searchCriteria.getDateTo(), DateUtility.FORMAT_DATE_ONLY));
			}
			// organization id
			if(WHERE_QUERY_ELEMENTS.get(i).equals(ORGANIZATION_ID_SQL)){
				preparedSEARCHstatement.setLong(statementIndex
						, organizationId);
			}
		}
		getLog().info("SEARCH DOCUMENTS DAO: - PREPARED STATEMENT - " + preparedSEARCHstatement.toString());

		// execute the statement 
        resultSet = preparedSEARCHstatement.executeQuery();
        
        //
        // process the result
        result = extractDocumentFromResult(resultSet, null, null, false);

		// release the connection
		closeConnection(conn);
		
		//
		// Check if the user has read the document
		for(int i=0; i<result.size(); i++){
			boolean wasRead = wasDocumentRead(result.get(i).getSyncID(), userName);
			result.get(i).setCurrentUserRead(wasRead);
		}

		// return the result
		return result;
				
	}
	
	/**
	 * Fetch the given document page
	 * 
	 * @param documentPageId
	 * @return
	 * @throws Exception - if there was an issue
	 */
	public InputStream getDocumentPage(long documentPageId) throws Exception{
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		InputStream binaryStream=null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT page_data from document_page WHERE id=?");
		// execute the statement 
		preparedSELECTstatement.setLong(1, documentPageId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        while (resultSet.next()) {
			// extract the data
        	binaryStream = resultSet.getBinaryStream("page_data");
        }
        

		// release the connection
		closeConnection(conn);
				
		// return the result
		return binaryStream;
				
	}
	
	/**
	 * Get the thumbnail for the given page
	 * 
	 * @param documentPageId
	 * @return
	 * @throws Exception - if there was an issue
	 */
	public InputStream getDocumentPageThumbnail(long documentPageId) throws Exception{
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		InputStream binaryStream=null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT page_data_thumbnail from document_page WHERE id=?");
		// execute the statement 
		preparedSELECTstatement.setLong(1, documentPageId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        while (resultSet.next()) {
			// extract the data
        	binaryStream = resultSet.getBinaryStream("page_data_thumbnail");
        }
        

		// release the connection
		closeConnection(conn);
		
		// return the result
		return binaryStream;
				
	}
	
	
	/**
	 * Create a new document in the database
	 * 
	 * @param newDocument
	 * @return
	 * @throws Exception - if there was an issue
	 */
	public Document create(Document newDocument) throws PersistenceException, IllegalArgumentException { 
		PreparedStatement preparedINSERTstatement;
		long returnId=0;
		
		// get the connection
		Connection conn = openConnection();
		userService.init();
		
		// 
		// process the request
		// byte[] imageBlob = RESTUtility.customGson.fromJson(newDocument.getPages().get(0).getBase64ImageData(), byte[].class);
		
		//
		// Pre-process creation for Profile Docs
		if(newDocument.getType().getDocumentDesignation().equals(DocumentType.DESIGNATION_PROFILE)){
			newDocument.setStatus(Document.STATUS_SUBMITTED);
		}
		
		
		
		// create the query
		String insertQuery = " insert into document ("
				+ "username, "
				+ "creation_timestamp, "
				+ "document_type, "
				+ "document_type_id, "
				+ "type_hex_color, "
				// + "document_image_blob, "
				+ "sync_id, "
				+ "organization_id, "
				+ "group_id, "
				+ "doc_status"
				+ ")"
		        + " values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		// create the statement
		try {
			preparedINSERTstatement = conn
			        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			
			// execute the statement 
			preparedINSERTstatement.setString(1, newDocument.getOwner());
			preparedINSERTstatement.setString(2, newDocument.getCreationTimestamp());
			preparedINSERTstatement.setString(3, newDocument.getDocumentType());
			preparedINSERTstatement.setLong(4, newDocument.getType().getId());
			preparedINSERTstatement.setString(5, newDocument.getTypeHEXColor());
			// .setBlob(6, new javax.sql.rowset.serial.SerialBlob(imageBlob));
			preparedINSERTstatement.setString(6, newDocument.getSyncID());
			preparedINSERTstatement.setLong(7, newDocument.getOrganizationId());
			preparedINSERTstatement.setLong(8, newDocument.getGroupId());
			preparedINSERTstatement.setString(9, newDocument.getStatus());
			preparedINSERTstatement.executeUpdate();
			
			// get the id of the created 
			ResultSet rs = preparedINSERTstatement.getGeneratedKeys();
			if (rs.next()){
				returnId=rs.getLong(1);
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

		// release the connection
		closeConnection(conn);
		
		// add the new id to the created document
		newDocument.setId(returnId);
		getLog().info("CREATE DOCUMENT: " + returnId + " " + newDocument);
		
		//
		// audit the request
		try {
			auditRequest(newDocument, AuditUserType.USER, AuditAction.DOCUMENT_CREATE, newDocument.getOwner());
			//
			// Audit the document status
			AuditAction action = null;
			if(newDocument.getStatus().equals(Document.STATUS_ACCEPTED)){
				action = AuditAction.DOCUMENT_ACCEPT;
			}
			if(newDocument.getStatus().equals(Document.STATUS_REJECTED)){
				action = AuditAction.DOCUMENT_REJECT;
			}
			if(newDocument.getStatus().equals(Document.STATUS_RESUBMITTED)){
				action = AuditAction.DOCUMENT_RESUBMIT;
			}
			if(newDocument.getStatus().equals(Document.STATUS_SUBMITTED)){
				action = AuditAction.DOCUMENT_SUBMIT;
			}
			if(newDocument.getStatus().equals(Document.STATUS_PENDING)){
				action = AuditAction.DOCUMENT_PENDING;
			}
			if(action != null){
				auditRequest(newDocument, AuditUserType.USER, action, newDocument.getOwner());
			}
			
		}catch (Exception e) {
			getLog().error("Error Auditing Document-Creation: " + e);
		}
		
		//
		// audit the request
		try {
			
		}catch (Exception e) {
			getLog().error("Error Auditing Document-Creation: " + e);
		}
		
		//
		// Create the pages
		//
		for(int i=0; i<newDocument.getPages().size(); i++){
			try {
				createDocumentPages(newDocument.getPages().get(i), newDocument.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//
		// Create the recipients
		//
		if(newDocument.getToRecipients().size() > 0){
			try {
				createDocumentRecipients(newDocument.getToRecipients(), newDocument.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//
		// Create the Attachments
		//
		if(newDocument.getAttachedDocuments().size() > 0){
			try {
				createDocumentAttachments(newDocument.getAttachedDocuments(), newDocument.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//
		// Create the Linked Docs
		//
		if(newDocument.getLinkedDocuments().size() > 0){
			try {
				createDocumentLinks(newDocument.getLinkedDocuments(), newDocument.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//
		// Create the Tags
		//
		if(newDocument.getTags().size() > 0){
			tagService.init();
			try {
				tagService.attach(newDocument.getTags(), newDocument.getId());
				newDocument.setTags(tagService.getAllTagsByDocId(newDocument.getId()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		return newDocument;
	}
	
	/**
	 * Update an existing document in the database
	 * 
	 * @param newDocument
	 * @return
	 * @throws Exception - if there was an issue
	 */
	public Document update(Document oldDocument) throws IllegalArgumentException, PersistenceException, EntityNotFoundException {
		// create the java mysql update preparedstatement
	    String query = "UPDATE document set doc_status = ? WHERE id = ?";
	    long docID = oldDocument.getId(); 
	    
	    // get the connection
	 	Connection conn = openConnection();
		userService.init();
	    
	    
	    try {
	    	PreparedStatement preparedUPDATEstatement = conn.prepareStatement(query);

	    	preparedUPDATEstatement.setString(1, oldDocument.getStatus());
			preparedUPDATEstatement.setLong(2, docID);
			
		    // execute the java preparedstatement
		    preparedUPDATEstatement.executeUpdate();
		    
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// release the connection
		closeConnection(conn);
		
		//
		// audit the request
		try {
			AuditAction action = null;
			if(oldDocument.getStatus().equals(Document.STATUS_ACCEPTED)){
				action = AuditAction.DOCUMENT_ACCEPT;
			}
			if(oldDocument.getStatus().equals(Document.STATUS_REJECTED)){
				action = AuditAction.DOCUMENT_REJECT;
			}
			if(oldDocument.getStatus().equals(Document.STATUS_RESUBMITTED)){
				action = AuditAction.DOCUMENT_RESUBMIT;
			}
			if(oldDocument.getStatus().equals(Document.STATUS_SUBMITTED)){
				action = AuditAction.DOCUMENT_SUBMIT;
			}
			if(oldDocument.getStatus().equals(Document.STATUS_PENDING)){
				action = AuditAction.DOCUMENT_PENDING;
			}
			auditRequest(oldDocument, AuditUserType.USER, action, oldDocument.getOwner());
		}catch (Exception e) {
			getLog().error("Error Auditing Document-Creation: " + e);
		}
		
		
		//
		// Create the pages
		//
		for(int i=0; i<oldDocument.getPages().size(); i++){
			try {
				createDocumentPages(oldDocument.getPages().get(i), oldDocument.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//
		// Create the recipients
		//
		if(oldDocument.getToRecipients().size() > 0){
			try {
				createDocumentRecipients(oldDocument.getToRecipients(), oldDocument.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//
		// Create the Attachments
		//
		if(oldDocument.getAttachedDocuments().size() > 0){
			try {
				createDocumentAttachments(oldDocument.getAttachedDocuments(), oldDocument.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//
		// Create the Linked Docs
		//
		if(oldDocument.getLinkedDocuments().size() > 0){
			try {
				createDocumentLinks(oldDocument.getLinkedDocuments(), oldDocument.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//
		// Create the Tags
		//
		if(oldDocument.getTags().size() > 0){
			tagService.init();
			try {
				tagService.attach(oldDocument.getTags(), oldDocument.getId());
				oldDocument.setTags(tagService.getAllTagsByDocId(oldDocument.getId()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		return oldDocument;
	}
	
	
	/**
	 * Delete the given document with the input ID.
	 * If the document does not exist then we will just ignore the request.
	 * 
	 * @param id - the sync id for the document to delete
	 * @throws ServiceManagementException - if any error occurred during operation
	 */
	public void delete(String id) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		long docId = 0;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		

		// create the query
		String deleteQuery = " DELETE FROM document WHERE sync_id = ?";
		
		// create the statement
		try {
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			
			// get the actual id of the document
			docId = getDocIdBySyncId(id);
			
			// delete the pages first
			deleteAllDocumentPages(docId);
			deleteAllDocumentAttachedDocs(docId);
			deleteAllDocumentLinkedDocs(docId);
			deleteAllDocumentNotes(docId);
			deleteDocumentRecipients(docId);
			deleteAllDocumentTags(docId);
			
			
			// execute the statement to delete the doc
			preparedDELETEStatement.setString(1, id);
			preparedDELETEStatement.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting a document with id=" + id, e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// release the connection
		closeConnection(conn);
	}
	
	/**
	 * check if a given document has been read by the specific user
	 * 
	 * @param documentID - the id of the document to check
	 * @param userName - the user to check against
	 * @return - true if the user has read this document already; false otherwise
	 * @throws Exception - if there was an issue
	 */
	public boolean wasDocumentRead(String documentID, String userName) throws Exception {
		boolean result=false;
		
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT item_id, actor_name from audit_data " 
                		+ "WHERE item_id=? AND actor_name=?");
		// execute the statement 
		preparedSELECTstatement.setString(1, documentID);
		preparedSELECTstatement.setString(2, userName);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        while (resultSet.next()) {
			// extract the data
        	result = true;
        }

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
	}
	
	
	/**
	 * Set the GPS location for the document
	 * @param docId
	 * @param gpsLocation
	 * @throws PersistenceException 
	 * @throws SQLException 
	 */
	public void setDocumentGPSLocation(String syncId, String gpsLocation) throws PersistenceException, SQLException{
		PreparedStatement preparedINSERTstatement;
		
		// get the connection
		Connection conn = openConnection();
		
		
		// create the query
		String insertQuery = " insert into document_location_data ("
				+ "document_sync_id, "
				+ "gps_location"
				+ ")"
		        + " values (?, ?)";
		
		// create the statement
		preparedINSERTstatement = conn
                .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
		// execute the statement 
		preparedINSERTstatement.setString(1, syncId);
		preparedINSERTstatement.setString(2, gpsLocation);
		preparedINSERTstatement.executeUpdate();
		
		// release the connection
		closeConnection(conn);
		
		// return the result
		getLog().info("Add GPS LOcation: " + syncId + ":" + gpsLocation);

		
	}
	
	/**
	 * Flag the audit doc as being read.
	 * 
	 * @param record - the audit record to flag
	 * @return - the updated entity
	 * @throws Exception - if there was an issue
	 */
	public AuditEntity setDocumentAuditAsRead(AuditEntity record) throws Exception {
		AuditEntity newAuditEntity = new AuditEntity();
		PreparedStatement preparedINSERTstatement;
		long returnId=0;
		
		// get the connection
		Connection conn = openConnection();
		
		
		// create the query
		String insertQuery = " insert into audit_data ("
				+ "actor_name, "
				+ "action, "
				+ "item_type, "
				+ "item_id"
				+ ")"
		        + " values (?, ?, ?, ?)";
		
		// create the statement
		preparedINSERTstatement = conn
                .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
		// execute the statement 
		preparedINSERTstatement.setString(1, record.getActor().getName());
		preparedINSERTstatement.setString(2, record.getAction().toString());
		preparedINSERTstatement.setString(3, record.getItemType());
		preparedINSERTstatement.setString(4, record.getItemId());
		preparedINSERTstatement.executeUpdate();
		
		// get the id of the created 
		ResultSet rs = preparedINSERTstatement.getGeneratedKeys();
		if (rs.next()){
			returnId=rs.getLong(1);
		}

		// release the connection
		closeConnection(conn);
		
		// return the result
		newAuditEntity.setId(returnId);
		getLog().info("CREATE AUDIT RECORD: " + newAuditEntity);
		
		return newAuditEntity;
	}
	
	
	
	/**
	 * Private helper methods
	 */
	
	/**
	 * Translate SQL results into model data
	 * 
	 * @param resultSet - the result set with Document data
	 * @return - converted data
	 * @throws Exception - if there was an issue
	 */
	private List<Document> extractDocumentFromResult(ResultSet resultSet, User callerUser, String docType, boolean permissionsOverride) throws Exception {
		List<Document> documents= new ArrayList<Document>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            String username = resultSet.getString("username");
            String creationDate = resultSet.getString("creation_timestamp");
            String documentType = resultSet.getString("document_type");
            String typeHexColor = resultSet.getString("type_hex_color");
            Long documentTypeId = resultSet.getLong("document_type_id");
            String documentSyncId = resultSet.getString("sync_id");
            long organizationId = resultSet.getLong("organization_id");
            long groupId = resultSet.getLong("group_id");
            String groupName = resultSet.getString("group_name");
            String groupTypeName = resultSet.getString("group_type_name");
            int groupTypeOrderIndex = resultSet.getInt("order_index");
            String  docStatus = resultSet.getString("doc_status");
            
            // Create a new Document
            Document document = new Document();
            // DocumentType docType = new DocumentType();
            // docType.setId(documentTypeId);
            
			long docTypeId = resultSet.getLong("document_type_id");
            String name = resultSet.getString("name");
            String value = resultSet.getString("value");
            String typeColorCode = resultSet.getString("color_hex_code");
            String documentDesignation = resultSet.getString("document_designation");
            
      
            
            // Create a new Document Type
            DocumentType documentTypeObject = new DocumentType();
            
            // populate the entity
            documentTypeObject.setId(docTypeId);
            documentTypeObject.setName(name);
            documentTypeObject.setValue(value);
            documentTypeObject.setHexColorCode(typeColorCode);
            documentTypeObject.setDocumentDesignation(documentDesignation);

            document.setType(documentTypeObject);
            
            
            // populate the Document entity
            document.setOwner(username);
            document.setId(id);
            document.setDocumentType(documentType);
            document.setStatus(docStatus);
            // convert the data through proper format
            //
            if(creationDate.contains("/")){
            	// convert to older style
            	creationDate = DateUtility.formatFullDateStringToSimpleDateTimeString(creationDate);
            }
            document.setCreationTimestamp(creationDate);
            document.setTypeHEXColor(typeHexColor);
            document.setSyncID(documentSyncId);
            document.setOrganizationId(organizationId);
            document.setGroupId(groupId);
            document.setGroupName(groupName);
            document.setGroupTypeName(groupTypeName);
            document.setGroupTypeOrderIndex(groupTypeOrderIndex);
            
            //
            // Get the document pages
            List<DocumentPage> pages = fetchDocumentPages(id);
            document.setPages(pages);
            
            //
            // Get Tags
            List<TagData> tags = fetchDocumentTags(id);
            document.setTags(tags);
            
            //
            // Get Linked Documents
            List<Document> linkedDocs = fetchDocumentLinks(id);
            document.setLinkedDocuments(linkedDocs);
            
            //
            // Get attached Documents
            List<Document> attacheddDocs = fetchDocumentAttachments(id);
            document.setAttachedDocuments(attacheddDocs);
            
            //
            // Get recipients
            List<User> recipients = fetchDocumentRecipients(document.getId());
            
            //
            // Get notes
            List<NoteData> documentNotes = fetchDocumentNotes(document.getId());
            document.setNotes(documentNotes);
            
            //
            // Get Types
            //docType = fetchDocumentTypeData(docType.getId());
            //document.setType(docType);
            
            if(callerUser != null){
            	boolean wasRead = wasDocumentRead(document.getSyncID(),callerUser.getName() );
                document.setCurrentUserRead(wasRead);
            }
            
            document.setToRecipients(recipients);
            
            boolean doNotAddDoc = false;
            if(docType != null){
            	if(!hasPermissions(callerUser, document, permissionsOverride)){
            		doNotAddDoc = true;
    		    }else if(!docType.isEmpty() && !document.getType().getDocumentDesignation().equals(docType)){
    		    	doNotAddDoc = true;
    		    }
            }

            if(!doNotAddDoc){
            	documents.add(document);
            }
             
        }
		
		return documents;
	}
	
	
	/**
	 * Create a new page for the given document
	 * 
	 * @param page - the page to be added
	 * @param documentID - the document for which the page is being added
	 * @return - the updated page data
	 * @throws Exception - if there was an issue
	 */
	public DocumentPage createDocumentPages(DocumentPage page, long documentID) throws Exception {
		PreparedStatement preparedINSERTstatement;
		long returnId=0;
		byte[] imageBlob = null;
		 
		 
		// check if this is already created
		if(page.getId() > 0){
			return page;
		}
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// extract the image
		
		if(page.getBase64ImageData() != null){
			imageBlob = RESTUtility.customGson.fromJson(page.getBase64ImageData(), byte[].class);
			getLog().info("EXTRACTING PAGE: Size: " + FileUtils.readableFileSize(imageBlob.length));
			//getLog().info("EXTRACTING PAGE: Size In KB: " + (page.getBase64ImageData().length()/1000f));
		}else if(page.getPageData() != null){
			imageBlob = (byte[])(page.getPageData());
			getLog().info("EXTRACTING PAGE: Size: " + FileUtils.readableFileSize(imageBlob.length));
		}
		
		// imageBlob =  rotatePageIfNeded(imageBlob);
		// getLog().info("EXTRACTING PAGE: Size After Rotation: " + FileUtils.readableFileSize(imageBlob.length));
		
		//
		// generate the thumbnail
		byte[] thumbnailInByte = generateThumbnail(imageBlob);
		


		// create the query
		String insertQuery = " insert into document_page ("
				+ "page_number, "
				+ "document_id, "
				+ "page_data_thumbnail, "
				+ "page_data"
				+ ")"
		        + " values (?, ?, ?, ?)";
		
		// create the statement
		preparedINSERTstatement = conn
                .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
		// execute the statement 
		preparedINSERTstatement.setLong(1, page.getPageNumber());
		preparedINSERTstatement.setLong(2, documentID);
		preparedINSERTstatement.setBlob(3, new javax.sql.rowset.serial.SerialBlob(thumbnailInByte));
		preparedINSERTstatement.setBlob(4, new javax.sql.rowset.serial.SerialBlob(imageBlob));
		preparedINSERTstatement.executeUpdate();
		
		// get the id of the created 
		ResultSet rs = preparedINSERTstatement.getGeneratedKeys();
		if (rs.next()){
			returnId=rs.getLong(1);
		}

		// release the connection
		closeConnection(conn);
		
		// return the result
		page.setId(returnId);
		getLog().info("CREATE PAGE: " + page);
		return page;
		
	}
	
	/**
	 * Create a new page for the given document
	 * 
	 * @param page - the page to be added
	 * @param documentID - the document for which the page is being added
	 * @return - the updated page data
	 * @throws Exception - if there was an issue
	 */
	public DocumentPage createDocumentPageFromFormData(InputStream profileImage, DocumentPage page, long docId) throws Exception {
		PreparedStatement preparedINSERTstatement;
		BufferedImage bImageFromConvert = null;
		long returnId=0;
		byte[] imageBlob = null;
		 
		 
		// check if this is already created
		if(page.getId() > 0){
			return page;
		}
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		// 
		// process the request
		try {
			bImageFromConvert = ImageIO.read(profileImage);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write( bImageFromConvert, "jpg", baos );
			baos.flush();
			imageBlob = baos.toByteArray();
			baos.close();

			// imageBlob =  rotatePageIfNeded(imageBlob);
			// getLog().info("EXTRACTING PAGE: Size After Rotation: " + FileUtils.readableFileSize(imageBlob.length));
			
			//
			// generate the thumbnail
			byte[] thumbnailInByte = generateThumbnail(imageBlob);

			// create the query
			String insertQuery = " insert into document_page ("
					+ "page_number, "
					+ "document_id, "
					+ "page_data_thumbnail, "
					+ "page_data"
					+ ")"
			        + " values (?, ?, ?, ?)";
		
			// create the statement
			preparedINSERTstatement = conn
	                .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			// execute the statement 
			preparedINSERTstatement.setLong(1, page.getPageNumber());
			preparedINSERTstatement.setLong(2, docId);
			preparedINSERTstatement.setBlob(3, new javax.sql.rowset.serial.SerialBlob(thumbnailInByte));
			preparedINSERTstatement.setBlob(4, new javax.sql.rowset.serial.SerialBlob(imageBlob));
			preparedINSERTstatement.executeUpdate();
			
			// get the id of the created 
			ResultSet rs = preparedINSERTstatement.getGeneratedKeys();
			if (rs.next()){
				returnId=rs.getLong(1);
			}
	
			// release the connection
			closeConnection(conn);
			
			// return the result
			page.setId(returnId);
			getLog().info("CREATE PAGE: " + page);
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return page;
		
	}
	
	
	
	
	/**
	 * Create a new document type for this matrix
	 * 
	 * @param docType - the document type to be added
	 * @return - the updated/created document type
	 * @throws Exception - if there was an issue
	 */
	public DocumentType createDocType(DocumentType docType) throws Exception {
		PreparedStatement preparedINSERTstatement;
		long returnId=0;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the query
		String insertQuery = " insert into document_type ("
				+ "name, "
				+ "value, "
				+ "color_hex_code, "
				+ "document_designation"
				+ ")"
		        + " values (?, ?, ?, ?)";
		
		// create the statement
		preparedINSERTstatement = conn
                .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
		// execute the statement 
		preparedINSERTstatement.setString(1, docType.getName());
		preparedINSERTstatement.setString(2, docType.getValue());
		preparedINSERTstatement.setString(3, docType.getHexColorCode());
		preparedINSERTstatement.setString(4, docType.getDocumentDesignation());
		preparedINSERTstatement.executeUpdate();
		
		// get the id of the created 
		ResultSet rs = preparedINSERTstatement.getGeneratedKeys();
		if (rs.next()){
			returnId=rs.getLong(1);
		}

		// release the connection
		closeConnection(conn);
		
		// return the result
		docType.setId(returnId);
		getLog().info("CREATE Doc Type: " + docType);
		return docType;
		
	}

	/**
	 * Get all the document pages for the given document
	 * 
	 * @param documentID - the document to fetch the pages for
	 * @return - the list of sorted (ordinal) pages for this document
	 * @throws Exception - if there was an issue
	 */
	private List<DocumentPage> fetchDocumentPages(long documentID) throws Exception {
		List<DocumentPage> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT id, page_number from document_page " 
                		+ "WHERE document_id=?");
		// execute the statement 
		preparedSELECTstatement.setLong(1, documentID);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractDocumentPagesFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
		
	}
	
	/**
	 * Translate SQL results into model data
	 * 
	 * @param resultSet - the result set with Document Page data
	 * @return - converted data
	 * @throws SQLException - if there was an issue with executing the given query
	 */
	private List<DocumentPage> extractDocumentPagesFromResult(ResultSet resultSet) throws SQLException {
		List<DocumentPage> documentPages = new ArrayList<DocumentPage>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            int pageNumber = resultSet.getInt("page_number");
      
            
            // Create a new Document
            DocumentPage documentPage = new DocumentPage();
            
            // populate the entity
            documentPage.setId(id);
            documentPage.setPageNumber(pageNumber);

            documentPages.add(documentPage);
        }
		
		return documentPages;
	}
	
	/**
	 * Translate SQL results into model data
	 * 
	 * @param resultSet - the result set with Document Type data
	 * @return - converted data
	 * @throws SQLException - if there was an issue with executing the given query
	 */
	private List<DocumentType> extractDocumentTypesFromResult(ResultSet resultSet) throws SQLException {
		List<DocumentType> documentTypes = new ArrayList<DocumentType>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            String name = resultSet.getString("name");
            String value = resultSet.getString("value");
            String typeColorCode = resultSet.getString("color_hex_code");
            String documentDesignation = resultSet.getString("document_designation");
            
      
            
            // Create a new Document
            DocumentType documentType = new DocumentType();
            
            // populate the entity
            documentType.setId(id);
            documentType.setName(name);
            documentType.setValue(value);
            documentType.setHexColorCode(typeColorCode);
            documentType.setDocumentDesignation(documentDesignation);

            documentTypes.add(documentType);
        }
		
		return documentTypes;
	}
	
	/**
	 * get all document notes for the given document
	 * 
	 * @param documentID - the id of the document for which to get the notes
	 * @return - list of sorted notes
	 * @throws Exception - if there was an issue.
	 */
	public List<NoteData> fetchDocumentNotes(long documentID) throws Exception {
		List<NoteData> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT timestamp AS note_timestamp, note_data, creator_user AS note_creator_user "
                		+ "FROM document_notes_data " 
                		+ "WHERE document_notes_data.parent_doc_id = ? ");
		// execute the statement 
		preparedSELECTstatement.setLong(1, documentID);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractDocumentNotesFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
		
	}
	
	/**
	 * get all tags for the given document
	 * 
	 * @param documentID - the id of the document for which to get the tags
	 * @return - list of sorted tags
	 * @throws Exception - if there was an issue.
	 */
	public List<TagData> fetchDocumentTags(long documentID) throws Exception {
		List<TagData> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT tag_data.id, tag_data.tag_text, tag_data.custom_tag_prefix, tag_data.custom "
                		+ "FROM tag_data " 
                		+ "INNER JOIN document_tag_data ON tag_data.id=document_tag_data.tag_data_id "
                		+ "WHERE document_tag_data.document_id = ? ");
		// execute the statement 
		preparedSELECTstatement.setLong(1, documentID);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractDocumentTagsFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
		
	}
	
	
	/**
	 * get all backup docuemnts for the given document
	 * 
	 * @param documentID - the id of the document for which to get the backup docs
	 * @return - list of sorted backup docs
	 * @throws Exception - if there was an issue.
	 */
	public List<Document> fetchDocumentAttachments(long documentID) throws Exception {
		List<Document> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT document.id, document.doc_status, document.username, document.creation_timestamp, document.sync_id, "
                		+ "document.document_type, document.organization_id, document.group_id, document.document_type_id "
                		+ "FROM document " 
                		+ "INNER JOIN document_attachment_data ON document.id=document_attachment_data.attached_doc_id "
                		+ "WHERE document_attachment_data.parent_doc_id = ? ");
		// execute the statement 
		preparedSELECTstatement.setLong(1, documentID);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractDocumentAttachmentsFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
		
	}
	
	/**
	 * get all document links for the given document
	 * 
	 * @param documentID - the id of the document for which to get the links
	 * @return - list of sorted links
	 * @throws Exception - if there was an issue.
	 */
	public List<Document> fetchDocumentLinks(long documentID) throws Exception {
		List<Document> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT d.id, d.doc_status, d.username, d.creation_timestamp, d.document_type, d.type_hex_color, "
                		+ "d.document_type_id, d.sync_id, d.organization_id, d.group_id, gd.name AS group_name, "
                		+ "gdt.name AS group_type_name, gdt.order_index "
						+ "FROM document d "
						+ "JOIN group_data gd ON gd.id = d.group_id "
						+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id " 
                		+ "INNER JOIN document_link_data ON d.id=document_link_data.linked_doc_id "
                		+ "WHERE document_link_data.parent_doc_id = ? ");
		// execute the statement 
		preparedSELECTstatement.setLong(1, documentID);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractDocumentLinksFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
		
	}
	/**
	 * get all document recipients for the given document
	 * 
	 * @param documentID - the id of the document for which to get the recipients
	 * @return - list of sorted recipients
	 * @throws Exception - if there was an issue.
	 */
	public List<User> fetchDocumentRecipients(long documentID) throws Exception {
		List<User> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT * "
						+ "FROM document_recipient_data "
                		+ "WHERE parent_doc_id = ? ");
		// execute the statement 
		preparedSELECTstatement.setLong(1, documentID);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractUsersFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
		
	}
	
	/**
	 * get all document recipients for the given document by its sync id
	 * 
	 * @param docSyncId - the id of the document for which to get the links
	 * @return - list of sorted links
	 * @throws Exception - if there was an issue.
	 */
	public List<Document> getAllLinkedDocsByDocSyncId(String docSyncId) throws Exception{
		List<Document> result=null;
		
		long docId = getDocIdBySyncId(docSyncId);
		result = fetchDocumentLinks(docId);
	
		// return the result
		return result;
	}
	
	/**
	 * get all document attachments for the given document by its sync id
	 * 
	 * @param docSyncId - the id of the document for which to get the attachments
	 * @return - list of sorted attachments
	 * @throws Exception - if there was an issue.
	 */
	public List<Document> getAllAttachedDocsByDocSyncId(String docSyncId) throws Exception{
		List<Document> result=null;
		
		long docId = getDocIdBySyncId(docSyncId);
		result = fetchDocumentAttachments(docId);
	
		// return the result
		return result;
	}
	
	/**
	 * get the sync id of a document by its id
	 * 
	 * @param docSyncId - the sync id to get the actual id with
	 * @return - the sync id of the document
	 * @throws Exception - if there was an issue.
	 */
	public long getDocIdBySyncId(String syncId) throws Exception {
		long documentId=0;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT id from document WHERE sync_id=?");
		preparedSELECTstatement.setString(1, syncId);
		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        while (resultSet.next()) {
			// extract the data
        	documentId = resultSet.getLong("id");
        }

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return documentId;
	}
	
	
	/**
	 * get the sync id of a document by its id
	 * 
	 * @param docSyncId - the sync id to get the actual id with
	 * @return - the sync id of the document
	 * @throws Exception - if there was an issue.
	 */
	public String getSyncIDByDocId(long docID) throws Exception {
		String  syncID=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT sync_id from document WHERE id=?");
		preparedSELECTstatement.setLong(1, docID);
		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        while (resultSet.next()) {
			// extract the data
        	syncID = resultSet.getString("sync_id");
        }

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return syncID;
	}
	
	/***************************************************************************
	 * Private helper methods
	 */
	
	/**
	 * Translate SQL results into model data
	 * 
	 * @param resultSet - the result set with Tag data
	 * @return - converted data
	 * @throws Exception - if there was an issue.
	 */
	private List<TagData> extractDocumentTagsFromResult(ResultSet resultSet) throws Exception {
		List<TagData> tags= new ArrayList<TagData>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            String tagText = resultSet.getString("tag_text");
            String tagCustomPrefix = resultSet.getString("custom_tag_prefix");
            boolean isCustom = resultSet.getBoolean("custom");
            
            
            // Create a new Document
            TagData tag = new TagData();
            
            
            // populate the Document entity
            tag.setText(tagText);
            if(isCustom){
            	tag.setText(tagCustomPrefix + " " + tagText);
            }else{
            	tag.setText(tagText);
            }
            tag.setId(id);
            tag.setCustomPrefix(tagCustomPrefix);
            tag.setCustom(isCustom);


            tags.add(tag);
        }
		
		return tags;
	}
	
	
	/**
	 * Translate SQL results into model data
	 * 
	 * @param resultSet - the result set with Tag data
	 * @return - converted data
	 * @throws Exception - if there was an issue.
	 */
	private List<DocumentPage> extractSparseDocumentPagesFromResult(ResultSet resultSet) throws Exception {
		List<DocumentPage> pages= new ArrayList<DocumentPage>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            int pageNumber = resultSet.getInt("page_number");
            
            
            // Create a new Document
            DocumentPage page = new DocumentPage();
            
            
            // populate the Page Sparsely entity
            page.setId(id);
            page.setPageNumber(pageNumber);

            pages.add(page);
        }
		//
		// sort by page number and collate
		
		// sort
		Collections.sort(pages, new PageComparator());
		// collate
		
		
		
		return pages;
	}
		
	
	
	/**
	 * Translate SQL results into model data
	 * 
	 * @param resultSet - the result set with Document Notes
	 * @return - converted data
	 * @throws Exception - if there was an issue.
	 */
	private List<NoteData> extractDocumentNotesFromResult(ResultSet resultSet) throws Exception {
		List<NoteData> notes= new ArrayList<NoteData>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			String  creationTimestamp = resultSet.getString("note_timestamp");
            String noteText = resultSet.getString("note_data");
            String noteOwner = resultSet.getString("note_creator_user");
            
            
            // Create a new Document
            NoteData note = new NoteData();
            
            
            // populate the Document entity
            note.setNote(noteText);
            note.setOwner(noteOwner);
            note.setCreationTimestamp(creationTimestamp);


            notes.add(note);
        }
		
		return notes;
	}
	
	/**
	 * Translate SQL results into model data
	 * 
	 * @param resultSet - the result set with Document Attachments
	 * @return - converted data
	 * @throws Exception - if there was an issue.
	 */
	private List<Document> extractDocumentAttachmentsFromResult(ResultSet resultSet) throws Exception {
		List<Document> attachments= new ArrayList<Document>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            String owner = resultSet.getString("username");
            String docType = resultSet.getString("document_type");
            long docTypeId = resultSet.getLong("document_type_id");
            long organizationId = resultSet.getLong("organization_id");
            long groupId = resultSet.getLong("group_id");
            String creationDate = resultSet.getString("creation_timestamp");
            
            
            // Create a new Document
            Document doc = new Document();
            
            
            // populate the Document entity
            doc.setOwner(owner);
            doc.setDocumentType(docType);
            // convert the data through proper format
            //
            if(creationDate.contains("/")){
            	// convert to older style
            	creationDate = DateUtility.formatFullDateStringToSimpleDateTimeString(creationDate);
            }
            doc.setCreationTimestamp(creationDate);
            doc.setId(id);
            doc.setOrganizationId(organizationId);
            doc.setGroupId(groupId);
            
            //
            // Doc Type
            DocumentType docTypeData = null;
            docTypeData = fetchDocumentTypeData(docTypeId);
            doc.setType(docTypeData);


            attachments.add(doc);
        }
		
		return attachments;
	}
	
	/**
	 * Translate SQL results into model data
	 * 
	 * @param resultSet - the result set with Users
	 * @return - converted data
	 * @throws Exception - if there was an issue.
	 */
	private List<User> extractUsersFromResult(ResultSet resultSet) throws Exception {
		List<User> toUsers= new ArrayList<User>();
		userService.init();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("parent_doc_id");
			long userId = resultSet.getLong("to_user_id");

            
            // Get user data
            User user = userService.getUserByUserId(userId);     
            
            toUsers.add(user);
        }
		
		return toUsers;
	}
	
	

	/**
	 * Translate SQL results into model data
	 * 
	 * @param resultSet - the result set with Document Links
	 * @return - converted data
	 * @throws Exception - if there was an issue.
	 */
	private List<Document> extractDocumentLinksFromResult(ResultSet resultSet) throws Exception {
		List<Document> links= new ArrayList<Document>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
            String owner = resultSet.getString("username");
            String docType = resultSet.getString("document_type");
            long organizationId = resultSet.getLong("organization_id");
            long groupId = resultSet.getLong("group_id");
            String syncId = resultSet.getString("sync_id");
            String groupName = resultSet.getString("group_name");
            String groupTypeName = resultSet.getString("group_type_name");
            int groupTypeOrderIndex = resultSet.getInt("order_index");
            String creationDate = resultSet.getString("creation_timestamp");
            long docTypeId = resultSet.getLong("document_type_id");
            
            // Create a new Document
            Document doc = new Document();
            
            
            // populate the Document entity
            doc.setOwner(owner);
            doc.setDocumentType(docType);
            // convert the data through proper format
            //
            if(creationDate.contains("/")){
            	// convert to older style
            	creationDate = DateUtility.formatFullDateStringToSimpleDateTimeString(creationDate);
            }
            doc.setCreationTimestamp(creationDate);
            doc.setId(id);
            doc.setOrganizationId(organizationId);
            doc.setGroupId(groupId);
            doc.setSyncID(syncId);
            doc.setGroupName(groupName);
            doc.setGroupTypeName(groupTypeName);
            doc.setGroupTypeOrderIndex(groupTypeOrderIndex);
            
            //
            // Doc Type
            DocumentType docTypeData = null;
            docTypeData = fetchDocumentTypeData(docTypeId);
            doc.setType(docTypeData);


            links.add(doc);
        }
		
		return links;
	}
	
	/**
	 * Update the profile image for the user
	 * @param profileImage - the image data
	 * @param username - the user for whom the update is done
	 * @throws PersistenceException
	 * 		- if there were any issues in processing this request
	 */
	public Document importPDFDocFile(InputStream importDocPDFStream, Document doc) throws PersistenceException {
		long importedDocID = 0;
		byte [] bDocImportPDF = new byte [0];
		Document importedNewwDoc = doc;
		
		PreparedStatement preparedINSERTstatement;
	    
		//
		// get the bytes of the file
	    try {
	          byte [] buffer = new byte[4096];
	          ByteArrayOutputStream outs = new ByteArrayOutputStream();
	          
	          int read = 0;
	          while ((read = importDocPDFStream.read(buffer)) != -1 ) {
	            outs.write(buffer, 0, read);
	          }
	          
	          importDocPDFStream.close();
	          outs.close();
	          bDocImportPDF = outs.toByteArray();
	          
	    } catch (Exception e) { 
	        e.printStackTrace();
	    }
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		//
		// Get the images out of the PDFand create a new document
		// List<DocumentPage> documentPages = convertToDocPages(bDocImportPDF);
		List<DocumentPage> documentPages = convertPDFToDocPages(bDocImportPDF);
		// importedNewwDoc.setCreationTimestamp(new Date());
		
		// add pages to the document
		importedNewwDoc.setPages(documentPages);
		
		//
		// create the document
		importedNewwDoc = create(importedNewwDoc);
		
		
		// create the query
		String insertQuery = " INSERT INTO document_import ("
						+ "doc_import_id, "
						+ "file_data "
						+ ")"
						+ " values (?, ?)";
		
		// create the statement
		try {
			preparedINSERTstatement = conn
			        .prepareStatement(insertQuery);
			
			// execute the statement
			preparedINSERTstatement.setLong(1, importedDocID);
			preparedINSERTstatement.setBlob(2, new javax.sql.rowset.serial.SerialBlob(bDocImportPDF));
			
			preparedINSERTstatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			

		// release the connection
		closeConnection(conn);
		
		getLog().info("Imported document for user " + doc.getOwner() + " with imported id: " + importedDocID);
		
		return importedNewwDoc;
		
	}
	
	/**
	 * Create a thumbnail for the given page image by scaling it down
	 * 
	 * @param pageData - the page to generate the thumbnail for
	 * @return - the generated images as bytes.
	 * @throws Exception - if there was an issue.
	 */
	public byte[] generateThumbnail(byte[] pageData) throws Exception{
		InputStream in = new ByteArrayInputStream(pageData);
		BufferedImage bImageFromConvert = ImageIO.read(in);
		
		//
		// Create a thumbnail of the image
		// <TODO>
		BufferedImage thumbnail = Thumbnails.of(bImageFromConvert)
		        .size(250, 167)
		        .asBufferedImage();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( thumbnail, "png", baos );
		baos.flush();
		byte[] thumbnailInByte = baos.toByteArray();
		baos.close();
		
		return thumbnailInByte;
		
	}
	
	/**
	 * Create a thumbnail for the given page image by scaling it down
	 * 
	 * @param pageData - the page to generate the thumbnail for
	 * @return - the generated images as bytes.
	 * @throws Exception - if there was an issue.
	 */
	public byte[] generateThumbnail(InputStream pageData) throws Exception{
		BufferedImage bImageFromConvert = ImageIO.read(pageData);
		
		//
		// Create a thumbnail of the image
		// <TODO>
		BufferedImage thumbnail = Thumbnails.of(bImageFromConvert)
		        .size(250, 167)
		        .asBufferedImage();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( thumbnail, "png", baos );
		baos.flush();
		byte[] thumbnailInByte = baos.toByteArray();
		baos.close();
		
		return thumbnailInByte;
		
	}
	
	/**
	 * Update the thumbnail for the page with the input thumbnail
	 * @param thumbnail - the thumbnail to substitute with
	 * @param pageId - the page for which the substitution will be done
	 * @throws Exception - if there was an issue.
	 */
	public void updatePageThumbnail(byte[] thumbnail, long pageId) throws Exception {
		// create the java mysql update preparedstatement
	    String query = "UPDATE document_page set page_data_thumbnail = ? WHERE id = ?";
	    
	    // get the connection
	 	Connection conn = openConnection();
	    
	    PreparedStatement preparedUPDATEstatement = conn.prepareStatement(query);
	    preparedUPDATEstatement.setLong(2, pageId);
	    preparedUPDATEstatement.setBlob(1, new javax.sql.rowset.serial.SerialBlob(thumbnail));

	    // execute the java preparedstatement
	    preparedUPDATEstatement.executeUpdate();
	      
	    conn.close();
		
	}
	
	/**
	 * Update the status for the given document
	 * 
	 * @param status - the new status
	 * @param docSessionId - the id of the document
	 * @param userName - the user making the change
	 * @return - the updated document
	 * @throws Exception - if there was an issue.
	 */
	public Document updateDocumentStatus(String status, String docSessionId, String userName) throws Exception {
		// create the java mysql update preparedstatement
	    String query = "UPDATE document set doc_status = ? WHERE id = ?";
	    long docID = getDocIdBySyncId(docSessionId); 
	    Document doc = null;
	    
	    // get the connection
	 	Connection conn = openConnection();
	    
	    PreparedStatement preparedUPDATEstatement = conn.prepareStatement(query);
	    preparedUPDATEstatement.setLong(2, docID);
	    preparedUPDATEstatement.setString(1, status);

	    // execute the java preparedstatement
	    preparedUPDATEstatement.executeUpdate();
	      
	    conn.close();
	    
		//
		// audit the request
		try {
			AuditAction action = null;
			if(status.equals(Document.STATUS_ACCEPTED)){
				action = AuditAction.DOCUMENT_ACCEPT;
			}
			if(status.equals(Document.STATUS_REJECTED)){
				action = AuditAction.DOCUMENT_REJECT;
			}
			if(status.equals(Document.STATUS_RESUBMITTED)){
				action = AuditAction.DOCUMENT_RESUBMIT;
			}
			if(status.equals(Document.STATUS_SUBMITTED)){
				action = AuditAction.DOCUMENT_SUBMIT;
			}
			if(status.equals(Document.STATUS_PENDING)){
				action = AuditAction.DOCUMENT_PENDING;
			}
			// get the document entry
			doc = getDocumentById(docID).get(0);
			auditRequest(doc, AuditUserType.USER, action, userName);
		}catch (Exception e) {
			getLog().error("Error Auditing Document-Creation: " + e);
		}
		
		return doc;
		
	}
	
	/**
	 * Rotate the page image data.
	 * 
	 * @param pageData - the page to rotate
	 * @return - the rotated page image (if it was needed)
	 */
	public byte[] rotatePageIfNeded(byte[] pageData) {
		
		int scaleFactor = 1728;
		/**
		 * Represents the Logger used to perform logging.
		 */
		final Logger log = Logger.getLogger(getClass().getName());
		byte[] thumbnailInByte = null;
		
		
		try{
			InputStream in = new ByteArrayInputStream(pageData);
			BufferedImage bImageFromConvert = ImageIO.read(in);
			int width = bImageFromConvert.getWidth();
			int height = bImageFromConvert.getHeight();
			float percentage=0f;
			
			
			System.out.println("ROTATION DImensions " + width + " " +height );
			log.info("[ROTATION DImensions] " + width + " " +height );
			if(width < height){
				System.out.println("NO ROTATION");
				// check for scaling
				if(height > scaleFactor){
					percentage = (float)scaleFactor/(float)height;
					
					//
					// Create a thumbnail of the image
					// <TODO>
					BufferedImage thumbnail = Thumbnails.of(bImageFromConvert)
							.scale(percentage)
					        .asBufferedImage();
					
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write( thumbnail, "png", baos );
					baos.flush();
					thumbnailInByte = baos.toByteArray();
					baos.close();
					return thumbnailInByte;
				}
				return pageData;
			}
			log.info("[NEED FOR ROTATION] ");
			
			
			//
			// Create a thumbnail of the image
			// <TODO>
			if(width > scaleFactor){
				percentage = (float)scaleFactor/(float)width;
			}else{
				percentage = 1.0f;
			}
			BufferedImage thumbnail = Thumbnails.of(bImageFromConvert)
					.scale(percentage)
			        .rotate(90)
			        .asBufferedImage();
			
			log.info("[AFTER ROTATION DImensions] " + thumbnail.getWidth() + " " +thumbnail.getHeight() );
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write( thumbnail, "png", baos );
			baos.flush();
			thumbnailInByte = baos.toByteArray();
			baos.close();
		}catch(Exception e){
			log.info("[ERROR ROTATINMG] " + e);
			log.info("[ERROR ROTATINMG]" + e.getMessage());
		}
		
		return thumbnailInByte;
	}
	
	/**
	 * Get all of the potential documents that could be linked for the given user
	 * 
	 * @param user - the user requesting the docs
	 * @param docType - the document type restriction
	 * @param customTag - any custom tag that must be present
	 * @param syncId - the sync id of the document to link to
	 * @return a list of documents that can be linked.
	 * @throws Exception - if there was an issue.
	 */
	public List<Document> getAllDocsToLink(User user, String docType,String customTag, String syncId) throws Exception{
		List<Document> allDocuments = getAllDocuments(user, docType, false);
		List<Document> linkedDocuments = null; 
		// extract only the documents that the user can access
		
		Iterator<Document> itr = allDocuments.iterator();
	      
	      while(itr.hasNext()) {
	         Document element = itr.next();
	         // if the user is the owner disregard this
	         if(element.getOwner().equals(user.getCredentials().getUsername())){
	        	 itr.remove();
		     // remove any documents whose status makes them not available
	         }else if(element.getStatus().equals(Document.STATUS_DRAFT)
	        		 	|| element.getStatus().equals(Document.STATUS_PENDING)
	        		 	|| element.getStatus().equals(Document.STATUS_REJECTED)
	        		 	|| element.getStatus().equals(Document.STATUS_SUBMITTED)
	        		 	|| element.getStatus().equals(Document.STATUS_RESUBMITTED)){
	        	 itr.remove();
	         // remove any element where this user is not a recipient
	         }else if(!element.getToRecipients().contains(user)){
	        	 // but first check if this document has the tag we are asked about
	        	 if(!customTag.isEmpty() && customTag.contains(CUSTOM_TAG_PREFIX)){
	        		 // check if this document has the tag
	        		 if(!containsTag(element, customTag)){
	        			 itr.remove();
	        		 }else{ // 
	        			 
	        		 }
	        	 }
	         }
	      }
	      //
	      // check if documents that are already linked are properly synced
	      
	      //
	      // try other docs that might have been added 
	  	  if(!syncId.isEmpty()){
	  		linkedDocuments = getAllLinkedDocsByDocSyncId(syncId);
	  	  }
	  	  if(linkedDocuments != null && linkedDocuments.size() > 0){
	  		  for(int i=0; i<linkedDocuments.size(); i++){
	  			if(!allDocuments.contains(linkedDocuments.get(i))){
	  				// remove the link from the back-end
	  				allDocuments.add(linkedDocuments.get(i));
	  			}
	  		  }
	  	  }
	  	  
	  	  return allDocuments;
	}
	
	/**
	 * Get all of the potential documents that could be linked for the given user
	 * 
	 * @param user - the user requesting the docs
	 * @param docType - the document type restriction
	 * @param customTag - any custom tag that must be present
	 * @param syncId - the sync id of the document to link to
	 * @return a list of documents that can be linked.
	 * @throws Exception - if there was an issue.
	 */
	public List<Document> getAllTaggedDocs(User user, String docType,String customTag) throws Exception{
		List<Document> allDocuments = getAllDocuments(user, docType, true);
		// extract only the documents that the user can access
		
		Iterator<Document> itr = allDocuments.iterator();
	      
	      while(itr.hasNext()) {
	         Document element = itr.next();
	         // if the user is the owner disregard this
	         if(element.getOwner().equals(user.getCredentials().getUsername())){
	        	 itr.remove();
		     // remove any documents whose status makes them not available
	         }else if(element.getStatus().equals(Document.STATUS_DRAFT)
	        		 	|| element.getStatus().equals(Document.STATUS_PENDING)
	        		 	|| element.getStatus().equals(Document.STATUS_REJECTED)
	        		 	|| element.getStatus().equals(Document.STATUS_SUBMITTED)
	        		 	|| element.getStatus().equals(Document.STATUS_RESUBMITTED)){
	        	 itr.remove();
	         // remove any element where this user is not a recipient
	         }else if(!customTag.isEmpty()){
        		 // check if this document has the tag
        		 if(!containsSearchTag(element, customTag, false)){
        			 itr.remove();
        		 }else{ 
        			 // Nothing to do
        		 }
	         }
	      }
	  	  
	  	  return allDocuments;
	}
	
	/**
	 * Get all of the potential documents that could be attached for the given user
	 * 
	 * @param user - the user requesting the docs
	 * @param docType - the document type restriction
	 * @param customTag - any custom tag that must be present
	 * @param syncId - the sync id of the document to attach to
	 * @return a list of documents that can be attached.
	 * @throws Exception - if there was an issue.
	 */
	public List<Document> getAllDocsToAttach(User user, String docType,String customTag, String syncId) throws Exception{
		List<Document> allDocuments = getAllDocuments(user, docType, false);
		// extract only the documents that the user can access
		
		Iterator<Document> itr = allDocuments.iterator();
	      
	      while(itr.hasNext()) {
	         Document element = itr.next();
	         // if the user is NOT the owner disregard this
	         if(!element.getOwner().equals(user.getCredentials().getUsername())){
	        	 itr.remove();
		     // remove any documents whose status makes them not available
	         }else if(!element.getType().getDocumentDesignation().equals(DocumentType.DESIGNATION_PROFILE)){
	        	 // but first check if this document has the tag we are asked about
	        	 itr.remove();
	         }
	      }
	        
	  	  return allDocuments;
	}	
	
	public PDDocument generateTracePDFDocument( long docId)  throws Exception {
		PDDocument pdfTraceDocument = null;
		List<Document> allDocuments=null;
		String numberingFormat = "Page {0}";
		int offset_X = 60;
		int offset_Y = 18;
		int page_counter = 1;
		
		
		//
		// get all documents for the trace
		allDocuments = getRecursiveDocumentById(docId);
		
		pdfTraceDocument = new PDDocument();
		
		//
		// create the trace document
		for(int i =0; i<allDocuments.size(); i++){
			
			// create the pages for each trace document
			for(int page =0; page < allDocuments.get(i).getPages().size(); page++){
				long pageId = allDocuments.get(i).getPages().get(page).getId();
				//PDPage newPage = new PDPage();
				
				//pdfTraceDocument.addPage(newPage);
				
				//
				// get the page as a straem
				//PDImageXObject pdImage = PDImageXObject.createFromByteArray(pdfTraceDocument, IOUtils.toByteArray(getDocumentPage(pageId)), null);
				//
				// prepare the image for addition
				//PDPageContentStream contentStream = new PDPageContentStream(pdfTraceDocument, newPage);
				//
				// Add the page
				//contentStream.drawImage(pdImage, 425, 675);
				
				PDPage myPage = new PDPage();
				pdfTraceDocument.addPage(myPage);

	            String imgFileName = "C:/Users/user/Desktop/JAMES/OCR-POC.png";
	            PDImageXObject pdImage = PDImageXObject.createFromByteArray(pdfTraceDocument, IOUtils.toByteArray(getDocumentPage(pageId)), null);
	            		//PDImageXObject.createFromFile(imgFileName, pdfTraceDocument);
	            
	            int iw = 425; // pdImage.getWidth();
	            int ih = 675; //pdImage.getHeight();
	            
	            float offsetWidth = (myPage.getMediaBox().getWidth() - iw)/2; 
	            float offsetHeight = 20f;
	            

	            try (PDPageContentStream contentStream = new PDPageContentStream(pdfTraceDocument, myPage)) {
	            	
	            	Table myTable = Table.builder()
	                        .addColumnsOfWidth(200, 200, 150)
	                        .addRow(Row.builder()
	                                .add(TextCell.builder().text(allDocuments.get(i).getType().getValue()).borderWidth(1).horizontalAlignment(HorizontalAlignment.CENTER).backgroundColor(Color.LIGHT_GRAY).build())
	                                .add(TextCell.builder().text(allDocuments.get(i).getGroupName()).borderWidth(1).horizontalAlignment(HorizontalAlignment.CENTER).backgroundColor(Color.LIGHT_GRAY).build())
	                                .add(TextCell.builder().text(allDocuments.get(i).getOwner()).borderWidth(1).horizontalAlignment(HorizontalAlignment.CENTER).backgroundColor(Color.LIGHT_GRAY).build())
	                                .build())
	                        .addRow(Row.builder()
	                                .add(TextCell.builder().text(allDocuments.get(i).getStatus()).borderWidth(1).horizontalAlignment(HorizontalAlignment.CENTER).textColor(Color.RED).build())
	                                .add(TextCell.builder().text(allDocuments.get(i).getCreationTimestamp()).borderWidth(1).build())
	                                .add(TextCell.builder().text("Page " + (page+1) + " of " + allDocuments.get(i).getPages().size()).borderWidth(1).horizontalAlignment(HorizontalAlignment.RIGHT).backgroundColor(Color.GRAY).build())
	                                .build())
	                        .build();

	                // Set up the drawer
	                TableDrawer tableDrawer = TableDrawer.builder()
	                        .contentStream(contentStream)
	                        .startX(20f)
	                        .startY(myPage.getMediaBox().getUpperRightY() - 20f)
	                        .table(myTable)
	                        .build();

	                // And go for it!
	                tableDrawer.draw();
	                
	                contentStream.drawImage(pdImage, offsetWidth, offsetHeight, iw, ih);
	                
	                contentStream.beginText();
	                contentStream.setFont(PDType1Font.TIMES_ITALIC, 10);
	                PDRectangle pageSize = myPage.getMediaBox();
	                float x = pageSize.getLowerLeftX();
	                float y = pageSize.getLowerLeftY();
	                contentStream.newLineAtOffset(x+ pageSize.getWidth()-offset_X, y+offset_Y);
	                String text = MessageFormat.format(numberingFormat,page_counter);
	                contentStream.showText(text);
	                contentStream.endText();
	                contentStream.close();
	                ++page_counter;
	            }
				
				
				//contentStream.close();
			}
		}
		
		return pdfTraceDocument;

	}
	
	/**
	 * Fetches the document type information by id
	 * 
	 * @param docTypeId - the id to fetch the data with
	 * @return - the specific document type
	 * @throws Exception - if there was an issue.
	 */
	private DocumentType fetchDocumentTypeData(long docTypeId) throws Exception {
		DocumentType result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// look up the user by name which should be unique
		//
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT * from document_type " 
                		+ "WHERE id=?");
		// execute the statement 
		preparedSELECTstatement.setLong(1, docTypeId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractDocumentTypesFromResult(resultSet).get(0);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
		
	}
	
	/**
	 * Check if the user has permissions for this document
	 * 
	 * @param user - the user being tested
	 * @param doc - the document being tested against
	 * @return - true if the user has permissions for this doc; false if they do not
	 */
	private boolean hasPermissions(User user, Document doc, boolean override){
		boolean result = false;
		
		//
		// initialize
		groupService.init();
		Role role = user.getRoles().get(0);
		List<GroupType> allGroupTypes = null;
		/**
		try {
			allGroupTypes = groupService.getAllGroupTypes();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		
		// process
		if(role.getValue().equals("Admin")){
			return true;
		}
		if(role.getValue().equals("Super Admin")){
			return true;
		}
		if(role.getValue().equals("Auditor")){
			return true;
		}
		
		if(override){
			return true;
		}
		
		if(doc.getOwner().equals(user.getCredentials().getUsername())){
			return true;
		}
		
		if(doc.getToRecipients().contains(user)){
			return true;
		}
		
		
		
		
		//
		// Check for 1-up and 1-downp number
		/**
		long stageNumForUser = user.getUserGroups().get(0).getGroupType().getId();
		int groupTypeDoc = 0;
		// get the document group
		for(int i=0; i < allGroupTypes.size(); i++){
			if(doc.getGroupTypeName().equals(allGroupTypes.get(i).getName())){
				groupTypeDoc = allGroupTypes.get(i).getOrderIndex();
				break;
			}
		}

		//
		// test
		if((stageNumForUser == groupTypeDoc)
			|| (stageNumForUser - 1 == groupTypeDoc)
			|| (stageNumForUser + 1 == groupTypeDoc)){
			return true;
		}
		
		*/
		
		return result;
	}
	
	/**
	 * Create an audit record for the given request
	 * 
	 * @param newDocument - the document being created
	 * @param userType - the user type
	 * @param action - the gesture being audited
	 * @param userName - the name of the user who initiated the gesture
	 * @return - The created audit entity
	 * @throws Exception - if there were any issues 
	 */
	private AuditEntity auditRequest(Document newDocument, AuditUserType userType, AuditAction action, String userName) throws Exception {
		AuditEntity newAuditEntity = new AuditEntity();
		auditService.init();
		
		// actor name
		newAuditEntity.setActor(userService.getUserByName(userName));
		// user type
		newAuditEntity.setUserType(userType);
		// action
		newAuditEntity.setAction(action);
		// item type
		newAuditEntity.setItemType("Document");
		// item id
		newAuditEntity.setItemId(newDocument.getSyncID());
		// creation time stamp
		newAuditEntity.setTimestamp(DateUtility.simpleDateFormat(DateUtility.getCurrentDateTime(), DateUtility.FORMAT_DATE_AND_TIME));
		
		// execute
		newAuditEntity = auditService.create(newAuditEntity);
		
		// return results
		return newAuditEntity;
	}
	
	/**
	 * This will extract all the PDF doc pages as images to be inserted 
	 * into Document instance @see Document
	 * 
	 * @param bDocImportPDF - binary array for the PDF document content
	 * @return - a list of document pages or empty if none found.
	 */
	private List<DocumentPage> convertToDocPages(byte [] bDocImportPDF){
		List<DocumentPage> result = new ArrayList<DocumentPage>();
		
		//
		// Extract the images from the doc
		try (final PDDocument document = PDDocument.load(bDocImportPDF)){

            PDPageTree list = document.getPages();
            for (PDPage page : list) {
                PDResources pdResources = page.getResources();
                int i = 1;
                for (COSName name : pdResources.getXObjectNames()) {
                    PDXObject o = pdResources.getXObject(name);
                    if (o instanceof PDImageXObject) {
                        PDImageXObject image = (PDImageXObject)o;
                        
            			ByteArrayOutputStream baos = new ByteArrayOutputStream();
            			ImageIO.write( image.getImage(), "png", baos );
            			baos.flush();
            			byte[] profileImageBytes = baos.toByteArray();
            			baos.close();
            			// create a new page
            			DocumentPage documentPage = new DocumentPage();
            			documentPage.setPageData(profileImageBytes);
            			documentPage.setPageNumber(i-1);
            			result.add(documentPage);

                        i++;
                    }
                }
            }

        } catch (IOException e){
            System.err.println("Exception while trying to create pdf document - " + e);
        }
		
		return result;
	}
	
	/**
	 * This will extract all the PDF doc pages as images to be inserted 
	 * into Document instance @see Document
	 * 
	 * @param bDocImportPDF - binary array for the PDF document content
	 * @return - a list of document pages or empty if none found.
	 */
	private List<DocumentPage> convertPDFToDocPages(byte [] bDocImportPDF){
		List<DocumentPage> result = new ArrayList<DocumentPage>();
		
		//
		// Extract the images from the doc
		try (final PDDocument document = PDDocument.load(bDocImportPDF)){

			PDFRenderer pdfRenderer = new PDFRenderer(document);
			int pageCounter = 0;
			for (PDPage page : document.getPages()){
			    // note that the page number parameter is zero based
			    BufferedImage bim = pdfRenderer.renderImageWithDPI(pageCounter, 150, ImageType.RGB);
			    
			    ByteArrayOutputStream baos = new ByteArrayOutputStream();

			    // suffix in filename will be used as the file format
			    ImageIOUtil.writeImage(bim, "jpg", baos, 150);
			    
			    baos.flush();
				byte[] pdfPageBytes = baos.toByteArray();
				baos.close();
				
    			// create a new page
    			DocumentPage documentPage = new DocumentPage();
    			documentPage.setPageData(pdfPageBytes);
    			documentPage.setPageNumber(pageCounter++);
    			result.add(documentPage);
				
			}
			document.close();

        } catch (IOException e){
            System.err.println("Exception while trying to create pdf document - " + e);
        }
		
		return result;
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
	
	/**
	 * check if the document is tagged
	 * @param doc - the document in question
	 * @param tag - the specific tag 
	 * @return - true if the document is tagged and false if it is not
	 */
	private boolean containsSearchTag(Document doc, String tag, boolean partialSearch){
		boolean result = false;
		
		for(int i =0; i<doc.getTags().size(); i++){
			// strip the tag 
			String tempTag = doc.getTags().get(i).getText().replace(CUSTOM_TAG_PREFIX, "");
			if(partialSearch){
				if(tempTag.indexOf(tag) != -1){
					return true;
				}
			}else{
				if(tempTag.equals(tag)){
					return true;
				}
			}
		}
		return result;
		
	}
	
	private class PageComparator implements Comparator<DocumentPage> {
	    @Override
	    public int compare(DocumentPage a, DocumentPage b) {
	        return a.getPageNumber() < b.getPageNumber() ? -1 : a.getPageNumber() == b.getPageNumber() ? 0 : 1;
	    }
	}
	
	

}
