package com.wwf.shrimp.application.client.android.dialogs;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.models.dto.PasswordCredentials;
import com.wwf.shrimp.application.client.android.models.dto.User;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;

/**
 * Dialog for Password management
 */
public class EditProfilePasswordDialog extends DialogFragment {

    // logging tag
    private static final String LOG_TAG = "EditPassword Dialog";

    SessionData globalVariable;
    EditText editTextPassword;
    EditText editTextPassword2;

    public EditProfilePasswordDialog(){
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_edit_password, container,
                false);

        getDialog().setTitle(getResources().getString(R.string.profile_update_password_text_header));

        // set the session data
        globalVariable = (SessionData)getContext().getApplicationContext();

        //
        // Get the input data elements
        editTextPassword = rootView.findViewById(R.id.editTextPassword);
        editTextPassword2 = rootView.findViewById(R.id.editTextPassword2);

        //
        // Cancelling the password edit
        Button cancelButton = (Button)rootView.findViewById(R.id.buttonPasswordEditCancel);
        cancelButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                EditProfilePasswordDialog.super.dismiss();
            }
        });

        //
        // creating a new Tag
        Button saveButton = (Button)rootView.findViewById(R.id.buttonPasswordEditSave);
        saveButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //
                // read the data from the input
                // use globalVariable.getConfigurationData().getGlobalConstantTaggingCustomPrefix() to get the global prefix
                User newUser;
                PasswordCredentials editedCredentials = new PasswordCredentials();
                editedCredentials.setPassword(editTextPassword.getText().toString());
                editedCredentials.setUsername(globalVariable.getCurrentUser().getCredentials().getUsername());
                newUser = globalVariable.getCurrentUser();
                newUser.setCredentials(editedCredentials);

                //
                // Prepare the POST rest request to create a new tag and get the id of the tag
                if(!editTextPassword.getText().toString().equals(editTextPassword2.getText().toString())){
                    Toast.makeText(getContext(), getResources().getString(R.string.profile_update_error_passwords_do_not_match), Toast.LENGTH_SHORT).show();
                }else if(editTextPassword.getText().toString().isEmpty()){
                    Toast.makeText(getContext(), getResources().getString(R.string.profile_update_error_passwords_cannot_be_empty), Toast.LENGTH_SHORT).show();
                }else{
                    sendPasswordChange(newUser);
                    EditProfilePasswordDialog.super.dismiss();
                }
            }
        });

        // Do something else
        return rootView;

    }

    /**
     * Initialize the UI data for a list of all tags
     */
    protected void sendPasswordChange(User editedUser) {
        /**
         * get data from the server about tags
         */
        String url = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestUserUpdateCredentials();
        Log.i(LOG_TAG, "REST - create new tag - call: " + url);

        String postBody = RESTUtils.getJSON(((PasswordCredentials)editedUser.getCredentials()));
        Log.e(LOG_TAG, "REST - update Password - call <data>: " + postBody);

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        RESTUtils.executePOSTUserUpdateRequest(url, postBody, null, "", globalVariable, null, activity);
        Log.e(LOG_TAG, "REST - update Password - call success: ");

        // mDataArray = TagItemDataHelper.getAlphabetData();
    }





}
