package com.mobile.dts.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mobile.dts.R;

/*Use to show talk with us screen*/
public class TalkWithUsActivity extends AppCompatActivity {

    private ImageView icon_home;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_with_us);
        icon_home = findViewById(R.id.img_back);
        icon_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeScreen();
            }
        });
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setScreenNameFirebaseAnalytics("Talk with Us Screen", null);
    }


    private void setScreenNameFirebaseAnalytics(String screenName, String className) {
        mFirebaseAnalytics.setCurrentScreen(this, screenName, className);
    }

    @Override
    public void onBackPressed() {
        homeScreen();
    }

    private void homeScreen() {

        finish();
    }

}
