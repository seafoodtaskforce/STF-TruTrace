package com.wwf.shrimp.application.client.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wwf.shrimp.application.client.android.fragments.AllDocumentsFragment;
import com.wwf.shrimp.application.client.android.fragments.MyDocumentsFragment;
import com.wwf.shrimp.application.client.android.fragments.ProfileDocumentsFragment;
import com.wwf.shrimp.application.client.android.listeners.ConnectivityReceiver;
import com.wwf.shrimp.application.client.android.models.ConfigurationData;
import com.wwf.shrimp.application.client.android.models.dto.Organization;
import com.wwf.shrimp.application.client.android.models.dto.User;
import com.wwf.shrimp.application.client.android.notifications.NotificationAlarmReceiver;
import com.wwf.shrimp.application.client.android.notifications.NotificationService;
import com.wwf.shrimp.application.client.android.services.CameraIntentService;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.ImageFilePath;
import com.wwf.shrimp.application.client.android.utils.ImageUtils;
import com.wwf.shrimp.application.client.android.utils.dialogs.QuitApplicationlDialogUtility;

import java.util.ArrayList;
import java.util.List;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Home page activity which is the main page that the user will see after they login.
 * @author AleaActaEst
 */
public class HomePageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ConnectivityReceiver.ConnectivityReceiverListener {

    public static final int GALLERY_REQUEST_CODE = 101;
    public static final String FILE_UPLOAD_URL = "";


    // logging tag
    private static final String LOG_TAG = "Home Activity";

    // GPS
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;

    //
    // UI elements
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private CoordinatorLayout coordinatorLayout;
    private ImageView imageView;

    // Profile Upload
    private long totalSize = 0;
    private String uploadProfileFilePath = null;

