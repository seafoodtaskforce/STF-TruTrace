package com.wwf.shrimp.application.client.android.adapters.helpers;


import com.wwf.shrimp.application.client.android.models.dto.DocumentType;

/**
 * Created by AleaActaEst on 11/09/2017.
 */

public class AttachedDocumentItemDataHelper {


    public static class AttachedDocumentDataCard {
        private String owner;
        private String attachedDocText;
        private String timestamp;
        private boolean checkState = false;
        private DocumentType documentType;
        private long id;
        private String syncId;

        public AttachedDocumentDataCard(String attachedDocText, String owner, boolean checkState){
            this.checkState = checkState;
            this.attachedDocText = attachedDocText;
            this.owner = owner;

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
        public String getAttachedDocText() {
            return attachedDocText;
        }

        public void setAttachedDocText(String attachedDocText) {
            this.attachedDocText = attachedDocText;
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
            if (!(o instanceof AttachedDocumentDataCard)) return false;

            AttachedDocumentDataCard that = (AttachedDocumentDataCard) o;

            if (checkState != that.checkState) return false;
            if (id != that.id) return false;
            if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
            return attachedDocText != null ? attachedDocText.equals(that.attachedDocText) : that.attachedDocText == null;

        }

        @Override
        public int hashCode() {
            int result = owner != null ? owner.hashCode() : 0;
            result = 31 * result + (attachedDocText != null ? attachedDocText.hashCode() : 0);
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
