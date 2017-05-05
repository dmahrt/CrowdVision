package net.dividedattention.crowdvision.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.io.IOException;
import java.util.Calendar;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    private static final String TAG = "CreateEventActivity";
    private static final int PLACE_PICKER_REQUEST = 0;
    private Button mSubmitButton, mChooseImageButton;
    private EditText mTitleText;
    private TextView mLocationText, mDateText;
    private ProgressBar mProgressBar;
    private ImageView mEventImageView, mEndDateImage, mLocationImage;
    private LinearLayout mLocationLayout, mDateLayout;
    private TextInputLayout mTitleTextLayout;
    private Snackbar mPictureErrorSnackbar;
    private String mEndDate;
    private Bitmap mSelectedImage;
    private DatabaseReference mFirebaseRef;
    private AddressResultReceiver mResultReceiver;


    private int PICK_IMAGE_REQUEST = 1;

    private String mCity, mState, mLocationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.create_event_activity_title));

        mSubmitButton = (Button)findViewById(R.id.submit_button);
        mChooseImageButton = (Button)findViewById(R.id.choose_image_button);

        mTitleText = (EditText)findViewById(R.id.input_title);
        mLocationText = (TextView) findViewById(R.id.location_text);
        mDateText = (TextView) findViewById(R.id.date_text);


        mEventImageView = (ImageView)findViewById(R.id.event_image);
        mLocationImage = (ImageView)findViewById(R.id.location_image);
        mEndDateImage = (ImageView)findViewById(R.id.end_date_image);

        mLocationLayout = (LinearLayout) findViewById(R.id.layout_location);
        mDateLayout = (LinearLayout) findViewById(R.id.end_date_layout);

        mTitleTextLayout = (TextInputLayout) findViewById(R.id.input_layout_title);


        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);

        mFirebaseRef = FirebaseDatabase.getInstance().getReference().child("events");

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkEventData()){
                    mSubmitButton.setEnabled(false);
                    uploadToFirebase();
                }
            }
        });

        mDateLayout.setOnClickListener(new View.OnClickListener() {
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

        mLocationLayout.setOnClickListener(new View.OnClickListener() {
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
                    mSubmitButton.setEnabled(true);
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
            mTitleTextLayout.setError("Title is required");
            isValid = false;
        }

        if(mLocationName == null || mCity == null || mState == null){
            Log.e(TAG, "checkEventData: "+mLocationName+", "+mCity+", "+mState);
            mLocationText.setTextColor(Color.RED);
            isValid = false;
        }

        if(mEndDate == null){
            mDateText.setTextColor(Color.RED);
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
        mDateText.setText("End Date: "+mEndDate);
        mDateText.setTextColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
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
                    mEventImageView.setOnClickListener(v -> {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                    });

                    mChooseImageButton.setVisibility(View.GONE);

                    if(mSelectedImage.getHeight() >= mSelectedImage.getWidth()) {
                        mPictureErrorSnackbar = Snackbar.make(findViewById(R.id.coord_layout),
                                "We recommend using a landscape image",
                                Snackbar.LENGTH_INDEFINITE);
                        mPictureErrorSnackbar.getView().setBackgroundColor(Color.YELLOW);
                        TextView textView = (TextView) mPictureErrorSnackbar.getView()
                                .findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(Color.BLACK);
                        mPictureErrorSnackbar.show();
                    } else {
                        if(mPictureErrorSnackbar != null)
                            mPictureErrorSnackbar.dismiss();
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
                textView.setTextColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));


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
