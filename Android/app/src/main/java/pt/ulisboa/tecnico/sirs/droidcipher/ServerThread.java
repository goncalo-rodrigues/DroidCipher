package pt.ulisboa.tecnico.sirs.droidcipher;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class ServerThread extends Thread {
    private final String LOG_TAG = ServerThread.class.getSimpleName();
    private final BluetoothServerSocket mmServerSocket;
    private final Context context;

    public ServerThread(Context context) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        InputStream tmpIn = null;
        this.context = context;

        BluetoothAdapter device = BluetoothAdapter.getDefaultAdapter();

        if (device == null) {
            Log.d(LOG_TAG, "No bluetooth adapter found");
        }

        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = device.listenUsingRfcommWithServiceRecord(context.getString(R.string.app_name),
                    UUID.fromString(context.getString(R.string.androidUUID)));
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
                Log.i("Received message", new String(buffer, 0, size));

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
