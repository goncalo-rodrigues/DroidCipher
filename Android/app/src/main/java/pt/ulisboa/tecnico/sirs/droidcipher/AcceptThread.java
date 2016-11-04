package pt.ulisboa.tecnico.sirs.droidcipher;

import android.bluetooth.*;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;

    private static final String NAME = "DroidCipher"; //TODO: Use the one from strings.xml
    private static final UUID MY_UUID =
            UUID.fromString("d1418830-a213-11e6-bdf4-0800200c9a66");


    public AcceptThread() {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        InputStream tmpIn = null;

        BluetoothAdapter device = BluetoothAdapter.getDefaultAdapter();

        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = device.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) { }

        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        InputStream mmInStream = null;

        try {
            socket = mmServerSocket.accept();
            mmInStream = socket.getInputStream();
        } catch (IOException e) { }

        byte[] buffer = new byte[1024];

        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                int size = mmInStream.read(buffer);
                String s = "";

                // Converts the received text to a string
                for(int i = 0; i < size; i++) {
                    byte[] singleByte = { buffer[i] };
                    s += new String(singleByte, "UTF-8");
                }

                Log.i("Received message", s);
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                try {
                    mmServerSocket.close();
                } catch(IOException e) { }
                break;
            }
        }
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }

}
