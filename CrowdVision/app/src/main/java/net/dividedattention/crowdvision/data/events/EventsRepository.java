package net.dividedattention.crowdvision.data.events;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.androidhuman.rxfirebase2.database.RxFirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kelvinapps.rxfirebase.RxFirebaseStorage;

import net.dividedattention.crowdvision.R;
import net.dividedattention.crowdvision.data.CrowdEvent;
import net.dividedattention.crowdvision.data.Photo;
import net.dividedattention.crowdvision.data.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by drewmahrt on 5/15/17.
 */

public class EventsRepository implements EventsDataSource {
    private static final String TAG = "EventsRepository";

    private static EventsRepository instance = null;
    private Context mContext;

    private EventsRepository(Context context) {
        mContext = context;
    }

    public static EventsRepository getInstance(Context context) {
        if (instance == null) {
            instance = new EventsRepository(context.getApplicationContext());
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

        return RxFirebaseDatabase.setValue(photoReference,photo)
                .mergeWith(RxFirebaseDatabase.setValue(userReference,user));
    }

    @Override
    public void addPhoto() {

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
        return null;
    }

    @Override
    public Observable<Photo> getPhotos() {
        return null;
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
}
