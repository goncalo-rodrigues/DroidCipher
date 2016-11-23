package pt.ulisboa.tecnico.sirs.droidcipher.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import pt.ulisboa.tecnico.sirs.droidcipher.ClientThread;
import pt.ulisboa.tecnico.sirs.droidcipher.Constants;

import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.Asserter;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.CipherHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.KeyGenHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.NotificationsHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Interfaces.IAcceptConnectionCallback;
import pt.ulisboa.tecnico.sirs.droidcipher.MainActivity;
import pt.ulisboa.tecnico.sirs.droidcipher.R;
import pt.ulisboa.tecnico.sirs.droidcipher.ServerThread;
import pt.ulisboa.tecnico.sirs.droidcipher.data.Event;

/**
 * Created by goncalo on 10-11-2016.
 */

public class MainProtocolService extends Service implements IAcceptConnectionCallback {
    private static final String LOG_TAG = MainProtocolService.class.getSimpleName();
    public static final String STATE_CHANGE_ACTION = "pt.ulisboa.tecnico.sirs.droidcipher.services.MainProtocolService.STATE_CHANGED";
    public static final String NEW_EVENT_ACTION = "pt.ulisboa.tecnico.sirs.droidcipher.services.MainProtocolService.NEW_EVENT";

    public static final String EXTRA_STATE = "extra:service_state";
    public static final String EXTRA_EVENT = "extra:event";
    public static final String EXTRA_CONNECTION = "extra:current_connection";
    public static final String EXTRA_DEVICE = "extra:current_device";

    private final IBinder mBinder = new LocalBinder();
    private SecretKeySpec commKey = null;
    private byte[] commIV = null;

    private SecretKeySpec newCommKey = null;
    private byte[] newCommIV = null;

    private PrivateKey privateKey = null;
    private ServerThread serverThread;
    private boolean accepted = false;

