package pt.ulisboa.tecnico.sirs.droidcipher.BroadcastReceivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import pt.ulisboa.tecnico.sirs.droidcipher.Constants;
import pt.ulisboa.tecnico.sirs.droidcipher.Interfaces.IAcceptConnectionCallback;
import pt.ulisboa.tecnico.sirs.droidcipher.Services.MainProtocolService;

/**
 * Created by goncalo on 04-11-2016.
 */

public class DismissNotificationReceiver extends BroadcastReceiver{
    private static final String LOG_TAG = DismissNotificationReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        int notificationId = intent.getIntExtra(Constants.NOTIFICATION_ID_EXTRA, 0);

        // if you want cancel notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
        Log.d(LOG_TAG, "Rejecting new connection");


        if (context instanceof IAcceptConnectionCallback) {
            ((IAcceptConnectionCallback) context).OnRejectConnection();
        } else {
            MainProtocolService.LocalBinder binder = (MainProtocolService.LocalBinder)
                    peekService(context, new Intent(context, MainProtocolService.class));
            if (binder != null)
                binder.getService().OnRejectConnection();
        }
    }
}
