package com.vicmns.bluetoothglass.client.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.glass.widget.CardScrollView;
import com.vicmns.bluetoothglass.client.R;
import com.vicmns.bluetoothglass.client.adapters.BluetoothDevicesAdapter;
import com.vicmns.bluetoothglass.client.bluetooth.SendFileToDeviceTask;
import com.vicmns.bluetoothglass.client.models.BluetoothDeviceModel;
import com.vicmns.bluetoothglass.client.services.SendPictureToDevice;
import com.vicmns.bluetoothglass.client.tools.FileExtensionFilter;
import com.vicmns.bluetoothglass.client.views.OverlayView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

/**
 * Created by Victor Cervantes on 3/14/14.
 */
public class BluetoothClient extends Activity implements AdapterView.OnItemClickListener,
        SendFileToDeviceTask.ConnectToDeviceListeners {

    public static final String TAG = BluetoothClient.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    private SendFileToDeviceTask sendFileToDeviceTask;
    private CardScrollView devicesScrollView;
    private String deviceName = "";
    private Context context;
    private OverlayView overlayView;
    private BluetoothAdapter myBt;
    private BluetoothDevicesAdapter bluetoothDevicesAdapter;
    private BluetoothDeviceModel bDevice;

    private SharedPreferences pref;
    private static final String SAVED_BT_DEVICE= "";

    private File selectedFile;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                if (device != null) {
                    deviceName = (device.getName() != null) ? device.getName() :
                            context.getString(R.string.unnamed_device_string);
                    bDevice = new BluetoothDeviceModel(deviceName, device.getAddress());
                    bluetoothDevicesAdapter.add(bDevice);
                }
                update();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_pairing);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        context = this;
        pref = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        overlayView = new OverlayView(this, findViewById(R.id.main_overlay_layout),
                OverlayView.LOADING);
        overlayView.showWithMessageLoading();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();

        // if this is from the share menu
        if (Intent.ACTION_SEND.equals(action) && extras != null &&
                extras.containsKey(Intent.EXTRA_STREAM)) {

            // Get resource path
            Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
            if(uri != null) {
                String filename = parseUriToFilename(uri);
                selectedFile = new File(uri.getPath());

            }
        }
        if(extras != null && extras.containsKey(MainActivity.SHARE_PICTURE)) {
            String filePath = extras.getString(MainActivity.SHARE_PICTURE);
            selectedFile = new File(filePath);
        }

        setBtDevicesScroll();
        initializeBluetooth();



    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        super.onResume();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mReceiver);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "Failed to enable Bluetooth", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Bluetooth Enabled", Toast.LENGTH_LONG).show();
                detectAndSetUp();
            }
        }
    }

    private void setBtDevicesScroll() {
        devicesScrollView = (CardScrollView) findViewById(R.id.pairing_devices_scroll_view);
        bluetoothDevicesAdapter = new BluetoothDevicesAdapter(this);
        devicesScrollView.setAdapter(bluetoothDevicesAdapter);
        devicesScrollView.setOnItemClickListener(this);
        devicesScrollView.activate();
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
            detectAndSetUp();
        }
    }

    private void detectAndSetUp() {
        String savedDevice = pref.getString(SAVED_BT_DEVICE, "");

        if(savedDevice.length() > 0) {
            BluetoothDevice dev = myBt.getRemoteDevice(savedDevice);
            if(dev != null) {
                callSendPictureService(savedDevice);
                finish();
                return;
            }
        }

        Set<BluetoothDevice> pairedDevices = myBt.getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceName = (device.getName() != null) ? device.getName() : "Unnamed device";
                bDevice = new BluetoothDeviceModel(deviceName, device.getAddress());
                bluetoothDevicesAdapter.add(bDevice);
            }
        }

        myBt.startDiscovery();
    }

    public void update() {
        bluetoothDevicesAdapter.notifyDataSetChanged();
        if (overlayView.isLoading())
            overlayView.hide();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.bluetooth, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        overlayView.showWithMessageLoading();

        if (sendFileToDeviceTask != null) {
            Log.e(TAG, "Canceling old connection, and starting new one.");
            sendFileToDeviceTask.cancel();
        }

        BluetoothDeviceModel bDevice = (BluetoothDeviceModel)
                bluetoothDevicesAdapter.getItem(position);
        Log.i(TAG, "Connecting to device: " + bDevice.getDeviceMACAddress());

        //BluetoothDevice dev = myBt.getRemoteDevice(bDevice.getDeviceMACAddress());
        if(selectedFile == null)
            selectedFile = sendTestPicture();

        /*sendFileToDeviceTask = new SendFileToDeviceTask(context, dev, selectedFile);
        sendFileToDeviceTask.setConnectToDeviceListeners(this);
        sendFileToDeviceTask.execute();*/



        pref.edit().putString(SAVED_BT_DEVICE, bDevice.getDeviceMACAddress()).commit();

        callSendPictureService(bDevice.getDeviceMACAddress());
        finish();
    }

    private void callSendPictureService(String deviceAddress) {
        if(!selectedFile.exists()) {
            selectedFile = sendTestPicture();
        }
        Intent intent = new Intent(this, SendPictureToDevice.class);
        intent.putExtra(SendPictureToDevice.PICTURE_TO_SEND, selectedFile.getAbsolutePath());
        intent.putExtra(SendPictureToDevice.DEVICE_MAC_ADDRESS, deviceAddress);
        startService(intent);
    }

    private File sendTestPicture() {
        File[] imagesFiles = null;
        File currentDir = new File(Environment.getExternalStorageDirectory(), "DCIM");
        if (currentDir.canRead()) {
            String nameAlbum = "Camera";
            File folder = new File(currentDir, nameAlbum);
            if (folder.exists()) {
                //Make an array type File  with the list of all files of each folder
                imagesFiles = folder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return FileExtensionFilter.isFileImage(name);
                    }
                });
                if (imagesFiles.length > 0) {
                    Arrays.sort(imagesFiles, new Comparator<Object>() {
                        public int compare(Object o1, Object o2) {
                            if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                                return -1;
                            } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                                return +1;
                            } else {
                                return 0;
                            }
                        }
                    });
                }
            }
        }
        if (imagesFiles != null && imagesFiles.length > 0)
            return imagesFiles[0];
        return new File("");
    }

    public String parseUriToFilename(Uri uri) {
        String selectedImagePath = null;
        String fileManagerPath = uri.getPath();

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);

        if (cursor != null) {
            // Here you will get a null pointer if cursor is null
            // This can be if you used OI file manager for picking the media
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            selectedImagePath = cursor.getString(column_index);
        }

        if (selectedImagePath != null) {
            return selectedImagePath;
        }
        else if (fileManagerPath != null) {
            return fileManagerPath;
        }
        return null;
    }

    @Override
    public void onPrepareFileSending() {
        myBt.cancelDiscovery();
    }

    @Override
    public void onFileSent() {
        overlayView.hide();
    }

    @Override
    public void onConnectionClosed() {
        myBt.startDiscovery();
    }

}
