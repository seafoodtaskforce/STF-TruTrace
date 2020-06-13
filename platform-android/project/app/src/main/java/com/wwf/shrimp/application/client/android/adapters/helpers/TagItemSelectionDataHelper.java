package com.wwf.shrimp.application.client.android.adapters.helpers;

/**
 * Created by AleaActaEst on 11/09/2017.
 */

public class TagItemSelectionDataHelper {

    public static class TagDataCard {
        private String tagText;
        private long id;
        private boolean custom = false;
        private String customPrefix;

        public TagDataCard(long id, String tagText, String tagPrefix){
            this.tagText = tagText;
            this.customPrefix = tagPrefix;
            this.id = id;


        }
        public String getTagText() {
            return tagText;
        }
        public long getId() {
            return id;
        }

        public void setTagText(String tagText) {
            this.tagText = tagText;
        }

        public boolean isCustom() {
            return custom;
        }

        public void setCustom(boolean custom) {
            this.custom = custom;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TagDataCard)) return false;

            TagDataCard that = (TagDataCard) o;

            if (id != that.id) return false;
            return tagText != null ? tagText.equals(that.tagText) : that.tagText == null;

        }

        @Override
        public int hashCode() {
            int result = tagText != null ? tagText.hashCode() : 0;
            result = 31 * result + (int) (id ^ (id >>> 32));
            return result;
        }

        public String getCustomPrefix() {
            return customPrefix;
        }

        public void setCustomPrefix(String customPrefix) {
            this.customPrefix = customPrefix;
        }
    }
}

