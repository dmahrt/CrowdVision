package net.dividedattention.crowdvision.activities;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import net.dividedattention.crowdvision.fragments.ExpandedPhotoFragment;
import net.dividedattention.crowdvision.fragments.PhotoDisplayFragment;
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
        ExpandedPhotoFragment expanded = (ExpandedPhotoFragment)getSupportFragmentManager().findFragmentByTag("expanded");

        Log.d(TAG, "onCreate: backstack count: "+getSupportFragmentManager().getBackStackEntryCount());

        if(displayFragment == null){
            Log.d(TAG, "onCreate: First time launching");
            PhotoDisplayFragment fragment = PhotoDisplayFragment.newInstance(eventKey,eventTitle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, fragment,"photos")
                    .commit();
        }else{
            //Fragment already exists
            Log.d(TAG, "onCreate: Photo Display Fragment already exists");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, displayFragment,"photos")
                    .commit();

            if(expanded != null) {
                Log.d(TAG, "onCreate: Expanded Fragment already exists");
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.container, expanded,"expanded")
                        .commit();
            }
        }
    }
}
