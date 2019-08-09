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

    @SuppressLint("CommitPrefEdits")
    private DTSPreferences(Context context) {
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
}
