package net.dividedattention.crowdvision.adapters;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;


import net.dividedattention.crowdvision.PhotoClickListener;
import net.dividedattention.crowdvision.R;

/**
 * Created by drewmahrt on 5/11/16.
 */
public class EventImagesRecyclerViewAdapter extends FirebaseRecyclerAdapter<String,EventImagesRecyclerViewAdapter.EventViewHolder> {
    private Context mContext;
    private PhotoClickListener mListener;

    public EventImagesRecyclerViewAdapter(Class<String> modelClass, int modelLayout, Class<EventViewHolder> viewHolderClass, DatabaseReference ref, Context context, PhotoClickListener listener) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        mContext = context;
        mListener = listener;
    }


    @Override
    protected void populateViewHolder(final EventViewHolder eventViewHolder, String s, final int i) {
        Glide.with(mContext)
                .load(s)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(eventViewHolder.imageView);

        ViewCompat.setTransitionName(eventViewHolder.imageView,i+"_image");

        eventViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPhotoClicked(eventViewHolder,i);
            }
        });
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public EventViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.image);
        }
    }

}
