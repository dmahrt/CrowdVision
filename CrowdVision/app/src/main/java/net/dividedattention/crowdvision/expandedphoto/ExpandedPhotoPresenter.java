package net.dividedattention.crowdvision.expandedphoto;

import android.graphics.Color;
import android.util.Log;

import net.dividedattention.crowdvision.data.Photo;
import net.dividedattention.crowdvision.data.User;
import net.dividedattention.crowdvision.data.events.EventsDataSource;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by drewmahrt on 5/21/17.
 */

public class ExpandedPhotoPresenter implements ExpandedPhotoContract.Presenter {
    private static final String TAG = "ExpandedPhotoPresenter";
    private ExpandedPhotoContract.View mView;
    private EventsDataSource mEventsDataSource;

    private CompositeDisposable mCompositeDisposable;

    private Photo mCurrentPhoto;
    private User mCurrentUser;
    private String mPhotoPath, mPhotoKey;

    public ExpandedPhotoPresenter(ExpandedPhotoContract.View view, EventsDataSource eventsDataSource) {
        mView = view;
        mEventsDataSource = eventsDataSource;
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public void start() {
    }

    @Override
    public void toggleLikeStatus() {
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

        mEventsDataSource
                .changeLikeStatus(mPhotoPath,mCurrentPhoto,mCurrentUser)
                .subscribe();
    }

    @Override
    public void loadImageData(String photoPath) {
        mPhotoPath = photoPath;
        String[] splitPath = mPhotoPath.split("/");
        mPhotoKey = splitPath[splitPath.length - 1];

        //setup firebase listeners
        mCompositeDisposable.add(
                mEventsDataSource
                        .getIndividualPhoto(mPhotoPath)
                        .subscribe(photo -> {
                            mCurrentPhoto = photo;
                            Log.d(TAG, "subscribed: Data changing");
                            Log.d(TAG, "subscribed: Likes: "+mCurrentPhoto.getLikes());
                            mView.showImage(mCurrentPhoto.getPhotoUrl());
                            mView.showUpdatedLikeCount(mCurrentPhoto.getLikes());
                        }
                )
        );

        mCompositeDisposable.add(
                mEventsDataSource
                .getUser()
                .subscribe(user -> {
                    mCurrentUser = user;
                    mView.showUpdatedLikeButton(mCurrentUser.getLikesList().contains(mPhotoKey));
                })
        );
    }

    @Override
    public void cleanUp() {
        mCompositeDisposable.clear();
        mView = null;
    }
}
