package com.vicmns.bluetoothglass.client.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;
import com.vicmns.bluetoothglass.client.activities.MainActivity;

/**
 * Created by Victor Cervantes on 3/20/14.
 */
public class GoogleVoiceTriggerService extends Service {
    private static final String LIVE_CARD_TAG = "itexico_picture";

    private TimelineManager mTimelineManager;
    private LiveCard mLiveCard;



    @Override
    public void onCreate() {
        mTimelineManager = TimelineManager.from(this);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_TAG);
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

        return START_STICKY;
    }
}
