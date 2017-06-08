package com.example.android.obscured;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.obscured.DatabaseUtilities.PicsContract;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.android.obscured.R.attr.reverseLayout;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, ImageAdapter.ImageOnClickHandler{

    @BindView(R.id.rv_photos)
    RecyclerView mPhotosRecyclerView;
    int PHOTOS_EXTERNAL_LOADER_ID = 100;
    int PHOTOS_INTERNAL_LOADER_ID = 101;
    ImageAdapter imageAdapter;
    Handler handler;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    private boolean loading = false;
    private int page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        imageAdapter = new ImageAdapter(getApplicationContext(), this);
        mPhotosRecyclerView.setAdapter(imageAdapter);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        mPhotosRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        loadImageFromStorage();
        registerForContextMenu(mPhotosRecyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadImageFromStorage()
    {
        LoaderManager.LoaderCallbacks<Cursor> loadedLoaderCallbacks = this;
        Bundle bundleExternal = new Bundle();
        //bundleExternal.putChar("parm", 'e');
        getSupportLoaderManager().initLoader(PHOTOS_EXTERNAL_LOADER_ID, bundleExternal, loadedLoaderCallbacks);
        //getSupportLoaderManager().initLoader(PHOTOS_INTERNAL_LOADER_ID, null, loadedLoaderCallbacks);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //if(args.getChar("parm")=='e')
        {
            String[] projection = {MediaStore.Images.Media._ID};
            String orderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            return new CursorLoader(this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    orderBy);
        }/*
        else
        {
            String[] projection = {MediaStore.Images.Thumbnails._ID};
            return new CursorLoader(this, MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);
        }*/

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        System.out.println(data);
        imageAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void OnClick(Cursor rowCursor) {


    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        Uri uriForImageInsert = PicsContract.PicsEntry.CONTENT_URI.buildUpon().appendPath("1").build();

        String imagePath = imageAdapter.imageData[imageAdapter.mAdapterPosition];
        ContentValues contentValues = new ContentValues();
        contentValues.put(PicsContract.PicsEntry.PIC_DATA, imagePath);

        getContentResolver().insert(uriForImageInsert, contentValues);
        System.out.println(imagePath);
        return true;
    }

}
