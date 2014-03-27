package com.vicmns.bluetoothglass.client.data;

import java.util.UUID;

/**
 * Created by Victor Cervantes on 3/14/14.
 */
public class BluetoothParametersHolder {
    public static final int STATE_CONNECTION_STARTED = 0;
    public static final int STATE_CONNECTION_LOST = 1;
    public static final int READY_TO_CONN = 2;
    public static final int MESSAGE_READ = 3;

    public static final String NAME = "G6BITCHES";
    public static final  UUID[] uuids = new UUID[2];

    private String uuid1 = "05f2934c-1e81-4554-bb08-44aa761afbfb";
    private String uuid2 = "c2911cd0-5c3c-11e3-949a-0800200c9a66";

    public BluetoothParametersHolder() {
        uuids[0] = UUID.fromString(uuid1);
        uuids[1] = UUID.fromString(uuid2);
    }
}
