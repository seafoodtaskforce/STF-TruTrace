package com.wwf.shrimp.application.client.android.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.models.dto.DocumentPage;
import com.wwf.shrimp.application.client.android.models.view.GalleryDocumentPage;
import com.wwf.shrimp.application.client.android.system.SessionData;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Adapter to load image pages remotely as thumbnails to reduce initial transfer rates
 *
 * @author AleaActaEst
 */
public class RemoteDocumentPageThumbnailImageAdapter extends BaseAdapter {
    private Context mContext;
    private SessionData globalVariable = null;
    private List<GalleryDocumentPage> mDataSet = new ArrayList<>();

    public RemoteDocumentPageThumbnailImageAdapter(Context c, List<GalleryDocumentPage> dataSet) {

        mContext = c;
        globalVariable = (SessionData) mContext.getApplicationContext();
        this.mDataSet = dataSet;
    }

    public int getCount() {

        return mDataSet.size();
    }

    public GalleryDocumentPage getItem(int position) {

        return mDataSet.get(position);
    }

    public void removeItem(int position){
        mDataSet.remove(position);
        notifyDataSetChanged();
    }

    public void removeAllDeletedItems(){

        Iterator iter = this.mDataSet.iterator();
        while(iter.hasNext()){
            GalleryDocumentPage page = (GalleryDocumentPage)iter.next();
            if(page.isDeleted()){
                iter.remove();
            }
        }
        notifyDataSetChanged();
    }

    public boolean isDeleted(int position){
        return mDataSet.get(position).isDeleted();
    }

    public void setDeleted(int position, boolean value){
        mDataSet.get(position).setDeleted(value);
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setBackgroundColor(Color.parseColor("#fbdcbb"));
        if(mDataSet.get(position).isDeleted()){
            imageView.setAlpha(0.3f);
        }else{
            imageView.setAlpha(1.0f);
        }

        imageView.setBackgroundResource(R.drawable.imageview_border);

        if(((GalleryDocumentPage)mDataSet.get(position).getPage()).getPage() instanceof DocumentPage) {
            DocumentPage data = (DocumentPage) ((GalleryDocumentPage)mDataSet.get(position).getPage()).getPage();
            //Loading image from below url into imageView
            String getUrl = globalVariable.getConfigurationData().getServerURL()
                    + globalVariable.getConfigurationData().getApplicationInfixURL()
                    + globalVariable.getConfigurationData().getRestDocumentFetchPageThumbnailByDocIdURL();
            Glide.with(mContext)
                    // .load(RESTUtils.REST_SERVER_URL + "/document/page?doc_id="+ mDataSet.get(position).getId())
                    .load(getUrl + data.getId())
                    .fitCenter()
                    .into(imageView);

        }
        if(((GalleryDocumentPage)mDataSet.get(position).getPage()).getPage() instanceof File) {
            File data =  (File) ((GalleryDocumentPage)mDataSet.get(position).getPage()).getPage();
            Glide.with(mContext)
                    // .load(document.getImagePath())
                    .load(data)
                    .fitCenter()
                    .into(imageView);
        }

        return imageView;
    }

}
