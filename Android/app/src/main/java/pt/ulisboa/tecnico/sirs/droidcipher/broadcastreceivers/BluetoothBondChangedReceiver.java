package pt.ulisboa.tecnico.sirs.droidcipher.broadcastreceivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import pt.ulisboa.tecnico.sirs.droidcipher.Services.MainProtocolService;

/**
 * Created by goncalo on 13-11-2016.
 */

public class BluetoothBondChangedReceiver extends BroadcastReceiver {
    public final static String LOG_TAG = BluetoothBondChangedReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice mDevice;
        Log.d(LOG_TAG, "Received bond state change");
        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
            mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (mDevice .getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.d(LOG_TAG, "Paired with: " + mDevice.getName());
                Toast.makeText(context, "Paired with: " + mDevice.getName(), Toast.LENGTH_SHORT);
                Intent serviceIntent = new Intent(context, MainProtocolService.class);
                serviceIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, mDevice);
                context.startService(serviceIntent);
            }
        }
    }
}
