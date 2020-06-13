package com.wwf.shrimp.application.client.android.services.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.common.util.JsonUtils;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.DocumentType;
import com.wwf.shrimp.application.client.android.models.dto.Organization;
import com.wwf.shrimp.application.client.android.models.localdb.dao.DocumentDao;
import com.wwf.shrimp.application.client.android.models.localdb.dao.DocumentJSONDao;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.models.view.GalleryDocumentPage;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.DateUtils;
import com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils;
import com.wwf.shrimp.application.client.android.utils.MappingUtilities;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by AleaActaEst on 20/06/2017.
 */

/**
 * Local DAO for document storage such as offline and caching with JSON payload.
 */
public class DocumentJSONService {

    private Context context;
    private SQLiteDatabase database;
    private DocumentJSONDao dbDocumentDao;
    private SessionData globalVariable;

    public DocumentJSONService(Context context){
        this.context = context;
        dbDocumentDao = new DocumentJSONDao(context);
        globalVariable  = (SessionData) context.getApplicationContext();
    }

    public void open() throws SQLException {
        database = dbDocumentDao.getWritableDatabase();
    }

    public void close() {
        dbDocumentDao.close();
    }


    public DocumentCardItem createDocument(DocumentCardItem documentEntity, String jsonPayload){
        //DateUtils.formatDateToString(smsEntity.getTimestamp()));
        // Add a new document record
        //

        /**
         * Create the Document first
         */
        ContentValues values = new ContentValues();

        // set the document name
        values.put(dbDocumentDao.DOCUMENT_NAME,
                documentEntity.getName());
        // set the JSON
        values.put(dbDocumentDao.DOCUMENT_IMAGE,
                jsonPayload);

        // set the Dc Type
        values.put(dbDocumentDao.DOCUMENT_TYPE,
                documentEntity.getDocumentType().getDocumentDesignation());
        //
        values.put(dbDocumentDao.CREATION_TIMESTAMP,
                DateUtils.formatDateTimeToString(documentEntity.getCreationTimestamp()));

        // add the owner name
        values.put(dbDocumentDao.USER_OWNER_NAME, documentEntity.getUsername());

        // add the sync id
        values.put(dbDocumentDao.SYNC_ID, documentEntity.getSyncID());


        // do the actual insertion
        long insertId = database.insert(dbDocumentDao.DOCUMENT_TABLE_NAME, null,
                values);
        // update the data
        documentEntity.setId(insertId);

        // return the updated one
        return documentEntity;

    }

    public List<DocumentCardItem> getAllMyDocumentCardItems(String username) {
        List<DocumentCardItem> documentEntities = new ArrayList<DocumentCardItem>();

        Cursor cursor = database.query(DocumentDao.DOCUMENT_TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DocumentDao.ID);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            // convert the cursor data
            DocumentCardItem documentEntity = cursorToDocumentCardItemEntity(cursor);
            if(documentEntity.getUsername().equals(username)) {
                documentEntities.add(documentEntity);
            }

            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        // sort the entities by their date
        Collections.sort(documentEntities, new Comparator<DocumentCardItem>() {
            @Override
            public int compare(DocumentCardItem lhs, DocumentCardItem rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getCreationTimestamp().after(rhs.getCreationTimestamp()) ? -1 : (lhs.getCreationTimestamp().before(rhs.getCreationTimestamp()) ) ? 1 : 0;
            }
        });

        return documentEntities;
    }

    public List<Document> getAllMyDocuments(String username) {
        List<Document> documentEntities = new ArrayList<Document>();

        List<String> documentSyncIds = new ArrayList<String>();
        String[] tableColumns = new String[] {
                "document_image",
                "document_type"
        };
        String whereClause = "sync_id = ?";




        //
        // get all ids first
        List<String> syncIds = getAllDocIds(username);

        for(int i=0; i< syncIds.size(); i++){
            String[] whereArgs = new String[] {
                    syncIds.get(i)
            };
            Cursor cursor = database.query(DocumentDao.DOCUMENT_TABLE_NAME,
                    tableColumns,
                    whereClause,
                    whereArgs,
                    null,
                    null,
                    DocumentDao.ID);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                // convert the cursor data
                Document documentEntity = cursorToDocumentEntity(cursor);
                documentEntities.add(documentEntity);
                cursor.moveToNext();
            }
            // make sure to close the cursor
            cursor.close();
            SQLiteDatabase.releaseMemory();
        }


        // sort the entities by their date
        Collections.sort(documentEntities, new Comparator<Document>() {
            @Override
            public int compare(Document lhs, Document rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getCreationTimestamp().compareTo(rhs.getCreationTimestamp());
            }
        });

