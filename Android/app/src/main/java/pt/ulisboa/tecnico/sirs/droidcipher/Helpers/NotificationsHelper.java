package pt.ulisboa.tecnico.sirs.droidcipher.Helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import pt.ulisboa.tecnico.sirs.droidcipher.Services.Connection;
import pt.ulisboa.tecnico.sirs.droidcipher.Services.MainProtocolService;
import pt.ulisboa.tecnico.sirs.droidcipher.broadcastreceivers.AcceptConnectionReceiver;
import pt.ulisboa.tecnico.sirs.droidcipher.broadcastreceivers.DismissNotificationReceiver;
import pt.ulisboa.tecnico.sirs.droidcipher.Constants;
import pt.ulisboa.tecnico.sirs.droidcipher.NewConnectionActivity;
import pt.ulisboa.tecnico.sirs.droidcipher.R;

/**
 * Created by goncalo on 04-11-2016.
 */

public class NotificationsHelper {

    public static int  startNewConnectionNotification(Context context, Connection connection){
        int notificationId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(ns);

        //the intent that is started when the notification is clicked
        Intent notificationIntent = new Intent(context, NewConnectionActivity.class);
        notificationIntent.putExtra(Constants.NOTIFICATION_ID_EXTRA,notificationId);
        notificationIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, connection);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(context, notificationId,
                notificationIntent, 0);


        // intent that is called when "accept" is clicked
        Intent acceptIntent = new Intent(context, AcceptConnectionReceiver.class);
        acceptIntent.putExtra(Constants.NOTIFICATION_ID_EXTRA,notificationId);
        acceptIntent.putExtra(Constants.CONNECTION_EXTRA, connection);
        PendingIntent pendingAcceptIntent = PendingIntent.getBroadcast(context, notificationId,
                acceptIntent, 0);

        // intent that is called when "close" is clicked
        Intent closeIntent = new Intent(context, DismissNotificationReceiver.class);
        closeIntent.putExtra(Constants.NOTIFICATION_ID_EXTRA,notificationId);
        closeIntent.putExtra(Constants.CONNECTION_EXTRA, connection);
        PendingIntent pendingCloseIntent = PendingIntent.getBroadcast(context, notificationId,
                closeIntent, 0);



        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_new_connection)
                .setContentText(context.getString(R.string.new_connection_long))
                .setContentTitle(context.getString(R.string.new_connection_short))
                .setTicker(context.getString(R.string.new_connection_short))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.new_connection_full)))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingNotificationIntent)
                .addAction(R.drawable.ic_check_black_24dp, context.getString(R.string.accept), pendingAcceptIntent)
                .addAction(R.drawable.ic_close_black_24dp, context.getString(R.string.close), pendingCloseIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true);


        Notification notification = notificationBuilder.build();
        notificationManager.notify(notificationId, notification);
        return notificationId;
    }
}
