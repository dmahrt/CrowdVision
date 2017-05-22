package net.dividedattention.crowdvision.data.events;

import android.graphics.Bitmap;
import android.net.Uri;

import com.androidhuman.rxfirebase2.database.ChildEvent;

import net.dividedattention.crowdvision.data.CrowdEvent;
import net.dividedattention.crowdvision.data.Photo;
import net.dividedattention.crowdvision.data.User;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by drewmahrt on 5/15/17.
 */

public interface EventsDataSource {
    rx.Observable addEvent(String title, String location, String city, String state, String endDate, Bitmap image);

    Completable changeLikeStatus(String photoPath, Photo photo, User user);

    rx.Observable addPhotoToEvent(String eventKey, Uri uri);

    Bitmap getImageFromGallery(Uri uri);

    Observable<CrowdEvent> getEvents();

    Single<CrowdEvent> getSingleEvent(String eventKey);

    Observable<Photo> getPhotos(String eventKey);

    Observable<Photo> getIndividualPhoto(String photoPath);

    Observable<User> getUser();

    boolean cacheEvent(CrowdEvent event,boolean isNearby,boolean isCurrent);

    List<CrowdEvent> getNearbyEvents();

    List<CrowdEvent> getRemoteEvents();

    List<CrowdEvent> getExpiredEvents();
}
