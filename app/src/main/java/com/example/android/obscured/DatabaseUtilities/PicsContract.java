package com.example.android.obscured.DatabaseUtilities;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Shubham on 07-02-2017.
 */

public class PicsContract {

    //authority for this database
    public static final String AUTHORITY = "com.example.android.obscured";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Define the possible paths for accessing data in this contract
    // This is the path for the "tasks" directory
    public static final String PICS = "pics";

    public static final class PicsEntry implements BaseColumns
    {
        // MoviesEntry content URI = base content URI + path
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PICS).build();



        // Task table and column names
        public static final String TABLE_NAME = "pics_table";

        // Since TaskEntry implements the interface "BaseColumns", it has an automatically produced
        // "_ID" column in addition to the two below

        public static final String PIC_DATA = "pic_data";
    }
}


