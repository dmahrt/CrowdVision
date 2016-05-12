package net.dividedattention.crowdvision;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.ui.auth.core.AuthProviderType;
import com.firebase.ui.auth.core.FirebaseLoginError;

import net.dividedattention.crowdvision.adapters.EventListRecyclerViewAdapter;
import net.dividedattention.crowdvision.firebaselogin.CustomFirebaseLoginActivity;

public class EventListActivity extends CustomFirebaseLoginActivity {

    private Firebase mFirebaseRef;
    private EventListRecyclerViewAdapter mAdapter;

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
                Intent i = new Intent(EventListActivity.this,CreateEventActivity.class);
                startActivity(i);
            }
        });

        mFirebaseRef = new Firebase(Constants.FIREBASE_EVENTS);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.events_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new EventListRecyclerViewAdapter(CrowdEvent.class,R.layout.event_card,EventListRecyclerViewAdapter.EventViewHolder.class,mFirebaseRef,this);
        recyclerView.setAdapter(mAdapter);
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
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_logout){
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setEnabledAuthProvider(AuthProviderType.FACEBOOK);
    }

    @Override
    protected Firebase getFirebaseRef() {
        return new Firebase(Constants.FIREBASE_BASE_URL);
    }

    @Override
    protected void onFirebaseLoginProviderError(FirebaseLoginError firebaseLoginError) {
        Toast.makeText(EventListActivity.this, "Provider Error", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onFirebaseLoginUserError(FirebaseLoginError firebaseLoginError) {
        Toast.makeText(EventListActivity.this, "User Error", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }

    @Override
    public void onFirebaseLoggedIn(AuthData authData) {
    }

    @Override
    public void onFirebaseLoggedOut() {
        showFirebaseLoginPrompt();
    }
}
