package com.wwf.shrimp.application.client.android.models.view;

import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.DocumentPage;
import com.wwf.shrimp.application.client.android.models.dto.DocumentType;
import com.wwf.shrimp.application.client.android.models.dto.Group;
import com.wwf.shrimp.application.client.android.models.dto.IdentifiableEntity;
import com.wwf.shrimp.application.client.android.models.dto.NoteData;
import com.wwf.shrimp.application.client.android.models.dto.TagData;
import com.wwf.shrimp.application.client.android.models.dto.User;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by AleaActaEst on 16/06/2017.
 */

/**
 * Document mapping to a GUI card
 */
public class DocumentCardItem extends IdentifiableEntity{

    private String name;
    private String type;
    private String typeHEXColor;
    private File imagePath;
    private List<GalleryDocumentPage> imagePages = new ArrayList<>();
    private List<Boolean> imagePagesSynced = new ArrayList<>();
    private Date creationTimestamp;
    private String username;
    private DocumentType documentType;
    private String syncID;
    private List<GalleryDocumentPage> documentPages = new ArrayList<>();
    private boolean wasRead = true;
    private List<TagData> tags= new ArrayList<TagData>();
    private List<Document> linkedDocuments = new ArrayList<Document>();
    private List<Document> attachedDocuments = new ArrayList<Document>();;
    private String groupType;
    private String groupName;
    private boolean backendSynced = true;
    private List<User> recipients = new ArrayList<User>();
    private DocumentContext context = null;
    private String status= Document.STATUS_DRAFT;
    private List<NoteData> notes = new ArrayList<NoteData>();

    public DocumentCardItem(){

    }


    public boolean isBackendSynced() {
        return backendSynced;
    }

    public void setBackendSynced(boolean backendSynced) {
        this.backendSynced = backendSynced;
    }


    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public boolean isWasRead() {
        return wasRead;
    }

    public void setWasRead(boolean wasRead) {
        this.wasRead = wasRead;
    }

    public List<GalleryDocumentPage> getImagePages() {
        return imagePages;
    }

    public void setImagePages(List<GalleryDocumentPage> imagePages) {
        this.imagePages = imagePages;
    }

    public String getSyncID() {
        return syncID;
    }

    public void setSyncID(String syncID) {
        this.syncID = syncID;
    }


    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getTypeHEXColor() {
        return typeHEXColor;
    }

    public void setTypeHEXColor(String typeHEXColor) {
        this.typeHEXColor = typeHEXColor;
    }

    public File getImagePath() {
        return imagePath;
    }

    public void setImagePath(File imagePath) {
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DocumentCardItem that = (DocumentCardItem) o;
        return Objects.equals(syncID, that.syncID);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), syncID);
    }

    @Override
    public String toString() {
        return "DocumentCardItem{" +
                "name='" + name + '\'' +

                ", type='" + type + '\'' +
                ", typeHEXColor='" + typeHEXColor + '\'' +
                ", imagePath=" + imagePath +
                ", imagePages=" + imagePages +
                ", creationTimestamp=" + creationTimestamp +
                ", username='" + username + '\'' +
                ", documentType=" + documentType +
                ", syncID='" + syncID + '\'' +
                ", documentPages=" + documentPages +
                ", wasRead=" + wasRead +
                ", tags=" + tags +
                ", linkedDocuments=" + linkedDocuments +
                ", attachedDocuments=" + attachedDocuments +
                ", groupName=" + groupType +
                '}';
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<GalleryDocumentPage> getDocumentPages() {
        return documentPages;
    }

    public void setDocumentPages(List<GalleryDocumentPage> documentPages) {
        this.documentPages = documentPages;
    }

    public List<TagData> getTags() {
        return tags;
    }

    public void setTags(List<TagData> tags) {
        this.tags = tags;
    }


    public List<Document> getLinkedDocuments() {
        return linkedDocuments;
    }

    public void setLinkedDocuments(List<Document> linkedDocuments) {
        this.linkedDocuments = linkedDocuments;
    }

    public List<Document> getAttachedDocuments() {
        return attachedDocuments;
    }

    public void setAttachedDocuments(List<Document> attachedDocuments) {
        this.attachedDocuments = attachedDocuments;
    }

    public List<User> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<User> recipients) {
        this.recipients = recipients;
    }

    public List<Boolean> getImagePagesSynced() {
        return imagePagesSynced;
    }

    public void setImagePagesSynced(List<Boolean> imagePagesSynced) {
        this.imagePagesSynced = imagePagesSynced;
    }

    public DocumentContext getContext() {
        return context;
    }

    public void setContext(DocumentContext context) {
        this.context = context;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<NoteData> getNotes() {
        return notes;
    }

    public void setNotes(List<NoteData> notes) {
        this.notes = notes;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void collatePages(){
        int leastPosition = 0;
        int minPos = Integer.MAX_VALUE;

        while(leastPosition < (imagePages.size() + documentPages.size()) ){

            boolean imageArray = false;
            int foundIndex = -1;

            //
            // go through document pages
            for(int dp=0; dp < documentPages.size(); dp++){
                if(documentPages.get(dp).getPosition() < minPos && documentPages.get(dp).getPosition() >= leastPosition){
                    minPos = documentPages.get(dp).getPosition();
                    foundIndex = dp;
                    imageArray = false;
                }
            }
            //
            // go through document images
            for(int di=0; di < imagePages.size(); di++){
                if(imagePages.get(di).getPosition() < minPos && documentPages.get(di).getPosition() >= leastPosition){
                    minPos = imagePages.get(di).getPosition();
                    foundIndex = di;
                    imageArray = true;
                }
            }
            //
            // change the index of the found
            if(foundIndex != -1){
                if(imageArray){
                    imagePages.get(foundIndex).setPosition(leastPosition++);
                }else{
                    documentPages.get(foundIndex).setPosition(leastPosition++);
                }
            }
        }
    }
}
