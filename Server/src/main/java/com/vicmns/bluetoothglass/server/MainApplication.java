package com.vicmns.bluetoothglass.server;

import android.app.Application;

import com.vicmns.bluetoothglass.server.data.BluetoothParametersHolder;

/**
 * Created by Victor Cervantes on 3/14/14.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        new BluetoothParametersHolder();
        super.onCreate();
    }
}
