package com.mobile.dts.helper;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.dts.R;
import com.mobile.dts.utills.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.mobile.dts.utills.Constants.everyThirdDaytime;
import static com.mobile.dts.utills.Constants.isDisbledNotification;
import static com.mobile.dts.utills.Constants.isRealTimeNotification;

/*Use to show custom dialog for Notification settings*/
public class CustomDialogMethod extends Dialog implements
        View.OnClickListener {

    public Activity activity;
    public TextView cancel,update;
    public CheckedTextView checkedTextView1, checkedTextView2, checkedTextView3;
    private SharedPreferences sharedpreferences,settingsPref;
    private boolean isRealtimeNotification;

    public CustomDialogMethod(Activity activity) {
        super(activity, R.style.MySettingsStyle);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.method);
      //  sharedpreferences = getContext().getApplicationContext().getSharedPreferences(Constants.appPref, Activity.MODE_PRIVATE);
        checkedTextView1 = (CheckedTextView) findViewById(R.id.text1);
        checkedTextView2 = (CheckedTextView) findViewById(R.id.text2);
     //   checkedTextView3 = (CheckedTextView) findViewById(R.id.text3);
        cancel = (TextView) findViewById(R.id.cancel);
        update = (TextView) findViewById(R.id.update);
        checkedTextView1.setOnClickListener(this);
        checkedTextView2.setOnClickListener(this);

    //    checkedTextView3.setOnClickListener(this);
        settingsPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        cancel.setOnClickListener(this);
        update.setOnClickListener(this);
     String test=   settingsPref.getString("method","");
        boolean isDisbledNotification = settingsPref.getBoolean(Constants.isDisbledNotification, false);
        if (test.equals("1008")) {
          //  isRealtimeNotification = settingsPref.getBoolean(Constants.isRealTimeNotification, false);

                checkedTextView1.setChecked(true);
                checkedTextView2.setChecked(false);
            } else {
                checkedTextView2.setChecked(true);
                checkedTextView1.setChecked(false);
            }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text1:
                SharedPreferences.Editor editor1 = settingsPref.edit();
                editor1.putString("method","1008");
                editor1.commit();
                checkedTextView1.setChecked(true);
                checkedTextView2.setChecked(false);

                break;
            case R.id.text2:
                try {
                    SharedPreferences.Editor editor2 = settingsPref.edit();

                     editor2.putString("method","1009");
                     editor2.commit();
                    checkedTextView1.setChecked(false);
                    checkedTextView2.setChecked(true);
                     break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.cancel:
                dismiss();
                break;


                case R.id.update:
dismiss();
Toast.makeText(getContext(),"Update",Toast.LENGTH_SHORT).show();

                    break;


            default:
                break;
        }
        //dismiss();
    }
}