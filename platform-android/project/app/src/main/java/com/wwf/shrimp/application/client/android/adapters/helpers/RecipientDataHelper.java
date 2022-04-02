package com.wwf.shrimp.application.client.android.adapters.helpers;

import java.util.Random;

/**
 * Created by AleaActaEst on 11/09/2017.
 */

public class RecipientDataHelper {
    public static Random randomGenerator = new Random(System.currentTimeMillis());


    public static class RecipientCard {
        private String organizationName;
        private String userName;
        private boolean checkState = false;
        private long id;

        public boolean isCustom() {
            return custom;
        }

        public void setCustom(boolean custom) {
            this.custom = custom;
        }

        private boolean custom = false;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }



        public RecipientCard(String userName, String organizationName, boolean checkState){
            this.checkState = checkState;
            this.userName = userName;
            this.organizationName = organizationName;
            this.id = randomGenerator.nextLong();

        }
        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
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
            if (!(o instanceof RecipientCard)) return false;

            RecipientCard that = (RecipientCard) o;

            //if (checkState != that.checkState) return false;
            if (id != that.id) return false;
            return userName != null ? userName.equals(that.userName) : that.userName == null;

        }

        @Override
        public int hashCode() {
            int result = userName != null ? userName.hashCode() : 0;
            result = 31 * result + (checkState ? 1 : 0);
            result = 31 * result + (int) (id ^ (id >>> 32));
            return result;
        }

        public String getOrganizationName() {
            return organizationName;
        }

        public void setOrganizationName(String organizationName) {
            this.organizationName = organizationName;
        }
    }


}
