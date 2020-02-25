package com.wwf.shrimp.application.client.android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.wwf.shrimp.application.client.android.adapters.SelectedLinkedDocumentsListRecyclerViewAdapter;
import com.wwf.shrimp.application.client.android.adapters.ShowLinkedDocumentsListRecyclerViewAdapter;
import com.wwf.shrimp.application.client.android.adapters.helpers.LinkedDocumentItemDataHelper;
import com.wwf.shrimp.application.client.android.adapters.helpers.LinkedDocumentItemSelectionDataHelper;
import com.wwf.shrimp.application.client.android.dialogs.TabbedDocumentDialog;
import com.wwf.shrimp.application.client.android.fragments.AllDocumentsFragment;
import com.wwf.shrimp.application.client.android.fragments.MyDocumentsFragment;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.DocumentCollection;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.models.view.DocumentContext;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;
import com.wwf.shrimp.application.client.android.utils.dialogs.ErrorConnectingDialogUtility;
import com.wwf.shrimp.application.client.android.utils.listeners.ClickListener;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Activity which will allow documents to be linked to other documents.
 * @author AleaActaEst
 */
public class DocumentLinkingActivity extends AppCompatActivity {

    // logging tag
    private static final String LOG_TAG = "Doc Linking Activity";

    // global session data access
    private SessionData globalVariable = null;

    // progress bar
    private ProgressBar progressBar;

    RecyclerView mRecyclerViewSelectedDocumentLinksList;
    RecyclerView mRecyclerViewDocumentLinksList;
    RecyclerViewFastScroller fastScrollerDocumentList;
    Button buttonCancelDocumentLinking;
    Button buttonSaveDocumentLinking;


