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

import java.util.List;

/**
 * Created by drewmahrt on 5/11/16.
 */
public class EventPhotosRecyclerViewAdapter extends RecyclerView.Adapter<EventPhotosRecyclerViewAdapter.ImageViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private PhotoClickListener mListener;
    private List<Photo> mPhotoList;

    public EventPhotosRecyclerViewAdapter(List items, PhotoClickListener listener) {
        mListener = listener;
        mPhotoList = items;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ImageViewHolder(inflater.inflate(R.layout.photo_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder imageViewHolder, final int position) {

        Log.d(TAG, "onBindViewHolder: url: " + mPhotoList.get(position).getPhotoUrl());
        Log.d(TAG, "onBindViewHolder: "+imageViewHolder.getAdapterPosition());

        imageViewHolder.progressBar.setVisibility(View.VISIBLE);
        Glide.with(imageViewHolder.imageView.getContext())
                .load(mPhotoList.get(imageViewHolder.getAdapterPosition()).getPhotoUrl())
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

        ViewCompat.setTransitionName(imageViewHolder.imageView, position + "_image");

        imageViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = imageViewHolder.getAdapterPosition();
                mListener.onPhotoClicked(mPhotoList.get(currentPosition).getPhotoUrl(), imageViewHolder.imageView, position, mPhotoList.get(currentPosition).getKey());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPhotoList.size();
    }


    public void addPhoto(Photo photo) {
        mPhotoList.add(0, photo);
        notifyItemInserted(0);
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public ProgressBar progressBar;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        }
    }

}
