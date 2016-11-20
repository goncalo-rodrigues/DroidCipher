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
import java.util.Arrays;
import java.util.UUID;

import pt.ulisboa.tecnico.sirs.droidcipher.Services.MainProtocolService;

public class ServerThread extends Thread {
    private final String LOG_TAG = ServerThread.class.getSimpleName();
    private final BluetoothServerSocket mmServerSocket;
    private BluetoothSocket clientSocket = null;
    private final Context context;
    private final MainProtocolService providedService;

    // TODO: Change the size, so that it receives all the sent bytes on the first try
    private final int BUFFER_SIZE = 1024;

    public ServerThread(MainProtocolService context) {
        BluetoothServerSocket tmp = null;
        this.context = context;
        providedService = context;

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

                String messageType = buffer[0] == 0x0 ? Constants.MESSAGE_TYPE_NEWCONNECTION
                        : Constants.MESSAGE_TYPE_FILEKEY;
                byte[] result;
                byte[] message = Arrays.copyOfRange(buffer, 1, size);
                if (messageType == Constants.MESSAGE_TYPE_NEWCONNECTION) {
                    result = providedService.onNewConnection(message, clientSocket.getRemoteDevice());
                } else {
                    // Provide the service
                    result = providedService.onNewMessage(messageType, message);
                }

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
            clientSocket.close();
        } catch (IOException e) { }
        catch (NullPointerException e) {}
    }

}
