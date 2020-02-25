package com.wwf.shrimp.application.client.android.adapters.helpers;

import com.wwf.shrimp.application.client.android.models.dto.DocumentType;

/**
 * Created by AleaActaEst on 11/09/2017.
 */

public class LinkedDocumentItemSelectionDataHelper {

    public static class LinkedDocumentDataCard {
        private String owner;
        private String linkedDocText;
        private long id;
        private String syncId;
        private DocumentType docType;

        public LinkedDocumentDataCard(long id, String linkedDocText, String owner, DocumentType docType){
            this.linkedDocText = linkedDocText;
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

        public String getLinkedDocText() {
            return linkedDocText;
        }

        public void setLinkedDocText(String linkedDocText) {
            this.linkedDocText = linkedDocText;
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
            if (!(o instanceof LinkedDocumentDataCard)) return false;

            LinkedDocumentDataCard that = (LinkedDocumentDataCard) o;

            if (id != that.id) return false;
            if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
            return linkedDocText != null ? linkedDocText.equals(that.linkedDocText) : that.linkedDocText == null;

        }

        @Override
        public int hashCode() {
            int result = owner != null ? owner.hashCode() : 0;
            result = 31 * result + (linkedDocText != null ? linkedDocText.hashCode() : 0);
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

