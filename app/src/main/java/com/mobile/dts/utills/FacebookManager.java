package com.mobile.dts.utills;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.mobile.dts.model.OnFacebookLoginListener;
import com.mobile.dts.model.UserBean;

import org.json.JSONObject;

import java.util.Arrays;

public class FacebookManager implements FacebookCallback<LoginResult> {

    private CallbackManager callbackManager;
    private LoginManager loginManager;
    private Activity activity;
    private OnFacebookLoginListener listener;


    public FacebookManager(Activity activity) {
        FacebookSdk.sdkInitialize(activity.getApplicationContext());
        this.activity = activity;
        loginManager = LoginManager.getInstance();
        callbackManager = CallbackManager.Factory.create();

        if (loginManager != null) {
            loginManager.registerCallback(callbackManager, this);
        }

    }

    public void doLogin() {
        if (loginManager != null) {
            loginManager.logInWithReadPermissions(activity, Arrays.asList("public_profile", "email"));
        } else {
            if (listener != null) {
                listener.onError("Login manager is not initialized");
            }
        }
    }

    public void doLogout() {
        if (loginManager != null) {
            loginManager.logOut();
        } else {
            if (listener != null) {
                listener.onError("You must login first");
            }
        }
    }

    public void setOnLoginListener(OnFacebookLoginListener listener) {
        this.listener = listener;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSuccess(LoginResult result) {
        fetchProfileDetail(result.getAccessToken());

    }

    public void fetchProfileDetail(final AccessToken fbAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(fbAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    final UserBean userBean = UserBean.getObect();
                    JSONObject jsonObject = response.getJSONObject();
                    userBean.userName = jsonObject.optString("first_name");
                    userBean.lastName = jsonObject.optString("last_name");
                    userBean.uniqueId = jsonObject.optString("id");
                    userBean.emailId = jsonObject.optString("email");
                    userBean.accountType = "1";
                    if (jsonObject.has("picture")) {
                        JSONObject picture = jsonObject.getJSONObject("picture");
                        JSONObject picData = picture.getJSONObject("data");
                        userBean.picUrl = picData.optString("url");
                    }
                    if (listener != null) {
                        listener.onFacebookLogin();
                    } else {
                        listener.onError("Error Occured");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onError(e.getLocalizedMessage());
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,email,name,first_name,last_name,picture.height(300).width(300)");
        request.setParameters(parameters);
        request.executeAsync();
    }


    @Override
    public void onCancel() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onError("Login canceled");
            }
        });
    }

    @Override
    public void onError(final FacebookException error) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                error.printStackTrace();
            }
        });
    }


}
