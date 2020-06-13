package com.wwf.shrimp.application.client.android.utils;

import android.support.v7.app.AppCompatActivity;

import com.wwf.shrimp.application.client.android.system.SessionData;

/**
 * Utilities for camera based processing
 * @author AleaActaEst
 **/
public class CameraVisionProcessingUtils {

    public static boolean isPageLimitReached(SessionData globalVariable, AppCompatActivity caller){
        boolean result= false;

        String resourceStringName = globalVariable.getNextDocument().getDocumentType().getName();
        int resourceId = caller.getResources().
                getIdentifier(resourceStringName, "string", caller.getPackageName());
        Integer allowedCount = globalVariable.getDocumentPagesMap().get(resourceId);
        if(allowedCount != null  && globalVariable.getCurrDocPagesCameraSnapCount() >=  allowedCount){
            result = true;
        }

        return result;
    }
}
