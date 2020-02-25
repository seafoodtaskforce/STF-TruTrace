package com.wwf.shrimp.application.client.android.adapters.helpers;

/**
 * Created by AleaActaEst on 11/09/2017.
 */

public class RecipientSelectionDataHelper {

    public static class RecipientCard {
        private String userName;
        private String organizationName;
        private long id;

        public RecipientCard(long id, String organizationName, String userName){
            this.userName = userName;
            this.organizationName = organizationName;
            this.id = id;


        }
        public String getOrganizationName() {
            return organizationName;
        }
        public String getUserName() {
            return userName;
        }
        public long getId() {
            return id;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RecipientCard)) return false;

            RecipientCard that = (RecipientCard) o;

            if (id != that.id) return false;
            return userName != null ? userName.equals(that.userName) : that.userName == null;

        }

        @Override
        public int hashCode() {
            int result = userName != null ? userName.hashCode() : 0;
            result = 31 * result + (int) (id ^ (id >>> 32));
            return result;
        }
    }
}

