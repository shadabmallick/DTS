package com.mobile.dts.utills;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.mobile.dts.model.UserBean;


public class GooglePlusLogin implements GoogleApiClient.OnConnectionFailedListener {

    public static final int RC_SIGN_IN = 9001;
    String TAG = "Google Plus";
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private OnClientConnectedListener listener;

    public GooglePlusLogin(FragmentActivity context, OnClientConnectedListener listener) {
        this.context = context;
        this.listener = listener;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(context, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    public void onStart() {
        mGoogleApiClient.connect();
        OptionalPendingResult<GoogleSignInResult> opr =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN && mGoogleApiClient.isConnected()) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            signIn();
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            String imageUrl = "";
            String email = "";
            if (acct.getPhotoUrl() != null) {
                imageUrl = acct.getPhotoUrl().toString();

            }
            if (acct.getEmail() != null) {
                email = acct.getEmail();
            }
            if (acct.getDisplayName() != null) {
                String fullname = acct.getDisplayName();
                if (fullname.contains(" ")) {
                    String n[] = fullname.split(" ");
                    UserBean.getObect().userName = n[0];
                    UserBean.getObect().lastName = n[1];
                } else {
                    UserBean.getObect().userName = fullname;
                }
            }
            UserBean.getObect().uniqueId = acct.getId();
            UserBean.getObect().emailId = email;
            UserBean.getObect().accountType = "2";
            UserBean.getObect().picUrl = imageUrl;
            listener.onGoogleProfileFetchComplete();
        } else {
            listener.onClientFailed();
            signOut();
        }
    }

    public void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                    }
                });
    }

    private void revokeAccess() {
        try {
            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                            ((Activity) context).startActivityForResult(signInIntent, RC_SIGN_IN);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void signIn() {
        revokeAccess();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        ((OnClientConnectedListener) context).onClientFailed();
    }

    public interface OnClientConnectedListener {
        void onGoogleProfileFetchComplete();

        void onClientFailed();
    }
}
