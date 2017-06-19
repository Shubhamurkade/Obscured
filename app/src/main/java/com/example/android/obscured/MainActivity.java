package com.example.android.obscured;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Intent;
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
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.obscured.DatabaseUtilities.HideImageLoader;
import com.example.android.obscured.DatabaseUtilities.PicsContract;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.android.obscured.R.attr.reverseLayout;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, ImageAdapter.ImageOnClickHandler {

    @BindView(R.id.rv_photos)
    RecyclerView mPhotosRecyclerView;
    int PHOTOS_EXTERNAL_LOADER_ID = 100;
    int PHOTOS_HIDDEN_LOADER_ID = 101;
    ImageAdapter imageAdapter;
    HideImageAdapter hideImageAdapter;
    Handler handler;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    private boolean loading = false;
    private int page = 0;

    //variable for nav drawer
    private String[] mPlanetTitles; //title for the drawer
    private DrawerLayout mDrawerLayout; //the main layout of the drawer
    private ListView mDrawerList; //the list view of items for drawer
    private android.support.v4.app.ActionBarDrawerToggle mDrawerToggle;

    private String mTitle;
    private String mDrawerTitle;
    enum whichScreen{ALL_PICS, HIDDEN_PICS};
    whichScreen valueWhichScreen = whichScreen.ALL_PICS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        imageAdapter = new ImageAdapter(getApplicationContext(), this, this);
        mPhotosRecyclerView.setAdapter(imageAdapter);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        mPhotosRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        loadImageFromStorage();
        registerForContextMenu(mPhotosRecyclerView);

        mPlanetTitles = getResources().getStringArray(R.array.nav_list);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPlanetTitles));
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setHomeButtonEnabled(true);
        mTitle = "Obscured";
        mDrawerTitle = "Select an option";

        mDrawerToggle = new android.support.v4.app.ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        //instantiate HideImageDapter for nav drawer
        //hideImageAdapter = new HideImageAdapter(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private void loadImageFromStorage() {
        LoaderManager.LoaderCallbacks<Cursor> loadedLoaderCallbacks = this;
        getSupportLoaderManager().initLoader(PHOTOS_EXTERNAL_LOADER_ID, null, loadedLoaderCallbacks);

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
        System.out.println(data.getCount());
        if(valueWhichScreen == whichScreen.ALL_PICS)
        {
            imageAdapter.swapCursor(data);
        }
        else imageAdapter.swapCursorAndSetFlag(data, true);

        //mPhotosRecyclerView.

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        imageAdapter.swapCursor(null);
    }

    @Override
    public void OnClick(Cursor rowCursor) {

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Intent startActivityIntent;

        switch (position)
        {
            case 0:
                valueWhichScreen = whichScreen.ALL_PICS;
                if(valueWhichScreen != whichScreen.ALL_PICS)
                {
                    //imageAdapter = null;
                    //hideImageAdapter = null;
                    //getSupportLoaderManager().destroyLoader(PHOTOS_HIDDEN_LOADER_ID);
                    loadImageFromStorage();
                    //imageAdapter = new ImageAdapter(getApplicationContext(), this, this);
                    //mPhotosRecyclerView.invalidate();
                    //mPhotosRecyclerView.removeAllViews();
                    //mPhotosRecyclerView.swapAdapter(imageAdapter, false);
                    //mPhotosRecyclerView.setLayoutManager(staggeredGridLayoutManager);
                    //mPhotosRecyclerView.setAdapter(imageAdapter);
                }
                break;

            case 1:/*
                startActivityIntent = new Intent(this, HideImageActivity.class);
                startActivity(startActivityIntent);*/
                valueWhichScreen = whichScreen.HIDDEN_PICS;
                if(valueWhichScreen != whichScreen.HIDDEN_PICS)
                {
                    //imageAdapter.mCursor = null;
                    //imageAdapter.notifyDataSetChanged();
                    //hideImageAdapter = null;
                    //imageAdapter = null;
                    hideImageAdapter = new HideImageAdapter(this);
                    //getSupportLoaderManager().destroyLoader(PHOTOS_EXTERNAL_LOADER_ID);
                    //mPhotosRecyclerView.swapAdapter(hideImageAdapter, false);
                    HideImageLoader hideImageLoader = new HideImageLoader(this, hideImageAdapter);
                    //mPhotosRecyclerView.setAdapter(hideImageAdapter);
                    getSupportLoaderManager().initLoader(PHOTOS_HIDDEN_LOADER_ID, null, hideImageLoader);
                }

                break;

        }
    }
}
