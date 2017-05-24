package com.example.android.obscured;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by Shubham on 21-05-2017.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageAdapterViewHolder> {

    Cursor mCursor = null;
    Context mContext;
    ImageOnClickHandler imageOnClickHandler = null;
    int columnIndex;

    public interface ImageOnClickHandler{
        void OnClick(Cursor rowCursor);
    }

    public ImageAdapter(Context context, ImageOnClickHandler imageOnClickHandler)
    {
        mContext = context;
        this.imageOnClickHandler = imageOnClickHandler;
    }

    public class ImageAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView imageView;

        public ImageAdapterViewHolder(View view)
        {
            super(view);
            imageView = (ImageView)view.findViewById(R.id.iv_image);
            imageView.setOnClickListener(this);
            columnIndex = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        }

        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();
            if(mCursor != null)
            {
                mCursor.moveToPosition(adapterPosition);
                imageOnClickHandler.OnClick(mCursor);
            }
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
    public void onBindViewHolder(ImageAdapterViewHolder holder, int position) {

        mCursor.moveToPosition(position);
        int imageId = mCursor.getInt(columnIndex);
        Uri uri = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, ""+imageId);
        //Glide.with(mContext).load(uri).into(holder.imageView);
        holder.imageView.setImageURI(Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + imageId));
        holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    @Override
    public int getItemCount() {

        int size;
        if(mCursor != null)
        {
            size = mCursor.getCount();
            return size;
        }
        else return 0;
    }

    public void swapCursor(Cursor data)
    {
        mCursor = data;
        mCursor.moveToFirst();
        notifyDataSetChanged();
    }
}
