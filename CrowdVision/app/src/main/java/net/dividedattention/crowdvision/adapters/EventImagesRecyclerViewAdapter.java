package net.dividedattention.crowdvision.adapters;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;


import net.dividedattention.crowdvision.models.Photo;
import net.dividedattention.crowdvision.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by drewmahrt on 5/11/16.
 */
public class EventImagesRecyclerViewAdapter extends RecyclerView.Adapter<EventImagesRecyclerViewAdapter.EventViewHolder> {
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
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new EventViewHolder(inflater.inflate(R.layout.photo_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(final EventViewHolder eventViewHolder, final int position) {
        Glide.with(eventViewHolder.imageView.getContext())
                .load(mItems.get(position).getPhotoUrl())
                .into(eventViewHolder.imageView);


        ViewCompat.setTransitionName(eventViewHolder.imageView,position+"_image");

        eventViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPhotoClicked(eventViewHolder,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        //Log.d(TAG, "getItemCount: "+mItems.size());
        return mItems.size();
    }

    public void addKey(int i, String s) {
        mKeys.add(i,s);
    }

    public String getKey(int i){
        return mKeys.get(i);
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public EventViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.image);
        }
    }

}
