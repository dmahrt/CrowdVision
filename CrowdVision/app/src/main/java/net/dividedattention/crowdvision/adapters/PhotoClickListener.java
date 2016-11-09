package net.dividedattention.crowdvision.adapters;

import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import net.dividedattention.crowdvision.adapters.EventImagesRecyclerViewAdapter;

/**
 * Created by drewmahrt on 7/29/16.
 */
public interface PhotoClickListener{
    void onPhotoClicked(String photoUrl, ImageView imageView, int position, String key);
}
