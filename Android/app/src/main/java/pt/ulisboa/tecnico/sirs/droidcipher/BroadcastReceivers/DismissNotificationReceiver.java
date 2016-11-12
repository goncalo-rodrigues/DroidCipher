package pt.ulisboa.tecnico.sirs.droidcipher.BroadcastReceivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import pt.ulisboa.tecnico.sirs.droidcipher.Constants;
import pt.ulisboa.tecnico.sirs.droidcipher.Interfaces.IAcceptConnectionCallback;

/**
 * Created by goncalo on 04-11-2016.
 */

public class DismissNotificationReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

        int notificationId = intent.getIntExtra(Constants.NOTIFICATION_ID_EXTRA, 0);

        // if you want cancel notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);

        if (context instanceof IAcceptConnectionCallback) {
            ((IAcceptConnectionCallback) context).OnRejectConnection();
        }
        // TODO: Cancel connection
    }
}
