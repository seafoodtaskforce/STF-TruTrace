package com.wwf.shrimp.application.utils;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.wwf.shrimp.application.exceptions.AuthenticationException;

/**
 * De-Serialization of date in the database to proper JSON representation
 * @author argolite
 *
 */
public class DateDeserializer implements JsonDeserializer<Date> {



	/**
	 * Deserialize String into Java Date
	 * 
	 * @param element - The JSON element representing teh date
	 * @param arg1 - not utilized
	 * @param arg2 - not utilized
	 * @return - Deserialized date
	 * @throws JsonParseException 
	 *     - the JSON element cannot be parsed into a date
	 */
	@Override
	public Date deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
		String date = element.getAsString();
		
		SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			System.err.println("Failed to parse Date due to:" + e);
			return null;
		}
	}
}