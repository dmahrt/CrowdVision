package net.dividedattention.crowdvision;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
