package pt.ulisboa.tecnico.sirs.droidcipher;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;

import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.KeyGenHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Services.Connection;
import pt.ulisboa.tecnico.sirs.droidcipher.Services.Events;
import pt.ulisboa.tecnico.sirs.droidcipher.Services.MainProtocolService;
import pt.ulisboa.tecnico.sirs.droidcipher.Services.ServiceState;
import pt.ulisboa.tecnico.sirs.droidcipher.adapters.LogListAdapter;
import pt.ulisboa.tecnico.sirs.droidcipher.data.Event;

public class MainActivity extends Activity {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final int SCANQR_REQUEST_CODE = 1;
    public ServiceState serviceState = new ServiceState();
    private boolean mIsReceiverRegistered;
    private ServiceStateReceiver mReceiver;

    private ListView logList;
    private TextView statusTv;
    private TextView deviceInfoTv;
    private Button stopStartBt;
    private Button addDeviceBt;
    private LogListAdapter logAdapter;
    private ArrayList<Event> events = new ArrayList<>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case SCANQR_REQUEST_CODE:
                switch(resultCode) {
                    case RESULT_OK:
                        String rawData = data.getStringExtra(QRCodeReaderActivity.RESULT);
                        Intent serviceIntent = new Intent(this, MainProtocolService.class);
                        serviceIntent.putExtra(Constants.SERVICE_QRCODEINFO_EXTRA, rawData.getBytes());
                        startService(serviceIntent);
                        Log.d(LOG_TAG, "QR: " + rawData);
                        break;
                    case RESULT_CANCELED:
                        Toast.makeText(this, "Operation cancelled by user", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logList = (ListView) findViewById(R.id.main_log_lv);
        logList.setDivider(null);
        statusTv = (TextView) findViewById(R.id.main_service_status);
        deviceInfoTv = (TextView) findViewById(R.id.main_device_name);
        stopStartBt = (Button) findViewById(R.id.main_stopstart_bt);
        addDeviceBt =(Button) findViewById(R.id.main_add_device_bt);
        logAdapter = new LogListAdapter(this, events);
        logList.setAdapter(logAdapter);
        // temporary
        //((EditText) findViewById(R.id.main_pubkey_tv)).setText(KeyGenHelper.printKey(KeyGenHelper.getPublicKey(this)));

//        resetConnectionBt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, MainProtocolService.class);
//                intent.putExtra(Constants.SERVICE_COMMAND_EXTRA, Constants.RESET_CONN_COMMAND);
//                startService(intent);
//            }
//        });
//
        stopStartBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainProtocolService.class);
                if (serviceState.isOn()) {
                    intent.putExtra(Constants.SERVICE_COMMAND_EXTRA, Constants.STOP_COMMAND);
                }
                startService(intent);
            }
        });

        addDeviceBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, QRCodeReaderActivity.class);
                startActivityForResult(intent, SCANQR_REQUEST_CODE);
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

                    Toast.makeText(this, "Bluetooth is needed!", Toast.LENGTH_LONG).show();
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
            IntentFilter stateChanged = new IntentFilter(MainProtocolService.STATE_CHANGE_ACTION);
            IntentFilter eventLogger = new IntentFilter(MainProtocolService.NEW_EVENT_ACTION);
            registerReceiver(mReceiver, stateChanged);
            registerReceiver(mReceiver, eventLogger);
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

        if (serviceState.isOn()) {
            statusTv.setText(getString(R.string.service_running));
            stopStartBt.setText(R.string.button_stop);
        } else {
            statusTv.setText(getString(R.string.service_stopped));
            stopStartBt.setText(R.string.button_start);
        }

        if (serviceState.isConnected() && serviceState.getCurrentConnection() != null) {
            deviceInfoTv.setText(serviceState.getCurrentConnection().getDevice().getName());
        } else {
            deviceInfoTv.setText("-");
        }

    }

    public void newEvent(int eventID, Connection conn) {
        Event newEvent = new Event();
        String description = "";
        int icon = -1;
        switch(eventID) {
            case Events.SERVICE_STARTED:
                description = "Service started running";
                icon = R.drawable.ic_arrows_circle_check;
                break;
            case Events.SERVICE_STOPPED:
                description = "Service stopped running";
                icon = R.drawable.ic_arrows_circle_check;
                break;
            case Events.ACCEPTED_CONNECTION:
                description = "Connection established with " + conn.getDevice().getName() +
                        " (ID:" + conn.getConnectionId() +")";
                icon = R.drawable.ic_arrows_circle_check;
                break;
            case Events.NEW_CONNECTION_REQUEST:
                description = "New connection request from " + conn.getDevice().getName() +
                        " (ID:" + conn.getConnectionId() +")";
                icon = R.drawable.ic_arrows_circle_check;
                break;
            case Events.FILE_DECRYPT_REQUEST:
                description = "File decryption request from " + conn.getDevice().getName();
                icon = R.drawable.ic_arrows_circle_check;
                break;
            case Events.CONNECTION_LOST:
                description = "Connection with " + conn.getDevice().getName() +
                        " (ID:" + conn.getConnectionId() +") was dropped";
                icon = R.drawable.ic_arrows_circle_lightning;
                break;
            case Events.NEW_DEVICE_ADDED:
                description = "QRCode was read and a new device has been added";
                icon = R.drawable.ic_arrows_circle_check;
                break;
            case Events.REJECTED_CONNECTION:
                description = "Connection rejected with " + conn.getDevice().getName() +
                        " (ID:" + conn.getConnectionId() +")";
                icon = R.drawable.ic_arrows_deny;
                break;
            default:
                description = "Unknown event";
                icon = R.drawable.ic_arrows_circle_check;
                break;
        }
        newEvent.setDescription(description);

        newEvent.setIcon(icon);
        events.add(newEvent);
        logAdapter.notifyDataSetChanged();
    }

    public class ServiceStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainProtocolService.STATE_CHANGE_ACTION)) {
                ServiceState state = intent.getParcelableExtra(MainProtocolService.EXTRA_STATE);
                if (state != null) {
                    serviceState = state;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            updateUI();
                        }
                    });
                }
            } else if (intent.getAction().equals(MainProtocolService.NEW_EVENT_ACTION)) {
                int eventId = intent.getIntExtra(MainProtocolService.EXTRA_EVENT, -1);
                Connection conn = intent.getParcelableExtra(MainProtocolService.EXTRA_CONNECTION);
                newEvent(eventId, conn);
            }


        }

    }
}
