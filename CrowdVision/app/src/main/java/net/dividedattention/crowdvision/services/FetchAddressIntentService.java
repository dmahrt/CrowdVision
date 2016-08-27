package net.dividedattention.crowdvision.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import net.dividedattention.crowdvision.AddressServiceConstants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Drew on 8/27/16.
 */

public class FetchAddressIntentService extends IntentService {
    private static final String TAG = "FetchAddressIntentServi";
    protected ResultReceiver mReceiver;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchAddressIntentService(String name) {
        super(name);
    }

    public FetchAddressIntentService(){
        super("fetchaddress");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        mReceiver = intent.getParcelableExtra(AddressServiceConstants.RECEIVER);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(
                AddressServiceConstants.LOCATION_DATA_EXTRA);

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "Service not available";
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "Invalid coordinates";
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No address found";
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(AddressServiceConstants.FAILURE_RESULT, errorMessage,"","");
        } else {
            Address address = addresses.get(0);
            String city = address.getLocality();
            String state = address.getAdminArea();
            String name = address.getFeatureName();
            deliverResultToReceiver(AddressServiceConstants.SUCCESS_RESULT,
                    city,state,name);
        }
    }

    private void deliverResultToReceiver(int resultCode, String city, String state, String name) {
        Bundle bundle = new Bundle();
        bundle.putString(AddressServiceConstants.RESULT_DATA_CITY, city);
        bundle.putString(AddressServiceConstants.RESULT_DATA_STATE, state);
        bundle.putString(AddressServiceConstants.RESULT_LOC_NAME, name);
        mReceiver.send(resultCode, bundle);
    }
}
