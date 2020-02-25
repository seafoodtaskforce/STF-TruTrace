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
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.system.SessionData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an adapter for the images that represent document pages.
 * @author AleaActaEst
 */

public class DocumentPageImageAdapter extends BaseAdapter {
    private Context mContext;
    private List<File> mDataSet = new ArrayList<>();

    public DocumentPageImageAdapter(Context c, List<File> dataSet) {

        mContext = c;
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

        Glide.with(mContext)
                // .load(document.getImagePath())
                .load(mDataSet.get(position))
                .into(imageView);

        // imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

}
