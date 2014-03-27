package com.vicmns.bluetoothglass.client.models;

/**
 * Created by Victor Cervantes on 3/14/14.
 */
public class BluetoothDeviceModel {
    String deviceName;
    String deviceMACAddress;

    public BluetoothDeviceModel(String deviceName, String deviceMACAddress) {
        this.deviceName = deviceName;
        this.deviceMACAddress = deviceMACAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceMACAddress() {
        return deviceMACAddress;
    }
}
