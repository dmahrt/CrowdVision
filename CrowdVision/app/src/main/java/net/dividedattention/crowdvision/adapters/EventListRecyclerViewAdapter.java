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
public class EventListRecyclerViewAdapter extends FirebaseRecyclerAdapter<CrowdEvent,EventListRecyclerViewAdapter.EventViewHolder> {
    private Context context;

    public EventListRecyclerViewAdapter(Class<CrowdEvent> modelClass, int modelLayout, Class<EventViewHolder> viewHolderClass, Firebase ref, Context context) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.context = context;
    }

    @Override
    protected void populateViewHolder(EventViewHolder eventViewHolder, CrowdEvent crowdEvent, int i) {
        eventViewHolder.titleTextView.setText(crowdEvent.getTitle());

        int numPhotos = 0;
        if(crowdEvent.getPhotoUrls() != null)
            numPhotos = crowdEvent.getPhotoUrls().size();

        eventViewHolder.numPhotosTextView.setText("Photo count: "+numPhotos);
        eventViewHolder.locationTextView.setText("Location: "+crowdEvent.getLocation());
        eventViewHolder.endDateTextView.setText("End date: "+crowdEvent.getEndDate());

        Glide.with(context)
                .load(crowdEvent.getCoverImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(eventViewHolder.imageView);
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView, locationTextView, numPhotosTextView, endDateTextView;

        public EventViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.event_image);
            titleTextView = (TextView)itemView.findViewById(R.id.title);
            locationTextView = (TextView)itemView.findViewById(R.id.location);
            numPhotosTextView = (TextView)itemView.findViewById(R.id.num_photos);
            endDateTextView = (TextView)itemView.findViewById(R.id.end_date);
        }
    }
}
