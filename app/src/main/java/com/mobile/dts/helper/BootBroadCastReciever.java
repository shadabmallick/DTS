package com.mobile.dts.helper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mobile.dts.utills.Constants;

import static com.mobile.dts.utills.Constants.isBoot;

/*Broad cast receiver to get Boot event*/
public class BootBroadCastReciever extends BroadcastReceiver {
    private SharedPreferences sharedpreferences, settingsPref;

    @Override
    public void onReceive(final Context context, Intent _intent) {
        sharedpreferences = context.getApplicationContext()
                .getSharedPreferences(Constants.appPref, Activity.MODE_PRIVATE);
        settingsPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isAutoLauchEnable = settingsPref.getBoolean("autolaunch", true);
        if (!isAutoLauchEnable) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(isBoot, true);
            editor.commit();
        }
    }
}
