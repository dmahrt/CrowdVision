package net.dividedattention.crowdvision.expandedphoto;

import net.dividedattention.crowdvision.BasePresenter;
import net.dividedattention.crowdvision.BaseView;

/**
 * Created by drewmahrt on 5/19/17.
 */

public interface ExpandedPhotoContract {
    interface View extends BaseView<Presenter> {
        void showUpdatedLikeButton(boolean isLiked);
        void showUpdatedLikeCount(int count);
        void showImage(String photoUrl);
    }

    interface Presenter extends BasePresenter<View>{
        void toggleLikeStatus();
        void loadImageData(String photoPath);
    }
}
