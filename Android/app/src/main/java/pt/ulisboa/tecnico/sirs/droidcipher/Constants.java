package pt.ulisboa.tecnico.sirs.droidcipher;

import java.security.PublicKey;

/**
 * Created by goncalo on 03-11-2016.
 */

public final class Constants {
    public static final String PREFERENCES_KEY_FILENAME = "keys";
    public static final String PRIVATE_KEY_PREF = "prefs:private_key";
    public static final String PUBLIC_KEY_PREF = "prefs:public_key";
    public static final String COMMUNICATION_KEY_PREF = "prefs:comm_key";
    public static final String COMMUNICATION_IV_PREF = "prefs:comm_iv";
    public static final String NOTIFICATION_ID_EXTRA = "notifications:extra:not_id";
    public static final String SYMMETRIC_CIPHER_ALGORITHM = "AES/CBC/PKCS7Padding";
    public static final String ASYMMETRIC_CIPHER_ALGORITHM = "RSA/NONE/OAEPWithSHA256AndMGF1Padding";
    public static final String MAIN_PROTO_DATA = "extras:main_proto_data";
    public static final String MESSAGE_TYPE = "extras:message_type";
    public static final String MESSAGE_TYPE_NEWCONNECTION = "message_type:new_connection";
    public static final String MESSAGE_TYPE_FILEKEY = "message_type:file_key";
    public static final String HMAC_ALGORITHM = "HmacSHA512";

    public static final String SERVICE_COMMAND_EXTRA = "extras:service_command";
    public static final String SERVICE_QRCODEINFO_EXTRA = "extras:service_qrcodeinfo";


    public static final int ACCEPT_COMMAND = 1;
    public static final int REJECT_COMMAND = 2;
    public static final int RESET_CONN_COMMAND = 3;
    public static final int STOP_COMMAND = 4;
    public static final int QR_CODE = 5;
}
