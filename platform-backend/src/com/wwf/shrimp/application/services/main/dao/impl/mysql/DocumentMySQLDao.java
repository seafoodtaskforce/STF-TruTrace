package com.wwf.shrimp.application.services.main.dao.impl.mysql;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
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
import org.vandeseer.easytable.structure.Table.TableBuilder;
import org.vandeseer.easytable.structure.cell.ImageCell;
import org.vandeseer.easytable.structure.cell.ImageCell.ImageCellBuilder;
import org.vandeseer.easytable.structure.cell.TextCell;
import static org.vandeseer.easytable.settings.VerticalAlignment.MIDDLE;
import static org.vandeseer.easytable.settings.HorizontalAlignment.CENTER;

import com.mysql.cj.api.jdbc.Statement;
import com.wwf.shrimp.application.exceptions.EntityNotFoundException;
import com.wwf.shrimp.application.exceptions.PersistenceException;
import com.wwf.shrimp.application.exceptions.ServiceManagementException;
import com.wwf.shrimp.application.models.AuditAction;
import com.wwf.shrimp.application.models.AuditEntity;
import com.wwf.shrimp.application.models.AuditUserType;
import com.wwf.shrimp.application.models.Document;
import com.wwf.shrimp.application.models.DocumentOCRMatchingData;
import com.wwf.shrimp.application.models.DocumentPage;
import com.wwf.shrimp.application.models.DocumentType;
import com.wwf.shrimp.application.models.DynamicFieldData;
import com.wwf.shrimp.application.models.DynamicFieldDefinition;
import com.wwf.shrimp.application.models.GroupType;
import com.wwf.shrimp.application.models.IdentifiableEntity;
import com.wwf.shrimp.application.models.NoteData;
import com.wwf.shrimp.application.models.Organization;
import com.wwf.shrimp.application.models.Role;
import com.wwf.shrimp.application.models.Screening;
import com.wwf.shrimp.application.models.TagData;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.search.AuditSearchCriteria;
import com.wwf.shrimp.application.models.search.DocumentSearchCriteria;
import com.wwf.shrimp.application.models.search.OrganizationSearchCriteria;
import com.wwf.shrimp.application.models.search.TagSearchCriteria;
import com.wwf.shrimp.application.models.search.UserSearchCriteria;
import com.wwf.shrimp.application.services.main.ConfigurationService;
import com.wwf.shrimp.application.services.main.impl.PropertyConfigurationService;
import com.wwf.shrimp.application.utils.DataUtils;
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
	private ConfigurationService configService = new PropertyConfigurationService();
	
	public  static final String CUSTOM_TAG_PREFIX = "CUSTOM: ";
	public static final int PDF_GENERATION_TABLE_FONT_SIZE = 8;

	
	
	/**
	 * Get all the documents in the data base.
	 * @return -  a list of documents that were found.
	 * @throws Exception - if there was an issue
	 */
	public List<Document> getAllDocuments_v2(User user, String docType, boolean permissionsOverride) throws Exception{
		List<Document> result=new ArrayList<Document>();
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		String userName = null;
		long organizationId = 1;
		getLog().debug("FETCH ALL: <getAllDocuments_v2> doctype: " + docType );
		
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
		preparedSELECTstatement = generateFetchAllDocsPreparedStatement(conn, user,docType, organizationId);
		

		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        getLog().debug("FETCH ALL: <getAllDocuments> <Service Create Statement DONE>");
        
        //
        // process the result
        getLog().debug("FETCH ALL: <getAllDocuments> <Service Extracting Data>");
        result = extractDocumentFromResult(resultSet, user,docType, permissionsOverride);
        getLog().debug("FETCH ALL: <getAllDocuments> <Service Extracting Data DONE>");

		// release the connection
		closeConnection(conn);
		
		getLog().debug("FETCH ALL: <getAllDocuments> <Service Check READ>");
		//
		// Check if the user has read the document
		//for(int i=0; i<result.size(); i++){
		//	boolean wasRead = wasDocumentRead(result.get(i).getSyncID(),userName );
		//	result.get(i).setCurrentUserRead(wasRead);
		//}
		getLog().debug("FETCH ALL: <getAllDocuments> <Service Check READ DONE>");
		
		//////////////////////////////////////////////////////////////////////////////
		// check any permissions issues
		
		getLog().debug("FETCH ALL: <getAllDocuments> <Service Check Permissions>");
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
		
		//
		// Check for any locked documents
		ListIterator<Document> iter = result.listIterator();
		ArrayList<Document> allLinkedDocs = new ArrayList<>();
		
		// create the list of all linked docs
		while(iter.hasNext()){
			Document iterDoc = iter.next();
			//if(iterDoc.getAttachedDocuments().size() > 0){
			//	allLinkedDocs.addAll(iterDoc.getAttachedDocuments());
			//}
			if(iterDoc.getLinkedDocuments().size() > 0){
				allLinkedDocs.addAll(iterDoc.getAttachedDocuments());
			}
		}
		// remove duplicates
        ArrayList<Document> allLinkedDocsSparse = DataUtils.removeDuplicates(allLinkedDocs);
        iter = result.listIterator();
        //
        // check if the document is part of a trace (i.e. is being linked to any other document
        // or has any documents linked to it)
        while(iter.hasNext()){
			Document iterDoc = iter.next();
			//
			// is it part of any trace?
			if(allLinkedDocsSparse.contains(iterDoc) && iterDoc.getStatus().equals(Document.STATUS_ACCEPTED)){
				iterDoc.setLocked(true);
			}
			//
			// is any other document connected to it?
			//if((!iterDoc.getAttachedDocuments().isEmpty() || !iterDoc.getLinkedDocuments().isEmpty())
			//if(!iterDoc.getLinkedDocuments().isEmpty()	&& iterDoc.getStatus().equals(Document.STATUS_ACCEPTED)){
			//	iterDoc.setLocked(true);
			//}
			
		}
		getLog().debug("FETCH ALL: <Service Check Permissions Done>");
		getLog().debug("FETCH ALL: <Service Out>");		
		
		//
		//
		// Final post processing
		if(user.getRoles().get(0).getName().equals(Role.ROLE_NAME_MATRIX_ADMIN)) {
			//
			// extract all trace documents
			ListIterator<Document> traceDocsIter = result.listIterator();
			List<Document> tempTraceDocuments = new ArrayList<Document>();
			List<Document> allDocumentsWithoutDuplicates = new ArrayList<Document>();
			List<Document> traceDocuments = new ArrayList<Document>();
			
			// create the list of all linked docs
			while(traceDocsIter.hasNext()){
				Document iterDoc = traceDocsIter.next();
				//if(iterDoc.getAttachedDocuments().size() > 0){
				//	allLinkedDocs.addAll(iterDoc.getAttachedDocuments());
				//}
				if(iterDoc.getLinkedDocuments().size() > 0){
					tempTraceDocuments = getRecursiveDocumentById(iterDoc.getId());
					if(tempTraceDocuments.size() > 0){
						traceDocuments.addAll(tempTraceDocuments);
					}
				}
			}
			
			//
			// consolidate all documents
			result.addAll(traceDocuments);
			//
			// remove dupliucates
			allDocumentsWithoutDuplicates = new ArrayList<>(
				      new HashSet<>(result));
			
			result = allDocumentsWithoutDuplicates;
		}

		// return the result
		return result;
				
	}

	
	
	/**
	 * Get all the documents in the data base.
	 * @return -  a list of documents that were found.
	 * @throws Exception - if there was an issue
	 */
	public List<Document> getAllDocuments(User user, String docType, boolean permissionsOverride) throws Exception{
		List<Document> result=new ArrayList<Document>();
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		String userName = null;
		long organizationId = 1;
		getLog().debug("FETCH ALL: <getAllDocuments> doctype: " + docType );
		
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
		
		
		if(!docType.isEmpty()){
			getLog().debug("FETCH ALL: <getAllDocuments> with doctype");
			preparedSELECTstatement = conn
					.prepareStatement("SELECT d.id, d.doc_status, d.username, d.userid, d.creation_timestamp, d.document_type, d.updation_timestamp, "
							+ "d.updation_server_timestamp, d.document_type_id, d.sync_id, d.organization_id, d.group_id, d.type_hex_color, "
							+ "gd.name AS group_name, gd.gps_location AS group_gps_location, gd.business_id_number, "
							+ "gdt.name AS group_type_name, gdt.order_index, gdt.value AS group_type_value, dt.name, dt.value, "
							+ "dt.color_hex_code, dt.document_designation, dt.delete_flag, dld.gps_location "
							+ "FROM document d "
							+ "JOIN document_type dt ON d.document_type_id = dt.id "
							+ "JOIN group_data gd ON gd.id = d.group_id "
							+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id "
							+ "LEFT JOIN document_location_data dld ON dld.document_sync_id = d.sync_id "
							+ "WHERE organization_id = ? AND dt.document_designation = ? ");
			
			// execute the statement 
			preparedSELECTstatement.setLong(1, organizationId);
			preparedSELECTstatement.setString(2, docType);
		}else{
			getLog().debug("FETCH ALL: <getAllDocuments> *no* doctype");
			preparedSELECTstatement = conn
					.prepareStatement("SELECT d.id, d.doc_status, d.username, d.userid, d.creation_timestamp, d.document_type, d.updation_timestamp, "
							+ "d.updation_server_timestamp, d.document_type_id, d.sync_id, d.organization_id, d.group_id, d.type_hex_color, "
							+ "gd.name AS group_name, gd.gps_location AS group_gps_location, gd.business_id_number, "
							+ "gdt.name AS group_type_name, gdt.value AS group_type_value, gdt.order_index, "
							+ "dt.name, dt.value, dt.color_hex_code, dt.document_designation, dt.delete_flag, dld.gps_location "
							+ "FROM document d "
							+ "JOIN document_type dt ON d.document_type_id = dt.id "
							+ "JOIN group_data gd ON gd.id = d.group_id "
							+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id "
							+ "LEFT JOIN document_location_data dld ON dld.document_sync_id = d.sync_id "
							+ "WHERE organization_id = ?");
			// execute the statement 
			preparedSELECTstatement.setLong(1, organizationId);
		}
		

		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        getLog().debug("FETCH ALL: <getAllDocuments> <Service Create Statement DONE>");
        
        //
        // process the result
        getLog().debug("FETCH ALL: <getAllDocuments> <Service Extracting Data>");
        result = extractDocumentFromResult(resultSet, user,docType, permissionsOverride);
        getLog().debug("FETCH ALL: <getAllDocuments> <Service Extracting Data DONE>");

		// release the connection
		closeConnection(conn);
		
		getLog().debug("FETCH ALL: <getAllDocuments> <Service Check READ>");
		//
		// Check if the user has read the document
		//for(int i=0; i<result.size(); i++){
		//	boolean wasRead = wasDocumentRead(result.get(i).getSyncID(),userName );
		//	result.get(i).setCurrentUserRead(wasRead);
		//}
		getLog().debug("FETCH ALL: <getAllDocuments> <Service Check READ DONE>");
		
		//////////////////////////////////////////////////////////////////////////////
		// check any permissions issues
		
		getLog().debug("FETCH ALL: <getAllDocuments> <Service Check Permissions>");
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
		
		//
		// Check for any locked documents
		ListIterator<Document> iter = result.listIterator();
		ArrayList<Document> allLinkedDocs = new ArrayList<>();
		
		// create the list of all linked docs
		while(iter.hasNext()){
			Document iterDoc = iter.next();
			//if(iterDoc.getAttachedDocuments().size() > 0){
			//	allLinkedDocs.addAll(iterDoc.getAttachedDocuments());
			//}
			if(iterDoc.getLinkedDocuments().size() > 0){
				allLinkedDocs.addAll(iterDoc.getAttachedDocuments());
			}
		}
		// remove duplicates
        ArrayList<Document> allLinkedDocsSparse = DataUtils.removeDuplicates(allLinkedDocs);
        iter = result.listIterator();
        //
        // check if the document is part of a trace (i.e. is being linked to any other document
        // or has any documents linked to it)
        while(iter.hasNext()){
			Document iterDoc = iter.next();
			//
			// is it part of any trace?
			if(allLinkedDocsSparse.contains(iterDoc) && iterDoc.getStatus().equals(Document.STATUS_ACCEPTED)){
				iterDoc.setLocked(true);
			}
			//
			// is any other document connected to it?
			//if((!iterDoc.getAttachedDocuments().isEmpty() || !iterDoc.getLinkedDocuments().isEmpty())
			//if(!iterDoc.getLinkedDocuments().isEmpty()	&& iterDoc.getStatus().equals(Document.STATUS_ACCEPTED)){
			//	iterDoc.setLocked(true);
			//}
			
		}
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
				.prepareStatement("SELECT d.id, d.doc_status, d.username, d.userid, d.creation_timestamp, d.document_type, d.type_hex_color, "
									+ "d.document_type_id, d.sync_id, d.organization_id, d.group_id, d.updation_timestamp, d.updation_server_timestamp, "
									+ "gd.name AS group_name, gd.gps_location AS group_gps_location, gd.business_id_number, "
									+ "gdt.name AS group_type_name, gdt.value AS group_type_value, gdt.order_index, "
									+ "dt.name, dt.value, dt.color_hex_code, dt.document_designation, dt.delete_flag, dld.gps_location "
									+ "FROM document d "
									+ "JOIN document_type dt ON d.document_type_id = dt.id "
									+ "JOIN group_data gd ON gd.id = d.group_id "
									+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id "
									+ "LEFT JOIN document_location_data dld ON dld.document_sync_id = d.sync_id "
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
					.prepareStatement("SELECT d.id, d.doc_status, d.username, d.userid, d.creation_timestamp, d.document_type, d.type_hex_color, "
										+ "d.document_type_id, d.sync_id, d.organization_id, d.group_id, d.updation_timestamp, d.updation_server_timestamp, "
										+ "gd.name AS group_name, gd.gps_location AS group_gps_location, gd.business_id_number, "
										+ "gdt.name AS group_type_name, gdt.value AS group_type_value, gdt.order_index, "
										+ "dt.name, dt.value, dt.color_hex_code, dt.document_designation, dt.delete_flag, dld.gps_location "
										+ "FROM document d "
										+ "JOIN document_type dt ON d.document_type_id = dt.id "
										+ "JOIN group_data gd ON gd.id = d.group_id "
										+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id "
										+ "LEFT JOIN document_location_data dld ON dld.document_sync_id = d.sync_id "
										+ "WHERE d.id = ? "
										+ "Group By d.id");
			
			
			// execute the statement 
			preparedSELECTstatement.setLong(1, idToProcess);
	        resultSet = preparedSELECTstatement.executeQuery();
	        
	        //
	        // process the result
	        //
	        // NOTE: this grabs all the documents for the trace
	        //
	        resultDocument = extractDocumentFromResult(resultSet, null, null, false).get(0);
	        //
	        // ensure uniqueness of the document
	        if(!seenList.contains(resultDocument)){
	        	seenList.add(resultDocument);
	        }
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
                				+ "sync_id, organization_id, group_id, updation_timestamp, d.updation_server_timestamp, "
                				+ "dt.name, dt.value, dt.color_hex_code, dt.document_designation, dt.delete_flag, dld.gps_location, "
                				+ "gd.name AS group_name, gd.gps_location AS group_gps_location, gd.business_id_number "
                				+ "FROM document d "
                				+ "JOIN document_type dt ON d.document_type_id = dt.id "
                				+ "JOIN group_data gd ON gd.id = d.group_id "
                				+ "LEFT JOIN document_location_data dld ON dld.document_sync_id = d.sync_id "
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
	 * Get all the field definitions for the organization
	 * 
	 * @return -  a list of documents that were found.
	 * @throws Exception - if there was an issue
	 */
	public List<DynamicFieldDefinition> getDynamicFieldDefinitionsByOrgId(long orgId) throws Exception {
		List<DynamicFieldDefinition> result=null;
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
				
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT dfd.*, dt.name, dt.value AS document_type_value, "
                				+ "dft.name, dft.value AS dynamic_field_type_value "
                				+ "FROM dynamic_field_def dfd "
        				        + "JOIN document_type dt ON dfd.document_type_id = dt.id "
        						+ "JOIN dynamic_field_type dft ON dft.id = dfd.field_type_id "
                				+ "WHERE dfd.org_id = ? "
        						+ "ORDER BY dfd.id ASC ");
	
		// execute the statement 
		preparedSELECTstatement.setLong(1, orgId);
		resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractDynamicFieldDefinitionFromResult(resultSet);

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
	 * Add dynamic field to the document
	 * 
	 * @param docs - the backup docs to attach
	 * @param docId - the id of the parent doc
	 * @throws Exception - if there was an issue
	 */
	public void createDocumentInfoData(List<DynamicFieldData> dynamicFieldData, long docId) throws Exception { 
		
		// 
		// process the request
				
		// delete previous tags
		deleteDynamicFieldData(docId);
		
		// create the new doc data
		for(int i=0; i< dynamicFieldData.size(); i++){
			System.out.println("Creating Doc Dynamic Field : " + docId + " --> " + dynamicFieldData.get(i).getId());
			createDynamicFieldData(docId, dynamicFieldData.get(i));
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
	 * Delete the attached docs
	 * 
	 * @param docId - the id of the document to purge the attachments from 
	 * @throws ServiceManagementException - if there was an issue
	 */
	public void deleteDynamicFieldData(long docId) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		
		// create the query
		String deleteQuery = " DELETE FROM dynamic_field_data WHERE parent_doc_id = ?";
		
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
			throw new ServiceManagementException("Error while deleting doc info fields for document with id=" + docId, e);
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
	
	
	
	public void deleteDynamicFieldDefinition(long dynamicFieldDefinitionId) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		
		try {
			if(!this.canBeDeletedDynamicFieldDefinition(dynamicFieldDefinitionId)){
				// cannot be deleted as it already exists
				throw new ServiceManagementException("Cannot be deleted as data is already associated with definition");
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting a dynamic field definition for definition with id=" + dynamicFieldDefinitionId, e1);
		}
		
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
			preparedDELETEStatement.setLong(1, dynamicFieldDefinitionId);
			preparedDELETEStatement.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting a dynamic field definition for definition with id=" + dynamicFieldDefinitionId, e);
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
	 * Link a document to the target doc
	 * 
	 * @param documentID
	 * @param linkedDocumentId
	 * @throws Exception - if there was an issue
	 */
	private void createDynamicFieldData(long documentID, DynamicFieldData data) throws Exception {
		PreparedStatement preparedINSERTstatement;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request

		// create the query
		String insertQuery = " insert into dynamic_field_data ("
				+ "parent_doc_id, "
				+ "parent_dynamic_field_id, "
				+ "value "
				+ ")"
		        + " values (?, ?, ?)";
		
		// create the statement
		preparedINSERTstatement = conn
                .prepareStatement(insertQuery);
		// execute the statement 
		preparedINSERTstatement.setLong(1, documentID);
		preparedINSERTstatement.setLong(2, data.getDynamicFieldDefinitionId());
		preparedINSERTstatement.setString(3, data.getData());
		preparedINSERTstatement.executeUpdate();
		
		// release the connection
		closeConnection(conn);
		
		getLog().info("Added Doc Info: " + documentID + " " + data.getDynamicFieldDefinitionId());
		
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
		
		String SELECT_QUERY_PREFIX = "SELECT d.id, d.doc_status, d.username, d.userid, d.creation_timestamp, d.document_type, d.type_hex_color, "
				+ "d.document_type_id, d.sync_id, d.organization_id, d.group_id, d.updation_timestamp, d.updation_server_timestamp, "
				+ "gd.name AS group_name, gd.gps_location AS group_gps_location, gd.business_id_number, "
				+ "gdt.name AS group_type_name, gdt.value AS group_type_value, gdt.order_index, "
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
	
	public InputStream getDocumentDummyPage() throws Exception{
		InputStream binaryStream=null;
		try {
			BufferedImage img = ImageIO.read(new File((getWebInfPath("dummy.page.image.20x20.jpg"))));
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(img, "jpeg", os);                          // Passing: ​(RenderedImage im, String formatName, OutputStream output)
			binaryStream = new ByteArrayInputStream(os.toByteArray());
		} catch (IOException e) {
			// Handle the error
		}

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
		// delegate
		return create(newDocument, true);
	}
	
	
	/**
	 * Create a new document in the database
	 * 
	 * @param newDocument
	 * @return
	 * @throws Exception - if there was an issue
	 */
	public Document create(Document newDocument, boolean pagesUpdate) throws PersistenceException, IllegalArgumentException { 
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
				+ "userid, "
				+ "creation_timestamp, "
				+ "updation_timestamp, "
				+ "document_type, "
				+ "document_type_id, "
				+ "type_hex_color, "
				// + "document_image_blob, "
				+ "sync_id, "
				+ "organization_id, "
				+ "group_id, "
				+ "doc_status, "
				+ "gps_location, "
				+ "updation_server_timestamp "
				+ ")"
		        + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		// create the statement
		try {
			preparedINSERTstatement = conn
			        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			
			// execute the statement 
			preparedINSERTstatement.setString(1, newDocument.getOwner());
			preparedINSERTstatement.setLong(2, newDocument.getOwnerId());
			preparedINSERTstatement.setString(3, newDocument.getCreationTimestamp());
			preparedINSERTstatement.setString(4, newDocument.getUpdationTimestamp());
			preparedINSERTstatement.setString(5, newDocument.getDocumentType());
			preparedINSERTstatement.setLong(6, newDocument.getType().getId());
			preparedINSERTstatement.setString(7, newDocument.getTypeHEXColor());
			// .setBlob(6, new javax.sql.rowset.serial.SerialBlob(imageBlob));
			preparedINSERTstatement.setString(8, newDocument.getSyncID());
			preparedINSERTstatement.setLong(9, newDocument.getOrganizationId());
			preparedINSERTstatement.setLong(10, newDocument.getGroupId());
			preparedINSERTstatement.setString(11, newDocument.getStatus());
			preparedINSERTstatement.setString(12, newDocument.getGpsLocation());
			//
			// fetch server time
			Date serverDate = new Date();
			preparedINSERTstatement.setString(13, DateUtility.simpleDateFormat(serverDate, DateUtility.FORMAT_DATE_AND_TIME));
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
		
		if(pagesUpdate){
			//
			// Create the pages
			//
			for(int i=0; i<newDocument.getPages().size(); i++){
				try {
					// newDocument.getPages().get(i).setPageNumber(i+1);
					createDocumentPages(newDocument.getPages().get(i), newDocument.getId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
		// Create the Doc Data
		//
		if(newDocument.getDynamicFieldData().size() > 0){
			try {
				createDocumentInfoData(newDocument.getDynamicFieldData(), newDocument.getId());
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
		
		//
		// Set GPS location
		// 
		if(newDocument.getGpsLocation() != null && !newDocument.getGpsLocation().isEmpty()){
			try {
				setDocumentGPSLocation(newDocument.getSyncID(), newDocument.getGpsLocation());

			} catch (Exception e) {
				getLog().error("Error setting document gps: - " + e.getStackTrace());
				
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
		//
		// delegate
		return update(oldDocument, true);
	}
	
	
	/**
	 * Update an existing document in the database
	 * 
	 * @param newDocument
	 * @return
	 * @throws Exception - if there was an issue
	 */
	public Document update(Document oldDocument, boolean pagesUpdate) throws IllegalArgumentException, PersistenceException, EntityNotFoundException {
		// create the java mysql update preparedstatement
	    String query = "UPDATE document set doc_status = ?, creation_timestamp = ?,  "
	    		+ "updation_timestamp = ?, updation_server_timestamp = ? WHERE id = ?";
	    long docID = oldDocument.getId(); 
	    
	    // get the connection
	 	Connection conn = openConnection();
		userService.init();
	    
	    
	    try {
	    	PreparedStatement preparedUPDATEstatement = conn.prepareStatement(query);

	    	preparedUPDATEstatement.setString(1, oldDocument.getStatus());
	    	preparedUPDATEstatement.setString(2, oldDocument.getCreationTimestamp());
	    	preparedUPDATEstatement.setString(3, oldDocument.getUpdationTimestamp());
	    	//
			// fetch server time
			Date serverDate = new Date();
			preparedUPDATEstatement.setString(4, DateUtility.simpleDateFormat(serverDate, DateUtility.FORMAT_DATE_AND_TIME));
			preparedUPDATEstatement.setLong(5, docID);
			
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
			if(action == null){
				action = AuditAction.DOCUMENT_EDIT;
			}
			getLog().info("Auditing Document-Action: <pre> doc Id: " + oldDocument.getId() 
			+ " action: " + action 
			+ " owner : " + oldDocument.getOwner()
			+ " status: " + oldDocument.getStatus());
			auditRequest(oldDocument, AuditUserType.USER, action, oldDocument.getOwner());
			getLog().info("Auditing Document-Action: <post> doc Id: " + oldDocument.getId() 
			+ " action: " + action 
			+ " owner : " + oldDocument.getOwner()
			+ " status: " + oldDocument.getStatus());
			
		}catch (Exception e) {
			getLog().error("Error Auditing Document-Action: " + e);
			getLog().error("Error Auditing Document-Action <stack-trace> : " + ExceptionUtils.getStackTrace(e));
		}
		
		Document oldDoc = null;
		try {
			oldDoc = getDocumentById(oldDocument.getId()).get(0);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(pagesUpdate){
			//
			// transfer any old pages to the new doc
			List<DocumentPage> existingPages = new ArrayList<DocumentPage>();
			for(int i=0; i<oldDocument.getPages().size(); i++){
				if(oldDocument.getPages().get(i).getId() > 0){
					existingPages.add(oldDocument.getPages().get(i));
				}
			}
			deleteOmmitedPages(oldDoc,existingPages);
			
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
		}
		
		//
		// Create the recipients
		//
		if(oldDocument.getToRecipients().size() >= 0){
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
		if(oldDocument.getAttachedDocuments().size() >= 0){
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
		if(oldDocument.getLinkedDocuments().size() >= 0){
			try {
				createDocumentLinks(oldDocument.getLinkedDocuments(), oldDocument.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//
		// Create the Doc Data
		//
		if(oldDocument.getDynamicFieldData().size() > 0){
			try {
				createDocumentInfoData(oldDocument.getDynamicFieldData(), oldDocument.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//
		// Create the Tags
		//
		if(oldDocument.getTags().size() >= 0){
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
		
		//
		// preconditions
		deleteAllDocumentGPSLocations(syncId);
		
		// get the connection
		Connection conn = openConnection();
		
		//
		// first delete any of the previous settings for the GPS location for this document
		
		
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
	 * Remove all the GPS location elements for the given document
	 * @param docId - the sync id of the docuemnt
	 * @throws PersistenceException 
	 * @throws SQLException 
	 */
	public void deleteAllDocumentGPSLocations(String syncId) throws PersistenceException, SQLException{
		PreparedStatement preparedDELETEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		
		// create the query
		String deleteQuery = " DELETE FROM document_location_data WHERE document_sync_id = ?";
		
		// create the statement
		try {
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			
			// execute the statement 
			preparedDELETEStatement.setString(1, syncId);
			preparedDELETEStatement.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new PersistenceException("Error while deleting gps location for a document with id=" + syncId, e);
		}

		// release the connection
		closeConnection(conn);

		
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
			getLog().debug("FETCH ALL: <extractDocumentFromResult> Extracting Data...");
			// extract the data
			long id = resultSet.getLong("id");
            String username = resultSet.getString("username");
            long userid = resultSet.getLong("userid");
            String creationDate = resultSet.getString("creation_timestamp");
            String updationDate = resultSet.getString("updation_timestamp");
            String updationServerDate = resultSet.getString("updation_server_timestamp");
            String documentType = resultSet.getString("document_type");
            String typeHexColor = resultSet.getString("type_hex_color");
            Long documentTypeId = resultSet.getLong("document_type_id");
            String documentSyncId = resultSet.getString("sync_id");
            long organizationId = resultSet.getLong("organization_id");
            // group data
            long groupId = resultSet.getLong("group_id");
            String groupName = resultSet.getString("group_name");
            String groupTypeName = resultSet.getString("group_type_name");
            String groupTypeValue = resultSet.getString("group_type_value");
            int groupTypeOrderIndex = resultSet.getInt("order_index");
            String groupGPSLocation = resultSet.getString("group_gps_location");
            String groupBusinessId = resultSet.getString("business_id_number");
            
            // misc doc data
            String  docStatus = resultSet.getString("doc_status");
            String docGPSLocation = resultSet.getString("gps_location");
            
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
            getLog().debug("FETCH ALL: <extractDocumentFromResult> getting user data...");
            if(userid <= 0) {
            	User userOwner = userService.getUserByName(username);
            	document.setOwnerId(userOwner.getId());
            } else {
                document.setOwnerId(userid);
            }
            document.setOwner(username);
            document.setId(id);
            document.setDocumentType(documentType);
            document.setStatus(docStatus);
            document.setGpsLocation(docGPSLocation);
            // convert the data through proper format
            //
            getLog().debug("FETCH ALL: <extractDocumentFromResult> formatting dates...");
            if(creationDate != null && creationDate.contains("/")){
            	// convert to older style
            	creationDate = DateUtility.formatFullDateStringToSimpleDateTimeString(creationDate);
            }
            if(updationDate != null) {
            	if(updationDate.contains("/")){
                    // convert to older style
            		updationDate = DateUtility.formatFullDateStringToSimpleDateTimeString(creationDate);
            	}
            } else {
            	updationDate = creationDate;
            }
            if(updationServerDate != null){
            	if(updationServerDate.contains("/")){
            		// convert to older style
            		updationServerDate = DateUtility.formatFullDateStringToSimpleDateTimeString(creationDate);
            	}
            } else {
            	updationServerDate = updationDate;
            }
            
            //
            // set data into docuemnt
            document.setCreationTimestamp(creationDate);
            document.setUpdationTimestamp(updationDate);
            document.setUpdationServerTimestamp(updationServerDate);            
            document.setTypeHEXColor(typeHexColor);
            document.setSyncID(documentSyncId);
            document.setOrganizationId(organizationId);
            document.setGroupId(groupId);
            document.setGroupName(groupName);
            document.setGroupTypeName(groupTypeName);
            document.setGroupTypeValue(groupTypeValue);
            document.setGroupTypeOrderIndex(groupTypeOrderIndex);
            document.setGroupGPSCoordinates(groupGPSLocation);
            document.setGroupBusinessId(groupBusinessId);
            
            //
            // Get the document pages
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching page data...");
            List<DocumentPage> pages = fetchDocumentPages(id);
            document.setPages(pages);
            
            //
            // Get Tags
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching tag data...");
            List<TagData> tags = fetchDocumentTags(id);
            document.setTags(tags);
            

            
            //
            // Get recipients
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching recipient data...");
            List<User> recipients = fetchDocumentRecipients(id);
            
            //
            // Get Linked Documents
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching linked docs data...");
            List<Document> linkedDocs = fetchDocumentLinks(id);
            if(callerUser != null && (callerUser.getRoles().get(0).getValue().equals(Role.ROLE_NAME_MATRIX_ADMIN)
            	|| callerUser.getRoles().get(0).getValue().equals(Role.ROLE_NAME_SUPER_ADMIN))){
            	// set all linked docs
            	for(int i=0; i< linkedDocs.size(); i++){
            		List<DocumentPage> linkedDocPages = fetchDocumentPages(linkedDocs.get(i).getId());
            		linkedDocs.get(i).setPages(linkedDocPages);
            	}
            } else {
            	if(recipients.contains(callerUser)){
                	// add full backing document acccess
                	for(int i=0; i< linkedDocs.size(); i++){
                		List<DocumentPage> linkedDocPages = fetchDocumentPages(linkedDocs.get(i).getId());
                		linkedDocs.get(i).setPages(linkedDocPages);
                	}
                }
            }
            document.setLinkedDocuments(linkedDocs);
            
            //
            // Get attached Documents
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching attached docs data...");
            List<Document> attachedDocs = new ArrayList<Document>();;
            attachedDocs = fetchDocumentAttachments(id);
            if(callerUser != null && (callerUser.getRoles().get(0).getValue().equals(Role.ROLE_NAME_MATRIX_ADMIN)
                	|| callerUser.getRoles().get(0).getValue().equals(Role.ROLE_NAME_SUPER_ADMIN))){
                	// set all linked docs
	            	for(int i=0; i< attachedDocs.size(); i++){
	            		List<DocumentPage> backupDocPages = fetchDocumentPages(attachedDocs.get(i).getId());
	            		attachedDocs.get(i).setPages(backupDocPages);
	            	}
                } else {
                	if(recipients.contains(callerUser)){
                    	// add full backing document acccess
                    	for(int i=0; i< attachedDocs.size(); i++){
                    		List<DocumentPage> backupDocPages = fetchDocumentPages(attachedDocs.get(i).getId());
                    		attachedDocs.get(i).setPages(backupDocPages);
                    	}
                    }
                }
            document.setAttachedDocuments(attachedDocs);

            //
            // Get notes
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching document notes data...");
            List<NoteData> documentNotes = fetchDocumentNotes(document.getId());
            document.setNotes(documentNotes);
            
            //
            // get Doc Info Fields
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching dynamic field dta data...");
            List<DynamicFieldData> docData = getAllDocInfoFields(document.getId());
            document.setDynamicFieldData(docData);
            
            //
            // Get Types
            //docType = fetchDocumentTypeData(docType.getId());
            //document.setType(docType);
            
            getLog().debug("FETCH ALL: <extractDocumentFromResult> check if doc was read...");
            if(callerUser != null){
            	boolean wasRead = wasDocumentRead(document.getSyncID(),callerUser.getName() );
                document.setCurrentUserRead(wasRead);
            }
            
            document.setToRecipients(recipients);
            
            boolean doNotAddDoc = false;
            getLog().debug("FETCH ALL: <extractDocumentFromResult> process doc type... : " + docType);
            if(docType != null){
            	if(!hasPermissions(callerUser, document, permissionsOverride)){
            		doNotAddDoc = true;
    		    }else if(!docType.isEmpty() && !document.getType().getDocumentDesignation().equals(docType)){
    		    	doNotAddDoc = true;
    		    }
            }
            
            //
            // make sure there are no duplicates
            if(!doNotAddDoc && !documents.contains(document)){
            	documents.add(document);
            }
             
        }
		
		return documents;
	}
	
	
	/**
	 * Translate SQL results into model data
	 * 
	 * @param resultSet - the result set with Document data
	 * @return - converted data
	 * @throws Exception - if there was an issue
	 */
	private List<Document> extractDocumentFromResult_v2(ResultSet resultSet, User callerUser, String docType, boolean permissionsOverride) throws Exception {
		List<Document> documents= new ArrayList<Document>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			getLog().debug("FETCH ALL: <extractDocumentFromResult_v2> Extracting Data...");
			// extract the data
			long id = resultSet.getLong("id");
            String username = resultSet.getString("username");
            long userid = resultSet.getLong("userid");
            String creationDate = resultSet.getString("creation_timestamp");
            String updationDate = resultSet.getString("updation_timestamp");
            String documentType = resultSet.getString("document_type");
            String typeHexColor = resultSet.getString("type_hex_color");
            Long documentTypeId = resultSet.getLong("document_type_id");
            String documentSyncId = resultSet.getString("sync_id");
            long organizationId = resultSet.getLong("organization_id");
            // group data
            long groupId = resultSet.getLong("group_id");
            String groupName = resultSet.getString("group_name");
            String groupTypeName = resultSet.getString("group_type_name");
            String groupTypeValue = resultSet.getString("group_type_value");
            int groupTypeOrderIndex = resultSet.getInt("order_index");
            String groupGPSLocation = resultSet.getString("group_gps_location");
            String groupBusinessId = resultSet.getString("business_id_number");
            
            // misc doc data
            String  docStatus = resultSet.getString("doc_status");
            String docGPSLocation = resultSet.getString("gps_location");
            
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
            getLog().debug("FETCH ALL: <extractDocumentFromResult> getting user data...");
            if(userid <= 0) {
            	User userOwner = userService.getUserByName(username);
            	document.setOwnerId(userOwner.getId());
            } else {
                document.setOwnerId(userid);
            }
            document.setOwner(username);
            document.setId(id);
            document.setDocumentType(documentType);
            document.setStatus(docStatus);
            document.setGpsLocation(docGPSLocation);
            // convert the data through proper format
            //
            getLog().debug("FETCH ALL: <extractDocumentFromResult> formatting dates...");
            if(creationDate != null && creationDate.contains("/")){
            	// convert to older style
            	creationDate = DateUtility.formatFullDateStringToSimpleDateTimeString(creationDate);
            }
            if(updationDate != null && updationDate.contains("/")){
            	// convert to older style
            	updationDate = DateUtility.formatFullDateStringToSimpleDateTimeString(creationDate);
            }
            document.setCreationTimestamp(creationDate);
            document.setUpdationTimestamp(updationDate);
            document.setTypeHEXColor(typeHexColor);
            document.setSyncID(documentSyncId);
            document.setOrganizationId(organizationId);
            document.setGroupId(groupId);
            document.setGroupName(groupName);
            document.setGroupTypeName(groupTypeName);
            document.setGroupTypeValue(groupTypeValue);
            document.setGroupTypeOrderIndex(groupTypeOrderIndex);
            document.setGroupGPSCoordinates(groupGPSLocation);
            document.setGroupBusinessId(groupBusinessId);
            
            //
            // Get the document pages
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching page data...");
            List<DocumentPage> pages = fetchDocumentPages(id);
            document.setPages(pages);
            
            //
            // Get Tags
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching tag data...");
            List<TagData> tags = fetchDocumentTags(id);
            document.setTags(tags);
            

            
            //
            // Get recipients
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching recipient data...");
            List<User> recipients = fetchDocumentRecipients(id);
            
            //
            // Get Linked Documents
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching linked docs data...");
            List<Document> linkedDocs = fetchDocumentLinks(id);
            if(callerUser != null && (callerUser.getRoles().get(0).getValue().equals(Role.ROLE_NAME_MATRIX_ADMIN)
            	|| callerUser.getRoles().get(0).getValue().equals(Role.ROLE_NAME_SUPER_ADMIN))){
            	// set all linked docs
            	for(int i=0; i< linkedDocs.size(); i++){
            		List<DocumentPage> linkedDocPages = fetchDocumentPages(linkedDocs.get(i).getId());
            		linkedDocs.get(i).setPages(linkedDocPages);
            	}
            } else {
            	if(recipients.contains(callerUser)){
                	// add full backing document acccess
                	for(int i=0; i< linkedDocs.size(); i++){
                		List<DocumentPage> linkedDocPages = fetchDocumentPages(linkedDocs.get(i).getId());
                		linkedDocs.get(i).setPages(linkedDocPages);
                	}
                }
            }
            document.setLinkedDocuments(linkedDocs);
            
            //
            // Get attached Documents
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching attached docs data...");
            List<Document> attachedDocs = new ArrayList<Document>();;
            attachedDocs = fetchDocumentAttachments(id);
            if(callerUser != null && (callerUser.getRoles().get(0).getValue().equals(Role.ROLE_NAME_MATRIX_ADMIN)
                	|| callerUser.getRoles().get(0).getValue().equals(Role.ROLE_NAME_SUPER_ADMIN))){
                	// set all linked docs
	            	for(int i=0; i< attachedDocs.size(); i++){
	            		List<DocumentPage> backupDocPages = fetchDocumentPages(attachedDocs.get(i).getId());
	            		attachedDocs.get(i).setPages(backupDocPages);
	            	}
                } else {
                	if(recipients.contains(callerUser)){
                    	// add full backing document acccess
                    	for(int i=0; i< attachedDocs.size(); i++){
                    		List<DocumentPage> backupDocPages = fetchDocumentPages(attachedDocs.get(i).getId());
                    		attachedDocs.get(i).setPages(backupDocPages);
                    	}
                    }
                }
            document.setAttachedDocuments(attachedDocs);

            //
            // Get notes
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching document notes data...");
            List<NoteData> documentNotes = fetchDocumentNotes(document.getId());
            document.setNotes(documentNotes);
            
            //
            // get Doc Info Fields
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching dynamic field dta data...");
            List<DynamicFieldData> docData = getAllDocInfoFields(document.getId());
            document.setDynamicFieldData(docData);
            
            //
            // Get Types
            //docType = fetchDocumentTypeData(docType.getId());
            //document.setType(docType);
            
            getLog().debug("FETCH ALL: <extractDocumentFromResult> check if doc was read...");
            if(callerUser != null){
            	boolean wasRead = wasDocumentRead(document.getSyncID(),callerUser.getName() );
                document.setCurrentUserRead(wasRead);
            }
            
            document.setToRecipients(recipients);
            
            boolean doNotAddDoc = false;
            getLog().debug("FETCH ALL: <extractDocumentFromResult> process doc type... : " + docType);
            if(docType != null){
            	if(!hasPermissions(callerUser, document, permissionsOverride)){
            		doNotAddDoc = true;
    		    }else if(!docType.isEmpty() && !document.getType().getDocumentDesignation().equals(docType)){
    		    	doNotAddDoc = true;
    		    }
            }
            
            //
            // make sure there are no duplicates
            if(!doNotAddDoc && !documents.contains(document)){
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
			//
			// change the collation number
			updatePageCollation(page);
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
	 * Update an existing page for the given document
	 * 
	 * @param page - the page to be added
	 * @return - the updated page data
	 * @throws Exception - if there was an issue
	 */
	public DocumentPage updateDocumentPageFromFormData(DocumentPage page) throws Exception {
		PreparedStatement preparedUPDATEstatement;
	 
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		// 
		// process the request
		try {
			// create the query
			String updateQuery = "UPDATE document_page set page_number = ? WHERE id = ?";
		
			// create the statement
			preparedUPDATEstatement = conn.prepareStatement(updateQuery);
			// execute the statement 
			preparedUPDATEstatement.setLong(2, page.getId());
			preparedUPDATEstatement.setLong(1, page.getPageNumber());
			
			// execute the java prepared statement
		    preparedUPDATEstatement.executeUpdate();
			// release the connection
			closeConnection(conn);

			getLog().info("UPDATE PAGE: " + page);

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
	 * Update an existing document type for this matrix
	 * 
	 * @param docType - the document type to be updated
	 * @return - the updated/created document type
	 * @throws Exception - if there was an issue
	 */
	public DocumentType updateDocType(DocumentType docType) throws Exception {
		PreparedStatement preparedUPDATEstatement;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the query
		String insertQuery = " UPDATE document_type SET "
				+ "name = ?, "
				+ "value = ?, "
				+ "color_hex_code = ?, "
				+ "document_designation = ? "
		        + "WHERE id = ?";
		
		// create the statement
		preparedUPDATEstatement = conn
                .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
		// execute the statement 
		preparedUPDATEstatement.setString(1, docType.getName());
		preparedUPDATEstatement.setString(2, docType.getValue());
		preparedUPDATEstatement.setString(3, docType.getHexColorCode());
		preparedUPDATEstatement.setString(4, docType.getDocumentDesignation());
		preparedUPDATEstatement.setLong(5, docType.getId());
		preparedUPDATEstatement.executeUpdate();
		

		// release the connection
		closeConnection(conn);
		
		getLog().info("UPDATE Doc Type: " + docType);
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
                		+ "WHERE document_id=? ORDER BY page_number ASC");
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
	 * 
	 * @param fieldDefinition
	 * @return
	 * @throws Exception
	 */
	public DynamicFieldDefinition createDynamicFieldDefinition(DynamicFieldDefinition fieldDefinition) throws Exception {
		PreparedStatement preparedINSERTstatement;
		long returnId=0;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the query
		String insertQuery = " insert into dynamic_field_def ("
				+ "org_id, "
				+ "document_type_id, "
				+ "display_name, "
				+ "description, "
				+ "field_type_id, "
				+ "max_length, "
				+ "is_required, "
				+ "ordinal, "
				+ "is_doc_id, "
				+ "ocr_match_text, "
				+ "ocr_match_length"
				+ ")"
		        + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		// create the statement
		preparedINSERTstatement = conn
                .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
		// execute the statement 
		preparedINSERTstatement.setLong(1, fieldDefinition.getOrgID());
		preparedINSERTstatement.setLong(2, fieldDefinition.getDocTypeId());
		preparedINSERTstatement.setString(3, fieldDefinition.getDisplayName());
		preparedINSERTstatement.setString(4, fieldDefinition.getDescription());
		preparedINSERTstatement.setLong(5, fieldDefinition.getFieldTypeId());
		preparedINSERTstatement.setInt(6, fieldDefinition.getMaxLength());
		preparedINSERTstatement.setBoolean(7, fieldDefinition.isRequired());
		preparedINSERTstatement.setInt(8, fieldDefinition.getOrdinal());
		preparedINSERTstatement.setBoolean(9, fieldDefinition.isDocId());
		preparedINSERTstatement.setString(10, fieldDefinition.getOcrMatchText());
		preparedINSERTstatement.setInt(11, fieldDefinition.getOcrGrabLength());
		preparedINSERTstatement.executeUpdate();
		
		// get the id of the created 
		ResultSet rs = preparedINSERTstatement.getGeneratedKeys();
		if (rs.next()){
			returnId=rs.getLong(1);
		}

		// release the connection
		closeConnection(conn);
		
		// return the result
		fieldDefinition.setId(returnId);
		getLog().info("CREATE Dynamic Field Definition: " + fieldDefinition);
		return fieldDefinition;
		
	}
	
	/**
	 * 
	 * @param fieldDefinition
	 * @return
	 * @throws Exception
	 */
	public void updateDynamicFieldDefinition(DynamicFieldDefinition fieldDefinition) throws Exception {
		PreparedStatement preparedUPDATEstatement;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the query
		String updateQuery = " UPDATE dynamic_field_def SET "
				+ "org_id = ?, "
				+ "document_type_id = ?, "
				+ "display_name = ?, "
				+ "description = ?, "
				+ "field_type_id = ?, "
				+ "max_length = ?, "
				+ "is_required = ?, "
				+ "ordinal = ?, "
				+ "is_doc_id = ?, "
				+ "ocr_match_text = ?, "
				+ "ocr_match_length = ? "
				+ "WHERE id = ?";

		
		// create the statement
		preparedUPDATEstatement = conn
                .prepareStatement(updateQuery);
		// execute the statement 
		preparedUPDATEstatement.setLong(1, fieldDefinition.getOrgID());
		preparedUPDATEstatement.setLong(2, fieldDefinition.getDocTypeId());
		preparedUPDATEstatement.setString(3, fieldDefinition.getDisplayName());
		preparedUPDATEstatement.setString(4, fieldDefinition.getDescription());
		preparedUPDATEstatement.setLong(5, fieldDefinition.getFieldTypeId());
		preparedUPDATEstatement.setInt(6, fieldDefinition.getMaxLength());
		preparedUPDATEstatement.setBoolean(7, fieldDefinition.isRequired());
		preparedUPDATEstatement.setInt(8, fieldDefinition.getOrdinal());
		preparedUPDATEstatement.setBoolean(9, fieldDefinition.isDocId());
		preparedUPDATEstatement.setString(10, fieldDefinition.getOcrMatchText());
		preparedUPDATEstatement.setInt(11, fieldDefinition.getOcrGrabLength());
		preparedUPDATEstatement.setLong(12, fieldDefinition.getId());
		preparedUPDATEstatement.executeUpdate();
		
		// get the id of the created 
		preparedUPDATEstatement.executeUpdate();

		// release the connection
		closeConnection(conn);
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
                .prepareStatement("SELECT d.id, d.doc_status, d.username, d.userid, d.creation_timestamp, d.document_type, d.type_hex_color, "
                		+ "d.document_type_id, d.sync_id, d.organization_id, d.group_id, gd.name AS group_name, "
                		+ "gdt.name AS group_type_name, gdt.value AS group_type_value, gdt.order_index "
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
	 * get all possible recipients for the given user
	 * 
	 * @param documentID - the id of the document for which to get the recipients
	 * @return - list of sorted recipients
	 * @throws Exception - if there was an issue.
	 */
	public List<User> fetchRecipientsForUser(User user) throws Exception {
		List<User> result=null;
		String[] uberRolesList;
		String uberRolesUnparsed;
		
		//
		// init data
		userService.init();
		this.configService.open();
		uberRolesUnparsed = this.configService.readConfigurationProperty("system.permissions.role.recipients.all.access");
            
        // Get user data
		result = userService.getAllUsers(user);
				
		//
		// check for roles
		if(uberRolesUnparsed != null && !uberRolesUnparsed.isEmpty()){
			uberRolesList = uberRolesUnparsed.split(",");
			if(isUberUser(user, uberRolesList)){
				//
				// they have access to all users
				return result;
			}
		}
		
		//
		//if(user.getRoles().get(0).getName().equals(Role.ROLE_NAME_SUPER_ADMIN) 
		//		|| user.getRoles().get(0).getName().equals(Role.ROLE_NAME_MATRIX_ADMIN)) {
		//if(isUberUser(user, uberRolesList))
		//	//
		//	// they have access to all users
		//	return result;
		//}
		
		//
		// get the current user group and the corresponding 
		// associated group types (i.e. stages)
		GroupType curreUserStage = user.getUserGroups().get(0).getGroupType();
		long[] associatedIds = curreUserStage.getAssociatedStageIds();

		Iterator<User> it = result.iterator();
		while(it.hasNext()){
			User tempUser = (User) it.next();
			/**
			 * TODO <REMOVE> This is a temp hack for recipients
			 */
			
			if(isValidRecipient(user, tempUser )){
				// do nothing
			} else {
				it.remove();
			}
			/**
			if((tempUser.getUserGroups().get(0).getGroupType().getId() == 314 
							&& user.getUserGroups().get(0).getGroupType().getId() == 316)){
				// do nothing
			}else if(tempUser.getUserGroups().get(0).getGroupType().getOrderIndex() != user.getUserGroups().get(0).getGroupType().getOrderIndex() -1){
				it.remove();
			}
			*/
			
			
		}

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
	 * Extract the JOIned data for the dynamic field definitions
	 * 
	 * @param resultSet - the input data from SQL Query
	 * @return - the list of field definitions entities or empty of none found
	 * @throws Exception - if there was an issue
	 */
	private List<DynamicFieldDefinition> extractDynamicFieldDefinitionFromResult(ResultSet resultSet) throws Exception {
		List<DynamicFieldDefinition> fieldDefs= new ArrayList<DynamicFieldDefinition>();
		
		// process the extractions - there could be none
		//
		while (resultSet.next()) {
			// extract the data
			
			
			// Dynamic Field Definition Data
			long id = resultSet.getLong("id");
			long orgId = resultSet.getLong("org_id");
			long docTypeId = resultSet.getLong("document_type_id");
			long fieldTypeId = resultSet.getLong("field_type_id");
			String displayName = resultSet.getString("display_name");
			String description = resultSet.getString("description");
			int maxfieldLength = resultSet.getInt("max_length");
			boolean isRequired = resultSet.getBoolean("is_required");
			int ordinal = resultSet.getInt("ordinal");
			boolean isDocId = resultSet.getBoolean("is_doc_id");
			// Ocr Data
			String ocrMatchText = resultSet.getString("ocr_match_text"); 
			int ocrMatchLength = resultSet.getInt("ocr_match_length");

			// Document Type Data
			String docTypeName = resultSet.getString("document_type_value"); 
			// Field Type Data
			String fieldTypeName = resultSet.getString("dynamic_field_type_value"); 

            // Create a new Document
            DynamicFieldDefinition fieldDef = new DynamicFieldDefinition();
            
            
            // populate the Page Sparsely entity
            fieldDef.setId(id);
            fieldDef.setOrgID(orgId);
            fieldDef.setDocTypeId(docTypeId);
            fieldDef.setFieldTypeId(fieldTypeId);
            fieldDef.setDisplayName(displayName);
            fieldDef.setDescription(description);
            fieldDef.setMaxLength(maxfieldLength);
            fieldDef.setOrdinal(ordinal);
            fieldDef.setRequired(isRequired);
            fieldDef.setDocId(isDocId);
            fieldDef.setOcrMatchText(ocrMatchText);
            fieldDef.setOcrGrabLength(ocrMatchLength);
            
            
            fieldDef.setFieldType(fieldTypeName);
            fieldDef.setDocTypeName(docTypeName);

            fieldDefs.add(fieldDef);
        }

		//
		// Return the data
		return fieldDefs;
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
	 * @param resultSet - the result set with Document Notes
	 * @return - converted data
	 * @throws Exception - if there was an issue.
	 */
	private List<Screening> extractScreeningsFromResult(ResultSet resultSet) throws Exception {
		List<Screening> data = new ArrayList<Screening>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
			String creationDate = resultSet.getString("test_date");
            Long healthScore = resultSet.getLong("health_score");
            String floorNumber = resultSet.getString("floor_number");
            String stationId = resultSet.getString("station_id");
            
            
            // Create a new Document
            Screening screening = new Screening();
            
            
            // populate the Document entity
            screening.setId(id);;
            screening.setTestDate(creationDate);
            screening.setHealthScore(healthScore);
            screening.setFloorNumber(floorNumber);
            screening.setStationId(stationId);

            data.add(screening);
        }
		
		return data;
	}
	
	
	/**
	 * Translate SQL results into model data
	 * 
	 * @param resultSet - the result set with Document Notes
	 * @return - converted data
	 * @throws Exception - if there was an issue.
	 */
	private List<Screening> extractTotalScreeningsFromResult(ResultSet resultSet) throws Exception {
		List<Screening> data = new ArrayList<Screening>();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
			String creationDate = resultSet.getString("test_date");
            Long healthScore = resultSet.getLong("health_score");
            String floorNumber = resultSet.getString("floor_number");
            String stationId = resultSet.getString("station_id");
            String username = resultSet.getString("name");
            
            
            // Create a new Document
            Screening screening = new Screening();
            
            
            // populate the Document entity
            screening.setId(id);;
            screening.setTestDate(creationDate);
            screening.setHealthScore(healthScore);
            screening.setFloorNumber(floorNumber);
            screening.setStationId(stationId);
            screening.setUsername(username);

            data.add(screening);
        }
		
		return data;
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
		
		userService.init();
		
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
            //
            
            // get owner data
            User userOwner = userService.getUserByName(owner);
            doc.setOwnerId(userOwner.getId());
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
            
            //
            // Get the document pages
            List<DocumentPage> pages = fetchDocumentPages(id);
            doc.setPages(pages);
            
            //
            // get Doc Info Fields
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching dynamic field dta data...");
            List<DynamicFieldData> docData = getAllDocInfoFields(id);
            doc.setDynamicFieldData(docData);


            attachments.add(doc);
        }
		
		return attachments;
	}
	
	
	/**
	 * Translate SQL results into model data
	 * 
	 * @param resultSet - the result set with Doc Info Data Fields
	 * @return - converted data
	 * @throws Exception - if there was an issue.
	 */
	private List<DynamicFieldData> extractDocInfoData(ResultSet resultSet) throws Exception {
		List<DynamicFieldData> result = new ArrayList<DynamicFieldData>();
		userService.init();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long id = resultSet.getLong("id");
			long parentDocId = resultSet.getLong("parent_doc_id");
			long dynamicFieldId = resultSet.getLong("parent_dynamic_field_id");
			String value = resultSet.getString("value");
			String displayName = resultSet.getString("display_name");
			
			
			//
			// Create Instance
			DynamicFieldData data = new DynamicFieldData();
			data.setId(id);
			data.setParentResourceId(parentDocId);
			data.setDynamicFieldDefinitionId(dynamicFieldId);
			data.setFieldDisplayNameValue(displayName);
			data.setData(value);
            
			result.add(data);
        }
		
		return result;
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
			long userId = resultSet.getLong("to_user_id");

            
            // Get user data
            User user = userService.getUserByUserId(userId);     
            
            if(user != null) {
            	toUsers.add(user);
            }
            
        }
		
		return toUsers;
	}
	
	/**
	 * Translate SQL results into model data
	 * 
	 * @param resultSet - the result set with Users
	 * @return - converted data
	 * @throws Exception - if there was an issue.
	 */
	private List<User> extractRecipientUsersFromResult(ResultSet resultSet) throws Exception {
		List<User> toUsers= new ArrayList<User>();
		userService.init();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			// extract the data
			long userId = resultSet.getLong("user_id");
			long stageOrdinal = resultSet.getLong("order_index");

            
            // Get user data
            User user = userService.getUserByUserId(userId);     
            
            if(user != null) {
            	toUsers.add(user);
            }
            
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
            String groupTypeValue = resultSet.getString("group_type_value");
            int groupTypeOrderIndex = resultSet.getInt("order_index");
            String creationDate = resultSet.getString("creation_timestamp");
            long docTypeId = resultSet.getLong("document_type_id");
            
            // Create a new Document
            Document doc = new Document();
            
            
            // populate the Document entity
            User userOwner = userService.getUserByName(owner);
            doc.setOwnerId(userOwner.getId());
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
            doc.setGroupTypeValue(groupTypeValue);
            doc.setGroupTypeOrderIndex(groupTypeOrderIndex);
            
            //
            // Doc Type
            DocumentType docTypeData = null;
            docTypeData = fetchDocumentTypeData(docTypeId);
            doc.setType(docTypeData);
            
            //
            // Get the document pages
            List<DocumentPage> pages = fetchDocumentPages(id);
            doc.setPages(pages);
            
            //
            // get Doc Info Fields
            getLog().debug("FETCH ALL: <extractDocumentFromResult> fetching dynamic field dta data...");
            List<DynamicFieldData> docData = getAllDocInfoFields(id);
            doc.setDynamicFieldData(docData);


            links.add(doc);
        }
		
		return links;
	}
	
	
	/**
	 * Extract Pages from the PDF and return converted Doc Pages
	 * @param profileImage - the image data
	 * @param username - the user for whom the update is done
	 * @throws PersistenceException
	 * 		- if there were any issues in processing this request
	 */
	public List<DocumentPage> extractPDFDocumentPages(InputStream importDocPDFStream) throws PersistenceException {
		byte [] bDocImportPDF = new byte [0];
	    
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
			

		// release the connection
		closeConnection(conn);
		
		getLog().info("Extracted PDF Doc Pages: " + documentPages.size());
		
		return documentPages;
		
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
		Document importedNewDoc = doc;
		
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
		importedNewDoc.addPages(documentPages);
		
		//
		// create the document
		importedNewDoc = create(importedNewDoc);
		
		
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
		
		return importedNewDoc;
		
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
	public Document updateDocumentStatus(String status, String docSessionId, String userName, String... updationTimestamp) throws Exception {
		PreparedStatement preparedUPDATEstatement = null;
		// create the java mysql update preparedstatement
	    String queryWithTimestampUpdate = "UPDATE document set doc_status = ?, "
	    		+ "updation_timestamp = ?, updation_server_timestamp = ? WHERE id = ?";
	    String queryWithoutTimestampUpdate = "UPDATE document set doc_status = ? WHERE id = ?";
	    String query = "";
	    
	    long docID = getDocIdBySyncId(docSessionId); 
	    Document doc = getDocumentById(docID).get(0);;
	    
	    // get the connection
	 	Connection conn = openConnection();
	 	// fetch server time
	 	Date serverDate = new Date();
	 	String serverUpdationDate = DateUtility.simpleDateFormat(serverDate, DateUtility.FORMAT_DATE_AND_TIME);
	 	boolean returnServerUpdateDate = false;
	 	
	 	//
	 	// check yje specific update
	 	if(updationTimestamp.length > 0){
	 		if( (doc.getStatus().equals(Document.STATUS_DRAFT) && status.equals(Document.STATUS_SUBMITTED)) ||
	 				status.equals(Document.STATUS_REJECTED)	) {
		 			
	 			returnServerUpdateDate = true;
	 			doc.setUpdationServerTimestamp(serverUpdationDate);
		 		//
		 		// we update the server and updation time
		 		preparedUPDATEstatement = conn.prepareStatement(queryWithTimestampUpdate);
		 		preparedUPDATEstatement.setString(1, status);
		 		preparedUPDATEstatement.setString(2, serverUpdationDate);
		 		preparedUPDATEstatement.setString(3, updationTimestamp[0]);
		 		preparedUPDATEstatement.setLong(4, docID);
		 		// execute the java preparedstatement
			    preparedUPDATEstatement.executeUpdate();
	 		} else {
	 			preparedUPDATEstatement = conn.prepareStatement(queryWithoutTimestampUpdate);
		 		preparedUPDATEstatement.setString(1, status);
		 		preparedUPDATEstatement.setLong(2, docID);
		 		// execute the java preparedstatement
			    preparedUPDATEstatement.executeUpdate();
	 		}
	 		
	 	}else {
	 		preparedUPDATEstatement = conn.prepareStatement(queryWithoutTimestampUpdate);
	 		preparedUPDATEstatement.setString(1, status);
	 		preparedUPDATEstatement.setLong(2, docID);
	 		// execute the java preparedstatement
		    preparedUPDATEstatement.executeUpdate();
	 		
	 	}
	    
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
			auditRequest(doc, AuditUserType.USER, action, userName);
		}catch (Exception e) {
			getLog().error("Error Auditing Document-Creation: " + e);
		}
		
		return doc;
		
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
	public void updatePageCollation(DocumentPage page) throws Exception {
		// create the java mysql update preparedstatement
	    String query = "UPDATE document_page set page_number = ? WHERE id = ?";
	    
	    // get the connection
	 	Connection conn = openConnection();
	    
	    PreparedStatement preparedUPDATEstatement = conn.prepareStatement(query);
	    preparedUPDATEstatement.setLong(2, page.getId());
	    preparedUPDATEstatement.setInt(1, page.getPageNumber());

	    // execute the java prepared statement
	    preparedUPDATEstatement.executeUpdate();
	      
	    conn.close();
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
	public List<Document> getAllDocsToLink_v2(User user, String docType,String customTag, String syncId) throws Exception{
		List<Document> allDocuments = getAllDocuments_v2(user, docType, false);
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
	
	
	public List<DynamicFieldData> getAllDocInfoFields(long docId) throws Exception {
		List<DynamicFieldData> result = new ArrayList<DynamicFieldData>();
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT dfd.*, dfd2.display_name, dfd2.description "
                				+ "FROM dynamic_field_data dfd " 
                				+ "JOIN dynamic_field_def dfd2 ON dfd2.id=dfd.parent_dynamic_field_id "
                				+ "WHERE dfd.parent_doc_id= ?");
		;
		// execute the statement 
		preparedSELECTstatement.setLong(1, docId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractDocInfoData(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
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
	         }
	         //
	         // else if(!element.getType().getDocumentDesignation().equals(DocumentType.DESIGNATION_PROFILE)){
	        	// // but first check if this document has the tag we are asked about
	         //	 itr.remove();
	         //}
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
	public List<Document> getAllDocsToAttach_v2(User user, String docType,String customTag, String syncId) throws Exception{
		List<Document> allDocuments = getAllDocuments_v2(user, docType, false);
		// extract only the documents that the user can access
		
		Iterator<Document> itr = allDocuments.iterator();
	      
	      while(itr.hasNext()) {
	         Document element = itr.next();
	         // if the user is NOT the owner disregard this
	         if(!element.getOwner().equals(user.getCredentials().getUsername())){
	        	 itr.remove();
		     // remove any documents whose status makes them not available
	         }
	         //
	         // else if(!element.getType().getDocumentDesignation().equals(DocumentType.DESIGNATION_PROFILE)){
	        	// // but first check if this document has the tag we are asked about
	         //	 itr.remove();
	         //}
	      }
	        
	  	  return allDocuments;
	}	
	
	/**
	 * Generates the PDF document representation of the trace of the document collection
	 * @param docId - the id of the document to trace through
	 * @return
	 * @throws Exception
	 */
	public PDDocument generateTracePDFDocument( long docId)  throws Exception {
		//PDType1Font PDF_GENERATION_TABLE_FONT_TYPE = PDType1Font.COURIER;
		PDDocument pdfTraceDocument = null;
		List<Document> allDocuments=null;
		String numberingFormat = "Page {0}";
		int offset_X = 60;
		int offset_Y = 18;
		int page_counter = 1;
		boolean dummyPageImageFlag = false;
		
		
		//
		// get all documents for the trace
		allDocuments = getRecursiveDocumentById(docId);
		
		pdfTraceDocument = new PDDocument();
		
		// Load a custom font;
		String filePath = getWebInfPath("ArialUnicodeMS.ttf");
	    //PDType0Font PDF_GENERATION_TABLE_FONT_TYPE = PDType0Font.load(pdfTraceDocument, resourceAsStream);
	    PDType0Font PDF_GENERATION_TABLE_FONT_TYPE = PDType0Font.load(pdfTraceDocument, new File(filePath));
	    //PDType3Font PDF_GENERATION_TABLE_FONT_TYPE3 = PDType3Font.
		
		//
		// create the trce document
		for(int i =0; i<allDocuments.size(); i++){
			
			//
			// create the pages for each trace document
			
			//
			// Check if the pages exist
			if(allDocuments.get(i).getPages().size() == 0){
				//
				// add a dummy document
				dummyPageImageFlag = true;
				DocumentPage dummyPage = new DocumentPage();
				dummyPage.setId(-1);
				allDocuments.get(i).getPages().add(dummyPage);
			}else {
				dummyPageImageFlag = false;
			}
			
			
			for(int page = 0; page < allDocuments.get(i).getPages().size(); page++){
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
				
				//
				// create image for the page
				PDImageXObject pdImage;

				//
				// check if we are woking with a dummy image
				if(dummyPageImageFlag){
					pdImage = PDImageXObject.createFromByteArray(pdfTraceDocument, IOUtils.toByteArray(getDocumentDummyPage()), null);	
				} else {
					pdImage = PDImageXObject.createFromByteArray(pdfTraceDocument, IOUtils.toByteArray(getDocumentPage(pageId)), null);	
				}
				
	            		//PDImageXObject.createFromFile(imgFileName, pdfTraceDocument);
	            
	            int iw = 425; // pdImage.getWidth();
	            int ih = 675; //pdImage.getHeight();
	            
	            float offsetWidth = (myPage.getMediaBox().getWidth() - iw)/2; 
	            float offsetHeight = 20f;
	            

	            try (PDPageContentStream contentStream = new PDPageContentStream(pdfTraceDocument, myPage)) {

	            	contentStream.setFont(PDType1Font.TIMES_ITALIC, DocumentMySQLDao.PDF_GENERATION_TABLE_FONT_SIZE);
	            	Table myTable;
	            	
	            	TableBuilder tableBuilder = Table.builder()
	            			// Set columns
	                        .addColumnsOfWidth(200, 200, 150);
	                        //
	                        // create the first row with group, org and user data
	            	tableBuilder = tableBuilder.addRow(Row.builder()
	                                .add(TextCell.builder().text(
	                                		allDocuments.get(i).getType().getValue().replaceAll("\t", " ") // Document Type
	                                		).font(PDF_GENERATION_TABLE_FONT_TYPE).fontSize(DocumentMySQLDao.PDF_GENERATION_TABLE_FONT_SIZE).borderWidth(1).horizontalAlignment(HorizontalAlignment.CENTER).backgroundColor(Color.LIGHT_GRAY).build())
	                                .add(TextCell.builder().text(
	                                		allDocuments.get(i).getGroupName().replaceAll("\t", " ")		// the group name
	                                		).font(PDF_GENERATION_TABLE_FONT_TYPE).fontSize(DocumentMySQLDao.PDF_GENERATION_TABLE_FONT_SIZE).borderWidth(1).horizontalAlignment(HorizontalAlignment.CENTER).backgroundColor(Color.LIGHT_GRAY).build())
	                                .add(TextCell.builder().text(
	                                		allDocuments.get(i).getOwner().replaceAll("\t", " ")		// doc owner
	                                		).font(PDF_GENERATION_TABLE_FONT_TYPE).fontSize(DocumentMySQLDao.PDF_GENERATION_TABLE_FONT_SIZE).borderWidth(1).horizontalAlignment(HorizontalAlignment.CENTER).backgroundColor(Color.LIGHT_GRAY).build())
	                                .build());
	                        //
	                        // Create second row with date page and Doc Status
	            	tableBuilder = tableBuilder.addRow(Row.builder()
	                                .add(TextCell.builder().text(
	                                		allDocuments.get(i).getStatus()
	                                		).font(PDF_GENERATION_TABLE_FONT_TYPE).fontSize(DocumentMySQLDao.PDF_GENERATION_TABLE_FONT_SIZE).borderWidth(1).horizontalAlignment(HorizontalAlignment.CENTER).textColor(Color.RED).build())
	                                .add(TextCell.builder().text(
	                                		DateUtility.extractDateOnlyFromString(allDocuments.get(i).getCreationTimestamp())
	                                		).font(PDF_GENERATION_TABLE_FONT_TYPE).fontSize(DocumentMySQLDao.PDF_GENERATION_TABLE_FONT_SIZE).borderWidth(1).build())
	                                .add(TextCell.builder().text(
	                                		"Page " + (page+1) + " of " + allDocuments.get(i).getPages().size())
	                                		.font(PDF_GENERATION_TABLE_FONT_TYPE)
	                                		.fontSize(DocumentMySQLDao.PDF_GENERATION_TABLE_FONT_SIZE)
	                                		.borderWidth(1).horizontalAlignment(HorizontalAlignment.RIGHT)
	                                		.backgroundColor(Color.GRAY).build())
	                                .build());
	                        //
	                        // Create up to 3 Doc Info Fields
	            	tableBuilder = tableBuilder.addRow(Row.builder()
	                                .add(TextCell.builder().text(
	                                		"Recipient: " + getRecipient(allDocuments.get(i))
	                                		).font(PDF_GENERATION_TABLE_FONT_TYPE).fontSize(DocumentMySQLDao.PDF_GENERATION_TABLE_FONT_SIZE).borderWidth(getBorderWidth(allDocuments.get(i))).horizontalAlignment(HorizontalAlignment.LEFT).build())
	                                .add(TextCell.builder().text(
	                                		getDynamicFieldData(allDocuments.get(i), 0)
	                                		).font(PDF_GENERATION_TABLE_FONT_TYPE).fontSize(DocumentMySQLDao.PDF_GENERATION_TABLE_FONT_SIZE).borderWidth(getBorderWidth(allDocuments.get(i))).horizontalAlignment(HorizontalAlignment.LEFT).build())
	                                .add(TextCell.builder().text(
	                                		getDynamicFieldData(allDocuments.get(i), 1)
	                                		).font(PDF_GENERATION_TABLE_FONT_TYPE).fontSize(DocumentMySQLDao.PDF_GENERATION_TABLE_FONT_SIZE).borderWidth(getBorderWidth(allDocuments.get(i))).horizontalAlignment(HorizontalAlignment.LEFT).build())
	                                .build());

	            	//
	            	// get additional doc fields
	            	if(allDocuments.get(i).getDynamicFieldData().size() > 2){
	            		int counter = 1;
	            		int rows = (int) Math.ceil((allDocuments.get(i).getDynamicFieldData().size()-2)/3.0); 
	            		
	            		for(int row = 0; row < rows; row++){
	            			tableBuilder = tableBuilder.addRow(Row.builder()
	                                .add(TextCell.builder().text(
	                                		getDynamicFieldData(allDocuments.get(i), ++counter)
	                                		).font(PDF_GENERATION_TABLE_FONT_TYPE).fontSize(DocumentMySQLDao.PDF_GENERATION_TABLE_FONT_SIZE).borderWidth(getBorderWidth(allDocuments.get(i))).horizontalAlignment(HorizontalAlignment.LEFT).build())
	                                .add(TextCell.builder().text(
	                                		getDynamicFieldData(allDocuments.get(i), ++counter)
	                                		).font(PDF_GENERATION_TABLE_FONT_TYPE).fontSize(DocumentMySQLDao.PDF_GENERATION_TABLE_FONT_SIZE).borderWidth(getBorderWidth(allDocuments.get(i))).horizontalAlignment(HorizontalAlignment.LEFT).build())
	                                .add(TextCell.builder().text(
	                                		getDynamicFieldData(allDocuments.get(i), ++counter)
	                                		).font(PDF_GENERATION_TABLE_FONT_TYPE).fontSize(DocumentMySQLDao.PDF_GENERATION_TABLE_FONT_SIZE).borderWidth(getBorderWidth(allDocuments.get(i))).horizontalAlignment(HorizontalAlignment.LEFT).build())
	                                .build());
	            		}
	            	}
	            	
	                        
	                        //
	                        // add the image
	            	tableBuilder = tableBuilder.addRow(Row.builder()
	                                .add(createAndGetPageImageCellBuilder(pdImage).colSpan(3).build())
	                                .build());
	            	myTable = tableBuilder.build();
	            	

	                // Set up the drawer
	                TableDrawer tableDrawer = TableDrawer.builder()
	                        .contentStream(contentStream)
	                        .startX(20f)
	                        .startY(myPage.getMediaBox().getUpperRightY() - 20f)
	                        .table(myTable)
	                        .build();

	                // And go for it!
	                tableDrawer.draw();
	                offsetHeight = tableDrawer.getFinalY();
	                
	                //contentStream.drawImage(pdImage, offsetWidth, offsetHeight, iw, ih);
	                
	                contentStream.beginText();
	                contentStream.setFont(PDType1Font.TIMES_ITALIC, DocumentMySQLDao.PDF_GENERATION_TABLE_FONT_SIZE);
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
	 * Generates the PDF document representation of the trace of the document collection
	 * @param docId - the id of the document to trace through
	 * @return
	 * @throws Exception
	 */
	public List<String[]> generateTraceGPSData( long docId)  throws Exception {
		// create a List which contains String array 
        List<String[]> data = new ArrayList<String[]>(); 
        data.add(new String[] { 
        		"Doc ID", "Stage", "Organization", "Owner", "Recipient", "Doc Type", "Doc Info Defintion", "Doc Info Value", "Creation Date", "Org GPS", "Doc GPS" }); 
		List<Document> allDocuments=null;
		
		//
		// get all documents for the trace
		allDocuments = getRecursiveDocumentById(docId);
		//
		// create the trace document GPS Data
		for(int i =0; i<allDocuments.size(); i++){
			
			//
			// get the necessary data from the document
			Document tempDoc = allDocuments.get(i);
			//
			// prep data
			String currDocId = "";	
			String stageName = "";
			String orgName = "";
			String owner = "";
			String recipient = "";
			String docType = "";	
			String docInfoDefinitionName = "";
			String docInfoValue = "";
			String creationDate = "";
			String orgGPS = "";
			String docGPS = "";
			
			getLog().info("<generateTraceGPSData> Document " + String.valueOf(tempDoc.getId()));
			
			if(tempDoc.getDynamicFieldData().size() > 0){
				for(int docInfoIndex = 0; docInfoIndex < tempDoc.getDynamicFieldData().size(); docInfoIndex++){
					getLog().info("<generateTraceGPSData> <doc info field> " + docInfoIndex);
					//
					// set the data
					//
					currDocId = String.valueOf(tempDoc.getId());	
					stageName = tempDoc.getGroupTypeValue();
					orgName = tempDoc.getGroupName();
					owner = tempDoc.getOwner();
					if(!tempDoc.getToRecipients().isEmpty()) {
						recipient = tempDoc.getToRecipients().get(0).getName();
					}
					
					docType = tempDoc.getDocumentType();
					docInfoDefinitionName = tempDoc.getDynamicFieldData().get(docInfoIndex).getFieldDisplayNameValue();
					docInfoValue = tempDoc.getDynamicFieldData().get(docInfoIndex).getData();
					
					creationDate = tempDoc.getCreationTimestamp();
					orgGPS = tempDoc.getGroupGPSCoordinates();
					docGPS = tempDoc.getGpsLocation();
					
					data.add(new String[] { 
							currDocId, // "Doc Id"
							stageName, //"Stage Name", 
							orgName, // "Org Name",
							owner,
							recipient, 
							docType, 
							docInfoDefinitionName, 
							docInfoValue,
							creationDate, 
							orgGPS,
							docGPS
							}); 
				}
			} else {
				//
				// set the data
				//
				currDocId = String.valueOf(tempDoc.getId());	
				stageName = tempDoc.getGroupTypeValue();
				orgName = tempDoc.getGroupName();
				owner = tempDoc.getOwner();
				if(!tempDoc.getToRecipients().isEmpty()) {
					recipient = tempDoc.getToRecipients().get(0).getName();
				}
				
				docType = tempDoc.getDocumentType();
				docInfoDefinitionName = "";
				docInfoValue = "";
				
				creationDate = tempDoc.getCreationTimestamp();
				orgGPS = tempDoc.getGroupGPSCoordinates();
				docGPS = tempDoc.getGpsLocation();
				
				data.add(new String[] { 
						currDocId, // "Doc Id"
						stageName, //"Stage Name", 
						orgName, // "Org Name",
						owner,
						recipient, 
						docType, 
						docInfoDefinitionName, 
						docInfoValue,
						creationDate, 
						orgGPS,
						docGPS
						}); 
			}

		}
		
		return data;
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
	 * Simple method to test if a DynamiC Field Definition can be deleted
	 * A definition can not be deleted if there is any data associated with it.
	 * @param definition - the definition to test
	 * @return - true if the definition can be deleted; false otherwise
	 * @throws Exception - if there were any issues
	 */
	private boolean canBeDeletedDynamicFieldDefinition(long dynamicfieldDefinitionId) throws Exception {
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
                .prepareStatement("SELECT * from dynamic_field_data " 
                		+ "WHERE parent_dynamic_field_id=?");
		// execute the statement 
		preparedSELECTstatement.setLong(1, dynamicfieldDefinitionId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        if (!resultSet.next() ) {
        	result = true;
        } 

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
		if(role.getValue().equals(Role.ROLE_NAME_MATRIX_ADMIN)){
			//
			// If its not my document
			if(!doc.getOwner().equals(user.getCredentials().getUsername())
				&& (!doc.getStatus().equals(Document.STATUS_DRAFT)
					&&	!doc.getStatus().equals(Document.STATUS_REJECTED))){
				return true;
			} 
		}
		
		if(override){
			return true;
		}
		
		if(doc.getOwner().equals(user.getCredentials().getUsername())){
			return true;
		}
		
		if(doc.getToRecipients().contains(user) 
				&& (!doc.getStatus().equals(Document.STATUS_DRAFT)
						&& !doc.getStatus().equals(Document.STATUS_REJECTED))){
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
    			documentPage.setPageNumber(++pageCounter);
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
	
	/**
	 * 
	 * @param doc
	 * @param index
	 * @return
	 */
	private String getDynamicFieldData(Document doc, int index){
		String result = "";
		
		if(doc.getDynamicFieldData().size() > index){
			return doc.getDynamicFieldData().get(index).getFieldDisplayNameValue() + ": " +  doc.getDynamicFieldData().get(index).getData();
		}
		
		return result;
	}
	
	private float getBorderWidth(Document doc){
		float result = 1f;
		
		if(doc.getDynamicFieldData().size() > 0){
			return 1f;
		}
		
		return result;
		
	}
	
	/**
	 * 
	 * @param doc
	 * @param index
	 * @return
	 */
	private String getRecipient(Document doc){
		String result = "";
		
		if(doc.getToRecipients().size() > 0){
			return doc.getToRecipients().get(0).getName();
		}
		
		return result;
	}
	
	/**
	 * Removes the ommited Pages without collating them
	 * @param oldDoc
	 * @param currentPages
	 */
	public void deleteOmmitedPages(Document oldDoc, List<DocumentPage> currentPages){
		
		Iterator<DocumentPage> it = oldDoc.getPages().iterator();
		while(it.hasNext()){
			DocumentPage page = (DocumentPage) it.next();
			if(!currentPages.contains(page)){
				try {
					deleteDocumentPage(page.getId());
				} catch (ServiceManagementException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//
		// recollate the pages
		oldDoc.setPages(currentPages);
	}	
	
	
	/*******************************************************************************************
	 * 
	 */
	
	
	/**
	 * Create a new document in the database
	 * 
	 * @param newDocument
	 * @return
	 * @throws Exception - if there was an issue
	 */
	public Screening createScreening(Screening screening, long userId) throws PersistenceException, IllegalArgumentException { 
		PreparedStatement preparedINSERTstatement;
		long returnId=0;
		
		// get the connection
		Connection conn = openConnection();
		userService.init();
		
		
		
		// create the query
		String insertQuery = " insert into screening_data ("
				+ "user_id, "
				+ "test_date, "
				+ "health_score, "
				+ "floor_number, "
				+ "station_id"
				+ ")"
		        + " values (?, ?, ?, ?, ?)";
		
		// create the statement
		try {
			preparedINSERTstatement = conn
			        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			
			// execute the statement 
			preparedINSERTstatement.setLong(1, userId);
			preparedINSERTstatement.setString(2, screening.getTestDate());
			preparedINSERTstatement.setLong(3, screening.getHealthScore());
			preparedINSERTstatement.setString(4, screening.getFloorNumber());
			preparedINSERTstatement.setString(5, screening.getStationId());
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
		screening.setId(returnId);
		getLog().info("CREATE SCREENING: " + returnId + " " + screening);


		return screening;
	}
	
	/**
	 * get all document notes for the given document
	 * 
	 * @param documentID - the id of the document for which to get the notes
	 * @return - list of sorted notes
	 * @throws Exception - if there was an issue.
	 */
	public List<Screening> fetchScreenings(long userId) throws Exception {
		List<Screening> result= new ArrayList<Screening>();
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT * "
                		+ "FROM screening_data " 
                		+ "WHERE screening_data.user_id = ? ");
		// execute the statement 
		preparedSELECTstatement.setLong(1, userId);
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractScreeningsFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
		
	}
	
	/**
	 * get all document notes for the given document
	 * 
	 * @param documentID - the id of the document for which to get the notes
	 * @return - list of sorted notes
	 * @throws Exception - if there was an issue.
	 */
	public List<Screening> fetchAllScreenings() throws Exception {
		List<Screening> result= new ArrayList<Screening>();
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT * "
                		+ "FROM screening_data sd " 
                		+ "JOIN user u ON u.id = sd.user_id ORDER BY id ASC");

		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        result = extractTotalScreeningsFromResult(resultSet);

		// release the connection
		closeConnection(conn);
				
		// return the result
		return result;
		
	}
	
	/**
	 * Create a new OCR matching text in the database
	 * 
	 * @param newDocument
	 * @return
	 * @throws Exception - if there was an issue
	 */
	public DocumentOCRMatchingData createDocumentOCRMatchingData(DocumentOCRMatchingData newOCRMatchingData) throws PersistenceException, IllegalArgumentException { 
		PreparedStatement preparedINSERTstatement;
		long returnId=0;
		
		// get the connection
		Connection conn = openConnection();
		
		// create the query
		String insertQuery = " insert into document_ocr_data ("
				+ "ocr_match_text, "
				+ "doc_type_id, "
				+ "match_type "
				+ ")"
		        + " values (?, ?, ?)";
		
		// create the statement
		try {
			preparedINSERTstatement = conn
			        .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			
			// execute the statement 
			preparedINSERTstatement.setString(1, newOCRMatchingData.getOcrMatchText());
			preparedINSERTstatement.setLong(2, newOCRMatchingData.getDocType().getId());
			preparedINSERTstatement.setString(3, newOCRMatchingData.getMatchType());
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
		newOCRMatchingData.setId(returnId);
		getLog().info("CREATE OCR Match String Data: " + returnId + " " + newOCRMatchingData);

		return newOCRMatchingData;
	}
	
	public void deleteDocumentOCRMatchingData(long documentOCRMatchingDataId) throws ServiceManagementException {
		PreparedStatement preparedDELETEStatement = null;
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the query
		String deleteQuery = " DELETE FROM document_ocr_data WHERE id = ?";
		
		// create the statement
		try {
			preparedDELETEStatement = conn
			        .prepareStatement(deleteQuery);
			
			// execute the statement 
			preparedDELETEStatement.setLong(1, documentOCRMatchingDataId);
			preparedDELETEStatement.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// re-throw the proper exception
			throw new ServiceManagementException("Error while deleting an OCR Match data with id=" + documentOCRMatchingDataId, e);
		}

		// release the connection
		closeConnection(conn);
	}	
	
	/**
	 * Update an OCR Match Data in the database
	 * 
	 * @param newDocument
	 * @return
	 * @throws Exception - if there was an issue
	 */
	public DocumentOCRMatchingData updateDocumentOCRMatchingData(DocumentOCRMatchingData oldDocumentOCRMatchingData) throws IllegalArgumentException, PersistenceException, EntityNotFoundException {
		// create the java mysql update preparedstatement
	    String query = "UPDATE document_ocr_data set ocr_match_text = ?, doc_type_id = ?, match_type = ? WHERE id = ?";
	    long docID = oldDocumentOCRMatchingData.getId(); 
	    
	    // get the connection
	 	Connection conn = openConnection();
		userService.init();
	    
	    
	    try {
	    	PreparedStatement preparedUPDATEstatement = conn.prepareStatement(query);

			// execute the statement 
	    	preparedUPDATEstatement.setString(1, oldDocumentOCRMatchingData.getOcrMatchText());
	    	preparedUPDATEstatement.setLong(2, oldDocumentOCRMatchingData.getDocType().getId());
	    	preparedUPDATEstatement.setString(3, oldDocumentOCRMatchingData.getMatchType());
	    	preparedUPDATEstatement.setLong(4, docID);
	    	preparedUPDATEstatement.executeUpdate();
			
			
		    // execute the java preparedstatement
		    preparedUPDATEstatement.executeUpdate();
		    
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// release the connection
		closeConnection(conn);
		
		return oldDocumentOCRMatchingData;
	}
	
	/**
	 * get the sync id of a document by its id
	 * 
	 * @param docSyncId - the sync id to get the actual id with
	 * @return - the sync id of the document
	 * @throws Exception - if there was an issue.
	 */
	public DocumentOCRMatchingData getDocumentOCRMatchingDataById(long documentOCRMatchingDataId) throws Exception {
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		List<DocumentOCRMatchingData> data = new ArrayList<DocumentOCRMatchingData>();
		DocumentOCRMatchingData result = null;
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT * from document_ocr_data WHERE id=?");
		preparedSELECTstatement.setLong(1, documentOCRMatchingDataId);
		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        data = extractDocumentOCRMatchingData(resultSet);
        if(data.size() > 0) {
        	result = data.get(0);
        }

		// release the connection
		closeConnection(conn);
		
				
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
	public List<DocumentOCRMatchingData> getDocumentOCRMatchingDataList() throws Exception {
		PreparedStatement preparedSELECTstatement;
		ResultSet resultSet = null;
		List<DocumentOCRMatchingData> data = new ArrayList<DocumentOCRMatchingData>();
		
		
		// get the connection
		Connection conn = openConnection();
		
		// 
		// process the request
		
		// create the statement
		preparedSELECTstatement = conn
                .prepareStatement("SELECT * from document_ocr_data");
		// execute the statement 
        resultSet = preparedSELECTstatement.executeQuery();
        
        //
        // process the result
        data = extractDocumentOCRMatchingData(resultSet);

		// release the connection
		closeConnection(conn);
		
				
		// return the result
		return data;
	}
	
	
	/**
	 * Translate SQL results into model data
	 * 
	 * @param resultSet - the result set with Doc Info Data Fields
	 * @return - converted data
	 * @throws Exception - if there was an issue.
	 */
	private List<DocumentOCRMatchingData> extractDocumentOCRMatchingData(ResultSet resultSet) throws Exception {
		List<DocumentOCRMatchingData> result = new ArrayList<DocumentOCRMatchingData>();
		userService.init();
		
		// process the extractions - there will only be one user
		//
		while (resultSet.next()) {
			DocumentOCRMatchingData data = new DocumentOCRMatchingData();
			// extract the data
			long id = resultSet.getLong("id");
			String ocrMatchText = resultSet.getString("ocr_match_text");
			long docTypeId = resultSet.getLong("doc_type_id");
			String matchType = resultSet.getString("match_type");
			
			//
			// Create Instance
			data.setId(id);
			data.setOcrMatchText(ocrMatchText);
			data.setDocType(fetchDocumentTypeData(docTypeId));
			data.setMatchType(matchType);
			//
			// add to the outgoing data
			result.add(data);
        }
		
		return result;
	}
	
	private String getWebInfPath(String filename) throws UnsupportedEncodingException {
    	String path = this.getClass().getClassLoader().getResource("").getPath();
    	String fullPath = URLDecoder.decode(path, "UTF-8");
    	String pathArr[] = fullPath.split("/WEB-INF/classes/");
    	System.out.println(fullPath);
    	System.out.println(pathArr[0]);
    	fullPath = pathArr[0];

    	String reponsePath = "";
    	// to read a file from webcontent
    	reponsePath = new File(fullPath).getPath() + File.separatorChar + filename;
    	return reponsePath;
    }
	
	private ImageCellBuilder createAndGetPageImageCellBuilder(PDImageXObject pdImage) throws IOException {
        return ImageCell.builder()
                .verticalAlignment(MIDDLE)
                .horizontalAlignment(CENTER)
                .borderWidth(0)
                .image(pdImage)
                .scale(0.4f);
    }
	
	
	/**
	 * Check if the recipient is a valid recipient for this user
	 * @param user - the user being checked against
	 * @param tempUser - the user to be checked for being a recipient
	 * @return
	 */
	private boolean isValidRecipient(User user, User tempUser ){
		boolean result = false;
		boolean selfReferentialStage = false;
		
		//
		// Do not include self
		if(user.getId() == tempUser.getId()){
			return false;
		}
		
		//
		// get the stage ids for this user
		long[] ids = user.getUserGroups().get(0).getGroupType().getAssociatedStageIds();
		
		// check if we have a self-referential id
		for(int i=0; i<ids.length; i++){
			if(ids[i] == user.getUserGroups().get(0).getId()){
				selfReferentialStage = true;
			}
		}
		
		if(selfReferentialStage) {
			//
			// Check if this recipient is in the same stage as the requesting user
			if(user.getUserGroups().get(0).getGroupType().getId() == tempUser.getUserGroups().get(0).getGroupType().getId()){
				// check that they are the exact same org
				//if(user.getUserGroups().get(0).getId() != tempUser.getUserGroups().get(0).getId()){
				//	return false;
				//}else{
				//	return true;
				//}
				return true;
			} else {
				return false;
			}
		}
		
		
		//
		// check that this recipient is in the associated stages
		for(int i=0; i<ids.length; i++){
			if(ids[i] == tempUser.getUserGroups().get(0).getGroupType().getId()){
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	private boolean isUberUser(User user, String[] list){
		boolean result = false;
		
		for(int i=0;  i<list.length; i++){
			if(list[i].equals(user.getRoles().get(0).getName())){
				return true;
			}
		}
		
		return result;
	}
	
	private PreparedStatement generateFetchAllDocsPreparedStatement(Connection conn, User user, String docType, long organizationId) throws SQLException{
		PreparedStatement preparedSELECTstatement = null;
		
		//
		//
		// Normal User Role
		if(user.getRoles().get(0).getName().equals(Role.ROLE_NAME_USER)
				|| user.getRoles().get(0).getName().equals(Role.ROLE_NAME_GENERAL_USER)
				|| user.getRoles().get(0).getName().equals(Role.ROLE_NAME_MATRIX_ADMIN)) {
			if(!docType.isEmpty()){
				getLog().debug("FETCH ALL: <getAllDocuments> with doctype");
				preparedSELECTstatement = conn
						.prepareStatement("SELECT d.id, d.doc_status, d.username, d.userid, d.creation_timestamp, d.document_type, d.updation_timestamp, "
								+ "d.updation_server_timestamp, d.document_type_id, d.sync_id, d.organization_id, d.group_id, d.type_hex_color, "
								+ "gd.name AS group_name, gd.gps_location AS group_gps_location, gd.business_id_number, "
								+ "gdt.name AS group_type_name, gdt.order_index, gdt.value AS group_type_value, dt.name, dt.value, "
								+ "dt.color_hex_code, dt.document_designation, dt.delete_flag, dld.gps_location "
								+ "FROM document d "
								+ "JOIN document_type dt ON d.document_type_id = dt.id "
								+ "JOIN group_data gd ON gd.id = d.group_id "
								+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id "
								+ "LEFT JOIN document_location_data dld ON dld.document_sync_id = d.sync_id "
								+ "WHERE organization_id = ? AND dt.document_designation = ? ");
				
				// execute the statement 
				preparedSELECTstatement.setLong(1, organizationId);
				preparedSELECTstatement.setString(2, docType);
			}else{
				getLog().debug("FETCH ALL: <getAllDocuments> *no* doctype");
				preparedSELECTstatement = conn
						.prepareStatement("SELECT d.id, d.doc_status, d.username, d.userid, d.creation_timestamp, d.document_type, d.updation_timestamp, "
								+ "d.updation_server_timestamp, d.document_type_id, d.sync_id, d.organization_id, d.group_id, d.type_hex_color, "
								+ "gd.name AS group_name, gd.gps_location AS group_gps_location, gd.business_id_number, "
								+ "gdt.name AS group_type_name, gdt.value AS group_type_value, gdt.order_index, "
								+ "dt.name, dt.value, dt.color_hex_code, dt.document_designation, dt.delete_flag, dld.gps_location "
								+ "FROM document d "
								+ "JOIN document_type dt ON d.document_type_id = dt.id "
								+ "JOIN group_data gd ON gd.id = d.group_id "
								+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id "
								+ "JOIN user_role ur ON ur.user_id = d.userid "
								+ "LEFT JOIN document_recipient_data drd ON d.id = drd.parent_doc_id "
								+ "LEFT JOIN document_location_data dld ON dld.document_sync_id = d.sync_id "
								+ "WHERE organization_id = ? AND (d.userid=? OR drd.to_user_id=?) "
								+ "GROUP BY d.id" );
				// execute the statement 
				preparedSELECTstatement.setLong(1, organizationId);
				preparedSELECTstatement.setLong(2, user.getId());
				preparedSELECTstatement.setLong(3, user.getId());
			}
		}
		
		
		
		//
		//
		// Super Admin Role User
		/*
		if(user.getRoles().get(0).getName().equals(Role.ROLE_NAME_MATRIX_ADMIN)) {
			if(!docType.isEmpty()){
				getLog().debug("FETCH ALL: <getAllDocuments> with doctype");
				preparedSELECTstatement = conn
						.prepareStatement("SELECT d.id, d.doc_status, d.username, d.userid, d.creation_timestamp, d.document_type, d.updation_timestamp, "
								+ "d.document_type_id, d.sync_id, d.organization_id, d.group_id, d.type_hex_color, "
								+ "gd.name AS group_name, gd.gps_location AS group_gps_location, gd.business_id_number, "
								+ "gdt.name AS group_type_name, gdt.order_index, gdt.value AS group_type_value, dt.name, dt.value, "
								+ "dt.color_hex_code, dt.document_designation, dt.delete_flag, dld.gps_location "
								+ "FROM document d "
								+ "JOIN document_type dt ON d.document_type_id = dt.id "
								+ "JOIN group_data gd ON gd.id = d.group_id "
								+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id "
								+ "LEFT JOIN document_location_data dld ON dld.document_sync_id = d.sync_id "
								+ "WHERE organization_id = ? AND dt.document_designation = ? ");
				
				// execute the statement 
				preparedSELECTstatement.setLong(1, organizationId);
				preparedSELECTstatement.setString(2, docType);
			}else{
				getLog().debug("FETCH ALL: <getAllDocuments> *no* doctype");
				preparedSELECTstatement = conn
						.prepareStatement("SELECT d.id, d.doc_status, d.username, d.userid, d.creation_timestamp, d.document_type, d.updation_timestamp, "
								+ "d.document_type_id, d.sync_id, d.organization_id, d.group_id, d.type_hex_color, "
								+ "gd.name AS group_name, gd.gps_location AS group_gps_location, gd.business_id_number, "
								+ "gdt.name AS group_type_name, gdt.value AS group_type_value, gdt.order_index, "
								+ "dt.name, dt.value, dt.color_hex_code, dt.document_designation, dt.delete_flag, dld.gps_location "
								+ "FROM document d "
								+ "JOIN document_type dt ON d.document_type_id = dt.id "
								+ "JOIN group_data gd ON gd.id = d.group_id "
								+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id "
								+ "JOIN user_role ur ON ur.user_id = d.userid "
								+ "LEFT JOIN document_recipient_data drd ON d.id = drd.parent_doc_id "
								+ "LEFT JOIN document_location_data dld ON dld.document_sync_id = d.sync_id "
								+ "WHERE organization_id = ? "
								+ "GROUP BY d.id");
				// execute the statement 
				preparedSELECTstatement.setLong(1, organizationId);
			}
		}
		*/
		
		//
		//
		// Super Admin Role User
		if(user.getRoles().get(0).getName().equals(Role.ROLE_NAME_SUPER_ADMIN)) {
			if(!docType.isEmpty()){
				getLog().debug("FETCH ALL: <getAllDocuments> with doctype");
				preparedSELECTstatement = conn
						.prepareStatement("SELECT d.id, d.doc_status, d.username, d.userid, d.creation_timestamp, d.document_type, d.updation_timestamp, "
								+ "d.updation_server_timestamp, d.document_type_id, d.sync_id, d.organization_id, d.group_id, d.type_hex_color, "
								+ "gd.name AS group_name, gd.gps_location AS group_gps_location, gd.business_id_number, "
								+ "gdt.name AS group_type_name, gdt.order_index, gdt.value AS group_type_value, dt.name, dt.value, "
								+ "dt.color_hex_code, dt.document_designation, dt.delete_flag, dld.gps_location "
								+ "FROM document d "
								+ "JOIN document_type dt ON d.document_type_id = dt.id "
								+ "JOIN group_data gd ON gd.id = d.group_id "
								+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id "
								+ "LEFT JOIN document_location_data dld ON dld.document_sync_id = d.sync_id "
								+ "WHERE organization_id = ? AND dt.document_designation = ? ");
				
				// execute the statement 
				preparedSELECTstatement.setLong(1, organizationId);
				preparedSELECTstatement.setString(2, docType);
			}else{
				getLog().debug("FETCH ALL: <getAllDocuments> *no* doctype");
				preparedSELECTstatement = conn
						.prepareStatement("SELECT d.id, d.doc_status, d.username, d.userid, d.creation_timestamp, d.document_type, d.updation_timestamp, "
								+ "d.updation_server_timestamp, d.document_type_id, d.sync_id, d.organization_id, d.group_id, d.type_hex_color, "
								+ "gd.name AS group_name, gd.gps_location AS group_gps_location, gd.business_id_number, "
								+ "gdt.name AS group_type_name, gdt.value AS group_type_value, gdt.order_index, "
								+ "dt.name, dt.value, dt.color_hex_code, dt.document_designation, dt.delete_flag, dld.gps_location "
								+ "FROM document d "
								+ "JOIN document_type dt ON d.document_type_id = dt.id "
								+ "JOIN group_data gd ON gd.id = d.group_id "
								+ "JOIN group_data_type gdt ON gdt.id = gd.group_data_type_id "
								+ "JOIN user_role ur ON ur.user_id = d.userid "
								+ "LEFT JOIN document_recipient_data drd ON d.id = drd.parent_doc_id "
								+ "LEFT JOIN document_location_data dld ON dld.document_sync_id = d.sync_id "
								+ "WHERE organization_id = ? "
								+ "GROUP BY d.id" );
				// execute the statement 
				preparedSELECTstatement.setLong(1, organizationId);
			}
		}
		return preparedSELECTstatement;
	}
}