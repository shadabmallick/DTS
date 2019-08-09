package com.mobile.dts.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mobile.dts.R;
import com.mobile.dts.utills.Constants;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.mobile.dts.utills.Constants.FROMIMAGEGALLERY;
import static com.mobile.dts.utills.Constants.ISGRANTROOTPERMISSIONSDCARD;
import static com.mobile.dts.utills.Constants.ISSDCARDEXISTS;
import static com.mobile.dts.utills.Constants.SDCARDROOTDIRECTORYURI;
import static com.mobile.dts.utills.Constants.SDCARDROOTDIRECTORYURIPATH;
import static com.mobile.dts.utills.Constants.appPref;

/*Used to show Home screen*/
public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_PERMISSIONS = 102;
    private RelativeLayout rl_gallery, rl_restore_photos, rl_settings, rl_talk_with_us;
    private int REQUEST_PERMISSIONS_OVERLAY = 1005;
    private SharedPreferences sharedpreferences;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getBoolean(FROMIMAGEGALLERY)) {
                super.onBackPressed();
            }
        }
        initViews();
        initClicks();

    }

    private void setScreenNameFirebaseAnalytics(String screenName) {
        mFirebaseAnalytics.setCurrentScreen(this, screenName, this.getClass().getSimpleName());
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
            if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(HomeActivity.this)) {
                showOverlayPermissionDialog();

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
                    sdCardPermissionNaugot();
                }

            } else {
                if (sdcardUri != null &&
                        DocumentFile.fromTreeUri(this, Uri.parse(sdcardUri)).canWrite()) {
                    String sdcardUriPath = sharedpreferences.getString(SDCARDROOTDIRECTORYURIPATH, null);
                    if (sdcardUriPath != null && sdcardUriPath.indexOf(":") + 1 == sdcardUriPath.length()) {
                    } else {
                        sdCardPermission();
                    }

                } else {
                    sdCardPermission();
                }

            }

        } catch (Exception e) {
            sdCardPermissionNaugot();
        }

    }

    /*Use to get SD card permission if android version Android 7.0 and above*/
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

    /*Use to get SD card permission if android version ANdroid 6.0 and below*/
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_PERMISSIONS_OVERLAY == requestCode) {
            if (android.os.Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(HomeActivity.this)) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor overlay = settings.edit();
                overlay.putBoolean("overlay", true);
                overlay.commit();
                SharedPreferences.Editor notification = settings.edit();
                notification.putBoolean("notification", false);
                notification.commit();
            }
        } else if (requestCode == 1 && data != null) {
            Uri grantedDocTreeUri = data.getData();
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(SDCARDROOTDIRECTORYURI, grantedDocTreeUri.toString());
            editor.putString(SDCARDROOTDIRECTORYURIPATH, grantedDocTreeUri.getPath());
            editor.commit();
            SharedPreferences.Editor editorsd = sharedpreferences.edit();
            editorsd.putBoolean(ISGRANTROOTPERMISSIONSDCARD, true);
            editorsd.commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (android.os.Build.VERSION.SDK_INT >= 23
                        && !Settings.canDrawOverlays(HomeActivity.this)) {
                    showOverlayPermissionDialog();
                }
                return;
            }
        }
    }

    /*Use to show dialog to get overlay permission*/
    public void showOverlayPermissionDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Overlay permission")
                .setMessage("Please take Screen Overlay permission to show new detected media on screen")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(HomeActivity.this)) {   //Android M Or Over
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, REQUEST_PERMISSIONS_OVERLAY);
                            return;
                        }
                    }
                })
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void initViews() {
        rl_gallery = findViewById(R.id.rl_gallery);
        rl_restore_photos = findViewById(R.id.rl_restore_photos);
        rl_settings = findViewById(R.id.rl_settings);
        rl_talk_with_us = findViewById(R.id.rl_talk_with_us);
    }

    private void initClicks() {
        rl_gallery.setOnClickListener(this);
        rl_restore_photos.setOnClickListener(this);
        rl_settings.setOnClickListener(this);
        rl_talk_with_us.setOnClickListener(this);
    }

    /*Use to navigate Dts Gallery screen*/
    public void moveDtsGallery() {
        startActivity(new Intent(this, DtsGalleryActivity.class));
        finish();
    }

    /*Use to navigate Restore Gallery screen*/
    public void moveRestoreImageGallery() {
        Intent intent = new Intent(this, DtsGalleryActivity.class);
        intent.putExtra(Constants.galleryType, Constants.delete);
        startActivity(intent);
        finish();
    }

    /*Use to navigate Setting screen*/
    private void moveToSettingScreen() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rl_gallery) {
            moveDtsGallery();
        } else if (v.getId() == R.id.rl_restore_photos) {
            moveRestoreImageGallery();
        } else if (v.getId() == R.id.rl_settings) {
            moveToSettingScreen();
        } else if (v.getId() == R.id.rl_talk_with_us) {
            startActivity(new Intent(this, TalkWithUsActivity.class));
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

}
