package com.example.android.obscured;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.android.obscured.DatabaseUtilities.PicsContract;

/**
 * Created by Shubham on 08-06-2017.
 */

public class MyActionModeCallback implements ActionMode.Callback {

    Context mContext;
    String mImagePathFromAdapterArray;
    ImageView mImageView;
    boolean isHideButtonClicked = false;
    boolean mActivityFlag;
    Cursor mCursor;
    int mAdapterPosition;

    public MyActionModeCallback(Context context, String imagePath, ImageView imageView, boolean activityFlag, Cursor cursor, int adapterPosition)
    {
        mContext = context;
        mImagePathFromAdapterArray = imagePath;
        mImageView = imageView;
        mActivityFlag = activityFlag;
        mCursor = cursor;
        mAdapterPosition = adapterPosition;
    }
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {

        if(!mActivityFlag)
        {
            menu.add(R.string.hide_item_title);
            mImageView.setBackgroundColor(Color.rgb(0, 0, 100));
            mImageView.setImageAlpha(90);
        }
        else
        {
            menu.add(R.string.db_remove_hidden_image);
        }

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Uri uriForImageInsert = PicsContract.PicsEntry.CONTENT_URI.buildUpon().appendPath("1").build();

        if(item.getTitle() == mContext.getString(R.string.hide_item_title))
        {
            String imagePath = mImagePathFromAdapterArray;
            ContentValues contentValues = new ContentValues();
            contentValues.put(PicsContract.PicsEntry.PIC_DATA, imagePath);
            mContext.getContentResolver().insert(uriForImageInsert, contentValues);
            isHideButtonClicked = true;
        }
        else if(item.getTitle() == mContext.getString(R.string.db_remove_hidden_image))
        {
            Uri uriToRemoveImageFromDb = PicsContract.PicsEntry.CONTENT_URI.buildUpon().appendPath("12").build();
            int retVal = mContext.getContentResolver().delete(uriToRemoveImageFromDb, PicsContract.PicsEntry.PIC_DATA + " = ?", new String[]{mImagePathFromAdapterArray});
        }
        onDestroyActionMode(mode);
        mode.finish();
        mImageView.setImageAlpha(90);
        mImageView.setBackgroundColor(0);

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if(!isHideButtonClicked)
            mImageView.setImageAlpha(255);
        else mImageView.setImageAlpha(90);
    }

}
