package net.dividedattention.crowdvision.data.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by drewmahrt on 5/5/17.
 */

public class ConnectionBroadcastReceiver extends BroadcastReceiver {
    ConnectionChangeListener mListener;

    public ConnectionBroadcastReceiver(ConnectionChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Check network connectivity
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            mListener.connectionResumed();
        }else {
            Toast.makeText(context, "No network connection", Toast.LENGTH_SHORT).show();
        }
    }

    public interface ConnectionChangeListener{
        void connectionResumed();
    }
}
