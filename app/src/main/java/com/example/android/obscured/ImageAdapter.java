package com.example.android.obscured;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.android.obscured.DatabaseUtilities.PicsContract;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.LargestLimitedMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

/**
 * Created by Shubham on 21-05-2017.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageAdapterViewHolder> {

    Cursor mCursor = null;
    Context mContext;
    ImageOnClickHandler imageOnClickHandler = null;
    int idColumnIndex;
    int dataColumnIndex;
    String[] imageData;
    int mAdapterPosition;
    Activity mActivity;
    boolean activityFlag = false;
    boolean isLongClick = false;

    public interface ImageOnClickHandler{
        void OnClick(String imageData);
    }

    public ImageAdapter(Context context, ImageOnClickHandler imageOnClickHandler, Activity activity)
    {
        mContext = context;
        mActivity = activity;
        this.imageOnClickHandler = imageOnClickHandler;
    }

    public class ImageAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView imageView;

        public ImageAdapterViewHolder(View view)
        {
            super(view);
            //view.setOnCreateContextMenuListener(this);
            imageView = (ImageView)view.findViewById(R.id.iv_image);
            imageView.setOnClickListener(this);
            if(!activityFlag)
                idColumnIndex = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            //ImageLoader imageLoader=new  ImageLoader(mContext);
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                    .displayer(new FadeInBitmapDisplayer(300)).build();
            File cacheDir = StorageUtils.getCacheDirectory(mContext);
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                    mContext)
                    .defaultDisplayImageOptions(defaultOptions)
                    .memoryCache(new LruMemoryCache(2*1024*1024))
                    .build();

            ImageLoader.getInstance().init(config);
        }

        @Override
        public void onClick(View v) {

            if(!isLongClick)
            {
                int adapterPosition = getAdapterPosition();
                if(mCursor != null)
                {
                    mCursor.moveToPosition(adapterPosition);
                    imageOnClickHandler.OnClick(imageData[adapterPosition]);
                }
            }

            isLongClick = false;
        }
    }

    @Override
    public ImageAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        int layoutIdForImage = R.layout.single_photo;
        boolean shouldAttachToParentImmediatedly = false;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(layoutIdForImage, parent, shouldAttachToParentImmediatedly);
        return new ImageAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ImageAdapterViewHolder holder, final int position) {

        isLongClick = false;
        holder.imageView.setImageAlpha(255);
        mCursor.moveToPosition(position);
        imageData[position] = mCursor.getString(dataColumnIndex);

        Log.d("adapter_bin", imageData[position]);
        Uri uri;

        /*skip the showing of the image if MediaStore leads to some exception */
        boolean skipShowImage = false;

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                mActivity.startActionMode(new MyActionModeCallback(mContext, imageData[position], holder.imageView, activityFlag, mCursor, position));
                isLongClick = true;
                return false;
            }
        });

        if(!activityFlag)
        {
            int imageId = mCursor.getInt(idColumnIndex);
            uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ""+imageId);
            Uri uriForImageQuery = PicsContract.PicsEntry.CONTENT_URI.buildUpon().appendPath("12").build();
            try
            {
                if(mContext.getContentResolver().query(uriForImageQuery, null, null, new String[]{imageData[position]}, null).getCount()>0)
                {
                    holder.imageView.setImageTintMode(PorterDuff.Mode.SRC_ATOP);
                    holder.imageView.setImageAlpha(90);
                }
            }catch (NullPointerException e)
            {
                e.printStackTrace();
                //mContext.getContentResolver().delete(uriForImageQuery, MediaStore.Images.Media.DATA + " = ?", new String[]{imageData[position]});
                //skipShowImage = true;
            }
        }
        else
        {
            uri = Uri.parse("file://"+imageData[position]);
        }

        if(!skipShowImage)
        {
            DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).build();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(uri.toString(), holder.imageView, options);
        }
    }

    @Override
    public int getItemCount() {

        if(mCursor != null)
            return mCursor.getCount();

        else return 0;
    }

    public void swapCursor(Cursor data)
    {
        mCursor = data;
        imageData = new String[data.getCount()];
        notifyDataSetChanged();
        synchronized (this)
        {
            this.notifyAll();
        };
    }

    public void swapCursorAndSetFlag(Cursor data, boolean flag)
    {
        mCursor = data;
        imageData = new String[data.getCount()];
        notifyDataSetChanged();
        synchronized (this)
        {
            this.notifyAll();
        };

        activityFlag = flag;
        if(!activityFlag)
            dataColumnIndex = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        else dataColumnIndex = mCursor.getColumnIndexOrThrow(PicsContract.PicsEntry.PIC_DATA);
    }
}
