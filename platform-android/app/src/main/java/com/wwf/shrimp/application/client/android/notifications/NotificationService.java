package com.wwf.shrimp.application.client.android.notifications;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wwf.shrimp.application.client.android.HomePageActivity;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.models.dto.AuditAction;
import com.wwf.shrimp.application.client.android.models.dto.NotificationData;
import com.wwf.shrimp.application.client.android.services.dao.DocumentJSONService;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by user on 22/03/2018.
 */

public class NotificationService extends IntentService {
    public static int numMessages=0;

    // global session data access
    private SessionData globalVariable = null;
    // logging tag
    private static final String LOG_TAG = "NotificationService";

    // local services for offline data storage
    private DocumentJSONService documentService;


    public static final String CHANNEL_ID = "TOR Channel";
    public static final String NOTIFICATION_SERVICE_INTENT_DOC_DATA_KEY = "document.session.id";

    public NotificationService() {
        super("MyTestService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url;
        // documents holder for fetching all notifications
        ArrayList<NotificationData> notifications;
        //
        String documentJSON = null;
        //
        // GSON parser
        Gson gson = new GsonBuilder()
                .create();

        // remove later TODO
        //Toast.makeText(this, "1. Handling Notification PUSH", Toast.LENGTH_SHORT).show();

        //
        // Preconditions
        globalVariable  = (SessionData) getApplicationContext();
        if(globalVariable == null
                || globalVariable.getConfigurationData() == null
                ||  globalVariable.getCurrentUser() == null){
            // remove later TODO
            //Toast.makeText(this, "2. NULL DATA!!!", Toast.LENGTH_SHORT).show();
            return;
        }

        String postUrl = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestDocumentCreateURL();

        if(RESTUtils.checkConnection() == false){
            if(globalVariable.isConnected() == false){
                // do nothing

            }else{
                globalVariable.setConnected(false);
                Log.i(LOG_TAG, "LOST Internet Connectivity");
            }
            //Toast.makeText(this, "No Connection!!!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(RESTUtils.checkConnection() == true){
            if(globalVariable.isConnected() == false){
                globalVariable.setConnected(true);
                Log.i(LOG_TAG, "Internet connectivity is back ON.");

            }
            // get the next outstanding documents
            documentService = new DocumentJSONService(getApplicationContext());
            documentService.open();
                documentJSON = documentService.getNextDocumentJSONToSync(globalVariable.getCurrentUser().getName());
            documentService.close();

            if(documentJSON != null) {
                RESTUtils.executePOSTDocumentSyncRequest(postUrl, documentJSON, globalVariable, "Property Bag");
            }
        }

        url = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestNotificationFetchByUserName();
        // Toast.makeText(this, "4. Setting PUSH Data", Toast.LENGTH_SHORT).show();

        // Do the task here
        Log.i("MyTestService", "Service running");
        // get remote data
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .header("user-name", globalVariable.getCurrentUser().getName())
                .url(url)
                .build();
        try{
            Response response = client.newCall(request).execute();
            // extract the notifications
            Type listType = new TypeToken<ArrayList<NotificationData>>(){}.getType();
            notifications = gson.fromJson(response.body().string(), listType);
            for(int i=0; i<notifications.size(); i++ ){
                // addNotification(notifications.get(i));
                // Toast.makeText(this, "4.1 Getting PUSH Data", Toast.LENGTH_SHORT).show();
                addBigNotification(notifications.get(i));
            }
            Log.d("TAG",notifications.toString());
        }catch(Exception e){
            //Toast.makeText(this, "5. Unable To get the Notification", Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, "6. Unable To get the Notification " + e.toString(), Toast.LENGTH_LONG).show();
            System.out.print(e.getStackTrace());
        }


    }


    private void addNotification(NotificationData notification) {
        String messageTextHeader = null;
        String messageTextBody = null;

        //
        // get the proper notification text
        if(notification.getAuditData().getAction().equals(AuditAction.DOCUMENT_ACCEPT)){
            messageTextHeader = "Document Acceptance";
            messageTextBody = "Your Document was Accepted by: ";
        }
        if(notification.getAuditData().getAction().equals(AuditAction.DOCUMENT_REJECT)){
            messageTextHeader = "Document Rejection";
            messageTextBody = "Your Document was Rejected by: ";
        }
        if(notification.getAuditData().getAction().equals(AuditAction.DOCUMENT_SUBMIT)){
            messageTextHeader = "Document Submission";
            messageTextBody = "Document was Submitted for your review by: ";
        }
        if(notification.getAuditData().getAction().equals(AuditAction.DOCUMENT_RESUBMIT)){
            messageTextHeader = "Document Re-Submission";
            messageTextBody = "Document was Re-Submitted by: ";
        }

        //
        // Create the notification

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher_shrimp)
                        .setContentTitle(messageTextHeader)
                        .setContentText(messageTextBody
                                + notification.getAuditData().getActor().getName()
                                + " with id: "
                                + notification.getAuditData().getItemId());

        Intent notificationIntent = new Intent(this, HomePageActivity.class);
        notificationIntent.putExtra(NOTIFICATION_SERVICE_INTENT_DOC_DATA_KEY, notification.getAuditData().getItemId());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(numMessages++, builder.build());
    }

