package com.example.uniwuemensa;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MensaDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "MensaReader.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    
    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + MensaContract.MensaEntry.TABLE_NAME + " (" +
        MensaContract.MensaEntry._ID + " INTEGER PRIMARY KEY," +
        MensaContract.MensaEntry.COLUMN_NAME_DATE + INTEGER_TYPE + COMMA_SEP +
        MensaContract.MensaEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
        MensaContract.MensaEntry.COLUMN_NAME_STUDENT_PRICE + INTEGER_TYPE + COMMA_SEP +
        MensaContract.MensaEntry.COLUMN_NAME_STAFF_PRICE + INTEGER_TYPE + COMMA_SEP +
        MensaContract.MensaEntry.COLUMN_NAME_GUEST_PRICE + INTEGER_TYPE + COMMA_SEP +
        "UNIQUE(" + MensaContract.MensaEntry.COLUMN_NAME_DATE + COMMA_SEP +
        MensaContract.MensaEntry.COLUMN_NAME_TITLE + ") ON CONFLICT REPLACE" +
        " )";

    private static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + MensaContract.MensaEntry.TABLE_NAME;

    public MensaDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
