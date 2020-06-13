package com.wwf.shrimp.application.client.android.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wwf.shrimp.application.client.android.DocumentTaggingActivity;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.models.dto.TagData;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This is a dialog for the creation of a new custom tag.
 * @author AleaActaEst
 */
public class CreateNewTagDialog extends DialogFragment {
    private static final String LOG_TAG = "Tag Creation Dialog";

    Spinner tagPrefixSpinner;
    EditText tagNewText;
    SessionData globalVariable;


    public CreateNewTagDialog(){
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.custom_create_tag_dialog, container,
                false);

        getDialog().setTitle(getResources().getString(R.string.document_tag_dialog_label_new_tag));
        getDialog().getWindow().setLayout(600, 400);

        // set the session data
        globalVariable = (SessionData)getContext().getApplicationContext();

        //
        // Get the input data elements
        tagPrefixSpinner = (Spinner) rootView.findViewById(R.id.spinnerTagPrefixes);
        tagNewText = (EditText) rootView.findViewById(R.id.textViewNewCustomTag);

        //
        // Cancelling the Tag creation
        Button cancelButton = (Button)rootView.findViewById(R.id.buttonDocumentDialogCancel);
        cancelButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                CreateNewTagDialog.super.dismiss();
            }
        });

        //
        // creating a new Tag
        Button saveButton = (Button)rootView.findViewById(R.id.buttonDocumentDialogDone);
        saveButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //
                // read the data from the input
                // use globalVariable.getConfigurationData().getGlobalConstantTaggingCustomPrefix() to get the global prefix
                TagData newTag = new TagData();
                String newTagText = tagNewText.getText().toString();
                String tagPrefix = tagPrefixSpinner.getSelectedItem().toString();
                if(tagPrefix.equals(getString(R.string.tag_prefix_empty))){
                    newTag.setCustom(false);
                }else{
                    newTag.setCustom(true);
                    newTag.setCustomPrefix(tagPrefix);
                }
                newTag.setText(newTagText);
                // set the organization
                newTag.setOrganizationId(globalVariable.getCurrentUser().getUserGroups().get(0).getOrganizationId());

                //
                // Prepare the POST rest request to create a new tag and get the id of the tag
                createNewTag(newTag);



                CreateNewTagDialog.super.dismiss();
            }
        });



        // Do something else
        return rootView;
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
    protected void createNewTag(TagData newTag) {
        /**
         * get data from the server about tags
         */
        String url = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestTagCreateURL();
        Log.i(LOG_TAG, "REST - create new tag - call: " + url);

        String postBody = RESTUtils.getJSON(newTag);

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        RESTUtils.executePOSTTagCreationRequest(url, postBody, null, "", globalVariable, DocumentTaggingActivity.PROPERTY_BAG_KEY, activity);
        Log.e(LOG_TAG, "REST - create new tag - call success: " + globalVariable.getPropertyBag().get("newTagText"));

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
}
