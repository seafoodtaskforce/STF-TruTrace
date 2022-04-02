package com.wwf.shrimp.application.client.android;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.viethoa.RecyclerViewFastScroller;
import com.viethoa.models.AlphabetItem;
import com.wwf.shrimp.application.client.android.adapters.SelectedTagListRecyclerViewAdapter;
import com.wwf.shrimp.application.client.android.adapters.ShowTagListRecyclerViewAdapter;
import com.wwf.shrimp.application.client.android.adapters.helpers.TagItemDataHelper;
import com.wwf.shrimp.application.client.android.adapters.helpers.TagItemSelectionDataHelper;
import com.wwf.shrimp.application.client.android.dialogs.CreateNewTagDialog;
import com.wwf.shrimp.application.client.android.fragments.MyDocumentsFragment;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.TagData;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;
import com.wwf.shrimp.application.client.android.utils.listeners.ClickListener;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Activity used for tagging documents.
 * @author AleaActaEst
 */
public class DocumentTaggingActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {

    private static final String LOG_TAG = "Doc Tagging Activity";
    public static final String PROPERTY_BAG_KEY = "newCustomTagText";
    public static final int EDIT_TAGS_FOR_DOCUMENT_REQUEST = 1;

    // global session data access
    private SessionData globalVariable = null;

    //
    // UI Elements
    RecyclerView mRecyclerViewSelectedTagList;
    RecyclerView mRecyclerViewTagList;
    RecyclerViewFastScroller fastScrollerTagList;
    Button buttonCancelTagging;
    Button buttonSaveTagging;
    Button buttonCreateNewTag;


    //
    // Data used in the UI elements
    private List<TagItemDataHelper.TagDataCard> mDataArray;
    private List<TagItemSelectionDataHelper.TagDataCard> mSelectedDataArray;
    private List<AlphabetItem> mAlphabetItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalVariable  = (SessionData) getApplicationContext();
        setContentView(R.layout.activity_tag_document);

        mRecyclerViewTagList = (RecyclerView) findViewById(R.id.recyclerViewTagList);
        fastScrollerTagList = (RecyclerViewFastScroller)findViewById(R.id.fast_scroller);

        mRecyclerViewSelectedTagList = (RecyclerView) findViewById(R.id.recyclerViewChosenTags);
        buttonCancelTagging = (Button)findViewById(R.id.buttonCancelTagging);
        buttonSaveTagging = (Button)findViewById(R.id.buttonSaveTagging);
        buttonCreateNewTag = (Button)findViewById(R.id.buttonCreateNewTag);

