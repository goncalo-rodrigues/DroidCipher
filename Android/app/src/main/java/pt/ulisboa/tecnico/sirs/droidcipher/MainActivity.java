package pt.ulisboa.tecnico.sirs.droidcipher;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.CipherHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.KeyGenHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.NotificationsHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Services.MainProtocolService;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Requesting bluetooth permission.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH},
                    0);
        } else {
            Log.d(LOG_TAG, "Bluetooth has been accepted before.");
            init();
        }
        EditText tv = (EditText) findViewById(R.id.publickey_tv);
        tv.setText(KeyGenHelper.printKey(KeyGenHelper.getPublicKey(this)));



    }

    public void init() {
        Intent intent = new Intent(this, MainProtocolService.class);
        startService(intent);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        }, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    init();

                } else {

                    Toast.makeText(this, "Bluetooth is needed!", Toast.LENGTH_LONG);
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
