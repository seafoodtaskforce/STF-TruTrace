package com.wwf.shrimp.application.client.android.utils;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for OCR functionality
 *
 * @author AleaActaEst
 */
public class OCRUtils {
    // logging tag
    private static final String LOG_TAG = "OCRUtils";

    public static String getTagFromDocument(SparseArray<TextBlock> textBlocks){
        String result = null;


        // prepare the regex
        // Pattern p = Pattern.compile("^\\d{2}?\\d{3}?\\d{6}?\\d{4}");
        Pattern p = Pattern.compile("\\d{2}-\\d{3}-\\d{6}-\\d{4}");

        /**
         String blocks = "";
         String lines = "";
         String words = "";
         for (int index = 0; index < textBlocks.size(); index++) {
             //extract scanned text blocks here
             TextBlock tBlock = textBlocks.valueAt(index);
             blocks = blocks + tBlock.getValue() + "\n" + "\n";
            for (Text line : tBlock.getComponents()) {
                //extract scanned text lines here
                lines = lines + line.getValue() + "\n";
                 for (Text element : line.getComponents()) {
                     //extract scanned text words here
                     words = words + element.getValue() + ", ";
                }
            }
         }
         if (textBlocks.size() == 0) {
             Log.i(LOG_TAG, "Scan Failed: Found nothing to scan");
         } else {
             Log.i(LOG_TAG, "Scan OCR : " + "---------" + "\n");
             Log.i(LOG_TAG, "Scan OCR Blocks: " + blocks + "\n");
             Log.i(LOG_TAG, "Scan OCR : " + "---------" + "\n");
             Log.i(LOG_TAG, "Scan OCR Lines: " + lines + "\n");
             Log.i(LOG_TAG, "Scan OCR : " + "---------" + "\n");
             Log.i(LOG_TAG, "Scan OCR Words: " + words + "\n");
             Log.i(LOG_TAG, "Scan OCR : " + "---------" + "\n");
         }
         */

        String blocks = "";
        //
        // D0 the actual tag extraction
        for (int index = 0; index < textBlocks.size(); index++) {
            //extract scanned text blocks here
            TextBlock tBlock = textBlocks.valueAt(index);
            blocks = blocks + tBlock.getValue() + "\n" + "\n";
            for (Text line : tBlock.getComponents()) {
                //extract scanned text lines here
                for (Text element : line.getComponents()) {
                    //extract scanned text words here
                    // test the line for a specific regex pattern
                    Matcher m = p.matcher(element.getValue());
                    if(m.matches()){
                        result = element.getValue();
                        return result;
                    }
                }
            }
        }

        return result;
    }
}
