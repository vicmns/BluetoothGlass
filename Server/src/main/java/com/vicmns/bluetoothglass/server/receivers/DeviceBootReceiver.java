package com.vicmns.bluetoothglass.server.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.vicmns.bluetoothglass.server.service.BluetoothService;

/**
 * Created by Victor Cervantes on 3/19/14.
 */
public class DeviceBootReceiver extends BroadcastReceiver {

    public static final String TAG = DeviceBootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if( "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            ComponentName comp = new ComponentName(context.getPackageName(),
                    BluetoothService.class.getName());
            ComponentName service = context.startService(new Intent().setComponent(comp));
            if (null == service){
                // something really wrong here
                Log.e(TAG, "Could not start service " + comp.toString());
            }
        } else {
            Log.e(TAG, "Received unexpected intent " + intent.toString());
        }
    }

}
