package com.vicmns.bluetoothglass.client.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.vicmns.bluetoothglass.client.callbacks.CardScrollCallBacks;
import com.vicmns.bluetoothglass.client.models.BluetoothDeviceModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor Cervantes on 3/14/14.
 */
public class BluetoothDevicesAdapter extends CardScrollAdapter {
    private Context ctx;
    private List<Card> mCards;
    private List<BluetoothDeviceModel> mDevices;
    private Card card;
    private CardScrollCallBacks cardScrollCallBacks;

    public BluetoothDevicesAdapter(Context ctx) {
        this.ctx = ctx;
        mCards = new ArrayList<Card>();
        mDevices = new ArrayList<BluetoothDeviceModel>();
    }

    public void setCardScrollCallBacks(CardScrollCallBacks cardScrollCallBacks) {
        this.cardScrollCallBacks = cardScrollCallBacks;
    }

    public void add(BluetoothDeviceModel bDevice) {
        for(BluetoothDeviceModel cDevice: mDevices) {
            if(cDevice.getDeviceMACAddress().equals(bDevice.getDeviceMACAddress()))
                return;
        }

        mDevices.add(bDevice);
        mCards.add(createDeviceCard(bDevice));
    }

    private Card createDeviceCard(BluetoothDeviceModel bDevice) {
        card = new Card(ctx);
        card.setText(bDevice.getDeviceName());
        card.setFootnote(bDevice.getDeviceMACAddress());
        return card;
    }

    @Override
    public int findIdPosition(Object arg0) {
        return 0;
    }

    @Override
    public int findItemPosition(Object item) {
        return mDevices.indexOf(item);
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevices.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mCards.get(position).toView();
    }
}