    private List<LinkedDocumentItemDataHelper.LinkedDocumentDataCard> mDataArray;
    private List<LinkedDocumentItemDataHelper.LinkedDocumentDataCard> mDataArrayDelta;
    private List<LinkedDocumentItemSelectionDataHelper.LinkedDocumentDataCard> mSelectedDataArray;
    private List<AlphabetItem> mAlphabetItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "Initializing Document Linking UI.");

        globalVariable  = (SessionData) getApplicationContext();
        setContentView(R.layout.activity_link_document);

        mRecyclerViewDocumentLinksList = (RecyclerView) findViewById(R.id.recyclerViewDocumentLinksList);
        fastScrollerDocumentList = (RecyclerViewFastScroller)findViewById(R.id.fast_scroller);

        mRecyclerViewSelectedDocumentLinksList = (RecyclerView) findViewById(R.id.recyclerViewChosenDocumentLinks);
        buttonCancelDocumentLinking = (Button)findViewById(R.id.buttonCancelLinking);
        buttonSaveDocumentLinking = (Button)findViewById(R.id.buttonSaveLinking);

        // progress bar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ResourcesCompat.getColor(getResources(), R.color.login_hint_highlight, null)
                , android.graphics.PorterDuff.Mode.SRC_IN);

        initialiseLinkDocsCollection();

        //initialiseDocumentListData();
        //initialiseSelectedLinkedDocumentsListData();
    }

    protected void initialiseDocumentListData() {
        /**
         * get data from the server about all documents
         */
        String url;

        url = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestDocumentFetchallDocsToLinkURL();
        Log.i(LOG_TAG, "REST - get all documents - call: " + url);

        // Create the handler
        DocumentLinkingActivity.OkHttpLinkedDocumentDataHandler okHttpHandler= new DocumentLinkingActivity.OkHttpLinkedDocumentDataHandler();

        // execute the call synchronously
        okHttpHandler.execute(url);

    }

    protected void initialiseSelectedLinkedDocumentsListData() {
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
            //
            url = globalVariable.getConfigurationData().getServerURL()
                    + globalVariable.getConfigurationData().getApplicationInfixURL()
                    + globalVariable.getConfigurationData().getRestDocumentFetchLinkedDocsBySyncIdURL()
                    + documentCardItem.getSyncID();
            Log.i(LOG_TAG, "REST - get all linked docs for a doc id - call: " + url);

            // Create the handler
            DocumentLinkingActivity.OkHttpLinkedDocumentDataHandler okHttpHandler = new DocumentLinkingActivity.OkHttpLinkedDocumentDataHandler();

            // execute the call synchronously
            okHttpHandler.execute(url);
        }else{
            // get the data from the session about the document
            mSelectedDataArray = convertLinkedDocsToInternalDocs(globalVariable.getNextDocument().getLinkedDocuments());
            mDataArrayDelta = convertLinkedDocs(globalVariable.getNextDocument().getLinkedDocuments());
        }
    }

    /**
     * Single call to get the collection of attachement data from the backend
     */
    protected void initialiseLinkDocsCollection(){
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
                    + globalVariable.getConfigurationData().getRestDocumentLinkedFetchDocCollectionURL()
                    + documentCardItem.getSyncID();
            // New Document
        }else{

            url = globalVariable.getConfigurationData().getServerURL()
                    + globalVariable.getConfigurationData().getApplicationInfixURL()
                    + globalVariable.getConfigurationData().getRestDocumentLinkedFetchDocCollectionURL()
                    + "0";
        }

        Log.i(LOG_TAG, "REST - get document attachment collection - call: " + url);

        // Create the handler
        DocumentLinkingActivity.OkHttpLinkedDocumentDataHandler okHttpHandler = new DocumentLinkingActivity.OkHttpLinkedDocumentDataHandler();

        // execute the call synchronously
        okHttpHandler.execute(url);
    }



    protected void initialiseUI() {

        //
        // sync the linkable docs with the linked docs

        // get the delta and add it to the main linked docs list
        for(int i=0; i< mDataArrayDelta.size(); i++){
            if(!mDataArray.contains(mDataArrayDelta.get(i))){
                mDataArray.add(mDataArrayDelta.get(i));
            }
        }
        mAlphabetItems = new ArrayList<>();
        List<String> strAlphabets = new ArrayList<>();
        for (int i = 0; i < mDataArray.size(); i++) {
            String name = mDataArray.get(i).getOwner();
            if (name == null || name.trim().isEmpty())
                continue;

            String word = name.substring(0, 1);
            if (!strAlphabets.contains(word)) {
                strAlphabets.add(word);
                mAlphabetItems.add(new AlphabetItem(i, word, false));
            }
        }

        Log.i(LOG_TAG, "Initialize the main UI");
        // final syncing
        for(int i=0; i<mSelectedDataArray.size(); i++){
            int index = isSelectedDocument(mSelectedDataArray.get(i).getId());
            if( index != -1){
                mDataArray.get(index).setCheckState(true);
            }
        }

        mRecyclerViewDocumentLinksList.setLayoutManager(new LinearLayoutManager(this));
        // create adapter
        final ShowLinkedDocumentsListRecyclerViewAdapter listAdapter = new ShowLinkedDocumentsListRecyclerViewAdapter(mDataArray, getApplicationContext());
        mRecyclerViewDocumentLinksList.setAdapter(listAdapter);
        mRecyclerViewSelectedDocumentLinksList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SelectedLinkedDocumentsListRecyclerViewAdapter selectedAdapter = new SelectedLinkedDocumentsListRecyclerViewAdapter(mSelectedDataArray);
        mRecyclerViewSelectedDocumentLinksList.setAdapter(selectedAdapter);
        listAdapter.setSelectedAdapter(selectedAdapter);
        selectedAdapter.setListAdapter(listAdapter);

        mRecyclerViewDocumentLinksList.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerViewDocumentLinksList, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                DocumentCardItem documentCardItem;
                int dataLocalizationAccessMode;

                // get the current data object from the main adapter
                LinkedDocumentItemDataHelper.LinkedDocumentDataCard pickedItem = listAdapter.getDataSet().get(position);

                //
                // Search through both adapters
                documentCardItem = globalVariable.getDocumentAdapter(AllDocumentsFragment.RECYCLER_ADAPTER_KEY).findCardItemBySyncId(pickedItem.getSyncId());
                dataLocalizationAccessMode = TabbedDocumentDialog.DATA_MODE_REMOTE;
                if(documentCardItem == null){
                    documentCardItem = globalVariable.getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY).findCardItemBySyncId(pickedItem.getSyncId());
                    dataLocalizationAccessMode= TabbedDocumentDialog.DATA_MODE_LOCAL;
                }

                //
                // Load context data for the card
                DocumentContext context = new DocumentContext();
                context.setContextName(DocumentContext.DOCUMENT_LINIKING_ACTIVITY_NAME);
                context.setAdapter(listAdapter);
                context.setDataSet(listAdapter.getDataSet());
                context.setListPosition(position);
                context.setDataItem(pickedItem);
                context.setView(view);
                if(documentCardItem != null){
                    documentCardItem.setContext(context);
                }


                // display the image
                showFullDocumentPagedDialog(documentCardItem, TabbedDocumentDialog.DIALOG_MODE_READ_ONLY, dataLocalizationAccessMode);

            }

            @Override
            public void onLongClick(View view, int position) {
                // Do nothing at this point
            }
        }));

        fastScrollerDocumentList.setRecyclerView(mRecyclerViewDocumentLinksList);
        fastScrollerDocumentList.setUpAlphabet(mAlphabetItems);

        // buttons
        buttonCancelDocumentLinking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Cancelling the link data for the chosen document");
                // cancel the operation by going back
                onBackPressed();
            }
        });
        buttonSaveDocumentLinking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Saving the link data for the chosen document");
                // execute the POST action
                //

                // convert data
                List<Document> docs = DocumentPOJOUtils.convertLinkedDocumentFromInternalDocument(mSelectedDataArray);

                //////////////////////////////////////////////////////////////////////////////
                // Edit content
                //
                if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_EDIT
                        || globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_CONTEXT_EDIT) {

                    // Save the data for the document
                    DocumentCardItem documentCardItem = globalVariable
                            .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                            .getDataSet()
                            .get(globalVariable.getLongClickPositionMyDocument());

                    //
                    // Commit the data to REST Service
                    String postUrl = globalVariable.getConfigurationData().getServerURL()
                            + globalVariable.getConfigurationData().getApplicationInfixURL()
                            + globalVariable.getConfigurationData().getRestDocumentAttachLinkedDocsBySyncIdURL();
                    Log.i(LOG_TAG, "REST - Link selected docs to the main doc - call: " + postUrl);
                    Log.d(LOG_TAG, "REST - Documents to be linked: " + docs);

                    String postBody = RESTUtils.getJSON(docs);
                    RESTUtils.executePOSTDocumentSyncDocumentLinkAttachRequest(postUrl, postBody, documentCardItem.getSyncID(), "", globalVariable, "");

                    globalVariable
                            .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                            .getDataSet()
                            .get(globalVariable.getLongClickPositionMyDocument()).setLinkedDocuments(docs);

                    globalVariable
                            .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                            .notifyDataSetChanged();
                }else{
                //////////////////////////////////////////////////////////////////////////////
                // Create content
                //
                    globalVariable.getNextDocument().setLinkedDocuments(docs);

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
            // Empty implementation
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            // Empty implementation
        }
    }

    /**
     * Inner class to handle the back-end communication to get the data from the db
     */
    public class OkHttpLinkedDocumentDataHandler extends AsyncTask<String, String, String> {

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
            List<Document> linkedByDoc;

            //
            // GSON parser
            Gson gson = new GsonBuilder()
                    .create();

            //
            // parse the JSON input into the specific class

            ///////////////////////////////////////////////////////////////////////////////////////
            // Getting All Documents from the backend
            //
            if(requestURL.contains(globalVariable.getConfigurationData().getRestDocumentFetchallDocsToLinkURL())) {
                Type listType = new TypeToken<ArrayList<Document>>() {
                }.getType();
                documents = gson.fromJson(s, listType);
                myDocuments = new ArrayList<>();
                Log.d(LOG_TAG, "REST - all fetched documents request: " + requestURL);
                Log.d(LOG_TAG, "REST - all fetched documents: " + documents);
                DocumentCardItem documentCardItem = null;

                if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_EDIT
                        || globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_CONTEXT_EDIT) {
                    // filter documents first to only get your own docs
                    // and not the current doc
                    documentCardItem = globalVariable
                            .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                            .getDataSet()
                            .get(globalVariable.getLongClickPositionMyDocument());
                    // remove the current doc
                    documents.remove(DocumentPOJOUtils.convertDocumentCardItemToDocument(documentCardItem));

                }
                // sort the entities by their date
                Collections.sort(documents, new Comparator<Document>() {
                    @Override
                    public int compare(Document lhs, Document rhs) {
                        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                        int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getOwner(), rhs.getOwner());
                        if (res == 0) {
                            res = lhs.getOwner().compareTo(rhs.getOwner());
                        }
                        return res;
                    }
                });

                // convert session data
                mDataArray = convertLinkedDocs(documents);
                //Alphabet fast scroller data
                /**
                mAlphabetItems = new ArrayList<>();
                List<String> strAlphabets = new ArrayList<>();
                for (int i = 0; i < mDataArray.size(); i++) {
                    String name = mDataArray.get(i).getOwner();
                    if (name == null || name.trim().isEmpty())
                        continue;

                    String word = name.substring(0, 1);
                    if (!strAlphabets.contains(word)) {
                        strAlphabets.add(word);
                        mAlphabetItems.add(new AlphabetItem(i, word, false));
                    }
                }
                 */

                // get the data from the session about the document
                mSelectedDataArray = new ArrayList<>();
                mDataArrayDelta = new ArrayList<>();
            }

            ///////////////////////////////////////////////////////////////////////////////////////
            // Getting all the linked documents
            //
            if(requestURL.contains(globalVariable.getConfigurationData().getRestDocumentFetchLinkedDocsBySyncIdURL())){
                Type listType = new TypeToken<ArrayList<Document>>() {
                }.getType();
                linkedByDoc = gson.fromJson(s, listType);
                Log.d(LOG_TAG, "REST - all fetched documents request: " + requestURL);
                Log.d(LOG_TAG, "REST - all fetched linked documents: " + linkedByDoc);

                // Check if we have a tagged document that should be added
                for(int i = 0; i< globalVariable.getNextDocument().getLinkedDocuments().size(); i++){
                    if(linkedByDoc.contains(globalVariable.getNextDocument().getLinkedDocuments().get(i))){
                        continue;
                    }else{
                        linkedByDoc.add(globalVariable.getNextDocument().getLinkedDocuments().get(i));
                    }
                }
                // sort the entities by their date
                Collections.sort(linkedByDoc, new Comparator<Document>() {
                    @Override
                    public int compare(Document lhs, Document rhs) {
                        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                        int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getOwner(), rhs.getOwner());
                        if (res == 0) {
                            res = lhs.getOwner().compareTo(rhs.getOwner());
                        }
                        return res;
                    }
                });

                // get the data from the session about the document
                mSelectedDataArray = convertLinkedDocsToInternalDocs(linkedByDoc);
                mDataArrayDelta = convertLinkedDocs(linkedByDoc);

            }

            /////////////////////////////////////////////////////////////////////////////////////////
            // Getting the linked doc collection
            //
            if(requestURL.contains(globalVariable.getConfigurationData().getRestDocumentLinkedFetchDocCollectionURL())){

                String jsonDocumentCollection = s;
                DocumentCollection collection = (DocumentCollection) RESTUtils.getObjectFromJSON(jsonDocumentCollection, DocumentCollection.class);


                //
                // Docs to Link
                documents = collection.getAllDocsToLink();
                myDocuments = new ArrayList<>();
                Log.d(LOG_TAG, "REST - all fetched documents request: " + requestURL);
                Log.d(LOG_TAG, "REST - all fetched documents: " + documents);
                DocumentCardItem documentCardItem = null;

                if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_EDIT
                        || globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_CONTEXT_EDIT) {
                    // filter documents first to only get your own docs
                    // and not the current doc
                    documentCardItem = globalVariable
                            .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                            .getDataSet()
                            .get(globalVariable.getLongClickPositionMyDocument());
                    // remove the current doc
                    documents.remove(DocumentPOJOUtils.convertDocumentCardItemToDocument(documentCardItem));

                }
                // sort the entities by their date
                Collections.sort(documents, new Comparator<Document>() {
                    @Override
                    public int compare(Document lhs, Document rhs) {
                        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                        int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getOwner(), rhs.getOwner());
                        if (res == 0) {
                            res = lhs.getOwner().compareTo(rhs.getOwner());
                        }
                        return res;
                    }
                });

                if(documents.size() ==0){
                    Toast.makeText(globalVariable.getBaseContext(), "There are *NO* Document to Link", Toast.LENGTH_SHORT).show();
                }

                // convert session data
                mDataArray = convertLinkedDocs(documents);

                // get the data from the session about the document
                mSelectedDataArray = new ArrayList<>();
                mDataArrayDelta = new ArrayList<>();

                //
                // Attached docs
                if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_EDIT
                        || globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_CONTEXT_EDIT) {

                    linkedByDoc = collection.getLinkedDocs();
                    Log.d(LOG_TAG, "REST - all Linked documents request: " + requestURL);
                    Log.d(LOG_TAG, "REST - all fetched linked documents: " + linkedByDoc);

                    // Check if we have a tagged document that should be added
                    for(int i = 0; i< globalVariable.getNextDocument().getLinkedDocuments().size(); i++){
                        if(linkedByDoc.contains(globalVariable.getNextDocument().getLinkedDocuments().get(i))){
                            continue;
                        }else{
                            linkedByDoc.add(globalVariable.getNextDocument().getLinkedDocuments().get(i));
                        }
                    }
                    // sort the entities by their date
                    Collections.sort(linkedByDoc, new Comparator<Document>() {
                        @Override
                        public int compare(Document lhs, Document rhs) {
                            // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                            int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getOwner(), rhs.getOwner());
                            if (res == 0) {
                                res = lhs.getOwner().compareTo(rhs.getOwner());
                            }
                            return res;
                        }
                    });

                    // get the data from the session about the document
                    mSelectedDataArray = convertLinkedDocsToInternalDocs(linkedByDoc);
                    mDataArrayDelta = convertLinkedDocs(linkedByDoc);

                }else{
                    // get the data from the session about the document
                    mSelectedDataArray = convertLinkedDocsToInternalDocs(globalVariable.getNextDocument().getLinkedDocuments());
                    mDataArrayDelta = convertLinkedDocs(globalVariable.getNextDocument().getLinkedDocuments());
                }
            }

            progressBar.setVisibility(View.INVISIBLE);

            initialiseUI();
        }

    }

    private List<LinkedDocumentItemDataHelper.LinkedDocumentDataCard> convertLinkedDocs(List<Document> docs){
        List<LinkedDocumentItemDataHelper.LinkedDocumentDataCard> result = new ArrayList<>();

        for(int i=0; i< docs.size(); i++){
            Document doc = docs.get(i);
            LinkedDocumentItemDataHelper.LinkedDocumentDataCard item = new LinkedDocumentItemDataHelper.LinkedDocumentDataCard(doc.getDocumentType(), doc.getOwner(), false);
            item.setId(doc.getId());
            item.setTimestamp(doc.getCreationTimestamp());
            item.setDocumentType(doc.getType());
            item.setSyncId(doc.getSyncID());
            //int docTypeID = this.getResources().
            //        getIdentifier(docs.get(i).getType().getName(), "string", this.getPackageName());
            //String documentType = getResources().getString(docTypeID);

            String documentType = globalVariable.getInternationalizedResourceString(docs.get(i).getType().getName());
            item.setLinkedDocText(documentType);

            //
            // Custom tag
            docs.get(i).setTags(DocumentPOJOUtils.convertCustomTags(docs.get(i).getTags(), globalVariable.getConfigurationData().getGlobalConstantTaggingCustomPrefix()));
            String customTag = DocumentPOJOUtils.getCustomTag(docs.get(i).getTags());
            if(customTag != null && customTag.length() > 0){
                item.setCustomTag(customTag);
            }else{
                item.setCustomTag("");
            }

            result.add(item);
        }

        return result;
    }

    private List<LinkedDocumentItemSelectionDataHelper.LinkedDocumentDataCard> convertLinkedDocsToInternalDocs(List<Document> docs){
        List<LinkedDocumentItemSelectionDataHelper.LinkedDocumentDataCard> result = new ArrayList<LinkedDocumentItemSelectionDataHelper.LinkedDocumentDataCard>();

        for(int i=0; i< docs.size(); i++){
            Document doc = docs.get(i);
            //int docTypeID = this.getResources().
            //         getIdentifier(docs.get(i).getType().getName(), "string", this.getPackageName());
            //String documentType = getResources().getString(docTypeID);

            String documentType = globalVariable.getInternationalizedResourceString(docs.get(i).getType().getName());
            LinkedDocumentItemSelectionDataHelper.LinkedDocumentDataCard item = new LinkedDocumentItemSelectionDataHelper.LinkedDocumentDataCard(doc.getId(), documentType, doc.getOwner(), doc.getType());
            item.setSyncId(doc.getSyncID());

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
     * Will show the paged dialog to the user
     * @param document - the document to be displayed
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
