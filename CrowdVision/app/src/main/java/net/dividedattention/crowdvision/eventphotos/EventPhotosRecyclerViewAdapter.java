package net.dividedattention.crowdvision.eventphotos;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;


import net.dividedattention.crowdvision.data.Photo;
import net.dividedattention.crowdvision.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by drewmahrt on 5/11/16.
 */
public class EventPhotosRecyclerViewAdapter extends RecyclerView.Adapter<EventPhotosRecyclerViewAdapter.ImageViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private PhotoClickListener mListener;
    private List<Photo> mItems;
    private List<String> mKeys;

    public EventPhotosRecyclerViewAdapter(List items, PhotoClickListener listener) {
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
    public void onBindViewHolder(final ImageViewHolder imageViewHolder, final int position) {
        Log.d(TAG, "onBindViewHolder: Entering "+mItems.get(position).getHeight());

//        if(mItems.get(position).getHeight() > 0){
//            Log.d(TAG, "onBindViewHolder: retrieving bounds");
//            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)imageViewHolder.imageView.getLayoutParams();
//            float ratio = mItems.get(position).getHeight()/mItems.get(position).getWidth();
//            rlp.height = (int)(rlp.width * ratio);
//            imageViewHolder.imageView.setLayoutParams(rlp);
//        }

//        Picasso.with(imageViewHolder.imageView.getContext())
//                .load(mItems.get(position).getPhotoUrl())
//                .into(imageViewHolder.imageView, new Callback() {
//                    @Override
//                    public void onSuccess() {
//                        float width = imageViewHolder.imageView.getWidth();
//                        float height = imageViewHolder.imageView.getHeight();
//                        Log.d(TAG, "onResourceReady: Setting image bounds "+position+" with height "+height);
//                        mItems.get(position).setHeight(height);
//                        mItems.get(position).setWidth(width);
//                    }
//
//                    @Override
//                    public void onError() {
//
//                    }
//                });
        imageViewHolder.progressBar.setVisibility(View.VISIBLE);
        Glide.with(imageViewHolder.imageView.getContext())
                .load(mItems.get(position).getPhotoUrl())
                .thumbnail(0.2f)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFirstResource) {
                        imageViewHolder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        imageViewHolder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageViewHolder.imageView);

        ViewCompat.setTransitionName(imageViewHolder.imageView,position+"_image");

        imageViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPhotoClicked(mItems.get(position).getPhotoUrl(),imageViewHolder.imageView,position,mKeys.get(position));
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
        public ImageView imageView;
        public ProgressBar progressBar;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.image);
            progressBar = (ProgressBar)itemView.findViewById(R.id.progress_bar);
        }
    }

}
