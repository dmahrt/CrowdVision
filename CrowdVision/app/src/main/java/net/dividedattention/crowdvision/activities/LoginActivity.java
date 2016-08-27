package net.dividedattention.crowdvision.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import net.dividedattention.crowdvision.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 20;
    public static final int PERMISSION_LOCATION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = (Button)findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in
            Log.d(LoginActivity.class.getName(),"User already signed in");
            checkLocationPermission();
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_button:
                Log.d(TAG, "onClick: Login");
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() != null) {
                    // already signed in
                    Log.d(LoginActivity.class.getName(),"User already signed in");
                    checkLocationPermission();
                }else {
                    startActivityForResult(
                            AuthUI.getInstance(FirebaseApp.getInstance())
                                    .createSignInIntentBuilder()
                                    .setProviders(AuthUI.FACEBOOK_PROVIDER)
                                    .build(),
                            RC_SIGN_IN);
                }
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                LoginActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    LoginActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION
            );
        } else {
            Log.d(TAG, "onCreate: Permission granted");
            startActivity(new Intent(this, EventListActivity.class));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(this, EventListActivity.class));
                    finish();
                }else{
                    Log.e(TAG, "onRequestPermissionsResult: Permission not granted");
                    Snackbar.make(findViewById(R.id.coord_layout),
                            R.string.location_deny_message,
                            Snackbar.LENGTH_INDEFINITE).show();
                }
                return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // user is signed in!
                checkLocationPermission();
                Log.d(LoginActivity.class.getName(),"User is now signed in");
            } else {
                // user is not signed in. Maybe just wait for the user to press
                // "sign in" again, or show a message
            }
        }
    }
}
