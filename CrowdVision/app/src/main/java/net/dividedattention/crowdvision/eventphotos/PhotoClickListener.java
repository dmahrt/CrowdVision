package net.dividedattention.crowdvision.eventphotos;

import android.widget.ImageView;

/**
 * Created by drewmahrt on 7/29/16.
 */
public interface PhotoClickListener{
    void onPhotoClicked(String photoUrl, ImageView imageView, int position, String key);
}
