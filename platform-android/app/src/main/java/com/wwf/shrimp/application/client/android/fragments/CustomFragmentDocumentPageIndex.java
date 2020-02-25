package com.wwf.shrimp.application.client.android.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.adapters.CustomPagedDocumentDialogAdapter;
import com.wwf.shrimp.application.client.android.adapters.DocumentPageImageAdapter;
import com.wwf.shrimp.application.client.android.adapters.RemoteDocumentPageThumbnailImageAdapter;
import com.wwf.shrimp.application.client.android.dialogs.TabbedDocumentDialog;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.DocumentPage;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.models.view.GalleryDocumentPage;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Document Page Index fragment which will show the page gallery.
 * @author AleaActaEst
 */
public class CustomFragmentDocumentPageIndex extends Fragment {
    private int dataMode = TabbedDocumentDialog.DATA_MODE_LOCAL;
    private int pages = 0;
    private CustomPagedDocumentDialogAdapter mAdapter;
    // global session data
    private SessionData globalVariable;
    private GridView gridview=null;
    private Button pageDeleteButton;



    public static CustomFragmentDocumentPageIndex createInstance(int dataMode
            , CustomPagedDocumentDialogAdapter adapter
            , DocumentCardItem document){
        Log.d("Gallery", "createInstance: Gallery is being Created");
        CustomFragmentDocumentPageIndex fragment = new CustomFragmentDocumentPageIndex();
        fragment.dataMode = dataMode;
        fragment.mAdapter = adapter;
        return fragment;
    }

    @Override
    public void onStart (){
        super.onStart();
        // check if this is being called
        Log.d("Gallery", "onStart: Gallery is being Started");
    }

    @Override
    public void onPause (){
        super.onPause();
        // check if this is being called
        Log.d("Gallery", "onPause: Gallery is being Paused");
    }

    @Override
    public void onStop (){
        super.onStop();
        // check if this is being called
        Log.d("Gallery", "onStop: Gallery is being Stopped");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.custom_document_dialog_fragment_index,container,false);
        globalVariable  = (SessionData) getContext().getApplicationContext();
        gridview = v.findViewById(R.id.gridview);
        pageDeleteButton = v.findViewById(R.id.buttonDocumentDialogPageDelete);

        // TODO make the position dynamic and based on presence/absence of Notes Tab


        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                TabLayout tabHost = (TabLayout) getParentFragment().getView().findViewById(R.id.tabLayout);
                tabHost.getTabAt(position+3).select();
            }
        });

        // Set Long-Clickable
        gridview.setLongClickable(true);
        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @SuppressLint("NewApi")
            public boolean onItemLongClick(AdapterView<?> parent, View arg1,
                                           int position, long arg3) {
                if(globalVariable.getNextDocument().getStatus().equals(Document.STATUS_DRAFT)
                        || globalVariable.getNextDocument().getStatus().equals(Document.STATUS_REJECTED)){
                    // TODO Auto-generated method stub
                    Toast.makeText(getContext(), "Long Click" + position,
                            Toast.LENGTH_SHORT).show();

                    //if(arg1.getAlpha() == 1f){
                    if(!getGridViewAdapter().isDeleted(position)){
                        getGridViewAdapter().setDeleted(position, true);
                        arg1.setAlpha(0.3f);
                        globalVariable.setNumberofGalleryPagesToDelete(globalVariable.getNumberofGalleryPagesToDelete()+1);
                        markCurrentDocPageStatus((GalleryDocumentPage)getGridViewAdapter().getItem(position).getPage()
                                , true, position);

                    }else{
                        arg1.setAlpha(1f);
                        globalVariable.setNumberofGalleryPagesToDelete(globalVariable.getNumberofGalleryPagesToDelete()-1);
                        getGridViewAdapter().setDeleted(position, false);
                        markCurrentDocPageStatus((GalleryDocumentPage)getGridViewAdapter().getItem(position).getPage()
                                , false, position);
                    }
                    pageDeleteButton = (Button) getParentFragment().getView().findViewById(R.id.buttonDocumentDialogPageDelete);


                    Toast.makeText(getContext(), "Pages To Delete: " + globalVariable.getNumberofGalleryPagesToDelete(),
                            Toast.LENGTH_SHORT).show();

                    if(globalVariable.getNumberofGalleryPagesToDelete() == 0){
                        pageDeleteButton.setVisibility(View.GONE);
                    }else{
                        pageDeleteButton.setVisibility(View.VISIBLE);
                    }
                }
                return true;
            }
        });

        return v;
    }

    public GridView getGridview() {
        return gridview;
    }

    @Override
    public void onResume()
    {   super.onResume();
        //Refresh your stuff here
        Log.d("Gallery", "onResume: Gallery is being Resumed");
        refreshUI();
    }

    private void refreshUI() {
        //
        // Data
        int thumbnailDim = 300;
        int columns = 3;


        List<GalleryDocumentPage> pageData = new ArrayList<>();
        // populate the data
        for(int i=0; i< globalVariable.getNextDocument().getDocumentPages().size(); i++){
            pageData.add(new GalleryDocumentPage(globalVariable.getNextDocument().getDocumentPages().get(i),
                    globalVariable.getNextDocument().getDocumentPages().get(i).isDeleted()));
        }
        for(int i=0; i< globalVariable.getNextDocument().getImagePages().size(); i++){
            pageData.add(new GalleryDocumentPage(globalVariable.getNextDocument().getImagePages().get(i),
                    globalVariable.getNextDocument().getImagePages().get(i).isDeleted()));
        }

        //
        // calculate the width of each thumbnail
        Log.i("Gallery: ", "GALLERY DATA : Width: " + gridview.getMeasuredWidth());

        // get the width of the grid

        // we want three columns




        // process
        gridview.setNumColumns(3);
        gridview.setAdapter(
                new RemoteDocumentPageThumbnailImageAdapter(getContext(), pageData));
    }
    RemoteDocumentPageThumbnailImageAdapter getGridViewAdapter(){
        return (RemoteDocumentPageThumbnailImageAdapter) gridview.getAdapter();
    }

    /**
     * Mark a page as either deleted or not.
     * @param page
     * @param isDeleted
     */
    void markCurrentDocPageStatus(GalleryDocumentPage page, boolean isDeleted, int position){
        page.setPosition(position);
        if(page.getPage() instanceof DocumentPage) {
            for (int i = 0; i < globalVariable.getNextDocument().getDocumentPages().size(); i++) {
                if (((DocumentPage) globalVariable.getNextDocument().getDocumentPages().get(i).getPage()).getId()
                        == ((DocumentPage) page.getPage()).getId()) {
                    globalVariable.getNextDocument().getDocumentPages().get(i).setDeleted(isDeleted);
                    return;
                }
            }
        }
        if(page.getPage() instanceof File) {
            for (int i = 0; i < globalVariable.getNextDocument().getImagePages().size(); i++) {
                if (globalVariable.getNextDocument().getImagePages().get(i).getPage().equals(page.getPage())) {
                    globalVariable.getNextDocument().getImagePages().get(i).setDeleted(isDeleted);
                    return;
                }
            }
        }

        return;
    }

}
