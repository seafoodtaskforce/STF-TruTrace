package com.wwf.shrimp.application.client.android.adapters.helpers;

import com.wwf.shrimp.application.client.android.models.dto.DocumentType;

/**
 * Created by AleaActaEst on 11/09/2017.
 */

public class AttachedDocumentItemSelectionDataHelper {

    public static class AttachedDocumentDataCard {
        private String owner;
        private String attachedDocText;
        private long id;
        private String syncId;
        private DocumentType docType;

        public AttachedDocumentDataCard(long id, String attachedDocText, String owner, DocumentType docType){
            this.attachedDocText = attachedDocText;
            this.id = id;
            this.owner = owner;
            this.docType = docType;
        }


        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public String getAttachedDocText() {
            return attachedDocText;
        }

        public void setAttachedDocText(String attachedDocText) {
            this.attachedDocText = attachedDocText;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getSyncId() {
            return syncId;
        }

        public void setSyncId(String syncId) {
            this.syncId = syncId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AttachedDocumentDataCard)) return false;

            AttachedDocumentDataCard that = (AttachedDocumentDataCard) o;

            if (id != that.id) return false;
            if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
            return attachedDocText != null ? attachedDocText.equals(that.attachedDocText) : that.attachedDocText == null;

        }

        @Override
        public int hashCode() {
            int result = owner != null ? owner.hashCode() : 0;
            result = 31 * result + (attachedDocText != null ? attachedDocText.hashCode() : 0);
            result = 31 * result + (int) (id ^ (id >>> 32));
            return result;
        }

        public DocumentType getDocType() {
            return docType;
        }

        public void setDocType(DocumentType docType) {
            this.docType = docType;
        }
    }
}

