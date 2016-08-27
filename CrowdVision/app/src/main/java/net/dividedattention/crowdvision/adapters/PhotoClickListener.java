package net.dividedattention.crowdvision.adapters;

import android.support.v7.widget.RecyclerView;

import net.dividedattention.crowdvision.adapters.EventImagesRecyclerViewAdapter;

/**
 * Created by drewmahrt on 7/29/16.
 */
public interface PhotoClickListener{
    void onPhotoClicked(RecyclerView.ViewHolder viewHolder, int position);
}
