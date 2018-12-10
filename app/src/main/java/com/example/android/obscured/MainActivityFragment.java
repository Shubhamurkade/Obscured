package com.example.android.obscured;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.Optional;

import com.example.android.obscured.DatabaseUtilities.PicsContract;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ImageAdapter.ImageOnClickHandler {

    @BindView(R.id.rv_photos)
    RecyclerView mPhotosRecyclerView;
    int PHOTOS_EXTERNAL_LOADER_ID = 100;
    int PHOTOS_HIDDEN_LOADER_ID = 101;
    int PHOTOS_HIDDEN_ADDER = 102;
    ImageAdapter imageAdapter;
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
    onImageClickListener onImageClickListenerInstance = null;

    /*Change the image path in the table only if the app was actually being exited not when
    navigating to another fragment*/
    boolean isExiting = true;

    public interface onImageClickListener
    {
        void onImageClicked(String imagePath);
    }

    @Override
    public void OnClick(String imageData) {
        onImageClickListenerInstance.onImageClicked(imageData);
        isExiting = false;
    }

    enum whichScreen{ALL_PICS, HIDDEN_PICS};
    whichScreen valueWhichScreen = whichScreen.ALL_PICS;

    /*the context from the calling activity */
    private static Context mActivityContext;

    public static MainActivityFragment createFragment(Context context)
    {
        MainActivityFragment mainActivityFragment = new MainActivityFragment();
        mActivityContext = context;
        return mainActivityFragment;
    }

    public void setImageClickListener(onImageClickListener clickListener)
    {
        onImageClickListenerInstance = clickListener;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.activity_main_fragment, container, false);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        ButterKnife.bind(this, rootView);

        imageAdapter = new ImageAdapter(mActivityContext, this, getActivity());
        mPhotosRecyclerView.setAdapter(imageAdapter);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        mPhotosRecyclerView.setLayoutManager(staggeredGridLayoutManager);

        /*need to insert the path for hidden images into MediaStore class, in order to be shown on the main
        page */

        getLoaderManager().destroyLoader(PHOTOS_HIDDEN_ADDER);
        LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = this;
        getLoaderManager().initLoader(PHOTOS_HIDDEN_ADDER,null, loaderCallbacks);

        registerForContextMenu(mPhotosRecyclerView);

        mPlanetTitles = getResources().getStringArray(R.array.nav_list);
        mDrawerLayout = (DrawerLayout)rootView.findViewById(R.id.drawer_layout);
        mDrawerList = (ListView)rootView.findViewById(R.id.left_drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(mActivityContext,
                R.layout.drawer_list_item, mPlanetTitles));
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setHomeButtonEnabled(true);
        mTitle = "Obscured";
        mDrawerTitle = "Select an option";

        mDrawerToggle = new android.support.v4.app.ActionBarDrawerToggle(
                getActivity(),                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                getActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                getActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        isExiting = true;
        getLoaderManager().destroyLoader(PHOTOS_HIDDEN_ADDER);
        LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = this;
        getLoaderManager().initLoader(PHOTOS_HIDDEN_ADDER,null, loaderCallbacks);
        
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        if(valueWhichScreen == whichScreen.ALL_PICS)
            getLoaderManager().initLoader(PHOTOS_EXTERNAL_LOADER_ID, null, loadedLoaderCallbacks);
        else
            getLoaderManager().initLoader(PHOTOS_HIDDEN_LOADER_ID, null, loadedLoaderCallbacks);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        /* Add hdden images into MediaStore class */
        if (id == PHOTOS_HIDDEN_ADDER)
        {
            Uri uri = PicsContract.PicsEntry.CONTENT_URI;
            Cursor cursor = mActivityContext.getContentResolver().query(uri, null, null, null, null);

            while(cursor!=null && cursor.moveToNext())
            {
                ContentValues contentValues = new ContentValues();

                /* get column index for pics_data column */
                int dataColumnIndex = cursor.getColumnIndexOrThrow(PicsContract.PicsEntry.PIC_DATA);
                String imagePathFromTable = cursor.getString(dataColumnIndex);

                /*try{
                    getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media.DATA + " = ?", new String[]{imagePathFromTable}, null);
                    Log.d("is_present", imagePathFromTable);
                }
                catch (Exception e)
                {
                    e.printStackTrace();*/
                    /*put the value obtained into MediaStore class*/
                    contentValues.put(MediaStore.Images.Media.DATA, imagePathFromTable);
                    mActivityContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                //}
            }
        }

        if(valueWhichScreen == whichScreen.ALL_PICS)
        {
            String orderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            return new CursorLoader(mActivityContext, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    orderBy);
        }
        else
        {
            Uri uriForImageQuery = PicsContract.PicsEntry.CONTENT_URI.buildUpon().build();
            return new CursorLoader(mActivityContext, uriForImageQuery,
                    null,
                    null,
                    null,
                    null);
        }


    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        System.out.println(data);
        System.out.println(data.getCount());

        int loaderId = loader.getId();

        if(loaderId == PHOTOS_EXTERNAL_LOADER_ID)
            imageAdapter.swapCursorAndSetFlag(data, false);

        else if(loaderId == PHOTOS_HIDDEN_LOADER_ID)
            imageAdapter.swapCursorAndSetFlag(data, true);

        else if(loader.getId() == PHOTOS_HIDDEN_ADDER)
        {
            imageAdapter.swapCursorAndSetFlag(data, false);
        }
            //loadImageFromStorage();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //imageAdapter.swapCursor(null);
    }

    /*
    @Override
    public void OnClick(Cursor rowCursor) {

    }*/

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
                getLoaderManager().destroyLoader(PHOTOS_EXTERNAL_LOADER_ID);
                loadImageFromStorage();
                mDrawerLayout.closeDrawers();
                break;

            case 1:
                valueWhichScreen = whichScreen.HIDDEN_PICS;
                //imageAdapter.mCursor.close();
                getLoaderManager().destroyLoader(PHOTOS_HIDDEN_LOADER_ID);
                loadImageFromStorage();
                mDrawerLayout.closeDrawers();
                break;

        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if(isExiting)
        {
            Uri uri = PicsContract.PicsEntry.CONTENT_URI;
            Cursor cursor = mActivityContext.getContentResolver().query(uri, null, null, null, null);

            int dataColumn = cursor.getColumnIndexOrThrow(PicsContract.PicsEntry.PIC_DATA);
            String imagePath;
            while(cursor.moveToNext())
            {
                imagePath = cursor.getString(dataColumn);

            /* Following cases are applicable for this method
            *   1. If the image path in the table is not hidden image path (i.e during the app run
            *       time the user has hidden some images):
            *       In this case following will happen:
            *       a. The image will be first deleted from the table.
            *       b. Hidden image path for the image will be created.
            *       c. Then the hidden image path will be added to the table.4
            *       d. The actual image path will be deleted from the MediaStore class.
            *
            *   2. If the image path in the table is a hidden image path (This can happen when user
            *       hides some images and then exists the app).
            *       Following will happen in this case after the user exits the app and launches it again:
            *       a. All the image path from the table (which are hidden images paths) will be added
            *          to the MediaStore class.
            *       b. When the user is exiting the app, the hidden image path will only be delted from
            *          the MediaStore class.
            */

                MethodsDeclarations.DeleteImageFromMediaStoreAndStoreHiddenPathInTable(mActivityContext,imagePath, MethodsDeclarations.isHidden(imagePath));
            }
        }
    }
}
