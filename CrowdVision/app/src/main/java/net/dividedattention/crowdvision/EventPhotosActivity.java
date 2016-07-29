package net.dividedattention.crowdvision;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.View;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.dividedattention.crowdvision.adapters.EventImagesRecyclerViewAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EventPhotosActivity extends AppCompatActivity implements PhotoClickListener {
    private int PICK_IMAGE_REQUEST = 1;

    private EventImagesRecyclerViewAdapter mAdapter;
    private DatabaseReference mFirebaseRootRef, mFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_photos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        String eventTitle = getIntent().getStringExtra("eventTitle");
        toolbar.setTitle(eventTitle);
        setSupportActionBar(toolbar);

        String eventKey = getIntent().getStringExtra("eventKey");

        mFirebaseRootRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseRef = mFirebaseRootRef.child("events/"+eventKey+"/photoUrls");

        mAdapter = new EventImagesRecyclerViewAdapter(String.class,R.layout.photo_layout,EventImagesRecyclerViewAdapter.EventViewHolder.class,mFirebaseRef,this);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.photos_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == PICK_IMAGE_REQUEST){
                Uri uri = data.getData();

                String amazonFileName = "";

                CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                        getApplicationContext(),    /* get the context for the application */
                        ,    /* Identity Pool ID */
                        Regions.US_EAST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
                );

                // Create an S3 client
                AmazonS3 s3 = new AmazonS3Client(credentialsProvider);

                // Set the region of your S3 bucket
                s3.setRegion(Region.getRegion(Regions.US_EAST_1));

                TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());

                FileOutputStream fop = null;

                try {
                    Bitmap selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                    String fileName = "newImage.jpg";

                    File imageFile = new File(getFilesDir(),fileName);
                    fop = new FileOutputStream(imageFile);
                    selectedImage.compress(Bitmap.CompressFormat.JPEG,30,fop);

                    amazonFileName = System.currentTimeMillis() + "_" + selectedImage.getByteCount()+".jpg";

                    TransferObserver observer = transferUtility.upload(
                            "crowdvision",     /* The bucket to upload to */
                            System.currentTimeMillis() + "_" + selectedImage.getByteCount()+".jpg",    /* The key for the uploaded object */
                            imageFile        /* The file where the data to upload exists */
                    );

                    mFirebaseRef.push().setValue("https://s3.amazonaws.com/crowdvision/"+amazonFileName);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onPhotoClicked(EventImagesRecyclerViewAdapter.EventViewHolder viewHolder, int position) {
        PhotoDialogFragment fragment = PhotoDialogFragment.newInstance(((GlideBitmapDrawable)viewHolder.imageView.getDrawable()).getBitmap(),position+"_image");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fragment.setSharedElementEnterTransition(new ExpandedPhotoTransition());
            fragment.setEnterTransition(new Fade());
            fragment.setExitTransition(new Fade());
            fragment.setSharedElementReturnTransition(new ExpandedPhotoTransition());
        }

        //fragment.show(getSupportFragmentManager(),"expanded_dialog");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addSharedElement(viewHolder.imageView,position+"_image");
        fragment.show(transaction,"expanded_dialog");


    }
}
