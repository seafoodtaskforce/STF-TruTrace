package com.wwf.shrimp.application.client.android.utils;

import androidx.fragment.app.Fragment;
import android.util.Log;

import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.adapters.DocumentCardItemAdapter;
import com.wwf.shrimp.application.client.android.adapters.helpers.LinkedDocumentItemSelectionDataHelper;
import com.wwf.shrimp.application.client.android.dialogs.TabbedDocumentDialog;
import com.wwf.shrimp.application.client.android.fragments.AllDocumentsFragment;
import com.wwf.shrimp.application.client.android.fragments.MyDocumentsFragment;
import com.wwf.shrimp.application.client.android.fragments.ProfileDocumentsFragment;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.DocumentPage;
import com.wwf.shrimp.application.client.android.models.dto.DocumentType;
import com.wwf.shrimp.application.client.android.models.dto.DynamicFieldData;
import com.wwf.shrimp.application.client.android.models.dto.DynamicFieldDefinition;
import com.wwf.shrimp.application.client.android.models.dto.NoteData;
import com.wwf.shrimp.application.client.android.models.dto.Organization;
import com.wwf.shrimp.application.client.android.models.dto.TagData;
import com.wwf.shrimp.application.client.android.models.dto.User;
import com.wwf.shrimp.application.client.android.models.dto.search.DocumentSearchCriteria;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.models.view.GalleryDocumentPage;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.opennotescanner.helpers.Utils;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Utility for Document POJO functionality such as mapping to other entities.
 * Created by AleaActaEst on 12/23/2017.
 */

public class DocumentPOJOUtils {

    public static int PAGE_SOURCE_DOCUMENT = 1;
    public static int PAGE_SOURCE_GALLERY = 2;
    public static String LOG_TAG = "DocumentPOJOUtils";
    public static String DOC_NOTE_HEADER_DELIMITER = ";;;;";


    public static List<DocumentCardItem> convertDocuments(List<Document> documents, SessionData globalVariable, Fragment fragment, boolean convertWithoutCurrentUser){
        List<DocumentCardItem> result = new ArrayList<>();
        if(documents == null){
            return result;
        }

        String customTagPrefix = globalVariable.getConfigurationData().getGlobalConstantTaggingCustomPrefix();

        for(int i=0; i< documents.size(); i++){
            //
            // Omit documents from this user if the flag is set
            if(convertWithoutCurrentUser == true){
                if(documents.get(i).getOwner().equals(globalVariable.getCurrentUser().getCredentials().getUsername())){
                    continue;
                }
            }

            //
            // check for doc type
            if(fragment instanceof AllDocumentsFragment){
                if(documents.get(i).getStatus().equals(Document.STATUS_DRAFT)){
                    continue;
                }
            }
            //
            // check for doc type
            if(fragment instanceof AllDocumentsFragment){
                if(documents.get(i).getStatus().equals(Document.STATUS_REJECTED)
                        && !documents.get(i).getToRecipients().contains(globalVariable.getCurrentUser())){
                    continue;
                }
            }
            //
            // Check stages 1 up and 1 down
            //
            // check for doc type
            if(fragment instanceof AllDocumentsFragment){
                if(documents.get(i).getGroupId() != globalVariable.getCurrentUser().getUserGroups().get(0).getId()
                        && !documents.get(i).getToRecipients().contains(globalVariable.getCurrentUser())){
                    continue;
                }
            }


            ////////////////////////////////////////////////////////////////////////////////////////
            // Convert
            DocumentCardItem cardItem = new DocumentCardItem();
            //
            //
            // String documentType = fragment.getResources().getString(MappingUtilities.documentTypeIDToRTypeMap.get(documents.get(i).getType().getId()));
            String documentType = documents.get(i).getDocumentType();
            cardItem.setType(documentType);
            documents.get(i).getType().setValue(documentType);
            cardItem.setUsername(documents.get(i).getOwner());
            cardItem.setCreationTimestamp(DateUtils.formatStringToDateTime(documents.get(i).getCreationTimestamp()));
            cardItem.setTypeHEXColor(documents.get(i).getTypeHEXColor());
            cardItem.setId(documents.get(i).getId());
            cardItem.setDocumentType(documents.get(i).getType());
            cardItem.setSyncID(documents.get(i).getSyncID());
            cardItem.setWasRead(documents.get(i).isCurrentUserRead());
            //
            // Add any pages
            cardItem.setDocumentPages(DocumentPOJOUtils.convertDocumentPagesToGalleryPages(documents.get(i).getPages()));
            //
            // Add tags
            documents.get(i).setTags(convertCustomTags(documents.get(i).getTags(), customTagPrefix));
            cardItem.setTags(documents.get(i).getTags());
            //
            // Add linked docs
            cardItem.setLinkedDocuments(documents.get(i).getLinkedDocuments());
            //
            // Add Attached Docs
            cardItem.setAttachedDocuments(documents.get(i).getAttachedDocuments());
            //
            // Add Recipients
            cardItem.setRecipients(documents.get(i).getToRecipients());

            //
            //  Add Dynamic Document Data
            cardItem.setDynamicFieldData(documents.get(i).getDynamicFieldData());
            //
            // add group information
            long groupId = documents.get(i).getGroupId();
            long OrganizationId = documents.get(i).getOrganizationId();
            // find the proper value
            cardItem.setGroupType(DocumentPOJOUtils.getGroupTypeName(OrganizationId, groupId, globalVariable));
            cardItem.setGroupName(DocumentPOJOUtils.getGroupName(OrganizationId, groupId, globalVariable));
            // is synced?
            if(documents.get(i).getId() <= 0){
                cardItem.setBackendSynced(false);
            }

            //
            // Add status and any data on rejection notes
            cardItem.setStatus(documents.get(i).getStatus());
            cardItem.setNotes(documents.get(i).getNotes());

            result.add(cardItem);
        }

        return result;
    }

