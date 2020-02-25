package com.wwf.shrimp.application.client.android.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.adapters.DocumentPageImageAdapter;
import com.wwf.shrimp.application.client.android.dialogs.TabbedDocumentDialog;
import com.wwf.shrimp.application.client.android.models.dto.DocumentPage;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;

import java.io.File;

/**
 * Document Page Image fragment which will show the individual captured image
 * This fragment will be dynamically generated since we could have 0-n pages.
 * @author AleaActaEst
 */
public class CustomFragmentDocumentImagePage extends Fragment {

    // logging tag
    private static final String LOG_TAG = "Doc Image Page Fragment";

    // data tags
    protected static final String POSITION_DATA_KEY = "image position";


    private int dataMode = TabbedDocumentDialog.DATA_MODE_LOCAL;
    private int mPosition = -1;
    private File mImageFile;
    PhotoView photoView;

    // global session data
    private SessionData globalVariable;

    public static CustomFragmentDocumentImagePage createInstance(int dataMode, int imagePosition, File imageFile){
        CustomFragmentDocumentImagePage fragment;
        fragment  = new CustomFragmentDocumentImagePage();

        fragment.mPosition = imagePosition;
        fragment.mImageFile = imageFile;
        fragment.dataMode = dataMode;

        Log.d(LOG_TAG, "Create Instance Data: "
                + fragment.mPosition);
        return fragment;
    }

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // initialize the data

        Log.d(LOG_TAG, "Create Data: "
                + mPosition
                +
                (globalVariable == null ? " Session[OFF]":" Session[ON]")
                + "CLASS NAME: "
                + this.getClass().getName()

        );
        if (savedInstanceState != null) {
            this.mPosition = savedInstanceState.getInt(POSITION_DATA_KEY, -1);
        }
        this.setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(POSITION_DATA_KEY, this.mPosition);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.custom_document_dialog_fragment_page,container,false);
        photoView = (PhotoView) v.findViewById(R.id.photo_view);
        globalVariable  = (SessionData) getContext().getApplicationContext();
        Log.d(LOG_TAG, "Create View Data: "
                + mPosition
                +
                (globalVariable == null ? " Session[OFF]":" Session[ON]"));

        return v;
    }

    @Override
    public void onResume (){
        super.onResume();
        // show the view to the user
        int imagePosition = DocumentPOJOUtils.calculateImagePosition(globalVariable, mPosition);

        // determine the data mode
        dataMode = DocumentPOJOUtils.detectDataMode(globalVariable);
        // pProcess the data
        //if(dataMode == TabbedDocumentDialog.DATA_MODE_LOCAL) {
        if(DocumentPOJOUtils.decideImageSource(globalVariable, mPosition) == DocumentPOJOUtils.PAGE_SOURCE_GALLERY
                && globalVariable.getNextDocument().getImagePages().size() > 0){
            Log.d(LOG_TAG, "Loading page image locally: " + mPosition + " Image " + ((File)globalVariable.getNextDocument().getImagePages().get(imagePosition).getPage()).getName());
            Glide.with(getContext())
                    .load(globalVariable.getNextDocument().getImagePages().get(imagePosition).getPage())
                    .into(photoView);
        }
        // if(dataMode == TabbedDocumentDialog.DATA_MODE_REMOTE) {
        if(DocumentPOJOUtils.decideImageSource(globalVariable, mPosition) == DocumentPOJOUtils.PAGE_SOURCE_DOCUMENT
                && globalVariable.getNextDocument().getDocumentPages().size() > 0){
            String imageUrl = globalVariable.getConfigurationData().getServerURL()
                    + globalVariable.getConfigurationData().getApplicationInfixURL()
                    + globalVariable.getConfigurationData().getRestDocumentFetchPageByDocIdURL();

            Log.d(LOG_TAG, "Loading page image remotely: "
                    + imageUrl
                    + ((DocumentPage)globalVariable.getNextDocument().getDocumentPages().get(imagePosition).getPage()).getId());

            Glide.with(getContext())
                    .load(imageUrl + ((DocumentPage)globalVariable.getNextDocument().getDocumentPages().get(imagePosition).getPage()).getId())
                    .into(photoView);
        }

    }

    public int getPosition(){
        return this.mPosition;
    }

}
