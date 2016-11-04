package pt.ulisboa.tecnico.sirs.droidcipher.BroadcastReceivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import pt.ulisboa.tecnico.sirs.droidcipher.Constants;

/**
 * Created by goncalo on 04-11-2016.
 */

public class AcceptConnectionReceiver extends BroadcastReceiver {

    public static final String LOG_TAG = AcceptConnectionReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra(Constants.NOTIFICATION_ID_EXTRA, 0);
        // if you want cancel notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
        Log.d(LOG_TAG, "Accepting new connection");
        /*
        Accept new Connection
         */

    }
}