    public static String getGroupTypeName(long orgId, long groupId, SessionData globalVariable){
        String result = "N/A";
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

    public static String getGroupName(long orgId, long groupId, SessionData globalVariable){
        String result = "N/A";
        int orgIndex = -1;
        for(int i=0; i< globalVariable.getCurrentUser().getUserOrganizations().size(); i++){
            if(globalVariable.getCurrentUser().getUserOrganizations().get(i).getId() == orgId){
                orgIndex = i;
            }
        }
        Organization tempOrg = globalVariable.getCurrentUser().getUserOrganizations().get(orgIndex);
        for(int i=0; i < tempOrg.getSubGroups().size(); i++){
            if(tempOrg.getSubGroups().get(i).getId() == groupId){
                result = tempOrg.getSubGroups().get(i).getGroupType().getName();
            }
        }
        return result;
    }

    /**
     * Detect the data mode hfor document persistence
     * @param globalVariable - the session global variable
     * @return - the mode for the localization of data mode
     */
    public static int detectDataMode(SessionData globalVariable){

        if(globalVariable.getNextDocument().getDocumentPages() != null
                && globalVariable.getNextDocument().getDocumentPages().size() > 0){
            return TabbedDocumentDialog.DATA_MODE_REMOTE;
        }

        if(globalVariable.getNextDocument().getImagePages() != null
                && globalVariable.getNextDocument().getImagePages().size() > 0){
            return TabbedDocumentDialog.DATA_MODE_LOCAL;
        }

        if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_CREATION){
            return TabbedDocumentDialog.DATA_MODE_LOCAL;
        }

        return TabbedDocumentDialog.DATA_MODE_BOTH;
    }

    public static List<TagData> convertCustomTags(List<TagData> tags, String customTagPrefix){

        for(int i=0; i< tags.size(); i++){
            if(tags.get(i).getText().contains(customTagPrefix)){
                String tagText = tags.get(i).getText();
                tags.get(i).setText(tagText.replace(customTagPrefix, "").trim());
                tags.get(i).setCustom(true);
            }
        }

        return tags;
    }

    /**
     * Helper method to find a custom tag (which will be displayed in the card)
     * @param tags - the list of tags to search through
     * @return - the tag or a null string if not found.
     */
    public static String getCustomTag(List<TagData> tags){
        String result = "";

        //
        // Precondition
        if(tags == null) return result;

        //
        // Processing
        for(int i=0; i< tags.size(); i++){
            if(tags.get(i).isCustom()){
                return tags.get(i).getText();
            }
        }
        return result;
    }

    /**
     * get all the dynamic fields for the current document
     * @return - list of document fields; empty if none found.
     */
    public static List<DynamicFieldDefinition> getAllDynamicFieldsForDocument(SessionData globalVariable, DocumentCardItem doc) {
        List<DynamicFieldDefinition> result = new ArrayList<DynamicFieldDefinition>();
        long docTypeId = doc.getDocumentType().getId();

        for(int i=0; i< globalVariable.getCurrentUser().getDynamicFieldDefinitions().size() ;i++){
            if(globalVariable.getCurrentUser().getDynamicFieldDefinitions().get(i).getDocTypeId() == docTypeId){
                result.add(globalVariable.getCurrentUser().getDynamicFieldDefinitions().get(i));
            }
        }
        return result;
    }

    /**
     * get all the dynamic fields for the current document
     * @return - list of document fields; empty if none found.
     */
    public static List<DynamicFieldDefinition> getAllOCRDynamicFieldsForDocument(SessionData globalVariable, DocumentCardItem doc) {
        List<DynamicFieldDefinition> result = new ArrayList<DynamicFieldDefinition>();
        long docTypeId = doc.getDocumentType().getId();

        for(int i=0; i< globalVariable.getCurrentUser().getDynamicFieldDefinitions().size() ;i++){
            if(globalVariable.getCurrentUser().getDynamicFieldDefinitions().get(i).getDocTypeId() == docTypeId){
                if( globalVariable.getCurrentUser().getDynamicFieldDefinitions().get(i).getOcrGrabLength() > 0) {
                    result.add(globalVariable.getCurrentUser().getDynamicFieldDefinitions().get(i));
                }
            }
        }
        return result;
    }

