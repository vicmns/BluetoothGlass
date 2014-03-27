package com.vicmns.bluetoothglass.client.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.vicmns.bluetoothglass.client.bluetooth.SendFileToDeviceTask;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Victor Cervantes on 3/19/14.
 */
public class SendPictureToDevice extends Service implements
        SendFileToDeviceTask.ConnectToDeviceListeners {
    private static final String TAG = SendPictureToDevice.class.getSimpleName();
    public static final String DEVICE_MAC_ADDRESS = "device_mac_address";
    public static final String PICTURE_TO_SEND = "picture_to_send";
    private String deviceAddress, filePath;
    private BluetoothAdapter myBt;
    private BluetoothDevice dev;
    private Queue<File> picturesQueue;

    private SendFileToDeviceTask sendFileToDeviceTask;

    @Override
    public void onCreate() {
        if(picturesQueue == null)
            picturesQueue = new LinkedList<File>();

        if(myBt == null)
            myBt = BluetoothAdapter.getDefaultAdapter();

        Log.i(TAG, "Service Started");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();

        if(extras != null) {
            deviceAddress = extras.getString(DEVICE_MAC_ADDRESS);
            filePath = extras.getString(PICTURE_TO_SEND);

            picturesQueue.add(new File(filePath));

            if(myBt == null) return Service.START_NOT_STICKY;

            dev = myBt.getRemoteDevice(deviceAddress);

            startSendingProcess();
        }

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
    }

    private void startSendingProcess() {
        if(sendFileToDeviceTask != null) return;
        try {
            sendFileToDeviceTask = new SendFileToDeviceTask(this, dev, picturesQueue.peek());
            sendFileToDeviceTask.setConnectToDeviceListeners(this);
            sendFileToDeviceTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepareFileSending() {
        Log.i(TAG, "Sending File: " + picturesQueue.peek());
    }

    @Override
    public void onFileSent() {
        Log.i(TAG, "File Sent: " + picturesQueue.peek());
        sendFileToDeviceTask = null;
        if(picturesQueue.poll() == null)
            stopSelf();
        else
            startSendingProcess();
    }

    @Override
    public void onConnectionClosed() {
        if(picturesQueue.size() > 0)
            startSendingProcess();
        else
            stopSelf();
    }
}
