package net.dividedattention.crowdvision.eventlist;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import net.dividedattention.crowdvision.R;
import net.dividedattention.crowdvision.eventphotos.EventPhotosActivity;
import net.dividedattention.crowdvision.data.CrowdEvent;

import java.util.List;

/**
 * Created by drewmahrt on 11/9/16.
 */

public class EventListRecyclerViewAdapter extends RecyclerView.Adapter<EventListRecyclerViewAdapter.EventViewHolder> {
    private List<CrowdEvent> mEvents;

    public EventListRecyclerViewAdapter(List<CrowdEvent> events){
        mEvents = events;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new EventViewHolder(inflater.inflate(R.layout.event_card,parent,false));
    }

    @Override
    public void onBindViewHolder(final EventViewHolder eventViewHolder, int position) {
        CrowdEvent crowdEvent = mEvents.get(position);
        eventViewHolder.titleTextView.setText(crowdEvent.getTitle());

        int numPhotos = 0;
        if(crowdEvent.getPhotos() != null)
            numPhotos = crowdEvent.getPhotos().size();

        eventViewHolder.numPhotosTextView.setText("Photo count: "+numPhotos);
        eventViewHolder.locationTextView.setText("Location: "+crowdEvent.getLocation() + " ("+crowdEvent.getCity()+")");
        eventViewHolder.endDateTextView.setText("End date: "+crowdEvent.getEndDate());

        eventViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(eventViewHolder.view.getContext(), EventPhotosActivity.class);
                intent.putExtra("eventKey",mEvents.get(eventViewHolder.getAdapterPosition()).getKey());
                intent.putExtra("eventTitle",mEvents.get(eventViewHolder.getAdapterPosition()).getTitle());
                eventViewHolder.view.getContext().startActivity(intent);
            }
        });

        //Log.d("EventListActivity","Loading cover: "+crowdEvent.getCoverImageUrl());
        Glide.with(eventViewHolder.view.getContext())
                .load(crowdEvent.getCoverImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .centerCrop()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        //Log.d("EventListActivity","Image is ready");
                        eventViewHolder.imageView.setImageDrawable(resource);
                        return false;
                    }
                })
                .into(eventViewHolder.imageView);

    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public void swapData(List<CrowdEvent> events) {
        mEvents = events;
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder{
        View view;
        ImageView imageView;
        TextView titleTextView, locationTextView, numPhotosTextView, endDateTextView;

        public EventViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            imageView = (ImageView)itemView.findViewById(R.id.event_image);
            titleTextView = (TextView)itemView.findViewById(R.id.title);
            locationTextView = (TextView)itemView.findViewById(R.id.location);
            numPhotosTextView = (TextView)itemView.findViewById(R.id.num_photos);
            endDateTextView = (TextView)itemView.findViewById(R.id.end_date);
        }
    }
}
