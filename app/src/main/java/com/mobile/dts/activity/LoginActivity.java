package com.mobile.dts.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mobile.dts.BuildConfig;
import com.mobile.dts.R;
import com.mobile.dts.database.DTSPreferences;
import com.mobile.dts.dialogs.AlertDialogMessage;
import com.mobile.dts.model.OnFacebookLoginListener;
import com.mobile.dts.model.UserBean;
import com.mobile.dts.utills.FacebookManager;
import com.mobile.dts.utills.GooglePlusLogin;

import static com.mobile.dts.utills.Constants.KEY_IS_LOGIN;


/*Use to show login screen*/
public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GooglePlusLogin.OnClientConnectedListener, OnFacebookLoginListener {

    private ImageView loginGoogle, login_fb;
    private GooglePlusLogin mGooglePlusLogin;
    private FacebookManager facebookManager;
    private DTSPreferences dtsPreferences;
    private TextView tv_terms;
    private CheckBox cb_terms;
    private AlertDialogMessage alertDialogMessage;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        initViews();
        initObjects();

        initClickListner();
        setScreenNameFirebaseAnalytics("Login Screen", null);


        /// for debugg
        if (BuildConfig.DEBUG){
            dtsPreferences.addBoolean(KEY_IS_LOGIN, true);
            startHomescreen();
        }

    }


    private void initViews() {
        loginGoogle = findViewById(R.id.ll_LoginGoogle);
        login_fb = findViewById(R.id.ll_login_fb);
        tv_terms = findViewById(R.id.tv_terms);
        cb_terms = findViewById(R.id.cb_terms);
        alertDialogMessage = new AlertDialogMessage(this, getResources().getString(R.string.term_condition_warning));
    }

    private void initClickListner() {
        loginGoogle.setOnClickListener(this);
        login_fb.setOnClickListener(this);
        tv_terms.setOnClickListener(this);
    }

    private void initObjects() {
        mGooglePlusLogin = new GooglePlusLogin(this, this);
        facebookManager = new FacebookManager(this);
        facebookManager.setOnLoginListener(this);
        dtsPreferences = DTSPreferences.SharedInstance(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    private void setScreenNameFirebaseAnalytics(String screenName, String className) {
        mFirebaseAnalytics.setCurrentScreen(this, screenName, className /* class override */);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_LoginGoogle) {
            if (isTermChecked()) {
                mGooglePlusLogin.signIn();

            }
        } else if (v.getId() == R.id.ll_login_fb) {
            if (isTermChecked()) {
                facebookManager.doLogin();

            }
        } else if (v.getId() == R.id.tv_terms) {
            startActivity(new Intent(this, TermPageActivity.class));

        }
    }

    private boolean isTermChecked() {
        if (!cb_terms.isChecked()) {
            alertDialogMessage.setDialogMessage(getResources().getString(R.string.term_condition_warning));
            alertDialogMessage.show();
            return false;
        }
        return true;
    }

    @Override
    public void onGoogleProfileFetchComplete() {
        UserBean userBean = UserBean.getObect();
        dtsPreferences.addBoolean(KEY_IS_LOGIN, true);
        startHomescreen();
        mGooglePlusLogin.signOut();
    }

    private void startHomescreen() {
        Intent code=new Intent(this, IntroductionAfterLogin.class);
        code.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        code.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(code);
        this.finish();
    }

    @Override
    public void onClientFailed() {
    }

    @Override
    public void onFacebookLogin() {
        UserBean userBean = UserBean.getObect();
        dtsPreferences.addBoolean(KEY_IS_LOGIN, true);
        startHomescreen();
        facebookManager.doLogout();
    }

    @Override
    public void onError(String message) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GooglePlusLogin.RC_SIGN_IN) {
            mGooglePlusLogin.onActivityResult(requestCode, resultCode, data);
        } else if(FacebookSdk.isFacebookRequestCode(requestCode)) {
            facebookManager.onActivityResult(requestCode, resultCode, data);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
