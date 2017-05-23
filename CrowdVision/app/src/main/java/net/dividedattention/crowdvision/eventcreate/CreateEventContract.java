package net.dividedattention.crowdvision.eventcreate;

import android.graphics.Bitmap;
import android.net.Uri;

import net.dividedattention.crowdvision.BasePresenter;
import net.dividedattention.crowdvision.BaseView;

/**
 * Created by drewmahrt on 5/15/17.
 */

public interface CreateEventContract {
    interface View extends BaseView<Presenter>{
        void showPhotoRatioWarning();
        void dismissRationWarning();
        void showIncompleteErrors(boolean titleValid, boolean dateValid, boolean locationValid, boolean photoValid);
        void completePhotoUpload();
        void showGalleryImage(Bitmap bitmap);
        void showUploadError();
    }

    interface Presenter extends BasePresenter<View>{
        void checkImageRatio(Bitmap bitmap);
        void retrieveGalleryImage(Uri uri);
        void saveEvent(String title, String location, String city, String state, String endDate, Bitmap image);
    }
}
