package net.dividedattention.crowdvision.eventcreate;

import android.graphics.Bitmap;
import android.net.Uri;

import net.dividedattention.crowdvision.data.events.EventsDataSource;

/**
 * Created by drewmahrt on 5/15/17.
 */

public class CreateEventPresenter implements CreateEventContract.Presenter {
    private CreateEventContract.View mView;
    private EventsDataSource mEventsDataSource;


    public CreateEventPresenter(EventsDataSource dataSource) {
        mEventsDataSource = dataSource;
    }

    @Override
    public void checkImageRatio(Bitmap selectedImage) {
        if(mView != null) {
            if (selectedImage.getHeight() >= selectedImage.getWidth()) {
                mView.showPhotoRatioWarning();
            } else {
                mView.dismissRationWarning();
            }
        }
    }

    @Override
    public void retrieveGalleryImage(Uri uri) {
        if(mView != null)
            mView.showGalleryImage(mEventsDataSource.getImageFromGallery(uri));
    }

    @Override
    public void saveEvent(String title, String location, String city, String state, String endDate, Bitmap image) {
        if(mView != null && title.isEmpty() || location == null || city == null || state == null || endDate == null || image == null)
            mView.showIncompleteErrors(!title.isEmpty(), endDate != null, location != null, image != null);
        else{
            mEventsDataSource.addEvent(title,location,city,state,endDate,image)
                    .subscribe(result -> {if(mView != null)mView.completePhotoUpload();},
                            error -> {if(mView!=null)mView.showUploadError();});
        }
    }

    @Override
    public void attachView(CreateEventContract.View view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }
}
