package pt.ulisboa.tecnico.sirs.droidcipher.Helpers;

import pt.ulisboa.tecnico.sirs.droidcipher.Constants;

/**
 * Created by goncalo on 10-11-2016.
 */

public class Asserter {
    public static boolean assertAESKey(byte[] keyToAssert) {
        return keyToAssert.length == Constants.AES_KEY_SIZE / 8;
    }

    public static boolean assertMACAddress(byte[] macAddress) {

        if (macAddress.length != 17)
            return false;
        for (int i=0; i < 17; i++) {
            if (i % 3 == 2) {
                if (macAddress[i] != ':') {return false;}
            }
            else {
                if (!isHex(macAddress[i])) {return false;}
            }

        }
        return true;
    }

    public static boolean assertUUID(byte[] uuid) {
        if (uuid.length != 36)
            return false;
        String s = new String(uuid);
        for (int i=0; i < 36; i++) {
            if (i ==8 || i==13 || i==18 || i==23) {
                if (uuid[i] != '-') {return false;}
            }
            else {
                if (!isHex(uuid[i])) {return false;}
            }
        }
        return true;
    }

    public static boolean isHex(int c) {
        String validBytes = "0123456789abcdefABCDEF";
        return validBytes.indexOf(c) != -1;
    }
}
