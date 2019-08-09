package com.mobile.dts.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mobile.dts.R;

/*Use to show talk with us screen*/
public class TalkWithUsActivity extends AppCompatActivity {

    private ImageButton icon_home;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_with_us);
        icon_home = findViewById(R.id.icon_home);
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
        Intent intent = new Intent(TalkWithUsActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

}
