package com.wwf.shrimp.application.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simple date utility for conversion and formatting
 * 
 * @author AleaActaEst
 *
 */
public class DateUtility {
	
	public static int FORMAT_DATE_ONLY = 11;
	public static int FORMAT_DATE_AND_TIME = 13;
	
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_ONLY_FORMAT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_FORMALIZED = "dd/MM/yy HH:mm";
	
	
	/**
	 * Format the date to a simple format as a string
	 * 
	 * @param date - the date to be formatted
	 * @return - the formatted date
	 */
	public static String simpleDateFormat(Date date, int formatOption){
		String result;
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat(DATE_FORMAT);
		
		if(formatOption == FORMAT_DATE_ONLY){
			formatter = new SimpleDateFormat(DATE_ONLY_FORMAT);	
		}else if(formatOption == FORMAT_DATE_AND_TIME){
			formatter = new SimpleDateFormat(DATE_FORMAT);
		}
		
		result = formatter.format(date);
		
		return result;
	}
	
	
	/**
	 * Conversion from a string to a java.util.Date
	 * @param date - the string representation of the date
	 * @param formatOption - the formatting option for the string date
	 * @return - the parsed out Date
	 */
	public static Date simpleDateFormat(String date, int formatOption){
		Date result=null;
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat(DATE_FORMAT);
		
		if(formatOption == FORMAT_DATE_ONLY){
			formatter = new SimpleDateFormat(DATE_ONLY_FORMAT);	
		}else if(formatOption == FORMAT_DATE_AND_TIME){
			formatter = new SimpleDateFormat(DATE_FORMAT);
		}
		
		try {
			result = formatter.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 *  a full date with "/" used as date element separators into a simplified date 
	 * 
	 * @param dateInString - the date to be formatted
	 * @return the formatted date string
	 */
    public static String formatFullDateStringToSimpleDateTimeString(String dateInString){
        Date result=null;
        SimpleDateFormat formatter=null;


        // figure out the format of the date coming in
        if(dateInString.contains("/")){
            formatter = new SimpleDateFormat(DATE_FORMAT_FORMALIZED);
        }
        try {
            result = formatter.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return simpleDateFormat(result, FORMAT_DATE_AND_TIME);
    }
    


	/**
	 * Simple conversion routine for java.util.Date to java.sql.Timestamp
	 * 
	 * @param date - the input date (java.util.Date)
	 * @return - the converted date (java.sql.Timestamp)
	 */
	public static java.sql.Timestamp convertToSQLTimestamp(Date date){
		java.sql.Timestamp sqlDate = new java.sql.Timestamp(date.getTime());

		return sqlDate;
	}
	
	/**
	 * Simple conversion routine for java.sql.Timestamp to java.util.Date
	 * 
	 * @param sqlDate - the input date (java.sql.Timestamp)
	 * @return - the converted date (java.util.Date)
	 */
	public static Date convertFromSQLTimestamp(java.sql.Timestamp sqlDate){
		Date date = new Date(sqlDate.getTime());
		
		return date;
	}

	/**
	 * get the current date and time object
	 * @return - the current  time and date (on the server)
	 */
	public static Date getCurrentDateTime() {
		Date date = new Date();
		return date;
	}


}
