package com.mobile.dts.activity;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.shashank.sony.fancytoastlib.FancyToast;

import static com.facebook.FacebookSdk.getApplicationContext;


public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private Context context;


    public FingerprintHandler(Context mContext) {
        context = mContext;
    }

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError(int errMsgId,
                                      CharSequence errString) {
        Toast.makeText(context,
                "Authentication error\n" + errString,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailed() {
        FancyToast.makeText(getApplicationContext(), "", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();

    }

    @Override
    public void onAuthenticationHelp(int helpMsgId,
                                     CharSequence helpString) {
        Toast.makeText(context,
                "Authentication help\n" + helpString,
                Toast.LENGTH_LONG).show();
    }


    @Override
    public void onAuthenticationSucceeded(
            FingerprintManager.AuthenticationResult result) {

        FancyToast.makeText(getApplicationContext(), "", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
        Intent codeagain = new Intent(getApplicationContext(), DtsGalleryActivity.class);
        codeagain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        codeagain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        codeagain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        context.startActivity(codeagain);
    }


}
