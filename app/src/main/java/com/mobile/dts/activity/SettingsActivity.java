package com.mobile.dts.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mobile.dts.R;
import com.mobile.dts.helper.CustomDialogClass;

import java.util.Locale;

/*Use to show Setting screen*/
public class SettingsActivity extends AppCompatActivity {

    private static final int OVERLAY_PERMISSION_REQ_CODE = 103;
    private ImageView icon_home,tell_friend;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        icon_home = findViewById(R.id.icon_home);
        tell_friend = findViewById(R.id.tell_friend);
        icon_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeScreen();
            }
        });
/*
        tell_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, TellFriend.class);
                startActivity(intent);
                finish();
            }
        });
*/
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setScreenNameFirebaseAnalytics("Settings Screen", null);
    }

    private void setScreenNameFirebaseAnalytics(String screenName, String className) {
        mFirebaseAnalytics.setCurrentScreen(this, screenName, className /* class override */);
    }

    private void homeScreen() {
        Intent intent = new Intent(SettingsActivity.this, DtsGalleryActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        homeScreen();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        Preference notification;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.activity_settings);
            Preference scheduling = findPreference("scheduling");
            scheduling.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (!Settings.canDrawOverlays(getContext())) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getActivity().getPackageName()));
                        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                    }
                    return false;
                }
            });
            notification =  findPreference("notification");
            notification.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {


                @Override
                public boolean onPreferenceClick(Preference preference) {
                    CustomDialogClass cdd = new CustomDialogClass(getActivity());
                    cdd.show();
                    return false;
                }
            });
            Preference widgetlocation = findPreference("widgetlocation");
            widgetlocation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {


                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), WidgetLocationActivity.class);
                    startActivity(intent);
                    return false;
                }
            });
            Preference aboutus = findPreference("aboutus");
            aboutus.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {


                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), AboutUsActivity.class);
                    startActivity(intent);
                    return false;
                }
            });
            Preference widgetsize = findPreference("widgetsize");
            widgetsize.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {


                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), WidgetSizeDialog.class);
                    startActivity(intent);
                    return false;
                }
            });

            Preference talk_with_us = findPreference("talk_with_us");
            talk_with_us.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {


                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), TalkWithUsActivity.class);
                    startActivity(intent);
                    return false;
                }
            });

            Preference help_support = findPreference("help_support");
            help_support.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {


                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), HelpSupport.class);
                    startActivity(intent);
                    return false;
                }
            });


            Preference safe = findPreference("safe");
            safe.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {


                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), SafeSetting.class);
                    startActivity(intent);
                    return false;
                }
            });
            Preference language = findPreference(getString(R.string.setLanguage));
            language.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        setUserSelectedLanguage((String) newValue);
                    } catch (Exception e) {
                    }
                    return true;
                }
            });
        }

        /*Handle lauguage selection process*/
        private void setUserSelectedLanguage(String code) {
            if (code.equals("en")) {
                String languageToLoad = "en";
                Locale myLocale = new Locale(languageToLoad);
                Locale.setDefault(myLocale);
                android.content.res.Configuration config = new android.content.res.Configuration();
                config.locale = myLocale;
                getActivity().getBaseContext().getResources().updateConfiguration(config,
                        getActivity().getBaseContext().getResources().getDisplayMetrics());
                Intent intent = getActivity().getIntent();
                getActivity().overridePendingTransition(0, 0);
                getActivity().finish();
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(0, 0);

            } else if (code.equals("iw")) {
                String languageToLoad = "iw";
                Locale myLocale = new Locale(languageToLoad);
                Locale.setDefault(myLocale);
                android.content.res.Configuration config = new android.content.res.Configuration();
                config.locale = myLocale;
                getActivity().getBaseContext().getResources().updateConfiguration(config,
                        getActivity().getBaseContext().getResources().getDisplayMetrics());
                Intent intent = getActivity().getIntent();
                getActivity().overridePendingTransition(0, 0);
                getActivity().finish();
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(0, 0);
            }
        }
    }
}

