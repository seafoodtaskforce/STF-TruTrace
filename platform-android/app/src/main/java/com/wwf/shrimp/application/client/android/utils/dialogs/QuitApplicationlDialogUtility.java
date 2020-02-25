package com.wwf.shrimp.application.client.android.utils.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;

import com.wwf.shrimp.application.client.android.R;

/**
 * Application Quit Generic Dialog
 * Created by AleaActaEst on 14/07/2017.
 */

public class QuitApplicationlDialogUtility {
    public static AlertDialog showCancelDialog(Context context){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);

        // Set up its view
        builder.setTitle(R.string.document_custom_dialog_quit_application_heading);
        builder.setMessage(R.string.document_custom_dialog_quit_application_message);
        builder.setPositiveButton(R.string.document_custom_dialog_quit_app_quit_button, null);
        builder.setNegativeButton(R.string.document_custom_dialog_quit_app_cancel_button, null);

        // create alert dialog
        android.support.v7.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00838F"));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#00838F"));

        return alertDialog;

    }
}
