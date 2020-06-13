package com.wwf.shrimp.application.client.android.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wwf.shrimp.application.client.android.DocumentAttachingActivity;
import com.wwf.shrimp.application.client.android.DocumentLinkingActivity;
import com.wwf.shrimp.application.client.android.DocumentRecipientsActivity;
import com.wwf.shrimp.application.client.android.DocumentTaggingActivity;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.dialogs.TabbedDocumentDialog;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.DateUtils;
import com.wwf.shrimp.application.client.android.DocumentDynamicFieldsActivity;
import com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils;

/**
 * Document Page Summary fragment which will show the document details.
 * @author AleaActaEst
 */
public class CustomFragmentDocumentPageSummary extends Fragment {
    private int dataMode = TabbedDocumentDialog.DATA_MODE_LOCAL;
    // global session data
    private SessionData globalVariable;

    //
    // view data

    //
    // Summary Card
    TextView textViewGroupTypeName;
    TextView textViewDocumentStatus;
    TextView textViewUserName;
    TextView textViewDocumentType;
    TextView textViewTimestamp;
    TextView textViewNumberOfPages;

    //
    // Tags Card
    TextView textViewTagListing;

    //
    // Recipient Card
    TextView textViewRecipientsListing;

    //
    // Linked Docs Card
    TextView textViewDocumentLinksListing;

    //
    // Backup Docs Card
    TextView textViewDocumentAttachmentsListing;

    //
    // Dynamic Fields Card
    TextView textViewDocInfoListing;

    View viewColoredBar;
    // Edit buttons
    ImageView imageViewEditRecipients;
    ImageView imageViewEditTags;
    ImageView imageViewEditLinkedDocs;
    ImageView imageViewEditAttachedDocs;
    ImageView imageViewEditDynamicFields;

    public static CustomFragmentDocumentPageSummary createInstance(int dataMode)
    {
        CustomFragmentDocumentPageSummary fragment = new CustomFragmentDocumentPageSummary();
        fragment.dataMode = dataMode;
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_document_dialog_fragment_summary,container,false);




        // fetch data definitions
        this.textViewGroupTypeName = (TextView) view.findViewById(R.id.textViewGroupTypeName);

        this.textViewDocumentType = (TextView) view.findViewById(R.id.textViewDocumentType);
        this.textViewUserName = (TextView) view.findViewById(R.id.textViewUsername);
        this.textViewTimestamp = (TextView) view.findViewById(R.id.textViewTimestamp);
        this.textViewNumberOfPages = (TextView) view.findViewById(R.id.textViewNumberOfPages);
        this.viewColoredBar = view.findViewById(R.id.colored_bar);
        this.textViewTagListing =(TextView) view.findViewById(R.id.textViewTagListing);
        this.textViewRecipientsListing =(TextView) view.findViewById(R.id.textViewRecipientsListing);
        this.textViewDocInfoListing =(TextView) view.findViewById(R.id.textViewDocInfoListing);

        this.textViewDocumentLinksListing =(TextView) view.findViewById(R.id.textViewDocumentLinksListing);
        this.textViewDocumentAttachmentsListing =(TextView) view.findViewById(R.id.textViewDocumentAttachmentsListing);
        this.textViewDocumentStatus = (TextView) view.findViewById(R.id.textViewDocumentStatus);

        // edit buttons
        this.imageViewEditRecipients =(ImageView) view.findViewById(R.id.imageViewEditRecipients);
        this.imageViewEditTags =(ImageView) view.findViewById(R.id.imageViewEditTags);
        this.imageViewEditLinkedDocs =(ImageView) view.findViewById(R.id.imageViewEditLinkedDocs);
        this.imageViewEditAttachedDocs =(ImageView) view.findViewById(R.id.imageViewEditAttachedDocs);
        this.imageViewEditDynamicFields =(ImageView) view.findViewById(R.id.imageViewEditDynamicFields);