        initialiseTagListData();
        initialiseSelectedTagListData();
    }

    @Override
    protected void onResume() {

        super.onResume();
        Log.i(LOG_TAG, "Resuming the activity");
    }

    /**
     * This is a listener for the pop-up dialog dismissal
     * @param dialog - the dialog being dismissed
     */
    @Override
    public void onDismiss(final DialogInterface dialog) {
        //Fragment dialog had been dismissed
        Log.i(LOG_TAG, "Dialog Has been dismissed and we are resuming");
        TagData newTag = (TagData) globalVariable.getPropertyBag().get(PROPERTY_BAG_KEY);
        // addNewTag(newTag);

    }

    public void addNewTag(final TagData newTag){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(newTag != null){
                    Log.i(LOG_TAG, "Adding the new Tag to the list in the resume");
                    // Toast.makeText(this, "Got the value of the tag: " + newTag.getText(), Toast.LENGTH_SHORT).show();
                    globalVariable.getPropertyBag().remove(PROPERTY_BAG_KEY);

                    // set this to custom
                    newTag.setCustom(true);

                    // get the tags
                    List<TagData> allTags = convertTagsToInternalTags(mDataArray);
                    allTags.add(newTag);
                    assembleAllTags(allTags);

                    // get the selected tags
                    List<TagData> allSelectedTags = convertDocTagsFromInternalTags(mSelectedDataArray);
                    allSelectedTags.add(newTag);
                    assembleSelectedTags(allSelectedTags);

                    // done with the data
                    //

                    // initialize the refres of the UI
                    initialiseUI();
                }
            }
        });
    }


    /**
     * Initialize the UI data for a list of all tags
     */
    protected void initialiseTagListData() {
        /**
         * get data from the server about tags
         */
        String url = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestTagFetchAllURL();
        Log.i(LOG_TAG, "REST - get all tags - call: " + url);

        // Create the handler
        DocumentTaggingActivity.OkHttpTagDataHandler okHttpHandler= new DocumentTaggingActivity.OkHttpTagDataHandler();

        // execute the call synchronously
        okHttpHandler.execute(url);
        // mDataArray = TagItemDataHelper.getAlphabetData();
    }

    protected void initialiseSelectedTagListData() {
        /**
         * get data from the server about tags
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
                    + globalVariable.getConfigurationData().getRestTagFetchByDocumentSyncIdURL()
                    + documentCardItem.getSyncID();
            Log.i(LOG_TAG, "REST - get all tags for specific document - call: " + url);

            // Create the handler
            DocumentTaggingActivity.OkHttpTagDataHandler okHttpHandler = new DocumentTaggingActivity.OkHttpTagDataHandler();

            // execute the call synchronously
            okHttpHandler.execute(url);
        }else{
            mSelectedDataArray = convertDocTagsToInternalTags(globalVariable.getNextDocument().getTags());
        }

    }

    protected void initialiseUI() {

        // final syncing
        for(int i=0; i<mSelectedDataArray.size(); i++){
            int index = isSelectedTag(mSelectedDataArray.get(i).getId());
            if( index != -1){
                mDataArray.get(index).setCheckState(true);
            }
        }

        mRecyclerViewTagList.setLayoutManager(new LinearLayoutManager(this));
        // create adapter
        ShowTagListRecyclerViewAdapter listAdapter = new ShowTagListRecyclerViewAdapter(mDataArray, globalVariable);
        mRecyclerViewTagList.setAdapter(listAdapter);
        mRecyclerViewSelectedTagList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SelectedTagListRecyclerViewAdapter selectedAdapter = new SelectedTagListRecyclerViewAdapter(mSelectedDataArray,globalVariable);
        mRecyclerViewSelectedTagList.setAdapter(selectedAdapter);
        listAdapter.setSelectedAdapter(selectedAdapter);
        selectedAdapter.setListAdapter(listAdapter);

        mRecyclerViewTagList.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerViewTagList, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {

                // do nothing at this point
            }

            @Override
            public void onLongClick(View view, int position) {
                // do nothing at this point
            }
        }));

        fastScrollerTagList.setRecyclerView(mRecyclerViewTagList);
        fastScrollerTagList.setUpAlphabet(mAlphabetItems);

        //
        // Dialog Button Handlers
        buttonCancelTagging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cancel the operation by going back
                onBackPressed();
            }
        });

        /**
         * Save the data from the user choices
         */
        buttonSaveTagging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // execute the POST action
                //

                // first convert data
                List<TagData> tags = convertDocTagsFromInternalTags(mSelectedDataArray);

                //////////////////////////////////////////////////////////////////////////////
                // Edit content
                //
                if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_EDIT ||
                        globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_CONTEXT_EDIT ) {
                    // Save the data for the document
                    DocumentCardItem documentCardItem = globalVariable
                            .getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY)
                            .getDataSet()
                            .get(globalVariable.getLongClickPositionMyDocument());

                    //
                    // Commit the converted data to REST Service to attach the selected tags
                    String postUrl = globalVariable.getConfigurationData().getServerURL()
                            + globalVariable.getConfigurationData().getApplicationInfixURL()
                            + globalVariable.getConfigurationData().getRestTagAttachByDocumentSyncIdURL();


                    String postBody = RESTUtils.getJSON(tags);
                    globalVariable.getNextDocument().setTags(tags);
                    Log.i(LOG_TAG, "PRE-REST - attach tag to a document by sync id - call: Call for MyDocs Doc ");
                    RESTUtils.executePOSTDocumentSyncTagAttachRequest(postUrl, postBody, documentCardItem.getSyncID(), "", globalVariable, "");


                }else{
                    //////////////////////////////////////////////////////////////////////////////
                    // Create content
                    //
                    globalVariable.getNextDocument().setTags(tags);

                }




                onBackPressed();
            }
        });

        buttonCreateNewTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the new dialog and pass to it the global variable
                CreateNewTagDialog dFragment = new CreateNewTagDialog();

                // Show DialogFragment
                dFragment.show(getSupportFragmentManager(),"Dialog Fragment" );
                // dFragment.show(fm, "Dialog Fragment");
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
    public class OkHttpTagDataHandler extends AsyncTask<String, String, String> {

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
            Log.d(LOG_TAG, "OkHTTP Request: " + request.url());

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            }catch (Exception e){
                e.printStackTrace();
                serverDownFlag = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            /**
             * extract the data for the GSON authentication
             */

            Log.i(LOG_TAG, "REST - getting response for url: " + requestURL);

            // documents holder for fetching all documents
            ArrayList<TagData> tags;
            ArrayList<TagData> tagsByDoc;
            ArrayList<Document> linkedByDoc;

            //
            // GSON parser
            Gson gson = new GsonBuilder()
                    .create();

            //
            // parse the JSON input into the specific class

            ///////////////////////////////////////////////////////////////////////////////////////
            // Getting Tags form the backend
            //
            if(requestURL.contains(globalVariable.getConfigurationData().getRestTagFetchAllURL())) {
                Type listType = new TypeToken<ArrayList<TagData>>() {
                }.getType();
                tags = gson.fromJson(s, listType);
                Log.d(LOG_TAG, "REST - fetch all tags - result from remote server: " + tags);

                // process the tags
                assembleAllTags(tags);

                // get the data from the session about the document
                mSelectedDataArray = new ArrayList<TagItemSelectionDataHelper.TagDataCard>();

            }

            ///////////////////////////////////////////////////////////////////////////////////////
            // Getting users from the backend
            //
            if(requestURL.contains(globalVariable.getConfigurationData().getRestTagFetchByDocumentSyncIdURL())){
                Type listType = new TypeToken<ArrayList<TagData>>() {
                }.getType();
                tagsByDoc = gson.fromJson(s, listType);
                Log.d(LOG_TAG, "REST - fetch all tags attached to a doc - result from remote server: " + tagsByDoc);
                assembleSelectedTags(tagsByDoc);
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
                //mSelectedDataArray = convertLinkedDocsToInternalDocs(linkedByDoc);

            }

            initialiseUI();
        }

    }

    /**************************************************************************
     ****** Private helper methods
     *****************************************************************************/

    private void assembleAllTags(List<TagData> tags){

        ////tags = convertTagsToCustomForm(tags);
        // sort the entities by their name
        Collections.sort(tags, new Comparator<TagData>() {
            @Override
            public int compare(TagData lhs, TagData rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getText(), rhs.getText());
                if (res == 0) {
                    res = lhs.getText().compareTo(rhs.getText());
                }
                return res;
            }
        });

        // convert session data
        mDataArray = convertTags(tags);
        //Alphabet fast scroller data
        mAlphabetItems = new ArrayList<>();
        List<String> strAlphabets = new ArrayList<>();
        for (int i = 0; i < mDataArray.size(); i++) {

            String name = mDataArray.get(i).getTagText();
            if (name == null || name.trim().isEmpty())
                continue;

            String word = name.substring(0, 1);
            if (!strAlphabets.contains(word)) {
                strAlphabets.add(word);
                mAlphabetItems.add(new AlphabetItem(i, word, false));
            }
        }

    }

    private void assembleSelectedTags(List<TagData> tagsByDoc){

       //// tagsByDoc = convertTagsToCustomForm(tagsByDoc);
        // sort the entities by their date
        Collections.sort(tagsByDoc, new Comparator<TagData>() {
            @Override
            public int compare(TagData lhs, TagData rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getText(), rhs.getText());
                if (res == 0) {
                    res = lhs.getText().compareTo(rhs.getText());
                }
                return res;
            }
        });

        // get the data from the session about the document
        mSelectedDataArray = convertDocTagsToInternalTags(tagsByDoc);
    }

    private List<TagData> convertTagsToCustomForm(List<TagData> tags){

        for(int i=0; i< tags.size(); i++){
            if(tags.get(i).isCustom()) {
                if(!tags.get(i).getText().contains(tags.get(i).getCustomPrefix())){
                    tags.get(i).setText(tags.get(i).getCustomPrefix() + " " + tags.get(i).getText());
                }
            }
        }
        return tags;
    }

    private List<TagItemDataHelper.TagDataCard> convertTags(List<TagData> tags){
        List<TagItemDataHelper.TagDataCard> result = new ArrayList<>();

        for(int i=0; i< tags.size(); i++){
            TagData tag = tags.get(i);

            // Strip the custom prefix
            String tempTag = stripTagPrefix(tag.getText());
            tag.setText(tempTag.trim());
            TagItemDataHelper.TagDataCard item = new TagItemDataHelper.TagDataCard(tag.getText(), tag.getCustomPrefix(), false);
            item.setId(tag.getId());

            result.add(item);
        }

        return result;
    }

    private  List<TagData> convertTagsToInternalTags(List<TagItemDataHelper.TagDataCard> tags){
        List<TagData> result = new ArrayList<>();

        for(int i=0; i< tags.size(); i++){
            TagItemDataHelper.TagDataCard tag = tags.get(i);
            TagData item = new TagData();

            item.setId(tag.getId());
            item.setText(tag.getTagText());
            item.setCustom(tag.isCustom());
            item.setCustomPrefix(tag.getCustomPrefix());

            result.add(item);
        }

        return result;
    }

    /**
     * Conversion from actual Tag data to visual selection data
     * @param tags -  internal tag data (actual data)
     * @return - the converted visual representation
     */
    private List<TagItemSelectionDataHelper.TagDataCard> convertDocTagsToInternalTags(List<TagData> tags){
        List<TagItemSelectionDataHelper.TagDataCard> result = new ArrayList<TagItemSelectionDataHelper.TagDataCard>();

        for(int i=0; i< tags.size(); i++){
            TagData tag = tags.get(i);
            // Strip the custom prefix
            String tempTag = stripTagPrefix(tag.getText());
            tag.setText(tempTag.trim());
            TagItemSelectionDataHelper.TagDataCard item = new TagItemSelectionDataHelper.TagDataCard(tag.getId(), tag.getText(), tag.getCustomPrefix());
            item.setCustom(tag.isCustom());


            result.add(item);
        }

        return result;
    }

    /**
     * Conversion from visual Tag data to actual data
     * @param tags - the visual list representation of tag data
     * @return - the list of actual TagData entities
     */
    private List<TagData> convertDocTagsFromInternalTags(List<TagItemSelectionDataHelper.TagDataCard> tags){
        List<TagData> result = new ArrayList<TagData>();

        for(int i=0; i< tags.size(); i++){
            TagItemSelectionDataHelper.TagDataCard tag = tags.get(i);
            TagData item = new TagData();
            item.setId(tag.getId());
            item.setText(tag.getTagText());
            item.setCustom(tag.isCustom());
            item.setCustomPrefix(tag.getCustomPrefix());

            result.add(item);
        }

        return result;
    }

    /**
     * Check if the tag has been already selected in the visual list
     * @param tagId - the id of the tag (in the visual list)
     * @return - true if selected; false otherwise
     */
    private int isSelectedTag(long tagId){
        int result = -1;
        for(int i=0; i<mDataArray.size(); i++){
            if(mDataArray.get(i).getId() == tagId){
                return i;
            }
        }

        return result;
    }

    /**
     * Helper method to strip the custom tag of the custom prefix
     * @param tagText - the tag to be stripped
     * @return - the stripped tag
     */
    private String stripTagPrefix(String tagText){
        //
        // Precondition
        if(tagText == null) return "";

        // Process
        String replacementText = globalVariable.getConfigurationData().getGlobalConstantTaggingCustomPrefix();
        return tagText.replace(replacementText, "");
    }

    private boolean checkCustomTag(){
        boolean result = true;

        return result;
    }

    public Fragment getParentFragmentCustom() {
        List<Fragment>  fragments = getSupportFragmentManager().getFragments();
        ListIterator<Fragment> fragmentIterator = getSupportFragmentManager().getFragments().listIterator();
        while (fragmentIterator.hasNext()) {
            Fragment fragment = fragmentIterator.next();
            if(fragment instanceof MyDocumentsFragment){
                return fragment;
            }
        }
        return null;
    }

    public void getLinkedDocsByTags(){

        //
        // Get the current doc
        DocumentCardItem currDocItem = globalVariable.getNextDocument();

        // got each tag that was chosen
        for(int tag_index= 0; tag_index< globalVariable.getNextDocument().getTags().size(); tag_index++){
            if(!currDocItem.getTags().get(tag_index).isCustom()){
                continue;
            }

            //
            // get the list of documents that could be linked

            // for each adapter that holds document items for this session
            for(int j=0; j<globalVariable.getDocumentAdapterMap().keySet().size(); j++){

                // for each adapter key
                Iterator<String> iterator = globalVariable.getDocumentAdapterMap().keySet().iterator();
                while(iterator.hasNext()){
                    // for each document in the adapter
                    List<DocumentCardItem> items = globalVariable.getDocumentAdapterMap().get(iterator.next()).getDataSet();
                    for(int item_index=0; item_index<items.size(); item_index++ ){
                        //
                        // cannot link itself
                        if(items.get(item_index).getId() == currDocItem.getId()){
                            // skip
                            continue;
                        }
                        //
                        // Make sure that the doc can be legally linked, so it cannot be from teh same org
                        // but must be from the same stage
                        if(items.get(item_index).getGroupName().equals(currDocItem.getGroupName())
                                || items.get(item_index).getGroupType().equals(currDocItem.getGroupType())){
                            // skip
                            continue;
                        }
                        // check if the element is a linked doc by tag

                        for(int item_tag_index=0; item_tag_index < items.get(item_index).getTags().size(); item_tag_index++){
                            if(items.get(item_index).getTags().get(item_tag_index)
                                    .equals(currDocItem.getTags().get(tag_index))) {
                                // add the item into the linked docs if not present
                                boolean isPresent = false;
                                for(int curr_linked_doc_index = 0;
                                    curr_linked_doc_index < currDocItem.getLinkedDocuments().size();
                                    curr_linked_doc_index++){
                                    if(currDocItem.getLinkedDocuments().get(curr_linked_doc_index).getId()
                                            == items.get(item_index).getId()){
                                        isPresent = true;
                                        break;
                                    }
                                }
                                if(!isPresent){
                                    // add the doc into linked list
                                    currDocItem.getLinkedDocuments()
                                            .add(DocumentPOJOUtils.convertDocumentCardItemToDocument(items.get(item_index)));
                                }

                            }
                        }

                    }
                }


            }
        }
    }

    public void initializeLinkingData(){
        /**
         * get data from the server about all documents
         */
        String url;

        url = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestDocumentFetchAllURL();
        Log.i(LOG_TAG, "REST - get all documents - call: " + url);

        // Create the handler
        OkHttpTagDataHandler okHttpHandler= new OkHttpTagDataHandler();

        // execute the call synchronously
        okHttpHandler.execute(url);
    }
}
