package pt.ulisboa.tecnico.sirs.droidcipher.Services;

import android.bluetooth.BluetoothDevice;

/**
 * Created by goncalo on 17-11-2016.
 */

public class ServiceState {
    public boolean isOn;
    public boolean isWaitingUser;
    public boolean isConnected;
    public BluetoothDevice currentConnection;
}
