package com.wwf.shrimp.application.client.android.utils;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.wwf.shrimp.application.client.android.DocumentTaggingActivity;
import com.wwf.shrimp.application.client.android.fragments.AllDocumentsFragment;
import com.wwf.shrimp.application.client.android.fragments.MyDocumentsFragment;
import com.wwf.shrimp.application.client.android.fragments.ProfileDocumentsFragment;
import com.wwf.shrimp.application.client.android.listeners.ConnectivityReceiver;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.DocumentPage;
import com.wwf.shrimp.application.client.android.models.dto.NoteData;
import com.wwf.shrimp.application.client.android.models.dto.TagData;
import com.wwf.shrimp.application.client.android.models.dto.User;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.models.view.GalleryDocumentPage;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.opennotescanner.helpers.Utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.apache.commons.codec.binary.Base64;

/**
 * Collection of REST utility methods
 * @author AleaActaEst
 */
public class RESTUtils {
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    public static final String REST_HEADER_USER_NAME = "user-name";
    public static final String REST_HEADER_DOC_ID = "doc-id";

    private static final String LOG_TAG = "Doc Tagging Activity";

    public static final Gson customGson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class,
            new ByteArrayToBase64TypeAdapter()).create();

    /**
     * Convert the input object into a JSON Strings
     * @param object - the object to convert into JSON
     * @return - the String (JSON) representation of the object input
     */
    public static String getJSON(Object object){

        // convert to JSON
        String json = new GsonBuilder()
                // .setDateFormat("YYYY-MM-DD HH:MM:SS")
                .create()
                .toJson(object);

        return json;
    }

    /**
     * Convert the JSON string representation to the specific object in the named class type
     * @param json - the JSON string to convert
     * @param clazz - the class type to convert the JSON string to.
     * @return - the converted object of type clazz
     */
    public static Object getObjectFromJSON(String json, Class clazz){
        Gson gson = new Gson();
        Object object = gson.fromJson(json, clazz);

        return object;
    }

    /**
     * Execute the given POST request with the provided data input
     * @param postUrl - the URL to execute the query against
     * @param postBody - the body part of the HTTP request
     * @param globalData - any session data needed by the request
     * @param propertyBagKey - any properties passed to the request
     * @throws IOException
     *      - if there were any issues with the request execution
     */
    public static void executePOSTRequest(String postUrl, String postBody, final SessionData globalData, final String propertyBagKey) {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, postBody);

        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // remove this doc from the stack
                globalData.popNextDocument();
                if(e instanceof SocketTimeoutException){
                    Log.d("POST Request","Created new Document: Socket Time out. Please try again.");
                }
                Log.d("POST Request","Created new Document: FAILURE: ");
                //
                // Let the user know
                Toast.makeText(globalData, "Something Went Wrong. Unable to Create the Document. Please try again.", Toast.LENGTH_SHORT).show();

                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-mm-dd hh:mm:ss")
                        .create();

                if(!response.isSuccessful()){
                    //
                    // Let the user know
                    Toast.makeText(globalData, "Something Went Wrong. Unable to Create the Document. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // delete all and any other images
                //
                DocumentPOJOUtils.removeGalleryFiles(globalData);
                ImageUtils.deleteAllImages();

                Document newDoc = gson.fromJson(response.body().string(), Document.class);
                // post process
                DocumentPOJOUtils.postProcessDocumentAddition(globalData, newDoc, propertyBagKey,true);
            }
        });
    }

    /**
     * Execute the given POST request with the provided data input
     * @param postUrl - the URL to execute the query against
     * @param postBody - the body part of the HTTP request
     * @param globalData - any session data needed by the request
     * @param propertyBagKey - any properties passed to the request
     * @throws IOException
     *      - if there were any issues with the request execution
     */
    public static void executePOSTDocumentUpdateRequest(String postUrl, String postBody, final SessionData globalData, final String propertyBagKey){
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, postBody);

        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // remove this doc from the stack
                globalData.popNextDocument();
                if(e instanceof SocketTimeoutException){
                    Log.d("POST Request","Update existing Document: Socket Time out. Please try again.");
                }
                Log.d("POST Request","Updated Document: FAILURE: ");
                //
                // Let the user know
                Toast.makeText(globalData, "Something Went Wrong. Unable to Update the Document. Please try again.", Toast.LENGTH_SHORT).show();
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if(!response.isSuccessful()){
                    //
                    // Let the user know
                    Toast.makeText(globalData, "Something Went Wrong. Unable to Update the Document. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // delete all and any other images
                //
                DocumentPOJOUtils.removeGalleryFiles(globalData);
                ImageUtils.deleteAllImages();

                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-mm-dd hh:mm:ss")
                        .create();

                Document newDoc = gson.fromJson(response.body().string(), Document.class);
                // post process
                DocumentPOJOUtils.postProcessDocumentUpdation(globalData, newDoc, propertyBagKey,true);
            }
        });
    }


    /**
     * Execute the given POST request with the provided data input
     * @param postUrl - the URL to execute the query against
     * @param postBody - the body part of the HTTP request
     * @param globalData - any session data needed by the request
     * @throws IOException
     *      - if there were any issues with the request execution
     */
    public static void executePOSTDocumentSetLocationRequest(String postUrl, String postBody, final SessionData globalData, final String sessionId, String gpsLocation) {
        OkHttpClient client = new OkHttpClient();

        if(postBody == null) {
            postBody = "";
        }
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, postBody);

        // add the parameters to the URL
        //

        // Doc session id
        postUrl = addURLParameter(postUrl, "doc_id", sessionId);

        // location
        postUrl = addURLParameter(postUrl, "gps_location", gpsLocation);

        Log.i("POST Request","Changed Document GPS Location POST URL: " + postUrl);


        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(e instanceof SocketTimeoutException){
                    Log.d("POST Request","Changed Document GPS Location: Socket Time out. Please try again.");
                }
                Log.d("POST Request","Changed Document GPS Location: FAILURE: ");
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //Ignore
            }
        });
    }

    /**
     * Execute the given POST request with the provided data input
     * @param postUrl - the URL to execute the query against
     * @param postBody - the body part of the HTTP request
     * @param globalData - any session data needed by the request
     * @param propertyBagKey - any properties passed to the request
     * @throws IOException
     *      - if there were any issues with the request execution
     */
    public static void executePOSTDocumentSetStatusRequest(String postUrl, String postBody, final SessionData globalData, final String propertyBagKey) {
        OkHttpClient client = new OkHttpClient();

        if(postBody == null) {
            postBody = "";
        }
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, postBody);

        // add the parameters to the URL
        //

        // add status
        postUrl = addURLParameter(postUrl, "doc_status", globalData.getNextDocument().getStatus());

        // add user
        postUrl = addURLParameter(postUrl, "user_name", globalData.getCurrentUser().getName());

        // Doc session id
        postUrl = addURLParameter(postUrl, "doc_id", globalData.getNextDocument().getSyncID());

        Log.i("POST Request","Changed Document Status POST URL: " + postUrl);


        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // remove this doc from the stack
                globalData.popNextDocument();
                if(e instanceof SocketTimeoutException){
                    Log.d("POST Request","Changed Document Status: Socket Time out. Please try again.");
                }
                Log.d("POST Request","Changed Document Status: FAILURE: ");
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Fragment parentFragment;
                DocumentCardItem item;

                item = globalData.getNextDocument();
                parentFragment = (globalData.getCurrFragment());

                if(globalData.getCurrFragment() instanceof AllDocumentsFragment){
                    int changedIndex = ((AllDocumentsFragment)parentFragment).getAdapter().replaceCardItem(item);
                    ((AllDocumentsFragment)parentFragment).refreshListAtIndex(changedIndex);
                }
                globalData.popNextDocument();
            }
        });
    }

    /**
     * Execute the given POST request with the provided data input
     * @param postUrl - the URL to execute the query against
     * @param postBody - the body part of the HTTP request
     * @param globalData - any session data needed by the request
     * @param propertyBagKey - any properties passed to the request
     * @throws IOException
     *      - if there were any issues with the request execution
     */
    public static void executePOSTDocumentSyncRequest(String postUrl, String postBody, final SessionData globalData, final String propertyBagKey)  {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, postBody);

        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // remove this doc from the stack
                globalData.popNextDocument();
                if(e instanceof SocketTimeoutException){
                    Log.d("POST Request","Created new Document: Socket Time out. Please try again.");
                }
                Log.d("POST Request","Created new Document: FAILURE: ");
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-mm-dd hh:mm:ss")
                        .create();

                Document newDoc = gson.fromJson(response.body().string(), Document.class);
                // post process
                DocumentPOJOUtils.postProcessDocumentAdditionSync(globalData, newDoc, propertyBagKey);
            }
        });
    }

    /**
     * Execute the given POST request with the provided data input
     * @param postUrl - the URL to execute the query against
     * @param postBody - the body part of the HTTP request
     * @param globalData - any session data needed by the request
     * @param propertyBagKey - any properties passed to the request
     * @throws IOException
     *      - if there were any issues with the request execution
     */
    public static void executePOSTDocumentAddPagesRequest(String postUrl, String postBody, final SessionData globalData, final String propertyBagKey)  {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, postBody);

        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();

        // delete all and any other images
        //
        ArrayList<String> galleryFiles = new ArrayList<>();
        galleryFiles = new Utils(globalData.getApplicationContext()).getFilePaths(null);
        for (int i = 0; i < galleryFiles.size(); i++) {
            new File(galleryFiles.get(i)).delete();
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // remove this doc from the stack
                globalData.popNextDocument();
                if(e instanceof SocketTimeoutException){
                    Log.d("POST Request","Created new Document Pages: Socket Time out. Please try again.");
                }
                Log.d("POST Request","Created new Document Pages: FAILURE: ");
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //
                //
                ArrayList<DocumentPage> documentPages;
                //
                // GSON parser
                Gson gson = new GsonBuilder()
                        .create();

                Type listType = new TypeToken<ArrayList<DocumentPage>>(){}.getType();
                documentPages = gson.fromJson(response.body().string(), listType);

                globalData.getNextDocument().getImagePages().clear();
                for(int i =0; i<documentPages.size(); i++){
                    documentPages.get(i).setPageSynced(true);

                    globalData.getNextDocument().getDocumentPages().add(new GalleryDocumentPage(documentPages.get(i)));
                }

                globalData.getNextDocument().setBackendSynced(true);

                // globalData.getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY).findCardItemBySyncId(newDoc.getSyncID()).setBackendSynced(true);
                Fragment parentFragment = (globalData.getCurrFragment());


                if(globalData.getCurrFragment() instanceof MyDocumentsFragment){
                    if(((MyDocumentsFragment)parentFragment).getAdapter() != null){
                        int changedIndex = ((MyDocumentsFragment)parentFragment).getAdapter().replaceCardItem(globalData.getNextDocument());
                        ((MyDocumentsFragment)parentFragment).refreshListAtIndex(changedIndex);
                    }else{

                        //int changedIndex = globalData.getDocumentAdapterMap().get(MyDocumentsFragment.RECYCLER_ADAPTER_KEY).replaceCardItem(globalData.getNextDocument());
                        //((MyDocumentsFragment)parentFragment).refreshListAtIndex(changedIndex);
                    }

                }
                if(globalData.getCurrFragment() instanceof ProfileDocumentsFragment){
                    int changedIndex = ((ProfileDocumentsFragment)parentFragment).getAdapter().replaceCardItem(globalData.getNextDocument());
                    ((ProfileDocumentsFragment)parentFragment).refreshListAtIndex(changedIndex);
                }

                // remove this doc from the stack
                globalData.popNextDocument();
            }
        });
    }

    /**
     * Delete all the pages in the deleted list
     * @param deletePagesUrl
     * @param deleteID
     * @param globalData
     * @param propertyBagKey
     */
    public static void executeDELETEDocumentPagesRequest(String deletePagesUrl, String deleteID, final SessionData globalData, final String propertyBagKey) {
        OkHttpClient client = new OkHttpClient();

        //
        // get the ids to be deleted
        Iterator iter = globalData.getNextDocument().getDocumentPages().iterator();
        List<Long> deletePagesIDs = new ArrayList<Long>();
        while(iter.hasNext()){
            GalleryDocumentPage page = (GalleryDocumentPage)iter.next();
            if(page.isDeleted()){
                deletePagesIDs.add(Long.valueOf( ((DocumentPage)page.getPage()).getId()));
            }
        }
        long[] longArray = convertToPrimites(deletePagesIDs.toArray());
        String deleteBody = RESTUtils.getJSON(longArray);

        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, deleteBody);
        Request request = new Request.Builder().url(deletePagesUrl + "/" + deleteID).delete(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG",response.body().string());
                // place the data in the session queue
                // globalData.getPropertyBag().put(propertyBagKey, response.body().string());
            }
        });
    }

    public static void executeDELETERequest(String deleteUrl, String deleteID, final SessionData globalData, final String propertyBagKey) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(deleteUrl + "/" + deleteID).delete().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG",response.body().string());
                // place the data in the session queue
                // globalData.getPropertyBag().put(propertyBagKey, response.body().string());
            }
        });
    }

    public static void executePOSTDocumentReadRequest(String markAsReadUrl, String documentId, String username, final SessionData globalData, final String propertyBagKey) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, "");

        Request request = new Request.Builder().url(markAsReadUrl + "?" + "doc_id=" + documentId + "&" + "user_name=" + username)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG",response.body().string());
                // place the data in the session queue
                // globalData.getPropertyBag().put(propertyBagKey, response.body().string());
            }
        });
    }

    public static void executePOSTDocumentSyncTagAttachRequest(String attachUrl, String postBody, String documentSyncId, String username, final SessionData globalData, final String propertyBagKey) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, postBody);

        Request request = new Request.Builder().url(attachUrl + documentSyncId)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                call.cancel();
                if(globalData.getDocumentCreationStatus() == SessionData.DOCUMENT_CONTEXT_EDIT){
                    globalData.popNextDocument();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG",response.body().string());
                // place the data in the session queue

                //
                // Find all documents linked by the custom tags
                DocumentPOJOUtils.getLinkedDocsByTags(globalData);

            }
        });
    }

    public static void executePOSTDocumentSyncDocumentLinkAttachRequest(String attachUrl, String postBody, String documentSyncId, String username, final SessionData globalData, final String propertyBagKey) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, postBody);

        Request request = new Request.Builder().url(attachUrl + documentSyncId)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG",response.body().string());
                // place the data in the session queue
                // globalData.getPropertyBag().put(propertyBagKey, response.body().string());
            }
        });
    }

    public static void executePOSTDocumentSyncDocumentAddRecipientsRequest(String attachUrl, String postBody, String documentSyncId, String username, final SessionData globalData, final String propertyBagKey) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, postBody);

        Request request = new Request.Builder().url(attachUrl + documentSyncId)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG",response.body().string());
                // place the data in the session queue
                // globalData.getPropertyBag().put(propertyBagKey, response.body().string());
            }
        });
    }

    public static void executePOSTDocumentSyncDocumentLinkAttachmentRequest(String attachUrl, String postBody, String documentSyncId, String username, final SessionData globalData, final String propertyBagKey) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, postBody);

        Request request = new Request.Builder().url(attachUrl + documentSyncId)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG",response.body().string());
                // place the data in the session queue
                // globalData.getPropertyBag().put(propertyBagKey, response.body().string());
            }
        });
    }

    public static void executePOSTTagCreationRequest(String tagCreateURL, String postBody, String documentSyncId, String username, final SessionData globalData, final String propertyBagKey, final AppCompatActivity parent) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, postBody);

        Request request = new Request.Builder().url(tagCreateURL)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-mm-dd hh:mm:ss")
                        .create();

                TagData newTag = gson.fromJson(response.body().string(), TagData.class);
                // place the data in the session queue
                globalData.getPropertyBag().put(propertyBagKey, newTag);
                Log.d("TAG",globalData.getPropertyBag().get(propertyBagKey).toString());
                ((DocumentTaggingActivity)parent).addNewTag(newTag);
            }
        });
    }


    public static void executePOSTNoteCreationRequest(String docNoteCreateURL, String postBody, String documentSyncId, String username, final SessionData globalData, final String propertyBagKey, final AppCompatActivity parent) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, postBody);

        Request request = new Request.Builder().url(docNoteCreateURL)
                .addHeader(RESTUtils.REST_HEADER_USER_NAME, globalData.getCurrentUser().getName())
                .addHeader(RESTUtils.REST_HEADER_DOC_ID, Long.toString(globalData.getNextDocument().getId()))
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-mm-dd hh:mm:ss")
                        .create();

                NoteData newNote = gson.fromJson(response.body().string(), NoteData.class);
                // place the data in the session queue
                // add the notes to the document
                globalData.getNextDocument().getNotes().clear();
                globalData.getNextDocument().getNotes().add(newNote);

                Fragment parentFragment;
                DocumentCardItem item;

                item = globalData.getNextDocument();
                parentFragment = (globalData.getCurrFragment());

                if(globalData.getCurrFragment() instanceof AllDocumentsFragment){
                    int changedIndex = ((AllDocumentsFragment)parentFragment).getAdapter().replaceCardItem(item);
                    ((AllDocumentsFragment)parentFragment).refreshListAtIndex(changedIndex);
                }
                globalData.popNextDocument();

            }
        });
    }



    public static void executePOSTUserUpdateRequest(String tagCreateURL, String postBody, String documentSyncId, String username, final SessionData globalData, final String propertyBagKey, final AppCompatActivity parent) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, postBody);

        Log.e(LOG_TAG, "REST - update Password - call <data>: " + body.toString());

        Request request = new Request.Builder().url(tagCreateURL)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    public static String addURLParameter(String url, String name, String value){
        if(!url.contains("?")){
            url+="?";
        }

        String param=null;
        try {
            param = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if(url.endsWith("?")){
            url += name + "=" + param;
        }else{
            url += "&" + name + "=" + param;
        }

        return url;
    }

    public static long[] convertToPrimites(Object[] data){
        long[] primitives = new long[data.length];
        for (int i = 0; i < data.length; i++)
            primitives[i] = (Long)data[i];

        return primitives;
    }


    /***********************************************************************************************
     * Private Helper Methods
     */


    // Using Android's base64 libraries. This can be replaced with any base64 library.
    private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            System.out.println("BYTE Serialization");
            Base64 base64 = new Base64();
            return base64.decodeBase64(json.getAsString());
        }

        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            System.out.println("BYTE DE-Serialization");
            Base64 base64 = new Base64();
            String encodedVersion = new String(base64.encode(src));

            return new JsonPrimitive(encodedVersion);
        }
    }

    // Method to manually check connection status
    public static boolean checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        return isConnected;
    }
}