        //
        // Attach edit handlers
        //
        imageViewEditRecipients.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (v.equals(imageViewEditRecipients)) {
                    // Write your awesome code here

                    // start the edit activity
                    Intent intent = new Intent(getActivity(), DocumentRecipientsActivity.class);
                    startActivity(intent);

                }
            }
        });
        imageViewEditTags.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (v.equals(imageViewEditTags)) {
                    Intent intent = new Intent(getActivity(), DocumentTaggingActivity.class);
                    startActivity(intent);
                }
            }
        });
        imageViewEditLinkedDocs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (v.equals(imageViewEditLinkedDocs)) {
                    Intent intent = new Intent(getActivity(), DocumentLinkingActivity.class);
                    startActivity(intent);
                }
            }
        });
        imageViewEditAttachedDocs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (v.equals(imageViewEditAttachedDocs)) {
                    Intent intent = new Intent(getActivity(), DocumentAttachingActivity.class);
                    startActivity(intent);
                }
            }
        });

        imageViewEditDynamicFields.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (v.equals(imageViewEditDynamicFields)) {
                    Intent intent = new Intent(getActivity(), DocumentDynamicFieldsActivity.class);
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    @Override
    public void onResume()
    {   super.onResume();
        //Refresh your stuff here
        refreshUI();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // create access to session
        globalVariable  = (SessionData) context.getApplicationContext();
    }


    /**
     * Get the number of pages that the current document has
     * @return - the number of pages
     */
    private int getNumberOfPages(){
        int result=0;
        result += globalVariable.getNextDocument().getImagePages().size();
        result += globalVariable.getNextDocument().getDocumentPages().size();
        return result;
    }

    private void refreshUI(){
        // fill in data for the document details
        //
        // long colorIndex = globalVariable.getNextDocument().getDocumentType().getId();
        // int colorId = MappingUtilities.documentTypeIDToRColorMap.get(colorIndex);
        // viewColoredBar.setBackgroundColor(ContextCompat.getColor(getContext(), colorId));
        viewColoredBar.setBackgroundColor(Color.parseColor(globalVariable.getNextDocument().getTypeHEXColor()));
        textViewGroupTypeName.setText(globalVariable.getNextDocument().getGroupType());
        int stringNumPagesResource = getContext().getResources().getIdentifier("document_custom_dialog_summary_number_of_pages"
                , "string"
                , getContext().getPackageName());
        String formattedNumPages = String.format("%1s %2s", getContext().getResources().getString(stringNumPagesResource),getNumberOfPages());
        textViewNumberOfPages.setText(formattedNumPages);
        int stringDocTypeResource = getContext().getResources().getIdentifier(globalVariable.getNextDocument().getDocumentType().getName()
                , "string"
                , getContext().getPackageName());

        String documentType = globalVariable.getInternationalizedResourceString(globalVariable.getNextDocument().getDocumentType().getName());
        textViewDocumentType.setText(documentType);
        textViewUserName.setText(globalVariable.getNextDocument().getUsername());
        textViewTimestamp.setText(DateUtils.formatDateTimeToString(globalVariable.getNextDocument().getCreationTimestamp()));
        // status
        // textViewDocumentStatus.setText(globalVariable.getNextDocument().getStatus());
        textViewDocumentStatus.setText(
                DocumentPOJOUtils.getTranslatedStatus(globalVariable, globalVariable.getNextDocument().getStatus())
        );

        //
        // check for edit buttons, only my own docs can be edited.
        if(!globalVariable.getCurrentUser().getName().equals(globalVariable.getNextDocument().getUsername())
                || globalVariable.getNextDocumentStackSize() > 1
                || globalVariable.getNextDocument().getStatus().equals(Document.STATUS_SUBMITTED)
                || globalVariable.getNextDocument().getStatus().equals(Document.STATUS_RESUBMITTED)
                || globalVariable.getNextDocument().getStatus().equals(Document.STATUS_PENDING)
                || (globalVariable.getCurrFragment() instanceof MyDocumentsFragment
                        && globalVariable.getNextDocument().getStatus().equals(Document.STATUS_ACCEPTED))){
            // Cannot edit
            this.imageViewEditRecipients.setVisibility(View.INVISIBLE);
            this.imageViewEditTags.setVisibility(View.INVISIBLE);
            this.imageViewEditLinkedDocs.setVisibility(View.INVISIBLE);
            this.imageViewEditAttachedDocs.setVisibility(View.INVISIBLE);
            this.imageViewEditDynamicFields.setVisibility(View.INVISIBLE);
        }


        //
        // get the recipients
        String recipients="";
        for(int i=0; i<globalVariable.getNextDocument().getRecipients().size(); i++){
            if(globalVariable.getNextDocument().getRecipients().get(i) != null) {
                recipients += ", " + globalVariable.getNextDocument().getRecipients().get(i).getName();
            }
        }
        if(recipients.length() > 0){
            recipients = recipients.substring(2);
        }
        textViewRecipientsListing.setText(recipients);

        //
        // get the tags
        String tags="";
        for(int i=0; i<globalVariable.getNextDocument().getTags().size(); i++){
            tags += ", " + globalVariable.getNextDocument().getTags().get(i).getText();
        }
        if(tags.length() > 0){
            tags = tags.substring(2);
        }
        textViewTagListing.setText(tags);

        //
        // get the linked documents
        String linkedDocuments="";
        for(int i=0; i<globalVariable.getNextDocument().getLinkedDocuments().size(); i++){
            // get the doc type i18n
            documentType = globalVariable.getInternationalizedResourceString( globalVariable.getNextDocument().getLinkedDocuments().get(i).getType().getName());

            //stringDocTypeResource = getContext().getResources().getIdentifier(
            //        globalVariable.getNextDocument().getLinkedDocuments().get(i).getType().getName()
            //       , "string"
            //        , getContext().getPackageName());

            //linkedDocuments += ", "
            //        + getContext().getResources().getString(stringDocTypeResource)
            //        + " - "
            //        + globalVariable.getNextDocument().getLinkedDocuments().get(i).getOwner()
            //;

            linkedDocuments += ", "
                    + documentType
                    + " - "
                    + globalVariable.getNextDocument().getLinkedDocuments().get(i).getOwner()
            ;
        }
        if(linkedDocuments.length() > 0){
            linkedDocuments = linkedDocuments.substring(2);
        }
        textViewDocumentLinksListing.setText(linkedDocuments);

        //
        // get the attached documents
        String attachedDocuments="";
        for(int i=0; i<globalVariable.getNextDocument().getAttachedDocuments().size(); i++){
            // get the doc type i18n
            documentType = globalVariable.getInternationalizedResourceString(globalVariable.getNextDocument().getAttachedDocuments().get(i).getType().getName());

            //stringDocTypeResource = getContext().getResources().getIdentifier(
            //        globalVariable.getNextDocument().getAttachedDocuments().get(i).getType().getName()
            //        , "string"
            //        , getContext().getPackageName());

            // attachedDocuments += ", " + getContext().getResources().getString(stringDocTypeResource);
            attachedDocuments += ", " + documentType;
        }
        if(attachedDocuments.length() > 0){
            attachedDocuments = attachedDocuments.substring(2);
        }
        textViewDocumentAttachmentsListing.setText(attachedDocuments);

        //
        // get the doc info
        String dynamicFieldData="";
        for(int i=0; i<globalVariable.getNextDocument().getDynamicFieldData().size(); i++){
            // get the doc type i18n
             documentType = globalVariable.getNextDocument().getDynamicFieldData().get(i).getData();

            //stringDocTypeResource = getContext().getResources().getIdentifier(
            //        globalVariable.getNextDocument().getAttachedDocuments().get(i).getType().getName()
            //        , "string"
            //        , getContext().getPackageName());

            // attachedDocuments += ", " + getContext().getResources().getString(stringDocTypeResource);
            dynamicFieldData += ", " + documentType;
        }
        if(dynamicFieldData.length() > 0){
            dynamicFieldData = dynamicFieldData.substring(2);
        }
        textViewDocInfoListing.setText(dynamicFieldData);
    }
}
