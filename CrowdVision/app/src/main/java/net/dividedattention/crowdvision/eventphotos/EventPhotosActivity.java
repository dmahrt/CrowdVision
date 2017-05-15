package net.dividedattention.crowdvision.eventphotos;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import net.dividedattention.crowdvision.R;

public class EventPhotosActivity extends AppCompatActivity{
    private static final String TAG = "EventPhotosActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_photos);
        String eventTitle = getIntent().getStringExtra("eventTitle");
        String eventKey = getIntent().getStringExtra("eventKey");

        PhotoDisplayFragment displayFragment = (PhotoDisplayFragment)getSupportFragmentManager().findFragmentByTag("photos");

        if(displayFragment == null){
            displayFragment = PhotoDisplayFragment.newInstance(eventKey,eventTitle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, displayFragment, "photos")
                    .commit();
        }
    }
}
