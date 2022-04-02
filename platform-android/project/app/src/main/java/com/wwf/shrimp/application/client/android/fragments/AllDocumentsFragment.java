package com.wwf.shrimp.application.client.android.fragments;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.adapters.DocumentCardItemAdapter;
import com.wwf.shrimp.application.client.android.dialogs.TabbedDocumentDialog;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.DocumentType;
import com.wwf.shrimp.application.client.android.models.dto.Organization;
import com.wwf.shrimp.application.client.android.models.dto.User;
import com.wwf.shrimp.application.client.android.models.dto.search.DocumentSearchCriteria;
import com.wwf.shrimp.application.client.android.models.dto.search.SearchResult;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.services.dao.DocumentService;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.DateUtils;
import com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils;
import com.wwf.shrimp.application.client.android.utils.MappingUtilities;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;
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
 * Fragment which will hold the list of the all the documents
 * that the user has access to.
 * @author argolite
 */
public class AllDocumentsFragment extends Fragment {

    // logging tag
    private static final String LOG_TAG = "List All Docs Activity";
    public static final String FRAGMENT_TAG = "List All Docs Activity";


    // UI elements
    //
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // adapters
    public static final String RECYCLER_ADAPTER_KEY = "AllDocumentsFragment";
    private RecyclerView recyclerView;
    private DocumentCardItemAdapter mAdapter;
    private List<DocumentCardItem> documentList = new ArrayList<DocumentCardItem>();

    // global session data
    private SessionData globalVariable;

    // services
    private DocumentService documentService;

    public AllDocumentsFragment() {
        // Required empty public constructor
    }

