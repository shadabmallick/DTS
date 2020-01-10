package com.mobile.dts.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.mobile.dts.R;
import com.mobile.dts.utills.Constants;
import com.mobile.dts.utills.Utils;


/*Use to Image viewer activity on Widget click*/
public class ImageViewLoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view_loading);
        Intent overlayIntent = new Intent(getApplicationContext(), ImageViewerActivity.class);
        overlayIntent.putExtra(Constants.KEY_IS_FROM_NOTIFICATION, true);
        overlayIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        overlayIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(overlayIntent);
        finish();
        Utils.setLanguageFromLocale(ImageViewLoadingActivity.this);
    }
}
