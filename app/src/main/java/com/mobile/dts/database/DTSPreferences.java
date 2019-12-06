package com.mobile.dts.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/*use to set share preference*/
public class DTSPreferences {
    static DTSPreferences instance;
    final SharedPreferences.Editor editor;
    final SharedPreferences prefs;
    private static final String FIRST_LAUNCH = "firstLaunch";
    private static final String FIRST_LAUNCHAPPS = "firstLaunchApps";
    private static final String FIRST_LAUNCHAPPSMain = "firstLaunchAppsMain";
    int MODE = 0;
    private static final String PREFERENCE = "Javapapers";
    @SuppressLint("CommitPrefEdits")
    public DTSPreferences(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = prefs.edit();
    }

    public static DTSPreferences SharedInstance(Context context) {
        if (instance == null) {
            instance = new DTSPreferences(context);
        }
        return instance;
    }

    public void addBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        return prefs.getBoolean(key, false);
    }
    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(FIRST_LAUNCH, isFirstTime);
        editor.commit();
    }
    public void setFirstTimeLaunchApps(boolean isFirstTime) {
        editor.putBoolean(FIRST_LAUNCHAPPS, isFirstTime);
        editor.commit();
    }
    public void setFirstTimeLaunchMain(boolean isFirstTime) {
        editor.putBoolean(FIRST_LAUNCHAPPSMain, isFirstTime);
        editor.commit();
    }

    public boolean FirstLaunch() {
        return prefs.getBoolean(FIRST_LAUNCH, true);
    }
    public boolean FirstLaunchApps() {
        return prefs.getBoolean(FIRST_LAUNCHAPPS, true);
    }
    public boolean FirstLaunchAppsMain() {
        return prefs.getBoolean(FIRST_LAUNCHAPPSMain, true);
    }
}
