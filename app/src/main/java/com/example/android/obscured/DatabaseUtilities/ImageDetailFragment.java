package com.example.android.obscured.DatabaseUtilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.obscured.MethodsDeclarations;
import com.example.android.obscured.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Shubham on 30-06-2017.
 */

public class ImageDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    String mImagePath;

    //@BindView(R.id.iv_fg_image)
    ImageView mainImageView;
    TextView mImageName;
    TextView mImageDate;
    TextView hiddenOrNot;
    Context mActivityContext;

    int PHOTO_DETAILS_LOADER_ID = 104;
    boolean isExiting = true;

    public class ImageAllDetails
    {
        String imageName;
        String imageDate;
        int imageSize;
        boolean isHidden;
        String hideOrVisible;
    }

    ImageAllDetails currentImageDetails = new ImageAllDetails();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImagePath = getArguments().getString("image_path_key");
        mActivityContext = getContext();
        getLoaderManager().initLoader(PHOTO_DETAILS_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.image_detail_fragment, container, false);
        //ButterKnife.bind(this, container);

        mainImageView = (ImageView)rootView.findViewById(R.id.iv_fg_image);
        mImageName = (TextView)rootView.findViewById(R.id.tv_fg_image_name);
        mImageDate = (TextView)rootView.findViewById(R.id.tv_fg_image_date);
        hiddenOrNot = (TextView)rootView.findViewById(R.id.tv_fg_image_ishidden);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getActivity().getApplicationContext(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media.DATA + " = ?", new String[]{mImagePath}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        data.moveToNext();
        currentImageDetails.imageName = data.getString(data.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
        currentImageDetails.imageDate = data.getString(data.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN));

        Uri uriToCheckImagePresence = PicsContract.PicsEntry.CONTENT_URI.buildUpon().appendPath("12").build();

        Cursor tableQuery;
        if((tableQuery = getContext().getContentResolver().query(uriToCheckImagePresence, null, null, new String[]{mImagePath}, null)) != null)
        {
            currentImageDetails.isHidden = true;
        }
        else
        {
            currentImageDetails.isHidden = false;
        }

        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage("file://"+mImagePath, mainImageView, options);

        //Glide.with(getContext()).load("file://"+mImagePath).into(mainImageView);
        mImageName.setText(currentImageDetails.imageName);

        long date = Long.parseLong(currentImageDetails.imageDate)/1000;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        mImageDate.setText((df.format(date)));

        if(currentImageDetails.isHidden)
            currentImageDetails.hideOrVisible = "Hidden";
        else
            currentImageDetails.hideOrVisible = "Not Hidden";

        hiddenOrNot.setText(currentImageDetails.hideOrVisible);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onStop()
    {
        super.onStop();


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