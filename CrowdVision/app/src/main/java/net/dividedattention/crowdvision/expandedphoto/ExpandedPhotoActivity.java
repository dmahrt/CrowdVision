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
import net.dividedattention.crowdvision.data.events.EventsRepository;
import net.dividedattention.crowdvision.eventcreate.CreateEventContract;
import net.dividedattention.crowdvision.eventcreate.CreateEventPresenter;

import io.reactivex.disposables.CompositeDisposable;

public class ExpandedPhotoActivity extends AppCompatActivity implements ExpandedPhotoContract.View {
    private static final String TAG = "ExpandedPhotoActivity";
    public static final String PHOTO_URL_KEY = "photo_url";
    public static final String TRANSITION_KEY = "transition_key";
    public static final String PHOTO_PATH_KEY = "photo_path";

    private ImageView mFavImage;
    private TextView mLikesText;

    private ExpandedPhotoContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expanded_photo);

        mFavImage = (ImageView) findViewById(R.id.fav_image);
        mLikesText = (TextView) findViewById(R.id.likes_text);

        attachPresenter();

        ImageView imageView = (ImageView) findViewById(R.id.image);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setTransitionName(getIntent().getStringExtra(TRANSITION_KEY));
        }

        mFavImage.setOnClickListener(v -> mPresenter.toggleLikeStatus());

    }

    private void attachPresenter() {
        mPresenter = (ExpandedPhotoContract.Presenter) getLastCustomNonConfigurationInstance();
        if (mPresenter == null) {
            mPresenter = new ExpandedPhotoPresenter(EventsRepository.getInstance(this));
        }
        mPresenter.attachView(this);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mPresenter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.loadImageData(getIntent().getStringExtra(PHOTO_PATH_KEY));
    }


    //View contract methods

    @Override
    public void setPresenter(ExpandedPhotoContract.Presenter presenter) {

    }

    @Override
    public void showUpdatedLikeButton(boolean isLiked) {
        if (isLiked) {
            mFavImage.setColorFilter(Color.argb(255, 255, 51, 51));
        } else {
            mFavImage.setColorFilter(Color.argb(255, 0, 0, 0));
        }
    }

    @Override
    public void showUpdatedLikeCount(int count) {
        mLikesText.setText("Likes: " + count);
    }

    @Override
    public void showImage(String photoUrl) {
        ImageView imageView = (ImageView) findViewById(R.id.image);
        Glide.with(this)
                .load(photoUrl)
                .thumbnail(0.2f)
                .into(imageView);
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }
}
