package com.wwf.shrimp.application.client.android.utils;

import java.util.regex.Pattern;

public class EmailUtils {

    /**
     * Check the validity of the email address in terms of syntax
     *
     * @param emailAddress - the date to be formatted
     * @return - true if the email has a valid format; false otherwise
     */
    public static boolean isValid(String emailAddress){
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (emailAddress == null)
            return false;
        return pat.matcher(emailAddress).matches();
    }
}
