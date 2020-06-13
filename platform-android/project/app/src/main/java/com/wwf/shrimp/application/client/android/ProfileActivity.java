package com.wwf.shrimp.application.client.android;

import android.graphics.Bitmap;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.wwf.shrimp.application.client.android.dialogs.EditProfilePasswordDialog;
import com.wwf.shrimp.application.client.android.dialogs.TabbedDocumentDialog;
import com.wwf.shrimp.application.client.android.fragments.ProfileDocumentsFragment;
import com.wwf.shrimp.application.client.android.models.dto.DocumentType;
import com.wwf.shrimp.application.client.android.models.dto.User;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.system.SessionData;

import java.util.Date;

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
    private TextView textViewUserNickName;
    private TextView textViewUserPassword;
    private RelativeLayout linearLayoutUserPassword;
    private ImageView imageViewPasswordEdit;



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
        textViewUserNickName = findViewById(R.id.textViewUserNickName);
        textViewUserPassword = findViewById(R.id.textViewUserPassword);
        linearLayoutUserPassword = findViewById(R.id.linearLayoutUserPassword);
        imageViewPasswordEdit = findViewById(R.id.imageViewPasswordEdit);

        //
        // set the on click handler
        textViewUserPassword.setOnClickListener(this);
        linearLayoutUserPassword.setOnClickListener(this);

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
        textViewUserNickName.setText(globalVariable.getCurrentUser().getContactInfo().getNickName());
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
        // get the clicked elements
        if(v.getId() == textViewUserPassword.getId()
                || v.getId() == linearLayoutUserPassword.getId()
                || v.getId() == imageViewPasswordEdit.getId()){
            Toast.makeText(this, "Editing Password", Toast.LENGTH_SHORT).show();
            // Create an instance of the dialog fragment and show it
            DialogFragment dialog = new EditProfilePasswordDialog();
            dialog.show(getSupportFragmentManager(), "EditProfilePasswordDialog");
        }
    }
}
