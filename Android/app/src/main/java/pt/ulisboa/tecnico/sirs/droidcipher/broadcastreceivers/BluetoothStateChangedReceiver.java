package pt.ulisboa.tecnico.sirs.droidcipher.broadcastreceivers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import pt.ulisboa.tecnico.sirs.droidcipher.Constants;
import pt.ulisboa.tecnico.sirs.droidcipher.Services.MainProtocolService;

/**
 * Created by goncalo on 13-11-2016.
 */

public class BluetoothStateChangedReceiver extends BroadcastReceiver {
    public final static String LOG_TAG = BluetoothStateChangedReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent serviceIntent = new Intent(context, MainProtocolService.class);
        Log.i(LOG_TAG, "Received bluetooth state change");
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    context.stopService(serviceIntent);
                    break;
                case BluetoothAdapter.STATE_ON:
                    context.startService(serviceIntent);
                    break;
            }
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                // disconnected
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                serviceIntent.putExtra(MainProtocolService.EXTRA_CONNECTION, device);
                serviceIntent.putExtra(Constants.SERVICE_COMMAND_EXTRA, Constants.RESET_CONN_COMMAND);
                context.startService(serviceIntent);


        }
    }
}
