package pt.ulisboa.tecnico.sirs.droidcipher;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.CipherHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.KeyGenHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.NotificationsHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Services.MainProtocolService;
import pt.ulisboa.tecnico.sirs.droidcipher.Services.ServiceState;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public ServiceState serviceState = new ServiceState();
    private boolean mIsReceiverRegistered;
    private ServiceStateReceiver mReceiver;

    private TextView runningTv;
    private TextView connectedTv;
    private TextView waitingTv;
    private TextView devicenameTv;
    private TextView deviceaddrTv;
    private Button resetConnectionBt;
    private Button toggleServiceBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        runningTv = (TextView) findViewById(R.id.main_service_running_tv);
        connectedTv = (TextView) findViewById(R.id.main_connected_tv);
        waitingTv = (TextView) findViewById(R.id.main_waiting_connection_tv);
        devicenameTv = (TextView) findViewById(R.id.main_device_name_tv);
        deviceaddrTv = (TextView) findViewById(R.id.main_device_addr_tv);
        resetConnectionBt = (Button) findViewById(R.id.main_reset_connection);
        toggleServiceBt = (Button) findViewById(R.id.main_toggle_service);

        // temporary
        ((EditText) findViewById(R.id.main_pubkey_tv)).setText(KeyGenHelper.printKey(KeyGenHelper.getPublicKey(this)));

        resetConnectionBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainProtocolService.class);
                intent.putExtra(Constants.SERVICE_COMMAND_EXTRA, Constants.RESET_CONN_COMMAND);
                startService(intent);
            }
        });

        toggleServiceBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainProtocolService.class);
                if (serviceState.isOn()) {
                    intent.putExtra(Constants.SERVICE_COMMAND_EXTRA, Constants.STOP_COMMAND);
                }
                startService(intent);
            }
        });
        serviceState.setOn(false);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(LOG_TAG, "Requesting bluetooth permission.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH},
                    0);
        } else {
            Log.i(LOG_TAG, "Bluetooth has been accepted before.");
            init();
        }



    }

    public void init() {
        Intent intent = new Intent(this, MainProtocolService.class);
        startService(intent);
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

    @Override
    protected void onResume() {
        if (!mIsReceiverRegistered) {
            if (mReceiver == null)
                mReceiver = new ServiceStateReceiver();
            registerReceiver(mReceiver, new IntentFilter(MainProtocolService.STATE_CHANGE_ACTION));
            mIsReceiverRegistered = true;
        }
        init();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mIsReceiverRegistered) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
            mIsReceiverRegistered = false;
        }

        super.onPause();

    }

    public void updateUI() {
        connectedTv.setText("-");
        deviceaddrTv.setText("-");
        devicenameTv.setText("-");
        waitingTv.setText("-");
        if (!serviceState.isOn()) {
            runningTv.setText("No");
            toggleServiceBt.setText("Start Service");
            return;
        }
        toggleServiceBt.setText("Stop Service");
        runningTv.setText("Yes");
        if (serviceState.isWaitingUser()) {
            waitingTv.setText("Yes");
        } else {
            waitingTv.setText("No");
        }
        if (!serviceState.isConnected()) {
            connectedTv.setText("No");
            return;
        }
        connectedTv.setText("Yes");
        if (serviceState.getCurrentConnection() != null) {
            devicenameTv.setText(serviceState.getCurrentConnection().getDevice().getName());
            deviceaddrTv.setText(serviceState.getCurrentConnection().getDevice().getAddress());
        }

    }

    public class ServiceStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ServiceState state = intent.getParcelableExtra(MainProtocolService.EXTRA_STATE);
            if (state != null) {
                serviceState = state;
                updateUI();
            }
        }

    }
}
