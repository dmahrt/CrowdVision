package net.dividedattention.crowdvision.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.os.ResultReceiver;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.dividedattention.crowdvision.AddressServiceConstants;
import net.dividedattention.crowdvision.adapters.EventListPagerAdapter;
import net.dividedattention.crowdvision.fragments.EventListFragment;
import net.dividedattention.crowdvision.models.CrowdEvent;
import net.dividedattention.crowdvision.R;
import net.dividedattention.crowdvision.adapters.EventListFirebaseRecyclerViewAdapter;
import net.dividedattention.crowdvision.services.FetchAddressIntentService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventListActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "EventListActivity";

    private DatabaseReference mFirebaseRef;
    //private EventListFirebaseRecyclerViewAdapter mAdapter;
    private EventListPagerAdapter mPagerAdapter;

    private String mCity, mState;
    private AddressResultReceiver mResultReceiver;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private ArrayList<CrowdEvent> mCurrentEvents, mRemoteEvents, mExpiredEvents;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.event_list_activity_title));
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EventListActivity.this, CreateEventActivity.class);
                startActivity(i);
            }
        });


        mCurrentEvents = new ArrayList<>();
        mRemoteEvents = new ArrayList<>();
        mExpiredEvents = new ArrayList<>();


//        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.events_list);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mAdapter = new EventListFirebaseRecyclerViewAdapter(CrowdEvent.class, R.layout.event_card, EventListFirebaseRecyclerViewAdapter.EventViewHolder.class, mFirebaseRef, this);
//        recyclerView.setAdapter(mAdapter);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mPagerAdapter = new EventListPagerAdapter(getSupportFragmentManager(),mCurrentEvents,mRemoteEvents,mExpiredEvents);
        ViewPager eventsViewPager = (ViewPager) findViewById(R.id.events_viewpager);
        eventsViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(eventsViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            AuthUI.getInstance(FirebaseApp.getInstance())
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            startActivity(new Intent(EventListActivity.this, LoginActivity.class));
                            finish();
                        }
                    });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                startIntentService(mLastLocation);
            } else {
                Toast.makeText(this, "Could not find location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
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
            Log.d(TAG, "testCurrentEvent: Event: "+eventDate.toString()+" Current: "+currentDateFormatted.toString());
            return !currentDateFormatted.after(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void createEventLists(){
        mFirebaseRef = FirebaseDatabase.getInstance().getReference().child("events");
        mFirebaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(EventListActivity.class.getName(), "Key added: " + dataSnapshot.getKey());
                CrowdEvent event = dataSnapshot.getValue(CrowdEvent.class);
                boolean isCurrentEvent = testCurrentEvent(event.getEndDate());
                boolean isNearby = testNearby(event.getCity(),event.getState());

                EventListFragment currentFragment = null;

                if(!isCurrentEvent){
                    Log.d(TAG, "onChildAdded: Expired");
                    if(!mExpiredEvents.contains(event)) {
                        mExpiredEvents.add(event);
                        currentFragment = (EventListFragment) findFragmentByPosition(2);
                    }
                } else if(isNearby) {
                    Log.d(TAG, "onChildAdded: Nearby");
                    if(!mCurrentEvents.contains(event)) {
                        mCurrentEvents.add(event);
                        currentFragment = (EventListFragment) findFragmentByPosition(0);
                    }
                }else {
                    Log.d(TAG, "onChildAdded: Remote");
                    if(!mRemoteEvents.contains(event)) {
                        mRemoteEvents.add(event);
                        currentFragment = (EventListFragment) findFragmentByPosition(1);
                    }
                }

                if(currentFragment != null) {
                    currentFragment.addEvent(event);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                CrowdEvent event = dataSnapshot.getValue(CrowdEvent.class);
                boolean isCurrentEvent = testCurrentEvent(event.getEndDate());
                boolean isNearby = testNearby(event.getCity(),event.getState());

                EventListFragment currentFragment;
                int position = -1;

                Log.d(EventListActivity.class.getName(), "Key changed: " + dataSnapshot.getKey());
                if(!isCurrentEvent) {
                    currentFragment = (EventListFragment) findFragmentByPosition(2);
                    position = mExpiredEvents.indexOf(event);
                    mExpiredEvents.set(position,event);
                } else if(isNearby) {
                    currentFragment = (EventListFragment) findFragmentByPosition(0);
                    position = mCurrentEvents.indexOf(event);
                    mCurrentEvents.set(position,event);
                }else {
                    currentFragment = (EventListFragment) findFragmentByPosition(1);
                    position = mRemoteEvents.indexOf(event);
                    mRemoteEvents.set(position,event);
                }

                if(position >= 0 && currentFragment != null){
                    currentFragment.modifyEvent(position,event);
                }else{
                    Log.d(TAG, "onChildRemoved: Error removing event");
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                CrowdEvent event = dataSnapshot.getValue(CrowdEvent.class);
                boolean isCurrentEvent = testCurrentEvent(event.getEndDate());
                boolean isNearby = testNearby(event.getCity(),event.getState());

                EventListFragment currentFragment;
                int position = -1;

                Log.d(EventListActivity.class.getName(), "Key removed: " + dataSnapshot.getKey());

                if(!isCurrentEvent) {
                    currentFragment = (EventListFragment) findFragmentByPosition(2);
                    position = mExpiredEvents.indexOf(event);
                    mExpiredEvents.remove(position);
                } else if(isNearby) {
                    currentFragment = (EventListFragment) findFragmentByPosition(0);
                    position = mCurrentEvents.indexOf(event);
                    mCurrentEvents.remove(position);
                }else {
                    currentFragment = (EventListFragment) findFragmentByPosition(1);
                    position = mRemoteEvents.indexOf(event);
                    mRemoteEvents.remove(position);
                }

                if(position >= 0 && currentFragment != null){
                    currentFragment.removeEvent(position);
                }else{
                    Log.d(TAG, "onChildRemoved: Error removing event");
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            if(resultCode == AddressServiceConstants.SUCCESS_RESULT) {
                mCity = resultData.getString(AddressServiceConstants.RESULT_DATA_CITY);
                mState = resultData.getString(AddressServiceConstants.RESULT_DATA_STATE);

                SharedPreferences sharedPreferences = getSharedPreferences("net.dividedattention.crowdvision",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("city",mCity);
                editor.putString("state",mState);
                editor.commit();

                Toast.makeText(EventListActivity.this, "Current Location: "+mCity+", "+mState, Toast.LENGTH_SHORT).show();
                createEventLists();
            }else{
                Toast.makeText(EventListActivity.this,"Error finding address",Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void startIntentService(Location location) {
        mResultReceiver = new AddressResultReceiver(new Handler());
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(AddressServiceConstants.RECEIVER, mResultReceiver);
        intent.putExtra(AddressServiceConstants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public Fragment findFragmentByPosition(int position) {
        ViewPager eventsViewPager = (ViewPager) findViewById(R.id.events_viewpager);
        return getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + eventsViewPager.getId() + ":"
                        + mPagerAdapter.getItemId(position));
    }
}
