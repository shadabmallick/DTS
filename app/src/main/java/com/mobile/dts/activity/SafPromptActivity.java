package com.mobile.dts.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.mobile.dts.utills.Constants;
import com.mobile.dts.utills.Utils;

import static android.content.Intent.ACTION_OPEN_DOCUMENT_TREE;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static com.mobile.dts.utills.Constants.SDCARDROOTDIRECTORYURI;
import static com.mobile.dts.utills.Constants.SDCARDROOTDIRECTORYURIPATH;

/*use to show SD card permission dialog*/
public class SafPromptActivity extends Activity {
    private static final int REQUEST_CODE_SAF = 1;

    private boolean granted = false;
    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedpreferences = getApplicationContext().getSharedPreferences(Constants.appPref, Activity.MODE_PRIVATE);
        new AlertDialog.Builder(this)
                .setTitle("Write access required")
                .setMessage("You need to grant SD card access to dts in order to complete operations. Please select SD card storage from the left sidebar and then tap the select button at the bottom. Make sure you’re in root of SD card storage")
                .setNeutralButton("Help", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.viewUri(SafPromptActivity.this, "http://pxhouse.co/saf", null);
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Grant access", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent safPickIntent = new Intent(ACTION_OPEN_DOCUMENT_TREE);
                        startActivityForResult(safPickIntent, REQUEST_CODE_SAF);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SAF) {
            if (resultCode == RESULT_OK && data.getData() != null) {
                Uri grantedDocTreeUri = data.getData();
                getContentResolver().takePersistableUriPermission(grantedDocTreeUri,
                        FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                granted = true;
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(SDCARDROOTDIRECTORYURI, grantedDocTreeUri.toString());
                editor.putString(SDCARDROOTDIRECTORYURIPATH, grantedDocTreeUri.getPath());
                editor.commit();
            } else {
                granted = false;
            }
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
