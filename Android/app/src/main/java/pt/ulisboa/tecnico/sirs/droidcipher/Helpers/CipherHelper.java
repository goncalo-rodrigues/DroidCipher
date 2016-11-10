package pt.ulisboa.tecnico.sirs.droidcipher.Helpers;

import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import pt.ulisboa.tecnico.sirs.droidcipher.Constants;

/**
 * Created by goncalo on 04-11-2016.
 */

public class CipherHelper {
    public static  final String LOG_TAG = CipherHelper.class.getSimpleName();

    public static byte[] RSADecrypt(PrivateKey key, byte[] cipheredText) throws InvalidKeyException {
        //Decrypt messsage
        try {
            Cipher cipher = Cipher.getInstance(Constants.ASYMMETRIC_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(cipheredText);
            Log.d(LOG_TAG, "Decrypted message: " + new String(decryptedBytes));
            return decryptedBytes;
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] AESEncrypt(SecretKeySpec key, byte[] iv, byte[] plainText) throws InvalidKeyException{
        final Cipher cipher;
        try {
            cipher = Cipher.getInstance(Constants.SYMMETRIC_CIPHER_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] cipherText = cipher.doFinal(plainText);

            Log.d(LOG_TAG, "Encrypted message (AES): " + cipherText);
            return cipherText;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] AESDecrypt(SecretKeySpec key, byte[] iv, byte[] cipheredText) throws InvalidKeyException{
        final Cipher cipher;
        try {
            cipher = Cipher.getInstance(Constants.SYMMETRIC_CIPHER_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            byte[] plainText = cipher.doFinal(cipheredText);

            Log.d(LOG_TAG, "Decrypted message (AES): " + plainText);
            return plainText;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }


}
