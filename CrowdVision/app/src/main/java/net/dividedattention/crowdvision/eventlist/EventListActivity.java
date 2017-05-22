package net.dividedattention.crowdvision.eventlist;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.os.ResultReceiver;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.tbruyelle.rxpermissions2.RxPermissions;

import net.dividedattention.crowdvision.data.events.EventsRepository;
import net.dividedattention.crowdvision.eventcreate.CreateEventActivity;
import net.dividedattention.crowdvision.login.LoginActivity;
import net.dividedattention.crowdvision.util.AddressServiceConstants;
import net.dividedattention.crowdvision.data.CrowdEvent;
import net.dividedattention.crowdvision.R;
import net.dividedattention.crowdvision.data.receivers.ConnectionBroadcastReceiver;
import net.dividedattention.crowdvision.data.services.FetchAddressIntentService;

public class EventListActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ConnectionBroadcastReceiver.ConnectionChangeListener, EventListContract.View {
    private static final String TAG = "EventListActivity";
    private EventListContract.Presenter mPresenter;
    private EventListPagerAdapter mPagerAdapter;
    private ViewPager mEventsViewPager;

    private String mCity, mState;
    private AddressResultReceiver mResultReceiver;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private ConnectionBroadcastReceiver mConnectionReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.event_list_activity_title));
        setSupportActionBar(toolbar);

        mPresenter = new EventListPresenter(this, EventsRepository.getInstance(this));

        //Check if user is logged in
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            //If user is not logged in, go to Login Activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EventListActivity.this, CreateEventActivity.class);
                startActivity(i);
            }
        });

        findViewById(R.id.enable_location_button).setOnClickListener(view -> {
            checkLocationPermission();
        });

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mPagerAdapter = new EventListPagerAdapter(getSupportFragmentManager(), mPresenter.getNearbyEvents(), mPresenter.getRemoteEvents(), mPresenter.getExpiredEvents());
        mEventsViewPager = (ViewPager) findViewById(R.id.events_viewpager);
        mEventsViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(mEventsViewPager);
    }

    private void checkLocationPermission() {
        View locationLayout = findViewById(R.id.request_location_layout);
        View fab = findViewById(R.id.fab);
        View tabs = findViewById(R.id.tablayout);

        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {
                        locationLayout.setVisibility(View.GONE);
                        mEventsViewPager.setVisibility(View.VISIBLE);
                        fab.setVisibility(View.VISIBLE);
                        tabs.setVisibility(View.VISIBLE);
                        mGoogleApiClient.connect();
                    } else {
                        locationLayout.setVisibility(View.VISIBLE);
                        fab.setVisibility(View.GONE);
                        tabs.setVisibility(View.GONE);
                        mEventsViewPager.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", mEventsViewPager.getCurrentItem());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mEventsViewPager.setCurrentItem(savedInstanceState.getInt("position"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                mPresenter.cleanUp();
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                startActivity(new Intent(EventListActivity.this, LoginActivity.class));
                                finish();
                            }
                        });
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLastLocation != null)
                        startIntentService(mLastLocation);
                });
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        } else {
//            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
//                    mGoogleApiClient);
//            if (mLastLocation != null) {
//                startIntentService(mLastLocation);
//            } else {
//                Toast.makeText(this, "Could not find location", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void connectionResumed() {
        checkLocationPermission();
    }


    private class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            if (resultCode == AddressServiceConstants.SUCCESS_RESULT) {
                mCity = resultData.getString(AddressServiceConstants.RESULT_DATA_CITY);
                mState = resultData.getString(AddressServiceConstants.RESULT_DATA_STATE);

                SharedPreferences sharedPreferences = getSharedPreferences("net.dividedattention.crowdvision", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("city", mCity);
                editor.putString("state", mState);
                editor.commit();

                //Toast.makeText(EventListActivity.this, "Current Location: "+mCity+", "+mState, Toast.LENGTH_SHORT).show();
                //createEventLists();
                mPresenter.loadEvents(mCity, mState);
            } else {
                Toast.makeText(EventListActivity.this, "Error finding address", Toast.LENGTH_LONG).show();
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
        super.onStart();

        //Register connection broadcast receiver
        mConnectionReceiver = new ConnectionBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectionReceiver, filter);

        //Check network connectivity
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            checkLocationPermission();
        } else {
            Log.d(TAG, "onStart: No network connection");
            Toast.makeText(this, "No network connection", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onStop() {
        unregisterReceiver(mConnectionReceiver);
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.cleanUp();
    }

    public Fragment findFragmentByPosition(int position) {
        ViewPager eventsViewPager = (ViewPager) findViewById(R.id.events_viewpager);
        return getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + eventsViewPager.getId() + ":"
                        + mPagerAdapter.getItemId(position));
    }

    @Override
    public void setPresenter(EventListContract.Presenter presenter) {

    }

    @Override
    public void showNearbyEvent(CrowdEvent event) {
        EventListFragment currentFragment = (EventListFragment) findFragmentByPosition(0);
        if (currentFragment != null) {
            currentFragment.showAddedEvent();
        }
    }

    @Override
    public void showRemoteEvent(CrowdEvent event) {
        EventListFragment currentFragment = (EventListFragment) findFragmentByPosition(1);
        if (currentFragment != null) {
            currentFragment.showAddedEvent();
        }
    }

    @Override
    public void showExpiredEvent(CrowdEvent event) {
        EventListFragment currentFragment = (EventListFragment) findFragmentByPosition(2);
        if (currentFragment != null) {
            currentFragment.showAddedEvent();
        }
    }
}
