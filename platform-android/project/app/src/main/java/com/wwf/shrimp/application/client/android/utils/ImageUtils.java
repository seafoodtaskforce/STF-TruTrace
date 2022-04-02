package com.wwf.shrimp.application.client.android.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.wwf.shrimp.application.client.android.models.view.GalleryDocumentPage;
import com.wwf.shrimp.application.client.android.system.SessionData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Utilities for managing image functionality
 * Created by AleaActaEst on 01/07/2017.
 */

public class ImageUtils {

    public static final String IMAGE_DIRECTORY = "/truetrace";

    public static int getImageOrientation(Context context, String imagePath) throws IOException {
        int orientation = getOrientationFromExif(imagePath);
        if(orientation <= 0) {
            // orientation = getOrientationFromMediaStore(context, imagePath);
            orientation = getOrientationFromExifByBitmapDimensions(imagePath);
        }

        return orientation;
    }

    /**
     * Save an image (from attachment) on the phone external storage.
     * @param myBitmap - the image being stored
     * @param globalVariable - session data
     * @param context - the context for the application
     * @return - the path to the file that was saved or empty string if no success
     */
    public static String saveImage(Bitmap myBitmap, SessionData globalVariable, Context context){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        String galleryPrefix = globalVariable.getConfigurationData().getGlobalConstantGalleryImageRemovalPrefix();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 65, bytes);
        File externalStorageDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!externalStorageDirectory.exists()) {
            externalStorageDirectory.mkdirs();
        }

        try {
            File f = new File(externalStorageDirectory, galleryPrefix + Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(context,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved to::---&gt;" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    public static void deleteAllImages(){
        File externalStorageDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!externalStorageDirectory.exists()) {
            return;
        }

        //
        // delete all files
        try {
            FileUtils.cleanDirectory(externalStorageDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeCachedScannerImages(SessionData globalVariable){
        Iterator<GalleryDocumentPage> iter = globalVariable.getNextDocument().getImagePages().iterator();
        while(iter.hasNext()){
            GalleryDocumentPage page = iter.next();
            if(page.getPage() instanceof File){
                if(((File) page.getPage()).getAbsolutePath().contains(IMAGE_DIRECTORY)){
                    // Do nothing
                }else{
                    // remove everything else <TODO> was removed to facilitate proper collation
                    //iter.remove();
                }
            }
        }
        //
        // Re-collate the pages in the document
        // globalVariable.getNextDocument().collatePages();
        // <TODO> Collation

    }

    private static int getOrientationFromExif(String imagePath) throws IOException{
        int orientation = -1;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            ShowExif(exif);

            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = 270;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = 180;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = 90;

                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                    orientation = 0;

                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            System.out.println("Unable to get image exif orientation" + e);
        }

        return orientation;
    }

    private static int getOrientationFromMediaStore(Context context, String imagePath) {
        Uri imageUri = getImageContentUri(context, imagePath);
        if(imageUri == null) {
            return -1;
        }

        String[] projection = {MediaStore.Images.ImageColumns.ORIENTATION};
        Cursor cursor = context.getContentResolver().query(imageUri, projection, null, null, null);

        int orientation = -1;
        if (cursor != null && cursor.moveToFirst()) {
            orientation = cursor.getInt(0);
            cursor.close();
        }

        return orientation;
    }

    private static Uri getImageContentUri(Context context, String imagePath) {
        String[] projection = new String[] {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.DATA + "=? ";
        String[] selectionArgs = new String[] {imagePath};
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                selection, selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            int imageId = cursor.getInt(0);
            cursor.close();

            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(imageId));
        }

        if (new File(imagePath).exists()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, imagePath);

            return context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }

        return null;
    }

    public static Boolean uploadFile(String serverURL, String uploadProfileFilePath, SessionData globalVariable) {
        OkHttpClient client = new OkHttpClient();
        try {

            File sourceFile;
            sourceFile = new File(uploadProfileFilePath);

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", sourceFile.getName(),
                            RequestBody.create(MediaType.parse("image/jpeg"), sourceFile))
                    .addFormDataPart("userName", globalVariable.getCurrentUser().getName())
                    .build();

            Request request = new Request.Builder()
                    .url(serverURL)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(final Call call, final IOException e) {
                    // Handle the error
                }

                @Override
                public void onResponse(final Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        // Handle the error
                    }
                    // Upload successful
                }
            });

            return true;
        } catch (Exception ex) {
            // Handle the error
        }
        return false;
    }

    private static void ShowExif(ExifInterface exif)
    {
        String myAttribute="Exif information ---\n";
        myAttribute += getTagString(ExifInterface.TAG_DATETIME, exif);
        myAttribute += getTagString(ExifInterface.TAG_FLASH, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE_REF, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE_REF, exif);
        myAttribute += getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif);
        myAttribute += getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif);
        myAttribute += getTagString(ExifInterface.TAG_MAKE, exif);
        myAttribute += getTagString(ExifInterface.TAG_MODEL, exif);
        myAttribute += getTagString(ExifInterface.TAG_ORIENTATION, exif);
        myAttribute += getTagString(ExifInterface.TAG_WHITE_BALANCE, exif);
        System.out.println("Exif Data: " + myAttribute);
    }

    private static String getTagString(String tag, ExifInterface exif){
        return(tag + " : " + exif.getAttribute(tag) + "\n");
    }

    private static int getOrientationFromExifByBitmapDimensions(String imagePath) throws IOException{
        int orientation = -1;
        try {
            ExifInterface exif = new ExifInterface(imagePath);

            if(Integer.parseInt(exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)) < Integer.parseInt(exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH))){
                orientation = 90;
            }else{
                orientation = 0;
            }

        } catch (IOException e) {
            System.out.println("Unable to get image exif orientation" + e);
        }

        return orientation;
    }
}
