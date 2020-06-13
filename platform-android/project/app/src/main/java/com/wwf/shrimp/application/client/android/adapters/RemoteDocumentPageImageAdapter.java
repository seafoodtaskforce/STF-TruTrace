package com.wwf.shrimp.application.client.android.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.wwf.shrimp.application.client.android.models.dto.DocumentPage;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter to load image pages remotely
 * @author AleaActaEst
 */
public class RemoteDocumentPageImageAdapter extends BaseAdapter {
    private Context mContext;
    private SessionData globalVariable = null;
    private List<DocumentPage> mDataSet = new ArrayList<>();

    public RemoteDocumentPageImageAdapter(Context c, List<DocumentPage> dataSet) {

        mContext = c;
        globalVariable = (SessionData) mContext.getApplicationContext();
        this.mDataSet = dataSet;
    }

    public int getCount() {

        return mDataSet.size();
    }

    public Object getItem(int position) {

        return mDataSet.get(position);
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
            imageView.setLayoutParams(new GridView.LayoutParams(400, 400));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        //Loading image from below url into imageView
        String getUrl = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestDocumentFetchPageByDocIdURL();
        Glide.with(mContext)
                // .load(RESTUtils.REST_SERVER_URL + "/document/page?doc_id="+ mDataSet.get(position).getId())
                .load(getUrl + mDataSet.get(position).getId())
                .fitCenter()
                .into(imageView);

        // imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

}