    // global session data access
    private SessionData globalVariable = null;
    // Saved Instance Keys
    private static String SAVED_INSTANCE_KEY_USER = "CurrentUser";
    private static String SAVED_INSTANCE_KEY_ORGANIZATION = "CurrentOrganization";
    private static String SAVED_INSTANCE_KEY_CONFIG = "CurrentConfig";
    //


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // create access to session
        globalVariable  = (SessionData) getApplicationContext();
        setContentView(R.layout.activity_home_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        //
        // Progress Bar
        //progressBar = findViewById(R.id.progressBarProfileImageUpload);

        //
        // set User Profile information
        View header=navigationView.getHeaderView(0);
        TextView textViewUsername = (TextView) header.findViewById(R.id.textViewUsername);
        TextView textViewEmailAddress = (TextView) header.findViewById(R.id.textViewEmailAddress);
        imageView = (ImageView) header.findViewById(R.id.imageViewUserProfileImage);

        //
        // Get the event for changing the profile pic
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //
                // Launch the intent


                //Toast.makeText(getApplicationContext(), "Launching Intent", Toast.LENGTH_LONG).show();
                pickFromGallery();
            }
        });

        //
        // Restore Session Data
        //
        if (savedInstanceState == null) {
            // do nothing
        } else {
            //
            // GSON parser
            Gson gson = new GsonBuilder()
                    .create();

            // get the data
            String sessionUser = savedInstanceState.getString(HomePageActivity.SAVED_INSTANCE_KEY_USER);
            String sessionOrganization = savedInstanceState.getString(HomePageActivity.SAVED_INSTANCE_KEY_ORGANIZATION);
            String sessionConfig = savedInstanceState.getString(HomePageActivity.SAVED_INSTANCE_KEY_CONFIG);
            //set the data back into the session
            globalVariable.setCurrentUser(gson.fromJson(sessionUser, User.class));
            globalVariable.setCurrentOrganization(gson.fromJson(sessionOrganization, Organization.class));
            globalVariable.setConfigurationData(gson.fromJson(sessionConfig, ConfigurationData.class));

        }
        textViewUsername.setText( globalVariable.getCurrentUser().getCredentials().getUsername());
        textViewEmailAddress.setText( globalVariable.getCurrentUser().getContactInfo().getEmailAddress());

        // check for notification data
        String docNotificationSyncId = getIntent().getStringExtra(NotificationService.NOTIFICATION_SERVICE_INTENT_DOC_DATA_KEY);
        if(docNotificationSyncId != null){

            globalVariable.setNotificationDocSessionId(docNotificationSyncId);
            viewPager.setCurrentItem(1);
        }

        //
        // Load profile image

        // get the  url to get the image
        String url = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestUserFetchProfileImage()
                + globalVariable.getCurrentUser().getName();

        Glide.with(getApplicationContext()).load(url).asBitmap().centerCrop().into(new BitmapImageViewTarget(imageView) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                imageView.setImageDrawable(circularBitmapDrawable);
            }
        });
        /**
        Glide
                .with(getApplicationContext())
                .load(url)
                .transform(new CircleTransform(HomePageActivity.this))
                .into(imageView);
         */


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission to save videos in external storage
            ActivityCompat.requestPermissions(
                    this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, CameraIntentService.PERMISSION_RQ);
        }

        // start the notifications listener
        scheduleNotificationsAlarm();

        //
        // check of the user preferences should be set
        SharedPreferences sharedPreferences;
        sharedPreferences = getDefaultSharedPreferences(this);
        String rememberMeUserName = sharedPreferences.getString(SessionData.USER_REMEMBER_ME_KEY, "");
        String userToken;
        if(globalVariable.isRememberMeIsOn()){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            // user name
            editor.putString(SessionData.USER_REMEMBER_ME_KEY, globalVariable.getCurrentUser().getName());
            editor.apply();

            // token
            editor.putString(SessionData.USER_TOKEN_REMEMBER_ME_KEY, globalVariable.getCurrentUser().getCredentials().getToken().getTokenValue());
            editor.apply();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        // get the session data as a string

        //
        // GSON parser
        Gson gson = new GsonBuilder()
                .create();

        String sessionUser = gson.toJson(globalVariable.getCurrentUser());
        String sessionOrganization = gson.toJson(globalVariable.getCurrentOrganization());
        String sessionConfig = gson.toJson(globalVariable.getConfigurationData());
        savedInstanceState.putString(HomePageActivity.SAVED_INSTANCE_KEY_USER, sessionUser);
        savedInstanceState.putString(HomePageActivity.SAVED_INSTANCE_KEY_ORGANIZATION, sessionOrganization);
        savedInstanceState.putString(HomePageActivity.SAVED_INSTANCE_KEY_CONFIG, sessionConfig);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            final AlertDialog alertDialog = QuitApplicationlDialogUtility.showCancelDialog(this);
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Toast.makeText(getContext(), getResources().getString(R.string.document_custom_dialog_cancel_toast_message), Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                    HomePageActivity.this.onSuperBackPressed();

                }
            });
            // super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_page, menu);
        return true;
    }

    @Override
    /**
     * Handle settings/options menu items
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //
        // handle the different options
        if (id == R.id.action_settings) {
            // do nothing here <TODO>
            return true;

        }else if(id == R.id.action_about){
            String version;
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                version = pInfo.versionName;
            }catch(Exception e){
                version = "N/A";
            }
            // show the version of the application to the user
            Toast.makeText(this, version, Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    /**
     * Handle navigation drawer menu items
     */
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Handle the profile action
            startActivity(new Intent(this, ProfileActivity.class));

            /**
        } if (id == R.id.nav_home) {
            // Handle the home action
            // TODO
        } else if (id == R.id.nav_documents) {
            // TODO

        } else if (id == R.id.nav_search) {
            // TODO

        } else if (id == R.id.nav_settings) {
            // TODO
*/
        } else if (id == R.id.nav_logout) {
            String logout_dialog_title = getResources().getString(R.string.logout_dialog_title);
            String logout_dialog_text = getResources().getString(R.string.logout_dialog_text);
            String logout_dialog_button_confirm = getResources().getString(R.string.logout_dialog_button_confirm);
            String logout_dialog_button_cancel = getResources().getString(R.string.logout_dialog_button_cancel);

            androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(this);
            alertDialogBuilder
                    .setTitle(logout_dialog_title)
                    .setMessage(logout_dialog_text)
                    //
                    // here the user is confirming a logout action
                    .setPositiveButton(logout_dialog_button_confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // clear session data
                            globalVariable.clearState();
                            Intent logout = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(logout);
                            finish();

                        }
                    })
                    .setNegativeButton(logout_dialog_button_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // dismiss dialog here because user doesn't want to logout
                        }
                    });

            // create alert dialog
            androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Tabbed view pager being setup for the two lists being shown.
     * Two fragments are being injected here:
     *      1. MyDocumentsFragment
     *      2. AllDocumentsFragment
     *  We use an inner class for this: ViewPagerAdapter
     * @param viewPager - the view pager to setup
     */
    private void setupViewPager(ViewPager viewPager) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        String my_documents_tab_text = getResources().getString(R.string.my_documents_tab_text);
        String other_documents_tab_text = getResources().getString(R.string.other_documents_tab_text);
        String profile_documents_tab_text = getResources().getString(R.string.profile_documents_tab_text);

        if(profileDocsExist()){
            adapter.addFragment(new ProfileDocumentsFragment(), profile_documents_tab_text);
        }
        adapter.addFragment(new MyDocumentsFragment(), my_documents_tab_text);
        adapter.addFragment(new AllDocumentsFragment(), other_documents_tab_text);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                globalVariable.setCurrFragment(adapter.getItem(position));
                // Toast.makeText(getApplicationContext(), "Clicked a Tab", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        globalVariable.setCurrFragment(adapter.getItem(0));
    }

    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    //data.getData returns the content URI for the selected Image
                    final Uri selectedImage = data.getData();
                    Glide.with(getApplicationContext()).load(selectedImage).asBitmap().centerCrop().into(new BitmapImageViewTarget(imageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            imageView.setImageDrawable(circularBitmapDrawable);

                        }
                    });
                    uploadProfileFilePath = ImageFilePath.getPath(HomePageActivity.this, selectedImage);
                    // start the upload process
                    // get the  url to get the image
                    String url = globalVariable.getConfigurationData().getServerURL()
                            + globalVariable.getConfigurationData().getApplicationInfixURL()
                            + globalVariable.getConfigurationData().getRestUserUpdateProfileImage();
                    ImageUtils.uploadFile(url, uploadProfileFilePath, globalVariable);
                    break;
                case RESULT_CANCELED:
                    // user cancelled Image capture
                    Toast.makeText(getApplicationContext(),
                            R.string.profile_edit_image_request_cancel, Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
    }

    /**
     * Simple ViewPagerAdapter which will allow for tabbed data to be
     * inserted into the UI as a set of tabbed lists.
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
            notifyDataSetChanged();
        }




        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        SessionData.getInstance().setConnectivityListener(this);
        Toast.makeText(globalVariable, "Setting connectivity", Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        // showSnack(isConnected);
        // Toast.makeText(globalVariable, "Connected: " + isConnected, Toast.LENGTH_SHORT).show();
        Log.i(LOG_TAG, "Connectivity Changed!!! " + isConnected);

    }



    public void onSuperBackPressed(){
        super.onBackPressed();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                    Toast.makeText(this, "GPS Good To Go!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please set the location for this app!", Toast.LENGTH_SHORT).show();
                    //showSettingsAlert();
                }

                break;
        }
    }

    // Setup a recurring alarm every half hour
    public void scheduleNotificationsAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), NotificationAlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, NotificationAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                5000, pIntent);
    }

    private void pickFromGallery(){
        //Create an Intent with action as ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    private boolean profileDocsExist() {
        //
        // preconditions
        if(globalVariable == null
            || globalVariable.getCurrentUser() == null
            || globalVariable.getCurrentUser().getUserGroups() == null) {
            return false;
        }
        //
        // Process
        for (int i = 0; i < globalVariable.getCurrentUser().getUserGroups().get(0).getAllowedDocTypes().size(); i++) {
            // change the colors and the visibility
            if (globalVariable.getCurrentUser().getUserGroups().get(0).getAllowedDocTypes().get(i).getDocumentDesignation().equals("Profile")) {
                return true;
            }
        }
        return false;
    }
}
