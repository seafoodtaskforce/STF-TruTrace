package com.wwf.shrimp.application.client.android;

import android.content.DialogInterface;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;

import com.wwf.shrimp.application.client.android.models.dto.DynamicFieldData;
import com.wwf.shrimp.application.client.android.models.dto.DynamicFieldDefinition;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.utils.InputMask;

import java.util.ArrayList;
import java.util.List;


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
    TextInputLayout textView006;
    TextInputLayout textView007;
    TextInputLayout textView008;
    TextInputLayout textView009;
    TextInputLayout textView010;
    TextInputLayout textView011;
    TextInputLayout textView012;
    TextInputLayout textView013;
    TextInputLayout textView014;
    TextInputLayout textView015;
    TextInputLayout textView016;
    TextInputLayout textView017;
    TextInputLayout textView018;
    TextInputLayout textView019;
    TextInputLayout textView020;


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
        // is the document in edit or view mode?
        if(globalVariable.isDocumentEditableFlag() == false) {
            // do not allow edits
            this.buttonSaveDynamicFields.setVisibility(View.INVISIBLE);
        }
        //
        // Get the field references
        textView001 = (TextInputLayout) findViewById(R.id.dynamicField001Wrapper);
        textView002 = (TextInputLayout) findViewById(R.id.dynamicField002Wrapper);
        textView003 = (TextInputLayout) findViewById(R.id.dynamicField003Wrapper);
        textView004 = (TextInputLayout) findViewById(R.id.dynamicField004Wrapper);
        textView005 = (TextInputLayout) findViewById(R.id.dynamicField005Wrapper);
        textView006 = (TextInputLayout) findViewById(R.id.dynamicField006Wrapper);
        textView007 = (TextInputLayout) findViewById(R.id.dynamicField007Wrapper);
        textView008 = (TextInputLayout) findViewById(R.id.dynamicField008Wrapper);
        textView009 = (TextInputLayout) findViewById(R.id.dynamicField009Wrapper);
        textView010 = (TextInputLayout) findViewById(R.id.dynamicField010Wrapper);
        textView011 = (TextInputLayout) findViewById(R.id.dynamicField011Wrapper);
        textView012 = (TextInputLayout) findViewById(R.id.dynamicField012Wrapper);
        textView013 = (TextInputLayout) findViewById(R.id.dynamicField013Wrapper);
        textView014 = (TextInputLayout) findViewById(R.id.dynamicField014Wrapper);
        textView015 = (TextInputLayout) findViewById(R.id.dynamicField015Wrapper);
        textView016 = (TextInputLayout) findViewById(R.id.dynamicField016Wrapper);
        textView017 = (TextInputLayout) findViewById(R.id.dynamicField017Wrapper);
        textView018 = (TextInputLayout) findViewById(R.id.dynamicField018Wrapper);
        textView019 = (TextInputLayout) findViewById(R.id.dynamicField019Wrapper);
        textView020 = (TextInputLayout) findViewById(R.id.dynamicField020Wrapper);

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

    @Override
    public void onBackPressed() {
        Log.i(LOG_TAG, "Dialog Has been dismissed via back-pressing");
        globalVariable.setDocumentEditableFlag(true);
        super.onBackPressed();
    }

    /**
     * This is a listener for the pop-up dialog dismissal
     * @param dialog - the dialog being dismissed
     */
    @Override
    public void onDismiss(final DialogInterface dialog) {
        //Fragment dialog had been dismissed
        Log.i(LOG_TAG, "Dialog Has been dismissed and we are resuming");
        globalVariable.setDocumentEditableFlag(true);
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
                    textView001.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView001.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView001.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView001.getEditText()));
                    textView001.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
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
                    textView002.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView002.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView002.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView002.getEditText()));
                    textView002.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
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
                    textView003.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView003.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView003.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView003.getEditText()));
                    textView003.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
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
                    textView004.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView004.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView004.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView004.getEditText()));
                    textView004.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
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
                    textView005.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView005.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView005.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView005.getEditText()));
                    textView005.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView005.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
            if(i==5){
                textView006.setVisibility(View.VISIBLE);
                textView006.setHint(dynamicFields.get(i).getDisplayName());
                textView006.setCounterEnabled(true);
                textView006.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView006.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView006.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView006.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView006.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView006.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView006.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView006.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView006.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView006.getEditText()));
                    textView006.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView006.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView006.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView006.getEditText()));
                    textView006.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView006.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
            if(i==6){
                textView007.setVisibility(View.VISIBLE);
                textView007.setHint(dynamicFields.get(i).getDisplayName());
                textView007.setCounterEnabled(true);
                textView007.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView007.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView007.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView007.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView007.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView007.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView007.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView007.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView007.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView007.getEditText()));
                    textView007.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView007.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView007.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView007.getEditText()));
                    textView007.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView007.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
            if(i==7){
                textView008.setVisibility(View.VISIBLE);
                textView008.setHint(dynamicFields.get(i).getDisplayName());
                textView008.setCounterEnabled(true);
                textView008.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView008.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView008.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView008.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView008.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView008.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView008.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView008.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView008.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView008.getEditText()));
                    textView008.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView008.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView008.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView008.getEditText()));
                    textView008.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView008.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
            if(i==8){
                textView009.setVisibility(View.VISIBLE);
                textView009.setHint(dynamicFields.get(i).getDisplayName());
                textView009.setCounterEnabled(true);
                textView009.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView009.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView009.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView009.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView009.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView009.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView009.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView009.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView009.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView009.getEditText()));
                    textView009.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView009.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView009.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView009.getEditText()));
                    textView009.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView009.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
            if(i==9){
                textView010.setVisibility(View.VISIBLE);
                textView010.setHint(dynamicFields.get(i).getDisplayName());
                textView010.setCounterEnabled(true);
                textView010.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView010.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView010.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView010.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView010.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView010.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView010.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView010.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView010.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView010.getEditText()));
                    textView010.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView010.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView010.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView010.getEditText()));
                    textView010.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView010.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
            if(i==10){
                textView011.setVisibility(View.VISIBLE);
                textView011.setHint(dynamicFields.get(i).getDisplayName());
                textView011.setCounterEnabled(true);
                textView011.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView011.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView011.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView011.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView011.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView011.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView011.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView011.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView011.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView011.getEditText()));
                    textView011.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView011.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView011.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView011.getEditText()));
                    textView011.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView011.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
            if(i==11){
                textView012.setVisibility(View.VISIBLE);
                textView012.setHint(dynamicFields.get(i).getDisplayName());
                textView012.setCounterEnabled(true);
                textView012.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView012.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView012.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView012.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView012.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView012.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView012.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView012.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView012.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView012.getEditText()));
                    textView012.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView012.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView012.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView012.getEditText()));
                    textView012.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView012.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
            if(i==12){
                textView013.setVisibility(View.VISIBLE);
                textView013.setHint(dynamicFields.get(i).getDisplayName());
                textView013.setCounterEnabled(true);
                textView013.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView013.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView013.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView013.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView013.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView013.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView013.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView013.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView013.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView013.getEditText()));
                    textView013.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView013.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView013.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView013.getEditText()));
                    textView013.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView013.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
            if(i==13){
                textView014.setVisibility(View.VISIBLE);
                textView014.setHint(dynamicFields.get(i).getDisplayName());
                textView014.setCounterEnabled(true);
                textView014.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView014.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView014.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView014.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView014.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView014.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView014.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView014.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView014.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView014.getEditText()));
                    textView014.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView014.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView014.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView014.getEditText()));
                    textView014.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView014.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
            if(i==14){
                textView015.setVisibility(View.VISIBLE);
                textView015.setHint(dynamicFields.get(i).getDisplayName());
                textView015.setCounterEnabled(true);
                textView015.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView015.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView015.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView015.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView015.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView015.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView015.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView015.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView015.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView015.getEditText()));
                    textView015.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView015.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView015.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView015.getEditText()));
                    textView015.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView015.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }

            if(i==15){
                textView016.setVisibility(View.VISIBLE);
                textView016.setHint(dynamicFields.get(i).getDisplayName());
                textView016.setCounterEnabled(true);
                textView016.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView016.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView016.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView016.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView016.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView016.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView016.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView016.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView016.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView016.getEditText()));
                    textView016.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView016.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView016.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView016.getEditText()));
                    textView016.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView016.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }

            if(i==16){
                textView017.setVisibility(View.VISIBLE);
                textView017.setHint(dynamicFields.get(i).getDisplayName());
                textView017.setCounterEnabled(true);
                textView017.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView017.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView017.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView017.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView017.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView017.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView017.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView017.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView017.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView017.getEditText()));
                    textView017.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView017.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView017.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView017.getEditText()));
                    textView017.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView017.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }

            if(i==17){
                textView018.setVisibility(View.VISIBLE);
                textView018.setHint(dynamicFields.get(i).getDisplayName());
                textView018.setCounterEnabled(true);
                textView018.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView018.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView018.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView018.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView018.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView018.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView018.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView018.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView018.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView018.getEditText()));
                    textView018.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView018.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView018.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView018.getEditText()));
                    textView018.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView018.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
            if(i==18){
                textView019.setVisibility(View.VISIBLE);
                textView019.setHint(dynamicFields.get(i).getDisplayName());
                textView019.setCounterEnabled(true);
                textView019.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView019.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView019.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView019.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView019.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView019.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView019.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView019.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView019.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView019.getEditText()));
                    textView019.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView019.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView019.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView019.getEditText()));
                    textView019.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView019.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            }
            if(i==19){
                textView020.setVisibility(View.VISIBLE);
                textView020.setHint(dynamicFields.get(i).getDisplayName());
                textView020.setCounterEnabled(true);
                textView020.setCounterMaxLength(dynamicFields.get(i).getMaxLength());
                textView020.setHelperText(dynamicFields.get(i).getDescription());
                if(dynamicFieldData.size() > i ){
                    textView020.getEditText().setText(dynamicFieldData.get(i).getData());
                }
                if(dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.NUMERIC_FIELD_TYPE){
                    textView020.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    textView020.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textView020.getEditText().setKeyListener(DigitsKeyListener.getInstance(false,true));
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.ALPHA_NUMERIC_FIELD_TYPE){
                    textView020.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.DATE_FIELD_TYPE){
                    textView020.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView020.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView020.getEditText()));
                    textView020.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }else if (dynamicFields.get(i).getFieldTypeId() == DynamicFieldDefinition.EXPIRY_DATE_FIELD_TYPE){
                    textView020.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                    textView020.getEditText().addTextChangedListener(InputMask.insert("##/##/####", textView020.getEditText()));
                    textView020.setHelperText(dynamicFields.get(i).getDescription() + " " + "DD/MM/YYYY");
                }
                if(i+1 == dynamicFields.size()){
                    textView020.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
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
                globalVariable.setDocumentEditableFlag(true);
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
                    if(i==5){
                        if(textView006.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView006.getEditText().getText().toString());
                        }
                    }
                    if(i==6){
                        if(textView007.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView007.getEditText().getText().toString());
                        }
                    }
                    if(i==7){
                        if(textView008.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView008.getEditText().getText().toString());
                        }
                    }
                    if(i==8){
                        if(textView009.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView009.getEditText().getText().toString());
                        }
                    }
                    if(i==9){
                        if(textView010.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView010.getEditText().getText().toString());
                        }
                    }
                    if(i==10){
                        if(textView011.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView011.getEditText().getText().toString());
                        }
                    }
                    if(i==11){
                        if(textView012.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView012.getEditText().getText().toString());
                        }
                    }
                    if(i==12){
                        if(textView013.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView013.getEditText().getText().toString());
                        }
                    }
                    if(i==13){
                        if(textView014.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView014.getEditText().getText().toString());
                        }
                    }
                    if(i==14){
                        if(textView015.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView015.getEditText().getText().toString());
                        }
                    }

                    if(i==15){
                        if(textView016.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView016.getEditText().getText().toString());
                        }
                    }

                    if(i==16){
                        if(textView017.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView017.getEditText().getText().toString());
                        }
                    }

                    if(i==17){
                        if(textView018.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView018.getEditText().getText().toString());
                        }
                    }

                    if(i==18){
                        if(textView019.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView019.getEditText().getText().toString());
                        }
                    }

                    if(i==19){
                        if(textView020.getEditText().getText().length() >= 0){
                            if(globalVariable.getNextDocument().getDynamicFieldData().size() <= i){
                                DynamicFieldData data = new DynamicFieldData();
                                data.setParentResourceId(globalVariable.getNextDocument().getId());
                                data.setDynamicFieldDefinitionId(dynamicFields.get(i).getId());
                                globalVariable.getNextDocument().getDynamicFieldData().add(data);
                            }
                            globalVariable.getNextDocument().getDynamicFieldData().get(i).setData(textView020.getEditText().getText().toString());
                        }
                    }
                }
                 onBackPressed();
            }
        });

        //
        // check that the fields are editable, otherwise
        // .setEnabled(false)
        if(globalVariable.isDocumentEditableFlag() == false){
            disableDynamicfieldsReadOnly();
        }
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
        textView006.setVisibility(View.INVISIBLE);
        textView007.setVisibility(View.INVISIBLE);
        textView008.setVisibility(View.INVISIBLE);
        textView009.setVisibility(View.INVISIBLE);
        textView010.setVisibility(View.INVISIBLE);
        textView011.setVisibility(View.INVISIBLE);
        textView012.setVisibility(View.INVISIBLE);
        textView013.setVisibility(View.INVISIBLE);
        textView014.setVisibility(View.INVISIBLE);
        textView015.setVisibility(View.INVISIBLE);
        textView016.setVisibility(View.INVISIBLE);
        textView017.setVisibility(View.INVISIBLE);
        textView018.setVisibility(View.INVISIBLE);
        textView019.setVisibility(View.INVISIBLE);
        textView020.setVisibility(View.INVISIBLE);
    }

    void disableDynamicfieldsReadOnly() {

        // disable the text edit portion of the widget
        textView001.getEditText().setEnabled(false);
        textView002.getEditText().setEnabled(false);
        textView003.getEditText().setEnabled(false);
        textView004.getEditText().setEnabled(false);
        textView005.getEditText().setEnabled(false);
        textView006.getEditText().setEnabled(false);
        textView007.getEditText().setEnabled(false);
        textView008.getEditText().setEnabled(false);
        textView009.getEditText().setEnabled(false);
        textView010.getEditText().setEnabled(false);
        textView011.getEditText().setEnabled(false);
        textView012.getEditText().setEnabled(false);
        textView013.getEditText().setEnabled(false);
        textView014.getEditText().setEnabled(false);
        textView015.getEditText().setEnabled(false);
        textView016.getEditText().setEnabled(false);
        textView017.getEditText().setEnabled(false);
        textView018.getEditText().setEnabled(false);
        textView019.getEditText().setEnabled(false);
        textView020.getEditText().setEnabled(false);
        // remove the caption leave only the label
        textView001.setHelperText(null);
        textView002.setHelperText(null);
        textView003.setHelperText(null);
        textView004.setHelperText(null);
        textView005.setHelperText(null);
        textView006.setHelperText(null);
        textView007.setHelperText(null);
        textView008.setHelperText(null);
        textView009.setHelperText(null);
        textView010.setHelperText(null);
        textView011.setHelperText(null);
        textView012.setHelperText(null);
        textView013.setHelperText(null);
        textView014.setHelperText(null);
        textView015.setHelperText(null);
        textView016.setHelperText(null);
        textView017.setHelperText(null);
        textView018.setHelperText(null);
        textView019.setHelperText(null);
        textView020.setHelperText(null);
    }

}
