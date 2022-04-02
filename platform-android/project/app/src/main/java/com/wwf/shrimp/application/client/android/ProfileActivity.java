package com.wwf.shrimp.application.client.android;

import android.graphics.Bitmap;

import androidx.fragment.app.DialogFragment;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.wwf.shrimp.application.client.android.dialogs.EditProfileLineIdDialog;
import com.wwf.shrimp.application.client.android.dialogs.EditProfileNicknameDialog;
import com.wwf.shrimp.application.client.android.dialogs.EditProfilePasswordDialog;
import com.wwf.shrimp.application.client.android.dialogs.EditProfileEmailDialog;
import com.wwf.shrimp.application.client.android.dialogs.EditProfilePhoneNumberDialog;
import com.wwf.shrimp.application.client.android.models.dto.User;
import com.wwf.shrimp.application.client.android.system.SessionData;

/**
 * Activity which will profile documents to be managed
 * @author AleaActaEst
 */
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    //
    // UI elements
    private ImageView imageViewProfileImage;
    private TextView textViewUserFirstLastName;
    private TextView textViewUserName;
    private TextView textViewUserEmail;
    private TextView textViewUserPhoneNumber;
    private TextView textViewUserLineId;
    private TextView textViewUserNickname;
    private TextView textViewUserPassword;
    private RelativeLayout linearLayoutUserPassword;
    private RelativeLayout linearLayoutUserEmail;
    private RelativeLayout linearLayoutUserNickname;
    private RelativeLayout linearLayoutUserLineId;
    private RelativeLayout linearLayoutUserPhoneNumber;
    private ImageView imageViewPasswordEdit;
    private ImageView imageViewEmailEdit;
    private ImageView imageViewNicknameEdit;
    private ImageView imageViewLineIdEdit;
    private ImageView imageViewPhoneNumberEdit;



    // global session data access
    private SessionData globalVariable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // create access to session
        globalVariable  = (SessionData) getApplicationContext();

        //
        // Set the GUI elements
        imageViewProfileImage = findViewById(R.id.imageViewUserProfileImage);
        textViewUserFirstLastName = findViewById(R.id.textViewUserFirstLastName);
        textViewUserName = findViewById(R.id.textViewUserName);
        textViewUserEmail = findViewById(R.id.textViewUserEmail);
        textViewUserPhoneNumber = findViewById(R.id.textViewUserPhoneNumber);
        textViewUserLineId = findViewById(R.id.textViewUserLineId);
        textViewUserPhoneNumber = findViewById(R.id.textViewUserPhoneNumber);
        textViewUserNickname = findViewById(R.id.textViewUserNickname);
        textViewUserPassword = findViewById(R.id.textViewUserPassword);
        linearLayoutUserPassword = findViewById(R.id.linearLayoutUserPassword);
        linearLayoutUserEmail = findViewById(R.id.linearLayoutUserEmail);
        linearLayoutUserNickname = findViewById(R.id.linearLayoutUserNickname);
        linearLayoutUserLineId = findViewById(R.id.linearLayoutUserLineId);
        linearLayoutUserPhoneNumber = findViewById(R.id.linearLayoutUserPhoneNumber);
        imageViewPasswordEdit = findViewById(R.id.imageViewPasswordEdit);
        imageViewEmailEdit = findViewById(R.id.imageViewEmailEdit);
        imageViewNicknameEdit = findViewById(R.id.imageViewNicknameEdit);
        imageViewLineIdEdit = findViewById(R.id.imageViewLIneIdEdit);
        imageViewPhoneNumberEdit = findViewById(R.id.imageViewPhoneNumberEdit);



        //
        // set the on click handler
        textViewUserPassword.setOnClickListener(this);
        linearLayoutUserPassword.setOnClickListener(this);
        textViewUserEmail.setOnClickListener(this);
        linearLayoutUserEmail.setOnClickListener(this);
        textViewUserNickname.setOnClickListener(this);
        linearLayoutUserNickname.setOnClickListener(this);
        textViewUserLineId.setOnClickListener(this);
        linearLayoutUserLineId.setOnClickListener(this);
        textViewUserPhoneNumber.setOnClickListener(this);
        linearLayoutUserPhoneNumber.setOnClickListener(this);




        //
        // get the  url to get the image
        String url = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestUserFetchProfileImage()
                + globalVariable.getCurrentUser().getName();

        Glide.with(getApplicationContext()).load(url).asBitmap().centerCrop().into(new BitmapImageViewTarget(imageViewProfileImage) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                imageViewProfileImage.setImageDrawable(circularBitmapDrawable);
            }
        });

        //
        //get the first and last name
        textViewUserFirstLastName.setText(String.format("%1$s %2$s"
                , globalVariable.getCurrentUser().getContactInfo().getFirstName()
                , globalVariable.getCurrentUser().getContactInfo().getLastName()));
        textViewUserName.setText(globalVariable.getCurrentUser().getCredentials().getUsername());

        textViewUserEmail.setText(globalVariable.getCurrentUser().getContactInfo().getEmailAddress());
        textViewUserPhoneNumber.setText(globalVariable.getCurrentUser().getContactInfo().getCellNumber());
        textViewUserLineId.setText(globalVariable.getCurrentUser().getContactInfo().getLineId());
        textViewUserNickname.setText(globalVariable.getCurrentUser().getContactInfo().getNickName());
        textViewUserPassword.setText("********");
    }

    @Override
    /**
     * Creation of a document through the FAB button
     */
    public void onClick(View v) {
        User user = globalVariable.getCurrentUser();
        long organizationId=-1;
        long groupId=-1;

        if(user.getUserOrganizations().size() > 0) {
            organizationId = user.getUserOrganizations().get(0).getId();
            groupId = user.getUserGroups().get(0).getId();
        }

        //
        // get the clicked elements
        //
        //

        //
        // Password Edit
        if(v.getId() == textViewUserPassword.getId()
                || v.getId() == linearLayoutUserPassword.getId()
                || v.getId() == imageViewPasswordEdit.getId()){
            Toast.makeText(this, "Editing Password", Toast.LENGTH_SHORT).show();
            // Create an instance of the dialog fragment and show it
            DialogFragment dialog = new EditProfilePasswordDialog();
            dialog.show(getSupportFragmentManager(), "EditProfilePasswordDialog");
        }

        //
        // Email Edit
        if(v.getId() == textViewUserEmail.getId()
                || v.getId() == linearLayoutUserEmail.getId()
                || v.getId() == imageViewEmailEdit.getId()){
            Toast.makeText(this, "Editing Email", Toast.LENGTH_SHORT).show();
            // Create an instance of the dialog fragment and show it
            DialogFragment dialog = new EditProfileEmailDialog();
            dialog.show(getSupportFragmentManager(), "EditProfileEmailDialog");
        }

        //
        // Nickname Edit
        if(v.getId() == textViewUserNickname.getId()
                || v.getId() == linearLayoutUserNickname.getId()
                || v.getId() == imageViewNicknameEdit.getId()){
            Toast.makeText(this, "Editing Nickname", Toast.LENGTH_SHORT).show();
            // Create an instance of the dialog fragment and show it
            DialogFragment dialog = new EditProfileNicknameDialog();
            dialog.show(getSupportFragmentManager(), "EditProfileNicknameDialog");
        }

        //
        // Line Id Edit
        if(v.getId() == textViewUserLineId.getId()
                || v.getId() == linearLayoutUserLineId.getId()
                || v.getId() == imageViewLineIdEdit.getId()){
            Toast.makeText(this, "Editing LineId", Toast.LENGTH_SHORT).show();
            // Create an instance of the dialog fragment and show it
            DialogFragment dialog = new EditProfileLineIdDialog();
            dialog.show(getSupportFragmentManager(), "EditProfileLineIdDialog");
        }

        //
        // Line Id Edit
        if(v.getId() == textViewUserPhoneNumber.getId()
                || v.getId() == linearLayoutUserPhoneNumber.getId()
                || v.getId() == imageViewPhoneNumberEdit.getId()){
            Toast.makeText(this, "Editing PhoneNumber", Toast.LENGTH_SHORT).show();
            // Create an instance of the dialog fragment and show it
            DialogFragment dialog = new EditProfilePhoneNumberDialog();
            dialog.show(getSupportFragmentManager(), "EditProfilePhoneNumberDialog");
        }
    }
}
