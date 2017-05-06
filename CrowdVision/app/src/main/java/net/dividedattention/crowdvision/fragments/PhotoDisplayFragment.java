package net.dividedattention.crowdvision.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
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

import net.dividedattention.crowdvision.activities.EventPhotosActivity;
import net.dividedattention.crowdvision.activities.ExpandedPhotoActivity;
import net.dividedattention.crowdvision.adapters.EventImagesRecyclerViewAdapter;
import net.dividedattention.crowdvision.adapters.PhotoClickListener;
import net.dividedattention.crowdvision.R;
import net.dividedattention.crowdvision.adapters.EventImagesRecyclerViewAdapter;
import net.dividedattention.crowdvision.models.CrowdEvent;
import net.dividedattention.crowdvision.models.Photo;
import net.dividedattention.crowdvision.transitions.ExpandedPhotoTransition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by drewmahrt on 7/29/16.
 */
public class PhotoDisplayFragment extends Fragment implements PhotoClickListener {
    private static final String TAG = "PhotoDisplayFragment";
    public static final String EVENT_KEY = "eventKey";
    public static final String EVENT_TITLE = "eventTitle";
    private int mLastPos = 0;

    private int PICK_IMAGE_REQUEST = 1;

    private ArrayList<Photo> mPhotos;

    private RecyclerView mRecyclerView;
    private EventImagesRecyclerViewAdapter mAdapter;
    private DatabaseReference mFirebaseRootRef, mFirebaseEventRef, mFirebasePhotosRef;

    public static PhotoDisplayFragment newInstance(String eventKey, String eventTitle) {
        Bundle args = new Bundle();
        args.putString(EVENT_KEY,eventKey);
        args.putString(EVENT_TITLE,eventTitle);
        PhotoDisplayFragment fragment = new PhotoDisplayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mFirebaseRootRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseEventRef = mFirebaseRootRef.child("events/"+getArguments().getString(EVENT_KEY));
        mFirebasePhotosRef = mFirebaseEventRef.child("photos");


        Log.d(TAG, "onCreate: Emptying photo list");
        mPhotos = new ArrayList<>();

        mAdapter = new EventImagesRecyclerViewAdapter(mPhotos, this);
        mAdapter.setHasStableIds(true);
        mFirebasePhotosRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Photo photo = dataSnapshot.getValue(Photo.class);
                mPhotos.add(0,photo);
                //Log.d(TAG, "onChildAdded: index: "+mPhotos.indexOf(photo)+" key: "+dataSnapshot.getKey());
                mAdapter.addKey(mPhotos.indexOf(photo),dataSnapshot.getKey());
                mAdapter.notifyItemInserted(mPhotos.indexOf(photo));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Photo photo = dataSnapshot.getValue(Photo.class);
                Log.d(TAG, "onChildRemoved: list size "+mPhotos.size());
                int index = mPhotos.indexOf(photo.getPhotoUrl());
                if(index >= 0) {
                    mPhotos.remove(index);
                    mAdapter.removeKey(index);
                    mAdapter.notifyItemRemoved(index);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_all_photos,container,false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.photos_recycler);

        Log.d(TAG, "onCreateView: backstack count: "+getActivity().getSupportFragmentManager().getBackStackEntryCount());
        for(int entry = 0; entry < getActivity().getSupportFragmentManager().getBackStackEntryCount(); entry++){
            Log.d(TAG, "onCreateView: Found fragment: " + getActivity().getSupportFragmentManager().getBackStackEntryAt(entry).getName());
        }

        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(getArguments().getString(EVENT_TITLE));
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


//        mAdapter = new EventImagesRecyclerViewAdapterOld(String.class,
//                R.layout.photo_layout,
//                EventImagesRecyclerViewAdapterOld.EventViewHolder.class,
//                mFirebaseRef,
//                getContext(),
//                this);

        Log.d(TAG, "onViewCreated: size: "+mPhotos.size());

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        final FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("net.dividedattention.crowdvision", Context.MODE_PRIVATE);
        final String city = sharedPreferences.getString("city",null);
        final String state = sharedPreferences.getString("state",null);

        mFirebaseEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CrowdEvent event = dataSnapshot.getValue(CrowdEvent.class);

                Log.d(TAG, "onDataChange: city: "+city+" "+event.getCity()+" || "+state+" "+event.getState());

                if(city != null && state != null && state.equals(event.getState()) && city.equals(event.getCity()) && testCurrentEvent(event.getEndDate())){
                    fab.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == PICK_IMAGE_REQUEST){
                Uri uri = data.getData();
                uploadPhotoToEvent(uri);
            }
        }
    }

    public void uploadPhotoToEvent(Uri uri){
        ByteArrayOutputStream baos = null;

        try {
            Log.d(TAG, "onActivityResult: Attempting to upload image");

            String fileName = "newImage.jpg";

            File imageFile = new File(getActivity().getFilesDir(),fileName);
            //Uri fileUri = Uri.fromFile(imageFile);
            Bitmap selectedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(getString(R.string.firebase_storage));
            StorageReference imagesRef = storageRef.child("images");
            StorageReference spaceRef = storageRef.child("images/"+System.currentTimeMillis() + "_" + selectedImage.getByteCount()+".jpg");


            baos = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.JPEG,50,baos);
            final byte[] imageData = baos.toByteArray();

            UploadTask uploadTask = spaceRef.putBytes(imageData);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getContext(), "Image failed to upload", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    String imagePath = downloadUrl.toString();
                    Log.d(TAG, "onSuccess: "+imagePath);
                    Toast.makeText(getContext(), "Image added to event", Toast.LENGTH_SHORT).show();
                    mFirebasePhotosRef.push().setValue(new Photo(imagePath,0));
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPhotoClicked(String photoUrl, ImageView imageView, int position,String key) {
        mLastPos = position;

        Intent expandedIntent = new Intent(getContext(), ExpandedPhotoActivity.class);
        expandedIntent.putExtra(ExpandedPhotoActivity.PHOTO_URL_KEY,photoUrl);
        expandedIntent.putExtra(ExpandedPhotoActivity.TRANSITION_KEY,position+"_image");
        expandedIntent.putExtra(ExpandedPhotoActivity.PHOTO_PATH_KEY,"events/"+getArguments().getString(EVENT_KEY)+"/photos/"+key);

        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(getActivity(),
                        imageView,
                        position+"_image");

        startActivity(expandedIntent,options.toBundle());
    }
}
