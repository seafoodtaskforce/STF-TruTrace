package com.wwf.shrimp.application.client.android.services.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.wwf.shrimp.application.client.android.models.dto.DocumentType;
import com.wwf.shrimp.application.client.android.models.dto.Organization;
import com.wwf.shrimp.application.client.android.models.localdb.dao.DocumentDao;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.models.view.GalleryDocumentPage;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.DateUtils;
import com.wwf.shrimp.application.client.android.utils.MappingUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by AleaActaEst on 20/06/2017.
 */

/**
 * Local Document DAO Service for CRUD operations
 */
public class DocumentService {

    private Context context;
    private SQLiteDatabase database;
    private DocumentDao dbDocumentDao;
    private SessionData globalVariable;

    public DocumentService(Context context){
        this.context = context;
        dbDocumentDao = new DocumentDao(context);
        globalVariable  = (SessionData) context.getApplicationContext();
    }

    public void open() throws SQLException {
        database = dbDocumentDao.getWritableDatabase();
    }

    public void close() {
        dbDocumentDao.close();
    }


    public DocumentCardItem createDocument(DocumentCardItem documentEntity){
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
        // set the document path
        /**
        values.put(dbDocumentDao.DOCUMENT_IMAGE,
                // documentEntity.getImagePath().getAbsolutePath());
                documentEntity.getImagePages().get(0).getAbsolutePath());
         */
        // set the message text
        values.put(dbDocumentDao.DOCUMENT_TYPE,
                documentEntity.getType());
        //
        values.put(dbDocumentDao.CREATION_TIMESTAMP,
                DateUtils.formatDateTimeToString(documentEntity.getCreationTimestamp()));

        // add the color coding
        values.put(dbDocumentDao.DOCUMENT_COLOR_CODE, documentEntity.getTypeHEXColor());

        // add the owner name
        values.put(dbDocumentDao.USER_OWNER_NAME, documentEntity.getUsername());

        // add the document type id
        values.put(dbDocumentDao.DOCUMENT_TYPE_ID, documentEntity.getDocumentType().getId());

        // add the sync id
        values.put(dbDocumentDao.SYNC_ID, documentEntity.getSyncID());


        // do the actual insertion
        long insertId = database.insert(dbDocumentDao.DOCUMENT_TABLE_NAME, null,
                values);
        // update the data
        documentEntity.setId(insertId);

        /**
         * Next create all the pages
         */
        createDocumentPages(documentEntity);

        // return the updated one
        return documentEntity;

    }

    public List<DocumentCardItem> getAllMyDocuments(String username) {
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

    /**
     * Delete the document by its sync id
     * @param syncID -  the sync id of the document
     * @return - true if the operation was a success; false otherwise.
     */
    public boolean deleteDocument(String syncID){
        boolean pagesDeleteSuccess;
        boolean documentDeleteSuccess;

        pagesDeleteSuccess = deletePages(syncID);
        documentDeleteSuccess =  database.delete(DocumentDao.DOCUMENT_TABLE_NAME,
                DocumentDao.SYNC_ID + "=\"" + syncID + "\"" , null) > 0;

        return pagesDeleteSuccess && documentDeleteSuccess;
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
        DocumentType docType = new DocumentType();
        DocumentCardItem documentEntity = new DocumentCardItem();
        docType.setId(cursor.getLong(8));
        docType.setValue(context.getResources().getString(MappingUtilities.documentTypeIDToRTypeMap.get(docType.getId())));

        documentEntity.setId(cursor.getLong(0));
        documentEntity.setUsername(cursor.getString(1));
        documentEntity.setName(cursor.getString(4));
        // documentEntity.setImagePath(new File(cursor.getString(3)));
        // <TODO> change this to the exact number of pages fetched from the database
        // documentEntity.getImagePages().add(new File(cursor.getString(3)));
        documentEntity.setCreationTimestamp(DateUtils.formatStringToDateTime(cursor.getString(5)));
        documentEntity.setType(cursor.getString(7));
        documentEntity.setTypeHEXColor(cursor.getString(6));
        documentEntity.setDocumentType(docType);
        documentEntity.setSyncID(cursor.getString(9));
        documentEntity.setGroupType(getGroupTypeName());

        return documentEntity;
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
    /**
     * Helper method to add page entries for the document
     * @param documentEntity
     */
    private void createDocumentPages(DocumentCardItem documentEntity){
        ContentValues values = null;

        //
        // for each page in the document
        for(int i=0; i < documentEntity.getImagePages().size(); i++){
            values = new ContentValues();

            // set the document foreign key id
            values.put(dbDocumentDao.PAGE_IMAGE_DOCUMENT_PAGE_FOREIGN_ID,
                    documentEntity.getId());

            // set the document foreign sync id
            values.put(dbDocumentDao.PAGE_IMAGE_DOCUMENT_PAGE_FOREIGN_SYNC_ID,
                    documentEntity.getSyncID());

            // set the page file path
            values.put(dbDocumentDao.PAGE_IMAGE_DOCUMENT_PAGE,
                    ((File)documentEntity.getImagePages().get(i).getPage()).getAbsolutePath());

            // set the page index
            values.put(dbDocumentDao.PAGE_IMAGE_DOCUMENT_PAGE_NUMBER,
                    i);

            // insert the page
            database.insert(dbDocumentDao.DOCUMENT_PAGE_TABLE_NAME, null,
                    values);

        }
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
