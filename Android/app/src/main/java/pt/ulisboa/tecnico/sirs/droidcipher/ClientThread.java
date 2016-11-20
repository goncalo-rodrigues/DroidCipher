package pt.ulisboa.tecnico.sirs.droidcipher;

import android.bluetooth.*;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;


public class ClientThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final Context context;
    private final byte[] publicKey;
    private final byte[] hash;
    private final int BUFFER_SIZE = 10;
    private final int NUMBER_TRIES = 5;

    public ClientThread(Context context, String mac, String pcUuid, byte[] publicKey, byte[] hash) {
        this.context = context;
        this.publicKey = publicKey;
        this.hash = hash;

        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac);

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(pcUuid));
        } catch (IOException e) { }
        mmSocket = tmp;
    }

    public void run() {
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();

            int tryNumber = 0;
            while(tryNumber < NUMBER_TRIES) {
                sendSmartphoneInfo(publicKey, hash);
                if(goodResponse())
                    break;
                else
                    tryNumber++;
            }

            if(tryNumber == NUMBER_TRIES)
                throw new TooManyTriesException();

        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
        }

        cancel();
    }

    private void sendSmartphoneInfo(byte[] publicKey, byte[] hash)throws IOException {
        OutputStream out = mmSocket.getOutputStream();
        String macAddress = android.provider.Settings.Secure
                .getString(context.getContentResolver(), "bluetooth_address");

        out.write(hash);
        out.write(context.getString(R.string.androidUUID).getBytes("UTF-8"));
        out.write(macAddress.getBytes("UTF-8"));
        out.write(publicKey);
    }

    private boolean goodResponse() throws IOException {
        InputStream in = mmSocket.getInputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int size = in.read(buffer);

        if(size > 2) {
            String message = new String(buffer, 0, size);
            return message.startsWith("OK");
        }

        return false;
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
