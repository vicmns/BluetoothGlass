package com.vicmns.bluetoothglass.server.handlers;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Victor Cervantes on 3/14/14.
 */
public class BluetoothReadFromSocketHandler extends AsyncTask<Void, Integer, Integer> {
    private static final String TAG = BluetoothReadFromSocketHandler.class.getSimpleName();
    private static final int CONNECTION_CLOSE = 0;
    private static final int bufferSize = 1024;
    private BluetoothSocket socket;

    private int totalBytesToReceieve;
    private int bytesProgress;

    private InputStream inputStream;
    private OutputStream outputStream;
    private ByteArrayOutputStream byteBuffer;

    private BluetoothSocketHandlerCallbacks callbacks;

    public BluetoothReadFromSocketHandler(BluetoothSocket socket,
                                          BluetoothSocketHandlerCallbacks callbacks) {
        this.socket = socket;
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            this.callbacks = callbacks;
        } catch (IOException e) {
            this.cancel(true);
            Log.e(TAG, "temp sockets not created", e);
        }
    }

    @Override
    protected void onPreExecute() {
        bytesProgress = 0;
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        Log.i(TAG, "BEGIN BluetoothReadFromSocketHandler");
        /*byte[] buffer = new byte[bufferSize];
        int bytes = -1;
        boolean isTransferCompleted = false;
        // Keep listening to the InputStream while connected
        while (socket.isConnected()) {
            try {
                while ((bytes = inputStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, bytes);
                    publishProgress(bytes);
                    isTransferCompleted = true;
                }

                Thread.sleep(1000);
            } catch (Exception e) {
                Log.e(TAG, "Disconnected", e);
                if(isTransferCompleted) {
                    bytes = -1;
                    publishProgress(bytes);
                }
            }
        }*/

        savePicture();
        callbacks.onTransferComplete();
        return CONNECTION_CLOSE;
    }

    @Override
    protected void onProgressUpdate(Integer... bytes) {
        callbacks.onTransferProgress((int) (((float) bytes[0] / totalBytesToReceieve) * 100.0));
        super.onProgressUpdate(bytes);
    }

    @Override
    protected void onPostExecute(Integer status) {
        switch (status) {
            case CONNECTION_CLOSE:
                callbacks.onConnectionLost();
                break;
        }
        super.onPostExecute(status);
    }

    private void savePicture() {
        File dcimDir = new File(Environment.getExternalStorageDirectory(), "DCIM");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        int read;
        boolean headerInfo = true;
        byte[] bytes = new byte[bufferSize];
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(new
                    File(dcimDir.getAbsolutePath(), "glass" + timeStamp + ".jpg"));
            while ((read = inputStream.read(bytes)) != -1) {
                if (headerInfo) {
                    parseHeader(bytes);
                    headerInfo = false;
                } else {
                    outputStream.write(bytes, 0, read);
                    this.onProgressUpdate(bytesProgress += read);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void parseHeader(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        totalBytesToReceieve = (int) bb.getLong();
        Log.i(TAG, "Bytes to receive: " + totalBytesToReceieve);
    }

    public interface BluetoothSocketHandlerCallbacks {
        public void onTransferProgress(int progress);

        public void onTransferComplete();

        public void onConnectionLost();
    }
}
