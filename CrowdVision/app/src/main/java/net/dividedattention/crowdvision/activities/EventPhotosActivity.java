package net.dividedattention.crowdvision.activities;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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
        for(int entry = 0; entry < getSupportFragmentManager().getBackStackEntryCount(); entry++){
            Log.d(TAG, "Found fragment: " + getSupportFragmentManager().getBackStackEntryAt(entry).getId());
        }

        if(displayFragment == null){
            Log.d(TAG, "onCreate: First time launching");
            Log.d(TAG, "onCreate: Adding photo fragment");
            displayFragment = PhotoDisplayFragment.newInstance(eventKey,eventTitle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, displayFragment, "photos")
                    .commit();
        }

        if(expanded != null){
            //getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().popBackStack();
            Log.d(TAG, "onCreate: backstack count before re-adding expanded: "+getSupportFragmentManager().getBackStackEntryCount());

            Log.d(TAG, "onCreate: Expanded Fragment already exists");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, expanded,"expanded")
                    .addToBackStack("photos")
                    .commit();

            Log.d(TAG, "onCreate: backstack count after re-adding expanded: "+getSupportFragmentManager().getBackStackEntryCount());

        }
    }
}
