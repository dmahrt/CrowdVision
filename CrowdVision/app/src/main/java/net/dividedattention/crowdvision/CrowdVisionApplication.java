package net.dividedattention.crowdvision;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by drewmahrt on 5/11/16.
 */
public class CrowdVisionApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
