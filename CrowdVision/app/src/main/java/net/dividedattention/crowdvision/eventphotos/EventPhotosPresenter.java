package net.dividedattention.crowdvision.eventphotos;

import android.net.Uri;
import android.util.Log;
import android.view.View;

import net.dividedattention.crowdvision.data.CrowdEvent;
import net.dividedattention.crowdvision.data.Photo;
import net.dividedattention.crowdvision.data.events.EventsDataSource;
import net.dividedattention.crowdvision.data.events.EventsRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.disposables.DisposableContainer;

/**
 * Created by drewmahrt on 5/21/17.
 */

public class EventPhotosPresenter implements EventPhotosContract.Presenter {
    private static final String TAG = "EventPhotosPresenter";

    private EventsDataSource mRepository;
    private EventPhotosContract.View mView;
    private CompositeDisposable mCompositeDisposable;


    public EventPhotosPresenter(EventPhotosContract.View view, EventsDataSource dataSource) {
        mView = view;
        mRepository = dataSource;
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public void start() {

    }

    private boolean isCurrentEvent(String date){
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
            Log.d(TAG, "testCurrentEvent: Event: "+eventDate.toString()+" Current: "+currentDateFormatted.toString());
            return !currentDateFormatted.after(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return true;
    }


    @Override
    public void loadEventInfo(String eventKey, String city, String state) {
        mRepository.getSingleEvent(eventKey)
                .subscribe(event -> {
                    boolean isValidEvent = city != null && state != null && state.equals(event.getState()) && city.equals(event.getCity()) && isCurrentEvent(event.getEndDate());
                    mView.showAddPhotoButton(isValidEvent);
                });

        Log.d(TAG, "loadEventInfo: "+eventKey);

        mCompositeDisposable.add(
            mRepository.getPhotos(eventKey)
                .subscribe(photo -> mView.showNewPhoto(photo),
                        throwable -> Log.d(TAG, "accept: Error loading photos"))
        );
    }

    @Override
    public void addPhotoClicked(String eventKey, Uri uri) {
        mRepository.addPhotoToEvent(eventKey,uri)
                .subscribe();
    }

    @Override
    public void cleanUp() {
        mCompositeDisposable.clear();
        mView = null;
    }
}
