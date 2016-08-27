package net.dividedattention.crowdvision.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import net.dividedattention.crowdvision.AddressServiceConstants;
import net.dividedattention.crowdvision.models.CrowdEvent;
import net.dividedattention.crowdvision.R;
import net.dividedattention.crowdvision.services.FetchAddressIntentService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    private static final String TAG = "CreateEventActivity";
    private static final int PLACE_PICKER_REQUEST = 0;
    Button mEndDateButton, mSubmitButton, mChooseImageButton, mLocationButton;
    EditText mTitleText;
    TextView mLocationText;
    ProgressBar mProgressBar;
    ImageView mEventImageView;
    String mEndDate;
    Bitmap mSelectedImage;
    DatabaseReference mFirebaseRef;
    private AddressResultReceiver mResultReceiver;


    private int PICK_IMAGE_REQUEST = 1;

    private String mCity, mState, mLocationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.create_event_activity_title));

        mEndDateButton = (Button)findViewById(R.id.end_date_button);
        mSubmitButton = (Button)findViewById(R.id.submit_button);
        mChooseImageButton = (Button)findViewById(R.id.choose_image_button);
        mLocationButton = (Button)findViewById(R.id.location_button);

        mTitleText = (EditText)findViewById(R.id.input_title);
        mLocationText = (TextView) findViewById(R.id.location_text);

        mEventImageView = (ImageView)findViewById(R.id.event_image);

        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);

        mFirebaseRef = FirebaseDatabase.getInstance().getReference().child("events");

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkEventData()){
                    uploadToFirebase();
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

        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    startActivityForResult(builder.build(CreateEventActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void uploadToFirebase() {
        ByteArrayOutputStream baos = null;

        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(getString(R.string.firebase_storage));
            StorageReference spaceRef = storageRef.child("images/" + System.currentTimeMillis() + "_" + mSelectedImage.getByteCount() + ".jpg");

            baos = new ByteArrayOutputStream();
            mSelectedImage.compress(Bitmap.CompressFormat.JPEG, 30, baos);
            final byte[] imageData = baos.toByteArray();

            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            UploadTask uploadTask = spaceRef.putBytes(imageData);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(CreateEventActivity.this, "Image failed to upload", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    String imagePath = downloadUrl.toString();
                    Log.d(TAG, "onSuccess: " + imagePath);

                    CrowdEvent event = new CrowdEvent(mTitleText.getText().toString(),
                            mLocationName,
                            mCity,
                            mState,
                            mEndDate,
                            null,
                            imagePath);
                    DatabaseReference subRef = mFirebaseRef.push();
                    event.setKey(subRef.getKey());
                    subRef.setValue(event);
                    finish();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean checkEventData(){
        boolean isValid = true;
        if(mTitleText.getText().toString().length() == 0){
            mTitleText.setError("Title is required");
            isValid = false;
        }

        if(mLocationName == null || mCity == null || mState == null){
            Log.e(TAG, "checkEventData: "+mLocationName+", "+mCity+", "+mState);
            mLocationButton.setError("Location is required");
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
        if(requestCode == PICK_IMAGE_REQUEST){
            if(resultCode == RESULT_OK){
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

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                TextView textView = (TextView) findViewById(R.id.location_text);
                Place place = PlacePicker.getPlace(this, data);
                mLocationName = place.getName().toString();
                textView.setText(mLocationName);


                Location location = new Location("");
                location.setLatitude(place.getLatLng().latitude);
                location.setLongitude(place.getLatLng().longitude);
                startIntentService(location);

            }
        }
    }

    class AddressResultReceiver extends ResultReceiver {
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
            }else{
                Toast.makeText(CreateEventActivity.this,"Error finding address",Toast.LENGTH_LONG).show();
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
}
