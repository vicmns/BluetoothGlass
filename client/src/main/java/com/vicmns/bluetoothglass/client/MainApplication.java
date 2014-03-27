package com.vicmns.bluetoothglass.client;

import android.app.Application;

import com.vicmns.bluetoothglass.client.data.BluetoothParametersHolder;

/**
 * Created by Victor Cervantes on 3/19/14.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        new BluetoothParametersHolder();
        super.onCreate();
    }
}
