package com.mobile.dts.activity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.mobile.dts.model.ImageBean;
import com.mobile.dts.saveImageEvent.CameraBroadcastReceiver;
import com.mobile.dts.saveImageEvent.ImageJobService;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class MainApplication extends MultiDexApplication {
    public static String KEEPTODIRECTORYPATH;
    private SharedPreferences sharedpreferences;
    private ArrayList<String> imageBeanArrayList;
    private String isDelete;


    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        Fabric.with(this, new Crashlytics());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // use job scheduler
            ImageJobService.startJob(this, false);
        } else {
            // use broadcast receiver
            CameraBroadcastReceiver.register(this);
        }
        KEEPTODIRECTORYPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.dir/";
    }



}


