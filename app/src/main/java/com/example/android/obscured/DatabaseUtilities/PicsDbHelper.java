package com.example.android.obscured.DatabaseUtilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by Shubham on 07-02-2017.
 */

public class PicsDbHelper extends SQLiteOpenHelper {
    // The name of the database
    private static final String DATABASE_NAME = "moviesDb.db";

    // If you change the database schema, you must increment the database version
    private static final int VERSION = 1;


    // Constructor
    PicsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }


    /**
     * Called when the tasks database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create tasks table (careful to follow SQL formatting rules)
        final String CREATE_TABLE = "CREATE TABLE "  + PicsContract.PicsEntry.TABLE_NAME + " (" +
                PicsContract.PicsEntry._ID                + " INTEGER PRIMARY KEY, " +
                PicsContract.PicsEntry.PIC_DATA + " TEXT NOT NULL UNIQUE);";

        db.execSQL(CREATE_TABLE);
    }


    /**
     * This method discards the old table of data and calls onCreate to recreate a new one.
     * This only occurs when the version number for this database (DATABASE_VERSION) is incremented.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PicsContract.PicsEntry.TABLE_NAME);
        onCreate(db);
    }
}