    public ServiceState state;
    public MainProtocolService() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "Created");
        state = new ServiceState();
        state.setOn(false);
        state.setConnected(false);
        state.setWaitingUser(false);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (!KeyGenHelper.isKeyPairStored(this)) {
            KeyGenHelper.generateNewKeyPair(this);
        }
        if (intent != null) {

            int command = intent.getIntExtra(Constants.SERVICE_COMMAND_EXTRA, -1);
            switch (command) {
                case Constants.ACCEPT_COMMAND:
                    Connection toBeAccepted = intent.getParcelableExtra(EXTRA_CONNECTION);
                    onAcceptConnection(toBeAccepted);
                    return super.onStartCommand(intent, flags, startId);
                case Constants.REJECT_COMMAND:
                    Connection toBeRejected = intent.getParcelableExtra(EXTRA_CONNECTION);
                    onRejectConnection(toBeRejected);
                    return super.onStartCommand(intent, flags, startId);
                case Constants.RESET_CONN_COMMAND:
                    BluetoothDevice toBeStopped = intent.getParcelableExtra(EXTRA_DEVICE);
                    if (toBeStopped == null)
                        onStopCurrentConnection();
                    else onStopConnection(toBeStopped);
                    return super.onStartCommand(intent, flags, startId);
                case Constants.QR_CODE:
                    byte[] qrcodeInfo = intent.getByteArrayExtra(Constants.SERVICE_QRCODEINFO_EXTRA);
                    onQRCode(qrcodeInfo);
                    return super.onStartCommand(intent, flags, startId);
                case Constants.STOP_COMMAND:
                    stopSelf();
                    return START_NOT_STICKY;
            }
        }

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            Log.d(LOG_TAG, "Started");
            if (serverThread == null) {
                serverThread = new ServerThread(this);
                serverThread.start();
                logEvent(Events.SERVICE_STARTED, null);
            }
            state.setOn(true);
            broadcastState();
            return super.onStartCommand(intent, flags, startId);
        }
        stopSelf();
        return START_NOT_STICKY;

    }

    public void onQRCode(byte[] qrcodeinfo) {
        int cursor = 0;
        byte[] macAddressBytes;
        byte[] pcUUIDBytes;
        byte[] integrityKeyBytesEncoded;
        byte[] integrityKeyBytes;

        try {
            macAddressBytes = Arrays.copyOfRange(qrcodeinfo, cursor, (cursor += 17)); //17 bytes
            pcUUIDBytes = Arrays.copyOfRange(qrcodeinfo, cursor, (cursor += 36)); //36 bytes
            integrityKeyBytesEncoded = Arrays.copyOfRange(qrcodeinfo, cursor, qrcodeinfo.length); //remaining bytes
            integrityKeyBytes = Base64.decode(integrityKeyBytesEncoded, Base64.DEFAULT);
        } catch (IndexOutOfBoundsException e) {
            Log.e(LOG_TAG, "Qr code is too small");
            logEvent(Events.FAILED_QRCODE, null);
            return;
        }

        if (!Asserter.assertMACAddress(macAddressBytes)) {
            Log.e(LOG_TAG, "Qr code does not contain a valid mac address");
            logEvent(Events.FAILED_QRCODE, null);
            return;
        }

        if (!Asserter.assertUUID(pcUUIDBytes)) {
            Log.e(LOG_TAG, "Qr code does not contain a valid UUID");
            logEvent(Events.FAILED_QRCODE, null);
            return;
        }

        if (!Asserter.assertAESKey(integrityKeyBytes)) {
            Log.e(LOG_TAG, "Qr code doest not contain a AES Key");
            logEvent(Events.FAILED_QRCODE, null);
            return;
        }

        String macAddress = new String(macAddressBytes);
        String pcUUID = new String(pcUUIDBytes);
        byte[] pubKey;
        byte[] hmac;
        SecretKeySpec integrityKey = new SecretKeySpec(integrityKeyBytes, Constants.SYMMETRIC_CIPHER_ALGORITHM);
        PublicKey publicKey = KeyGenHelper.getPublicKey(this);
        pubKey = KeyGenHelper.printKey(publicKey).getBytes();
        hmac = CipherHelper.HMac(pubKey, integrityKey);

        ClientThread client = new ClientThread(this, macAddress, pcUUID, pubKey, hmac);

        client.start();

        //logEvent(Events.NEW_DEVICE_ADDED, null);
    }

    public byte[] onNewConnection(byte[] message, BluetoothDevice device) {
        accepted = false;
        if (privateKey == null) {
            privateKey = KeyGenHelper.getPrivateKey(this);
        }
        byte[] decrypted = null;
        try {
            decrypted = CipherHelper.RSADecrypt(privateKey, message);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Invalid private key");
        }

        if (decrypted == null || decrypted.length < 20) {
            logEvent(Events.FAILED_CONNECTION_REQUEST, new Connection(device, "-"));
            return null;
        }

        byte[] nonce = Arrays.copyOfRange(decrypted, 0, 4);
        byte[] decryptedIV = Arrays.copyOfRange(decrypted, 4, 20);
        byte[] decryptedKey = Arrays.copyOfRange(decrypted, 20, decrypted.length);
        Connection connection = new Connection(device,
                Base64.encodeToString(nonce, Base64.DEFAULT));

        if (!Asserter.assertAESKey(decryptedKey)) {
            logEvent(Events.FAILED_CONNECTION_REQUEST, connection);
            Log.e(LOG_TAG, "Malformed communication key");
            return null;
        }

        logEvent(Events.NEW_CONNECTION_REQUEST, connection);


        synchronized(this) {
            try {
                state.setWaitingUser(true);
                state.setIncomingConnection(connection);
                broadcastState();

                newCommKey = new SecretKeySpec(decryptedKey, Constants.SYMMETRIC_CIPHER_ALGORITHM);
                newCommIV = decryptedIV;

                NotificationsHelper.startNewConnectionNotification(this, connection);
                this.wait();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Service was closed before getting an answer from an user");
                // Unable to get an answer
                return null;
            }
        }
        state.setIncomingConnection(null);
        state.setWaitingUser(false);
        if (accepted) {
            if (state.getCurrentConnection() != null) {
                logEvent(Events.CONNECTION_LOST, state.getCurrentConnection());
            }
            logEvent(Events.ACCEPTED_CONNECTION, connection);
            setForeground();
            state.setCurrentConnection(connection);
            state.setConnected(true);
            broadcastState();

            return nonce;
        } else {
            broadcastState();
            logEvent(Events.REJECTED_CONNECTION, connection);
            return null;
        }
    }
    public byte[] onNewMessage(String messageType, byte [] message) {
         if (messageType.equals(Constants.MESSAGE_TYPE_FILEKEY)) {

            // loading to memory
            if (commKey == null) {
                commKey = KeyGenHelper.getLastCommunicationKey(this);
            }
            if (commIV == null) {
                commIV = KeyGenHelper.getLastCommunicationIV(this);
            }
            if (privateKey == null) {
                privateKey = KeyGenHelper.getPrivateKey(this);
            }

            Log.d(LOG_TAG, "IV:" + Base64.encodeToString(commIV, Base64.DEFAULT));
            boolean accepted = commKey != null && commIV != null && privateKey != null;
            if (!accepted) {
                Log.e(LOG_TAG, "Trying to communicate with a rejected session. Ignoring.");
                return null;
            }
            try {
                byte[] decryptedMessage = CipherHelper.AESDecrypt(commKey, commIV, message);
                byte[] fileKey = CipherHelper.RSADecrypt(privateKey, decryptedMessage);
                byte[] encryptedFileKey = CipherHelper.AESEncrypt(commKey, commIV, fileKey);
                logEvent(Events.FILE_DECRYPT_REQUEST, state.getCurrentConnection());
                return encryptedFileKey;
            } catch (Exception e) {
                logEvent(Events.FAILED_FILE_DECRYPT_REQUEST, state.getCurrentConnection());
                e.printStackTrace();
                Log.e(LOG_TAG, "Invalid key!");
            }

        }
        else {
             Log.e(LOG_TAG, "Unknown message type");
         }
        return null;
    }

    public void OnStop() {
        stopSelf();
    }
    @Override
    public void onDestroy() {

        state.setOn(false);
        onStopCurrentConnection();
        if (serverThread != null) {
            serverThread.cancel();
            serverThread.interrupt();
        }

        serverThread = null;

        logEvent(Events.SERVICE_STOPPED, null);
    }

    @Override
    public void onAcceptConnection(Connection toBeAccepted) {
        synchronized (this) {
            String commId = toBeAccepted.getConnectionId();
            if (newCommKey != null && newCommIV != null && state.getIncomingConnection() != null &&
                    state.getIncomingConnection().getConnectionId().equals(commId)) {
                KeyGenHelper.saveCommuncationKey(this, newCommKey.getEncoded(), newCommIV);
                commKey = newCommKey;
                commIV = newCommIV;
                newCommKey = null;
                newCommIV = null;
                accepted = true;
                this.notify();
            } else {
                Log.d(LOG_TAG, "Accepted old connection. Ignoring.");
            }
        }
    }

    @Override
    public void onRejectConnection(Connection toBeRejected) {
        synchronized (this) {
            String commId = toBeRejected.getConnectionId();
            if (newCommKey != null && newCommIV != null && state.getIncomingConnection() != null &&
                    state.getIncomingConnection().getConnectionId().equals(commId)) {
                newCommKey = null;
                newCommIV = null;
                accepted = false;
                this.notify();
            } else {
                Log.d(LOG_TAG, "Accepted old connection. Ignoring.");
            }
        }
    }

    public void onStopCurrentConnection() {
        commKey = null;
        commIV = null;
        KeyGenHelper.saveCommuncationKey(this, null, null);
        if (state.getCurrentConnection() != null) {
            logEvent(Events.CONNECTION_LOST, state.getCurrentConnection());
        }
        state.setCurrentConnection(null);
        state.setConnected(false);
        stopForeground(true);

        broadcastState();

    }

    public void onStopConnection(BluetoothDevice device) {
        Connection conn = state.getCurrentConnection();
        if (conn != null && conn.getDevice().equals(device))
            onStopCurrentConnection();
    }

    // TODO: call when device added
    public void onDeviceAdded(BluetoothDevice device) {
        logEvent(Events.NEW_DEVICE_ADDED, new Connection(device, "-"));
    }

    public void onDeviceAddFail(BluetoothDevice device) {
        logEvent(Events.FAILED_QRCODE, new Connection(device, "-"));
    }

    public void broadcastState() {
        Intent intent = new Intent(STATE_CHANGE_ACTION);
        intent.putExtra(EXTRA_STATE, state);
        sendBroadcast(intent);
    }

    public void logEvent(int eventId, Connection currentConnection) {
        Intent intent = new Intent(NEW_EVENT_ACTION);
        Event event = new Event(eventId, currentConnection);
        event.save();
        intent.putExtra(EXTRA_EVENT, event);

        sendBroadcast(intent);
    }

    public void setForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        Intent closeIntent = new Intent(this, MainProtocolService.class);
        closeIntent.putExtra(Constants.SERVICE_COMMAND_EXTRA, Constants.RESET_CONN_COMMAND);
        PendingIntent pendingCloseIntent=PendingIntent.getService(this, 0,
                closeIntent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification=new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(getString(R.string.service_short))
                .setContentTitle(getString(R.string.service_title))
                .addAction(R.drawable.ic_close_black_24dp, getString(R.string.cancel_connection), pendingCloseIntent)
                .setContentIntent(pendingIntent).build();

        startForeground(1, notification);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public MainProtocolService getService() {
            // Return this instance so clients can call public methods
            return MainProtocolService.this;
        }
    }
}
