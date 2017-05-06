package net.dividedattention.crowdvision.activities;

import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.dividedattention.crowdvision.R;
import net.dividedattention.crowdvision.models.Photo;
import net.dividedattention.crowdvision.models.User;

import static java.security.AccessController.getContext;

public class ExpandedPhotoActivity extends AppCompatActivity implements View.OnClickListener {
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
    private boolean mPhotoLoaded;
    private boolean mUserLoaded;
    private String mPhotoKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expanded_photo);

        mPhotoPath = getIntent().getStringExtra(PHOTO_PATH_KEY);
        String[] splitPath = mPhotoPath.split("/");
        mPhotoKey = splitPath[splitPath.length - 1];
        mPhotoLoaded = false;
        mUserLoaded = false;

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
        mFavImage.setOnClickListener(this);
        mLikesText = (TextView) findViewById(R.id.likes_text);


        DatabaseReference photoReference = FirebaseDatabase.getInstance().getReference().child(mPhotoPath);
        photoReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mPhotoLoaded = true;
                mCurrentPhoto = dataSnapshot.getValue(Photo.class);
                mLikesText.setText("Likes: " + mCurrentPhoto.getLikes());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCurrentUser = dataSnapshot.getValue(User.class);
                if (mCurrentUser == null) {
                    mCurrentUser = new User();
                }
                mUserLoaded = true;
                if (mCurrentUser.getLikesList().contains(mPhotoKey)) {
                    mFavImage.setColorFilter(Color.argb(255, 255, 51, 51));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fav_image:
                pressFavorite();
                break;
        }
    }

    private void pressFavorite() {
        Log.d(TAG, "pressFavorite: ");
        if (mPhotoLoaded && mUserLoaded) {
            toggleLike();
        } else {
            Toast.makeText(this, "Sorry, something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleLike() {
        if (mPhotoLoaded && mUserLoaded) {
            Log.d(TAG, "toggleLike: " + mPhotoPath);

            if (mCurrentUser.getLikesList().contains(mPhotoKey)) {
                //User has already liked this photo
                Log.d(TAG, "User contained photo key " + mPhotoKey);
                mCurrentPhoto.setLikes(mCurrentPhoto.getLikes() - 1);
                mCurrentUser.getLikesList().remove(mPhotoKey);
                mFavImage.setColorFilter(Color.argb(255, 0, 0, 0));
            } else {
                //User hasn't liked this photo yet
                Log.d(TAG, "toggleLike: User didn't have photo key: " + mPhotoKey);
                mCurrentPhoto.setLikes(mCurrentPhoto.getLikes() + 1);
                mCurrentUser.getLikesList().add(mPhotoKey);
                mFavImage.setColorFilter(Color.argb(255, 255, 51, 51));
            }

            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
            DatabaseReference photoReference = FirebaseDatabase.getInstance().getReference().child(mPhotoPath);
            userReference.setValue(mCurrentUser);
            photoReference.setValue(mCurrentPhoto);
            mLikesText.setText("Likes: " + mCurrentPhoto.getLikes());

        }
    }
}
