package com.mobile.dts.utills;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor spEditor;
    Context context;
    private static final String FIRST_LAUNCH = "firstLaunch";
    private static final String FIRST_LAUNCHAPPS = "firstLaunchApps";
    int MODE = 0;
    private static final String PREFERENCE = "Javapapers";

    public PreferenceManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREFERENCE, MODE);
        spEditor = sharedPreferences.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        spEditor.putBoolean(FIRST_LAUNCH, isFirstTime);
        spEditor.commit();
    }
    public void setFirstTimeLaunchApps(boolean isFirstTime) {
        spEditor.putBoolean(FIRST_LAUNCHAPPS, isFirstTime);
        spEditor.commit();
    }

    public boolean FirstLaunch() {
        return sharedPreferences.getBoolean(FIRST_LAUNCH, true);
    }
    public boolean FirstLaunchApps() {
        return sharedPreferences.getBoolean(FIRST_LAUNCHAPPS, true);
    }

}
