package com.mobile.dts.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.dts.R;
import com.mobile.dts.helper.LocationWidget;

import static com.mobile.dts.helper.LocationWidget.isMoved;
import static com.mobile.dts.utills.Constants.appPref;
import static com.mobile.dts.utills.Constants.stopServiceBroadcast;
import static com.mobile.dts.utills.Constants.xCoordinate;
import static com.mobile.dts.utills.Constants.yCoordinate;

/*Use to set Widget location*/
public class WidgetLocationActivity extends AppCompatActivity {
    private Context context;
    private TextView updatebtn;
    private SharedPreferences sharedpreferences;
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = WidgetLocationActivity.this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_locationsetting);
        sharedpreferences = context.getSharedPreferences(appPref, Activity.MODE_PRIVATE);
        updatebtn = findViewById(R.id.updatebtn);
        updatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMoved) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putFloat(xCoordinate, LocationWidget._xCoordinate);
                    editor.putFloat(yCoordinate, LocationWidget._yCoordinate);
                    editor.commit();
                    Toast.makeText(WidgetLocationActivity.this, "Location updated successfully", Toast.LENGTH_SHORT).show();
                    finish();

                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*Start service to show widget to set location*/
        serviceIntent = new Intent(WidgetLocationActivity.this, LocationWidget.class);
        startService(serviceIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sendBroadcast(new Intent(stopServiceBroadcast));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent(stopServiceBroadcast));
    }

}
