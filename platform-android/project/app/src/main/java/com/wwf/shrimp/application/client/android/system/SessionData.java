package com.wwf.shrimp.application.client.android.system;

import android.app.Application;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.adapters.DocumentCardItemAdapter;
import com.wwf.shrimp.application.client.android.fragments.ProfileDocumentsFragment;
import com.wwf.shrimp.application.client.android.listeners.ConnectivityReceiver;
import com.wwf.shrimp.application.client.android.models.ConfigurationData;
import com.wwf.shrimp.application.client.android.models.dto.AppResource;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.DocumentType;
import com.wwf.shrimp.application.client.android.models.dto.Organization;
import com.wwf.shrimp.application.client.android.models.dto.User;
import com.wwf.shrimp.application.client.android.models.dto.search.DocumentSearchCriteria;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.models.view.FilterDTO;

import androidx.fragment.app.Fragment;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Session Data for application-wide property bag functionality.
 * Created by AleaActaEst on 17/06/2017.
 */

public class SessionData extends Application {

    public static final int IMAGE_ORIENTATION_PORTRAIT = 1;
    public static final int IMAGE_ORIENTATION_LANDSCAPE = 2;
    public static final int DOCUMENT_FILTER_ALL_DOCS = 0;
    public static final int DOCUMENT_FILTER_MY_DOCS = 1;
    public static final int DOCUMENT_CREATION = 1;
    public static final int DOCUMENT_EDIT= 2;
    public static final int DOCUMENT_READ_ONLY= 4;
    public static final int DOCUMENT_CONTEXT_EDIT= 8;

    public static final String USER_REMEMBER_ME_KEY = "user_name";
    public static final String USER_TOKEN_REMEMBER_ME_KEY = "user_token";


    public static final String MISSING_RESOURCE_VALUE= "MISSING VALUE";

    private static SessionData mInstance;

    private Map<String, AppResource> appResources = new HashMap<String, AppResource>();

    private Object changedField = null;

    /**
     * Latest profile docs for this user
     */
    private List<Document> profileDocs = new ArrayList<Document>();

    /**
     * GPS Data service
     */
    private FusedLocationProviderClient fusedLocationClient;

    /**
     *
     */
    private Map<String, DocumentCardItemAdapter> documentAdapterMap = new HashMap<String, DocumentCardItemAdapter>();


    /**
     * Document pages mapped to the number of pages for continuous camera shots
     */
    private Map<Integer, Integer> documentPagesMap = new HashMap<Integer, Integer>();

    private int currDocPagesCameraSnapCount = 0;

    /**
     * Access to the adapter that controls the document data
     */
    private transient DocumentCardItemAdapter documentAdapter;

    /**
     * Next document to be processed from the camera
     */
    private Deque<DocumentCardItem> nextDocument = new ArrayDeque<DocumentCardItem>();

    private int documentCreationStatus = DOCUMENT_CREATION;

    /**
     * used to hold the last long clikc position for my document
     */
    private int lastLongClickPositionMyDocument = -1;

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    private boolean isConnected = true;

    /**
     * The current user
     */
    private User currentUser;


    private transient Fragment currFragment = null;
    private transient ProfileDocumentsFragment currProfileFragment = null;

    /**
     * Image orientation for photo image previews
     */
    private int imageOrientation = IMAGE_ORIENTATION_PORTRAIT;


    /**
     * The current fab menu
     */
    private transient Map<String, FloatingActionsMenu> fabMenu  = new HashMap<String, FloatingActionsMenu>();

    /**
     * Property bag for data exchange for asynchronous requests
     */
    private Map<String, Object> propertyBag = new HashMap<String, Object>();

    /**
     * Property for storing data about the search filter
     */
    private DocumentSearchCriteria[] documentSearchFilterData  = new DocumentSearchCriteria[]{null, null};

    /**
     * The state of the filter (true-on; false-off) which signifies if the filter is applied or not.
     */
    private boolean[] documentSearchFilterState = new boolean[]{false, false};

    /**
     * Document types available to the
     */
    private List<DocumentType> documentTypes = new ArrayList<DocumentType>();

    private boolean popupDialogUp = false;

    /**
     * Current organization data - will be used to get organization and group information
     */
    private Organization currentOrganization;

    /**
     * Configuration and settings data
     */
    private transient ConfigurationData configurationData;

    /**
     * Used to sync data between smart camera gallery and the document pages
     */
    private boolean smartCameraSync = false;

    /**
     * Will hold document data for remote snapshot
     */
    private Map<String, List<DocumentCardItem>> documentLocalMap = new HashMap<String, List<DocumentCardItem>>();

