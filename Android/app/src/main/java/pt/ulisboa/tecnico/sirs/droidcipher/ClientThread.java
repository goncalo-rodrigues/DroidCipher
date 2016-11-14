package pt.ulisboa.tecnico.sirs.droidcipher;

import android.bluetooth.*;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

// Nuno's PC bluetooth MAC address: 48:45:20:C3:19:09
public class ClientThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final Context context;

    public ClientThread(Context context, String mac) {
        this.context = context;

        // Used for testing purposes
        mac = mac == "" ? "48:45:20:C3:19:09" : mac;

        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac);

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(context.getString(R.string.pcUUID)));
        } catch (IOException e) { }
        mmSocket = tmp;
    }

    public void run() {
        InputStream in = null;
        OutputStream out = null;

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
            out = mmSocket.getOutputStream();
            String s = "I am the client!";
            out.write(s.getBytes(Charset.forName("UTF-8")));

            in = mmSocket.getInputStream();
            byte[] buffer = new byte[1024];
            int size = in.read(buffer);
            Log.i("Received message", new String(buffer, 0, size));

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
