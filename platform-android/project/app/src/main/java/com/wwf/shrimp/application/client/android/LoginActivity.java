package com.wwf.shrimp.application.client.android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wwf.shrimp.application.client.android.models.ConfigurationData;
import com.wwf.shrimp.application.client.android.models.dto.SecurityToken;
import com.wwf.shrimp.application.client.android.models.dto.PasswordCredentials;
import com.wwf.shrimp.application.client.android.models.dto.User;
import com.wwf.shrimp.application.client.android.notifications.NotificationService;
import com.wwf.shrimp.application.client.android.services.ConfigurationService;
import com.wwf.shrimp.application.client.android.services.impl.PropertiesConfigurationService;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;
import com.wwf.shrimp.application.client.android.utils.StorageUtils;
import com.wwf.shrimp.application.client.android.utils.dialogs.ErrorConnectingDialogUtility;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Login activity which shows the user a login screen.
 * This is where ewe will capture the credentials as well as the authentication token and store it in session.
 * We will need this token each time we make a call to the back-endAuthentication is done
 *
 * @author AleaActaEst
 *
 */
public class LoginActivity extends AppCompatActivity {

    // logging tag
    private static final String LOG_TAG = "Login Activity";

    // global session data access
    private SessionData globalVariable = null;
    private ConfigurationService configService = null;
    private ConfigurationData configData = new ConfigurationData();


    // progress bar
    private ProgressBar progressBar;

    // UI fields
    TextInputLayout usernameWrapper;
    TextInputLayout passwordWrapper;
    TextView applicationVersionId;
    TextView registerTextViewButton;
    Button loginButton;
    CheckBox rememberMeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // create access to session
        globalVariable  = (SessionData) getApplicationContext();

        // get configuration data
        //
        Log.i(LOG_TAG, "Configuring the application: ");
        configService = new PropertiesConfigurationService(getApplicationContext());
        configService.open();
            configData = configService.readAllProperties();
        configService.close();
        globalVariable.setConfigurationData(configData);

        //
        // set UI elements
        usernameWrapper = (TextInputLayout) findViewById(R.id.usernameWrapper);
        passwordWrapper = (TextInputLayout) findViewById(R.id.passwordWrapper);
        applicationVersionId = (TextView) findViewById(R.id.applicationVersionId);
        registerTextViewButton = (TextView) findViewById(R.id.tvRegisterButton);
        rememberMeCheckBox = (CheckBox)findViewById(R.id.checkBoxRememberMe);
        loginButton = (Button) findViewById(R.id.btn);

        activateProgressBar();

        rememberMeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rememberMeCheckBox.isChecked()){
                    // <TODO>
                    globalVariable.setRememberMeIsOn(true);

                } else {
                    globalVariable.setRememberMeIsOn(false);
                    //
                    // clear preferences
                    clearPreferences();
                }
            }

        });

        if(!globalVariable.isRememberMeIsOn()) {
            //
            // init UI elements
            if(isRememberMeOn()) {
                rememberMeCheckBox.setActivated(true);
                rememberMeCheckBox.setChecked(true);
                String username = getRememberMeUsername();
                if(!username.isEmpty()){
                    usernameWrapper.getEditText().setText(username);
                }

            } else {
                rememberMeCheckBox.setActivated(true);
                rememberMeCheckBox.setChecked(false);
            }


            // set the hints internationalized
            String username_field_hint = getResources().getString(R.string.username_field_hint);
            usernameWrapper.setHint(username_field_hint);
            String password_field_hint = getResources().getString(R.string.password_field_hint);
            passwordWrapper.setHint(password_field_hint);

            String version = null;
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                version = pInfo.versionName;
            }catch(Exception e){
                version = "N/A";
            }
            applicationVersionId.setText(version);

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Logging routine
                    hideKeyboard();

                    String username = usernameWrapper.getEditText().getText().toString();
                    String password = passwordWrapper.getEditText().getText().toString();
                    Log.d(LOG_TAG, "Credentials " + username + " " + password);

                    // if success then log the person in
                    validatePasswordLogin(username, password);
                }
            });

            registerTextViewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //
                    // build the registration URL
                    String url;
                    url = globalVariable.getConfigurationData().getServerURL()
                            + globalVariable.getConfigurationData().getWebUserRegistrationURL();
                    Log.i(LOG_TAG, "REST - get registration URL " + url);
                    //
                    // execute the intent
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
        } else{
            usernameWrapper.setVisibility(View.INVISIBLE);
            passwordWrapper.setVisibility(View.INVISIBLE);
            applicationVersionId.setVisibility(View.INVISIBLE);
            registerTextViewButton.setVisibility(View.INVISIBLE);
            rememberMeCheckBox.setVisibility(View.INVISIBLE);
            loginButton.setVisibility(View.INVISIBLE);

            validateTokenLogin(getRememberMeUsername(), getRememberMeUserTokenString());

        }
    }

    /**
     * Get rid of the keyboard when not needed
     */
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * Validate the user and load some session data on a successful authentication
     * @param username - the username
     * @param password - the password that the user is logging in with
     */
    public void validatePasswordLogin(String username, String password) {
        User currentUser = new User();
        PasswordCredentials credentials = new PasswordCredentials();
        credentials.setUsername(username);
        credentials.setPassword(password);
        currentUser.setCredentials(credentials);

        Log.i(LOG_TAG, "Initiating authentication");
        // Authenticate the user input
        //
        authenticateUser(currentUser);
        progressBar.setVisibility(View.VISIBLE);

    }

    /**
     * Validate the user and load some session data on a successful authentication
     * @param username - the username
     * @param tokenString - the token ti log the user with
     */
    public void validateTokenLogin(String username, String tokenString) {
        User currentUser = new User();
        PasswordCredentials credentials = new PasswordCredentials();
        credentials.setUsername(username);
        SecurityToken token = new SecurityToken();
        token.setTokenValue(tokenString);
        credentials.setToken(token);
        currentUser.setCredentials(credentials);

        Log.i(LOG_TAG, "Initiating authentication");
        // Authenticate the user input
        //
        authenticateUser(currentUser);
        progressBar.setVisibility(View.VISIBLE);

    }

    protected void authenticateUser(User currentUser){
        // Set up the GET request URL
        String url = globalVariable.getConfigurationData().getServerURL()
                        + globalVariable.getConfigurationData().getApplicationInfixURL()
                        + globalVariable.getConfigurationData().getRestLoginURL();
        Log.i(LOG_TAG, "REST - Login - call: " + url);

        // Create the handler
        OkHttpHandler okHttpHandler= new OkHttpHandler(this);

        // create the credentials as JSON string
        String jsonCredentials = RESTUtils.getJSON(currentUser.getCredentials());
        Log.d(LOG_TAG, "JSON Credentials: " + jsonCredentials);

        // execute the call synchronously to get credentials
        okHttpHandler.execute(url, jsonCredentials);
    }


    /**
     * Inner HTTP REST call Handler which will be called when the remote call is executed.
     * This will execute the Login Service on the backend
     */
    public class OkHttpHandler extends AsyncTask<String, String, String> {
        String requestURL;
        Context context;
        boolean serverDownFlag = false;

        public OkHttpHandler(Context context) {
            this.context = context;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(globalVariable.getConfigurationData().getClientSocketTimeoutInSeconds(), TimeUnit.SECONDS)
                .build();

        @Override
        protected String doInBackground(String... params) {

            RequestBody body = RequestBody.create(RESTUtils.JSON_MEDIA_TYPE, params[1]);
            Request request  = new Request.Builder()
                    .url(params[0])
                    .post(body)
                    .build();

            requestURL = request.url().toString();
            Log.d(LOG_TAG, "OkHTTP Request " + request.url());
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            }catch (Exception e){
                Log.e(LOG_TAG, "No connection to the server " + params[1]);
                serverDownFlag = true;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


            // extract the data for the GSON authentication
            User user=null;

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-mm-dd hh:mm:ss")
                    .create();

            ///////////////////////////////////////////////////////////////////////////////////////
            // Getting USER credentials and token from the backend
            //
            if(requestURL.contains(globalVariable.getConfigurationData().getRestLoginURL())){
                // parse the JSON input into the specific class
                try{
                    user = gson.fromJson(s, User.class);
                    Log.d(LOG_TAG, "Authentication response from server: " + user);
                }catch(Exception e){
                    String password_failed_message = getResources().getString(R.string.password_failed_message);
                    passwordWrapper.setError(password_failed_message);
                }



                // check that we got anything from the server
                if(serverDownFlag ==true || user==null ){
                    Log.d(LOG_TAG, "Server failed authentication." );
                    ErrorConnectingDialogUtility.showServerSystemErrorDialog(context);
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                // set the dialog based on the request response
                //

                // On Success
                if(user.getCredentials().getToken() != null){
                    // Reflect the validation on the UI
                    usernameWrapper.setErrorEnabled(false);
                    passwordWrapper.setErrorEnabled(false);

                    //
                    // Modify user name to be lower case
                    user.setName(user.getName().toLowerCase());
                    user.getCredentials().setUsername(user.getCredentials().getUsername().toLowerCase());

                    // Load the user and token into session
                    //
                    globalVariable.setCurrentUser(user);
                    globalVariable.setAppResources(user.getAppResources());

                    //
                    // start notifications
                    createNotificationChannel();

                    // Start the next activity
                    startActivity(new Intent(LoginActivity.this, HomePageActivity.class));
                    LoginActivity.this.finish();
                // On Failure
                }else{
                    String password_failed_message = getResources().getString(R.string.password_failed_message);
                    passwordWrapper.setError(password_failed_message);
                }
            }

            //
            // Adjust the progress bar
            progressBar.setVisibility(View.INVISIBLE);
            Log.i(LOG_TAG, "Authentication is done." );
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NotificationService.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String getRememberMeUsername() {
        SharedPreferences sharedPreferences;
        sharedPreferences = getDefaultSharedPreferences(this);
        String rememberMeUserName = sharedPreferences.getString(SessionData.USER_REMEMBER_ME_KEY, "");
        return rememberMeUserName;
    }

    private String getRememberMeUserTokenString() {
        SharedPreferences sharedPreferences;
        sharedPreferences = getDefaultSharedPreferences(this);
        String rememberMeUserName = sharedPreferences.getString(SessionData.USER_TOKEN_REMEMBER_ME_KEY, "");
        return rememberMeUserName;
    }


    private boolean isRememberMeOn(){
        //
        // check shared preferences
        SharedPreferences sharedPreferences;
        sharedPreferences = getDefaultSharedPreferences(this);
        String rememberMeUserName = sharedPreferences.getString(SessionData.USER_REMEMBER_ME_KEY, "");
        String userToken;
        if(!rememberMeUserName.isEmpty()){
            //
            // get the user's token
            userToken = sharedPreferences.getString(SessionData.USER_TOKEN_REMEMBER_ME_KEY, "");
            //
            // the user already exists we can log them in
            globalVariable.setRememberMeIsOn(true);

            // extract the user name and data

            return true;

        }
        return false;
    }

    private void clearPreferences() {
        SharedPreferences sharedPreferences;
        sharedPreferences = getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // user name
        editor.remove(SessionData.USER_REMEMBER_ME_KEY);
        editor.remove(SessionData.USER_TOKEN_REMEMBER_ME_KEY);
        editor.apply();
    }

    private void activateProgressBar() {
        // progress bar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ResourcesCompat.getColor(getResources(), R.color.login_hint_highlight, null)
                , android.graphics.PorterDuff.Mode.SRC_IN);
    }


}