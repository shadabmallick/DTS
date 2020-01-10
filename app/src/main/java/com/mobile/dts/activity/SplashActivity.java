package com.mobile.dts.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mobile.dts.R;
import com.mobile.dts.database.DTSPreferences;
import com.mobile.dts.helper.Scheduler;
import com.mobile.dts.utills.Constants;
import com.mobile.dts.utills.Utils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import io.fabric.sdk.android.Fabric;

import static com.mobile.dts.utills.Constants.KEY_IS_LOGIN;
import static com.mobile.dts.utills.Constants.alarmRequestCode;
import static com.mobile.dts.utills.Constants.appPref;
import static com.mobile.dts.utills.Constants.defaultTimeset;
import static com.mobile.dts.utills.Constants.firstLaunchtime;
import static com.mobile.dts.utills.Constants.isBoot;
import static com.mobile.dts.utills.Constants.lastViewTime;

/*Uses to show Splash screen*/
public class SplashActivity extends AppCompatActivity {

    private SharedPreferences sharedpreferences, settingsPref;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);
        Fabric.with(this, new Crashlytics());
        addDynamicShortcuts();
        sharedpreferences = getSharedPreferences(appPref, Activity.MODE_PRIVATE);
        /*Set first launch time*/
        if (!sharedpreferences.contains(firstLaunchtime)) {
            Date currentTime = Calendar.getInstance().getTime();
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putLong(firstLaunchtime, currentTime.getTime());
            editor.commit();
        }
        /*Set recent view launch time*/
        if (!sharedpreferences.contains(lastViewTime)) {
            Date _currentTime = Calendar.getInstance().getTime();
            SharedPreferences.Editor _editor = sharedpreferences.edit();
            _editor.putLong(lastViewTime, _currentTime.getTime());
            _editor.commit();
        }

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(isBoot, false);
        editor.commit();
        /*Set scheduler */
        settingsPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String setTime = settingsPref.getString(getResources().getString(R.string.setTime), defaultTimeset);
        Intent intent = new Intent(SplashActivity.this, Scheduler.class);
        intent.setAction(Constants.ACTION_ALARM_RECEIVER);
        boolean isWorking = (PendingIntent.getBroadcast(SplashActivity.this,
                alarmRequestCode, intent, PendingIntent.FLAG_NO_CREATE) != null);
        if (!isWorking) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(SplashActivity.this, alarmRequestCode, intent, 0);
            AlarmManager am = (AlarmManager) SplashActivity.this.getSystemService(SplashActivity.this.ALARM_SERVICE);
            am.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance()
                    .getTime().getTime() + Long.parseLong(setTime) * 1000, pendingIntent);
        }
        /*Set selected language*/
        Utils.setLanguageFromLocale(SplashActivity.this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setScreenNameFirebaseAnalytics("Splash screen", null);
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void addDynamicShortcuts() {
        //get Shortcut manager
        ShortcutManager shortcutManager;

        //Add Compose Email Shortcut

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            shortcutManager =  getSystemService(ShortcutManager.class);
            Intent intent= new Intent(this, FingerScan.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("weburl"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "shortcutId")
                    .setShortLabel("dummy")
                    .setLongLabel("dummy").setRank(0)
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_launcher_background))
                    .setIntent(intent).build();

            shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut));
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            shortcutManager =  getSystemService(ShortcutManager.class);
            Intent intent= new Intent(this, FingerScan.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("weburl"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "shortcutId")
                    .setShortLabel("Recent Media")
                    .setLongLabel("dummy").setRank(1)
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_message))
                    .setIntent(intent).build();

            shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut));
        }
        //NOTE : You can add more shortcuts here according to your requirement


    }


    private void setScreenNameFirebaseAnalytics(String screenName, String className) {
        mFirebaseAnalytics.setCurrentScreen(this, screenName, className);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Log.d("TAG", "is login = "+DTSPreferences.SharedInstance(SplashActivity.this).getBoolean(KEY_IS_LOGIN));
                Log.d("TAG", "1st intro = "+DTSPreferences.SharedInstance(SplashActivity.this).FirstLaunch());
                Log.d("TAG", "2st intro = "+DTSPreferences.SharedInstance(SplashActivity.this).FirstLaunchApps());


                if (DTSPreferences.SharedInstance(SplashActivity.this).getBoolean(KEY_IS_LOGIN)) {

                    Intent intent = new Intent(SplashActivity.this, IntroductionAfterLogin.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    SplashActivity.this.startActivity(intent);

                    /*Intent intent = new Intent(SplashActivity.this, FingerScan.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    SplashActivity.this.startActivity(intent);*/

                } else {

                    if (DTSPreferences.SharedInstance(SplashActivity.this).FirstLaunch()) {

                        DTSPreferences.SharedInstance(SplashActivity.this).setFirstTimeLaunch(false);
                        startActivity(new Intent(SplashActivity.this, MainScreen.class));
                        finish();

                    } else {

                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }


                  /*  Intent intent = new Intent(SplashActivity.this, MainScreen.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    SplashActivity.this.startActivity(intent);*/


                }
                SplashActivity.this.finish();
            }
        }, 1000);
    }
}
