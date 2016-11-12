package pt.ulisboa.tecnico.sirs.droidcipher.Services;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import pt.ulisboa.tecnico.sirs.droidcipher.Constants;

import pt.ulisboa.tecnico.sirs.droidcipher.Constants;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.Asserter;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.CipherHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.KeyGenHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.NotificationsHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Interfaces.IAcceptConnectionCallback;
import pt.ulisboa.tecnico.sirs.droidcipher.ServerThread;

/**
 * Created by goncalo on 10-11-2016.
 */

public class MainProtocolService extends Service implements IAcceptConnectionCallback {
    private static final String LOG_TAG = MainProtocolService.class.getSimpleName();
    private SecretKeySpec commKey = null;
    private byte[] commIV = null;
    private SecretKeySpec newCommKey = null;
    private byte[] newCommIV = null;
    private PrivateKey privateKey = null;
    private ServerThread serverThread;
    private boolean accepted = false;
    public MainProtocolService() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serverThread = new ServerThread(this);
        serverThread.run();
        return super.onStartCommand(intent, flags, startId);
    }

    public byte[] onNewMessage(String messageType, byte [] message) {
        if (messageType.equals(Constants.MESSAGE_TYPE_NEWCONNECTION)) {
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

            byte[] decryptedIV = Arrays.copyOfRange(decrypted, 0, 15);
            byte[] decryptedKey = Arrays.copyOfRange(decrypted, 16, decrypted.length);

            if (Asserter.AssertAESKey(decryptedKey)) {
                newCommKey = new SecretKeySpec(decryptedKey, Constants.SYMMETRIC_CIPHER_ALGORITHM);
                newCommIV = decryptedIV;
            } else {
                Log.e(LOG_TAG, "Malformed communication key");
            }

            NotificationsHelper.startNewConnectionNotification(this);
            synchronized(this) {
                try {
                    // Calling wait() will block this thread until another thread
                    // calls notify() on the object.
                    this.wait();
                } catch (InterruptedException e) {
                    // Happens if someone interrupts your thread.
                }
            }
            if (accepted) {
                //todo: decide what to respond on accept/reject of comm key
                return new byte[] {'o', 'k'};
            } else {
                return new byte[] {'n', 'o', 'k'};
            }
        } else if (messageType.equals(Constants.MESSAGE_TYPE_FILEKEY)) {

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
            boolean accepted = commKey != null && commIV != null && privateKey != null;
            if (!accepted) {
                Log.d(LOG_TAG, "Trying to communicate with a rejected session. Ignoring.");
                return null;
            }
            try {
                byte[] decryptedMessage = CipherHelper.AESDecrypt(commKey, commIV, message);
                byte[] fileKey = CipherHelper.RSADecrypt(privateKey, decryptedMessage);
                byte[] encryptedFileKey = CipherHelper.AESEncrypt(commKey, commIV, fileKey);

                return encryptedFileKey;
            } catch (InvalidKeyException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Invalid key!");
            }


        }
        return null;
    }

    @Override
    public void onDestroy() {
        serverThread.cancel();
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
        Log.d(LOG_TAG, "Connection rejected by user.");
        synchronized(this) {
            accepted = false;
            this.notify();
        }
    }
}
