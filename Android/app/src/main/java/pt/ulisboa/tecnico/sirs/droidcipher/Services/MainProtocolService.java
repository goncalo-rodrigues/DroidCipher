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

import javax.crypto.spec.SecretKeySpec;

import pt.ulisboa.tecnico.sirs.droidcipher.Constants;

import pt.ulisboa.tecnico.sirs.droidcipher.Constants;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.Asserter;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.CipherHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.KeyGenHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Interfaces.IAcceptConnectionCallback;
import pt.ulisboa.tecnico.sirs.droidcipher.ServerThread;

/**
 * Created by goncalo on 10-11-2016.
 */

public class MainProtocolService extends Service implements IAcceptConnectionCallback {
    private static final String LOG_TAG = MainProtocolService.class.getSimpleName();
    private SecretKeySpec commKey = null;
    private PrivateKey privateKey = null;
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
        ServerThread at = new ServerThread(this);
        at.run();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onNewMessage(String messageType, byte [] message) {
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

            if (Asserter.AssertAESKey(decrypted)) {
                commKey = new SecretKeySpec(decrypted, Constants.SYMMETRIC_CIPHER_ALGORITHM);
                KeyGenHelper.saveCommuncationKey(this, decrypted);
            } else {
                Log.e(LOG_TAG, "Malformed communication key");
            }
            return;
        } else if (messageType.equals(Constants.MESSAGE_TYPE_FILEKEY)) {
            if (!accepted) {
                Log.d(LOG_TAG, "Trying to communicate with a rejected session. Ignoring.");
                return;
            }
            // loading to memory
            if (commKey == null) {
                commKey = KeyGenHelper.getLastCommunicationKey(this);
            }
        }
    }


    @Override
    public void OnAcceptConnection() {
        accepted = true;
    }

    @Override
    public void OnRejectConnection() {
        accepted = false;
        Log.d(LOG_TAG, "Connection rejected by user.");
    }
}
