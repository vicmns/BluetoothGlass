package com.vicmns.bluetoothglass.server;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.vicmns.bluetoothglass.server.service.BluetoothService;


public class MainActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private BluetoothAdapter myBt;
    private TextView connectedDevices, messageToShowTV;
    private Context context;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if(bundle.containsKey(BluetoothService.PROGRESS)) {
                    int progress = bundle.getInt(BluetoothService.PROGRESS);
                    messageToShowTV.setText("Transferring file... " + progress + "%");
                } else if(bundle.containsKey(BluetoothService.MESSAGE)) {
                    String message = bundle.getString(BluetoothService.MESSAGE);
                    messageToShowTV.setText(message);
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        // the activity for this is pretty stripped, just a basic selection ui....
        setContentView(R.layout.activity_main);

        connectedDevices = (TextView) findViewById(R.id.connected_devices_values);
        messageToShowTV = (TextView) findViewById(R.id.connected_devices_message);

        initializeBluetooth();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(BluetoothService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "Failed to enable Bluetooth", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Bluetooth Enabled", Toast.LENGTH_LONG).show();
                startBluetoothService();
            }
        }
    }

    private void initializeBluetooth() {
        myBt = BluetoothAdapter.getDefaultAdapter();
        if (myBt == null) {
            Toast.makeText(this, "Device Does not Support Bluetooth", Toast.LENGTH_LONG).show();
        } else if (!myBt.isEnabled()) {
            // we need to wait until bt is enabled before set up, so that's done either in the following else, or
            // in the onActivityResult for our code ...
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            startBluetoothService();
        }
    }

    private void startBluetoothService() {
        Intent i= new Intent(context, BluetoothService.class);
        context.startService(i);
    }

}
