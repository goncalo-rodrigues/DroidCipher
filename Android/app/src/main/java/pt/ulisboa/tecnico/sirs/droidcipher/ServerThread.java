package pt.ulisboa.tecnico.sirs.droidcipher;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import pt.ulisboa.tecnico.sirs.droidcipher.Services.MainProtocolService;

public class ServerThread extends Thread {
    private final String LOG_TAG = ServerThread.class.getSimpleName();
    private BluetoothServerSocket mmServerSocket;
    private BluetoothSocket clientSocket = null;
    private final Context context;
    private final MainProtocolService providedService;
    private List<ConnectionThread> clientThreads;

    public ServerThread(MainProtocolService context) {

        this.context = context;
        providedService = context;
        clientThreads = new ArrayList<>();

    }

    public void run() {
        BluetoothAdapter device = BluetoothAdapter.getDefaultAdapter();

        if (device == null) {
            Log.d(LOG_TAG, "No bluetooth adapter found");
        }

        try {
            // MY_UUID is the app's UUID string, also used by the client code
            mmServerSocket = device.listenUsingRfcommWithServiceRecord(context.getString(R.string.app_name),
                    UUID.fromString(context.getString(R.string.androidUUID)));
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to start server thread");
            return;
        }

        Log.d(LOG_TAG, "Listening...");

        // Keep listening until connection to specific client occurs
        while (!Thread.currentThread().isInterrupted()) {
            try {
                clientSocket = mmServerSocket.accept();
            } catch (IOException e) { }

            // create new thread to handle the new client
            ConnectionThread newThread = new ConnectionThread(clientSocket, providedService);
            clientThreads.add(newThread);
            newThread.start();
        }


    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            if (mmServerSocket != null)
                mmServerSocket.close();
            for (ConnectionThread ct: clientThreads) {
                if (!ct.isInterrupted()) {
                    ct.cancel();
                    ct.interrupt();
                }

            }
        }catch (RuntimeException rte) {
            Log.e(LOG_TAG, "Error closing: " + rte);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error closing: " + e);
        }
    }
    @Override
    public void interrupt() {
        try {
            cancel();
        }
        finally {
            super.interrupt();
        }
    }
}
