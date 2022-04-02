package com.wwf.shrimp.application.client.android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.viethoa.RecyclerViewFastScroller;
import com.viethoa.models.AlphabetItem;
import com.wwf.shrimp.application.client.android.adapters.SelectedAttachedDocumentsListRecyclerViewAdapter;
import com.wwf.shrimp.application.client.android.adapters.ShowAttachedDocumentsListRecyclerViewAdapter;
import com.wwf.shrimp.application.client.android.adapters.helpers.AttachedDocumentItemDataHelper;
import com.wwf.shrimp.application.client.android.adapters.helpers.AttachedDocumentItemSelectionDataHelper;
import com.wwf.shrimp.application.client.android.dialogs.TabbedDocumentDialog;
import com.wwf.shrimp.application.client.android.fragments.AllDocumentsFragment;
import com.wwf.shrimp.application.client.android.fragments.MyDocumentsFragment;
import com.wwf.shrimp.application.client.android.fragments.ProfileDocumentsFragment;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.DocumentCollection;
import com.wwf.shrimp.application.client.android.models.dto.DocumentType;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.models.view.DocumentContext;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;
import com.wwf.shrimp.application.client.android.utils.dialogs.ErrorConnectingDialogUtility;
import com.wwf.shrimp.application.client.android.utils.listeners.ClickListener;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 */
public class DocumentAttachingActivity extends AppCompatActivity {

    // logging tag
    private static final String LOG_TAG = "Doc Attaching Activity";

    // global session data access
    private SessionData globalVariable = null;

    // progress bar
    private ProgressBar progressBar;

    // UI elements for the activity
    RecyclerView mRecyclerViewSelectedDocumentAttachmentsList;
    RecyclerView mRecyclerViewDocumentAttachmentsList;
    RecyclerViewFastScroller fastScrollerDocumentList;
    Button buttonCancelDocumentAttaching;
    Button buttonSaveDocumentAttaching;

    // List data for the UI visuals
    private List<AttachedDocumentItemDataHelper.AttachedDocumentDataCard> mDataArray;
    private List<AttachedDocumentItemSelectionDataHelper.AttachedDocumentDataCard> mSelectedDataArray;
    private List<AlphabetItem> mAlphabetItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalVariable  = (SessionData) getApplicationContext();
        setContentView(R.layout.activity_attachment_document);

        mRecyclerViewDocumentAttachmentsList = (RecyclerView) findViewById(R.id.recyclerViewDocumentAttachmentsList);
        fastScrollerDocumentList = (RecyclerViewFastScroller)findViewById(R.id.fast_scroller);

        mRecyclerViewSelectedDocumentAttachmentsList = (RecyclerView) findViewById(R.id.recyclerViewChosenDocumentAttachments);
        buttonCancelDocumentAttaching = (Button)findViewById(R.id.buttonCancelAttaching);
        buttonSaveDocumentAttaching = (Button)findViewById(R.id.buttonSaveAttaching);

