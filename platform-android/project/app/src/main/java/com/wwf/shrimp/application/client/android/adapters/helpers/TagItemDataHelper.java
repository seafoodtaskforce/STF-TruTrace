package com.wwf.shrimp.application.client.android.adapters.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by AleaActaEst on 11/09/2017.
 */

public class TagItemDataHelper {
    public static Random randomGenerator = new Random(System.currentTimeMillis());


    public static class TagDataCard {
        private String tagText;
        private boolean checkState = false;
        private boolean custom = false;
        private String customPrefix;

        public boolean isCustom() {
            return custom;
        }

        public void setCustom(boolean custom) {
            this.custom = custom;
        }



        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        private long id;

        public TagDataCard(String tagText, String customPrefix, boolean checkState){
            this.checkState = checkState;
            this.tagText = tagText;
            this.customPrefix = customPrefix;
            if(customPrefix != null){
                this.setCustom(true);
            }
            this.id = randomGenerator.nextLong();

        }
        public String getTagText() {
            return tagText;
        }

        public void setTagText(String tagText) {
            this.tagText = tagText;
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
            if (!(o instanceof TagDataCard)) return false;

            TagDataCard that = (TagDataCard) o;

            if (checkState != that.checkState) return false;
            if (id != that.id) return false;
            return tagText != null ? tagText.equals(that.tagText) : that.tagText == null;

        }

        @Override
        public int hashCode() {
            int result = tagText != null ? tagText.hashCode() : 0;
            result = 31 * result + (checkState ? 1 : 0);
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
