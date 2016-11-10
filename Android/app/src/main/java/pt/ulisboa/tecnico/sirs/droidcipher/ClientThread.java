package pt.ulisboa.tecnico.sirs.droidcipher;

import android.bluetooth.*;
import android.content.Context;

import java.io.IOException;
import java.util.UUID;

public class ClientThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final Context context;

    public ClientThread(Context context, String mac, UUID uuid) {

        this.context = context;

        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac);
        mmDevice = device;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(context.getString(R.string.UUID)));
        } catch (IOException e) { }
        mmSocket = tmp;
    }

    public void run() {

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        // manageConnectedSocket(mmSocket);
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
