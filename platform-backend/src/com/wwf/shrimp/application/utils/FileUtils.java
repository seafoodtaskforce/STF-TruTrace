package com.wwf.shrimp.application.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
    
    public static String getWebInfPath(String filename, Object caller) throws UnsupportedEncodingException {
    	String path = caller.getClass().getClassLoader().getResource("").getPath();
    	String fullPath = URLDecoder.decode(path, "UTF-8");
    	String pathArr[] = fullPath.split("/WEB-INF/classes/");
    	System.out.println(fullPath);
    	System.out.println(pathArr[0]);
    	fullPath = pathArr[0];

    	String reponsePath = "";
    	// to read a file from webcontent
    	reponsePath = new File(fullPath).getPath() + File.separatorChar + filename;
    	return reponsePath;
    }

}
