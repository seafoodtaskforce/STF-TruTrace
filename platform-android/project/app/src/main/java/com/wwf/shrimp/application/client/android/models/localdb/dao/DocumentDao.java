package com.wwf.shrimp.application.client.android.models.localdb.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by AleaActaEst on 20/06/2017.
 */

/**
 * Document DAT (CRUD) implementation
 */
public class DocumentDao extends SQLiteOpenHelper { // {

    // Database
    private static final String DATABASE_NAME = "WWFShrimpDatabase.db";
    private static final int DATABASE_VERSION = 1;

    //
    // Table Names
    //
    public static final String DOCUMENT_TABLE_NAME = "document_table";
    public static final String DOCUMENT_PAGE_TABLE_NAME = "document_page_table";

    //
    // Column definitions
    //

    // DOCUMENT_TABLE
    public static final String ID = "_id";
    public static final String USER_OWNER_NAME = "user_owner";
    public static final String USER_OWNER_ID = "user_owner_id";
    public static final String DOCUMENT_IMAGE = "document_image";
    public static final String DOCUMENT_NAME = "document_name";
    public static final String CREATION_TIMESTAMP = "creation_timestamp";
    public static final String DOCUMENT_TYPE = "document_type";
    public static final String DOCUMENT_COLOR_CODE = "document_color_code";
    public static final String DOCUMENT_TYPE_ID = "document_type_id";
    public static final String SYNC_ID = "sync_id";

    // DOCUMENT_PAGE_TABLE
    public static final String ID_DOCUMENT_PAGE = "_id";
    public static final String PAGE_IMAGE_DOCUMENT_PAGE = "document_image";
    public static final String PAGE_IMAGE_DOCUMENT_PAGE_NUMBER = "document_image_number";
    public static final String PAGE_IMAGE_DOCUMENT_PAGE_FOREIGN_ID = "document_id";
    public static final String PAGE_IMAGE_DOCUMENT_PAGE_FOREIGN_SYNC_ID = "document_sync_id";



    //
    // Table definitions
    //
    private static final String SQL_CREATE_DOCUMENT_TABLE = "CREATE TABLE " + DOCUMENT_TABLE_NAME +
            " ( " + ID + " INTEGER PRIMARY KEY autoincrement, " +   // 0
            USER_OWNER_NAME + " TEXT, " +                           // 1
            USER_OWNER_ID + " INTEGER, " +                          // 2
            DOCUMENT_IMAGE + " TEXT, " +                            // 3
            DOCUMENT_NAME + " TEXT, " +                             // 4
            CREATION_TIMESTAMP + " TEXT, " +                        // 5
            DOCUMENT_COLOR_CODE + " TEXT, " +                       // 6
            DOCUMENT_TYPE + " TEXT, " +                             // 7
            DOCUMENT_TYPE_ID + " INTEGER, " +                       // 8
            SYNC_ID + " TEXT " +                                    // 9
            ");";

    private static final String SQL_CREATE_DOCUMENT_PAGE_TABLE = "CREATE TABLE " + DOCUMENT_PAGE_TABLE_NAME +
            " ( " + ID_DOCUMENT_PAGE + " INTEGER PRIMARY KEY autoincrement, " +         // 0
            PAGE_IMAGE_DOCUMENT_PAGE + " TEXT, " +                                      // 1
            PAGE_IMAGE_DOCUMENT_PAGE_NUMBER + " INTEGER, " +                            // 2
            PAGE_IMAGE_DOCUMENT_PAGE_FOREIGN_ID + " INTEGER, " +                        // 3
            PAGE_IMAGE_DOCUMENT_PAGE_FOREIGN_SYNC_ID + " TEXT " +                       // 4
            ");";
            /**
            " FOREIGN KEY ("+ PAGE_IMAGE_DOCUMENT_PAGE_FOREIGN_ID + ")" + " " +         // FOREIGN KEY
            " REFERENCES " + DOCUMENT_TABLE_NAME + "("+ ID +"));";
             */


    private static final String SQL_DROP_DOCUMENT_TABLE = "DROP TABLE IS EXISTS " + DOCUMENT_TABLE_NAME ;
    private static final String SQL_DROP_DOCUMENT_PAGE_TABLE = "DROP TABLE IS EXISTS " + DOCUMENT_PAGE_TABLE_NAME ;
    /**
     * Public constructor
     * @param context - the context for the operation
     */
    public DocumentDao(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DOCUMENT_TABLE);
        Log.i("DATABASE", "The CREATE Query is " + SQL_CREATE_DOCUMENT_TABLE );
        db.execSQL(SQL_CREATE_DOCUMENT_PAGE_TABLE);
        Log.i("DATABASE", "The CREATE Query is " + SQL_CREATE_DOCUMENT_PAGE_TABLE );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_DOCUMENT_TABLE);
        db.execSQL(SQL_DROP_DOCUMENT_PAGE_TABLE);
        onCreate(db);
    }
}
