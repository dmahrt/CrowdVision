package net.dividedattention.crowdvision.eventphotos;

import android.net.Uri;

import net.dividedattention.crowdvision.BasePresenter;
import net.dividedattention.crowdvision.BaseView;
import net.dividedattention.crowdvision.data.Photo;

/**
 * Created by drewmahrt on 5/19/17.
 */

public interface EventPhotosContract {
    interface View extends BaseView<Presenter> {
        void showAddPhotoButton(boolean shouldShow);
        void showNewPhoto(Photo photo);
    }

    interface Presenter extends BasePresenter{
        void loadEventInfo(String eventKey, String city, String state);
        void addPhotoClicked(String eventKey, Uri uri);
        void cleanUp();
    }
}
