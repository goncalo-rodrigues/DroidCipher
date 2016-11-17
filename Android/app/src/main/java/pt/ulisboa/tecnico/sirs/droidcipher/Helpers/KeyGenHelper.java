package pt.ulisboa.tecnico.sirs.droidcipher.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import pt.ulisboa.tecnico.sirs.droidcipher.Constants;

/**
 * Created by goncalo on 03-11-2016.
 */

public class KeyGenHelper {
    public static final String LOG_TAG = KeyGenHelper.class.getSimpleName();


    public static SecretKeySpec getLastCommunicationKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCES_KEY_FILENAME, Context.MODE_PRIVATE);
        String keyString = prefs.getString(Constants.COMMUNICATION_KEY_PREF, null);
        byte[] keyBytes = Base64.decode(keyString, Base64.DEFAULT);
        return new SecretKeySpec(keyBytes, Constants.SYMMETRIC_CIPHER_ALGORITHM);
    }

    public static byte[] getLastCommunicationIV(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCES_KEY_FILENAME, Context.MODE_PRIVATE);
        String ivString = prefs.getString(Constants.COMMUNICATION_IV_PREF, null);
        byte[] ivBytes = Base64.decode(ivString, Base64.DEFAULT);
        return ivBytes;
    }

    public static void saveCommuncationKey(Context context, byte[] keyBytes, byte[] ivBytes) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCES_KEY_FILENAME, Context.MODE_PRIVATE);
        String keyString = null;
        String ivString = null;
        if (keyBytes != null)
            keyString = Base64.encodeToString(keyBytes, Base64.DEFAULT);
        if (ivBytes != null)
            ivString = Base64.encodeToString(ivBytes, Base64.DEFAULT);
        prefs.edit().putString(Constants.COMMUNICATION_KEY_PREF, keyString).apply();
        prefs.edit().putString(Constants.COMMUNICATION_IV_PREF, ivString).apply();
    }


    public static PrivateKey getPrivateKey(Context context) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCES_KEY_FILENAME, Context.MODE_PRIVATE);
            String privateKeyString = prefs.getString(Constants.PRIVATE_KEY_PREF, null);
            byte[] privateKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT);
            PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            Log.d(LOG_TAG, "PrivateKey: ..." + privateKeyString.substring(privateKeyString.length() - 5));
            return privateKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public static PublicKey getPublicKey(Context context) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCES_KEY_FILENAME, Context.MODE_PRIVATE);
            String publicKeyString = prefs.getString(Constants.PUBLIC_KEY_PREF, null);
            byte[] publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT);
            PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            Log.d(LOG_TAG, "PublicKey: ..." + publicKeyString.substring(publicKeyString.length() - 5));
            return publicKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public static void generateNewKeyPair(Context context) {
        try {

            KeyPair newPair = KeyPairGenerator.getInstance("RSA").genKeyPair();
            SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCES_KEY_FILENAME, Context.MODE_PRIVATE);

            String privateKeyString = printKey(newPair.getPrivate());
            String publicKeyString = printKey(newPair.getPublic());

            prefs.edit().putString(Constants.PRIVATE_KEY_PREF, privateKeyString).commit();
            prefs.edit().putString(Constants.PUBLIC_KEY_PREF, publicKeyString).commit();

            Log.d(LOG_TAG, "Creating new KeyPair. PublicKey: ..." +
                    publicKeyString.substring(publicKeyString.length() - 5) +
                    "PrivateKey: ..." + privateKeyString.substring(privateKeyString.length() - 5));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String printKey(Key key) {
        return Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
    }

    public static String printKeyHex(Key key) {
        return Arrays.toString(key.getEncoded());
    }

    public static boolean isKeyPairStored(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCES_KEY_FILENAME, Context.MODE_PRIVATE);
        String publicKey = prefs.getString(Constants.PUBLIC_KEY_PREF, null);
        return publicKey != null;
    }
}