    /**
     * get all the dynamic fields for the input document
     * @return - list of document fields; empty if none found.
     */
    public static List<DynamicFieldData> getAllDynamicFieldData(DocumentCardItem doc) {
        List<DynamicFieldData> result = new ArrayList<DynamicFieldData>();

        for(int i=0; i< doc.getDynamicFieldData().size() ;i++){
            //if(globalVariable.getCurrentUser().getDynamicFieldData().get(i)){
            result.add(doc.getDynamicFieldData().get(i));
            //}
        }

        return result;
    }

    /**
     * Will check the if this document has an expiry data field
     * @param doc - the document to get the expiry field for
     * @return - true if the document has an expiry date; false of not
     */
    public static boolean hasExpiryDate(SessionData globalVariable, DocumentCardItem doc) {
        List<DynamicFieldDefinition> definitions = DocumentPOJOUtils.getAllDynamicFieldsForDocument(globalVariable, doc);

        if(!doc.getDocumentType().getDocumentDesignation().equals(DocumentType.DESIGNATION_PROFILE)) {
            return false;
        }
        List<DynamicFieldData> fields = doc.getDynamicFieldData();
        for(int i=0; i< fields.size() ;i++){
            for(int j=0; j< definitions.size() ;j++) {
                if (fields.get(i).getDynamicFieldDefinitionId() == definitions.get(j).getId()
                        && definitions.get(j).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Will check the if this document has an expiry data field
     * @param doc - the document to get the expiry field for
     * @return - null of the date is not found; an actual date otherwise
     */
    public static Date getExpiryDate(SessionData globalVariable, DocumentCardItem doc) {
        List<DynamicFieldDefinition> definitions = DocumentPOJOUtils.getAllDynamicFieldsForDocument(globalVariable, doc);

        if(!doc.getDocumentType().getDocumentDesignation().equals(DocumentType.DESIGNATION_PROFILE)) {
            return null;
        }
        List<DynamicFieldData> fields = doc.getDynamicFieldData();
        for(int i=0; i< fields.size() ;i++){
            for(int j=0; j< definitions.size() ;j++) {
                if (fields.get(i).getDynamicFieldDefinitionId() == definitions.get(j).getId()
                        && definitions.get(j).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE) {
                    return DateUtils.formatStringToDateOnly(fields.get(i).getData());
                }
            }
        }
        return null;
    }

    /**
     * Will check the if this document has an expiry data field
     * @param doc - the document to get the expiry field for
     * @return - null of the date is not found; an actual date otherwise
     */
    public static String getExpiryDateAsString(SessionData globalVariable, DocumentCardItem doc) {
        List<DynamicFieldDefinition> definitions = DocumentPOJOUtils.getAllDynamicFieldsForDocument(globalVariable, doc);

        if(!doc.getDocumentType().getDocumentDesignation().equals(DocumentType.DESIGNATION_PROFILE)) {
            return null;
        }
        List<DynamicFieldData> fields = doc.getDynamicFieldData();
        for(int i=0; i< fields.size() ;i++){
            for(int j=0; j< definitions.size() ;j++) {
                if (fields.get(i).getDynamicFieldDefinitionId() == definitions.get(j).getId()
                        && definitions.get(j).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE) {
                    return fields.get(i).getData();
                }
            }
        }
        return null;
    }

    public static long getDifferenceBetweenDatesDays(Date date1, Date date2) {
        long diff = date1.getTime() - date2.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }


    /**
     * Helper method to get and format the main doc field for the document
     * @param fields - the document fields
     * @return - the formatted doc field string or empty string if not found
     */
    public static String getFormattedDocFields(List<DynamicFieldData> fields, SessionData globalVariable){
        String result = "";
        String displayName = "";

        //
        // Precondition
        if(fields == null || fields.size() == 0) return result;

        //
        // Processing
        if(fields.get(0).getFieldDisplayNameValue() == null){
            displayName = getFieldDisplayName(globalVariable, fields.get(0).getDynamicFieldDefinitionId());
        }else{
            displayName = fields.get(0).getFieldDisplayNameValue();
        }
        result = displayName + ": " + fields.get(0).getData();
        return result;
    }

    /**
     * Localized search filter execution
     * @param globalVariable - session data
     * @param adapterKey - adapter key to use when getting the recycler adapter
     */
    public static void filterDocuments(SessionData globalVariable, String adapterKey ){
        List<DocumentCardItem> result = new ArrayList<>();
        // get unfiltered docs
        DocumentCardItemAdapter mAdapter = globalVariable.getDocumentAdapter(adapterKey);
        List<DocumentCardItem> filterList = globalVariable.getDocumentLocalMap().get(adapterKey);
        boolean filterOwner = false;

        DocumentSearchCriteria search = null;

        if(adapterKey.equals(AllDocumentsFragment.RECYCLER_ADAPTER_KEY)){
            search = globalVariable.getDocumentSearchFilterData(SessionData.DOCUMENT_FILTER_ALL_DOCS);
        }
        if(adapterKey.equals(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)){
            search = globalVariable.getDocumentSearchFilterData(SessionData.DOCUMENT_FILTER_MY_DOCS);
            filterOwner = true;
        }
        if(adapterKey.equals(ProfileDocumentsFragment.RECYCLER_ADAPTER_KEY)){
            search = globalVariable.getDocumentSearchFilterData(SessionData.DOCUMENT_FILTER_ALL_DOCS);
        }

        //
        // filter the data
        for(int i=0; i<filterList.size(); i++){
            // by user name
            if(search != null && search.getUserName() != null){
                if(!filterList.get(i).getUsername().equals(search.getUserName())){
                    // skip to next
                    continue;
                }
            }

            // by doc type
            if(search != null && search.getDocType() != null){
                if(!filterList.get(i).getDocumentType().equals(search.getDocType())){
                    // skip to next
                    continue;
                }
            }

            // ensure that we filter also by owner for my docs
            if(filterOwner){
                if(!filterList.get(i).getUsername().equals(globalVariable.getCurrentUser().getName())){
                    // skip to next
                    continue;
                }
            }
            result.add(filterList.get(i));
        }

        //
        // Set the new data set into the recycler view

        mAdapter.getDataSet().clear();
        mAdapter.getDataSet().addAll(result);
        mAdapter.notifyDataSetChanged();
    }

    public static int decideImageSource(SessionData globalVariable, int mPosition){
        if(globalVariable.getNextDocument().getDocumentPages().size() ==0
                && globalVariable.getNextDocument().getImagePages().size() ==0){
            return PAGE_SOURCE_DOCUMENT;
        }
        if(mPosition <= globalVariable.getNextDocument().getDocumentPages().size()-1 ){
            return PAGE_SOURCE_DOCUMENT;
        }
        if(mPosition > globalVariable.getNextDocument().getDocumentPages().size()-1 ){
            return PAGE_SOURCE_GALLERY;
        }
        return PAGE_SOURCE_DOCUMENT;
    }

    public static int calculateImagePosition(SessionData globalVariable, int mPosition){

        if(globalVariable.getNextDocument().getDocumentPages().size() ==0
                || globalVariable.getNextDocument().getImagePages().size() ==0){
            return mPosition;
        }
        if(mPosition <= globalVariable.getNextDocument().getDocumentPages().size()-1 ){
            return mPosition;
        }
        if(mPosition > globalVariable.getNextDocument().getDocumentPages().size()-1 ){
            return mPosition - globalVariable.getNextDocument().getDocumentPages().size();
        }
        return mPosition;
    }

    /**
     * Convert the inner Document Card based data into the JSON ready Document DTO data
     * @param item - the document card item internal to the application
     * @param globalVariable - the session context data
     * @return - the equivalent Document entity
     */
    public static Document convertDTO(DocumentCardItem item, SessionData globalVariable){
        Document document = new Document();
        long organizationId=0;
        long groupId=0;

        //
        // main document-level data
        //
        document.setId(item.getId());
        document.setSyncID(item.getSyncID());
        //document.setCreationTimestamp(DateUtils.formatDateTimeToString(item.getCreationTimestamp()));
        document.setCreationTimestamp(DateUtils.formatDateTimeToString(new Date()));
        // document type
        document.setDocumentType(item.getType());
        // get the mapped document type
        document.setType(item.getDocumentType());
        // color data
        document.setTypeHEXColor(item.getTypeHEXColor());
        // owner
        document.setOwner(item.getUsername());
        // get the organization and the group id
        User user = globalVariable.getCurrentUser();
        if(user.getUserOrganizations().size() > 0) {
            organizationId = user.getUserOrganizations().get(0).getId();
            groupId = user.getUserGroups().get(0).getId();
        }
        document.setOrganizationId(organizationId);
        document.setGroupId(groupId);

        // set the status
        document.setStatus(item.getStatus());

        // ste pages as synced
        for(int i=0; i<item.getDocumentPages().size(); i++){
            ((DocumentPage)item.getDocumentPages().get(i).getPage()).setPageSynced(true);
            document.getPages().add((DocumentPage)item.getDocumentPages().get(i).getPage());
            // set page number
            document.getPages().get(document.getPages().size()-1).setPageNumber(item.getDocumentPages().get(i).getPageNumber());
        }

        // create image encoding for each page
        for(int i=0; i<item.getImagePages().size(); i++){
            DocumentPage page = new DocumentPage();
            page.setPageSynced(false);
            byte[] base64ImageData=null;
            try {
                Log.d(LOG_TAG, "Create a new document - Loading image page with SIZE: " +
                        FileUtils.fileSize((File)item.getImagePages().get(i).getPage()));
                base64ImageData = org.apache.commons.io.FileUtils.readFileToByteArray((File)item.getImagePages().get(i).getPage());
            }catch(Exception e){
                System.out.println("Cannot convert image data to base 64");
                e.printStackTrace();
            }
            //
            // Get the serialized version of the file
            String base64ImageDataString =  RESTUtils.customGson.toJson(base64ImageData);
            // set data
            page.setBase64ImageData(base64ImageDataString);

            //
            // assign page number to the encoded page for the backend
            // page.setPageNumber(document.getPages().size());
            page.setPageNumber(item.getImagePages().get(i).getPageNumber());
            //  Log.d(LOG_TAG, "Create a new document - page designation " +
            //        page.getPageNumber());

            // add to document
            document.getPages().add(page);
        }

        // recollate pages
        // <TODO> check if need to recollate
        //document.setPages(DocumentPOJOUtils.collateDocPages(document.getPages()));

        document.setAttachedDocuments(item.getAttachedDocuments());
        document.setLinkedDocuments(item.getLinkedDocuments());
        document.setTags(item.getTags());
        document.setToRecipients(item.getRecipients());
        document.setDynamicFieldData(item.getDynamicFieldData());

        return document;
    }

    public static List<DocumentPage> collateDocPages(List<DocumentPage> pages) {
        int pageCounter = 1;

        // sort the pages first based on position

        for(int i=0; i< pages.size(); i++){
            pages.get(i).setPageNumber(pageCounter++);
        }

        return pages;
    }

    public static List<GalleryDocumentPage> collateGalleryDocPages(List<GalleryDocumentPage> pages) {
        int pageCounter = 1;

        // sort the pages first based on position

        for(int i=0; i< pages.size(); i++){
            pages.get(i).setPageNumber(pageCounter++);
        }

        return pages;
    }

    public static List<GalleryDocumentPage>  collatePages(List<GalleryDocumentPage> pageData){
        // sort the entities by their type
        Collections.sort(pageData, new Comparator<GalleryDocumentPage>() {
            @Override
            public int compare(GalleryDocumentPage lhs, GalleryDocumentPage rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                if(lhs.getPageNumber() < rhs.getPageNumber()){
                    return -1;
                }else if(lhs.getPageNumber() > rhs.getPageNumber()){
                    return 1;
                }else {
                    return 0;
                }
            }
        });
        return pageData;
    }

    public static List<GalleryDocumentPage>  collateInnerDocPages(List<GalleryDocumentPage> pageData){
        // sort the entities by their type
        Collections.sort(pageData, new Comparator<GalleryDocumentPage>() {
            @Override
            public int compare(GalleryDocumentPage lhs, GalleryDocumentPage rhs) {

                int lhsPageNumber = 0;
                int rhsPageNumber = 0;
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                if(((GalleryDocumentPage) ((GalleryDocumentPage)lhs).getPage()).getPage() instanceof DocumentPage){
                    GalleryDocumentPage innerPage = (GalleryDocumentPage) ((GalleryDocumentPage)lhs).getPage();
                    lhsPageNumber= innerPage.getPageNumber();
                }
                if(((GalleryDocumentPage) ((GalleryDocumentPage)lhs).getPage()).getPage() instanceof File) {
                    GalleryDocumentPage innerPage = (GalleryDocumentPage) ((GalleryDocumentPage)lhs).getPage();
                    lhsPageNumber= innerPage.getPageNumber();
                }
                if(((GalleryDocumentPage) ((GalleryDocumentPage)rhs).getPage()).getPage() instanceof DocumentPage){
                    GalleryDocumentPage innerPage = (GalleryDocumentPage) ((GalleryDocumentPage)rhs).getPage();
                    rhsPageNumber= innerPage.getPageNumber();
                }
                if(((GalleryDocumentPage) ((GalleryDocumentPage)rhs).getPage()).getPage() instanceof File) {
                    GalleryDocumentPage innerPage = (GalleryDocumentPage) ((GalleryDocumentPage)rhs).getPage();
                    rhsPageNumber= innerPage.getPageNumber();
                }
                if(lhsPageNumber < rhsPageNumber){
                    return -1;
                }else if(lhsPageNumber > rhsPageNumber){
                    return 1;
                }else {
                    return 0;
                }
            }
        });
        return DocumentPOJOUtils.reindexDocPages(pageData);
    }

    public static List<GalleryDocumentPage>  reindexDocPages(List<GalleryDocumentPage> pageData){
        int pageCounter = 1;

        // sort the pages first based on position

        for(int i=0; i< pageData.size(); i++){
            if(((GalleryDocumentPage) ((GalleryDocumentPage)pageData.get(i)).getPage()).getPage() instanceof DocumentPage){
                GalleryDocumentPage innerPage = (GalleryDocumentPage) ((GalleryDocumentPage)pageData.get(i)).getPage();
                innerPage.setPageNumber(pageCounter++);
                continue;
            }

            if(((GalleryDocumentPage) ((GalleryDocumentPage)pageData.get(i)).getPage()).getPage() instanceof File) {
                GalleryDocumentPage innerPage = (GalleryDocumentPage) ((GalleryDocumentPage)pageData.get(i)).getPage();
                innerPage.setPageNumber(pageCounter++);
                continue;
            }
        }

        return pageData;
    }

    /**
     * Remove all the gallery files that have been created when OpenCV camera created its images.
     * They have been saved so do not have to be stored locally anymore.
     * @param globalData - context session data
     */
    public static void removeGalleryFiles(SessionData globalData){
        // delete all and any other images
        //
        ArrayList<String> galleryFiles = new ArrayList<>();
        galleryFiles = new Utils(globalData.getApplicationContext()).getFilePaths(null);
        for (int i = 0; i < galleryFiles.size(); i++) {
            new File(galleryFiles.get(i)).delete();
        }
    }

    public static void postProcessDocumentAddition(SessionData globalData, Document newDoc, String propertyBagKey, boolean synced){
        DocumentCardItem item;

        if(synced) {
            // place the data in the session queue
            globalData.getPropertyBag().put(propertyBagKey, newDoc);
            Log.d("POST Request", "Created new Document:return: " + newDoc);

            List<Document> docs = new ArrayList<Document>();
            docs.add(newDoc);

            item = DocumentPOJOUtils.convertDocuments(docs, globalData, null, false).get(0);
            item.setBackendSynced(synced);
        }else{
            item = globalData.getNextDocument();
        }
        //
        //


        Fragment parentFragment;
        globalData.getNextDocument().setBackendSynced(synced);
        parentFragment = (globalData.getCurrFragment());


        // DocumentCardItem item = parentFragment.getAdapter().findCardItemBySyncId(newDoc.getSyncID());


        // parentFragment.getAdapter().findCardItemBySyncId(newDoc.getSyncID()).setBackendSynced(true);
        // globalData.getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY).findCardItemBySyncId(newDoc.getSyncID()).setBackendSynced(true);


        // parentFragment.getAdapter().addDataItem(DocumentPOJOUtils.convertDocuments(docs, globalData, null, false).get(0));
        //parentFragment.getAdapter().notifyDataSetChanged();
        if(globalData.getCurrFragment() instanceof MyDocumentsFragment){
            int changedIndex = ((MyDocumentsFragment)parentFragment).getAdapter().replaceCardItem(item);
            ((MyDocumentsFragment)parentFragment).refreshListAtIndex(changedIndex);
        }
        if(globalData.getCurrFragment() instanceof ProfileDocumentsFragment){
            int changedIndex = ((ProfileDocumentsFragment)parentFragment).getAdapter().replaceCardItem(item);
            ((ProfileDocumentsFragment)parentFragment).refreshListAtIndex(changedIndex);
        }

        // remove this doc from the stack
        globalData.popNextDocument();
    }

    public static void postProcessDocumentUpdation(SessionData globalData, Document newDoc, String propertyBagKey, boolean synced){
        DocumentCardItem item;

        if(synced) {
            // place the data in the session queue
            globalData.getPropertyBag().put(propertyBagKey, newDoc);
            Log.d("POST Request", "Updated Document:return: " + newDoc);

            List<Document> docs = new ArrayList<Document>();
            docs.add(newDoc);

            item = DocumentPOJOUtils.convertDocuments(docs, globalData, null, false).get(0);
            item.setBackendSynced(synced);
        }else{
            item = globalData.getNextDocument();
        }
        //
        //


        Fragment parentFragment;
        globalData.getNextDocument().setBackendSynced(synced);
        parentFragment = (globalData.getCurrFragment());


        // DocumentCardItem item = parentFragment.getAdapter().findCardItemBySyncId(newDoc.getSyncID());


        // parentFragment.getAdapter().findCardItemBySyncId(newDoc.getSyncID()).setBackendSynced(true);
        // globalData.getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY).findCardItemBySyncId(newDoc.getSyncID()).setBackendSynced(true);


        // parentFragment.getAdapter().addDataItem(DocumentPOJOUtils.convertDocuments(docs, globalData, null, false).get(0));
        //parentFragment.getAdapter().notifyDataSetChanged();
        if(globalData.getCurrFragment() instanceof MyDocumentsFragment){
            int changedIndex = ((MyDocumentsFragment)parentFragment).getAdapter().replaceCardItem(item);
            ((MyDocumentsFragment)parentFragment).refreshListAtIndex(changedIndex);
            ((MyDocumentsFragment)parentFragment).refreshList();
        }
        if(globalData.getCurrFragment() instanceof ProfileDocumentsFragment){
            int changedIndex = ((ProfileDocumentsFragment)parentFragment).getAdapter().replaceCardItem(item);
            ((ProfileDocumentsFragment)parentFragment).refreshListAtIndex(changedIndex);
        }

        // remove this doc from the stack
        globalData.popNextDocument();
    }

    public static void postProcessDocumentAdditionSync(SessionData globalData, Document newDoc, String propertyBagKey){
        DocumentCardItem item;

            // place the data in the session queue
            globalData.getPropertyBag().put(propertyBagKey, newDoc);
            Log.d("POST Request", "Created new Document:return: " + newDoc);

            List<Document> docs = new ArrayList<Document>();
            docs.add(newDoc);

            item = DocumentPOJOUtils.convertDocuments(docs, globalData, null, false).get(0);
            item.setBackendSynced(true);

        //
        //

        Fragment parentFragment;
        parentFragment = (globalData.getCurrFragment());


        // DocumentCardItem item = parentFragment.getAdapter().findCardItemBySyncId(newDoc.getSyncID());


        // parentFragment.getAdapter().findCardItemBySyncId(newDoc.getSyncID()).setBackendSynced(true);
        // globalData.getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY).findCardItemBySyncId(newDoc.getSyncID()).setBackendSynced(true);


        // parentFragment.getAdapter().addDataItem(DocumentPOJOUtils.convertDocuments(docs, globalData, null, false).get(0));
        //parentFragment.getAdapter().notifyDataSetChanged();
        if(globalData.getCurrFragment() instanceof MyDocumentsFragment){
            int changedIndex = ((MyDocumentsFragment)parentFragment).getAdapter().replaceCardItem(item);
            ((MyDocumentsFragment)parentFragment).refreshListAtIndex(changedIndex);
        }
        if(globalData.getCurrFragment() instanceof ProfileDocumentsFragment){
            int changedIndex = ((ProfileDocumentsFragment)parentFragment).getAdapter().replaceCardItem(item);
            ((ProfileDocumentsFragment)parentFragment).refreshListAtIndex(changedIndex);
        }
    }

    public static String getDocumentNotesHeader(List<NoteData> notes){
        String result = "";

        if(notes == null || notes.size() == 0) return result;

        String[] headerData = notes.get(0).getNote().split(DocumentPOJOUtils.DOC_NOTE_HEADER_DELIMITER);
        if(headerData != null && headerData.length > 0) {
            result = headerData[0];
        }

        return result;
    }

    public static String getDocumentNotes(List<NoteData> notes){
        String result = "";

        if(notes == null || notes.size() == 0) return result;

        String[] headerData = notes.get(0).getNote().split(DocumentPOJOUtils.DOC_NOTE_HEADER_DELIMITER);
        if(headerData != null && headerData.length > 1){
            result = headerData[1];
        }else if(headerData != null && headerData.length > 0){
            result = headerData[0];
        }

        return result;
    }

    public static void linkDocsByTag(SessionData globalVariable, String tagText ){

        // get all the documents
        //

        // loop through docs to find any that have this tag
    }

    protected static boolean isPartOfTrace(){
        boolean result = false;

        return result;
    }

    public static Document convertDocumentCardItemToDocument(DocumentCardItem cardItem){
        Document item = new Document();

        item.setId(cardItem.getId());
        item.setOwner(cardItem.getUsername());
        item.setSyncID(cardItem.getSyncID());
        item.setDocumentType(cardItem.getType());
        item.setType(cardItem.getDocumentType());
        return item;
    }

    public static String getTranslatedStatus(SessionData globalData, String status){
        String result = null;
        if(status.equals(Document.STATUS_REJECTED)){
            result = globalData.getResources().getString(R.string.document_acceptance_workflow_status_REJECTED);
        }
        if(status.equals(Document.STATUS_ACCEPTED)){
            result = globalData.getResources().getString(R.string.document_acceptance_workflow_status_ACCEPTED);
        }
        if(status.equals(Document.STATUS_DRAFT)){
            result = globalData.getResources().getString(R.string.document_acceptance_workflow_status_DRAFT);
        }
        if(status.equals(Document.STATUS_PENDING)){
            result = globalData.getResources().getString(R.string.document_acceptance_workflow_status_PENDING);
        }
        if(status.equals(Document.STATUS_RESUBMITTED)){
            result = globalData.getResources().getString(R.string.document_acceptance_workflow_status_RESUBMITTED);
        }
        if(status.equals(Document.STATUS_SUBMITTED)){
            result = globalData.getResources().getString(R.string.document_acceptance_workflow_status_SUBMITTED);
        }

        return result;
    }

    public static String getDocumentTagPrefix(DocumentCardItem doc){
        if(doc.getDocumentType().getValue().equals("Movement Document")){
            return "MD:";
        }
        if(doc.getDocumentType().getValue().equals("Fry Movement Document")){
            return "FMD:";
        }
        return null;
    }

    public static boolean doesTagExistInDoc(DocumentCardItem doc, TagData tag){
        boolean result = false;

        for(int i=0; i < doc.getTags().size(); i++){
            if(doc.getTags().get(i).getText().equals(tag.getText())){
                return true;
            }
        }
        return result;
    }

    public static void getLinkedDocsByTags(SessionData globalVariable){
        //
        // Get the current doc
        DocumentCardItem currDocItem = globalVariable.getNextDocument();

        // got each tag that was chosen
        for(int tag_index= 0; tag_index< globalVariable.getNextDocument().getTags().size(); tag_index++){
            TagData currentDocTag = currDocItem.getTags().get(tag_index);

            //if(!currentDocTag.isCustom()){
            //    continue;
            //}

            //
            // get the list of documents that could be linked

            // for each adapter that holds document items for this session
            for(int j=0; j<globalVariable.getDocumentAdapterMap().keySet().size(); j++){

                // for each adapter key
                Iterator<String> iterator = globalVariable.getDocumentAdapterMap().keySet().iterator();
                while(iterator.hasNext()){

                    String key = iterator.next();
                    if(key.equals(ProfileDocumentsFragment.RECYCLER_ADAPTER_KEY) || key.equals(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)){
                        // skip
                        continue;
                    }
                    // for each document in the adapter
                    List<DocumentCardItem> items = globalVariable.getDocumentAdapterMap().get(key).getDataSet();
                    for(int item_index=0; item_index<items.size(); item_index++ ){
                        //
                        // cannot link itself
                        if(items.get(item_index).getId() == currDocItem.getId()){
                            // skip
                            continue;
                        }
                        //<TODO> check the permisssions for auto linking based on tags
                        // Make sure that the doc can be legally linked, so it cannot be from teh same org
                        // but must be from the same stage
                        //if(items.get(item_index).getGroupName().equals(currDocItem.getGroupName())
                        //        || items.get(item_index).getGroupType().equals(currDocItem.getGroupType())){
                        //    // skip
                        //    continue;
                        //}

                        //
                        // Skip
                        // check if the element is a linked doc by tag

                        for(int item_tag_index=0; item_tag_index < items.get(item_index).getTags().size(); item_tag_index++){
                            TagData currComparedDocTag = items.get(item_index).getTags().get(item_tag_index);
                            if(currComparedDocTag.equals(currentDocTag)) {
                                // add the item into the linked docs if not present
                                boolean isPresent = false;
                                for(int curr_linked_doc_index = 0;
                                    curr_linked_doc_index < currDocItem.getLinkedDocuments().size();
                                    curr_linked_doc_index++){
                                    if(currDocItem.getLinkedDocuments().get(curr_linked_doc_index).getId()
                                            == items.get(item_index).getId()){
                                        isPresent = true;
                                        break;
                                    }
                                }
                                if(!isPresent){
                                    // add the doc into linked list
                                    currDocItem.getLinkedDocuments()
                                            .add(DocumentPOJOUtils.convertDocumentCardItemToDocument(items.get(item_index)));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static List<Document> convertLinkedDocumentFromInternalDocument(List<LinkedDocumentItemSelectionDataHelper.LinkedDocumentDataCard> docs){
        List<Document> result = new ArrayList<>();

        for(int i=0; i< docs.size(); i++){
            LinkedDocumentItemSelectionDataHelper.LinkedDocumentDataCard doc = docs.get(i);
            Document item = new Document();
            item.setId(doc.getId());
            item.setOwner(doc.getOwner());
            item.setSyncID(doc.getSyncId());
            item.setDocumentType(doc.getLinkedDocText());
            item.setType(doc.getDocType());

            result.add(item);
        }

        return result;
    }

    public static List<GalleryDocumentPage> convertDocumentPagesToGalleryPages(List<DocumentPage> pages ){
        List<GalleryDocumentPage> result = new ArrayList<GalleryDocumentPage>();
        for(int i=0; i< pages.size(); i++){
            // create a new page
            GalleryDocumentPage page = new GalleryDocumentPage(pages.get(i), false, i);
            page.setPageNumber(pages.get(i).getPageNumber());
            result.add(page);
        }

        return result;
    }

    public static String getFieldDisplayName(SessionData globalVariable, long fieldDefinitionId){
        String result = "";
        for(int i=0; i< globalVariable.getCurrentUser().getDynamicFieldDefinitions().size() ;i++){
            if(globalVariable.getCurrentUser().getDynamicFieldDefinitions().get(i).getId() == fieldDefinitionId){
                result = globalVariable.getCurrentUser().getDynamicFieldDefinitions().get(i).getDisplayName();
            }
        }
        return result;
    }

    public static List<GalleryDocumentPage> getNewestPageData(SessionData globalVariable) {
        List<GalleryDocumentPage> pageData = new ArrayList<>();
        // populate the data
        for(int i=0; i< globalVariable.getNextDocument().getDocumentPages().size(); i++){
            pageData.add(new GalleryDocumentPage(globalVariable.getNextDocument().getDocumentPages().get(i),
                    globalVariable.getNextDocument().getDocumentPages().get(i).isDeleted()));
        }
        for(int i=0; i< globalVariable.getNextDocument().getImagePages().size(); i++){
            pageData.add(new GalleryDocumentPage(globalVariable.getNextDocument().getImagePages().get(i),
                    globalVariable.getNextDocument().getImagePages().get(i).isDeleted()));
        }

        //
        // recollate pages
        pageData = DocumentPOJOUtils.collateInnerDocPages(pageData);

        return pageData;
    }

    public static boolean doesImagePageExistInCurrentDocument(SessionData globalVariable, File imagePage){
        boolean result = false;

        for(int i=0; i< globalVariable.getNextDocument().getImagePages().size(); i++){
            File filePage = (File) ((GalleryDocumentPage) ((GalleryDocumentPage)globalVariable.getNextDocument().getImagePages().get(i))).getPage();
            if(filePage.equals(imagePage)){
                result = true;
            }
        }

        return result;

    }
}
