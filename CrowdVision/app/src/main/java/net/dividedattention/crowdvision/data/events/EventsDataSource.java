package net.dividedattention.crowdvision.data.events;

import android.graphics.Bitmap;
import android.net.Uri;

import net.dividedattention.crowdvision.data.CrowdEvent;
import net.dividedattention.crowdvision.data.Photo;
import net.dividedattention.crowdvision.data.User;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by drewmahrt on 5/15/17.
 */

public interface EventsDataSource {
    rx.Observable addEvent(String title, String location, String city, String state, String endDate, Bitmap image);

    void addPhoto();

    Bitmap getImageFromGallery(Uri uri);

    Observable<CrowdEvent> getEvents();

    Observable<Photo> getPhotos();

    Observable<Photo> getIndividualPhotos();

    Observable<User> getUser();


}
