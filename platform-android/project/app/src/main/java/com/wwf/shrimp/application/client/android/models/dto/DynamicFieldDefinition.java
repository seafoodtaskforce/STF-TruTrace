package com.wwf.shrimp.application.client.android.models.dto;

public class DynamicFieldDefinition  extends IdentifiableEntity {

    public final static long NUMERIC_FIELD_TYPE = 2;
    public final static long ALPHA_NUMERIC_FIELD_TYPE = 3;
    public final static long DATE_FIELD_TYPE = 4;
    public final static long EXPIRY_DATE_FIELD_TYPE =5;

    private long orgID ;
    private long docTypeId;
    private long fieldTypeId;
    private String docTypeName;
    private String displayName;
    private String description;
    private String fieldType;
    private int maxLength;
    private boolean isRequired;
    private int ordinal;
    private boolean isDocId;
    private String ocrMatchText="";
    private int ocrGrabLength=0;

    public long getOrgID() {
        return orgID;
    }

    public void setOrgID(long orgID) {
        this.orgID = orgID;
    }
    public long getDocTypeId() {
        return docTypeId;
    }
    public void setDocTypeId(long docTypeId) {
        this.docTypeId = docTypeId;
    }
    public String getDocTypeName() {
        return docTypeName;
    }
    public void setDocTypeName(String docTypeName) {
        this.docTypeName = docTypeName;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getFieldType() {
        return fieldType;
    }
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }
    public int getMaxLength() {
        return maxLength;
    }
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
    public boolean isRequired() {
        return isRequired;
    }
    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }
    public int getOrdinal() {
        return ordinal;
    }
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public boolean isDocId() {
        return isDocId;
    }

    public void setDocId(boolean docId) {
        isDocId = docId;
    }

    public String getOcrMatchText() {
        return ocrMatchText;
    }

    public void setOcrMatchText(String ocrMatchText) {
        this.ocrMatchText = ocrMatchText;
    }

    public int getOcrGrabLength() {
        return ocrGrabLength;
    }

    public void setOcrGrabLength(int ocrGrabLength) {
        this.ocrGrabLength = ocrGrabLength;
    }

    public long getFieldTypeId() {
        return fieldTypeId;
    }
    public void setFieldTypeId(long fieldTypeId) {
        this.fieldTypeId = fieldTypeId;
    }
    @Override
    public String toString() {
        return "DynamicFieldDefinition [orgID=" + orgID + ", docTypeId=" + docTypeId + ", docTypeName=" + docTypeName
                + ", displayName=" + displayName + ", description=" + description + ", fieldType=" + fieldType
                + ", maxLength=" + maxLength + ", isRequired=" + isRequired + ", ordinal=" + ordinal + "]";
    }



}
