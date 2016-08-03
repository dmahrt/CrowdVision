package net.dividedattention.crowdvision;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

/**
 * Created by drewmahrt on 7/29/16.
 */
public class PhotoDisplayFragment extends Fragment implements PhotoClickListener{
    public final static String EVENT_KEY = "eventKey";
    public final static String EVENT_TITLE = "eventTitle";

    private int PICK_IMAGE_REQUEST = 1;

    private EventImagesRecyclerViewAdapter mAdapter;
    private DatabaseReference mFirebaseRootRef, mFirebaseRef;

    public static PhotoDisplayFragment newInstance(String eventKey, String eventTitle) {
        Bundle args = new Bundle();
        args.putString(EVENT_KEY,eventKey);
        args.putString(EVENT_TITLE,eventTitle);
        PhotoDisplayFragment fragment = new PhotoDisplayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_all_photos,container,false);

        mFirebaseRootRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseRef = mFirebaseRootRef.child("events/"+getArguments().getString(EVENT_KEY)+"/photoUrls");

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(getArguments().getString(EVENT_TITLE));
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        mAdapter = new EventImagesRecyclerViewAdapter(String.class,R.layout.photo_layout,EventImagesRecyclerViewAdapter.EventViewHolder.class,mFirebaseRef,getContext(),this);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.photos_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.fab);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == PICK_IMAGE_REQUEST){
                Uri uri = data.getData();

                String amazonFileName = "";
                String identityPoolID = getString(R.string.identity_pool_id);

                CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                        getActivity().getApplicationContext(),    /* get the context for the application */
                        identityPoolID,    /* Identity Pool ID */
                        Regions.US_EAST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
                );

                // Create an S3 client
                AmazonS3 s3 = new AmazonS3Client(credentialsProvider);

                // Set the region of your S3 bucket
                s3.setRegion(Region.getRegion(Regions.US_EAST_1));

                TransferUtility transferUtility = new TransferUtility(s3, getActivity().getApplicationContext());

                FileOutputStream fop = null;

                try {
                    Bitmap selectedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);

                    String fileName = "newImage.jpg";

                    File imageFile = new File(getActivity().getFilesDir(),fileName);
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
        ExpandedPhotoFragment fragment = ExpandedPhotoFragment.newInstance(((GlideBitmapDrawable)viewHolder.imageView.getDrawable()).getBitmap(),position+"_image");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fragment.setSharedElementEnterTransition(new ExpandedPhotoTransition());
            fragment.setEnterTransition(new Fade());
            fragment.setExitTransition(new Fade());
            fragment.setSharedElementReturnTransition(new ExpandedPhotoTransition());
        }

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addSharedElement(viewHolder.imageView, position+"_image")
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();

    }
}
