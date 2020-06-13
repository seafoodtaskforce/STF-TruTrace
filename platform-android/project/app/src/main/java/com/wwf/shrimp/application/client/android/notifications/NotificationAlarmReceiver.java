package com.wwf.shrimp.application.client.android.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import okhttp3.MediaType;

/**
 * Created by argolite on 22/03/2018.
 */

/**
 * Notification receiver
 * <TODO> Replace with server PUSH version </TODO>
 */
public class NotificationAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.codepath.example.servicesdemo.alarm";
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            Intent i = new Intent(context, NotificationService.class);
            i.putExtra("foo", "bar");
            context.startService(i);

        }catch(Exception e){
            Toast.makeText(context, "[Communication] Notification Issue Caught. Restarting Notification Manager.", Toast.LENGTH_SHORT).show();
        }

    }
}
