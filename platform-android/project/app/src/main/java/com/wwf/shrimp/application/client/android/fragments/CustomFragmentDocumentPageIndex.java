package com.wwf.shrimp.application.client.android.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
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
import com.wwf.shrimp.application.client.android.adapters.ThumbnailDynamicAdapter;
import com.wwf.shrimp.application.client.android.dialogs.TabbedDocumentDialog;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.DocumentPage;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.models.view.GalleryDocumentPage;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils;
import com.wwf.shrimp.application.client.android.utils.adapters.dynamicgrid.BaseDynamicGridAdapter;
import com.wwf.shrimp.application.client.android.utils.adapters.dynamicgrid.DynamicGridView;

import java.io.File;
import java.util.ArrayList;
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
    private DynamicGridView gridview = null;
    private Button pageDeleteButton;

    private FloatingActionButton fabSwapDocImages;

    // swap toggle button
    private boolean pageSwapFlag = false;



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
        //pageDeleteButton = v.findViewById(R.id.buttonDocumentDialogPageDelete);

        //fabSwapDocImages = (FloatingActionButton) v.findViewById(R.id.fabSwapImages);

        // TODO make the position dynamic and based on presence/absence of Notes Tab

        gridview.setOnDragListener(new DynamicGridView.OnDragListener() {
            @Override
            public void onDragStarted(int position) {
                Log.d("Gallery", "drag started at position " + position);
            }

            @Override
            public void onDragPositionsChanged(int oldPosition, int newPosition) {
                Log.d("Gallery", String.format("drag item position changed from %d to %d", oldPosition, newPosition));
                swapPages(oldPosition, newPosition);
            }
        });

        refreshUI();


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
                        //
                        //
                        GalleryDocumentPage item = (GalleryDocumentPage) (getGridViewAdapter().getItem(position));
                        markCurrentDocPageStatus(item
                                , true, position);

                    }else{
                        arg1.setAlpha(1f);
                        globalVariable.setNumberofGalleryPagesToDelete(globalVariable.getNumberofGalleryPagesToDelete()-1);
                        getGridViewAdapter().setDeleted(position, false);
                        //
                        //
                        GalleryDocumentPage item = (GalleryDocumentPage) (getGridViewAdapter().getItem(position));
                        markCurrentDocPageStatus(item
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

    @Override
    public void setMenuVisibility(final boolean visible)
    {
        super.setMenuVisibility(visible);
        if (visible){
            if(fabSwapDocImages != null && globalVariable != null) {
                if (globalVariable.getNextDocument().getStatus().equals(Document.STATUS_DRAFT)
                        || globalVariable.getNextDocument().getStatus().equals(Document.STATUS_REJECTED)) {
                    fabSwapDocImages.setVisibility(View.VISIBLE);
                } else {
                    fabSwapDocImages.setVisibility(View.INVISIBLE);
                }
            }
        }else{
            if(fabSwapDocImages != null){
                fabSwapDocImages.setVisibility(View.INVISIBLE);
            }

        }
    }

    private void refreshUI() {

        fabSwapDocImages = getParentFragment().getView().findViewById(R.id.fabSwapImages);

        /**
         * Swap routine for page images in the gallery
         */
        fabSwapDocImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set the swaping mode for pages

                // toggle the swap
                pageSwapFlag = !pageSwapFlag;
                if(pageSwapFlag == true){
                    fabSwapDocImages.setImageResource(R.drawable.outline_swap_horizontal_circle_white);
                    gridview.startEditMode();
                }else{
                    fabSwapDocImages.setImageResource(R.drawable.outline_apps_white);
                    if (gridview.isEditMode()) {
                        gridview.stopEditMode();
                        //
                        // Set the values for pages in

                        // loop through all grid values
                        for(int i=0; i< gridview.getAdapter().getCount(); i++){
                            GalleryDocumentPage page = (GalleryDocumentPage)((GalleryDocumentPage) gridview.getAdapter().getItem(i)).getPage();
                            GalleryDocumentPage item = (GalleryDocumentPage) (getGridViewAdapter().getItem(i));
                            // Find the corresponding doc page
                            markSwappedPageNumber(item,i);
                        }


                        // change the underlying page data
                    }
                }

            }
        });

        //
        // Get the latest page data
        List<GalleryDocumentPage> pageData = new ArrayList<>();
        // populate the data
        pageData = getNewestPageData(globalVariable);

        //
        // calculate the width of each thumbnail
        Log.i("Gallery: ", "GALLERY DATA : Width: " + gridview.getMeasuredWidth());

        // get the width of the grid

        // we want three columns

        //gridview.setAdapter(new ThumbnailDynamicAdapter(getContext(),
        //        new ArrayList<String>(Arrays.asList(Cheeses.sCheeseStrings)),
        //        3));

        gridview.setAdapter(new ThumbnailDynamicAdapter(getContext(), pageData,3));

        // process
        //gridview.setNumColumns(3);
        //gridview.setAdapter(
        //        new RemoteDocumentPageThumbnailImageAdapter(getContext(), pageData, 3));
    }

    BaseDynamicGridAdapter getGridViewAdapter(){
        return (BaseDynamicGridAdapter) gridview.getAdapter();
    }

    public void setAdapter(List<GalleryDocumentPage> pageData, Context context, int columns){
        if(gridview != null){
            gridview.setAdapter(new ThumbnailDynamicAdapter(context, pageData,columns));
            getGridViewAdapter().notifyDataSetChanged();
        }
    }

    /**
     * Mark a page as either deleted or not.
     * @param page
     * @param isDeleted
     */
    void markCurrentDocPageStatus(GalleryDocumentPage page, boolean isDeleted, int position){
        page.setPosition(position);

        // set the inner page
        Object innerPage = ((GalleryDocumentPage) page.getPage()).getPage();
        if(  innerPage instanceof DocumentPage) {
            for (int i = 0; i < globalVariable.getNextDocument().getDocumentPages().size(); i++) {
                if (((DocumentPage) globalVariable.getNextDocument().getDocumentPages().get(i).getPage()).getId()
                        == ((DocumentPage) innerPage).getId()) {
                    globalVariable.getNextDocument().getDocumentPages().get(i).setDeleted(isDeleted);
                    return;
                }
            }
        }
        if(innerPage instanceof File) {
            for (int i = 0; i < globalVariable.getNextDocument().getImagePages().size(); i++) {
                if (globalVariable.getNextDocument().getImagePages().get(i).getPage().equals(innerPage)) {
                    globalVariable.getNextDocument().getImagePages().get(i).setDeleted(isDeleted);
                    return;
                }
            }
        }

        return;
    }

    /**
     * Swap the page markers to refelct the changes done to the
     * pages during actual swap operation
     * @param page - the swapped page
     * @param position - the position
     */
    void markSwappedPageNumber(GalleryDocumentPage page, int position){

        // set the inner page
        Object innerPage = ((GalleryDocumentPage) page.getPage()).getPage();
        if(  innerPage instanceof DocumentPage) {
            for (int i = 0; i < globalVariable.getNextDocument().getDocumentPages().size(); i++) {
                if (((DocumentPage) globalVariable.getNextDocument().getDocumentPages().get(i).getPage()).getId()
                        == ((DocumentPage) innerPage).getId()) {
                    globalVariable.getNextDocument().getDocumentPages().get(i).setPageNumber(position + 1);
                    return;
                }
            }
        }
        if(innerPage instanceof File) {
            for (int i = 0; i < globalVariable.getNextDocument().getImagePages().size(); i++) {
                if (globalVariable.getNextDocument().getImagePages().get(i).getPage().equals(innerPage)) {
                    globalVariable.getNextDocument().getImagePages().get(i).setPageNumber(position + 1);
                    return;
                }
            }
        }

        return;
    }

    void swapPages(int startPos, int endPos){
        Log.i("Page Swap: ", " --- start" + startPos);
        Log.i("Page Swap: ", " --- end" + endPos);
    }

    public List<GalleryDocumentPage> getNewestPageData(SessionData globalVariable) {
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
        // recollate pages
        pageData = DocumentPOJOUtils.collatePages(pageData);

        return pageData;
    }
}
