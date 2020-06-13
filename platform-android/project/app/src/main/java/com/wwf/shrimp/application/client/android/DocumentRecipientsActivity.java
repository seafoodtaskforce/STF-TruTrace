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
import com.wwf.shrimp.application.client.android.adapters.SelectedRecipientListRecyclerViewAdapter;
import com.wwf.shrimp.application.client.android.adapters.ShowLinkedDocumentsListRecyclerViewAdapter;
import com.wwf.shrimp.application.client.android.adapters.ShowRecipientListRecyclerViewAdapter;
import com.wwf.shrimp.application.client.android.adapters.helpers.LinkedDocumentItemDataHelper;
import com.wwf.shrimp.application.client.android.adapters.helpers.LinkedDocumentItemSelectionDataHelper;
import com.wwf.shrimp.application.client.android.adapters.helpers.RecipientDataHelper;
import com.wwf.shrimp.application.client.android.adapters.helpers.RecipientSelectionDataHelper;
import com.wwf.shrimp.application.client.android.dialogs.TabbedDocumentDialog;
import com.wwf.shrimp.application.client.android.fragments.AllDocumentsFragment;
import com.wwf.shrimp.application.client.android.fragments.MyDocumentsFragment;
import com.wwf.shrimp.application.client.android.fragments.ProfileDocumentsFragment;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.Group;
import com.wwf.shrimp.application.client.android.models.dto.Organization;
import com.wwf.shrimp.application.client.android.models.dto.User;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils;
import com.wwf.shrimp.application.client.android.utils.MappingUtilities;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;
import com.wwf.shrimp.application.client.android.utils.dialogs.ErrorConnectingDialogUtility;
import com.wwf.shrimp.application.client.android.utils.listeners.ClickListener;

import java.io.IOException;
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
 * Activity which will allow recipients to be added to a document.
 * @author AleaActaEst
 */
public class DocumentRecipientsActivity extends AppCompatActivity {

    // logging tag
    private static final String LOG_TAG = "Doc Recipients Activity";

    // global session data access
    private SessionData globalVariable = null;

    // progress bar
    private ProgressBar progressBar;

    RecyclerView mRecyclerViewSelectedDocumentRecipientsList;
    RecyclerView mRecyclerViewDocumentRecipientsList;
    RecyclerViewFastScroller fastScrollerUserList;
    Button buttonCancelDocumentRecipients;
    Button buttonSaveDocumentRecipients;


