package pt.ulisboa.tecnico.sirs.droidcipher.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.camera2.params.BlackLevelPattern;
import android.inputmethodservice.Keyboard;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Base64;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import pt.ulisboa.tecnico.sirs.droidcipher.ClientThread;
import pt.ulisboa.tecnico.sirs.droidcipher.Constants;

import pt.ulisboa.tecnico.sirs.droidcipher.Constants;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.Asserter;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.CipherHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.KeyGenHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.NotificationsHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Interfaces.IAcceptConnectionCallback;
import pt.ulisboa.tecnico.sirs.droidcipher.MainActivity;
import pt.ulisboa.tecnico.sirs.droidcipher.R;
import pt.ulisboa.tecnico.sirs.droidcipher.ServerThread;

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

        int command = intent.getIntExtra(Constants.SERVICE_COMMAND_EXTRA, -1);
        switch (command) {
            case Constants.ACCEPT_COMMAND:
                OnAcceptConnection();
                return super.onStartCommand(intent, flags, startId);
            case Constants.REJECT_COMMAND:
                OnRejectConnection();
                return super.onStartCommand(intent, flags, startId);
            case Constants.RESET_CONN_COMMAND:
                OnStopConnection();
                return super.onStartCommand(intent, flags, startId);
            case Constants.QR_CODE:
                byte[] qrcodeInfo = intent.getByteArrayExtra(Constants.SERVICE_QRCODEINFO_EXTRA);
                onQRCode(qrcodeInfo);
                break;
            case Constants.STOP_COMMAND:
                stopSelf();
                return START_NOT_STICKY;
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
        byte[] macAddressBytes = Arrays.copyOfRange(qrcodeinfo, cursor, (cursor += 17)); //17 bytes
        byte[] pcUUIDBytes = Arrays.copyOfRange(qrcodeinfo, cursor, (cursor += 36)); //36 bytes
        byte[] integrityKeyBytes = Arrays.copyOfRange(qrcodeinfo, cursor, qrcodeinfo.length); //remaining bytes

        String macAddress = new String(macAddressBytes);
        String pcUUID = new String(pcUUIDBytes);
        SecretKeySpec integrityKey = new SecretKeySpec(integrityKeyBytes, Constants.SYMMETRIC_CIPHER_ALGORITHM);
        PublicKey publicKey = KeyGenHelper.getPublicKey(this);
        byte[] pubKey = KeyGenHelper.printKey(publicKey).getBytes();
        byte[] hmac = CipherHelper.HMac(pubKey, integrityKey);

        ClientThread client = new ClientThread(this, macAddress, pcUUID, pubKey, hmac);

        client.start();

        logEvent(Events.NEW_DEVICE_ADDED, null);
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

        byte[] nonce = Arrays.copyOfRange(decrypted, 0, 4);
        byte[] decryptedIV = Arrays.copyOfRange(decrypted, 4, 20);
        byte[] decryptedKey = Arrays.copyOfRange(decrypted, 20, decrypted.length);

        if (Asserter.AssertAESKey(decryptedKey)) {
            newCommKey = new SecretKeySpec(decryptedKey, Constants.SYMMETRIC_CIPHER_ALGORITHM);
            newCommIV = decryptedIV;
        } else {
            Log.e(LOG_TAG, "Malformed communication key");
        }
        Connection connection = new Connection(device,
                Base64.encodeToString(nonce, Base64.DEFAULT));

        logEvent(Events.NEW_CONNECTION_REQUEST, connection);
        state.setWaitingUser(true);
        state.setIncomingConnection(connection);
        broadcastState();
        NotificationsHelper.startNewConnectionNotification(this, connection);
        synchronized(this) {
            try {
                // Calling wait() will block this thread until another thread
                // calls notify() on the object.
                this.wait();
            } catch (InterruptedException e) {
                // Happens if someone interrupts your thread.
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
            } catch (InvalidKeyException e) {
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
        OnStopConnection();
        if (serverThread != null)
            serverThread.cancel();
        serverThread = null;
        logEvent(Events.SERVICE_STOPPED, null);
    }

    @Override
    public void OnAcceptConnection() {
        KeyGenHelper.saveCommuncationKey(this, newCommKey.getEncoded(), newCommIV);
        commKey = newCommKey;
        commIV = newCommIV;
        newCommKey = null;
        newCommIV = null;
        synchronized(this) {
            accepted = true;
            this.notify();
        }
    }

    @Override
    public void OnRejectConnection() {
        newCommKey = null;
        newCommIV = null;
        Log.i(LOG_TAG, "Connection rejected by user.");
        synchronized(this) {
            accepted = false;
            this.notify();
        }
    }

    public void OnStopConnection() {
        commKey = null;
        commIV = null;
        KeyGenHelper.saveCommuncationKey(this, null, null);
        state.setCurrentConnection(null);
        state.setConnected(false);
        stopForeground(true);
        broadcastState();

    }

    public void broadcastState() {
        Intent intent = new Intent(STATE_CHANGE_ACTION);
        intent.putExtra(EXTRA_STATE, state);
        sendBroadcast(intent);
    }

    public void logEvent(int eventId, Connection currentConnection) {
        Intent intent = new Intent(NEW_EVENT_ACTION);
        intent.putExtra(EXTRA_EVENT, eventId);
        intent.putExtra(EXTRA_CONNECTION, currentConnection);
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
