package net.dividedattention.crowdvision.expandedphoto;

import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidhuman.rxfirebase2.database.RxFirebaseDatabase;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.dividedattention.crowdvision.R;
import net.dividedattention.crowdvision.data.Photo;
import net.dividedattention.crowdvision.data.User;

import io.reactivex.disposables.CompositeDisposable;

public class ExpandedPhotoActivity extends AppCompatActivity{
    private static final String TAG = "ExpandedPhotoActivity";
    public static final String PHOTO_URL_KEY = "photo_url";
    public static final String TRANSITION_KEY = "transition_key";
    public static final String PHOTO_PATH_KEY = "photo_path";

    private int transitionName;
    private ImageView mFavImage;
    private TextView mLikesText;
    private String mPhotoPath;
    private User mCurrentUser;
    private Photo mCurrentPhoto;
    private DatabaseReference mPhotoReference;
    private DatabaseReference mUserReference;
    private String mPhotoKey;
    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expanded_photo);

        mCompositeDisposable = new CompositeDisposable();

        mPhotoPath = getIntent().getStringExtra(PHOTO_PATH_KEY);
        String[] splitPath = mPhotoPath.split("/");
        mPhotoKey = splitPath[splitPath.length - 1];

        String photoUrl = getIntent().getStringExtra(PHOTO_URL_KEY);
        ImageView imageView = (ImageView) findViewById(R.id.image);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setTransitionName(getIntent().getStringExtra(TRANSITION_KEY));
        }

        Glide.with(this)
                .load(photoUrl)
                .thumbnail(0.2f)
                .into(imageView);


        mFavImage = (ImageView) findViewById(R.id.fav_image);
        mLikesText = (TextView) findViewById(R.id.likes_text);


        mPhotoReference = FirebaseDatabase.getInstance().getReference().child(mPhotoPath);
        mUserReference = FirebaseDatabase.getInstance().getReference().child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());

                mCompositeDisposable.add(RxFirebaseDatabase
                        .dataChanges(mPhotoReference)
                        .map(dataSnapshot -> {
                            Log.d(TAG, "flatmap user: retrieved photo");

                            return dataSnapshot.getValue(Photo.class);
                        })
                        .subscribe(photo -> {
                            mCurrentPhoto = photo;
                            Log.d(TAG, "subscribed: Data changing");
                            Log.d(TAG, "subscribed: Likes: "+mCurrentPhoto.getLikes());
                            mLikesText.setText("Likes: " + mCurrentPhoto.getLikes());

                        }));

                mCompositeDisposable.add(RxFirebaseDatabase
                        .dataChanges(mUserReference)
                        .map(dataSnapshot -> {
                            User user = dataSnapshot.getValue(User.class);

                            //Create user object if it doesn't exist in Firebase
                            if (user == null)
                                user = new User();

                            Log.d(TAG, "flatmap user: retrieved user");
                            return user;
                        })
                        .subscribe(user -> {
                            mCurrentUser = user;
                            //Check if current user has liked this photo, and update view
                            if (mCurrentUser.getLikesList().contains(mPhotoKey)) {
                                mFavImage.setColorFilter(Color.argb(255, 255, 51, 51));
                            } else {
                                mFavImage.setColorFilter(Color.argb(255, 0, 0, 0));
                            }

                        }
                ));

        mFavImage.setOnClickListener(v -> toggleLike());

    }

    private void toggleLike() {

        if (mCurrentUser.getLikesList().contains(mPhotoKey)) {
            //User has already liked this photo
            Log.d(TAG, "User contained photo key " + mPhotoKey);
            mCurrentPhoto.setLikes(mCurrentPhoto.getLikes() - 1);
            mCurrentUser.getLikesList().remove(mPhotoKey);
        } else {
            //User hasn't liked this photo yet
            Log.d(TAG, "toggleLike: User didn't have photo key: " + mPhotoKey);
            mCurrentPhoto.setLikes(mCurrentPhoto.getLikes() + 1);
            mCurrentUser.getLikesList().add(mPhotoKey);
        }

        Log.d(TAG, "toggleLike: Likes: "+mCurrentPhoto.getLikes());

        //Update Firebase
        RxFirebaseDatabase.setValue(mPhotoReference,mCurrentPhoto)
                .mergeWith(RxFirebaseDatabase.setValue(mUserReference,mCurrentUser))
                .subscribe(() -> {});

//        mPhotoReference.setValue(mCurrentPhoto);
//        mUserReference.setValue(mCurrentUser);
//        mLikesText.setText("Likes: " + mCurrentPhoto.getLikes());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }
}
