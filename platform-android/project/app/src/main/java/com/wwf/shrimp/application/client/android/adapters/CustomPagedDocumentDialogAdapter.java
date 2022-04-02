package com.wwf.shrimp.application.client.android.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.util.Log;

import com.wwf.shrimp.application.client.android.dialogs.TabbedDocumentDialog;
import com.wwf.shrimp.application.client.android.fragments.CustomFragmentDocumentImagePage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Custom Paged Document Dialog for showing document details
 * @author AleaActaEst
 */

public class CustomPagedDocumentDialogAdapter extends FragmentPagerAdapter {
    // logging tag
    private static final String LOG_TAG = "Doc Pager Adapter";

    Map<String, FragmentDataItem> mFragmentMap = new HashMap<>();
    List<Fragment> mFragmentCollection = new ArrayList<>();
    List<FragmentDataItem> mFragmentItemCollection = new ArrayList<>();
    List<String> mTitleCollection = new ArrayList<>();
    List<Integer> mPageHashes = new ArrayList<>();

    public CustomPagedDocumentDialogAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(String title, Fragment fragment, int hashcode){
        mTitleCollection.add(title);
        mFragmentCollection.add(fragment);
        mPageHashes.add(new Integer(hashcode));
        // create a new mapping
        FragmentDataItem fragmentItem = new FragmentDataItem(fragment, title, hashcode);
        mFragmentMap.put(title, fragmentItem);
        // create a composite item list
        mFragmentItemCollection.add(fragmentItem);
    }

    //Needed for
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentItemCollection.get(position).getTitle();
        // return mTitleCollection.get(position);
    }
    @Override
    public Fragment getItem(int position) {
        Log.i(LOG_TAG, "Fetching Fragment for Adapter: position " + position);
        // TODO make the position dynamic and based on presence/absence of Notes Tab
        if(position < 3){
            Log.i(LOG_TAG, "Fetching Fragment for Adapter: [FETCHING EXISTING]");
            return mFragmentItemCollection.get(position).getFragment();
        }else{
            Log.i(LOG_TAG, "Fetching Fragment for Adapter: [CREATE NEW]");
            return CustomFragmentDocumentImagePage.createInstance(TabbedDocumentDialog.DATA_MODE_LOCAL, position -3, null);
        }

        // return mFragmentCollection.get(position);
    }
    @Override
    public int getCount() {

        return mFragmentItemCollection.size();
    }

    @Override
    public int getItemPosition(Object object) {
        if(object instanceof CustomFragmentDocumentImagePage){
            Log.i(LOG_TAG, "Getting item position " + ((CustomFragmentDocumentImagePage)object).getPosition());
        }
        return POSITION_UNCHANGED;
    }

    public Fragment getFragmentByTitle(String title){

        for(int i=0; i<mFragmentItemCollection.size(); i++){
            if(mFragmentItemCollection.get(i).getTitle().equals(title)){
                return mFragmentItemCollection.get(i).getFragment();
            }
        }
        return null;
        /**
        int index = mTitleCollection.indexOf(title);
        if(index < 0){
            return null;
        }
        return mFragmentCollection.get(index);
         */
    }

    public Fragment getFragmentByPageHash(int hashcode){

        for(int i=0; i<mFragmentItemCollection.size(); i++){
            if(mFragmentItemCollection.get(i).getHash().intValue() == hashcode){
                return mFragmentItemCollection.get(i).getFragment();
            }
        }
        return null;
        /**
        int index = mPageHashes.indexOf(new Integer(hashcode));
        if(index < 0){
            return null;
        }
        return mFragmentCollection.get(index);
         */
    }

    public void removeFragmentByTitle(String title){
        Iterator<FragmentDataItem> iterator = mFragmentItemCollection.iterator();
        while(iterator.hasNext()){
            if(iterator.next().getTitle().equals(title)){
                iterator.remove();
            }
        }
    }

    public void removeFragmentByPosition(int position){
        mFragmentItemCollection.remove(position);
    }



    public void clearPagesByTitle(String title){
        Iterator<FragmentDataItem> iterator = mFragmentItemCollection.iterator();
        while(iterator.hasNext()){
            if(iterator.next().getTitle().contains(title)){
                iterator.remove();
            }
        }
    }

    public void recollatePageFragments(String titlePrefix){
        int index = 1;
        for(int i=0; i< mFragmentItemCollection.size(); i++){
            FragmentDataItem item = mFragmentItemCollection.get(i);
            if(item.getTitle().contains(titlePrefix)){
                mFragmentItemCollection.get(i).setTitle(titlePrefix + " " + index++);
            }
        }
    }

    /**
     * Helper class to hold the data for the fragment
     */
    private class FragmentDataItem {
        Fragment fragment;
        String title;
        Integer hash;

        public FragmentDataItem(Fragment fragment, String title, Integer hash){
            this.fragment = fragment;
            this.title = title;
            this.hash = hash;
        }

        public Fragment getFragment() {
            return fragment;
        }

        public void setFragment(Fragment fragment) {
            this.fragment = fragment;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getHash() {
            return hash;
        }

        public void setHash(Integer hash) {
            this.hash = hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FragmentDataItem that = (FragmentDataItem) o;
            return Objects.equals(title, that.title);
        }

        @Override
        public int hashCode() {

            return Objects.hash(title);
        }
    }


}
