package com.wwf.shrimp.application.client.android.dialogs;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.wwf.shrimp.application.client.android.DocumentTaggingActivity;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.adapters.CustomPagedDocumentDialogAdapter;
import com.wwf.shrimp.application.client.android.adapters.DocumentPageImageAdapter;
import com.wwf.shrimp.application.client.android.adapters.ShowAttachedDocumentsListRecyclerViewAdapter;
import com.wwf.shrimp.application.client.android.adapters.ShowLinkedDocumentsListRecyclerViewAdapter;
import com.wwf.shrimp.application.client.android.adapters.helpers.AttachedDocumentItemDataHelper;
import com.wwf.shrimp.application.client.android.adapters.helpers.LinkedDocumentItemDataHelper;
import com.wwf.shrimp.application.client.android.fragments.AllDocumentsFragment;
import com.wwf.shrimp.application.client.android.fragments.CustomFragmentDocumentImagePage;
import com.wwf.shrimp.application.client.android.fragments.CustomFragmentDocumentNotes;
import com.wwf.shrimp.application.client.android.fragments.CustomFragmentDocumentPageIndex;
import com.wwf.shrimp.application.client.android.fragments.CustomFragmentDocumentPageSummary;
import com.wwf.shrimp.application.client.android.fragments.MyDocumentsFragment;
import com.wwf.shrimp.application.client.android.fragments.ProfileDocumentsFragment;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.DocumentPage;
import com.wwf.shrimp.application.client.android.models.dto.DynamicFieldData;
import com.wwf.shrimp.application.client.android.models.dto.DynamicFieldDefinition;
import com.wwf.shrimp.application.client.android.models.dto.NoteData;
import com.wwf.shrimp.application.client.android.models.dto.TagData;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.models.view.DocumentContext;
import com.wwf.shrimp.application.client.android.models.view.GalleryDocumentPage;
import com.wwf.shrimp.application.client.android.services.dao.DocumentJSONService;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils;
import com.wwf.shrimp.application.client.android.utils.FileUtils;
import com.wwf.shrimp.application.client.android.utils.ImageUtils;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;
import com.wwf.shrimp.application.client.android.utils.ViewPagerFixed;
import com.wwf.shrimp.application.client.android.utils.adapters.dynamicgrid.BaseDynamicGridAdapter;
import com.wwf.shrimp.application.client.android.utils.dialogs.CancelDialogUtility;
import com.wwf.shrimp.application.opennotescanner.OpenNoteScannerActivity;
import com.wwf.shrimp.application.opennotescanner.helpers.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import static com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils.getAllDynamicFieldData;
import static com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils.getAllDynamicFieldsForDocument;
import static com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils.getAllOCRDynamicFieldsForDocument;
import static com.wwf.shrimp.application.client.android.utils.OCRUtils.getFullOCRText;
import static com.wwf.shrimp.application.client.android.utils.OCRUtils.getTagFromDocument;

/**
 * Dialog with tabs for different types of Documents for this user
 *
 * @author AleaActaEst
 */
public class TabbedDocumentDialog extends DialogFragment implements CreateDocNotesDialog.DialogListener {

    // logging tag
    private static final String LOG_TAG = "Tabbed Document Dialog";
    public static final String FRAGMENT_TAG = "Tabbed Document Dialog";

    private static final String IMAGE_DIRECTORY = "/truetrace";

    public final static int DIALOG_MODE_READ_ONLY = 2;
    public final static int DIALOG_MODE_WRITE = 4;

    public final static int DATA_MODE_LOCAL = 1;
    public final static int DATA_MODE_REMOTE = 2;
    public final static int DATA_MODE_BOTH = 3;

    public final static int FRAGMENT_TAB_SUMMARY = R.string.document_custom_dialog_tab_details;
    public final static int FRAGMENT_TAB_INDEX = R.string.document_custom_dialog_tab_gallery;
    public final static int FRAGMENT_TAB_NOTES = R.string.document_custom_dialog_tab_notes;
    public final static String FRAGMENT_TAB_PAGE = "Page";

    private static final int CAPTURE_MEDIA = 368;

    private static int IMG_RESULT_ATTACH = 101;

    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;

    private TextRecognizer detector;

    // GPS Data
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    TabLayout tabLayout;
    ViewPager viewPager;
    private int pages = 0;
    private DocumentCardItem document = null;
    private int dialogMode = DIALOG_MODE_READ_ONLY;
    private CustomPagedDocumentDialogAdapter adapter;
    private int dataMode = DATA_MODE_LOCAL;
    private ProgressBar progressBar;
    private FloatingActionButton fabTakeDocImage;
    private FloatingActionButton fabAttachDocImage;

    // global session data
    private SessionData globalVariable;
    private Fragment containerFragment;
    private DocumentPageImageAdapter pageImageAdapter;

    // buttons
    private Button doneButton;
    private Button submitButton;
    private Button cancelButton;
    private Button acceptButton;
    private Button rejectButton;
    private Button deletePagesButton;

    // services
    private DocumentJSONService documentService;

