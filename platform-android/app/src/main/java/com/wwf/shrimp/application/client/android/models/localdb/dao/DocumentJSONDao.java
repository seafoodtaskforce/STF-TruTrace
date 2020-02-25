package com.wwf.shrimp.application.client.android.models.localdb.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by AleaActaEst on 20/06/2017.
 */

/**
 * Document DAT (CRUD) implementation with JSON Payload
 */
public class DocumentJSONDao extends SQLiteOpenHelper { // {

    // Database
    private static final String DATABASE_NAME = "WWFShrimpSyncDB_M2_001.db";
    private static final int DATABASE_VERSION = 1;

    //
    // Table Names
    //
    public static final String DOCUMENT_TABLE_NAME = "document_table";

    //
    // Column definitions
    //

    // DOCUMENT_TABLE
    public static final String ID = "_id";
    public static final String USER_OWNER_NAME = "user_owner";
    public static final String DOCUMENT_IMAGE = "document_image";
    public static final String DOCUMENT_NAME = "document_name";
    public static final String CREATION_TIMESTAMP = "creation_timestamp";
    public static final String SYNC_TIMESTAMP = "sync_timestamp";
    public static final String DOCUMENT_TYPE = "document_type";
    public static final String SYNC_ID = "sync_id";

    //
    // Table definitions
    //
    private static final String SQL_CREATE_DOCUMENT_TABLE = "CREATE TABLE " + DOCUMENT_TABLE_NAME +
            " ( " + ID + " INTEGER PRIMARY KEY autoincrement, " +   // 0
            USER_OWNER_NAME + " TEXT, " +                           // 1
            DOCUMENT_IMAGE + " TEXT, " +                            // 2
            DOCUMENT_NAME + " TEXT, " +                             // 3
            DOCUMENT_TYPE + " TEXT, " +                             // 4
            CREATION_TIMESTAMP + " TEXT, " +                        // 5
            SYNC_TIMESTAMP + " TEXT, " +                            // 6
            SYNC_ID + " TEXT " +                                    // 7
            ");";


    private static final String SQL_DROP_DOCUMENT_TABLE = "DROP TABLE IS EXISTS " + DOCUMENT_TABLE_NAME ;

    /**
     * Public constructor
     * @param context - the context for the operation
     */
    public DocumentJSONDao(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DOCUMENT_TABLE);
        Log.i("DATABASE", "The CREATE Query is " + SQL_CREATE_DOCUMENT_TABLE );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_DOCUMENT_TABLE);
        onCreate(db);
    }
}
