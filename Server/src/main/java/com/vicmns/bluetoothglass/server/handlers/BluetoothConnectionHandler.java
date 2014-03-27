package com.vicmns.bluetoothglass.server.handlers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import com.vicmns.bluetoothglass.server.data.BluetoothParametersHolder;

import java.io.IOException;

/**
 * Created by Victor Cervantes on 3/14/14.
 */
public class BluetoothConnectionHandler extends AsyncTask<Void, Void, BluetoothSocket> {
    private static final String TAG = BluetoothConnectionHandler.class.getSimpleName();

    private BluetoothServerSocket mmServerSocket;
    private BluetoothConnectionHandlerCallbacks callbacks;

    public BluetoothConnectionHandler(BluetoothAdapter bluetoothAdapter,
                                      BluetoothConnectionHandlerCallbacks callbacks) {
        try {
            mmServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                    BluetoothParametersHolder.NAME, BluetoothParametersHolder.uuids[0]);
            this.callbacks = callbacks;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected BluetoothSocket doInBackground(Void... voids) {
        Log.e(TAG, "Bluetooth on listening mode");
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
                if (socket != null)
                    mmServerSocket.close();
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return socket;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(BluetoothSocket socket) {
        if(callbacks != null)
            callbacks.onConnectionSuccessful(socket);
        super.onPostExecute(socket);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(BluetoothSocket socket) {
        super.onCancelled(socket);
    }

    @Override
    protected void onCancelled() {
        try {
            mmServerSocket.close();
            if(callbacks != null)
                callbacks.onConnectionCancel();
        } catch (IOException e) {
        }
        super.onCancelled();
    }


    public interface BluetoothConnectionHandlerCallbacks {
        public void onConnectionSuccessful(BluetoothSocket socket);
        public void onConnectionCancel();
    }
}
