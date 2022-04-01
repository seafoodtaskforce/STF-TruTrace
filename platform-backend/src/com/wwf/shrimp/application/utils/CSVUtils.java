package com.wwf.shrimp.application.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CSVUtils {
	
	/**
	 * Get the byte output stream for the CSV file stream input
	 * @param importDocCSVStream
	 * @return
	 */
	public static byte[] getCSVBytesFromInputStream(InputStream importDocCSVStream) {
		byte [] buffer = new byte[4096];
        ByteArrayOutputStream outs = new ByteArrayOutputStream();
        
        int read = 0;
        try {
			while ((read = importDocCSVStream.read(buffer)) != -1 ) {
			  outs.write(buffer, 0, read);
			}
			
			importDocCSVStream.close();
			outs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        return outs.toByteArray();
	}

}
