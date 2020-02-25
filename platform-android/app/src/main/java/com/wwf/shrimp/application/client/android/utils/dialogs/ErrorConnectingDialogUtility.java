package com.wwf.shrimp.application.client.android.utils.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;

import com.wwf.shrimp.application.client.android.R;

/**
 * Generic Connectivity Dialog
 * Created by AleaActaEst on 25/06/2017.
 */

public class ErrorConnectingDialogUtility {

    public static void showServerSystemErrorDialog(Context context){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);

        // Set up its view
        builder.setTitle(R.string.error_system_general);
        builder.setMessage(R.string.error_system_server_connection);
        builder.setPositiveButton(R.string.dialog_general_OK, null);

        // create alert dialog
        android.support.v7.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00838F"));

    }

    public static void showUnsyncedDocumentErrorDialog(Context context){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);

        // Set up its view
        builder.setTitle(R.string.error_system_general);
        builder.setMessage(R.string.error_system_unsynced_document);
        builder.setPositiveButton(R.string.dialog_general_OK, null);

        // create alert dialog
        android.support.v7.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00838F"));

    }

}
