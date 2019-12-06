package com.mobile.dts.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent i = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName())
                    .putExtra(Settings.EXTRA_CHANNEL_ID, "com.mobile.keeptoo")
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

        } else if (Build.VERSION.SDK_INT == 24){


        }else if (Build.VERSION.SDK_INT == 25){
            Intent i = new Intent()
                    .setAction("android.settings.APP_NOTIFICATION_SETTINGS")
                    .putExtra("app_package", context.getPackageName())
                    .putExtra("app_uid", context.getApplicationInfo().uid)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

        }

    }
}