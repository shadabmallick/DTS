package com.mobile.dts.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mobile.dts.R;


/*Used to show About us text*/
public class AboutUsActivity extends AppCompatActivity {
    private ImageView iv_back;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setScreenNameFirebaseAnalytics("About Us Screen");
    }

    private void setScreenNameFirebaseAnalytics(String screenName) {
        mFirebaseAnalytics.setCurrentScreen(this, screenName, this.getClass().getSimpleName());
    }


}
