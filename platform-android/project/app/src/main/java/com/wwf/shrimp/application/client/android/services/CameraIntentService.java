package com.wwf.shrimp.application.client.android.services;

/**
 * Created by AleaActaEst on 15/06/2017.
 */

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.wwf.shrimp.application.client.android.adapters.DocumentCardItemAdapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


import java.util.Locale;

/**
 * Services associated with camera Intent functionality
 */
public class CameraIntentService {

    // Activity request codes
    public static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public final static int CAMERA_RQ = 6969;
    public static final int PERMISSION_RQ = 84;

    // directory name to store captured images and videos
    public static final String IMAGE_DIRECTORY_NAME = "WWF_POC";

    private Context context;
    private DocumentCardItemAdapter documentCaptureAdapter;



    /**
     * Checking device has camera hardware or not
     *
     */
    private boolean isDeviceSupportCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Creating file uri to store image/video
     */
    public static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /*
     * returning image / video
     */
    public static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                CameraIntentService.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(CameraIntentService.IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + CameraIntentService.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == CameraIntentService.MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static File getMediaDirectoryPath(){
        File saveDir = null;
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        // Only use external storage directory if permission is granted, otherwise cache directory is used by default
        saveDir = new File(Environment.getExternalStorageDirectory(), IMAGE_DIRECTORY_NAME);
        saveDir.mkdirs();
        //
        return saveDir;

        //}

    }

    public static Bitmap getBitmapFromPath(String imagePath){
        // bimatp factory
        BitmapFactory.Options options = new BitmapFactory.Options();

        // downsizing image as it throws OutOfMemory Exception for larger
        // images
        options.inSampleSize = 2;

        final Bitmap bitmap = BitmapFactory.decodeFile(imagePath,
                options);

        return bitmap;
    }

    public DocumentCardItemAdapter getDocumentCaptureAdapter() {
        return documentCaptureAdapter;
    }

    public void setDocumentCaptureAdapter(DocumentCardItemAdapter horizontalAdapter) {
        this.documentCaptureAdapter = horizontalAdapter;
    }

}