    public TabbedDocumentDialog() {
        // Empty constructor required for DialogFragment
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // get the view
        View rootview = inflater.inflate(R.layout.custom_document_dialog, container, false);

        // set up the session
        globalVariable = (SessionData) getContext().getApplicationContext();


        // progress bar
        progressBar = (ProgressBar) rootview.findViewById(R.id.progressBarRemoteService);
        // tabl layout
        tabLayout = (TabLayout) rootview.findViewById(R.id.tabLayout);
        // the view pager that holds the tabs
        viewPager = (ViewPagerFixed) rootview.findViewById(R.id.masterViewPager);

        doneButton = (Button) rootview.findViewById(R.id.buttonDocumentDialogDone);
        submitButton = (Button) rootview.findViewById(R.id.buttonDocumentDialogSubmit);
        cancelButton = (Button) rootview.findViewById(R.id.buttonDocumentDialogCancel);
        acceptButton = (Button) rootview.findViewById(R.id.buttonDocumentDialogAccept);
        rejectButton = (Button) rootview.findViewById(R.id.buttonDocumentDialogReject);
        deletePagesButton = (Button) rootview.findViewById(R.id.buttonDocumentDialogPageDelete);

        /**
         * FAB Handling for taking a new Photo and attaching an existing photo
         * as well as swapping photos
         */
        fabTakeDocImage = (FloatingActionButton) rootview.findViewById(R.id.fabTakeImage);
        fabAttachDocImage = (FloatingActionButton) rootview.findViewById(R.id.fabAttachImage);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                // Check if this is the page you want.
                // Toast.makeText(getContext(), "Tab was selected " + position, Toast.LENGTH_SHORT).show();
                //Fragment fragment = adapter.getItem(position);
                //if (fragment != null) {
                //   fragment.onResume();
                //}
            }
        });

        refreshUI();

        System.out.println("DOCUMENT PAGED DIALOG " + document);

        fabTakeDocImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the snapshot here or camera data

                // TODO add settings for different cameras later, current setting calls scanner
                // snapCameraPhoto(getContext());

                // Start the note scanner
                startNoteScanner(getContext());
                globalVariable.setCurrFragment(getParentFragmentCustom());
            }
        });

        fabAttachDocImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // attach the document page
                attachDocumentPage();
            }
        });

        /**
         * Handling of the bottom buttons
         */


        //
        // Cancelling the Document creation/edition
        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_CREATION
                        || globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_EDIT) {
                    String status = globalVariable.getNextDocument().getStatus();
                    if (status.equals(Document.STATUS_SUBMITTED)
                            || status.equals(Document.STATUS_RESUBMITTED)
                            || status.equals(Document.STATUS_PENDING)
                            || status.equals(Document.STATUS_ACCEPTED)) {
                        if (
                                (getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(MyDocumentsFragment.FRAGMENT_TAG))
                                        || (getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(AllDocumentsFragment.FRAGMENT_TAG))) {
                            TabbedDocumentDialog.super.dismiss();
                            // remove this doc from the stack
                            globalVariable.popNextDocument();
                            return;
                        }
                    }
                    if (status.equals(Document.STATUS_REJECTED)
                            && (getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(AllDocumentsFragment.FRAGMENT_TAG))) {
                        TabbedDocumentDialog.super.dismiss();
                        // remove this doc from the stack
                        globalVariable.popNextDocument();
                        return;
                    }
                    final AlertDialog alertDialog = CancelDialogUtility.showCancelDialog(getContext());
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Toast.makeText(getContext(), getResources().getString(R.string.document_custom_dialog_cancel_toast_message), Toast.LENGTH_SHORT).show();
                            //
                            // Check for any gallery images and remove them
                            ArrayList<String> galleryFiles = new ArrayList<>();
                            galleryFiles = new Utils(getContext()).getFilePaths(null);
                            for (int i = 0; i < galleryFiles.size(); i++) {
                                deleteImageFromGallery(new File(galleryFiles.get(i)));
                            }

                            alertDialog.dismiss();
                            TabbedDocumentDialog.super.dismiss();
                            // remove this doc from the stack
                            globalVariable.popNextDocument();

                        }
                    });
                } else {
                    TabbedDocumentDialog.super.dismiss();
                    // remove this doc from the stack
                    globalVariable.popNextDocument();
                }
            }
        });


        /**
         * Deleting the Pages of the Document creation/edition
         */
        deletePagesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                /**
                 * remove the marked pages from the database
                 */

                String deletePagesUrl = globalVariable.getConfigurationData().getServerURL()
                        + globalVariable.getConfigurationData().getApplicationInfixURL()
                        + globalVariable.getConfigurationData().getRestDocumentDeletePagesURL();
                //
                // remove them from the actual document
                // <TODO> Deletion Issues
                //RESTUtils.executeDELETEDocumentPagesRequest(deletePagesUrl, globalVariable.getNextDocument().getSyncID(), globalVariable, null);

                /**
                 * remove the marked pages from the grid adapter
                 */
                globalVariable.setNumberofGalleryPagesToDelete(0);

                //
                // first image pages [in memory]
                Iterator iter = globalVariable.getNextDocument().getImagePages().iterator();
                List<GalleryDocumentPage> tempDocs = new ArrayList<GalleryDocumentPage>();
                while(iter.hasNext()){
                    GalleryDocumentPage page = (GalleryDocumentPage)iter.next();
                    if(page.isDeleted()){
                        tempDocs.add(page);
                        // deleteImageFromGallery((File)page.getPage());
                        iter.remove();
                    }
                }

                //
                // then actual pages [in memory]
                iter = globalVariable.getNextDocument().getDocumentPages().iterator();
                while(iter.hasNext()){
                    GalleryDocumentPage page = (GalleryDocumentPage)iter.next();
                    if(page.isDeleted()){
                        tempDocs.add(page);
                        iter.remove();
                    }
                }

                //
                // recollate pages in the document
                //globalVariable.getNextDocument().setDocumentPages(DocumentPOJOUtils.collateGalleryDocPages(globalVariable.getNextDocument().getDocumentPages()));

                //
                // recollate pages
                recollateDocumentPages();

                //
                // update the index page
                CustomFragmentDocumentPageIndex documentPageIndex
                        = (CustomFragmentDocumentPageIndex) adapter.getFragmentByTitle(getResources().getString(FRAGMENT_TAB_INDEX));
                if(documentPageIndex.getGridview() != null){
                    Log.i(LOG_TAG, "GridView is *NOT* null.");
                    BaseDynamicGridAdapter indexAdapter = (BaseDynamicGridAdapter) documentPageIndex.getGridview().getAdapter();
                    indexAdapter.removeAllDeletedItems();
                }else{
                    Log.i(LOG_TAG, "GridView *IS* null.");
                    Iterator<Fragment> fragIter =  getFragmentManager().getFragments().iterator();
                    TabbedDocumentDialog myFrag = null;
                    while(fragIter.hasNext()){
                        Fragment frag = fragIter.next();
                        if(frag instanceof TabbedDocumentDialog){
                            myFrag = (TabbedDocumentDialog)frag;
                            break;
                        }
                    }
                    if (myFrag != null) {
                        fragIter = myFrag.getChildFragmentManager().getFragments().iterator();
                        CustomFragmentDocumentPageIndex myGalleryFrag = null;
                        while(fragIter.hasNext()){
                            Fragment frag = fragIter.next();
                            if(frag instanceof CustomFragmentDocumentPageIndex){
                                myGalleryFrag = (CustomFragmentDocumentPageIndex)frag;
                                break;
                            }
                        }
                        if(myGalleryFrag.getGridview() != null) {
                            Log.i(LOG_TAG, "GridView <2nd pass> is *NOT* null.");
                            BaseDynamicGridAdapter indexAdapter = (BaseDynamicGridAdapter) myGalleryFrag.getGridview().getAdapter();
                            indexAdapter.removeAllDeletedItems();
                        }else{
                            Log.i(LOG_TAG, "GridView <2nd pass> *IS* null.");
                        }
                    }
                }

                //
                // Update all the dynamic tabs that deal with individual pages
                Iterator pageIter = tempDocs.iterator();
                while(pageIter.hasNext()){
                    GalleryDocumentPage page = (GalleryDocumentPage)pageIter.next();
                    if(page.getPage() instanceof File){
                        deleteImageFromGallery((File)page.getPage());
                    }
                    adapter.removeFragmentByTitle(getResources().getString(R.string.document_custom_dialog_tab_page)
                            + " "
                            + (page.getPosition() + 1));
                    pageIter.remove();
                }

                /**
                 * recalculate the Tab Names for the gallery pages
                 */
                adapter.recollatePageFragments(getResources().getString(R.string.document_custom_dialog_tab_page));

                /**
                 * remove the underlying delete button
                 */

                deletePagesButton.setVisibility(View.GONE);

                adapter.notifyDataSetChanged();

                /**
                 * Delete the actual images from the drive or sd card on teh phone
                 */


            }
        });



        //
        // Handling the creation/edition of document

        if (globalVariable.getNextDocument().getContext() != null) {
            DocumentContext context = globalVariable.getNextDocument().getContext();
            //
            // check if the referring page is a linking list
            if (context.getContextName().equals(DocumentContext.DOCUMENT_LINIKING_ACTIVITY_NAME)) {
                if (((LinkedDocumentItemDataHelper.LinkedDocumentDataCard) context.getDataItem()).isCheckState()) {
                    doneButton.setText(R.string.document_linked_document_unlink_button);
                    submitButton.setVisibility(View.INVISIBLE);
                } else {
                    doneButton.setText(R.string.document_linked_document_link_button);
                    submitButton.setVisibility(View.INVISIBLE);
                }
            }
            //
            // check if the referring page is the back up docs list
            if (context.getContextName().equals(DocumentContext.DOCUMENT_ATTACHING_ACTIVITY_NAME)) {
                if (((AttachedDocumentItemDataHelper.AttachedDocumentDataCard) context.getDataItem()).isCheckState()) {
                    doneButton.setText(R.string.document_attached_document_unattach_button);
                    submitButton.setVisibility(View.INVISIBLE);
                } else {
                    doneButton.setText(R.string.document_attached_document_attach_button);
                    submitButton.setVisibility(View.INVISIBLE);
                }
            }

        }
        //
        // Handle the action button for DONE document
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doneButtonClickHandler(view, globalVariable.getNextDocument().getStatus());
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (globalVariable.getNextDocument().getStatus().equals(Document.STATUS_DRAFT)) {
                    globalVariable.getNextDocument().setStatus(Document.STATUS_SUBMITTED);
                }
                if (globalVariable.getNextDocument().getStatus().equals(Document.STATUS_REJECTED)) {
                    globalVariable.getNextDocument().setStatus(Document.STATUS_RESUBMITTED);
                }
                doneButtonClickHandler(view, globalVariable.getNextDocument().getStatus());
            }
        });

        //
        // Handle the action button for DONE document
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptButtonClickHandler(view);
            }
        });

        //
        // Handle the action button for DONE document
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rejectButtonClickHandler(view);
            }
        });


        return rootview;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "ON CREATE DIALOG Call for Tabbed Doc");
        return new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                boolean warn = true;
                String status = globalVariable.getNextDocument().getStatus();
                if (status.equals(Document.STATUS_SUBMITTED)
                        || status.equals(Document.STATUS_RESUBMITTED)
                        || status.equals(Document.STATUS_PENDING)
                        || status.equals(Document.STATUS_ACCEPTED)) {
                    if (
                            (getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(MyDocumentsFragment.FRAGMENT_TAG))
                                    || (getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(AllDocumentsFragment.FRAGMENT_TAG))) {
                        warn = false;
                    }
                }
                if (warn) {
                    // super.onBackPressed();
                    Toast.makeText(getContext(), "Please use the CANCEL and DONE buttons of the dialog", Toast.LENGTH_SHORT).show();
                } else {
                    //do your stuff
                    Toast.makeText(getContext(), "Please use the CANCEL and DONE buttons of the dialog", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    /**
     * Instance creation with global data access initialization
     * @param savedInstanceState - the saved instance data from previous instance.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "CREATE Call for Tabbed Doc");
        super.onCreate(savedInstanceState);
        globalVariable = (SessionData) getContext().getApplicationContext();
        detector = new TextRecognizer.Builder(getContext()).build();
        setRetainInstance(true);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext().getApplicationContext());
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, "RESUME Call for Tabbed Doc");
        super.onResume();

        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        //refreshUI();

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "OnSTOP Call for Tabbed Doc");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "ON PAUSE Call for Tabbed Doc");
    }

    public TabLayout getTabLayout() {
        return this.tabLayout;
    }

    public DocumentCardItem getDocument() {
        return document;
    }



    public void setDocument(DocumentCardItem document) {
        this.document = document;
    }

    public int getDialogMode() {
        return dialogMode;
    }

    public void setDialogMode(int dialogMode) {
        this.dialogMode = dialogMode;
    }

    public void setDataMode(int dataMode) {
        this.dataMode = dataMode;
    }

    /**
     * Helper methods
     */

    private void refreshUI() {

        document = globalVariable.getNextDocument();
        //
        // set the main buttons
        //

        ////////////////////////////////////////
        // doneButton visibility

        //
        // My Documents
        if (getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(MyDocumentsFragment.FRAGMENT_TAG)) {
            // set text
            doneButton.setText((getResources().getString(R.string.document_custom_dialog_done_button)));
            // set visibility based on user/owner
            if (globalVariable.getCurrentUser().getName().toLowerCase().equals(document.getUsername().toLowerCase())) {
                if (!document.getStatus().equals(Document.STATUS_DRAFT)
                        && !document.getStatus().equals(Document.STATUS_REJECTED)) {
                    doneButton.setVisibility(View.INVISIBLE);
                    // Cannot edit
                    fabTakeDocImage.setVisibility(View.INVISIBLE);
                    fabAttachDocImage.setVisibility(View.INVISIBLE);

                } else {
                    doneButton.setVisibility(View.VISIBLE);
                    // TODO Reject <Add later>
                    rejectButton.setVisibility(View.GONE);
                }
            }
        }
        //
        // All documents
        if (getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(AllDocumentsFragment.FRAGMENT_TAG)) {
            doneButton.setText((getResources().getString(R.string.document_custom_dialog_done_button)));
            doneButton.setVisibility(View.INVISIBLE);
            // set visibility based on user/owner
            boolean showAcceptanceWorkflow = showAcceptanceWorkflow(document);
            if (showAcceptanceWorkflow) {
                if (document.getStatus().equals(Document.STATUS_SUBMITTED)
                        || document.getStatus().equals(Document.STATUS_RESUBMITTED)
                        || document.getStatus().equals(Document.STATUS_PENDING)
                ) {
                    doneButton.setVisibility(View.GONE);
                    // TODO Reject <Add later>
                    rejectButton.setVisibility(View.VISIBLE);
                }
            } else {
                doneButton.setVisibility(View.INVISIBLE);
                // TODO Reject <Add later>
                rejectButton.setVisibility(View.GONE);
            }
        }
        //
        // Profile Documents
        if (getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(ProfileDocumentsFragment.FRAGMENT_TAG)) {
            doneButton.setText((getResources().getString(R.string.document_custom_dialog_done_button)));
            doneButton.setVisibility(View.VISIBLE);
            // TODO Reject <Add later>
            rejectButton.setVisibility(View.GONE);
        }

        ///////////////////////////////////////
        // Submit Button Visibility


        if (getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(MyDocumentsFragment.FRAGMENT_TAG)) {
            submitButton.setText((getResources().getString(R.string.document_acceptance_workflow_button_submit)));
            if (!document.getStatus().equals(Document.STATUS_DRAFT)
                    && !document.getStatus().equals(Document.STATUS_REJECTED)) {
                submitButton.setVisibility(View.INVISIBLE);
            } else {
                submitButton.setVisibility(View.VISIBLE);
                // TODO remove <add later>
                acceptButton.setVisibility(View.GONE);
            }

        }
        if (getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(AllDocumentsFragment.FRAGMENT_TAG)) {
            submitButton.setText(getResources().getString(R.string.document_acceptance_workflow_button_submit));
            submitButton.setVisibility(View.INVISIBLE);
            // set visibility based on user/owner
            boolean showAcceptanceWorkflow = showAcceptanceWorkflow(document);
            if (showAcceptanceWorkflow) {
                if (document.getStatus().equals(Document.STATUS_SUBMITTED)
                        || document.getStatus().equals(Document.STATUS_RESUBMITTED)
                        || document.getStatus().equals(Document.STATUS_PENDING)
                ) {
                    submitButton.setVisibility(View.GONE);
                    // TODO remove <add later>
                    acceptButton.setVisibility(View.VISIBLE);
                }
            } else {
                submitButton.setVisibility(View.INVISIBLE);
                // TODO remove <add later>
                acceptButton.setVisibility(View.GONE);
            }
        }
        if (getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(ProfileDocumentsFragment.FRAGMENT_TAG)) {
            submitButton.setText((getResources().getString(R.string.document_acceptance_workflow_button_submit)));
            submitButton.setVisibility(View.INVISIBLE);
        }

        //
        // Cancel Button Visibility
        if (getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(MyDocumentsFragment.FRAGMENT_TAG)) {
            cancelButton.setText((getResources().getString(R.string.document_acceptance_workflow_button_cancel)));
        }
        if (getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(AllDocumentsFragment.FRAGMENT_TAG)) {
            cancelButton.setText((getResources().getString(R.string.document_acceptance_workflow_button_cancel)));
        }
        if (getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(ProfileDocumentsFragment.FRAGMENT_TAG)) {
            cancelButton.setText((getResources().getString(R.string.document_acceptance_workflow_button_cancel)));
        }

        ///////////////////////////////////////
        // Camera Button Visibility

        //
        // check for edit buttons, only my own docs can be edited.
        if (!globalVariable.getCurrentUser().getName().toLowerCase().equals(globalVariable.getNextDocument().getUsername().toLowerCase())
                || globalVariable.getNextDocumentStackSize() > 1
                || globalVariable.getNextDocument().getStatus().equals(Document.STATUS_SUBMITTED)
                || globalVariable.getNextDocument().getStatus().equals(Document.STATUS_RESUBMITTED)
                || globalVariable.getNextDocument().getStatus().equals(Document.STATUS_PENDING)
                || (globalVariable.getCurrFragment() instanceof MyDocumentsFragment
                && globalVariable.getNextDocument().getStatus().equals(Document.STATUS_ACCEPTED))) {
            // Cannot edit
            fabTakeDocImage.setVisibility(View.INVISIBLE);
            fabAttachDocImage.setVisibility(View.INVISIBLE);
        } else {
            fabTakeDocImage.setVisibility(View.VISIBLE);
            fabAttachDocImage.setVisibility(View.VISIBLE);
        }


        refreshSummaryPageUI();

        // set the progress dialog
        progressBar.setVisibility(View.VISIBLE);

        refreshGalleryPageUI();

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        adapter.notifyDataSetChanged();


        /**
         if(dialogMode == DIALOG_MODE_READ_ONLY){
         fabTakeDocImage.setVisibility(View.INVISIBLE);
         }else{
         fabTakeDocImage.setVisibility(View.VISIBLE);
         }
         */

        // set the progress dialog
        progressBar.setVisibility(View.GONE);
    }

    private void startNoteScanner(Context context) {

        globalVariable.setSmartCameraSync(true);
        // get the directory
        File saveDir = null;
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // Only use external storage directory if permission is granted, otherwise cache directory is used by default
            saveDir = new File(Environment.getExternalStorageDirectory(), "MaterialCamera");
            saveDir.mkdirs();
        }

        // launch the scanner intent
        globalVariable.setCurrDocPagesCameraSnapCount(0);
        Intent intent = new Intent(getContext(), OpenNoteScannerActivity.class);
        startActivityForResult(intent, OpenNoteScannerActivity.FETCH_PAGES_RQ);

    }


    /**
     * Receiving activity result method will be called after closing the camera
     * and will provide the actual captured image
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //
        // Received a result from CV Camera
        if (requestCode == OpenNoteScannerActivity.FETCH_PAGES_RQ) {
            // delegate the processing
            processCameraPageData(resultCode, data);
        }

        //
        // Received a a result form gallery
        if (requestCode == IMG_RESULT_ATTACH) {
            // delegate the processing
            processAttachementPageData(resultCode, data);
        }
    }

    public void setContainerFragment(Fragment containerFragment) {
        this.containerFragment = containerFragment;
    }


    public DocumentPageImageAdapter getPageImageAdapter() {

        return pageImageAdapter;
    }

    public void setPageImageAdapter(DocumentPageImageAdapter pageImageAdapter) {
        this.pageImageAdapter = pageImageAdapter;
    }

    public void deleteImageFromGallery(File file) {
        file.delete();
    }


    public void saveImageToGallery(File file) {

        //
        // create the new prefix
        //String galleryPrefix = globalVariable.getConfigurationData().getGlobalConstantGalleryImageRemovalPrefix()
        //        + globalVariable.getNextDocument().getSyncID();
        Log.d(LOG_TAG, String.format("[BEFORE PROCESSING] Saved to: %s, size: %s",
                file.getAbsolutePath(), FileUtils.fileSize(file)));


        File dir = new Utils(getContext()).getGalleryDirectory();
        //
        // get the prefix and check if the file name has it
        String galleryPrefix = globalVariable.getConfigurationData().getGlobalConstantGalleryImageRemovalPrefix();
        if (!file.getName().contains(galleryPrefix)) {
            //
            // rename the file
            file.renameTo(new File(dir, galleryPrefix + file.getName()));
            file = new File(dir, galleryPrefix + file.getName());

            Log.d(LOG_TAG, String.format("[BEFORE] Saved to: %s, size: %s",
                    file.getAbsolutePath(), FileUtils.fileSize(file)));
            FileUtils.saveBitmapToFile(file);
            Log.d(LOG_TAG, String.format("['AFTER] Saved to: %s, size: %s",
                    file.getAbsolutePath(), FileUtils.fileSize(file)));
        }


        // set the next image
        //
        int totalPagesSoFar = globalVariable.getNextDocument().getImagePages().size() + globalVariable.getNextDocument().getDocumentPages().size();
        GalleryDocumentPage newDoc = new GalleryDocumentPage(file, false, totalPagesSoFar);
        newDoc.setPageNumber(newDoc.getPosition() + 1);
        globalVariable.getNextDocument()
                .getImagePages()
                .add(newDoc);


        /**
         // create a new tab
         Toast.makeText(getContext(), getResources().getString(R.string.document_custom_dialog_new_page_toast_message), Toast.LENGTH_SHORT).show();
         adapter.addFragment(getResources().getString(R.string.document_custom_dialog_tab_page) +  " " + (globalVariable.getNextDocument().getImagePages().size())
         , CustomFragmentDocumentImagePage.createInstance(dataMode, globalVariable.getNextDocument().getImagePages().size()-1, file)
         );
         adapter.notifyDataSetChanged();
         */

         // update the index page
         CustomFragmentDocumentPageIndex documentPageIndex
         = (CustomFragmentDocumentPageIndex) adapter.getFragmentByTitle(getResources().getString(FRAGMENT_TAB_INDEX));

         //
         // check the type of data being pushed
         //documentPageIndex.getGridview().setAdapter(new DocumentPageImageAdapter(getContext(), globalVariable.getNextDocument().getImagePages()));

          //documentPageIndex.setAdapter(documentPageIndex.getNewestPageData(globalVariable), getContext(),3);
        if(documentPageIndex.getGridview() != null) {
             BaseDynamicGridAdapter indexAdapter = (BaseDynamicGridAdapter) documentPageIndex.getGridview().getAdapter();
             indexAdapter.clear();
             indexAdapter.add(documentPageIndex.getNewestPageData(globalVariable));
        }
         // indexAdapter.notifyDataSetChanged();

    }

    public Fragment getParentFragmentCustom() {
        ListIterator<Fragment> fragmentIterator = getFragmentManager().getFragments().listIterator();
        while (fragmentIterator.hasNext()) {
            Fragment fragment = fragmentIterator.next();
            if (fragment instanceof MyDocumentsFragment
                    && (containerFragment instanceof MyDocumentsFragment
                    || globalVariable.getCurrFragment() instanceof MyDocumentsFragment)) {
                return fragment;
            }
            if (fragment instanceof ProfileDocumentsFragment
                    && (containerFragment instanceof ProfileDocumentsFragment
                    || globalVariable.getCurrFragment() instanceof ProfileDocumentsFragment)) {
                return fragment;
            }
            if (fragment instanceof AllDocumentsFragment
                    && (containerFragment instanceof AllDocumentsFragment
                    || globalVariable.getCurrFragment() instanceof AllDocumentsFragment)) {
                return fragment;
            }
        }
        return null;
    }

    public String getParentFragmentCustomType() {

        ListIterator<Fragment> fragmentIterator = getFragmentManager().getFragments().listIterator();
        while (fragmentIterator.hasNext()) {
            Fragment fragment = fragmentIterator.next();
            if (fragment instanceof MyDocumentsFragment
                    && (containerFragment instanceof MyDocumentsFragment
                    || globalVariable.getCurrFragment() instanceof MyDocumentsFragment)) {
                return MyDocumentsFragment.FRAGMENT_TAG;
            }
            if (fragment instanceof ProfileDocumentsFragment
                    && (containerFragment instanceof ProfileDocumentsFragment
                    || globalVariable.getCurrFragment() instanceof ProfileDocumentsFragment)) {
                return ProfileDocumentsFragment.FRAGMENT_TAG;
            }
            if (fragment instanceof AllDocumentsFragment
                    && (containerFragment instanceof AllDocumentsFragment
                    || globalVariable.getCurrFragment() instanceof AllDocumentsFragment)) {
                return AllDocumentsFragment.FRAGMENT_TAG;
            }
        }
        return null;
    }



    private void refreshSummaryPageUI() {
        // set the fragment
        globalVariable.setCurrFragment(getParentFragmentCustom());

        progressBar.getIndeterminateDrawable().setColorFilter(
                ResourcesCompat.getColor(getResources(), R.color.login_hint_highlight, null)
                , android.graphics.PorterDuff.Mode.SRC_IN);
        if (adapter == null) {
            adapter = new CustomPagedDocumentDialogAdapter(getChildFragmentManager());
        }

        // clear the adapter
        adapter.clearPagesByTitle(getResources().getString(FRAGMENT_TAB_SUMMARY));
        adapter.clearPagesByTitle(getResources().getString(FRAGMENT_TAB_INDEX));
        adapter.clearPagesByTitle(getResources().getString(FRAGMENT_TAB_NOTES));


        //if(adapter.getFragmentByTitle(getResources().getString(FRAGMENT_TAB_SUMMARY)) == null) {
        adapter.addFragment(getResources().getString(FRAGMENT_TAB_SUMMARY)
                , CustomFragmentDocumentPageSummary.createInstance(dataMode)
                , 0);
        //}

        adapter.addFragment(getResources().getString(FRAGMENT_TAB_NOTES)
                , CustomFragmentDocumentNotes.createInstance(dataMode)
                , 0);
        //}


        //if(adapter.getFragmentByTitle(getResources().getString(FRAGMENT_TAB_INDEX)) == null) {
        adapter.addFragment(getResources().getString(FRAGMENT_TAB_INDEX)
                , CustomFragmentDocumentPageIndex.createInstance(dataMode, adapter, document)
                , 0);
        //}

    }

    private void refreshGalleryPageUI() {
        // check if this document is cached
        Document newDoc = (Document) globalVariable.getPropertyBag().get(document.getSyncID());
        List<Document> tempDocumentList = new ArrayList<>();
        List<DocumentCardItem> tempDocumentCardsList = new ArrayList<>();
        if (newDoc != null) {
            // convert the document
            //tempDocumentList.add(newDoc);
            //tempDocumentCardsList = DocumentPOJOUtils.convertDocuments(tempDocumentList, globalVariable, containerFragment, false);
            //document.setDocumentPages(tempDocumentCardsList.get(0).getDocumentPages());
            document.setId(newDoc.getId());
        }

        // clear the adapter
        adapter.clearPagesByTitle(getResources().getString(R.string.document_custom_dialog_tab_page));


        //
        // adjust the order of the gallery images
        // <TODO> Need to adjust based on position in the array
        int index = 0;
        List<GalleryDocumentPage> tempDocumentPageList = globalVariable.getNextDocument().getDocumentPages();
        Collections.sort(tempDocumentPageList, new Comparator<GalleryDocumentPage>() {
            public int compare(GalleryDocumentPage p1, GalleryDocumentPage p2) {
                return Integer.compare( ((DocumentPage)p1.getPage()).getPageNumber(), ((DocumentPage)p2.getPage()).getPageNumber());
            }
        });
        document.setDocumentPages(tempDocumentPageList);
        if (tempDocumentPageList.size() > 0) {
            while (index < tempDocumentPageList.size()) {
                //if(adapter.getFragmentByTitle(getResources().getString(R.string.document_custom_dialog_tab_page) + " " + (index + 1)) == null
                //        && adapter.getFragmentByPageHash(tempDocumentPageList.get(index).hashCode()) == null) {
                adapter.addFragment(getResources().getString(R.string.document_custom_dialog_tab_page) + " " + (index + 1)
                        , CustomFragmentDocumentImagePage.createInstance(TabbedDocumentDialog.DATA_MODE_REMOTE, index, null)
                        , tempDocumentPageList.get(index).hashCode()
                );
                System.out.println("--- CREATED REMOTE IMAGE PAGE FRAGMENT <index>" + index);
                //}
                index++;
            }
        }

        List<GalleryDocumentPage> tempDocumentPageImageList = document.getImagePages();
        //Collections.sort(tempDocumentPageImageList, new Comparator<GalleryDocumentPage>() {
        //    public int compare(GalleryDocumentPage f1, GalleryDocumentPage f2) {
        //        return ((File)f1.getPage()).getName().compareTo(((File)f2.getPage()).getName());
        //    }
        //});
        document.setImagePages(tempDocumentPageImageList);
        if (globalVariable.getNextDocument().getImagePages().size() > 0) {
            while (index < (tempDocumentPageImageList.size() + globalVariable.getNextDocument().getDocumentPages().size())) {
                File imageFile = (File)tempDocumentPageImageList.get(index - globalVariable.getNextDocument().getDocumentPages().size()).getPage();
                //if(adapter.getFragmentByTitle(getResources().getString(R.string.document_custom_dialog_tab_page) + " " + (index + 1)) == null
                //        && adapter.getFragmentByPageHash(imageFile.hashCode()) == null) {

                //Add a page
                adapter.addFragment(getResources().getString(R.string.document_custom_dialog_tab_page) + " " + (index + 1)
                        , CustomFragmentDocumentImagePage.createInstance(dataMode, index, imageFile)
                        , imageFile.hashCode()
                );
                System.out.println("--- CREATED LOCAL IMAGE PAGE FRAGMENT <index>" + index);
                //}
                index++;
            }
        }
    }

    private void doOCRExtraction(Bitmap bitmap) {
        try {
            if (detector.isOperational() && bitmap != null) {
                String tagPrefix = DocumentPOJOUtils.getDocumentTagPrefix(globalVariable.getNextDocument());
                if (tagPrefix == null) {
                    // Exit the method as no OCR is needed
                    return;
                }
                // otherwise continue
                //
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> textBlocks = detector.detect(frame);
                // get the OCR Data
                String detectedTag = getTagFromDocument(textBlocks);
                if (detectedTag != null) {
                    // Toast.makeText(getContext(), "Extracted a tag: " + detectedTag, Toast.LENGTH_LONG).show();
                    // create a new tag
                    TagData tag = new TagData();
                    tag.setText(tagPrefix + " " + detectedTag);
                    tag.setCustom(true);
                    tag.setCustomPrefix(tagPrefix);
                    tag.setOrganizationId(globalVariable.getCurrentUser().getUserGroups().get(0).getOrganizationId());
                    // check if this tag is already part of the document
                    //
                    if (!DocumentPOJOUtils.doesTagExistInDoc(globalVariable.getNextDocument(), tag)) {
                        globalVariable.getNextDocument().getTags().add(tag);

                        //
                        // Find all documents linked by the custom tags
                        DocumentPOJOUtils.getLinkedDocsByTags(globalVariable);
                    }
                } else {
                    // Toast.makeText(getContext(), "Unable to extract a tag", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getContext(), "Could not set up the OCR detector!", Toast.LENGTH_SHORT);

            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to load Image", Toast.LENGTH_SHORT)
                    .show();
            Log.e(LOG_TAG, e.toString());
        }
    }

    private void doOCRExtraction2(Bitmap bitmap) {
        try {
            if (detector.isOperational() && bitmap != null) {

                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> textBlocks = detector.detect(frame);
                // get the OCR Data
                String ocrText = getFullOCRText(textBlocks);
                Log.i(LOG_TAG, "OCR Text Extracted " + ocrText);
                //
                // get all dynamic field definitions for OCR for this document
                List<DynamicFieldDefinition> allOcrDefinitions = getAllOCRDynamicFieldsForDocument(globalVariable, globalVariable.getNextDocument());
                //
                // get all the actual fields currently available for this document
                List<DynamicFieldData> currentFieldData = getAllDynamicFieldData(globalVariable.getNextDocument());
                //
                // Process teh OCR data for each OCR field
                for(int i=0; i < allOcrDefinitions.size(); i++ ){
                    // get the OCR text
                    String matchedText;
                    int matchIndex = ocrText.indexOf(allOcrDefinitions.get(i).getOcrMatchText());
                    if(matchIndex != -1){
                        int start = matchIndex + allOcrDefinitions.get(i).getOcrMatchText().length();
                        int end = matchIndex + allOcrDefinitions.get(i).getOcrMatchText().length() + allOcrDefinitions.get(i).getOcrGrabLength();
                        if(start < ocrText.length() && end < ocrText.length()) {
                            // We have matched the text in OCR
                            matchedText = ocrText.substring(start, end);
                            Log.i(LOG_TAG, "OCR Text Matched " + matchedText + " for Key: " + allOcrDefinitions.get(i).getOcrMatchText());
                            //
                            // Add the matched text to the doc field in the document
                            boolean matchFieldFound = false;
                            for(int j=0; j < currentFieldData.size(); j++ ){
                                //
                                // if we find it add to it
                                if(currentFieldData.get(j).getParentResourceId() == allOcrDefinitions.get(i).getId()){
                                    //
                                    // replace it in the document
                                    matchFieldFound = true;
                                    globalVariable.getNextDocument().getDynamicFieldData().get(j).setData(matchedText);
                                }
                            }
                            //
                            // If we did not add it then add it here
                            if(!matchFieldFound){
                                //
                                // add the data
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(allOcrDefinitions.get(i).getId());
                                data.setData(matchedText);
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                                //
                                // relfect the new data in teh list
                                currentFieldData.add(data);
                            }

                        }
                    }
                }
            } else {
                Toast.makeText(getContext(), "Could not set up the OCR detector!", Toast.LENGTH_SHORT);

            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to load Image", Toast.LENGTH_SHORT)
                    .show();
            Log.e(LOG_TAG, e.toString());
        }
    }


    private void doOCRExtraction(String fileName) {

        try {
            Bitmap bitmap = FileUtils.decodeBitmapUri(getContext(), Uri.fromFile(new File(fileName)));
            doOCRExtraction2(bitmap);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to load Image", Toast.LENGTH_SHORT)
                    .show();
            Log.e(LOG_TAG, e.toString());
        }
    }

    /**
     * The handler for accepting a document
     * @param view
     */
    private void acceptButtonClickHandler(View view) {
        // Accept
        globalVariable.getNextDocument().setStatus(Document.STATUS_ACCEPTED);

        // create the URL
        String postSetStatusUrl = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestDocumentSetStatusURL();

        if (RESTUtils.checkConnection() == false) {
            //

        } else {
            if(getParentFragmentCustomType().equals(AllDocumentsFragment.FRAGMENT_TAG)) {
                AllDocumentsFragment parentFragment = (AllDocumentsFragment) getParentFragmentCustom();
                //parentFragment.getAdapter().addDataItem(globalVariable.getNextDocument());
                //parentFragment.getAdapter().notifyDataSetChanged();
                globalVariable.setDocumentAdapter(AllDocumentsFragment.RECYCLER_ADAPTER_KEY, parentFragment.getAdapter());
                globalVariable.setCurrFragment(parentFragment);
            }
            RESTUtils.executePOSTDocumentSetStatusRequest(postSetStatusUrl, null, globalVariable, globalVariable.getNextDocument().getSyncID());
            // Get GPS Data
            setGPSLocation(globalVariable.getNextDocument().getSyncID());
        }
        TabbedDocumentDialog.super.dismiss();

    }

    /**
     * The handler for rejecting a document
     * @param view
     */
    private void rejectButtonClickHandler(View view){



        // dFragment.show(fm, "Dialog Fragment");


        // create the URL
        String postUrl = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestDocumentSetStatusURL();

        if(RESTUtils.checkConnection() == false){
            //
            // Commit the data locally
            Toast.makeText(getContext(), "START Creating local image", Toast.LENGTH_SHORT).show();
            Toast.makeText(getContext(), "DONE Creating local image", Toast.LENGTH_SHORT).show();
        }else{
            if(getParentFragmentCustomType().equals(AllDocumentsFragment.FRAGMENT_TAG)) {
                AllDocumentsFragment parentFragment = (AllDocumentsFragment) getParentFragmentCustom();
                //parentFragment.getAdapter().addDataItem(globalVariable.getNextDocument());
                //parentFragment.getAdapter().notifyDataSetChanged();
                globalVariable.setDocumentAdapter(AllDocumentsFragment.RECYCLER_ADAPTER_KEY, parentFragment.getAdapter());
                globalVariable.setCurrFragment(parentFragment);
            }

            // Open the new dialog and pass to it the global variable
            CreateDocNotesDialog dFragment = new CreateDocNotesDialog();

            AppCompatActivity activity = (AppCompatActivity) getActivity();

            // Show DialogFragment
            dFragment.show(activity.getSupportFragmentManager(),"Dialog Fragment" );

            //RESTUtils.executePOSTDocumentSetStatusRequest(postUrl, null, globalVariable, globalVariable.getNextDocument().getSyncID());
        }

    }


    /**
     * The handler for accepting the done action for a document
     * @param view
     * @param docStatus
     */
    private void doneButtonClickHandler(View view, String docStatus){
        //
        // Check if we are coming back from a pop-up dialog
        if(globalVariable.getNextDocumentStackSize() > 1){
            // handle linking, attaching etc...
            if(globalVariable.getNextDocument().getContext() != null
                    && globalVariable.getNextDocument().getContext().getContextName().equals(DocumentContext.DOCUMENT_LINIKING_ACTIVITY_NAME)){
                DocumentContext context = globalVariable.getNextDocument().getContext();
                // get the current data object from the main adapter
                LinkedDocumentItemDataHelper.LinkedDocumentDataCard pickedItem
                        = (LinkedDocumentItemDataHelper.LinkedDocumentDataCard)context.getDataItem();
                int position = context.getListPosition();
                ShowLinkedDocumentsListRecyclerViewAdapter listAdapter
                        = (ShowLinkedDocumentsListRecyclerViewAdapter) context.getAdapter();
                listAdapter.getDataSet().get(position).setCheckState(true);
                listAdapter.notifyItemChanged(position);
                View viewCard = (View)context.getView();
                CheckBox mCheckBox = viewCard.findViewById(R.id.checkBox);
                mCheckBox.performClick();
            }
            if(globalVariable.getNextDocument().getContext() != null
                    && globalVariable.getNextDocument().getContext().getContextName().equals(DocumentContext.DOCUMENT_ATTACHING_ACTIVITY_NAME)){
                DocumentContext context = globalVariable.getNextDocument().getContext();
                // get the current data object from the main adapter
                AttachedDocumentItemDataHelper.AttachedDocumentDataCard pickedItem
                        = (AttachedDocumentItemDataHelper.AttachedDocumentDataCard)context.getDataItem();
                int position = context.getListPosition();
                ShowAttachedDocumentsListRecyclerViewAdapter listAdapter
                        = (ShowAttachedDocumentsListRecyclerViewAdapter) context.getAdapter();
                listAdapter.getDataSet().get(position).setCheckState(true);
                listAdapter.notifyItemChanged(position);
                View viewCard = (View)context.getView();
                CheckBox mCheckBox = viewCard.findViewById(R.id.checkBox);
                mCheckBox.performClick();
            }
            globalVariable.popNextDocument();
            TabbedDocumentDialog.super.dismiss();
            return;
        }

        boolean newDocument = false;

        // if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_CREATION ) {
        Log.i(LOG_TAG, "The done mode is DIALOG_MODE_WRITE");

        //
        // check if this is a creation
        if(globalVariable.getNextDocument().getSyncID() == null){
            // get the UUID for syncing the IDS
            String syncId = UUID.randomUUID().toString();
            System.out.println("SyncID UUID: " + syncId);
            globalVariable.getNextDocument().setSyncID(syncId);
            newDocument = true;
        }

        String postUrl = null;

        // get the DTO
        Document document = DocumentPOJOUtils.convertDTO(globalVariable.getNextDocument(), globalVariable);
        if(!document.getStatus().equals(docStatus)){
            document.setStatus(docStatus);
        }
        //
        // collate the pages
        // document = collatePages(document);

        //
        // Commit the data to REST Service
        //

        // commit a new document
        //
        if(newDocument) {
            postUrl = globalVariable.getConfigurationData().getServerURL()
                    + globalVariable.getConfigurationData().getApplicationInfixURL()
                    + globalVariable.getConfigurationData().getRestDocumentCreateURL();
            Log.i(LOG_TAG, "REST - create a new document - call: " + postUrl);

            String postBody = RESTUtils.getJSON(document);

            ////////////////////////////////////////////////////////////////////////////
            // save locally first
            //

            // Add the document to the local database
            documentService = new DocumentJSONService(getContext());

            ////////////////////////////////////////////////////////////////////////////
            // Do A remote save
            //

            //
            // Mark the UI view element as being synced
            globalVariable.getNextDocument().setBackendSynced(false);
            Toast.makeText(getContext(), getResources().getString(R.string.document_custom_dialog_done_toast_message), Toast.LENGTH_SHORT).show();

            if(getParentFragmentCustomType().equals(MyDocumentsFragment.FRAGMENT_TAG)){
                MyDocumentsFragment parentFragment = (MyDocumentsFragment) getParentFragmentCustom();
                parentFragment.getAdapter().addDataItem(globalVariable.getNextDocument());
                parentFragment.getAdapter().notifyDataSetChanged();
                globalVariable.setDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY, parentFragment.getAdapter());
                globalVariable.setCurrFragment(parentFragment);
                Log.i(LOG_TAG, "NEW DOCUMENT Call for MyDocs Doc - Updating Adapter");
                if(RESTUtils.checkConnection() == false){
                    //
                    // Commit the data locally
                    Toast.makeText(getContext(), "START Creating local image", Toast.LENGTH_SHORT).show();
                    documentService.open();
                    documentService.createDocument(globalVariable.getNextDocument(), postBody);
                    DocumentPOJOUtils.removeGalleryFiles(globalVariable);
                    DocumentPOJOUtils.postProcessDocumentAddition(globalVariable, null,null,false );
                    documentService.close();
                    Toast.makeText(getContext(), "DONE Creating local image", Toast.LENGTH_SHORT).show();
                }else{
                    RESTUtils.executePOSTRequest(postUrl, postBody, globalVariable, globalVariable.getNextDocument().getSyncID());
                    // Get GPS Data
                    setGPSLocation(globalVariable.getNextDocument().getSyncID());
                }

            }
            if(getParentFragmentCustomType().equals(ProfileDocumentsFragment.FRAGMENT_TAG)){
                ProfileDocumentsFragment parentFragment = (ProfileDocumentsFragment) getParentFragmentCustom();
                parentFragment.getAdapter().addDataItem(globalVariable.getNextDocument());
                parentFragment.getAdapter().notifyDataSetChanged();
                globalVariable.setDocumentAdapter(ProfileDocumentsFragment.RECYCLER_ADAPTER_KEY, parentFragment.getAdapter());
                globalVariable.setCurrFragment(parentFragment);
                Log.i(LOG_TAG, "NEW DOCUMENT Call for MyDocs Doc - Updating Adapter");
                if(RESTUtils.checkConnection() == false){
                    //
                    // Commit the data locally
                    Toast.makeText(getContext(), "START Creating local image", Toast.LENGTH_SHORT).show();
                    documentService.open();
                    documentService.createDocument(globalVariable.getNextDocument(), postBody);
                    DocumentPOJOUtils.removeGalleryFiles(globalVariable);
                    DocumentPOJOUtils.postProcessDocumentAddition(globalVariable, null,null,false );
                    documentService.close();
                    Toast.makeText(getContext(), "DONE Creating local image", Toast.LENGTH_SHORT).show();
                }else{
                    RESTUtils.executePOSTRequest(postUrl, postBody, globalVariable, globalVariable.getNextDocument().getSyncID());
                    // Get GPS Data
                    setGPSLocation(globalVariable.getNextDocument().getSyncID());
                }
            }

            //
            // we are done
            TabbedDocumentDialog.super.dismiss();
            return;

        }
        if(!newDocument ) { //&& (globalVariable.getNextDocument().getStatus().equals(Document.STATUS_SUBMITTED)
                        //        || globalVariable.getNextDocument().getStatus().equals(Document.STATUS_RESUBMITTED))) {
            postUrl = globalVariable.getConfigurationData().getServerURL()
                    + globalVariable.getConfigurationData().getApplicationInfixURL()
                    + globalVariable.getConfigurationData().getRestDocumentUpdateURL();
            Log.i(LOG_TAG, "REST - update an existing document - call: " + postUrl);

            String postBody = RESTUtils.getJSON(document);

            ////////////////////////////////////////////////////////////////////////////
            // save locally first
            //

            // Add the document to the local database
            documentService = new DocumentJSONService(getContext());

            ////////////////////////////////////////////////////////////////////////////
            // Do A remote save
            //

            //
            // Mark the UI view element as being synced
            globalVariable.getNextDocument().setBackendSynced(false);
            Toast.makeText(getContext(), getResources().getString(R.string.document_custom_dialog_submit_toast_message), Toast.LENGTH_SHORT).show();

            if(getParentFragmentCustomType().equals(MyDocumentsFragment.FRAGMENT_TAG)){
                MyDocumentsFragment parentFragment = (MyDocumentsFragment) getParentFragmentCustom();
                parentFragment.getAdapter().replaceCardItem(globalVariable.getNextDocument());
                parentFragment.getAdapter().notifyDataSetChanged();
                globalVariable.setDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY, parentFragment.getAdapter());
                globalVariable.setCurrFragment(parentFragment);
                Log.i(LOG_TAG, "NEW DOCUMENT Call for MyDocs Doc - Updating Adapter");
                if(RESTUtils.checkConnection() == false){
                    //
                    // Commit the data locally
                    Toast.makeText(getContext(), "START Creating local image", Toast.LENGTH_SHORT).show();
                    documentService.open();
                    documentService.createDocument(globalVariable.getNextDocument(), postBody);
                    DocumentPOJOUtils.removeGalleryFiles(globalVariable);
                    DocumentPOJOUtils.postProcessDocumentAddition(globalVariable, null,null,false );
                    documentService.close();
                    Toast.makeText(getContext(), "DONE Creating local image", Toast.LENGTH_SHORT).show();
                }else{
                    RESTUtils.executePOSTDocumentUpdateRequest(postUrl, postBody, globalVariable, globalVariable.getNextDocument().getSyncID());
                    // Get GPS Data
                    // setGPSLocation(globalVariable.getNextDocument().getSyncID());
                }

            }
            if(getParentFragmentCustomType().equals(ProfileDocumentsFragment.FRAGMENT_TAG)){
                ProfileDocumentsFragment parentFragment = (ProfileDocumentsFragment) getParentFragmentCustom();
                parentFragment.getAdapter().replaceCardItem(globalVariable.getNextDocument());
                parentFragment.getAdapter().notifyDataSetChanged();
                globalVariable.setDocumentAdapter(ProfileDocumentsFragment.RECYCLER_ADAPTER_KEY, parentFragment.getAdapter());
                globalVariable.setCurrFragment(parentFragment);
                Log.i(LOG_TAG, "NEW DOCUMENT Call for MyDocs Doc - Updating Adapter");
                if(RESTUtils.checkConnection() == false){
                    //
                    // Commit the data locally
                    Toast.makeText(getContext(), "START Creating local image", Toast.LENGTH_SHORT).show();
                    documentService.open();
                    documentService.createDocument(globalVariable.getNextDocument(), postBody);
                    DocumentPOJOUtils.removeGalleryFiles(globalVariable);
                    DocumentPOJOUtils.postProcessDocumentAddition(globalVariable, null,null,false );
                    documentService.close();
                    Toast.makeText(getContext(), "DONE Creating local image", Toast.LENGTH_SHORT).show();
                }else{
                    RESTUtils.executePOSTDocumentUpdateRequest(postUrl, postBody, globalVariable, globalVariable.getNextDocument().getSyncID());
                    // Get GPS Data
                    // setGPSLocation(globalVariable.getNextDocument().getSyncID());
                }
            }

            //
            // we are done
            TabbedDocumentDialog.super.dismiss();
            return;

        }
        // TODO add edit
        if(globalVariable.getNextDocument().getImagePages().size() > 0) {
            postUrl = globalVariable.getConfigurationData().getServerURL()
                    + globalVariable.getConfigurationData().getApplicationInfixURL()
                    + globalVariable.getConfigurationData().getRestDocumentAddPagesURL()
                    + globalVariable.getNextDocument().getId();
            Log.i(LOG_TAG, "REST - create new document Pages - call: " + postUrl);


            List<DocumentPage> newPages = new ArrayList<DocumentPage>();
            for (int i = 0; i < document.getPages().size(); i++) {
                if (!document.getPages().get(i).isPageSynced()) {
                    newPages.add(document.getPages().get(i));
                }
            }
            String postBody = RESTUtils.getJSON(newPages);
            //
            // Mark the UI view element as being synced
            globalVariable.getNextDocument().setBackendSynced(false);
            //MyDocumentsFragment parentFragment = (MyDocumentsFragment) getParentFragmentCustom();
            RESTUtils.executePOSTDocumentAddPagesRequest(postUrl, postBody, globalVariable, globalVariable.getNextDocument().getSyncID());
            //parentFragment.refreshList();
            //List<Document> docs = new ArrayList<Document>();
            //docs.add(document);
            // parentFragment.getAdapter().replaceCardItem(DocumentPOJOUtils.convertDocuments(docs, globalVariable, null, false).get(0));
            TabbedDocumentDialog.super.dismiss();
            return;

        }
        if(getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(MyDocumentsFragment.FRAGMENT_TAG)){
            MyDocumentsFragment parentFragment = (MyDocumentsFragment) getParentFragmentCustom();
            List<Document> docs = new ArrayList<Document>();
            docs.add(document);
            DocumentCardItem cardItem = DocumentPOJOUtils.convertDocuments(docs, globalVariable, null, false).get(0);
            //int changedIndex = parentFragment.getAdapter().getDataSet().indexOf(cardItem);
            int changedIndex = parentFragment.getAdapter().replaceCardItem(cardItem);
            parentFragment.getAdapter().notifyItemChanged(changedIndex);
            //globalVariable.popNextDocument();
        }
        if(getParentFragmentCustomType() != null && getParentFragmentCustomType().equals(ProfileDocumentsFragment.FRAGMENT_TAG)){
            ProfileDocumentsFragment parentFragment = (ProfileDocumentsFragment) getParentFragmentCustom();
            List<Document> docs = new ArrayList<Document>();
            docs.add(document);
            DocumentCardItem cardItem = DocumentPOJOUtils.convertDocuments(docs, globalVariable, null, false).get(0);
            //int changedIndex = parentFragment.getAdapter().getDataSet().indexOf(cardItem);
            int changedIndex = parentFragment.getAdapter().replaceCardItem(cardItem);
            parentFragment.getAdapter().notifyItemChanged(changedIndex);
            //globalVariable.popNextDocument();
        }

        // globalVariable.setNextDocument(null);
        globalVariable.popNextDocument();
        TabbedDocumentDialog.super.dismiss();
    }

    private boolean showAcceptanceWorkflow(DocumentCardItem document){
        boolean result = false;

        for(int i=0; i< document.getRecipients().size(); i++){
            if(document.getRecipients().get(i).getName().equals(globalVariable.getCurrentUser().getName())){
                result = true;
            }
        }

        return result;
    }

    private void setGPSLocation(final String sessionDocId){
        // Get GPS Data
        if (ActivityCompat.checkSelfPermission(this.getContext()
                , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_ACCESS_COARSE_LOCATION);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // return;
        }

        // fetchLastLocation();


        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        String postUrl = globalVariable.getConfigurationData().getServerURL()
                                + globalVariable.getConfigurationData().getApplicationInfixURL()
                                + globalVariable.getConfigurationData().getRestDocumentSetLocationURL();
                        String postBody = RESTUtils.getJSON("");
                        if (location != null) {
                            // Logic to handle location object
                            Toast.makeText(globalVariable, "GPS Location Data: " + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_LONG).show();
                            RESTUtils.executePOSTDocumentSetLocationRequest(postUrl, postBody, globalVariable, sessionDocId, location.getLatitude() + "," + location.getLongitude());
                        }
                    }
                });

    }

    public String getPath(Uri uri) {
        String result = null;

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
        getActivity().startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        result =  cursor.getString(column_index);
        return result;
    }



    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        String galleryPrefix = globalVariable.getConfigurationData().getGlobalConstantGalleryImageRemovalPrefix();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 65, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, galleryPrefix + Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(getContext(),
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved to::---&gt;" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    @Override
    public void onFinishEditDialog(NoteData note){
        // create the URL
        String postUrl = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestDocumentAddNotesURL();
        Log.i(LOG_TAG, "REST - create new doc notes - call: " + postUrl);



        if(note != null){
            // Reject
            globalVariable.getNextDocument().setStatus(Document.STATUS_REJECTED);
            if(RESTUtils.checkConnection() == false){
                //
                // Commit the data locally
                Toast.makeText(getContext(), "START Creating local image", Toast.LENGTH_SHORT).show();
                Toast.makeText(getContext(), "DONE Creating local image", Toast.LENGTH_SHORT).show();
            }else{
                if(getParentFragmentCustomType().equals(AllDocumentsFragment.FRAGMENT_TAG)) {
                    AllDocumentsFragment parentFragment = (AllDocumentsFragment) getParentFragmentCustom();
                    //parentFragment.getAdapter().addDataItem(globalVariable.getNextDocument());
                    //parentFragment.getAdapter().notifyDataSetChanged();
                    globalVariable.setDocumentAdapter(AllDocumentsFragment.RECYCLER_ADAPTER_KEY, parentFragment.getAdapter());
                    globalVariable.setCurrFragment(parentFragment);
                }

                AppCompatActivity activity = (AppCompatActivity) getActivity();
                String postBody = RESTUtils.getJSON(note);

                RESTUtils.executePOSTNoteCreationRequest(postUrl, postBody, null, "", globalVariable, DocumentTaggingActivity.PROPERTY_BAG_KEY, activity);
                TabbedDocumentDialog.super.dismiss();
               //RESTUtils.executePOSTDocumentSetStatusRequest(postUrl, null, globalVariable, globalVariable.getNextDocument().getSyncID());
            }

        }else{
            TabbedDocumentDialog.super.dismiss();
        }

    }

    private void fetchLastLocation(){
        Task<Location> task = fusedLocationClient.getLastLocation();

        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    Toast.makeText(globalVariable,currentLocation.getLatitude()+" "+currentLocation.getLongitude(),Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(globalVariable,"No Location recorded",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Document collatePages(Document doc){
        for(int i=0; i< doc.getPages().size(); i++){
            doc.getPages().get(i).setPageNumber(i+1);
        }

        return doc;
    }

    private void attachDocumentPage() {
        // Get the snapshot here or camera data

        // TODO add settings for different cameras later, current setting calls scanner
        // snapCameraPhoto(getContext());

        // start gellery
        //Intent intent = new Intent(Intent.ACTION_PICK,
        //android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Document Image"), IMG_RESULT_ATTACH);

        //startActivityForResult(intent, IMG_RESULT_ATTACH);

        globalVariable.setCurrFragment(getParentFragmentCustom());
    }

    /**
     * Process the data coming from CV Camera
     * @param resultCode - the activity result code
     * @param data - any additional data from the Intent
     */
    private void processCameraPageData(int resultCode, Intent data){
        if (resultCode == android.app.Activity.RESULT_OK) {
            //
            // remove any cached data
            // globalVariable.getNextDocument().getImagePages().clear();
            ImageUtils.removeCachedScannerImages(globalVariable);
            if (globalVariable.isSmartCameraSync()) {
                // check for gallery data transfer
                ArrayList<String> galleryFiles = new ArrayList<>();
                //
                // Sort By
                galleryFiles = new Utils(getContext()).getFilePaths(null);
                for (int i = 0; i < galleryFiles.size(); i++) {
                    //
                    // Check for duplicated
                    if(DocumentPOJOUtils.doesImagePageExistInCurrentDocument(globalVariable, new File(galleryFiles.get(i)))){
                        // skip this
                        continue;
                    }

                    // TODO OCR Extraction
                    //if (i == 0 && globalVariable.getNextDocument().getSyncID() == null) {
                        // extract the ocr from the first image
                        doOCRExtraction(galleryFiles.get(i));
                    //}
                    saveImageToGallery(new File(galleryFiles.get(i)));
                }
                globalVariable.setSmartCameraSync(false);
            }
            //

            // refreshUI();
            refreshSummaryPageUI();
            refreshGalleryPageUI();
            adapter.notifyDataSetChanged();

        } else {
            // There was an error getting the camera data
        }
    }

    /**
     * Process the data coming from CV Camera
     * @param resultCode - the activity result code
     * @param data - any additional data from the Intent
     */
    private void processAttachementPageData(int resultCode, Intent data){
        if (resultCode == android.app.Activity.RESULT_OK ) {
            // Did we get any data?
            if (null != data) {

                String imageEncoded;
                List<String> imagesEncodedList;

                // Get the Image from data

                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                imagesEncodedList = new ArrayList<String>();
                if (data.getData() != null) {

                    Uri mImageUri = data.getData();
                    String wholeID = DocumentsContract.getDocumentId(mImageUri);
                    // Split at colon, use second item in the array
                    String id = wholeID.split(":")[1];
                    String[] column = {MediaStore.Images.Media.DATA};

                    // where id is equal to
                    String sel = MediaStore.Images.Media._ID + "=?";

                    // Get the cursor
                    //Cursor cursor = getActivity().getContentResolver().query(mImageUri,
                    //        filePathColumn, null, null, null);

                    Cursor cursor = getActivity().getContentResolver().
                            query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    column, sel, new String[]{id}, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(column[0]);
                    imageEncoded = cursor.getString(columnIndex);
                    imagesEncodedList.add(imageEncoded);

                    ////
                    InputStream inputStream = null;
                    try {
                        inputStream = getContext().getContentResolver().openInputStream(data.getData());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                    Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);
                    ////
                    if (getContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.v("Permissions","Permission is granted");
                        //Toast.makeText(getContext(), "Permission is granted", Toast.LENGTH_SHORT).show();
                    }else{
                        Log.v("Permissions","Permission is *NOT* granted");
                        //Toast.makeText(getContext(), "Permission is *NOT* granted", Toast.LENGTH_SHORT).show();
                    }
                    //String savedFileLocation = ImageUtils.saveImage(BitmapFactory.decodeFile(imageEncoded)
                    String savedFileLocation = ImageUtils.saveImage(bmp
                            , globalVariable
                            , getContext());
                    // saveImageToGallery(new File(imageEncoded));
                    doOCRExtraction(savedFileLocation);
                    saveImageToGallery(new File(savedFileLocation));
                    cursor.close();

                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            String wholeID = DocumentsContract.getDocumentId(uri);
                            // Split at colon, use second item in the array
                            String id = wholeID.split(":")[1];
                            String[] column = {MediaStore.Images.Media.DATA};

                            // where id is equal to
                            String sel = MediaStore.Images.Media._ID + "=?";

                            mArrayUri.add(uri);
                            // Get the cursor
                            //Cursor cursor = getActivity().getContentResolver().query(uri, filePathColumn, null, null, null);

                            Cursor cursor = getActivity().getContentResolver().
                                    query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            column, sel, new String[]{id}, null);
                            // Move to first row
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncoded);
                            // save it
                            String savedFileLocation = ImageUtils.saveImage(BitmapFactory.decodeFile(imageEncoded)
                                    , globalVariable
                                    , getContext());
                            // saveImageToGallery(new File(imageEncoded));
                            doOCRExtraction(savedFileLocation);
                            saveImageToGallery(new File(savedFileLocation));
                            cursor.close();
                        }
                        Log.v("LOG_TAG", "Selected Images " + mArrayUri.size());
                    }
                }
                Log.v("LOG_TAG", "Selected Images converted " + imagesEncodedList.size());
                // refreshUI();
                refreshSummaryPageUI();
                refreshGalleryPageUI();
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } // End of Result OK
    }


    void recollateDocumentPages(){
        List<GalleryDocumentPage> docPages = DocumentPOJOUtils.getNewestPageData(globalVariable);

/*
        for(int index=0; index< docPages.size(); index++){
            GalleryDocumentPage page = docPages.get(index);

            // set the inner page
            Object innerPage = ((GalleryDocumentPage) page.getPage()).getPage();
            if(  innerPage instanceof DocumentPage) {
                for (int i = 0; i < globalVariable.getNextDocument().getDocumentPages().size(); i++) {
                    if (((DocumentPage) globalVariable.getNextDocument().getDocumentPages().get(i).getPage()).getId()
                            == ((DocumentPage) innerPage).getId()) {
                        globalVariable.getNextDocument().getDocumentPages().get(i).setPageNumber(((DocumentPage) innerPage).getPageNumber());
                        continue;
                    }
                }
            }
            if(innerPage instanceof File) {
                for (int i = 0; i < globalVariable.getNextDocument().getImagePages().size(); i++) {
                    if (globalVariable.getNextDocument().getImagePages().get(i).getPage().equals(innerPage)) {
                        globalVariable.getNextDocument().getImagePages().get(i).setPageNumber(((GalleryDocumentPage) page.getPage()).getPageNumber());
                        continue;
                    }
                }
            }
        }
        */

    }
}
