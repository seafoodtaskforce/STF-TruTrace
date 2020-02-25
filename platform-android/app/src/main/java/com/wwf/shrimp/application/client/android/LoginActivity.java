package com.wwf.shrimp.application.client.android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wwf.shrimp.application.client.android.models.ConfigurationData;
import com.wwf.shrimp.application.client.android.models.dto.PasswordCredentials;
import com.wwf.shrimp.application.client.android.models.dto.User;
import com.wwf.shrimp.application.client.android.notifications.NotificationService;
import com.wwf.shrimp.application.client.android.services.ConfigurationService;
import com.wwf.shrimp.application.client.android.services.impl.PropertiesConfigurationService;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;
import com.wwf.shrimp.application.client.android.utils.dialogs.ErrorConnectingDialogUtility;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Login activity which shows the user a login screen.
 * This is where ewe will capture the credentials as well as the authentication token and store it in session.
 * We will need this token each time we make a call to the back-end
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
    Button loginButton;

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
        loginButton = (Button) findViewById(R.id.btn);

        // set the hints internationalized
        String username_field_hint = getResources().getString(R.string.username_field_hint);
        usernameWrapper.setHint(username_field_hint);
        String password_field_hint = getResources().getString(R.string.password_field_hint);
        passwordWrapper.setHint(password_field_hint);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logging routine
                hideKeyboard();

                String username = usernameWrapper.getEditText().getText().toString();
                String password = passwordWrapper.getEditText().getText().toString();
                Log.d(LOG_TAG, "Credentials " + username + " " + password);

                // if success then log the person in
                validateLogin(username, password);
            }
        });

        // progress bar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ResourcesCompat.getColor(getResources(), R.color.login_hint_highlight, null)
                , android.graphics.PorterDuff.Mode.SRC_IN);

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
    public void validateLogin(String username, String password) {
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

        OkHttpClient client = new OkHttpClient();

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
}