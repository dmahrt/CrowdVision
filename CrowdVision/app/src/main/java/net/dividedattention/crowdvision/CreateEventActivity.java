package net.dividedattention.crowdvision;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class CreateEventActivity extends AppCompatActivity {
    Button mEndDateButton, mSubmitButton, mChooseImageButton;
    EditText mTitleText, mLocationText;
    ImageView mEventImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        mEndDateButton = (Button)findViewById(R.id.end_date_button);
        mSubmitButton = (Button)findViewById(R.id.submit_button);
        mChooseImageButton = (Button)findViewById(R.id.choose_image_button);

        mTitleText = (EditText)findViewById(R.id.input_title);
        mLocationText = (EditText)findViewById(R.id.input_location);

        mEventImageView = (ImageView)findViewById(R.id.event_image);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkEventData()){

                }else{

                }
            }
        });
    }

    private boolean checkEventData(){
        return false;
    }
}