    /**
     * Will hold information about notification document id
     */
    private String notificationDocSessionId = null;

    private int numberofGalleryPagesToDelete = 0;

    /**
     * Wil hold the filter data for the my docs information
     */
    private FilterDTO myDocsFilter = new FilterDTO();

    /**
     * Wil hold the filter data for the profile docs information
     */
    private FilterDTO profileDocsFilter = new FilterDTO();

    /**
     * Flag to check of the current document is editable
     */
    private boolean documentEditableFlag = true;

    /**
     * Flag to skip login if the user is already in the remember me preferences
     */
    private boolean rememberMeIsOn= false;



    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    //
    public SessionData() {
        // no op constructor
        this.documentPagesMap.put(R.string.vessel_captains_statement, 1);
        this.documentPagesMap.put(R.string.hatchery_fry_movement_document, 1);
        this.documentPagesMap.put(R.string.farm_fry_movement_document, 1);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Accessors
    //


    public Map<String, List<DocumentCardItem>> getDocumentLocalMap() {
        return documentLocalMap;
    }

    public void setDocumentLocalMap(Map<String, List<DocumentCardItem>> documentLocalMap) {
        this.documentLocalMap = documentLocalMap;
    }

    public Fragment getCurrFragment() {
        return currFragment;
    }

    public void setCurrFragment(Fragment currFragment) {

        this.currFragment = currFragment;
    }

    public ProfileDocumentsFragment getCurrProfileFragment() {
        return currProfileFragment;
    }



    public void setCurrProfileFragment(ProfileDocumentsFragment currProfileFragment) {
        this.currProfileFragment = currProfileFragment;
    }

    public boolean isSmartCameraSync() {
        return smartCameraSync;
    }

    public void setSmartCameraSync(boolean smartCameraSync) {
        this.smartCameraSync = smartCameraSync;
    }

    public ConfigurationData getConfigurationData() {
        return configurationData;
    }

    public void setConfigurationData(ConfigurationData configurationData) {
        this.configurationData = configurationData;
    }

    public Organization getCurrentOrganization() {
        return currentOrganization;
    }

    public void setCurrentOrganization(Organization currentOrganization) {
        this.currentOrganization = currentOrganization;
    }

    public List<DocumentType> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(List<DocumentType> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public boolean isDocumentSearchFilterState(int index) {
        return documentSearchFilterState[index];
    }

    public void setDocumentSearchFilterState(int index, boolean documentSearchFilterState) {
        this.documentSearchFilterState[index] = documentSearchFilterState;
    }

    public DocumentSearchCriteria getDocumentSearchFilterData(int index) {
        return documentSearchFilterData[index];
    }

    public void setDocumentSearchFilterData(int index, DocumentSearchCriteria documentSearchFilterData) {
        this.documentSearchFilterData[index] = documentSearchFilterData;
    }

    public Map<String, Object> getPropertyBag() {
        return propertyBag;
    }

    public void setPropertyBag(Map<String, Object> propertyBag) {
        this.propertyBag = propertyBag;
    }

    public int getLongClickPositionMyDocument() {
        return lastLongClickPositionMyDocument;
    }

    public void setLongClickPositionMyDocument(int lastLongClickPositionMyDocument) {
        this.lastLongClickPositionMyDocument = lastLongClickPositionMyDocument;
    }

    public int getImageOrientation() {
        return imageOrientation;
    }

    public void setImageOrientation(int imageOrientation) {
        this.imageOrientation = imageOrientation;
    }
    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currrentUser) {
        this.currentUser = currrentUser;
    }

    public Map<String, DocumentCardItemAdapter> getDocumentAdapterMap() {
        return documentAdapterMap;
    }

    public void setDocumentAdapterMap(Map<String, DocumentCardItemAdapter> documentAdapterMap) {
        this.documentAdapterMap = documentAdapterMap;
    }

    public DocumentCardItemAdapter getDocumentAdapter(String name){
        return documentAdapterMap.get(name);
    }

    public  void setDocumentAdapter(String name, DocumentCardItemAdapter adapter){
        documentAdapterMap.put(name, adapter);
    }

    public FloatingActionsMenu getFabMenu(String key) {
        return fabMenu.get(key);
    }

    public void setFabMenu(String key, FloatingActionsMenu fabMenu) {
        this.fabMenu.put(key, fabMenu);
    }

    public DocumentCardItemAdapter getDocumentAdapter() {
        return documentAdapter;
    }

    public void setDocumentAdapter(DocumentCardItemAdapter documentAdapter) {
        this.documentAdapter = documentAdapter;
    }

    public DocumentCardItem getNextDocument() {
        if(!nextDocument.isEmpty()) {
            return nextDocument.peek();
        }else{
            return null;
        }
    }

    public void setNextDocument(DocumentCardItem nextDocument) {
        this.nextDocument.push(nextDocument);
    }

    public void popNextDocument(){
        if(!nextDocument.isEmpty()) {
            nextDocument.pop();
        }
    }

    public int getNextDocumentStackSize(){
        return nextDocument.size();
    }

    //
    // initialization
    @Override
    public void onCreate(){

        super.onCreate();

        mInstance = this;
    }

    public boolean isPopupDialogUp() {
        return popupDialogUp;
    }

    public void setPopupDialogUp(boolean popupDialogUp) {
        this.popupDialogUp = popupDialogUp;
    }

    public void clearState(){

        if(documentAdapter != null && documentAdapter.getDataSet() != null) {
            documentAdapter.getDataSet().clear();
            documentAdapter.notifyDataSetChanged();
        }
        this.currFragment = null;
        this.currentUser = null;
        this.documentSearchFilterData = new DocumentSearchCriteria[]{null, null};
        this.setRememberMeIsOn(false);

    }

    public int getDocumentCreationStatus() {
        return documentCreationStatus;
    }

    public void setDocumentCreationStatus(int documentCreationStatus) {
        this.documentCreationStatus = documentCreationStatus;
    }

    public String getNotificationDocSessionId() {
        return notificationDocSessionId;
    }

    public void setNotificationDocSessionId(String notificationDocSessionId) {
        this.notificationDocSessionId = notificationDocSessionId;
    }

    public Map<Integer, Integer> getDocumentPagesMap() {
        return documentPagesMap;
    }

    public void setDocumentPagesMap(Map<Integer, Integer> documentPagesMap) {
        this.documentPagesMap = documentPagesMap;
    }

    public int getCurrDocPagesCameraSnapCount() {
        return currDocPagesCameraSnapCount;
    }

    public void setCurrDocPagesCameraSnapCount(int currDocPagesCameraSnapCount) {
        this.currDocPagesCameraSnapCount = currDocPagesCameraSnapCount;
    }

    public static synchronized SessionData getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

    public FusedLocationProviderClient getFusedLocationClient() {
        return fusedLocationClient;
    }

    public void setFusedLocationClient(FusedLocationProviderClient fusedLocationClient) {
        this.fusedLocationClient = fusedLocationClient;
    }

    public int getNumberofGalleryPagesToDelete() {
        return numberofGalleryPagesToDelete;
    }

    public void setNumberofGalleryPagesToDelete(int numberofGalleryPagesToDelete) {
        this.numberofGalleryPagesToDelete = numberofGalleryPagesToDelete;
    }

    public List<AppResource> getAppResources() {
        return (List<AppResource>)appResources.values();
    }

    public void setAppResources(List<AppResource> appResources) {
        for(int i=0; i<appResources.size(); i++){
            this.appResources.put(SessionData.generateResourceKey(appResources.get(i).getKey(),appResources.get(i).getLocale()), appResources.get(i));
        }
    }

    public List<Document> getProfileDocs() {
        return profileDocs;
    }

    public void setProfileDocs(List<Document> profileDocs) {
        this.profileDocs = profileDocs;
    }

    public String getInternationalizedResourceString(String key){
        String value;
        String language = Locale.getDefault().getLanguage();
        AppResource resource = appResources.get(SessionData.generateResourceKey(key, language));
        if(resource != null){
            value = resource.getValue();
        }else{
            value = MISSING_RESOURCE_VALUE;
        }

        return value;
    }

    public static String generateResourceKey(String key, String language){
        return key +"."+ language;
    }

    public Object getChangedField() {
        return changedField;
    }

    public void setChangedField(Object changedField) {
        this.changedField = changedField;
    }

    public FilterDTO getMyDocsFilter() {
        return myDocsFilter;
    }

    public void setMyDocsFilter(FilterDTO myDocsFilter) {
        this.myDocsFilter = myDocsFilter;
    }

    public FilterDTO getProfileDocsFilter() {
        return profileDocsFilter;
    }

    public void setProfileDocsFilter(FilterDTO profileDocsFilter) {
        this.profileDocsFilter = profileDocsFilter;
    }

    public boolean isDocumentEditableFlag() {
        return documentEditableFlag;
    }

    public void setDocumentEditableFlag(boolean documentEditableFlag) {
        this.documentEditableFlag = documentEditableFlag;
    }

    public boolean isRememberMeIsOn() {
        return rememberMeIsOn;
    }

    public void setRememberMeIsOn(boolean rememberMeIsOn) {
        this.rememberMeIsOn = rememberMeIsOn;
    }
}
