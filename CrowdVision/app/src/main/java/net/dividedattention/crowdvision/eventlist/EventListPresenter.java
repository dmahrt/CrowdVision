package net.dividedattention.crowdvision.eventlist;

import android.util.Log;

import net.dividedattention.crowdvision.data.CrowdEvent;
import net.dividedattention.crowdvision.data.events.EventsDataSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by drewmahrt on 5/21/17.
 */

public class EventListPresenter implements EventListContract.Presenter {
    private EventListContract.View mView;
    private EventsDataSource mDataSource;
    private CompositeDisposable mCompositeDisposable;

    private String mCity, mState;

    public EventListPresenter(EventListContract.View view, EventsDataSource dataSource) {
        mView = view;
        mDataSource = dataSource;
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public void start() {

    }

    @Override
    public void loadEvents(String city, String state) {
        mCity = city;
        mState = state;

        mDataSource
                .getEvents()
                .subscribe(event -> {
                    boolean isNearby = testNearby(event.getCity(),event.getState());
                    boolean isCurrent = testCurrentEvent(event.getEndDate());

                    if(mDataSource.cacheEvent(event,isNearby,isCurrent)){
                        if(isNearby && isCurrent)
                            mView.showNearbyEvent(event);
                        else if(isCurrent)
                            mView.showRemoteEvent(event);
                        else
                            mView.showExpiredEvent(event);
                    }
                }, throwable -> throwable.printStackTrace());
    }

    @Override
    public void cleanUp() {
        mCompositeDisposable.clear();
        mView = null;
    }

    @Override
    public List<CrowdEvent> getNearbyEvents() {
        return mDataSource.getNearbyEvents();
    }

    @Override
    public List<CrowdEvent> getRemoteEvents() {
        return mDataSource.getRemoteEvents();
    }

    @Override
    public List<CrowdEvent> getExpiredEvents() {
        return mDataSource.getExpiredEvents();
    }

    private boolean testNearby(String city, String state){
        return mCity.equals(city) && mState.equals(state);
    }

    private boolean testCurrentEvent(String date){
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date currentDateFormatted, eventDate;

        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);

        try {
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int year = cal.get(Calendar.YEAR);

            currentDateFormatted = sdf.parse(month+"/"+day+"/"+year);
            eventDate = sdf.parse(date);
            return !currentDateFormatted.after(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return true;
    }
}
