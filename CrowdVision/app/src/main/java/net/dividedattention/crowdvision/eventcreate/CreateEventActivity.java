package net.dividedattention.crowdvision.eventcreate;

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

import net.dividedattention.crowdvision.data.events.EventsRepository;
import net.dividedattention.crowdvision.util.AddressServiceConstants;
import net.dividedattention.crowdvision.data.CrowdEvent;
import net.dividedattention.crowdvision.R;
import net.dividedattention.crowdvision.data.services.FetchAddressIntentService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, CreateEventContract.View{
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

    private CreateEventPresenter mPresenter;


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

        mPresenter = new CreateEventPresenter(this,EventsRepository.getInstance(this));

        mEventImageView = (ImageView)findViewById(R.id.event_image);
        mLocationImage = (ImageView)findViewById(R.id.location_image);
        mEndDateImage = (ImageView)findViewById(R.id.end_date_image);

        mLocationLayout = (LinearLayout) findViewById(R.id.layout_location);
        mDateLayout = (LinearLayout) findViewById(R.id.end_date_layout);

        mTitleTextLayout = (TextInputLayout) findViewById(R.id.input_layout_title);


        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);


        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                mPresenter.saveEvent(mTitleText.getText().toString().trim(),mLocationName,mCity,mState,mEndDate,mSelectedImage);
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
                mPresenter.retrieveGalleryImage(uri);
            }
        } else if (requestCode == PLACE_PICKER_REQUEST) {
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

    @Override
    public void setPresenter(CreateEventContract.Presenter presenter) {
        //not using a fragment
    }

    @Override
    public void showPhotoRatioWarning() {
        mPictureErrorSnackbar = Snackbar.make(findViewById(R.id.coord_layout),
                "We recommend using a landscape image",
                Snackbar.LENGTH_INDEFINITE);
        mPictureErrorSnackbar.getView().setBackgroundColor(Color.YELLOW);
        TextView textView = (TextView) mPictureErrorSnackbar.getView()
                .findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.BLACK);
        mPictureErrorSnackbar.show();
    }

    @Override
    public void dismissRationWarning() {
        if(mPictureErrorSnackbar != null)
            mPictureErrorSnackbar.dismiss();
    }

    @Override
    public void showIncompleteErrors(boolean titleValid, boolean dateValid, boolean locationValid, boolean photoValid) {
        mProgressBar.setVisibility(View.GONE);
        if(!titleValid){
            Log.d(TAG, "showIncompleteErrors: Invalid title");
            mTitleTextLayout.setError("Title is required");
        }

        if(!locationValid){
            Log.e(TAG, "checkEventData: "+mLocationName+", "+mCity+", "+mState);
            mLocationText.setTextColor(Color.RED);
        }

        if(!dateValid){
            mDateText.setTextColor(Color.RED);
        }

        if(!photoValid){
            mChooseImageButton.setBackgroundColor(Color.RED);
        }
    }

    @Override
    public void completePhotoUpload() {
        mProgressBar.setVisibility(View.GONE);
        finish();
    }

    @Override
    public void showGalleryImage(Bitmap bitmap) {
        mSelectedImage = bitmap;
        mEventImageView.setImageBitmap(mSelectedImage);
        mEventImageView.setVisibility(View.VISIBLE);
        mEventImageView.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        mChooseImageButton.setVisibility(View.GONE);

        mPresenter.checkImageRatio(mSelectedImage);
    }

    @Override
    public void showUploadError() {
        Toast.makeText(this, "Sorry, an error occurred while creating your event.", Toast.LENGTH_SHORT).show();
    }

    protected void startIntentService(Location location) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(AddressServiceConstants.RECEIVER, new ResultReceiver(new Handler()){
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
        });
        intent.putExtra(AddressServiceConstants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }
}
