package pt.ulisboa.tecnico.sirs.droidcipher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;

import pt.ulisboa.tecnico.sirs.droidcipher.BroadcastReceivers.AcceptConnectionReceiver;
import pt.ulisboa.tecnico.sirs.droidcipher.BroadcastReceivers.DismissNotificationReceiver;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AcceptThread at = new AcceptThread();
        at.run();
    }



}
