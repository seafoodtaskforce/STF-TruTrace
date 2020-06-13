package com.wwf.shrimp.application.client.android.models.view;

import android.view.View;

/**
 * Document Context data which tracks where the document resides within the workflow
 */
public class DocumentContext {

    public static final String DOCUMENT_LINIKING_ACTIVITY_NAME = "DocumentLinkingActivity";
    public static final String DOCUMENT_ATTACHING_ACTIVITY_NAME = "DocumentAttachingActivity";
    public static final String DOCUMENT_RECIPIENTS_ACTIVITY_NAME = "DocumentRecipientsActivity";
    public static final String DOCUMENT_TAGGING_ACTIVITY_NAME = "DocumentTaggingActivity";

    private String contextName;
    private Object dataSet;
    private Object adapter;
    private Object dataItem;
    private int listPosition=- 1;
    private View view;

    public String getContextName() {
        return contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public Object getDataSet() {
        return dataSet;
    }

    public void setDataSet(Object dataSet) {
        this.dataSet = dataSet;
    }

    public Object getAdapter() {
        return adapter;
    }

    public void setAdapter(Object adapter) {
        this.adapter = adapter;
    }

    public Object getDataItem() {
        return dataItem;
    }

    public void setDataItem(Object dataItem) {
        this.dataItem = dataItem;
    }

    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }
}
