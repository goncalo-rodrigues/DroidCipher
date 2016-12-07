package pt.ulisboa.tecnico.sirs.droidcipher;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import pt.ulisboa.tecnico.sirs.droidcipher.Services.MainProtocolService;

public class RSSIThread extends Thread {
    private final String LOG_TAG = pt.ulisboa.tecnico.sirs.droidcipher.RSSIThread.class.getSimpleName();
    private final BluetoothServerSocket mmServerSocket;
    private final MainProtocolService service;
    private final int BUFFER_SIZE = 1024;

    public RSSIThread(MainProtocolService context) {
        this.service = context;
        BluetoothServerSocket tmp = null;

        BluetoothAdapter device = BluetoothAdapter.getDefaultAdapter();

        if (device == null) {
            Log.d(LOG_TAG, "No bluetooth adapter found");
        }

        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = device.listenUsingRfcommWithServiceRecord(context.getString(R.string.app_name),
                    UUID.fromString(context.getString(R.string.rssiUUID)));
        } catch (IOException e) { }

        mmServerSocket = tmp;

    }

    public void run() {
        Log.d(LOG_TAG, "Listening...");

        // It is the only way of always getting the RSSI value
        while (true) {
            try {
                BluetoothSocket clientSocket = mmServerSocket.accept();

                byte[] buffer = new byte[BUFFER_SIZE];
                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream();

                int size = in.read(buffer);
                Log.i(LOG_TAG, "Received message: " + new String(buffer, 0, size));

                byte[] result;
                byte[] message = Arrays.copyOfRange(buffer, 0, size);

                result = service.onRSSI(message);

                if (result == null) {
                    byte[] error = {0x0};
                    result = error;
                }

                out.write(result);

            } catch (IOException e) {
                break;
            }
        }
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
        catch (NullPointerException e) {}
    }
}