    private List<RecipientDataHelper.RecipientCard> mDataArray;
    private List<RecipientSelectionDataHelper.RecipientCard> mSelectedDataArray;
    private List<AlphabetItem> mAlphabetItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "Initializing Document Linking UI.");

        globalVariable  = (SessionData) getApplicationContext();
        setContentView(R.layout.activity_recipients_document);

        mRecyclerViewDocumentRecipientsList = (RecyclerView) findViewById(R.id.recyclerViewRecipientsList);
        fastScrollerUserList = (RecyclerViewFastScroller)findViewById(R.id.fast_scroller);

        mRecyclerViewSelectedDocumentRecipientsList = (RecyclerView) findViewById(R.id.recyclerViewChosenRecipients);
        buttonCancelDocumentRecipients = (Button)findViewById(R.id.buttonCancelRecipients);
        buttonSaveDocumentRecipients = (Button)findViewById(R.id.buttonSaveRecipients);

        // progress bar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ResourcesCompat.getColor(getResources(), R.color.login_hint_highlight, null)
                , android.graphics.PorterDuff.Mode.SRC_IN);

        progressBar.setVisibility(View.VISIBLE);

        initialiseRecipientsListData();
        initialiseSelectedRecipientsListData();
    }

    protected void initialiseRecipientsListData() {
        /**
         * get data from the server about all recipients
         */
        String url;

        url = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestDocumentFetchRecipientsForUserURL();
        Log.i(LOG_TAG, "REST - get all possible recipients - call: " + url);

        // Create the handler
        DocumentRecipientsActivity.OkHttpRecipientsDataHandler okHttpHandler= new DocumentRecipientsActivity.OkHttpRecipientsDataHandler();

        // execute the call synchronously
        okHttpHandler.execute(url);

    }

    protected void initialiseSelectedRecipientsListData() {
        /**
         * get data from the server about links
         */
        String url;

        if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_EDIT
                || globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_CONTEXT_EDIT) {

            DocumentCardItem documentCardItem = globalVariable
                    .getDocumentAdapter(getAdapterKey(globalVariable.getCurrFragment()))
                    .getDataSet()
                    .get(globalVariable.getLongClickPositionMyDocument());

            /**
            DocumentCardItem documentCardItem = globalVariable
                    .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                    .getDataSet()
                    .get(globalVariable.getLongClickPositionMyDocument());
             */

            // Try to get data from the database
            //
            url = globalVariable.getConfigurationData().getServerURL()
                    + globalVariable.getConfigurationData().getApplicationInfixURL()
                    + globalVariable.getConfigurationData().getRestRecipientsFetchByDocumentSyncIdURL()
                    + documentCardItem.getSyncID();
            Log.i(LOG_TAG, "REST - get all recipients for a doc id - call: " + url);

            // Create the handler
            DocumentRecipientsActivity.OkHttpRecipientsDataHandler okHttpHandler = new DocumentRecipientsActivity.OkHttpRecipientsDataHandler();

            // execute the call synchronously
            okHttpHandler.execute(url);
        }else{
            // get the data from the session about the document
            mSelectedDataArray = convertDocRecipientsToInternalRecipients(globalVariable.getNextDocument().getRecipients());
        }
    }

    protected void initialiseUI() {

        Log.i(LOG_TAG, "Initialize the main UI");
        // final syncing
        if(mSelectedDataArray != null) {
            for (int i = 0; i < mSelectedDataArray.size(); i++) {
                int index = isSelectedDocument(mSelectedDataArray.get(i).getId());
                if (index != -1) {
                    mDataArray.get(index).setCheckState(true);
                }
            }
        }

        mRecyclerViewDocumentRecipientsList.setLayoutManager(new LinearLayoutManager(this));
        // create adapter
        final ShowRecipientListRecyclerViewAdapter listAdapter = new ShowRecipientListRecyclerViewAdapter(mDataArray, globalVariable);
        mRecyclerViewDocumentRecipientsList.setAdapter(listAdapter);
        mRecyclerViewSelectedDocumentRecipientsList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SelectedRecipientListRecyclerViewAdapter selectedAdapter = new SelectedRecipientListRecyclerViewAdapter(mSelectedDataArray,globalVariable);
        mRecyclerViewSelectedDocumentRecipientsList.setAdapter(selectedAdapter);
        listAdapter.setSelectedAdapter(selectedAdapter);
        selectedAdapter.setListAdapter(listAdapter);

        mRecyclerViewDocumentRecipientsList.addOnItemTouchListener(new DocumentRecipientsActivity.RecyclerTouchListener(getApplicationContext(), mRecyclerViewDocumentRecipientsList, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {

                // do nothing at this point
            }

            @Override
            public void onLongClick(View view, int position) {
                // do nothing at this point
            }
        }));

        fastScrollerUserList.setRecyclerView(mRecyclerViewDocumentRecipientsList);
        fastScrollerUserList.setUpAlphabet(mAlphabetItems);

        //
        // Dialog Button Handlers

        //
        // Cancel the operation
        buttonCancelDocumentRecipients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cancel the operation by going back
                onBackPressed();
            }
        });

        //
        // Save the data
        buttonSaveDocumentRecipients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Saving the link data for the chosen document");

                // convert data
                List<User> users = convertRecipientsFromInternalRecipient(mSelectedDataArray);

                //////////////////////////////////////////////////////////////////////////////
                // Edit content
                //
                if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_EDIT
                        || globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_CONTEXT_EDIT){
                    // Save the data for the document
                    DocumentCardItem documentCardItem = globalVariable
                            .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                            .getDataSet()
                            .get(globalVariable.getLongClickPositionMyDocument());

                    // execute the POST action
                    //

                    //
                    // Commit the data to REST Service
                    String postUrl = globalVariable.getConfigurationData().getServerURL()
                            + globalVariable.getConfigurationData().getApplicationInfixURL()
                            + globalVariable.getConfigurationData().getRestRecipientsAddByDocumentSyncIdURL();
                    Log.i(LOG_TAG, "REST - add selected recipients to the main doc - call: " + postUrl);
                    Log.d(LOG_TAG, "REST - users to be set as recipients: " + users);

                    String postBody = RESTUtils.getJSON(users);
                    globalVariable.getNextDocument().setRecipients(users);
                    RESTUtils.executePOSTDocumentSyncDocumentAddRecipientsRequest(postUrl, postBody, documentCardItem.getSyncID(), "", globalVariable, "");
                    /**
                    globalVariable
                            .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                            .getDataSet()
                            .get(globalVariable.getLongClickPositionMyDocument()).setRecipients(users);

                    globalVariable
                            .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                            .notifyDataSetChanged();
 */
                }else{
                //////////////////////////////////////////////////////////////////////////////
                // Create content
                //
                    globalVariable.getNextDocument().setRecipients(users);

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
                // return true;
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
    public class OkHttpRecipientsDataHandler extends AsyncTask<String, String, String> {

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

            // documents holder for fetching all recipients
            ArrayList<User> allRecipients;
            ArrayList<Document> myDocuments;
            ArrayList<User> recipientsForDoc;

            //
            // GSON parser
            Gson gson = new GsonBuilder()
                    .create();

            //
            // parse the JSON input into the specific class

            ///////////////////////////////////////////////////////////////////////////////////////
            // Getting All Possible Recipients from the backend
            //
            if(requestURL.contains(globalVariable.getConfigurationData().getRestDocumentFetchRecipientsForUserURL())) {
                Type listType = new TypeToken<ArrayList<User>>() {
                }.getType();
                allRecipients = gson.fromJson(s, listType);
                Log.d(LOG_TAG, "REST - all fetched all recipients request: " + requestURL);
                Log.d(LOG_TAG, "REST - all fetched all recipients: " + allRecipients);

                if(allRecipients != null){
                    // remove current user
                    if(allRecipients.contains(globalVariable.getCurrentUser())){
                        allRecipients.remove(globalVariable.getCurrentUser());
                    }

                    // sort the entities by their date
                    Collections.sort(allRecipients, new Comparator<User>() {
                        @Override
                        public int compare(User lhs, User rhs) {
                            // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                            int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
                            if (res == 0) {
                                res = lhs.getName().compareTo(rhs.getName());
                            }
                            return res;
                        }
                    });

                    if(allRecipients.size() ==0){
                        Toast.makeText(globalVariable.getBaseContext(), "There are *NO* Recipients set", Toast.LENGTH_SHORT).show();
                    }
                    // convert session data
                    mDataArray = convertLinkedDocs(allRecipients);
                    //Alphabet fast scroller data
                    mAlphabetItems = new ArrayList<>();
                    List<String> strAlphabets = new ArrayList<>();
                    for (int i = 0; i < mDataArray.size(); i++) {
                        String name = mDataArray.get(i).getUserName();
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

            }

            ///////////////////////////////////////////////////////////////////////////////////////
            // Getting all the Recipients for the current doc
            //
            if(requestURL.contains(globalVariable.getConfigurationData().getRestRecipientsFetchByDocumentSyncIdURL())){
                Type listType = new TypeToken<ArrayList<User>>() {
                }.getType();
                recipientsForDoc = gson.fromJson(s, listType);
                Log.d(LOG_TAG, "REST - all fetched documents request: " + requestURL);
                Log.d(LOG_TAG, "REST - all fetched linked documents: " + recipientsForDoc);

                // sort the entities by their date
                Collections.sort(recipientsForDoc, new Comparator<User>() {
                    @Override
                    public int compare(User lhs, User rhs) {
                        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                        int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
                        if (res == 0) {
                            res = lhs.getName().compareTo(rhs.getName());
                        }
                        return res;
                    }
                });

                // get the data from the session about the document
                mSelectedDataArray = convertDocRecipientsToInternalRecipients(recipientsForDoc);

            }

            progressBar.setVisibility(View.INVISIBLE);

            initialiseUI();
        }

    }

    private List<RecipientDataHelper.RecipientCard> convertLinkedDocs(List<User> users){
        List<RecipientDataHelper.RecipientCard> result = new ArrayList<>();

        for(int i=0; i< users.size(); i++){
            User user = users.get(i);
            RecipientDataHelper.RecipientCard item
                    = new RecipientDataHelper.RecipientCard(
                            user.getName(),
                            user.getUserGroups().get(0).getName(),
                            //user.getUserOrganizations().get(0).getSubGroups().get(0).getName(),
                    false);
            item.setId(user.getId());

            result.add(item);
        }

        return result;
    }

    private List<RecipientSelectionDataHelper.RecipientCard> convertDocRecipientsToInternalRecipients(List<User> users){
        List<RecipientSelectionDataHelper.RecipientCard> result = new ArrayList<RecipientSelectionDataHelper.RecipientCard>();

        for(int i=0; i< users.size(); i++){
            User user = users.get(i);
            String orgName = "";
            if(user.getUserOrganizations() == null || user.getUserOrganizations().size() ==0){

            }else{
                orgName = user.getUserOrganizations().get(0).getSubGroups().get(0).getName();
            }
            RecipientSelectionDataHelper.RecipientCard item
                    = new RecipientSelectionDataHelper.RecipientCard(user.getId()
                    , orgName
                    , user.getName());

            result.add(item);
        }

        return result;
    }

    private List<User> convertRecipientsFromInternalRecipient(List<RecipientSelectionDataHelper.RecipientCard> users){
        List<User> result = new ArrayList<>();

        for(int i=0; i< users.size(); i++){
            RecipientSelectionDataHelper.RecipientCard user = users.get(i);
            User item = new User();
            item.setId(user.getId());
            item.setName(user.getUserName());
            Organization org = new Organization();
            Group subGroup = new Group();
            subGroup.setName(user.getOrganizationName());

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

    public String getAdapterKey(Fragment fragment){

        if(fragment instanceof MyDocumentsFragment){
            return MyDocumentsFragment.RECYCLER_ADAPTER_KEY;
        }
        if(fragment instanceof AllDocumentsFragment){
            return AllDocumentsFragment.RECYCLER_ADAPTER_KEY;
        }
        if(fragment instanceof ProfileDocumentsFragment){
            return ProfileDocumentsFragment.RECYCLER_ADAPTER_KEY;
        }

        return null;

    }
}
