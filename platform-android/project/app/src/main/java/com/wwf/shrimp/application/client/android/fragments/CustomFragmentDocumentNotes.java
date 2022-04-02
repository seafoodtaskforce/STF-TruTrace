package com.wwf.shrimp.application.client.android.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.dialogs.TabbedDocumentDialog;
import com.wwf.shrimp.application.client.android.models.dto.NoteData;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.DateUtils;
import com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils;

import java.util.List;

/**
 * Document Page Summary fragment which will show the document note details.
 * @author AleaActaEst
 */
public class CustomFragmentDocumentNotes extends Fragment {
    private int dataMode = TabbedDocumentDialog.DATA_MODE_LOCAL;
    // global session data
    private SessionData globalVariable;
    // view data
    TextView textViewGroupTypeName;
    TextView textViewDocumentStatus;
    TextView textViewUserName;
    TextView textViewDocumentType;
    TextView textViewTimestamp;
    TextView textViewNumberOfPages;
    TextView textViewDocumentNotesHeader;
    TextView textViewNotesDetails;
    View viewColoredBar;

    public static CustomFragmentDocumentNotes createInstance(int dataMode)
    {
        CustomFragmentDocumentNotes fragment = new CustomFragmentDocumentNotes();
        fragment.dataMode = dataMode;
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_document_dialog_fragment_notes,container,false);




        // fetch data definitions
        this.textViewGroupTypeName = (TextView) view.findViewById(R.id.textViewGroupTypeName);

        this.textViewDocumentType = (TextView) view.findViewById(R.id.textViewDocumentType);
        this.textViewUserName = (TextView) view.findViewById(R.id.textViewUsername);
        this.textViewTimestamp = (TextView) view.findViewById(R.id.textViewTimestamp);
        this.textViewNumberOfPages = (TextView) view.findViewById(R.id.textViewNumberOfPages);
        this.viewColoredBar = view.findViewById(R.id.colored_bar);
        this.textViewDocumentNotesHeader =(TextView) view.findViewById(R.id.textViewDocumentNotesHeader);
        this.textViewNotesDetails =(TextView) view.findViewById(R.id.textViewNotesDetails);

        this.textViewDocumentStatus = (TextView) view.findViewById(R.id.textViewDocumentStatus);

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
        // get the notes
        List<NoteData> documentNotes = globalVariable.getNextDocument().getNotes();
        textViewDocumentNotesHeader.setText(DocumentPOJOUtils.getDocumentNotesHeader(documentNotes));
        textViewNotesDetails.setText(DocumentPOJOUtils.getDocumentNotes(documentNotes));

    }
}
