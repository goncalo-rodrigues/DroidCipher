package pt.ulisboa.tecnico.sirs.droidcipher.Helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import pt.ulisboa.tecnico.sirs.droidcipher.BroadcastReceivers.AcceptConnectionReceiver;
import pt.ulisboa.tecnico.sirs.droidcipher.BroadcastReceivers.DismissNotificationReceiver;
import pt.ulisboa.tecnico.sirs.droidcipher.Constants;
import pt.ulisboa.tecnico.sirs.droidcipher.NewConnectionActivity;
import pt.ulisboa.tecnico.sirs.droidcipher.R;

/**
 * Created by goncalo on 04-11-2016.
 */

public class NotificationsHelper {

    public static int  startNewConnectionNotification(Context context){
        int notificationId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(ns);

        //the intent that is started when the notification is clicked
        Intent notificationIntent = new Intent(context, NewConnectionActivity.class);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);


        // intent that is called when "accept" is clicked
        Intent acceptIntent = new Intent(context, AcceptConnectionReceiver.class);
        acceptIntent.putExtra(Constants.NOTIFICATION_ID_EXTRA,notificationId);
        PendingIntent pendingAcceptIntent = PendingIntent.getBroadcast(context, 0,
                acceptIntent, 0);

        // intent that is called when "close" is clicked
        Intent closeIntent = new Intent(context, DismissNotificationReceiver.class);
        closeIntent.putExtra(Constants.NOTIFICATION_ID_EXTRA,notificationId);
        PendingIntent pendingCloseIntent = PendingIntent.getBroadcast(context, 0,
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
                .setAutoCancel(true);


        Notification notification = notificationBuilder.build();
        notificationManager.notify(notificationId, notification);
        return notificationId;
    }
}