        // progress bar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ResourcesCompat.getColor(getResources(), R.color.login_hint_highlight, null)
                , android.graphics.PorterDuff.Mode.SRC_IN);

        initialiseAttachmentDocsCollection();

        //initialiseDocumentListData();
        //initialiseSelectedAttachedDocumentsListData();
    }

    protected void initialiseDocumentListData() {
        /**
         * get data from the server about all documents
         */
        String url = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestDocumentFetchallDocsToAttachURL();
        Log.i(LOG_TAG, "REST - get all documents - call: " + url);

        // Create the handler
        DocumentAttachingActivity.OkHttpAttachedDocumentDataHandler okHttpHandler= new DocumentAttachingActivity.OkHttpAttachedDocumentDataHandler();

        // execute the call synchronously
        okHttpHandler.execute(url);


    }

    protected void initialiseSelectedAttachedDocumentsListData() {
        /**
         * get data from the server about links
         */
        String url;

        if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_EDIT
                || globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_CONTEXT_EDIT) {
            DocumentCardItem documentCardItem = globalVariable
                    .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                    .getDataSet()
                    .get(globalVariable.getLongClickPositionMyDocument());
            // Try to get data from the database

            url = globalVariable.getConfigurationData().getServerURL()
                    + globalVariable.getConfigurationData().getApplicationInfixURL()
                    + globalVariable.getConfigurationData().getRestDocumentFetchAttachedDocsBySyncIdURL()
                    + documentCardItem.getSyncID();
            Log.i(LOG_TAG, "REST - get all document attachments - call: " + url);

            // Create the handler
            DocumentAttachingActivity.OkHttpAttachedDocumentDataHandler okHttpHandler = new DocumentAttachingActivity.OkHttpAttachedDocumentDataHandler();

            // execute the call synchronously
            okHttpHandler.execute(url);
        }else{
            // get the data from the session about the document
            mSelectedDataArray = convertAttachedDocsToInternalDocs(globalVariable.getNextDocument().getAttachedDocuments());
        }
    }

    /**
     * Single call to get the collection of attachement data from the backend
     */
    protected void initialiseAttachmentDocsCollection(){
        /**
         * get data from the server about all documents
         */
        String url = null;

        progressBar.setVisibility(View.VISIBLE);

        // Existing docuemnt
        if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_EDIT
                || globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_CONTEXT_EDIT) {
            DocumentCardItem documentCardItem = globalVariable
                    .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                    .getDataSet()
                    .get(globalVariable.getLongClickPositionMyDocument());

            url = globalVariable.getConfigurationData().getServerURL()
                    + globalVariable.getConfigurationData().getApplicationInfixURL()
                    + globalVariable.getConfigurationData().getRestDocumentAttachmentFetchDocCollectionURL()
                    + documentCardItem.getSyncID();
        // New Document
        }else{

            url = globalVariable.getConfigurationData().getServerURL()
                    + globalVariable.getConfigurationData().getApplicationInfixURL()
                    + globalVariable.getConfigurationData().getRestDocumentAttachmentFetchDocCollectionURL()
                    + "0";
        }

        Log.i(LOG_TAG, "REST - get document attachment collection - call: " + url);

        // Create the handler
        DocumentAttachingActivity.OkHttpAttachedDocumentDataHandler okHttpHandler = new DocumentAttachingActivity.OkHttpAttachedDocumentDataHandler();

        // execute the call synchronously
        okHttpHandler.execute(url);
    }


    protected void initialiseUI() {

        // final syncing
        for(int i=0; i<mSelectedDataArray.size(); i++){
            int index = isSelectedDocument(mSelectedDataArray.get(i).getId());
            if( index != -1){
                mDataArray.get(index).setCheckState(true);
            }
        }

        mRecyclerViewDocumentAttachmentsList.setLayoutManager(new LinearLayoutManager(this));
        // create adapter
        final ShowAttachedDocumentsListRecyclerViewAdapter listAdapter = new ShowAttachedDocumentsListRecyclerViewAdapter(mDataArray, getApplicationContext());
        mRecyclerViewDocumentAttachmentsList.setAdapter(listAdapter);
        mRecyclerViewSelectedDocumentAttachmentsList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SelectedAttachedDocumentsListRecyclerViewAdapter selectedAdapter = new SelectedAttachedDocumentsListRecyclerViewAdapter(mSelectedDataArray);
        mRecyclerViewSelectedDocumentAttachmentsList.setAdapter(selectedAdapter);
        listAdapter.setSelectedAdapter(selectedAdapter);
        selectedAdapter.setListAdapter(listAdapter);

        mRecyclerViewDocumentAttachmentsList.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerViewDocumentAttachmentsList, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                DocumentCardItem documentCardItem;
                int dataLocalizationAccessMode;

                /**Toast.makeText(getApplicationContext()
                        , "Position clicked = " + position
                                + " Check Box status: " + mDataArray.get(position).isCheckState()
                        , Toast.LENGTH_SHORT).show();
                 */
                // get the file first
                // get the current data object from the main adapter
                AttachedDocumentItemDataHelper.AttachedDocumentDataCard pickedItem = listAdapter.getDataSet().get(position);


                //
                // Search through all adapters
                documentCardItem = globalVariable.getDocumentAdapter(AllDocumentsFragment.RECYCLER_ADAPTER_KEY).findCardItemBySyncId(pickedItem.getSyncId());
                dataLocalizationAccessMode = TabbedDocumentDialog.DATA_MODE_REMOTE;
                if(documentCardItem == null){
                    documentCardItem = globalVariable.getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY).findCardItemBySyncId(pickedItem.getSyncId());
                    dataLocalizationAccessMode= TabbedDocumentDialog.DATA_MODE_LOCAL;
                }
                if(documentCardItem == null){
                    documentCardItem = globalVariable.getDocumentAdapter(ProfileDocumentsFragment.RECYCLER_ADAPTER_KEY).findCardItemBySyncId(pickedItem.getSyncId());
                    dataLocalizationAccessMode= TabbedDocumentDialog.DATA_MODE_LOCAL;
                }

                //
                // Load context data for the card
                DocumentContext context = new DocumentContext();
                context.setContextName(DocumentContext.DOCUMENT_ATTACHING_ACTIVITY_NAME);
                context.setAdapter(listAdapter);
                context.setDataSet(listAdapter.getDataSet());
                context.setListPosition(position);
                context.setDataItem(pickedItem);
                context.setView(view);

                if(documentCardItem != null){
                    documentCardItem.setContext(context);
                }
                // display the image
                //
                // showFullDocumentDialog(documentCardItem);

                showFullDocumentPagedDialog(documentCardItem, TabbedDocumentDialog.DIALOG_MODE_READ_ONLY, dataLocalizationAccessMode);

            }

            @Override
            public void onLongClick(View view, int position) {
                /**Toast.makeText(getApplicationContext(), "Long press on position :"+position,
                        Toast.LENGTH_LONG).show();
                 */
            }
        }));

        fastScrollerDocumentList.setRecyclerView(mRecyclerViewDocumentAttachmentsList);
        fastScrollerDocumentList.setUpAlphabet(mAlphabetItems);

        // buttons
        buttonCancelDocumentAttaching.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cancel the operation by going back
                onBackPressed();
            }
        });
        buttonSaveDocumentAttaching.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * execute the POST action
                 */

                // convert data
                List<Document> docs = convertAttachedDocumentFromInternalDocument(mSelectedDataArray);

                //////////////////////////////////////////////////////////////////////////////
                // Edit content
                //
                if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_EDIT) {
                    // Save the data for the document
                    DocumentCardItem documentCardItem = globalVariable
                            .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                            .getDataSet()
                            .get(globalVariable.getLongClickPositionMyDocument());

                    //
                    // Commit the data to REST Service
                    String postUrl = globalVariable.getConfigurationData().getServerURL()
                            + globalVariable.getConfigurationData().getApplicationInfixURL()
                            + globalVariable.getConfigurationData().getRestDocumentAttachAttachmentDocsBySyncIdURL();
                    Log.i(LOG_TAG, "REST - attach documents - call: " + postUrl);
                    Log.d(LOG_TAG, "Documents being attached " + docs);

                    String postBody = RESTUtils.getJSON(docs);
                    RESTUtils.executePOSTDocumentSyncDocumentLinkAttachmentRequest(postUrl, postBody, documentCardItem.getSyncID(), "", globalVariable, "");

                    globalVariable
                            .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                            .getDataSet()
                            .get(globalVariable.getLongClickPositionMyDocument()).setAttachedDocuments(docs);

                    globalVariable
                            .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                            .notifyDataSetChanged();
                }else{
                //////////////////////////////////////////////////////////////////////////////
                // Create content
                //
                    globalVariable.getNextDocument().setAttachedDocuments(docs);

                }

                onBackPressed();
            }
        });
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
                // Prevent the double click on the card
                return true;
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    /**
     * Inner class to handle the back-end communication to get the data from the db
     */
    public class OkHttpAttachedDocumentDataHandler extends AsyncTask<String, String, String> {

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(globalVariable.getConfigurationData().getClientSocketTimeoutInSeconds(), TimeUnit.SECONDS)
                .build();
        boolean serverDownFlag = false;
        String requestURL;

        @Override
        protected String doInBackground(String... params) {

            Request request  = new Request.Builder()
                    .url(params[0])
                    .addHeader("user-name", globalVariable.getCurrentUser().getName())
                    .build();

            requestURL = request.url().toString();
            Log.d(LOG_TAG, "OkHTTP Request " + request.url());

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            }catch (Exception e){
                Log.e(LOG_TAG, "HTTP call failed.");
                e.printStackTrace();
                serverDownFlag = true;
                progressBar.setVisibility(View.INVISIBLE);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //
            // extract the data for the GSON authentication

            // documents holder for fetching all documents
            List<Document> documents;
            List<Document> myDocuments;
            List<Document> attachedByDoc;

            //
            // GSON parser
            Gson gson = new GsonBuilder()
                    .create();

            //
            // parse the JSON input into the specific class

            ///////////////////////////////////////////////////////////////////////////////////////
            // Getting All Documents from the backend
            //
            if(requestURL.contains(globalVariable.getConfigurationData().getRestDocumentFetchallDocsToAttachURL())) {
                Type listType = new TypeToken<ArrayList<Document>>() {
                }.getType();
                documents = gson.fromJson(s, listType);
                myDocuments = new ArrayList<>();
                Log.d(LOG_TAG, "REST - all fetched documents request: " + requestURL);
                Log.d(LOG_TAG, "REST - all fetched documents: " + documents);

                DocumentCardItem documentCardItem = null;

                if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_EDIT) {

                    // filter documents first to only get your own docs
                    // and not the current doc
                    documentCardItem = globalVariable
                            .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                            .getDataSet()
                            .get(globalVariable.getLongClickPositionMyDocument());
                    int startingSize = documents.size();
                    for(int i=0; i< startingSize; i++){
                        if(documents.get(i).getOwner().equals(globalVariable.getCurrentUser().getCredentials().getUsername())){
                            if(!documentCardItem.getSyncID().equals(documents.get(i).getSyncID())){
                                myDocuments.add(documents.get(i));
                            }
                        }
                    }
                    documents = myDocuments;
                }else{
                    // filter documents first to only get your own docs
                    // and not the current doc
                    documentCardItem = globalVariable.getNextDocument();
                    int startingSize = documents.size();
                    for(int i=0; i< startingSize; i++){
                        if(documents.get(i).getOwner().equals(globalVariable.getCurrentUser().getCredentials().getUsername())){
                            myDocuments.add(documents.get(i));
                        }
                    }
                    documents = myDocuments;
                }

                // sort the entities by their type
                Collections.sort(documents, new Comparator<Document>() {
                    @Override
                    public int compare(Document lhs, Document rhs) {
                        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                        int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getDocumentType(), rhs.getDocumentType());
                        if (res == 0) {
                            res = lhs.getOwner().compareTo(rhs.getOwner());
                        }
                        return res;
                    }
                });

                // convert session data
                mDataArray = convertAttachedDocs(documents);
                //Alphabet fast scroller data
                mAlphabetItems = new ArrayList<>();
                List<String> strAlphabets = new ArrayList<>();
                for (int i = 0; i < mDataArray.size(); i++) {
                    String name = mDataArray.get(i).getAttachedDocText();
                    if (name == null || name.trim().isEmpty())
                        continue;

                    String word = name.substring(0, 1);
                    if (!strAlphabets.contains(word)) {
                        strAlphabets.add(word);
                        mAlphabetItems.add(new AlphabetItem(i, word, false));
                    }
                }

                // get the data from the session about the document
                mSelectedDataArray = new ArrayList<>();
            }

            ///////////////////////////////////////////////////////////////////////////////////////
            // Getting all the attached documents
            //
            if(requestURL.contains(globalVariable.getConfigurationData().getRestDocumentFetchAttachedDocsBySyncIdURL())){
                Type listType = new TypeToken<ArrayList<Document>>() {
                }.getType();
                attachedByDoc = gson.fromJson(s, listType);
                Log.d(LOG_TAG, "REST - all attached documents request: " + requestURL);
                Log.d(LOG_TAG, "REST - all attached documents: " + attachedByDoc);

                // sort the entities by their date
                Collections.sort(attachedByDoc, new Comparator<Document>() {
                    @Override
                    public int compare(Document lhs, Document rhs) {
                        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                        int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getDocumentType(), rhs.getDocumentType());
                        if (res == 0) {
                            res = lhs.getDocumentType().compareTo(rhs.getDocumentType());
                        }
                        return res;
                    }
                });

                // get the data from the session about the document
                mSelectedDataArray = convertAttachedDocsToInternalDocs(attachedByDoc);

            }



            /////////////////////////////////////////////////////////////////////////////////////////
            // Getting the attachment doc collection
            //
            if(requestURL.contains(globalVariable.getConfigurationData().getRestDocumentAttachmentFetchDocCollectionURL())){

                String jsonDocumentCollection = s;
                DocumentCollection collection = (DocumentCollection) RESTUtils.getObjectFromJSON(jsonDocumentCollection, DocumentCollection.class);

                //
                // Docs to attach
                documents = collection.getAllDocsToAttach();
                myDocuments = new ArrayList<>();
                Log.d(LOG_TAG, "REST - all fetched documents request: " + requestURL);
                //Log.d(LOG_TAG, "REST - all fetched documents: " + documents);

                DocumentCardItem documentCardItem = null;

                if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_EDIT) {

                    // filter documents first to only get your own docs
                    // and not the current doc
                    documentCardItem = globalVariable
                            .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                            .getDataSet()
                            .get(globalVariable.getLongClickPositionMyDocument());
                    int startingSize = documents.size();
                    for(int i=0; i< startingSize; i++){
                        if(documents.get(i).getOwner().equals(globalVariable.getCurrentUser().getCredentials().getUsername())){
                            if(!documentCardItem.getSyncID().equals(documents.get(i).getSyncID())){
                                myDocuments.add(documents.get(i));
                            }
                        }
                    }
                    documents = myDocuments;
                }else{
                    // filter documents first to only get your own docs
                    // and not the current doc
                    documentCardItem = globalVariable.getNextDocument();
                    List<Document> outBoxBackingDocs = new ArrayList<Document>();
                    int startingSize = documents.size();
                    for(int i=0; i< startingSize; i++){
                        if(documents.get(i).getOwner().equals(globalVariable.getCurrentUser().getCredentials().getUsername())){
                            myDocuments.add(documents.get(i));
                            if(documents.get(i).getType().getDocumentDesignation().equals(DocumentType.DESIGNATION_PROFILE)){
                                outBoxBackingDocs.add(documents.get(i));
                            }
                        }
                    }
                    documents = myDocuments;

                    //
                    // get the attached docs out of box for the backing docs for this user
                    globalVariable.getNextDocument().setAttachedDocuments(outBoxBackingDocs);


                }
                if(documents.size() ==0){
                    Toast.makeText(globalVariable.getBaseContext(), "There are *NO* backing documents", Toast.LENGTH_SHORT).show();
                }
                // sort the entities by their type
                Collections.sort(documents, new Comparator<Document>() {
                    @Override
                    public int compare(Document lhs, Document rhs) {
                        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                        int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getDocumentType(), rhs.getDocumentType());
                        if (res == 0) {
                            res = lhs.getOwner().compareTo(rhs.getOwner());
                        }
                        return res;
                    }
                });

                // convert session data
                mDataArray = convertAttachedDocs(documents);
                //Alphabet fast scroller data
                mAlphabetItems = new ArrayList<>();
                List<String> strAlphabets = new ArrayList<>();
                for (int i = 0; i < mDataArray.size(); i++) {
                    String name = mDataArray.get(i).getAttachedDocText();
                    if (name == null || name.trim().isEmpty())
                        continue;

                    String word = name.substring(0, 1);
                    if (!strAlphabets.contains(word)) {
                        strAlphabets.add(word);
                        mAlphabetItems.add(new AlphabetItem(i, word, false));
                    }
                }

                // get the data from the session about the document
                mSelectedDataArray = new ArrayList<>();

                //
                // Attached docs
                if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_EDIT
                        || globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_CONTEXT_EDIT) {

                    attachedByDoc = collection.getAttachedDocs();
                    Log.d(LOG_TAG, "REST - all attached documents request: " + requestURL);
                    Log.d(LOG_TAG, "REST - all attached documents: " + attachedByDoc);

                    // sort the entities by their date
                    Collections.sort(attachedByDoc, new Comparator<Document>() {
                        @Override
                        public int compare(Document lhs, Document rhs) {
                            // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                            int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getDocumentType(), rhs.getDocumentType());
                            if (res == 0) {
                                res = lhs.getDocumentType().compareTo(rhs.getDocumentType());
                            }
                            return res;
                        }
                    });

                    // get the data from the session about the document
                    mSelectedDataArray = convertAttachedDocsToInternalDocs(attachedByDoc);

                }else{
                    // get the data from the session about the document
                    mSelectedDataArray = convertAttachedDocsToInternalDocs(globalVariable.getNextDocument().getAttachedDocuments());
                }
            }


            progressBar.setVisibility(View.INVISIBLE);


            initialiseUI();
        }

    }

    private List<AttachedDocumentItemDataHelper.AttachedDocumentDataCard> convertAttachedDocs(List<Document> docs){
        List<AttachedDocumentItemDataHelper.AttachedDocumentDataCard> result = new ArrayList<>();

        for(int i=0; i< docs.size(); i++){
            Document doc = docs.get(i);
            AttachedDocumentItemDataHelper.AttachedDocumentDataCard item = new AttachedDocumentItemDataHelper.AttachedDocumentDataCard(doc.getDocumentType(), doc.getOwner(), false);
            item.setId(doc.getId());
            item.setTimestamp(doc.getCreationTimestamp());
            item.setDocumentType(doc.getType());
            item.setSyncId(doc.getSyncID());
            //int docTypeID = this.getResources().
            //        getIdentifier(docs.get(i).getType().getName(), "string", this.getPackageName());
            //String documentType = getResources().getString(docTypeID);

            String documentType = globalVariable.getInternationalizedResourceString(docs.get(i).getType().getName());
            item.setAttachedDocText(documentType);


            result.add(item);
        }

        return result;
    }

    private List<AttachedDocumentItemSelectionDataHelper.AttachedDocumentDataCard> convertAttachedDocsToInternalDocs(List<Document> docs){
        List<AttachedDocumentItemSelectionDataHelper.AttachedDocumentDataCard> result = new ArrayList<AttachedDocumentItemSelectionDataHelper.AttachedDocumentDataCard>();

        for(int i=0; i< docs.size(); i++){
            Document doc = docs.get(i);
            //int docTypeID = this.getResources().
            //        getIdentifier(docs.get(i).getType().getName(), "string", this.getPackageName());
            //String documentType = getResources().getString(docTypeID);

            String documentType = globalVariable.getInternationalizedResourceString(docs.get(i).getType().getName());
            AttachedDocumentItemSelectionDataHelper.AttachedDocumentDataCard item = new AttachedDocumentItemSelectionDataHelper.AttachedDocumentDataCard(doc.getId(), documentType, doc.getOwner(), doc.getType());
            item.setSyncId(doc.getSyncID());

            result.add(item);
        }

        return result;
    }

    private List<Document> convertAttachedDocumentFromInternalDocument(List<AttachedDocumentItemSelectionDataHelper.AttachedDocumentDataCard> docs){
        List<Document> result = new ArrayList<>();

        for(int i=0; i< docs.size(); i++){
            AttachedDocumentItemSelectionDataHelper.AttachedDocumentDataCard doc = docs.get(i);
            Document item = new Document();
            item.setId(doc.getId());
            item.setOwner(doc.getOwner());
            item.setSyncID(doc.getSyncId());
            item.setDocumentType(doc.getAttachedDocText());
            item.setType(doc.getDocType());

            result.add(item);
        }

        return result;
    }

    private int isSelectedDocument(long documentId){
        int result = -1;
        for(int i=0; i<mDataArray.size(); i++){
            if(mDataArray.get(i).getId() == documentId){
                return i;
            }
        }

        return result;
    }

    /**
     * Will show the paged dialog to the
     * @param document
     */
    protected void showFullDocumentPagedDialog(final DocumentCardItem document, int dataMode, int localizationMode) {
        if(document == null){
            ErrorConnectingDialogUtility.showUnsyncedDocumentErrorDialog(this);
            return;
        }


        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // set the document to be worked on the session
        globalVariable.setNextDocument(document);

        // Create and show the dialog.
        TabbedDocumentDialog dialogFragment = new TabbedDocumentDialog();
        dialogFragment.setDialogMode(dataMode);
        // dialogFragment.setContainerFragment(this);
        // dialogFragment.setDataMode(localizationMode);
        // experimental
        dialogFragment.setDataMode(TabbedDocumentDialog.DATA_MODE_REMOTE);
        dialogFragment.show(ft,"dialog");
    }
}
