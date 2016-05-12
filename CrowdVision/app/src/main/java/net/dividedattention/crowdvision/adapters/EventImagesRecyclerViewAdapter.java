package net.dividedattention.crowdvision.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;

import net.dividedattention.crowdvision.CrowdEvent;
import net.dividedattention.crowdvision.R;

/**
 * Created by drewmahrt on 5/11/16.
 */
public class EventImagesRecyclerViewAdapter extends FirebaseRecyclerAdapter<CrowdEvent,EventImagesRecyclerViewAdapter.EventViewHolder> {
    private Context context;

    public EventImagesRecyclerViewAdapter(Class<CrowdEvent> modelClass, int modelLayout, Class<EventViewHolder> viewHolderClass, Firebase ref, Context context) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.context = context;
    }

    @Override
    protected void populateViewHolder(EventViewHolder eventViewHolder, CrowdEvent crowdEvent, int i) {
        Glide.with(context)
                .load(crowdEvent.getPhotoUrls().values().toArray()[i])
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(eventViewHolder.imageView);
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public EventViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.event_image);
        }
    }
}
