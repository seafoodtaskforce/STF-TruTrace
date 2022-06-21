package com.wwf.shrimp.application.client.android.dialogs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.vision.barcode.Barcode;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.models.dto.PasswordCredentials;
import com.wwf.shrimp.application.client.android.models.dto.User;
import com.wwf.shrimp.application.client.android.models.dto.UserContact;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.EmailUtils;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;

/**
 * Dialog for Email management
 */
public class EditProfileNicknameDialog extends DialogFragment {

    // logging tag
    private static final String LOG_TAG = "EditNicknameDialog";

    SessionData globalVariable;
    EditText editTextNickname;

    public EditProfileNicknameDialog(){
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_edit_nickname, container,
                false);

        getDialog().setTitle(getResources().getString(R.string.profile_update_email_text_header));

        // set the session data
        globalVariable = (SessionData)getContext().getApplicationContext();

        //
        // Get the input data elements
        editTextNickname = rootView.findViewById(R.id.editTextNickname);

        //
        // Cancelling the password edit
        Button cancelButton = (Button)rootView.findViewById(R.id.buttonNicknameEditCancel);
        cancelButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                EditProfileNicknameDialog.super.dismiss();
            }
        });

        //
        // creating a new Tag
        Button saveButton = (Button)rootView.findViewById(R.id.buttonNicknameEditSave);
        saveButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //
                // read the data from the input
                // use globalVariable.getConfigurationData().getGlobalConstantTaggingCustomPrefix() to get the global prefix
                User editUser = new User();
                User existingUser = globalVariable.getCurrentUser();
                //
                // create contact data
                UserContact contactInfo = existingUser.getContactInfo();
                contactInfo.setNickName(editTextNickname.getText().toString());
                editUser.setContactInfo(contactInfo);
                editUser.setId(existingUser.getId());

                //
                // Prepare the POST rest request to create a new tag and get the id of the tag
                //if(!EmailUtils.isValid(editTextNickname.getText().toString())){
                //    Toast.makeText(getContext(), getResources().getString(R.string.profile_update_error_email_wrong_format), Toast.LENGTH_SHORT).show();
                //}else{
                    sendDataChange(editUser);
                    EditProfileNicknameDialog.super.dismiss();
                //}
            }
        });

        // Do something else
        return rootView;

    }

    /**
     * Initialize the UI data for a list of all tags
     */
    protected void sendDataChange(User editedUser) {
        /**
         * get data from the server about tags
         */
        String url = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestUserUpdateProfile();
        Log.i(LOG_TAG, "REST - update user nickname - call: " + url);

        String postBody = RESTUtils.getJSON(editedUser);
        Log.e(LOG_TAG, "REST - update nickname - call <data>: " + postBody);

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        RESTUtils.executePOSTUserUpdateRequest(url, postBody, null, "", globalVariable, User.CONTACT_INFO_FIELD_NICKNAME, activity);
        globalVariable.setChangedField(editedUser.getContactInfo().getNickName());
        Log.e(LOG_TAG, "REST - update Password - call success: ");

        // mDataArray = TagItemDataHelper.getAlphabetData();
    }
}
