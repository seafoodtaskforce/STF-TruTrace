package com.wwf.shrimp.application.client.android;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wwf.shrimp.application.client.android.fragments.MyDocumentsFragment;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.DynamicFieldData;
import com.wwf.shrimp.application.client.android.models.dto.DynamicFieldDefinition;
import com.wwf.shrimp.application.client.android.models.dto.TagData;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.InputMask;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Activity used for tagging documents.
 * @author AleaActaEst
 */
public class DocumentDynamicFieldsActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {

    private static final String LOG_TAG = "Doc Dyn Field Activity";
    public static final String PROPERTY_BAG_KEY = "dynamicFieldData";

    // global session data access
    private SessionData globalVariable = null;

    //
    // UI Elements
    Button buttonCancelDynamicFields;
    Button buttonSaveDynamicFields;

    //
    // Dynamic Fields
    TextInputLayout textView001;
    TextInputLayout textView002;
    TextInputLayout textView003;
    TextInputLayout textView004;
    TextInputLayout textView005;


    //
    // Data used in the UI elements


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalVariable  = (SessionData) getApplicationContext();
        setContentView(R.layout.activity_document_info);

        buttonCancelDynamicFields = (Button)findViewById(R.id.buttonCancelDynamicFields);
        buttonSaveDynamicFields = (Button)findViewById(R.id.buttonSaveDynamicFields);

        //
        // Get the field references
        textView001 = (TextInputLayout) findViewById(R.id.dynamicField001Wrapper);
        textView002 = (TextInputLayout) findViewById(R.id.dynamicField002Wrapper);
        textView003 = (TextInputLayout) findViewById(R.id.dynamicField003Wrapper);
        textView004 = (TextInputLayout) findViewById(R.id.dynamicField004Wrapper);
        textView005 = (TextInputLayout) findViewById(R.id.dynamicField005Wrapper);

