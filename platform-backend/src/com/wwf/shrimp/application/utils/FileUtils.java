package com.wwf.shrimp.application.utils;

import java.io.File;
import java.text.DecimalFormat;

public class FileUtils {
	
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

}
