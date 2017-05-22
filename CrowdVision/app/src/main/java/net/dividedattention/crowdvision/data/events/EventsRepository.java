package net.dividedattention.crowdvision.data.events;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androidhuman.rxfirebase2.database.ChildAddEvent;
import com.androidhuman.rxfirebase2.database.ChildEvent;
import com.androidhuman.rxfirebase2.database.RxFirebaseDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kelvinapps.rxfirebase.RxFirebaseStorage;

import net.dividedattention.crowdvision.R;
import net.dividedattention.crowdvision.data.CrowdEvent;
import net.dividedattention.crowdvision.data.Photo;
import net.dividedattention.crowdvision.data.User;
import net.dividedattention.crowdvision.eventphotos.EventPhotosActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by drewmahrt on 5/15/17.
 */

public class EventsRepository implements EventsDataSource {
    private static final String TAG = "EventsRepository";
    private static List<CrowdEvent> mCurrentEvents, mRemoteEvents, mExpiredEvents;

    private static EventsRepository instance = null;
    private Context mContext;

    private EventsRepository(Context context) {
        mContext = context;
    }

    public static EventsRepository getInstance(Context context) {
        if (instance == null) {
            instance = new EventsRepository(context.getApplicationContext());
            mCurrentEvents = new ArrayList<>();
            mRemoteEvents = new ArrayList<>();
            mExpiredEvents = new ArrayList<>();
        }
        return instance;
    }

    @Override
    public rx.Observable addEvent(String title, String location, String city, String state, String endDate, Bitmap image) {
        DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference().child("events");
        ByteArrayOutputStream baos = null;

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(mContext.getString(R.string.firebase_storage));
        StorageReference spaceRef = storageRef.child("images/" + System.currentTimeMillis() + "_" + image.getByteCount() + ".jpg");

        baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        final byte[] imageData = baos.toByteArray();


        return RxFirebaseStorage.putBytes(spaceRef, imageData)
                .flatMap(taskSnapshot -> {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    String imagePath = downloadUrl.toString();
                    Log.d(TAG, "onSuccess: " + imagePath);

                    CrowdEvent event = new CrowdEvent(title,
                            location,
                            city,
                            state,
                            endDate,
                            null,
                            imagePath);
                    DatabaseReference subRef = firebaseRef.push();
                    event.setKey(subRef.getKey());
                    return rx.Observable.just(subRef.setValue(event));
                });
    }

    @Override
    public Completable changeLikeStatus(String photoPath, Photo photo, User user) {
        DatabaseReference photoReference = FirebaseDatabase.getInstance().getReference().child(photoPath);
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());

        return RxFirebaseDatabase.setValue(photoReference, photo)
                .mergeWith(RxFirebaseDatabase.setValue(userReference, user));
    }

    @Override
    public rx.Observable addPhotoToEvent(String eventKey, Uri uri) {

        try {
            Bitmap selectedImage = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(mContext.getString(R.string.firebase_storage));
            StorageReference spaceRef = storageRef.child("images/" + System.currentTimeMillis() + "_" + selectedImage.getByteCount() + ".jpg");

            DatabaseReference photosRef = FirebaseDatabase.getInstance().getReference().child("events/" + eventKey + "/photos");

            Log.d(TAG, "onActivityResult: Attempting to upload image");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            final byte[] imageData = baos.toByteArray();


            return RxFirebaseStorage.putBytes(spaceRef, imageData)
                    .flatMap(taskSnapshot -> {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        String imagePath = downloadUrl.toString();
                        Log.d(TAG, "onSuccess: " + imagePath);

                        Photo photo = new Photo(imagePath, 0);
                        DatabaseReference subRef = photosRef.push();
                        photo.setKey(subRef.getKey());
                        return rx.Observable.just(subRef.setValue(photo));
                    });
        } catch (IOException e) {
            e.printStackTrace();
            return rx.Observable.error(e);
        }

    }


    @Override
    public Bitmap getImageFromGallery(Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Observable<CrowdEvent> getEvents() {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("events");
        return RxFirebaseDatabase
                .childEvents(eventsRef)
                .ofType(ChildAddEvent.class)
                .map(childAddEvent -> childAddEvent.dataSnapshot().getValue(CrowdEvent.class));
    }

    @Override
    public Single<CrowdEvent> getSingleEvent(String eventKey) {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child("events/" + eventKey);

        return RxFirebaseDatabase
                .dataOf(eventRef, CrowdEvent.class);
    }

    @Override
    public Observable<Photo> getPhotos(String eventKey) {
        DatabaseReference photosRef = FirebaseDatabase.getInstance().getReference().child("events/" + eventKey + "/photos");
        return RxFirebaseDatabase
                .childEvents(photosRef)
                .ofType(ChildAddEvent.class)
                .map(childEvent -> childEvent.dataSnapshot().getValue(Photo.class));

    }

    @Override
    public Observable<Photo> getIndividualPhoto(String photoPath) {
        DatabaseReference photoReference = FirebaseDatabase.getInstance().getReference().child(photoPath);

        return RxFirebaseDatabase
                .dataChanges(photoReference)
                .map(dataSnapshot -> dataSnapshot.getValue(Photo.class));

    }

    @Override
    public Observable<User> getUser() {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());

        return RxFirebaseDatabase
                .dataChanges(userReference)
                .map(dataSnapshot -> {
                    User user = dataSnapshot.getValue(User.class);

                    //Create user object if it doesn't exist in Firebase
                    if (user == null)
                        user = new User();

                    Log.d(TAG, "flatmap user: retrieved user");
                    return user;
                });
    }

    @Override
    public boolean cacheEvent(CrowdEvent event, boolean isNearby, boolean isCurrent) {
        if(!isCurrent && !mExpiredEvents.contains(event)){
            mExpiredEvents.add(event);
            return true;
        } else if (isNearby && isCurrent && !mCurrentEvents.contains(event)) {
            mCurrentEvents.add(event);
            return true;
        } else if (!isNearby && isCurrent && !mRemoteEvents.contains(event)) {
            mRemoteEvents.add(event);
            return true;
        }
        return false;
    }

    @Override
    public List<CrowdEvent> getNearbyEvents() {
        return mCurrentEvents;
    }

    @Override
    public List<CrowdEvent> getRemoteEvents() {
        return mRemoteEvents;
    }

    @Override
    public List<CrowdEvent> getExpiredEvents() {
        return mExpiredEvents;
    }
}