        initDynamicFields();
        initialiseDynamicFieldDefinitions();
        initialiseDynamicFieldData();
        initialiseUI();
    }

    @Override
    protected void onResume() {

        super.onResume();
        Log.i(LOG_TAG, "Resuming the activity");
    }

    /**
     * This is a listener for the pop-up dialog dismissal
     * @param dialog - the dialog being dismissed
     */
    @Override
    public void onDismiss(final DialogInterface dialog) {
        //Fragment dialog had been dismissed
        Log.i(LOG_TAG, "Dialog Has been dismissed and we are resuming");
    }

    /**
     * Initialize the UI to get the list of fitting dynamic fields
     */
    protected void initialiseDynamicFieldDefinitions() {
        /**
         * initialize the Data fields
         */
        List<DynamicFieldDefinition> dynamicFields = getAllDynamicFields();
        List<DynamicFieldData> dynamicFieldData = getAllDynamicFieldData();


        for(int i=0; i< dynamicFields.size(); i++){
            if(i==0){
                textView001.setVisibility(View.VISIBLE);
                textView001.setHint(dynamicFields.get(i).getDisplayName());
                textView001.setCounterEnabled(true);
                textView001.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView001.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView001.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView001.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView001.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView001.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView001.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView001.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView001.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView001.getEditText()));
                    textView001.setHelperText(dynamicFields.get(i).getDescription() + " " + "MM/DD/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView001.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }

            }
            if(i==1){
                textView002.setVisibility(View.VISIBLE);
                textView002.setHint(dynamicFields.get(i).getDisplayName());
                textView002.setCounterEnabled(true);
                textView002.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView002.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView002.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView002.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView002.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView002.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView002.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView002.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView002.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView002.getEditText()));
                    textView002.setHelperText(dynamicFields.get(i).getDescription() + " " + "MM/DD/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView002.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
            if(i==2){
                textView003.setVisibility(View.VISIBLE);
                textView003.setHint(dynamicFields.get(i).getDisplayName());
                textView003.setCounterEnabled(true);
                textView003.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView003.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView003.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView003.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView003.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView003.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView003.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView003.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView003.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView003.getEditText()));
                    textView003.setHelperText(dynamicFields.get(i).getDescription() + " " + "MM/DD/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView003.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
            if(i==3){
                textView004.setVisibility(View.VISIBLE);
                textView004.setHint(dynamicFields.get(i).getDisplayName());
                textView004.setCounterEnabled(true);
                textView004.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView004.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView004.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView004.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView004.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView004.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView004.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView004.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView004.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView004.getEditText()));
                    textView004.setHelperText(dynamicFields.get(i).getDescription() + " " + "MM/DD/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView004.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
            if(i==4){
                textView005.setVisibility(View.VISIBLE);
                textView005.setHint(dynamicFields.get(i).getDisplayName());
                textView005.setCounterEnabled(true);
                textView005.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView005.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView005.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView005.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView005.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView005.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView005.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView005.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView005.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView005.getEditText()));
                    textView005.setHelperText(dynamicFields.get(i).getDescription() + " " + "MM/DD/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView005.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
        }
    }

    protected void initialiseDynamicFieldData() {
        /**
         * get data from the server about tags
         */
        String url;

    }

    protected void initialiseUI() {
        // buttons
        buttonCancelDynamicFields.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cancel the operation by going back
                onBackPressed();
            }
        });
        buttonSaveDynamicFields.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * Fetch all the values from the fields and add them to the document
                 */

                List<DynamicFieldDefinition> dynamicFields = getAllDynamicFields();

                for(int i=0; i< dynamicFields.size(); i++){
                    if(i==0){
                        if(textView001.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView001.getEditText().getText().toString());
                        }

                    }
                    if(i==1){
                        if(textView002.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView002.getEditText().getText().toString());
                        }
                    }
                    if(i==2){
                        if(textView003.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView003.getEditText().getText().toString());
                        }
                    }
                    if(i==3){
                        if(textView004.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView004.getEditText().getText().toString());
                        }
                    }
                    if(i==4){
                        if(textView005.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView005.getEditText().getText().toString());
                        }
                    }
                }
                 onBackPressed();
            }
        });

    }

    /**************************************************************************
     ****** Private helper methods
     *****************************************************************************/

    /**
     * get all the dynamic fields for the current document
     * @return - list of document fields; empty if none found.
     */
    List<DynamicFieldDefinition> getAllDynamicFields() {
        List<DynamicFieldDefinition> result = new ArrayList<DynamicFieldDefinition>();
        long docTypeId = globalVariable.getNextDocument().getDocumentType().getId();

        for(int i=0; i< globalVariable.getCurrentUser().getDynamicFieldDefinitions().size() ;i++){
            if(globalVariable.getCurrentUser().getDynamicFieldDefinitions().get(i).getDocTypeId() == docTypeId){
                result.add(globalVariable.getCurrentUser().getDynamicFieldDefinitions().get(i));
            }
        }

        return result;
    }


    /**
     * get all the dynamic fields for the current document
     * @return - list of document fields; empty if none found.
     */
    List<DynamicFieldData> getAllDynamicFieldData() {
        List<DynamicFieldData> result = new ArrayList<DynamicFieldData>();

        for(int i=0; i< globalVariable.getNextDocument().getDynamicFieldData().size() ;i++){
            //if(globalVariable.getCurrentUser().getDynamicFieldData().get(i)){
                result.add(globalVariable.getNextDocument().getDynamicFieldData().get(i));
            //}
        }

        return result;
    }

    void initDynamicFields(){
        textView001.setVisibility(View.INVISIBLE);
        textView002.setVisibility(View.INVISIBLE);
        textView003.setVisibility(View.INVISIBLE);
        textView004.setVisibility(View.INVISIBLE);
        textView005.setVisibility(View.INVISIBLE);
    }

}
