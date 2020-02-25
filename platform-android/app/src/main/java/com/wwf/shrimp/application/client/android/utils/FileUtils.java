package com.wwf.shrimp.application.client.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

/**
 * Created by AleaActaEst on 17/06/2017.
 *
 * Simple file utilizes for global file related functionality such as
 * determining size etc...
 *
 */
public class FileUtils {
    private static final String LOG_TAG = "FileUtils";

    /**
     * Format the size of a file into a readable string
     * @param size - the input size in bytes
     * @return - the formatted string file size
     */
    public static String readableFileSize(long size) {
        if (size <= 0) return size + " B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Get the size of a file in readable format
     * @param file - the file to get the length of
     * @return - the formatted file size for this file
     */
    public static String fileSize(File file) {

        return FileUtils.readableFileSize(file.length());
    }


    /**
     *
     * @param file
     * @return
     */
    public static File saveBitmapToFile(File file){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to (started with 150 - tried 100)
            final int REQUIRED_SIZE=70;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            Log.d(LOG_TAG, String.format("[BEFORE] Saved to: scale %s",scale));
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {

                scale *= 2;
            }
            Log.d(LOG_TAG, String.format("[AFTER] Saved to: scale %s",scale));
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            /**
             * Do not currently rotate the bitmap
            if(selectedBitmap.getWidth() > selectedBitmap.getHeight()) {
                selectedBitmap = RotateBitmap(selectedBitmap, 90f);
            }
             */

            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            outputStream.flush();
            outputStream.close();

            return file;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *
     * @param file
     * @return
     */
    public static void overrideBitmapToFile(File file, Bitmap processedImage){
        try {
            FileOutputStream outputStream = new FileOutputStream(file);

            processedImage.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            // do something with the error
        }
    }

    private static Bitmap RotateBitmap(Bitmap source, float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap scaleDownBitmap(Bitmap realImage, float maxImageSize,
                                   boolean filter) {

        // The new size we want to scale to
        final int REQUIRED_SIZE=500000;
        maxImageSize = REQUIRED_SIZE;
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    public static Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 500;
        int targetH = 500;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
    }


}
