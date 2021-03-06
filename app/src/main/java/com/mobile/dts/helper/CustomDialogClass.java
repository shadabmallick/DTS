package com.mobile.dts.helper;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.dts.R;
import com.mobile.dts.utills.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.mobile.dts.activity.DtsGalleryActivity.TAG;
import static com.mobile.dts.utills.Constants.everyThirdDaytime;
import static com.mobile.dts.utills.Constants.isDisbledNotification;
import static com.mobile.dts.utills.Constants.isRealTimeNotification;

/*Use to show custom dialog for Notification settings*/
public class CustomDialogClass extends Dialog implements
        android.view.View.OnClickListener {

    public Activity activity;
    public TextView cancel,update;
    public CheckedTextView checkedTextView1, checkedTextView2, checkedTextView3;
    private SharedPreferences sharedpreferences;
    private boolean isRealtimeNotification;

    public CustomDialogClass(Activity activity) {
        super(activity, R.style.MySettingsStyle);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customdialogui);
        setCancelable(false);
        sharedpreferences = getContext().getApplicationContext().getSharedPreferences(Constants.appPref, Activity.MODE_PRIVATE);
        checkedTextView1 =  findViewById(R.id.text1);
        checkedTextView2 =  findViewById(R.id.text2);
        checkedTextView3 =  findViewById(R.id.text3);
        cancel =  findViewById(R.id.cancel);
        update =  findViewById(R.id.update);
        checkedTextView1.setOnClickListener(this);
        checkedTextView2.setOnClickListener(this);
        checkedTextView3.setOnClickListener(this);
        cancel.setOnClickListener(this);
        update.setOnClickListener(this);


        boolean isDisbledNotification = sharedpreferences.getBoolean(Constants.isDisbledNotification, false);
        Log.d(TAG, "onCreate: "+isDisbledNotification);
        Log.d(TAG, "onCreate: "+isRealtimeNotification);
        if (!isDisbledNotification) {
            isRealtimeNotification = sharedpreferences.getBoolean(Constants.isRealTimeNotification, false);
            if (isRealtimeNotification) {
                checkedTextView1.setChecked(true);
                Log.d(TAG, "onCreate1: "+isDisbledNotification);
                Log.d(TAG, "onCreate1: "+isRealtimeNotification);
            } else {
                checkedTextView2.setChecked(true);
                Log.d(TAG, "onCreate2: "+isDisbledNotification);
                Log.d(TAG, "onCreate2: "+isRealtimeNotification);
            }
        } else {
            checkedTextView3.setChecked(true);
            Log.d(TAG, "onCreate3: "+isDisbledNotification);
            Log.d(TAG, "onCreate3: "+isRealtimeNotification);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text1:
                SharedPreferences.Editor editor1 = sharedpreferences.edit();
                editor1.putBoolean(isRealTimeNotification, true);
                editor1.commit();
                SharedPreferences.Editor editor4 = sharedpreferences.edit();
                editor4.putBoolean(isDisbledNotification, false);
                editor4.commit();

                checkedTextView1.setChecked(true);
                checkedTextView2.setChecked(false);
                checkedTextView3.setChecked(false);

                Log.d(TAG, "onClick: "+isDisbledNotification);
                Log.d(TAG, "onClick: "+isRealtimeNotification);
                break;
            case R.id.text2:
                try {
                    SharedPreferences.Editor editor2 = sharedpreferences.edit();
                    editor2.putBoolean(isRealTimeNotification, false);
                    editor2.commit();
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    Date today = new Date();
                    Date todayWithZeroTime = formatter.parse(formatter.format(today));
                    long timeaveryThirdDay = todayWithZeroTime.getTime() + 2 * (24 * 60 * 60 * 1000);
                    SharedPreferences.Editor editor3 = sharedpreferences.edit();
                    editor3.putLong(everyThirdDaytime, timeaveryThirdDay);
                    editor3.commit();
                    SharedPreferences.Editor editor5 = sharedpreferences.edit();
                    editor5.putBoolean(isDisbledNotification, false);
                    editor5.commit();

                    checkedTextView1.setChecked(false);
                    checkedTextView2.setChecked(true);
                    checkedTextView3.setChecked(false);

                    Log.d(TAG, "onClick: "+isDisbledNotification);
                    Log.d(TAG, "onClick: "+isRealtimeNotification);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.cancel:

                dismiss();

                break;

            case R.id.update:

                Toast.makeText(getContext(),"Update",Toast.LENGTH_SHORT).show();

                dismiss();

                break;

            case R.id.text3:
                try {
                    SharedPreferences.Editor editor2 = sharedpreferences.edit();
                    editor2.putBoolean(isDisbledNotification, true);
                    editor2.commit();
                    Log.d(TAG, "onClick: "+isDisbledNotification);
                    Log.d(TAG, "onClick: "+isRealtimeNotification);

                    checkedTextView1.setChecked(false);
                    checkedTextView2.setChecked(false);
                    checkedTextView3.setChecked(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}