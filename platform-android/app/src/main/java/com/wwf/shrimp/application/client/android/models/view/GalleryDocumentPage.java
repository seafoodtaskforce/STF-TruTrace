package com.wwf.shrimp.application.client.android.models.view;

public class GalleryDocumentPage {

    private Object page;
    private boolean deleted = false;
    private int position;

    public GalleryDocumentPage(Object page) {
        this.page = page;
    }

    public GalleryDocumentPage(Object page, boolean deleted) {
        this.page = page;
        this.deleted = deleted;
    }

    public GalleryDocumentPage(Object page, boolean deleted, int position) {
        this.page = page;
        this.deleted = deleted;
        this.position = position;
    }

    public Object getPage() {
        return page;
    }

    public void setPage(Object page) {
        this.page = page;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
