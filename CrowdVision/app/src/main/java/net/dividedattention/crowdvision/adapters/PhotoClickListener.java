package net.dividedattention.crowdvision.adapters;

import net.dividedattention.crowdvision.adapters.EventImagesRecyclerViewAdapter;

/**
 * Created by drewmahrt on 7/29/16.
 */
public interface PhotoClickListener{
    void onPhotoClicked(EventImagesRecyclerViewAdapter.EventViewHolder viewHolder, int position);
}
