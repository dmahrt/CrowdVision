package net.dividedattention.crowdvision;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

/**
 * Created by drewmahrt on 5/25/16.
 */
public class CrowdVisionApp extends FirebaseApp {
    protected CrowdVisionApp(Context context, String s, FirebaseOptions firebaseOptions) {
        super(context, s, firebaseOptions);
    }
}
