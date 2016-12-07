package pt.ulisboa.tecnico.sirs.droidcipher;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import pt.ulisboa.tecnico.sirs.droidcipher.Services.MainProtocolService;

/**
 * Created by goncalo on 07-12-2016.
 */

public class ConnectionThread extends Thread {
    private static final String LOG_TAG = ConnectionThread.class.getSimpleName();
    private BluetoothSocket clientSocket = null;
    private MainProtocolService providedService = null;
    private final int BUFFER_SIZE = 1024;
    public ConnectionThread(BluetoothSocket clientSocket, MainProtocolService context) {
        this.clientSocket = clientSocket;
        this.providedService = context;
    }


    @Override
    public void run() {
        InputStream in = null;
        OutputStream out = null;
        byte[] buffer = new byte[BUFFER_SIZE];
        int size = 1;

        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
        } catch (IOException e) { }
        while (size > 0) {
            try {
                size = in.read(buffer);
                Log.i(LOG_TAG, "Received message: " + new String(buffer, 0, size));

                byte[] result;
                byte[] message = Arrays.copyOfRange(buffer, 1, size);
                if (buffer[0] == 0x0) {
                    result = providedService.onNewConnection(message, clientSocket.getRemoteDevice());
                } else {
                    // Provide the service
                    result = providedService.onNewMessage(message);
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

    public void cancel() {

        try {
            if (clientSocket != null)
                clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
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
