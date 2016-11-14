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

public class ServerThread extends Thread {
    private final String LOG_TAG = ServerThread.class.getSimpleName();
    private final BluetoothServerSocket mmServerSocket;
    private final Context context;
    private final MainProtocolService providedService;

    // TODO: Change the size, so that it receives all the sent bytes on the first try
    private final int BUFFER_SIZE = 1024;

    public ServerThread(Context context) {
        BluetoothServerSocket tmp = null;
        this.context = context;
        providedService = new MainProtocolService();

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
        InputStream in = null;
        OutputStream out = null;
        byte[] buffer = new byte[BUFFER_SIZE];

        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }

            // If a connection was accepted
            if (socket != null) {
                try {
                    in = socket.getInputStream();
                    int size = in.read(buffer);
                    Log.i("Received message", new String(buffer, 0, size));

                    String messageType = buffer[0] == 0x0 ? Constants.MESSAGE_TYPE_NEWCONNECTION
                            : Constants.MESSAGE_TYPE_FILEKEY;

                    // Provide the service
                    // TODO: See what to do when the result is null
                    byte[] result = providedService.onNewMessage(messageType,
                            Arrays.copyOfRange(buffer, 1, buffer.length));

                    out = socket.getOutputStream();
                    out.write(result);

                    mmServerSocket.close();
                } catch(IOException e) {
                    break;
                }
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
