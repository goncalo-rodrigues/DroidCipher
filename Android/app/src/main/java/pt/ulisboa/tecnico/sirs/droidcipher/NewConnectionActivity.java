package pt.ulisboa.tecnico.sirs.droidcipher;

import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import pt.ulisboa.tecnico.sirs.droidcipher.BroadcastReceivers.AcceptConnectionReceiver;
import pt.ulisboa.tecnico.sirs.droidcipher.BroadcastReceivers.DismissNotificationReceiver;

public class NewConnectionActivity extends AppCompatActivity {

    private TextView nameTV;
    private TextView addressTV;
    private Button acceptButton;
    private Button rejectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_connection);
        findViews();

        Intent intent = getIntent();

        if (intent != null) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null) {
                nameTV.setText(device.getName());
                addressTV.setText(device.getAddress());
            }
        }

        int notificationId = intent.getIntExtra(Constants.NOTIFICATION_ID_EXTRA, 0);

        // intent that is called when "accept" is clicked
        final Intent acceptIntent = new Intent(this, AcceptConnectionReceiver.class);
        acceptIntent.putExtra(Constants.NOTIFICATION_ID_EXTRA,notificationId);


        // intent that is called when "close" is clicked
        final Intent closeIntent = new Intent(this, DismissNotificationReceiver.class);
        closeIntent.putExtra(Constants.NOTIFICATION_ID_EXTRA,notificationId);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendBroadcast(acceptIntent);
                NewConnectionActivity.this.finish();
            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendBroadcast(closeIntent);
                NewConnectionActivity.this.finish();
            }
        });
    }

    private void findViews() {
        nameTV = (TextView) findViewById(R.id.newconnection_nametv);
        addressTV = (TextView) findViewById(R.id.newconnection_addresstv);
        acceptButton = (Button) findViewById(R.id.newconnection_acceptbt);
        rejectButton = (Button) findViewById(R.id.newconnection_rejectbt);
    }
}
