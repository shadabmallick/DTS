package com.mobile.dts.helper;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.mobile.dts.R;
import com.mobile.dts.utills.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.mobile.dts.utills.Constants.everyThirdDaytime;
import static com.mobile.dts.utills.Constants.isDisbledNotification;
import static com.mobile.dts.utills.Constants.isRealTimeNotification;

/*Use to show custom dialog for Notification settings*/
public class CustomDialogClass extends Dialog implements
        android.view.View.OnClickListener {

    public Activity activity;
    public TextView cancel;
    public CheckedTextView checkedTextView1, checkedTextView2, checkedTextView3;
    private SharedPreferences sharedpreferences;
    private boolean isRealtimeNotification;

    public CustomDialogClass(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customdialogui);
        sharedpreferences = getContext().getApplicationContext().getSharedPreferences(Constants.appPref, Activity.MODE_PRIVATE);
        checkedTextView1 = (CheckedTextView) findViewById(R.id.text1);
        checkedTextView2 = (CheckedTextView) findViewById(R.id.text2);
        checkedTextView3 = (CheckedTextView) findViewById(R.id.text3);
        cancel = (TextView) findViewById(R.id.cancel);
        checkedTextView1.setOnClickListener(this);
        checkedTextView2.setOnClickListener(this);
        checkedTextView3.setOnClickListener(this);
        cancel.setOnClickListener(this);
        boolean isDisbledNotification = sharedpreferences.getBoolean(Constants.isDisbledNotification, false);
        if (!isDisbledNotification) {
            isRealtimeNotification = sharedpreferences.getBoolean(Constants.isRealTimeNotification, false);
            if (isRealtimeNotification) {
                checkedTextView1.setChecked(true);
            } else {
                checkedTextView2.setChecked(true);
            }
        } else {
            checkedTextView3.setChecked(true);
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

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.cancel:
                break;
            case R.id.text3:
                try {
                    SharedPreferences.Editor editor2 = sharedpreferences.edit();
                    editor2.putBoolean(isDisbledNotification, true);
                    editor2.commit();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        dismiss();
    }
}