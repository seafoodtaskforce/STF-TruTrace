package com.wwf.shrimp.application.client.android.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.models.dto.DocumentPage;
import com.wwf.shrimp.application.client.android.models.view.GalleryDocumentPage;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.adapters.dynamicgrid.BaseDynamicGridAdapter;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ThumbnailDynamicAdapter extends BaseDynamicGridAdapter {
    private Context mContext;
    private SessionData globalVariable = null;

    public ThumbnailDynamicAdapter(Context context, List<?> items, int columnCount) {
        super(context, items, columnCount);

        mContext = context;
        globalVariable = (SessionData) mContext.getApplicationContext();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ThumbnailDynamicAdapter.ThumbnailViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_grid, null);
            holder = new ThumbnailDynamicAdapter.ThumbnailViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ThumbnailDynamicAdapter.ThumbnailViewHolder) convertView.getTag();
        }
        //holder.build(getItem(position).toString());
        holder.build(position , getItem(position));
        return convertView;
    }

     private class ThumbnailViewHolder {
        private TextView titleText;
        private ImageView image;

        private ThumbnailViewHolder(View view) {
            titleText = (TextView) view.findViewById(R.id.item_title);
            image = (ImageView) view.findViewById(R.id.item_img);
        }

        void build(String title) {
            titleText.setText(title);
            Glide.with(getContext())
                    // .load(RESTUtils.REST_SERVER_URL + "/document/page?doc_id="+ mDataSet.get(position).getId())
                    .load("http://3.86.84.130:8080/WWFShrimpProject_v2/api_v2/document/pagethumbnail?doc_id=10016")
                    .fitCenter()
                    .into(image);
            //new DownloadImageTask(image)
            //        .execute("http://3.86.84.130:8080/WWFShrimpProject_v2/api_v2/document/pagethumbnail?doc_id=10016");
        }

        void build(int position, Object item) {
            //titleText.setText(item.toString());
            titleText.setText("page " + (position + 1));
            Log.i("<Swap>", ""+position);
            //Glide.with(getContext())
            //        // .load(RESTUtils.REST_SERVER_URL + "/document/page?doc_id="+ mDataSet.get(position).getId())
            //        .load("http://3.86.84.130:8080/WWFShrimpProject_v2/api_v2/document/pagethumbnail?doc_id=10016")
            //        .fitCenter()
            //        .into(image);
            //new DownloadImageTask(image)
            //        .execute("http://3.86.84.130:8080/WWFShrimpProject_v2/api_v2/document/pagethumbnail?doc_id=10016");

            if(((GalleryDocumentPage) ((GalleryDocumentPage)item).getPage()).getPage() instanceof DocumentPage){
                DocumentPage data = (DocumentPage) ((GalleryDocumentPage) ((GalleryDocumentPage)item).getPage()).getPage();
                GalleryDocumentPage innerPage = (GalleryDocumentPage) ((GalleryDocumentPage)item).getPage();
                //Loading image from below url into imageView
                String getUrl = globalVariable.getConfigurationData().getServerURL()
                        + globalVariable.getConfigurationData().getApplicationInfixURL()
                        + globalVariable.getConfigurationData().getRestDocumentFetchPageThumbnailByDocIdURL();
                Glide.with(getContext())
                        // .load(RESTUtils.REST_SERVER_URL + "/document/page?doc_id="+ mDataSet.get(position).getId())
                        .load(getUrl + data.getId())
                        .fitCenter()
                        .into(image);
                //titleText.setText("page " + data.getPageNumber());
                titleText.setText("page " + innerPage.getPageNumber());


            }
            if(((GalleryDocumentPage) ((GalleryDocumentPage)item).getPage()).getPage() instanceof File) {
                File data =  (File) ((GalleryDocumentPage) ((GalleryDocumentPage)item).getPage()).getPage();
                GalleryDocumentPage innerPage = (GalleryDocumentPage) ((GalleryDocumentPage)item).getPage();
                Glide.with(getContext())
                        // .load(document.getImagePath())
                        .load(data)
                        .fitCenter()
                        .into(image);

                titleText.setText("page " + innerPage.getPageNumber());

                //titleText.setText("" + ((GalleryDocumentPage)item).getPage()).getPageNumber();
            }

        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}