package net.dividedattention.crowdvision;

/**
 * Created by drewmahrt on 5/15/17.
 */

public interface BasePresenter<V extends BaseView> {
    void attachView(V view);
    void detachView();
}
