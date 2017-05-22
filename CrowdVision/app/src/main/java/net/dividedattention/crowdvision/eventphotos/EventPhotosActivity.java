package net.dividedattention.crowdvision.eventphotos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import net.dividedattention.crowdvision.R;
import net.dividedattention.crowdvision.data.CrowdEvent;
import net.dividedattention.crowdvision.data.Photo;
import net.dividedattention.crowdvision.data.events.EventsRepository;
import net.dividedattention.crowdvision.expandedphoto.ExpandedPhotoActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EventPhotosActivity extends AppCompatActivity implements PhotoClickListener, EventPhotosContract.View{
    private static final String TAG = "EventPhotosActivity";

    private EventPhotosContract.Presenter mPresenter;

    private int PICK_IMAGE_REQUEST = 1;

    private ArrayList<Photo> mPhotos;

    private RecyclerView mRecyclerView;
    private EventPhotosRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_photos);

        mPresenter = new EventPhotosPresenter(this, EventsRepository.getInstance(this));

        String eventTitle = getIntent().getStringExtra("eventTitle");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(eventTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPhotos = new ArrayList<>();

        mAdapter = new EventPhotosRecyclerViewAdapter(mPhotos, this);
        mAdapter.setHasStableIds(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.photos_recycler);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        final FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("net.dividedattention.crowdvision", Context.MODE_PRIVATE);
        String city = sharedPreferences.getString("city",null);
        String state = sharedPreferences.getString("state",null);

        mPresenter.loadEventInfo(getIntent().getStringExtra("eventKey"),city,state);
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == PICK_IMAGE_REQUEST){
                Uri uri = data.getData();
                mPresenter.addPhotoClicked(getIntent().getStringExtra("eventKey"),uri);
            }
        }
    }

    @Override
    public void onPhotoClicked(String photoUrl, ImageView imageView, int position, String key) {
        String eventKey = getIntent().getStringExtra("eventKey");
        Intent expandedIntent = new Intent(this, ExpandedPhotoActivity.class);
        expandedIntent.putExtra(ExpandedPhotoActivity.PHOTO_URL_KEY,photoUrl);
        expandedIntent.putExtra(ExpandedPhotoActivity.TRANSITION_KEY,position+"_image");
        expandedIntent.putExtra(ExpandedPhotoActivity.PHOTO_PATH_KEY,"events/"+eventKey+"/photos/"+key);

        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this,
                        imageView,
                        position+"_image");

        startActivity(expandedIntent,options.toBundle());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.cleanUp();
    }

    //View Contract methods

    @Override
    public void setPresenter(EventPhotosContract.Presenter presenter) {

    }

    @Override
    public void showAddPhotoButton(boolean shouldShow) {
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        if(shouldShow)
            fab.setVisibility(View.VISIBLE);
        else
            fab.setVisibility(View.GONE);
    }

    @Override
    public void showNewPhoto(Photo photo) {
        mAdapter.addPhoto(photo);
    }
}
