package net.dividedattention.crowdvision.adapters;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


import net.dividedattention.crowdvision.DynamicHeightImageView;
import net.dividedattention.crowdvision.models.Photo;
import net.dividedattention.crowdvision.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by drewmahrt on 5/11/16.
 */
public class EventImagesRecyclerViewAdapter extends RecyclerView.Adapter<EventImagesRecyclerViewAdapter.ImageViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private PhotoClickListener mListener;
    private List<Photo> mItems;
    private List<String> mKeys;

    public EventImagesRecyclerViewAdapter(List items, PhotoClickListener listener) {
        mListener = listener;
        mItems = items;
        mKeys = new ArrayList<>();
    }




    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ImageViewHolder(inflater.inflate(R.layout.photo_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder eventViewHolder, final int position) {

        Glide.with(eventViewHolder.imageView.getContext())
                .load(mItems.get(position).getPhotoUrl())
                .thumbnail(0.2f)
                .into(eventViewHolder.imageView);


        ViewCompat.setTransitionName(eventViewHolder.imageView,position+"_image");

        eventViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPhotoClicked(mItems.get(position).getPhotoUrl(),eventViewHolder.imageView,position,mKeys.get(position));
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return mKeys.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void addKey(int i, String s) {
        mKeys.add(i,s);
    }

    public String getKey(int i){
        return mKeys.get(i);
    }

    public void removeKey(int i){ mKeys.remove(i); }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        //public DynamicHeightImageView imageView;
        public ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.image);
        }

//        @Override
//        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//            // Calculate the image ratio of the loaded bitmap
//            float ratio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
//            // Set the ratio for the image
//            imageView.setHeightRatio(ratio);
//            // Load the image into the view
//            imageView.setImageBitmap(bitmap);
//        }
//
//        @Override
//        public void onBitmapFailed(Drawable errorDrawable) {
//
//        }
//
//        @Override
//        public void onPrepareLoad(Drawable placeHolderDrawable) {
//            imageView.setImageDrawable(placeHolderDrawable);
//        }
    }

}
