package pt.ulisboa.tecnico.sirs.droidcipher;

import android.Manifest;
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
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.KeyGenHelper;
import pt.ulisboa.tecnico.sirs.droidcipher.Services.MainProtocolService;
import pt.ulisboa.tecnico.sirs.droidcipher.Services.ServiceState;
import pt.ulisboa.tecnico.sirs.droidcipher.adapters.LogListAdapter;
import pt.ulisboa.tecnico.sirs.droidcipher.data.Event;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final int SCANQR_REQUEST_CODE = 1;
    public ServiceState serviceState = new ServiceState();
    private boolean mIsReceiverRegistered;
    private ServiceStateReceiver mReceiver;

    private ListView logList;
    private TextView statusTv;
    private TextView deviceInfoTv;
    private Button stopStartBt;
    private ImageButton addDeviceBt;
    private ImageButton settingsBt;
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
                        serviceIntent.putExtra(Constants.SERVICE_COMMAND_EXTRA, Constants.QR_CODE);
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
        addDeviceBt =(ImageButton) findViewById(R.id.main_add_device_bt);
        settingsBt = (ImageButton) findViewById(R.id.main_settings_bt);
        logAdapter = new LogListAdapter(this, events);
        logList.setAdapter(logAdapter);

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

        settingsBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(MainActivity.this, settingsBt);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.settings_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()) {
                            case R.id.get_pub_key_setting:
                                settingCopyPublicKey();
                                break;
                        }
                        return true;
                    }
                });

                popup.show(); //showing popup menu
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

    public void settingCopyPublicKey() {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("droidcipher pubkey",
                KeyGenHelper.printKey(KeyGenHelper.getPublicKey(MainActivity.this)));
        clipboard.setPrimaryClip(clip);
        Toast.makeText(MainActivity.this, "Public key has been copied to your clipboard", Toast.LENGTH_SHORT).show();
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
        events.clear();
        events.addAll(Event.listAll(Event.class));
        Collections.sort(events);
        logAdapter.notifyDataSetChanged();
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
                Event event = intent.getParcelableExtra(MainProtocolService.EXTRA_EVENT);
                if (!events.contains(event))
                    events.add(event);
                logAdapter.notifyDataSetChanged();
            }


        }

    }
}
