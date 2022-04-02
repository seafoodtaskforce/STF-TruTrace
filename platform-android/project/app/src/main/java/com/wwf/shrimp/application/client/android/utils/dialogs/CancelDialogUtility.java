package com.wwf.shrimp.application.client.android.utils.dialogs;

import android.content.Context;
import android.graphics.Color;
import androidx.appcompat.app.AlertDialog;

import com.wwf.shrimp.application.client.android.R;

/**
 * Generic Cancel Dialog
 * Created by AleaActaEst on 14/07/2017.
 */

public class CancelDialogUtility {
    public static AlertDialog showCancelDialog(Context context){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);

        // Set up its view
        builder.setTitle(R.string.document_custom_dialog_cancel_heading);
        builder.setMessage(R.string.document_custom_dialog_cancel_message);
        builder.setPositiveButton(R.string.document_custom_dialog_cancel_yes, null);
        builder.setNegativeButton(R.string.document_custom_dialog_cancel_no, null);

        // create alert dialog
        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00838F"));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#00838F"));

        return alertDialog;

    }
}
