package com.wwf.shrimp.application.client.android.utils;

import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.dialogs.TabbedDocumentDialog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for mapping different types of entities.
 *
 * Created by AleaActaEst on 27/06/2017.
 */

public class MappingUtilities {

    public static final Map<Integer, Long> documentRTypeToIDMap = new HashMap<Integer, Long>();
    static {
        documentRTypeToIDMap.put(R.string.mcpd_document_type, 1L);
        documentRTypeToIDMap.put(R.string.captain_statement_type, 2L);
        documentRTypeToIDMap.put(R.string.fishing_logbook_document_type, 3L);
        documentRTypeToIDMap.put(R.string.feed_lot_sheet_document_type, 4L);
        documentRTypeToIDMap.put(R.string.fishmeal_lot_traceability_document_type, 5L);
    }

    public static final Map<Long, Integer> documentTypeIDToRTypeMap = new HashMap<Long, Integer>();
    static {
        documentTypeIDToRTypeMap.put(1L, R.string.mcpd_document_type);
        documentTypeIDToRTypeMap.put(2L, R.string.captain_statement_type);
        documentTypeIDToRTypeMap.put(3L, R.string.fishing_logbook_document_type);
        documentTypeIDToRTypeMap.put(4L, R.string.feed_lot_sheet_document_type);
        documentTypeIDToRTypeMap.put(5L, R.string.fishmeal_lot_traceability_document_type);
    }

    public static final Map<Long, Integer> documentTypeIDToRColorMap = new HashMap<Long, Integer>();
    static {
        documentTypeIDToRColorMap.put(1L, R.color.colorFAB1Pressed);
        documentTypeIDToRColorMap.put(2L, R.color.colorFAB2Pressed);
        documentTypeIDToRColorMap.put(3L, R.color.colorFAB3Pressed);
        documentTypeIDToRColorMap.put(4L, R.color.colorFAB4Pressed);
        documentTypeIDToRColorMap.put(5L, R.color.colorFAB5Pressed);
    }

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static TabbedDocumentDialog getTabbedDialog(List fragmentManagerList){
        TabbedDocumentDialog instance = null;

        for(int i = 0; i< fragmentManagerList.size(); i++){
            if(fragmentManagerList.get(i) instanceof TabbedDocumentDialog){
                instance = (TabbedDocumentDialog) fragmentManagerList.get(i);
            }
        }
         return instance;
    }
}
