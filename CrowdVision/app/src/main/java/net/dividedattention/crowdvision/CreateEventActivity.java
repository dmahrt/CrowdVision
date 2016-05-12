package net.dividedattention.crowdvision;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.io.IOException;
import java.util.Calendar;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    Button mEndDateButton, mSubmitButton, mChooseImageButton;
    EditText mTitleText, mLocationText;
    ImageView mEventImageView;
    String mEndDate;
    Bitmap mSelectedImage;
    Firebase mFirebaseRef;

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

        mFirebaseRef = new Firebase(Constants.FIREBASE_EVENTS);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkEventData()){
                    CrowdEvent event = new CrowdEvent(mTitleText.getText().toString(),
                            mLocationText.getText().toString(),
                            mEndDate,
                            null,
                            "https://www.omnihotels.com/-/media/images/hotels/cltdtn/destinations/cltdtn-omni-charlotte-hotel-skyline-night.jpg?h=660&la=en&w=1170");
                    mFirebaseRef.push().setValue(event);
                    finish();
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
}
