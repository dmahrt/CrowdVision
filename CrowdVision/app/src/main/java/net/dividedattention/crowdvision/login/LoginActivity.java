package net.dividedattention.crowdvision.login;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import net.dividedattention.crowdvision.R;
import net.dividedattention.crowdvision.eventlist.EventListActivity;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_method_picker_layout);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        startActivityForResult(
                AuthUI.getInstance(FirebaseApp.getInstance())
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginThemeFirebase)
                        .setLogo(R.drawable.cv_logo)
                        .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "User is now signed in");
                startActivity(new Intent(this, EventListActivity.class));
                finish();
            }else {
                Log.d(TAG, "onActivityResult: Error logging in");
            }
        }
    }
}