    public void addBigNotification(NotificationData notification){
        Log.i("Start", "notification");

        // Reaction on tap
        Intent intent = new Intent(this, HomePageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        /* Invoking the default notification service */
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);

        //
        // init the global data
        mBuilder.setContentText(globalVariable.getResources().getString(R.string.alert_content_header));
        mBuilder.setTicker(globalVariable.getResources().getString(R.string.alert_content_ticker));
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_shrimp);
        // Set the intent that will fire when the user taps the notification
        mBuilder.setContentIntent(pendingIntent);

        /* Add Big View Specific Configuration */
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        //
        // get the proper notification text
        if(notification.getAuditData().getAction().equals(AuditAction.DOCUMENT_ACCEPT)){
            mBuilder.setContentTitle(globalVariable.getResources().getString(R.string.alert_document_header_acceptance));
            String[] events = new String[4];
            events[0] = globalVariable.getResources().getString(R.string.alert_content_title_accepted);
            events[1] = notification.getAuditData().getActor().getName();
            events[2] = notification.getAuditData().getTimestamp();

            // Sets a title for the Inbox style big view
            inboxStyle.setBigContentTitle(globalVariable.getResources().getString(R.string.alert_content_big_title_alert_details));

            // Moves events into the big view
            for (int i=0; i < events.length; i++) {
                inboxStyle.addLine(events[i]);
            }
            mBuilder.setStyle(inboxStyle);

        }
        if(notification.getAuditData().getAction().equals(AuditAction.DOCUMENT_REJECT)){
            mBuilder.setContentTitle(globalVariable.getResources().getString(R.string.alert_document_header_rejection));
            String[] events = new String[4];
            events[0] = globalVariable.getResources().getString(R.string.alert_content_title_rejected);
            events[1] = notification.getAuditData().getActor().getName();
            events[2] = notification.getAuditData().getTimestamp();

            // Sets a title for the Inbox style big view
            inboxStyle.setBigContentTitle(globalVariable.getResources().getString(R.string.alert_content_big_title_alert_details));

            // Moves events into the big view
            for (int i=0; i < events.length; i++) {
                inboxStyle.addLine(events[i]);
            }
            mBuilder.setStyle(inboxStyle);
        }
        if(notification.getAuditData().getAction().equals(AuditAction.DOCUMENT_SUBMIT)){
            mBuilder.setContentTitle(globalVariable.getResources().getString(R.string.alert_document_header_submission));
            String[] events = new String[4];
            events[0] = globalVariable.getResources().getString(R.string.alert_content_title_submitted);
            events[1] = notification.getAuditData().getActor().getName();
            events[2] = notification.getAuditData().getTimestamp();

            // Sets a title for the Inbox style big view
            inboxStyle.setBigContentTitle(globalVariable.getResources().getString(R.string.alert_content_big_title_alert_details));

            // Moves events into the big view
            for (int i=0; i < events.length; i++) {
                inboxStyle.addLine(events[i]);
            }
            mBuilder.setStyle(inboxStyle);
        }
        if(notification.getAuditData().getAction().equals(AuditAction.DOCUMENT_RESUBMIT)){
            mBuilder.setContentTitle(globalVariable.getResources().getString(R.string.alert_document_header_re_submission));
            String[] events = new String[4];
            events[0] = globalVariable.getResources().getString(R.string.alert_content_title_re_submitted);
            events[1] = notification.getAuditData().getActor().getName();
            events[2] = notification.getAuditData().getTimestamp();

            // Sets a title for the Inbox style big view
            inboxStyle.setBigContentTitle(globalVariable.getResources().getString(R.string.alert_content_big_title_alert_details));

            // Moves events into the big view
            for (int i=0; i < events.length; i++) {
                inboxStyle.addLine(events[i]);
            }
            mBuilder.setStyle(inboxStyle);
        }

        if(notification.getAuditData().getAction().equals(AuditAction.NOTIFICATION_INDIVIDUAL)){
            mBuilder.setContentTitle(globalVariable.getResources().getString(R.string.alert_document_header_individual));
            String[] events = new String[4];
            events[0] = globalVariable.getResources().getString(R.string.alert_content_title_individual);
            events[1] =globalVariable.getResources().getString(R.string.alert_content_custom_from) + notification.getAuditData().getActor().getName();
            events[2] = globalVariable.getResources().getString(R.string.alert_content_custom_text) + notification.getNotificationText();
            events[3] = notification.getAuditData().getTimestamp();

            // Sets a title for the Inbox style big view
            inboxStyle.setBigContentTitle(globalVariable.getResources().getString(R.string.alert_content_big_title_alert_details));

            // Moves events into the big view
            for (int i=0; i < events.length; i++) {
                inboxStyle.addLine(events[i]);
            }
            mBuilder.setStyle(inboxStyle);
        }

        /* Increase notification number every time a new notification arrives */
        mBuilder.setNumber(++numMessages);

        /* Creates an explicit intent for an Activity in your app */
        Intent resultIntent = new Intent(this, HomePageActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(HomePageActivity.class);

        /* Adds the Intent that starts the Activity to the top of the stack */
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        /* notificationID allows you to update the notification later on. */
        mNotificationManager.notify(numMessages-1, mBuilder.build());
    }


}
