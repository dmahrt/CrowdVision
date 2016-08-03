package net.dividedattention.crowdvision;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    Button mEndDateButton, mSubmitButton, mChooseImageButton;
    EditText mTitleText, mLocationText;
    ProgressBar mProgressBar;
    ImageView mEventImageView;
    String mEndDate;
    Bitmap mSelectedImage;
    DatabaseReference mFirebaseRef;

    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.create_event_activity_title));

        mEndDateButton = (Button)findViewById(R.id.end_date_button);
        mSubmitButton = (Button)findViewById(R.id.submit_button);
        mChooseImageButton = (Button)findViewById(R.id.choose_image_button);

        mTitleText = (EditText)findViewById(R.id.input_title);
        mLocationText = (EditText)findViewById(R.id.input_location);

        mEventImageView = (ImageView)findViewById(R.id.event_image);

        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);

        mFirebaseRef = FirebaseDatabase.getInstance().getReference().child("events");

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkEventData()){
                    AmazonUploadTask task = new AmazonUploadTask();
                    task.execute();
                }
            }
        });

        mEndDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = new DatePickerDialog(CreateEventActivity.this,
                        CreateEventActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show();
            }
        });

        mChooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
    }

    private boolean checkEventData(){
        boolean isValid = true;
        if(mTitleText.getText().toString().length() == 0){
            mTitleText.setError("Title is required");
            isValid = false;
        }

        if(mLocationText.getText().toString().length() == 0){
            mLocationText.setError("Location is required");
            isValid = false;
        }

        if(mEndDate == null){
            mEndDateButton.setBackgroundColor(Color.RED);
            isValid = false;
        }

        if(mSelectedImage == null){
            mChooseImageButton.setBackgroundColor(Color.RED);
            isValid = false;
        }
        return isValid;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mEndDate = (monthOfYear+1)+"/"+dayOfMonth+"/"+year;
        mEndDateButton.setText("End Date: "+mEndDate);
        Button defaultButton = new Button(this);
        mEndDateButton.setBackground(defaultButton.getBackground());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == PICK_IMAGE_REQUEST){
                Uri uri = data.getData();

                try {
                    mSelectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    mEventImageView.setImageBitmap(mSelectedImage);
                    mEventImageView.setVisibility(View.VISIBLE);

                    if(mSelectedImage.getHeight() >= mSelectedImage.getWidth()) {
                        Snackbar.make(findViewById(R.id.coord_layout), "We recommend using a landscape image", Snackbar.LENGTH_INDEFINITE).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class AmazonUploadTask extends AsyncTask<Void,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            String amazonFileName = "";
            String identityPoolID = getString(R.string.identity_pool_id);

            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),    /* get the context for the application */
                    identityPoolID,    /* Identity Pool ID */
                    Regions.US_EAST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
            );

            // Create an S3 client
            AmazonS3 s3 = new AmazonS3Client(credentialsProvider);

            // Set the region of your S3 bucket
            s3.setRegion(Region.getRegion(Regions.US_EAST_1));

            TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());

            FileOutputStream fop = null;

            try {
                String fileName = "newImage.jpg";

                File imageFile = new File(getFilesDir(),fileName);
                fop = new FileOutputStream(imageFile);
                mSelectedImage.compress(Bitmap.CompressFormat.JPEG,30,fop);

                amazonFileName = System.currentTimeMillis() + "_" + mSelectedImage.getByteCount()+".jpg";

                TransferObserver observer = transferUtility.upload(
                        "crowdvision",     /* The bucket to upload to */
                        System.currentTimeMillis() + "_" + mSelectedImage.getByteCount()+".jpg",    /* The key for the uploaded object */
                        imageFile        /* The file where the data to upload exists */
                );



            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try {
                    if (fop != null) {
                        fop.flush();
                        fop.close();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            return "https://s3.amazonaws.com/crowdvision/"+amazonFileName;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            CrowdEvent event = new CrowdEvent(mTitleText.getText().toString(),
                    mLocationText.getText().toString(),
                    mEndDate,
                    null,
                    s);
            DatabaseReference subRef = mFirebaseRef.push();
            event.setKey(subRef.getKey());
            subRef.setValue(event);
            mProgressBar.setVisibility(View.GONE);
            finish();
        }
    }
}
