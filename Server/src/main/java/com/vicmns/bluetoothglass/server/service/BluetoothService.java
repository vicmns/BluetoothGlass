package com.vicmns.bluetoothglass.server.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.vicmns.bluetoothglass.server.MainActivity;
import com.vicmns.bluetoothglass.server.R;
import com.vicmns.bluetoothglass.server.handlers.BluetoothConnectionHandler;
import com.vicmns.bluetoothglass.server.handlers.BluetoothReadFromSocketHandler;

/**
 * Created by Victor Cervantes on 3/18/14.
 */
public class BluetoothService extends Service implements
        BluetoothConnectionHandler.BluetoothConnectionHandlerCallbacks,
        BluetoothReadFromSocketHandler.BluetoothSocketHandlerCallbacks {

    public static final String TAG = BluetoothService.class.getSimpleName();
    public static final String NOTIFICATION = "com.vicmns.bluetoothglass.server.service";
    public static final String PROGRESS = "progress";
    public static final String MESSAGE = "message";

    private BluetoothAdapter myBt;
    private BluetoothConnectionHandler bluetoothConnectionHandler;
    private BluetoothReadFromSocketHandler bluetoothReadFromSocketHandler;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder notification;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        if(bluetoothConnectionHandler != null)
                            bluetoothConnectionHandler.cancel(true);
                        if(bluetoothReadFromSocketHandler != null)
                            bluetoothReadFromSocketHandler.cancel(true);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        startBtService();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service Started");
        startBtService();
        startNotificationVariables();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service Stopped");
        unregisterReceiver(mReceiver);
        if(bluetoothConnectionHandler != null)
            bluetoothConnectionHandler.cancel(true);
        if(bluetoothReadFromSocketHandler != null)
            bluetoothReadFromSocketHandler.cancel(true);
    }

    public void startBtService() {
        myBt = BluetoothAdapter.getDefaultAdapter();
        if(myBt != null) {
            if(myBt.isEnabled()) {
                bluetoothConnectionHandler = new BluetoothConnectionHandler(myBt, this);
                bluetoothConnectionHandler.execute();
            }
        } else {
            stopSelf();
        }
    }

    @Override
    public void onConnectionSuccessful(BluetoothSocket socket) {
        bluetoothReadFromSocketHandler = new BluetoothReadFromSocketHandler(socket, this);
        bluetoothReadFromSocketHandler.execute();
    }

    @Override
    public void onConnectionCancel() {
        bluetoothConnectionHandler = new BluetoothConnectionHandler(myBt, this);
        bluetoothConnectionHandler.execute();
    }

    @Override
    public void onTransferProgress(int progress) {
        Log.i(TAG, "Percentage: " + progress + "%");
        publishResults(progress);
        sendProgressNotification(progress, false);
    }

    @Override
    public void onTransferComplete() {
        publishTransferCompletion();
        sendProgressNotification(0, true);

    }

    @Override
    public void onConnectionLost() {
        bluetoothConnectionHandler = new BluetoothConnectionHandler(myBt, this);
        bluetoothConnectionHandler.execute();
    }

    private void startNotificationVariables() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void publishResults(int progress) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(PROGRESS, progress);
        sendBroadcast(intent);
    }

    private void publishTransferCompletion() {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(MESSAGE, "Transfer complete!");
        sendBroadcast(intent);
    }


    private void sendProgressNotification(int progress, boolean isComplete) {
        if(notification == null) {
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
            notification = new NotificationCompat.Builder(this);
            notification.setContentTitle("Picture Download")
                    .setContentText("Download in progress")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pIntent);
        }
        if(!isComplete) {
            notification.setProgress(100, progress, false);
        } else {
            // When the loop is finished, updates the notification
            notification.setContentText("Download complete")
                    // Removes the progress bar
                    .setProgress(0,0,false);
        }

        notificationManager.notify(0, notification.build());
    }
}
