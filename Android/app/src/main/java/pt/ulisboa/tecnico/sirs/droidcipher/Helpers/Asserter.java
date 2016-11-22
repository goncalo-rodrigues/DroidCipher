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
            if (i % 3 == 2)
                if (macAddress[i] != ':') {return false;}
            else
                if (!isHex(macAddress[i])) {return false;}
        }
        return true;
    }

    public static boolean assertUUID(byte[] uuid) {
        if (uuid.length != 36)
            return false;
        if (uuid[8] != '-' || uuid[13] != '-' || uuid[18] != '-' || uuid[23] != '-')
            return false;
        for (byte b: uuid) {
            if (!isHex(b))
                return false;
        }
        return true;
    }

    public static boolean isHex(int c) {
        String validBytes = "0123456789abcdefABCDEF";
        return validBytes.indexOf(c) != -1;
    }
}
