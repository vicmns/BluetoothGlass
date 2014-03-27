package com.vicmns.bluetoothglass.client.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.vicmns.bluetoothglass.client.data.BluetoothParametersHolder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by Victor Cervantes on 3/19/14.
 */
public class SendFileToDeviceTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = SendFileToDeviceTask.class.getSimpleName();
    private BluetoothDevice device;
    private BluetoothSocket bluetoothSocket;

    private InputStream mmInStream;
    private OutputStream mmOutStream;

    private ConnectToDeviceListeners listeners;

    private Context context;
    private File file;

    public SendFileToDeviceTask(Context context,
                                BluetoothDevice device, File file) {
        this.context = context;
        this.device = device;
        this.file = file;

        try {
            bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(
                    BluetoothParametersHolder.uuids[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setConnectToDeviceListeners(ConnectToDeviceListeners listeners) {
        this.listeners = listeners;
    }

    @Override
    protected void onPreExecute() {
        if(listeners != null)
            listeners.onPrepareFileSending();

        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.e(TAG, "stopping discovery");
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            Log.e(TAG, "connecting!");
            bluetoothSocket.connect();
        } catch (IOException connectException) {
            Log.e(TAG, "failed to connect");
            // Unable to connect; close the socket and get out
            try {
                Log.e(TAG, "close-ah-da-socket");
                bluetoothSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "failed to close hte socket");
            }
            Log.e(TAG, "returning..");

            connectException.printStackTrace();

            return null;
        }

        Log.e(TAG, "we can now manage our connection!");

        // Do work to manage the connection (in a separate thread)
        Log.i(TAG, "Connection acceptation!");

        try {
            sendFile(Uri.fromFile(file), file.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(listeners != null)
            listeners.onFileSent();
        cancel();
        super.onPostExecute(aVoid);
    }

    @Override
    protected void onCancelled() {
        cancel();
        super.onCancelled();
    }

    @Override
    protected void onCancelled(Void aVoid) {
        cancel();
        super.onCancelled(aVoid);
    }

    public void cancel() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
        }
        if(listeners != null)
            listeners.onConnectionClosed();
    }

    private void sendFile(Uri uri, long size) throws IOException {
        BufferedInputStream bis = new
                BufferedInputStream(context.getContentResolver().openInputStream(uri));
        try {
            mmInStream = bluetoothSocket.getInputStream();
            mmOutStream = bluetoothSocket.getOutputStream();

            int bufferSize = 1024;
            ByteBuffer bb = ByteBuffer.allocate(bufferSize);
            byte[] buffer = new byte[bufferSize];

            // we need to know how may bytes were read to write them to the byteBuffer
            int len = 0;
            //Send Header info
            bb.asLongBuffer().put(size);
            mmOutStream.write(bb.array(), 0, bufferSize);
            while ((len = bis.read(buffer)) != -1) {
                mmOutStream.write(buffer, 0, len);
            }
        } finally {
            bis.close();
        }
    }

    public interface ConnectToDeviceListeners {
        public void onPrepareFileSending();
        public void onFileSent();
        public void onConnectionClosed();
    }
}

