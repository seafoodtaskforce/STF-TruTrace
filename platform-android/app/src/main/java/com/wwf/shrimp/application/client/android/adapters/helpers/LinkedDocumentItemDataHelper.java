package com.wwf.shrimp.application.client.android.adapters.helpers;


import com.wwf.shrimp.application.client.android.models.dto.DocumentType;

/**
 * Created by AleaActaEst on 11/09/2017.
 */

public class LinkedDocumentItemDataHelper {


    public static class LinkedDocumentDataCard {
        private String owner;
        private String linkedDocText;
        private String timestamp;
        private boolean checkState = false;
        private DocumentType documentType;
        private long id;
        private String syncId;

        private String customTag="";

        public LinkedDocumentDataCard(String linkedDocText, String owner, boolean checkState){
            this.checkState = checkState;
            this.linkedDocText = linkedDocText;
            this.owner = owner;

        }

        public String getCustomTag() {
            return customTag;
        }

        public void setCustomTag(String customTag) {
            this.customTag = customTag;
        }

        public String getSyncId() {
            return syncId;
        }

        public void setSyncId(String syncId) {
            this.syncId = syncId;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
        public String getLinkedDocText() {
            return linkedDocText;
        }

        public void setLinkedDocText(String linkedDocText) {
            this.linkedDocText = linkedDocText;
        }

        public boolean isCheckState() {
            return checkState;
        }

        public void setCheckState(boolean checkState) {
            this.checkState = checkState;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LinkedDocumentDataCard)) return false;

            LinkedDocumentDataCard that = (LinkedDocumentDataCard) o;

            if (checkState != that.checkState) return false;
            if (id != that.id) return false;
            if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
            return linkedDocText != null ? linkedDocText.equals(that.linkedDocText) : that.linkedDocText == null;

        }

        @Override
        public int hashCode() {
            int result = owner != null ? owner.hashCode() : 0;
            result = 31 * result + (linkedDocText != null ? linkedDocText.hashCode() : 0);
            result = 31 * result + (checkState ? 1 : 0);
            result = 31 * result + (int) (id ^ (id >>> 32));
            return result;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public DocumentType getDocumentType() {
            return documentType;
        }

        public void setDocumentType(DocumentType documentType) {
            this.documentType = documentType;
        }
    }




}
