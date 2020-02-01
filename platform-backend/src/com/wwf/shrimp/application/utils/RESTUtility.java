package com.wwf.shrimp.application.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * REST helper methods specific to JSON related functionalities
 * @author AleaActaEst
 *
 */
public class RESTUtility {
	
	/**
	 * A custom GSON builder
	 */
	public static final Gson customGson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class,
            new ByteArrayToBase64TypeAdapter()).create();
	
	/**
	 * Utility method for getting a JSON string from a POJO
	 * @param object - the object to stringify
	 * @return - the JSON representation of the object
	 */
	public static String getJSON(Object object){
		
		String json = new GsonBuilder()
	               .setDateFormat("YYYY-MM-DD HH:MM:SS")
	               .create()
	               .toJson(object);
		return json;
	}
	
	/**
	 * Utility method for getting a JSON string from a POJO with a specific type
	 * @param object - the object to stringify
	 * @type - the type to use
	 * @return - the JSON representation of the object
	 */
	public static String getJSON(Object object, Type type){
		
		String json = new GsonBuilder()
	               .setDateFormat("YYYY-MM-DD HH:MM:SS")
	               .create()
	               .toJson(object, type);
		return json;
	}
	
	/**
	 * Utility method for GSON builder
	 * @return - a new GSON builder
	 */
	public static Gson getGson(){
		
		Gson gson = new GsonBuilder()
	            .setDateFormat("YYYY-MM-DD HH:MM:SS")
	            .create();
		return gson;
	}
	
	

	/**
	 * Base64 conversion.
	 * Using Android's base64 libraries. This can be replaced with any base64 library.
	 * @author AleaActaEst
	 *
	 */
    private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
    	
    	/**
    	 * De-serialize a JSON element
    	 */
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Base64.decodeBase64(json.getAsString());
        }

        /**
         * Serialize a byte array into a JSON element
         */
        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            Base64 base64 = new Base64();
            String encodedVersion = new String(base64.encode(src));

            return new JsonPrimitive(encodedVersion);
        }
    }
    
    /**
     * Utility for creation of a blob (binary object) for a given file
     * @param filePath - path to the file data
     * @return - the file as a string of bytes
     * @throws IOException
     * 		- if there were any issues with obtaining the data from the file
     */
    public static byte[] convertFileContentToBlob(String filePath) throws IOException {
    	// create file object
    	File file = new File(filePath);
    	// initialize a byte array of size of the file
    	byte[] fileContent = new byte[(int) file.length()];
    	FileInputStream inputStream = null;
    	try {
    		// create an input stream pointing to the file
    		inputStream = new FileInputStream(file);
    		// read the contents of file into byte array
    		inputStream.read(fileContent);
    	} catch (IOException e) {
    		throw new IOException("Unable to convert file to byte array. " + e.getMessage());
    	} finally {
    		// close input stream
    		if (inputStream != null) {
    			inputStream.close();
    		}
    	}
    	return fileContent;
    }
}
