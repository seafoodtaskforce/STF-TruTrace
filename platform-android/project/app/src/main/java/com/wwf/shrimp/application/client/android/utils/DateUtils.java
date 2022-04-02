package com.wwf.shrimp.application.client.android.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Set of utilities for date to string manipulation
 * @author AleaActaEst
 */
public class DateUtils {
    /**
     * Utilised formats
     */
    public static final String DATE_FORMAT_FULL = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "dd/MM/yy HH:mm";
    public static final String DATE_FORMAT2 = "yyyy-MM-dd HH:mm";
    public static final String DATE_ONLY_FORMAT = "yyyy-MM-dd";
    public static final String DATE_ONLY_FORMAT2 = "dd/MM/yy";


    /**
     * Format a java.util.Date to a string according to the defined pattern
     * @param date - the date to convert
     * @return - the converted string result
     */
    public static String formatDateTimeToString(Date date){
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        return df.format(DATE_FORMAT, date).toString();
    }

    /**
     * Convert a date (without the time component) to a String
     * @param date - the date to convert
     * @return - the converted string version of the date
     */
    public static String formatDateOnlyToString(Date date){
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        return df.format(DATE_ONLY_FORMAT, date).toString();
    }

    public static Date formatStringToDateTime(String dateInString){
        Date result=null;
        SimpleDateFormat formatter=null;

        // System.out.println("Data format: " + dateInString + "contains slash: " + dateInString.contains("/"));

        // figure out the format of the date coming in
        if(dateInString.contains("-")){
            formatter = new SimpleDateFormat(DATE_FORMAT2);
        }else if(dateInString.contains("/")){
            formatter = new SimpleDateFormat(DATE_FORMAT);
        }

        try {
            result = formatter.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String formatFullDateStringToSimpleDateTimeString(String dateInString){
        Date result=null;
        SimpleDateFormat formatter=null;


        // figure out the format of the date coming in
        if(dateInString.contains("-")){
            formatter = new SimpleDateFormat(DATE_FORMAT_FULL);
        }else if(dateInString.contains("/")){
            formatter = new SimpleDateFormat(DATE_FORMAT);
        }
        try {
            result = formatter.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formatDateTimeToString(result);
    }

    public static Date formatStringToDateOnly(String dateInString){
        Date result=null;

        SimpleDateFormat formatter = new SimpleDateFormat(DATE_ONLY_FORMAT);

        // figure out the format of the date coming in
        if(dateInString.contains("-")){
            formatter = new SimpleDateFormat(DATE_ONLY_FORMAT);
        }else if(dateInString.contains("/")){
            formatter = new SimpleDateFormat(DATE_ONLY_FORMAT2);
        }
        try {
            result = formatter.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

}
