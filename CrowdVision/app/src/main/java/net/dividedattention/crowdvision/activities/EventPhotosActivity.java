package net.dividedattention.crowdvision.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import net.dividedattention.crowdvision.fragments.PhotoDisplayFragment;
import net.dividedattention.crowdvision.R;

public class EventPhotosActivity extends AppCompatActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_photos);
        String eventTitle = getIntent().getStringExtra("eventTitle");
        String eventKey = getIntent().getStringExtra("eventKey");

        PhotoDisplayFragment fragment = PhotoDisplayFragment.newInstance(eventKey,eventTitle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
