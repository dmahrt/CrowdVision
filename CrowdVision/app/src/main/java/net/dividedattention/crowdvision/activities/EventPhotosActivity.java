package net.dividedattention.crowdvision.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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

        Fragment f = getFragmentManager().findFragmentById(R.id.container);
        if(f == null){
            PhotoDisplayFragment fragment = PhotoDisplayFragment.newInstance(eventKey,eventTitle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }else{
            //Fragment already exists
            Log.d(TAG, "onCreate: Fragment already exists");
        }
    }
}
