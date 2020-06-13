package com.wwf.shrimp.application.client.android.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wwf.shrimp.application.client.android.DocumentTaggingActivity;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.models.dto.NoteData;
import com.wwf.shrimp.application.client.android.models.dto.TagData;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils;
import com.wwf.shrimp.application.client.android.utils.MappingUtilities;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * This is a dialog for the creation of a new custom tag.
 * @author AleaActaEst
 */
public class CreateDocNotesDialog extends DialogFragment {
    private static final String LOG_TAG = "Doc Notes Create Dialog";

    EditText tagNewHeader;
    EditText tagNewNotes;
    SessionData globalVariable;


    public CreateDocNotesDialog(){
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("My Dialog Title");

        View rootView = inflater.inflate(R.layout.custom_create_doc_notes_dialog, container,
                false);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = FrameLayout.LayoutParams.MATCH_PARENT;
        params.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        //getDialog().setTitle(getResources().getString(R.string.document_notes_dialog_label_enter_doc_notes));
        //getDialog().getWindow().setLayout(400, 300);

        // set the session data
        globalVariable = (SessionData)getContext().getApplicationContext();

        //
        // Get the input data elements
        tagNewHeader = (EditText) view.findViewById(R.id.textViewDocumentNotesHeader);
        tagNewNotes = (EditText) view.findViewById(R.id.textViewNotesDetails);



        //
        // Cancelling the Tag creation
        Button cancelButton = view.findViewById(R.id.buttonDocumentDialogCancel);
        cancelButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                TabbedDocumentDialog listener = MappingUtilities.getTabbedDialog(getFragmentManager().getFragments());
                listener.onFinishEditDialog(null);
                CreateDocNotesDialog.super.dismiss();
            }
        });

        //
        // creating a new doc note
        Button saveButton = (Button)view.findViewById(R.id.buttonDocumentDialogDone);
        saveButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //
                // read the data from the input
                // use globalVariable.getConfigurationData().getGlobalConstantTaggingCustomPrefix() to get the global prefix

                NoteData newNote = new NoteData();
                String header = tagNewHeader.getText().toString();
                String note = tagNewNotes.getText().toString();
                newNote.setNote(header
                        + DocumentPOJOUtils.DOC_NOTE_HEADER_DELIMITER
                        + note
                );
                newNote.setOwner(globalVariable.getCurrentUser().getName());

                TabbedDocumentDialog listener = MappingUtilities.getTabbedDialog(getFragmentManager().getFragments());
                listener.onFinishEditDialog(newNote);
                CreateDocNotesDialog.super.dismiss();

            }
        });


    }



    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

    /**
     * Initialize the UI data for a list of all tags
     */
    protected void createNewDocumentNote(NoteData newNote) {
        /**
         * get data from the server about tags
         */
        String url = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestDocumentAddNotesURL();
        Log.i(LOG_TAG, "REST - create new doc notes - call: " + url);

        String postBody = RESTUtils.getJSON(newNote);

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        RESTUtils.executePOSTNoteCreationRequest(url, postBody, null, "", globalVariable, DocumentTaggingActivity.PROPERTY_BAG_KEY, activity);

        // mDataArray = TagItemDataHelper.getAlphabetData();
    }

    /**
     * Inner class to handle the back-end communication to get the data from the db
     */
    public class OkHttpTagDataHandler extends AsyncTask<String, String, String> {

        OkHttpClient client = new OkHttpClient();
        boolean serverDownFlag = false;
        String requestURL;

        @Override
        protected String doInBackground(String... params) {

            Request request = new Request.Builder()
                    .url(params[0])
                    .addHeader("user-name", globalVariable.getCurrentUser().getName())
                    .build();

            requestURL = request.url().toString();
            Log.d(LOG_TAG, "OkHTTP Request: " + request.url());

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
                serverDownFlag = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            /**
             * Extract the data for the creation of the tag
             */

            Log.i(LOG_TAG, "REST - getting response for URL: " + requestURL);

            // Holder for the new tag data
            TagData newTag;

            //
            // GSON parser
            Gson gson = new GsonBuilder()
                    .create();

            //
            // parse the JSON input into the specific class

            ///////////////////////////////////////////////////////////////////////////////////////
            // Getting Tags form the backend
            //
            if (requestURL.contains(globalVariable.getConfigurationData().getRestTagCreateURL())) {
                newTag = gson.fromJson(s, TagData.class);
                Log.d(LOG_TAG, "REST - create new tag - result from remote server: " + newTag);

                // updateUI();
            }

        }
    }

    interface DialogListener {
        public void onFinishEditDialog(NoteData note);
    }
}

