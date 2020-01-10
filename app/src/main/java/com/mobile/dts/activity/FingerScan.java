package com.mobile.dts.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mobile.dts.R;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.mobile.dts.activity.CodeAgain.MY_PREFS_NAME;
import static com.mobile.dts.utills.Constants.ISGRANTROOTPERMISSIONSDCARD;
import static com.mobile.dts.utills.Constants.ISSDCARDEXISTS;
import static com.mobile.dts.utills.Constants.SDCARDROOTDIRECTORYURI;
import static com.mobile.dts.utills.Constants.SDCARDROOTDIRECTORYURIPATH;
import static com.mobile.dts.utills.Constants.appPref;


public class FingerScan extends AppCompatActivity {
    String TAG="FingerScan";
    Button button_enter4digit;
    ImageView img_back;
    String name;
    RelativeLayout rel_center;
    private static final int REQUEST_PERMISSIONS = 102;
    private RelativeLayout rl_gallery, rl_restore_photos, rl_settings, rl_talk_with_us;
    private int REQUEST_PERMISSIONS_OVERLAY = 1005;
    private SharedPreferences sharedpreferences;
    private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finger_scan);
        button_enter4digit=findViewById(R.id.button_enter4digit);
        img_back=findViewById(R.id.img_back);
        rel_center=findViewById(R.id.rel_center);
        sharedpreferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        name = sharedpreferences.getString("code", "No name defined");



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(),
                    R.color.title_color));
        }

        Log.d(TAG, "onCreate: "+name);//"No name defined" is the default value.

        button_enter4digit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FingerScan.this,
                        ActivityCode.class);
                intent.putExtra("code",name);
                startActivity(intent);

                Log.d(TAG, "code1 : "+name);
            }
        });
        rel_center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent code=new Intent(FingerScan.this,SafeFingerPrint.class);
                code.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                code.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(code);
            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE))) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(FingerScan.this)) {
                //showOverlayPermissionDialog();

            }
        }
        sharedpreferences = getApplicationContext().getSharedPreferences(appPref, Activity.MODE_PRIVATE);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setScreenNameFirebaseAnalytics("Home Screen");
        /*Use to get SD card permission*/
        String sdcardUri = sharedpreferences.getString(SDCARDROOTDIRECTORYURI, null);
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (sdcardUri != null &&
                        DocumentFile.fromTreeUri(this, Uri.parse(sdcardUri)).canWrite()) {
                } else {
                  //  sdCardPermissionNaugot();
                }

            } else {
                if (sdcardUri != null &&
                        DocumentFile.fromTreeUri(this, Uri.parse(sdcardUri)).canWrite()) {
                    String sdcardUriPath = sharedpreferences.getString(SDCARDROOTDIRECTORYURIPATH, null);
                    if (sdcardUriPath != null && sdcardUriPath.indexOf(":") + 1 == sdcardUriPath.length()) {
                    } else {
                       // sdCardPermission();
                    }

                } else {
                  //  sdCardPermission();
                }

            }

        } catch (Exception e) {
           // sdCardPermissionNaugot();
        }

    }
    private void setScreenNameFirebaseAnalytics(String screenName) {
        mFirebaseAnalytics.setCurrentScreen(this, screenName, this.getClass().getSimpleName());
    }
    private void sdCardPermission() {
        if (getStorageDirectories(this).length == 2) {
            String sdcardUriPath = sharedpreferences.getString(SDCARDROOTDIRECTORYURIPATH, null);
            if (sdcardUriPath != null && sdcardUriPath.indexOf(":") + 1 == sdcardUriPath.length()) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(ISGRANTROOTPERMISSIONSDCARD, true);
                editor.commit();
            } else {
                Intent safPromptIntent = new Intent(this, SafPromptActivity.class);
                safPromptIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(safPromptIntent);
            }
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(ISSDCARDEXISTS, true);
            editor.commit();
        } else {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(ISSDCARDEXISTS, false);
            editor.commit();
        }
    }
    public String[] getStorageDirectories(Context pContext) {
        // Final set of paths
        final Set<String> rv = new HashSet<>();
        //Get primary & secondary external device storage (internal storage & micro SDCARD slot...)
        File[] listExternalDirs = ContextCompat.getExternalFilesDirs(pContext, null);
        for (int i = 0; i < listExternalDirs.length; i++) {
            if (listExternalDirs[i] != null) {
                String path = listExternalDirs[i].getAbsolutePath();
                int indexMountRoot = path.indexOf("/Android/data/");
                if (indexMountRoot >= 0 && indexMountRoot <= path.length()) {
                    //Get the root path for the external directory
                    rv.add(path.substring(0, indexMountRoot));
                }
            }
        }
        return rv.toArray(new String[rv.size()]);
    }
    private void sdCardPermissionNaugot() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StorageManager sm = getSystemService(StorageManager.class);
            if (sm != null && sm.getStorageVolumes().size() == 2 && sm.getStorageVolumes().get(1).isRemovable()) {
                StorageVolume volume = sm.getStorageVolumes().get(1);
                Intent intent = volume.createAccessIntent(null);
                startActivityForResult(intent, 1);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(ISSDCARDEXISTS, true);
                editor.commit();

            } else {
                sdCardPermission();
            }

        } else {
            sdCardPermission();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
}