        return documentEntities;
    }

    /**
     * Delete the document by its sync id
     * @param syncID -  the sync id of the document
     * @return - true if the operation was a success; false otherwise.
     */
    public boolean deleteDocument(String syncID){
        boolean documentDeleteSuccess;

        documentDeleteSuccess =  database.delete(DocumentDao.DOCUMENT_TABLE_NAME,
                DocumentDao.SYNC_ID + "=\"" + syncID + "\"" , null) > 0;

        return documentDeleteSuccess;
    }

    /**
     * Delete the document by its sync id
     * @param username -  the user name
     * @return - true if the operation was a success; false otherwise.
     */
    public String getNextDocumentJSONToSync(String username){
        String docJSON = null;
        String[] tableColumns = new String[] {
                "document_image",
                "document_type"
        };
        String whereClause = "sync_id = ?";
        //
        // get all ids first
        List<String> syncIds = getAllDocIds(username);

        if(syncIds.size() > 0) {
            String[] whereArgs = new String[]{
                    syncIds.get(0)
            };
            Cursor cursor = database.query(DocumentDao.DOCUMENT_TABLE_NAME,
                    tableColumns,
                    whereClause,
                    whereArgs,
                    null,
                    null,
                    DocumentDao.ID);
            if(cursor.moveToFirst()) {
                // convert the cursor data
                docJSON  = cursorToDocumentJSONEntity(cursor);
            }
            // make sure to close the cursor
            cursor.close();
            SQLiteDatabase.releaseMemory();

            deleteDocument(syncIds.get(0));
        }
        return docJSON;
    }

    public List<DocumentCardItem> getAllOtherDocuments(String username) {
        List<DocumentCardItem> documentEntities = new ArrayList<DocumentCardItem>();

        Cursor cursor = database.query(DocumentDao.DOCUMENT_TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DocumentDao.ID);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DocumentCardItem documentEntity = cursorToDocumentCardItemEntity(cursor);
            if(!documentEntity.getUsername().equals(username)) {
                documentEntities.add(documentEntity);
                // get associated pages
                documentEntity.setImagePages(getAllDocumentPages(documentEntity.getSyncID()));
            }
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        // sort the entities by their date
        Collections.sort(documentEntities, new Comparator<DocumentCardItem>() {
            @Override
            public int compare(DocumentCardItem lhs, DocumentCardItem rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getCreationTimestamp().after(rhs.getCreationTimestamp()) ? -1 : (lhs.getCreationTimestamp().before(rhs.getCreationTimestamp()) ) ? 1 : 0;
            }
        });

        return documentEntities;
    }

    /***********************************************************************************************
     Helper methods
     */

    /**
     *
     * @param cursor
     * @return
     */
    private DocumentCardItem cursorToDocumentCardItemEntity(Cursor cursor) {
        DocumentCardItem documentEntity;

        // get the JSON document
        String jsonDocument = cursor.getString(2);
        Document doc = (Document) RESTUtils.getObjectFromJSON(jsonDocument, Document.class);
        List<Document> docs = new ArrayList<Document>();
        docs.add(doc);

        documentEntity = DocumentPOJOUtils.convertDocuments(docs, globalVariable, null, false).get(0);
        documentEntity.setBackendSynced(false);

        return documentEntity;
    }

    /**
     *
     * @param cursor
     * @return
     */
    private Document cursorToDocumentEntity(Cursor cursor) {

        // get the JSON document
        String jsonDocument = cursor.getString(0);
        Document doc = (Document) RESTUtils.getObjectFromJSON(jsonDocument, Document.class);

        return doc;
    }

    /**
     *
     * @param cursor
     * @return
     */
    private String cursorToDocumentJSONEntity(Cursor cursor) {

        // get the JSON document
        String jsonDocument = cursor.getString(0);

        return jsonDocument;
    }

    private List<String> getAllDocIds(String username){
        List<String> documentSyncIds = new ArrayList<String>();
        String[] tableColumns = new String[] {
                "sync_id",
                "user_owner"
        };
        String whereClause = "user_owner = ?";
        String[] whereArgs = new String[] {
                username
        };

        Cursor cursor = database.query(DocumentDao.DOCUMENT_TABLE_NAME,
                tableColumns,
                whereClause,
                whereArgs,
                null,
                null,
                DocumentDao.ID);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String documentSyncId = cursorToDocumentSyncId(cursor);
            documentSyncIds.add(documentSyncId);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return documentSyncIds;
    }
    private List<GalleryDocumentPage> getAllDocumentPages(String syncID){
        List<GalleryDocumentPage> documentPages = new ArrayList<GalleryDocumentPage>();

        // the condition for the row(s) you want returned.
        String where = DocumentDao.PAGE_IMAGE_DOCUMENT_PAGE_FOREIGN_SYNC_ID +"=?";
        String[] whereArgs = new String[] {
                // The value of the column specified above for the rows to be included
                syncID
        };


        Cursor cursor = database.query(DocumentDao.DOCUMENT_PAGE_TABLE_NAME,
                null,
                where,
                whereArgs,
                null,
                null,
                DocumentDao.PAGE_IMAGE_DOCUMENT_PAGE_FOREIGN_SYNC_ID);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            // convert the cursor data
            File imageFile = cursorToDocumentPage(cursor);
            documentPages.add(new GalleryDocumentPage(imageFile));
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return documentPages;
    }

    private File cursorToDocumentPage(Cursor cursor) {
        File imageFile = null;
        imageFile = new File(cursor.getString(1));

        return imageFile;
    }

    private String cursorToDocumentSyncId(Cursor cursor) {
        String syncId = null;
        syncId = cursor.getString(0);

        return syncId;
    }

    private boolean deletePages(String syncID){
        return database.delete(DocumentDao.DOCUMENT_PAGE_TABLE_NAME,
                DocumentDao.PAGE_IMAGE_DOCUMENT_PAGE_FOREIGN_SYNC_ID + "=\"" + syncID + "\"" , null) > 0;

    }

    private String getGroupTypeName(){
        String result = "N/A";
        // get the ids og the organization and the group
        long orgId = globalVariable.getCurrentUser().getUserOrganizations().get(0).getId();
        long groupId = globalVariable.getCurrentUser().getUserGroups().get(0).getId();

        int orgIndex = -1;
        for(int i=0; i< globalVariable.getCurrentUser().getUserOrganizations().size(); i++){
            if(globalVariable.getCurrentUser().getUserOrganizations().get(i).getId() == orgId){
                orgIndex = i;
            }
        }
        Organization tempOrg = globalVariable.getCurrentUser().getUserOrganizations().get(orgIndex);
        for(int i=0; i < tempOrg.getSubGroups().size(); i++){
            if(tempOrg.getSubGroups().get(i).getId() == groupId){
                result = tempOrg.getSubGroups().get(i).getName();
            }
        }
        return result;
    }
}
