package pt.ulisboa.tecnico.sirs.droidcipher;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import pt.ulisboa.tecnico.sirs.droidcipher.Services.MainProtocolService;

public class RSSIThread extends Thread {
    private final String LOG_TAG = pt.ulisboa.tecnico.sirs.droidcipher.ServerThread.class.getSimpleName();
    private final BluetoothServerSocket mmServerSocket;
    private BluetoothSocket clientSocket = null;
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
        InputStream in = null;
        OutputStream out = null;
        byte[] buffer = new byte[BUFFER_SIZE];


        Log.d(LOG_TAG, "Listening...");

        // Keep listening until connection to specific client occurs
        while (clientSocket == null) {
            try {
                clientSocket = mmServerSocket.accept();
            } catch (IOException e) { }
        }

        // From now on, it will only use the client socket to speak with the PC
        try {
            mmServerSocket.close();
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
        } catch (IOException e) { }

        // It will provide the service until the clientSocket is closed
        while (true) {
            try {
                int size = in.read(buffer);
                Log.i(LOG_TAG, "Received message: " + new String(buffer, 0, size));

                byte[] result;
                byte[] message = Arrays.copyOfRange(buffer, 0, size);

                result = service.onRSSI(message);

                out.write(result);

            } catch (IOException e) {
                break;
            }
        }
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            clientSocket.close();
        } catch (IOException e) { }
        catch (NullPointerException e) {}
    }
}
