package com.wwf.shrimp.application.models;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a document which will capture the specific data 
 * for any type of document in the system.
 * It is made up of Pages, tags and associated metadata.
 * 
 * The currently relevant metadata would be:
 * - GPS location of document creation (only if supported by the client)
 * - time of creation
 * - User who created the document
 * - Image type (i.e. JPEG, PNG, etc...)
 * 
 * @author AleaActaEst
 *
 */
public class Document extends IdentifiableEntity implements IResource {
	

	public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_RESUBMITTED= "RESUBMITTED";

	private String name;
	private String description;
	private List<DocumentPage> pages = new ArrayList<DocumentPage>();
	private List<TagData> tags = new ArrayList<TagData>();
	private List<Metadata> metadata = new ArrayList<Metadata>();
	private List<DataEntity> data = new ArrayList<DataEntity>();
	private DocumentType type;
	private String documentType;
	private String owner;
	private String creationTimestamp;
	private String documentImageURI;
	private String base64ImageData;
    private String TypeHEXColor;
    private String syncID;
    private boolean currentUserRead;
    private List<Document> linkedDocuments = new ArrayList<Document>();
    private List<Document> attachedDocuments = new ArrayList<Document>();
    private long groupId;
    private long organizationId;
    private String groupName;
    private String groupTypeName;
    private int groupTypeOrderIndex;
    private List<User> toRecipients = new ArrayList<User>();
    private String status;
    private List<NoteData> notes = new ArrayList<NoteData>();
    private String gpsLocation;

    
	public List<NoteData> getNotes() {
		return notes;
	}

	public void setNotes(List<NoteData> notes) {
		this.notes = notes;
	}
    
	public List<User> getToRecipients() {
		return toRecipients;
	}

	public void setToRecipients(List<User> toRecipients) {
		this.toRecipients = toRecipients;
	}
	
	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @param groupName the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * @return the groupTypeName
	 */
	public String getGroupTypeName() {
		return groupTypeName;
	}

	/**
	 * @param groupTypeName the groupTypeName to set
	 */
	public void setGroupTypeName(String groupTypeName) {
		this.groupTypeName = groupTypeName;
	}

	/**
	 * @return the groupTYpeOrderIndex
	 */
	public int getGroupTypeOrderIndex() {
		return groupTypeOrderIndex;
	}

	/**
	 * @param groupTYpeOrderIndex the groupTYpeOrderIndex to set
	 */
	public void setGroupTypeOrderIndex(int groupTypeOrderIndex) {
		this.groupTypeOrderIndex = groupTypeOrderIndex;
	}

	/**
	 * @return the currentUserRead
	 */
	public boolean isCurrentUserRead() {
		return currentUserRead;
	}

	/**
	 * @param currentUserRead the currentUserRead to set
	 */
	public void setCurrentUserRead(boolean currentUserRead) {
		this.currentUserRead = currentUserRead;
	}
	

    public String getSyncID() {
		return syncID;
	}

	public void setSyncID(String syncID) {
		this.syncID = syncID;
	}

	public String getTypeHEXColor() {
        return TypeHEXColor;
    }

    /**
	 * @return the base64ImageData
	 */
	public String getBase64ImageData() {
		return base64ImageData;
	}

	/**
	 * @param base64ImageData the base64ImageData to set
	 */
	public void setBase64ImageData(String base64ImageData) {
		this.base64ImageData = base64ImageData;
	}

	public void setTypeHEXColor(String typeHEXColor) {
        TypeHEXColor = typeHEXColor;
    }
	
	
	/**
	 * @return the documentImageURI
	 */
	public String getDocumentImageURI() {
		return documentImageURI;
	}
	/**
	 * @param documentImageURI the documentImageURI to set
	 */
	public void setDocumentImageURI(String documentImageURI) {
		this.documentImageURI = documentImageURI;
	}
	/**
	 * @return the creationTimestamp
	 */
	public String getCreationTimestamp() {
		return creationTimestamp;
	}
	/**
	 * @param creationTimestamp the creationTimestamp to set
	 */
	public void setCreationTimestamp(String creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}
	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}
	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}
	/**
	 * @return the documentType
	 */
	public String getDocumentType() {
		return documentType;
	}
	/**
	 * @param documentType the documentType to set
	 */
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the pages
	 */
	public List<DocumentPage> getPages() {
		return pages;
	}
	/**
	 * @param pages the pages to set
	 */
	public void setPages(List<DocumentPage> pages) {
		this.pages = pages;
	}
	/**
	 * @return the tags
	 */
	public List<TagData> getTags() {
		return tags;
	}
	/**
	 * @param tags the tags to set
	 */
	public void setTags(List<TagData> tags) {
		this.tags = tags;
	}
	/**
	 * @return the metadata
	 */
	public List<Metadata> getMetadata() {
		return metadata;
	}
	/**
	 * @param metadata the metadata to set
	 */
	public void setMetadata(List<Metadata> metadata) {
		this.metadata = metadata;
	}
	/**
	 * @return the data
	 */
	public List<DataEntity> getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(List<DataEntity> data) {
		this.data = data;
	}
	/**
	 * @return the type
	 */
	public DocumentType getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(DocumentType type) {
		this.type = type;
	}

	/**
	 * @return the linkedDocuments
	 */
	public List<Document> getLinkedDocuments() {
		return linkedDocuments;
	}

	/**
	 * @param linkedDocuments the linkedDocuments to set
	 */
	public void setLinkedDocuments(List<Document> linkedDocuments) {
		this.linkedDocuments = linkedDocuments;
	}

	/**
	 * @return the attachedDocuments
	 */
	public List<Document> getAttachedDocuments() {
		return attachedDocuments;
	}

	/**
	 * @param attachedDocuments the attachedDocuments to set
	 */
	public void setAttachedDocuments(List<Document> attachedDocuments) {
		this.attachedDocuments = attachedDocuments;
	}
	
	/**
	 * @return the groupId
	 */
	public long getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the organizationId
	 */
	public long getOrganizationId() {
		return organizationId;
	}

	/**
	 * @param organizationId the organizationId to set
	 */
	public void setOrganizationId(long organizationId) {
		this.organizationId = organizationId;
	}
	

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
    public String getGpsLocation() {
		return gpsLocation;
	}

	public void setGpsLocation(String gpsLocation) {
		this.gpsLocation = gpsLocation;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Document [" + "status=" + status + ", name=" + name + ", description=" + description + ", pages=" + pages + ", tags=" + tags
				+ ", organizationId=" + organizationId + ", groupId=" + groupId
				+ ", metadata=" + metadata + ", data=" + data + ", type=" + type + ", documentType=" + documentType
				+ ", owner=" + owner + ", creationTimestamp=" + creationTimestamp + ", documentImageURI="
				+ documentImageURI + ", base64ImageData=" + base64ImageData + ", TypeHEXColor=" + TypeHEXColor
				+ ", syncID=" + syncID + ", currentUserRead=" + currentUserRead + ", linkedDocuments=" + linkedDocuments
				+ ", recipients=" + toRecipients
				+ ", attachedDocuments=" + attachedDocuments + "]";
	}

}
