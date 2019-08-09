package com.mobile.dts.saveImageEvent;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class RestartProcessService extends Service {
    private SharedPreferences settingsPref;

    public RestartProcessService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        settingsPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // use job scheduler
            ImageJobService.startJob(this, false);
        } else {
            // use broadcast receiver
            CameraBroadcastReceiver.register(this);
        }
        return START_STICKY;
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        settingsPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // use job scheduler
            ImageJobService.startJob(this, false);
        } else {
            // use broadcast receiver
            CameraBroadcastReceiver.register(this);
        }
    }
}
