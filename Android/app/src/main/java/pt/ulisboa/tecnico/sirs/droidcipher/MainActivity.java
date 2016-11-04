package pt.ulisboa.tecnico.sirs.droidcipher;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import pt.ulisboa.tecnico.sirs.droidcipher.Helpers.NotificationsHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AcceptThread at = new AcceptThread();
        at.run();

        NotificationsHelper.startNewConnectionNotification(this);
    }



}