    public static AllDocumentsFragment getInstance() {
        AllDocumentsFragment fragment = new AllDocumentsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public DocumentCardItemAdapter getAdapter() {
        return mAdapter;
    }

    public void refreshListAtIndex(final int index){
        Log.i(LOG_TAG, "REFRESH LIST Call for MyDocs Doc");
        if(getActivity() == null)
            return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                getAdapter().notifyItemChanged(index);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        globalVariable  = (SessionData) getContext().getApplicationContext();
        setRetainInstance(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_all_documents, container, false);


        // Refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        // adapters
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_documents);

        //
        // Try to get data from the database
        documentService = new DocumentService(getContext());
        documentService.open();
            documentList = documentService.getAllOtherDocuments((globalVariable.getCurrentUser().getCredentials()).getUsername());
        documentService.close();

        // continue with the adapter
        //
        mAdapter = new DocumentCardItemAdapter(documentList, getContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new AllDocumentsFragment.RecyclerTouchListener(getContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                if(position != -1) {
                    handleDocumentClickEvent(position);
                }
                /**
                final String markAsReadUrl = globalVariable.getConfigurationData().getServerURL()
                        + globalVariable.getConfigurationData().getApplicationInfixURL()
                        + globalVariable.getConfigurationData().getRestDocumentMarkAsReadURL();

                final String idDocumentMarkKey = "DOCUMENT-READ";

                // get the file first
                DocumentCardItem documentCardItem = globalVariable.getDocumentAdapter(RECYCLER_ADAPTER_KEY).getDataSet().get(position);
                // display the image
                //
                // showFullDocumentDialog(documentCardItem);

                showFullDocumentPagedDialog(documentCardItem, TabbedDocumentDialog.DIALOG_MODE_READ_ONLY);
                documentCardItem.setWasRead(true);
                globalVariable.getDocumentAdapter(RECYCLER_ADAPTER_KEY).getDataSet().set(position,documentCardItem );
                globalVariable.getDocumentAdapter(RECYCLER_ADAPTER_KEY).notifyDataSetChanged();

                try {
                    RESTUtils.executePOSTDocumentReadRequest(markAsReadUrl,documentCardItem.getSyncID(), globalVariable.getCurrentUser().getName(), globalVariable, idDocumentMarkKey);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */
            }

            @Override
            public void onLongClick(View view, int position) {
                /**
                Toast.makeText(getContext(), "Long press on position :"+position,
                        Toast.LENGTH_LONG).show();

                 */
            }
        }));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        // session data
        globalVariable.setDocumentAdapter(RECYCLER_ADAPTER_KEY, mAdapter);

        //
        // get the data from the database


        // Set up the GET request URL
        refreshItems();

        // get users for the bottom sheet
        refreshDocumentUsers();
        // get types for the bottom sheet
        refreshDocumentTypes();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Fetch the data from the back-end
     */
    void refreshItems() {
        if(RESTUtils.checkConnection() == false){
            if(globalVariable.isConnected() == false){
                // do nothing
            }else{
                globalVariable.setConnected(false);
                Toast.makeText(getContext(), "LOST Internet Connectivity", Toast.LENGTH_SHORT).show();
                Log.i(LOG_TAG, "LOST Internet Connectivity");
            }
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }
        if(RESTUtils.checkConnection() == true){
            if(globalVariable.isConnected() == false){
                globalVariable.setConnected(true);
                Toast.makeText(getContext(), "Internet connectivity is back ON.", Toast.LENGTH_SHORT).show();
                Log.i(LOG_TAG, "Internet connectivity is back ON.");
            }
        }
        String url;
        //
        // determine the type of fetch mechanism
        if(globalVariable.isDocumentSearchFilterState(SessionData.DOCUMENT_FILTER_ALL_DOCS) == false
                || globalVariable.getDocumentSearchFilterData(SessionData.DOCUMENT_FILTER_ALL_DOCS) == null){
            // simple fetch all documents request
            url = globalVariable.getConfigurationData().getServerURL()
                    + globalVariable.getConfigurationData().getApplicationInfixURL()
                    + globalVariable.getConfigurationData().getRestDocumentFetchAllURL();
        }else{
            // fetch through search
            url = globalVariable.getConfigurationData().getServerURL()
                    + globalVariable.getConfigurationData().getApplicationInfixURL()
                    + globalVariable.getConfigurationData().getRestDocumentSearchURL();


            if(globalVariable.getDocumentSearchFilterData(SessionData.DOCUMENT_FILTER_ALL_DOCS).getDocType() != null){
                url+= "docType=" + globalVariable.getDocumentSearchFilterData(SessionData.DOCUMENT_FILTER_ALL_DOCS).getDocType().getId() + "&";
            }
            if(globalVariable.getDocumentSearchFilterData(SessionData.DOCUMENT_FILTER_ALL_DOCS).getUserName() != null){
                url+= "owner=" + globalVariable.getDocumentSearchFilterData(SessionData.DOCUMENT_FILTER_ALL_DOCS).getUserName() + "&";
            }
            url+= "organization_id=" + globalVariable.getCurrentUser().getUserOrganizations().get(0).getId() + "&";

            // trim last element
            url = url.substring(0, url.length()-1);

            // TODO bypass remote seach for now
            DocumentPOJOUtils.filterDocuments(globalVariable, AllDocumentsFragment.RECYCLER_ADAPTER_KEY);
            // get the number of records
            DocumentCardItemAdapter adapter = globalVariable.getDocumentAdapter(RECYCLER_ADAPTER_KEY);
            adapter.getDataSet().size();

            return;

        }
        Log.d(LOG_TAG, "URL request to fetch all documents: " + url);
        mSwipeRefreshLayout.setRefreshing(true);

        // Create the handler
        OkHttpDocumentDataHandler okHttpHandler= new OkHttpDocumentDataHandler();

        // execute the call synchronously
        okHttpHandler.execute(url);
    }

    /**
     * Get all the users in the system for (for the current organization)
     */
    void refreshDocumentUsers(){
        if(RESTUtils.checkConnection() == false){
            if(globalVariable.isConnected() == false){
                // do nothing
            }else{
                globalVariable.setConnected(false);
                Log.i(LOG_TAG, "LOST Internet Connectivity");
            }
            return;
        }
        if(RESTUtils.checkConnection() == true){
            if(globalVariable.isConnected() == false){
                globalVariable.setConnected(true);
                Toast.makeText(getContext(), "Internet connectivity is back ON.", Toast.LENGTH_SHORT).show();
                Log.i(LOG_TAG, "Internet connectivity is back ON.");
            }
        }


        String url;

        if(globalVariable.getCurrentUser() != null) {
            url = globalVariable.getConfigurationData().getServerURL()
                    + globalVariable.getConfigurationData().getApplicationInfixURL()
                    + globalVariable.getConfigurationData().getRestUserFetchAllURL();
            Log.i(LOG_TAG, "REST - get all users - call: " + url);

            // Create the handler
            OkHttpDocumentDataHandler okHttpHandler = new OkHttpDocumentDataHandler();

            // execute the call synchronously
            okHttpHandler.execute(url);
        }
    }

    /**
     * Get all the document types
     */
    void refreshDocumentTypes(){
        if(RESTUtils.checkConnection() == false){
            if(globalVariable.isConnected() == false){
                // do nothing
            }else{
                globalVariable.setConnected(false);
                Toast.makeText(getContext(), "LOST Internet Connectivity", Toast.LENGTH_SHORT).show();
                Log.i(LOG_TAG, "LOST Internet Connectivity");
            }
            return;
        }
        if(RESTUtils.checkConnection() == true){
            if(globalVariable.isConnected() == false){
                globalVariable.setConnected(true);
                Toast.makeText(getContext(), "Internet connectivity is back ON.", Toast.LENGTH_SHORT).show();
                Log.i(LOG_TAG, "Internet connectivity is back ON.");
            }
        }

        if(globalVariable.getCurrentUser() != null) {
            String url = globalVariable.getConfigurationData().getServerURL()
                    + globalVariable.getConfigurationData().getApplicationInfixURL()
                    + globalVariable.getConfigurationData().getRestDocumentTypesFetchAllURL();
            Log.i(LOG_TAG, "REST - get all document types - call: " + url);

            // Create the handler
            OkHttpDocumentDataHandler okHttpHandler = new OkHttpDocumentDataHandler();

            // execute the call synchronously
            okHttpHandler.execute(url);
        }
    }

    private Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
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
    public class OkHttpDocumentDataHandler extends AsyncTask<String, String, String> {

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(globalVariable.getConfigurationData().getClientSocketTimeoutInSeconds(), TimeUnit.SECONDS)
                .build();
        boolean serverDownFlag = false;
        String requestURL;

        @Override
        protected String doInBackground(String... params) {

            if(RESTUtils.checkConnection() == false){
                if(globalVariable.isConnected() == false){
                    // do nothing
                }else{
                    globalVariable.setConnected(false);
                    Log.i(LOG_TAG, "LOST Internet Connectivity");
                }
                return null;
            }
            if(RESTUtils.checkConnection() == true){
                if(globalVariable.isConnected() == false){
                    globalVariable.setConnected(true);
                    Log.i(LOG_TAG, "Internet connectivity is back ON.");
                }
            }

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
                Log.e(LOG_TAG, "OkHTTP Request Failed");
                e.printStackTrace();
                serverDownFlag = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Activity activity = getActivity();
            if(activity == null){
                return;
            }
            //
            // extract the data for the GSON authentication

            // documents holder for fetching all documents
            ArrayList<Document> documents;
            // document holder for filtered documents
            SearchResult<Document> searchResult;
            // Users
            ArrayList<User> users;
            // document types
            ArrayList<DocumentType> docTypes;

            //
            // UI data

            // card items for documents
            List<DocumentCardItem> cardItems;
            // users
            String[] usersList;
            // doc types
            String[] docTypesList;

            //
            // GSON parser
            Gson gson = new GsonBuilder()
                    .create();

            //
            // parse the JSON input into the specific class

            ///////////////////////////////////////////////////////////////////////////////////////
            // Getting documents from the backend
            //
            if(requestURL.contains(globalVariable.getConfigurationData().getRestDocumentFetchAllURL())){
                    /**
                    || requestURL.contains(globalVariable.getConfigurationData().getRestDocumentSearchURL().substring(0
                        , globalVariable.getConfigurationData().getRestDocumentSearchURL().length()-1))){
                     */

                // check if this was a search or a fetch all
                /**
                if(globalVariable.getDocumentSearchFilterData(SessionData.DOCUMENT_FILTER_ALL_DOCS) != null && globalVariable.isDocumentSearchFilterState(SessionData.DOCUMENT_FILTER_ALL_DOCS) == true){
                    Type searchType = new TypeToken<SearchResult<Document>>(){}.getType();
                    searchResult = gson.fromJson(s, searchType);
                    documents = (ArrayList<Document>)searchResult.getList();
                }else {
                 */
                    Type listType = new TypeToken<ArrayList<Document>>(){}.getType();
                    documents = gson.fromJson(s, listType);
                // }
                Log.d(LOG_TAG, "Fetch all documents request: " + requestURL);
                Log.d(LOG_TAG, "All fetched documents: " + documents);

                // convert
                cardItems = DocumentPOJOUtils.convertDocuments(documents, globalVariable, AllDocumentsFragment.this, true);
                Log.d(LOG_TAG, "The cardItems: " + cardItems);

                // sort the entities by their date
                Collections.sort(cardItems, new Comparator<DocumentCardItem>() {
                    @Override
                    public int compare(DocumentCardItem lhs, DocumentCardItem rhs) {
                        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                        return lhs.getCreationTimestamp().after(rhs.getCreationTimestamp()) ? -1 : (lhs.getCreationTimestamp().before(rhs.getCreationTimestamp()) ) ? 1 : 0;
                    }
                });

                //
                // Set the new data set into the recycler view
                DocumentCardItemAdapter mAdapter = globalVariable.getDocumentAdapter(RECYCLER_ADAPTER_KEY);
                mAdapter.getDataSet().clear();
                mAdapter.getDataSet().addAll(cardItems);
                mAdapter.notifyDataSetChanged();
                // set the new global data
                globalVariable.getDocumentLocalMap().put(AllDocumentsFragment.RECYCLER_ADAPTER_KEY, cardItems);

                // set the dialog
                mSwipeRefreshLayout.setRefreshing(false);

                // get the number of records
                DocumentCardItemAdapter adapter = globalVariable.getDocumentAdapter(RECYCLER_ADAPTER_KEY);
                adapter.getDataSet().size();

                // if this is part of notification then show the doc
                //
                if(globalVariable.getNotificationDocSessionId() != null) {
                    Log.d(LOG_TAG, "Notification for " + globalVariable.getNotificationDocSessionId());
                    // get the document position
                    final int itemPosition = adapter.findCardItemPositionBySyncId(globalVariable.getNotificationDocSessionId());
                    Log.d(LOG_TAG, "Notification position " + itemPosition);
                    if(itemPosition != -1){
                        // trigger the click
                        Log.d(LOG_TAG, "Notification - triggering the click action ");
                        handleDocumentClickEvent(itemPosition);
                        //recyclerView.findViewHolderForAdapterPosition(itemPosition).itemView.performClick();
                    }
                    // remove the doc specification
                    globalVariable.setNotificationDocSessionId(null);
                }



            }

            ///////////////////////////////////////////////////////////////////////////////////////
            // Getting users from the backend
            //
            if(requestURL.contains(globalVariable.getConfigurationData().getRestUserFetchAllURL())){
                Type listType = new TypeToken<ArrayList<User>>(){}.getType();
                users = gson.fromJson(s, listType);
                Log.d(LOG_TAG, "REST - get all users - call result: " + users);
                if(users != null) {

                    // convert
                    usersList = convertUserData(users);
                    Log.d(LOG_TAG, "REST - get all users converted: " + usersList);

                    //
                    // Set the new data set into the recycler view
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, usersList);
                }

            }

            ///////////////////////////////////////////////////////////////////////////////////////
            // Getting document types from the backend
            //
            if(requestURL.contains(globalVariable.getConfigurationData().getRestDocumentTypesFetchAllURL())){
                Type listType = new TypeToken<ArrayList<DocumentType>>(){}.getType();
                docTypes = gson.fromJson(s, listType);
                if(docTypes != null) {
                    Log.d(LOG_TAG, "Rest - all doc types from server: " + docTypes);

                    // convert
                    docTypesList = convertDocTypeData(docTypes);
                    for (int i = 0; i < docTypes.size(); i++) {
                        docTypes.get(i).setValue(docTypesList[i + 1]);
                    }
                    Log.d(LOG_TAG, "The document type list converted: " + docTypesList);

                    //
                    // Set the new data set into the recycler view
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, docTypesList);

                    // add lookup data to the session
                    globalVariable.setDocumentTypes(docTypes);
                }
            }
        }
    }

    private List<DocumentCardItem> convertDocuments(List<Document> documents){
        List<DocumentCardItem> result = new ArrayList<DocumentCardItem>();

        for(int i=0; i< documents.size(); i++){
            //
            // Omit documents from this user
            if(documents.get(i).getOwner().equals(globalVariable.getCurrentUser().getCredentials().getUsername())){
                continue;
            }
            DocumentCardItem cardItem = new DocumentCardItem();
            //
            //
            String documentType = getResources().getString(MappingUtilities.documentTypeIDToRTypeMap.get(documents.get(i).getType().getId()));
            cardItem.setType(documentType);
            documents.get(i).getType().setValue(documentType);
            cardItem.setUsername(documents.get(i).getOwner());
            cardItem.setCreationTimestamp(DateUtils.formatStringToDateTime(documents.get(i).getCreationTimestamp()));
            cardItem.setTypeHEXColor(documents.get(i).getTypeHEXColor());
            cardItem.setId(documents.get(i).getId());
            cardItem.setDocumentType(documents.get(i).getType());
            cardItem.setSyncID(documents.get(i).getSyncID());
            cardItem.setWasRead(documents.get(i).isCurrentUserRead());
            //
            // Add any pages
            cardItem.setDocumentPages(DocumentPOJOUtils.convertDocumentPagesToGalleryPages(documents.get(i).getPages()));
            //
            // Add tags
            cardItem.setTags(documents.get(i).getTags());
            //
            // Add linked docs
            cardItem.setLinkedDocuments(documents.get(i).getLinkedDocuments());
            //
            // Add Attached Docs
            cardItem.setAttachedDocuments(documents.get(i).getAttachedDocuments());
            //
            // Add status and any data on rejection notes
            cardItem.setStatus(documents.get(i).getStatus());
            cardItem.setNotes(documents.get(i).getNotes());

            //
            // add group information
            long groupId = documents.get(i).getGroupId();
            long OrganizationId = documents.get(i).getOrganizationId();
            // find the proper value
            cardItem.setGroupType(getGroupTypeName(OrganizationId, groupId));

            result.add(cardItem);
        }

        return result;
    }

    private String[] convertUserData(List<User> users){
        List<String> result = new ArrayList<>();
        result.add(getResources().getString(R.string.document_filter_all_user_options));

        String omitUser = globalVariable.getCurrentUser().getCredentials().getUsername();

        for(int i=0; i< users.size(); i++){
            if(!omitUser.equals(users.get(i).getCredentials().getUsername())) {
                result.add(users.get(i).getCredentials().getUsername());
            }
        }

        String[] arrayResult = new String[result.size()];
        return result.toArray(arrayResult);
    }

    private String[] convertDocTypeData(List<DocumentType> docTypes){
        String[] result;
        if (docTypes == null) {
            result = new String[1];
            result[0] = getResources().getString(R.string.document_filter_all_doctypes_options);

        }else{
            result = new String[docTypes.size() + 1];
            result[0] = getResources().getString(R.string.document_filter_all_doctypes_options);

            for(int i=0; i< docTypes.size(); i++){
                // String documentType = getResources().getString(MappingUtilities.documentTypeIDToRTypeMap.get(docTypes.get(i).getId()));
                //int docTypeID = this.getResources().
                //        getIdentifier(docTypes.get(i).getName(), "string", getActivity().getPackageName());
                // String documentType = getResources().getString(docTypeID);
                //
                // Get the resource data
                String documentType = globalVariable.getInternationalizedResourceString(docTypes.get(i).getName());

                result[i+1] = documentType;
            }
        }
        return result;
    }

    private long getDocTypeIdFromValue(String value){
        long result=0;

        for(int i=0; i< globalVariable.getDocumentTypes().size(); i++){
            if(value.equals(globalVariable.getDocumentTypes().get(i).getValue())){
                result = globalVariable.getDocumentTypes().get(i).getId();
                return result;
            }
        }
        return result;
    }

    private String getGroupTypeName(long orgId, long groupId){
        String result = "N/A";
        int orgIndex = -1;
        for(int i=0; i< globalVariable.getCurrentUser().getUserOrganizations().size(); i++){
            if(globalVariable.getCurrentUser().getUserOrganizations().get(i).getId() == orgId){
                orgIndex = i;
            }
        }
        Organization tempOrg = globalVariable.getCurrentUser().getUserOrganizations().get(orgIndex);
        for(int i=0; i < tempOrg.getSubGroups().size(); i++){
            if(tempOrg.getSubGroups().get(i).getId() == groupId){
                result = tempOrg.getSubGroups().get(i).getName();
            }
        }
        return result;
    }


    /**
     * Will show the paged dialog to the user with the document details
     * @param document - the document to show the details for
     */
    protected void showFullDocumentPagedDialog(final DocumentCardItem document, int mode) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // set the document to be worked on the session
        globalVariable.setNextDocument(document);
        globalVariable.setCurrFragment(null);

        // Create and show the dialog.
        TabbedDocumentDialog dialogFragment = new TabbedDocumentDialog();
        dialogFragment.setDialogMode(mode);
        dialogFragment.setContainerFragment(this);
        dialogFragment.setDataMode(TabbedDocumentDialog.DATA_MODE_REMOTE);
        dialogFragment.show(ft,FRAGMENT_TAG);
    }

    protected void handleDocumentClickEvent(int docItemPosition){
        final String markAsReadUrl = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestDocumentMarkAsReadURL();

        final String idDocumentMarkKey = "DOCUMENT-READ";

        // get the file first
        DocumentCardItem documentCardItem = globalVariable.getDocumentAdapter(RECYCLER_ADAPTER_KEY).getDataSet().get(docItemPosition);
        // display the image
        //
        // showFullDocumentDialog(documentCardItem);

        showFullDocumentPagedDialog(documentCardItem, TabbedDocumentDialog.DIALOG_MODE_READ_ONLY);
        documentCardItem.setWasRead(true);
        globalVariable.getDocumentAdapter(RECYCLER_ADAPTER_KEY).getDataSet().set(docItemPosition,documentCardItem );
        globalVariable.getDocumentAdapter(RECYCLER_ADAPTER_KEY).notifyDataSetChanged();

        RESTUtils.executePOSTDocumentReadRequest(markAsReadUrl,documentCardItem.getSyncID(), globalVariable.getCurrentUser().getName(), globalVariable, idDocumentMarkKey);
    }

}
