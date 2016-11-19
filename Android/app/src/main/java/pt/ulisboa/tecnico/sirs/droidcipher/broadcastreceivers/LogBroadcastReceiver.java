package pt.ulisboa.tecnico.sirs.droidcipher.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import pt.ulisboa.tecnico.sirs.droidcipher.Services.Connection;
import pt.ulisboa.tecnico.sirs.droidcipher.Services.MainProtocolService;

/**
 * Created by goncalo on 18-11-2016.
 */

public class LogBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(MainProtocolService.NEW_EVENT_ACTION)) {
            int eventId = intent.getIntExtra(MainProtocolService.EXTRA_EVENT, -1);
            Connection conn = intent.getParcelableExtra(MainProtocolService.EXTRA_CONNECTION);
            //newEvent(eventId, conn);
        }
    }
}
