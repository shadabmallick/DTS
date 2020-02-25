package com.mobile.dts.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mobile.dts.R;
import com.mobile.dts.utills.Constants;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.Calendar;

import static com.mobile.dts.utills.Constants.isDisbledNotification;
import static com.mobile.dts.utills.Constants.isRealTimeNotification;
import static com.mobile.dts.utills.Constants.saved_time;


public class WidgetSchedule extends AppCompatActivity {
    String TAG="WidgetSchedule";
    TextView cancel,update;
    private SharedPreferences sharedpreferences;

    RadioGroup radioGroup;
    RadioButton real_time,time;
    TimePicker time1;
    LinearLayout ll_clock;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_schedule);
        cancel=findViewById(R.id.cancel);
        update=findViewById(R.id.update);
        radioGroup=findViewById(R.id.radioGroup);
        real_time=findViewById(R.id.real_time);
        time=findViewById(R.id.time);
        time1=findViewById(R.id.time1);
        ll_clock=findViewById(R.id.ll_clock);
       // hideAmPmLayout(time1);
        sharedpreferences =getApplicationContext().getSharedPreferences(Constants.appPref, Activity.MODE_PRIVATE);

        final boolean isDisbledNotification = sharedpreferences.getBoolean(Constants.isDisbledNotification, false);
        Log.d(TAG, "onCreate: "+isDisbledNotification);

        if (!isDisbledNotification) {
            String selected_time = sharedpreferences.getString(saved_time, "");
            Log.d(TAG, "onCreate: "+selected_time);
            String[] timeArray = selected_time.split(":");
            Log.d(TAG, "onCreate1: "+timeArray.length);
            if(timeArray.length > 1) {
                String time=timeArray[1];
                Log.d(TAG, "onCreate: "+time);
                int hours = Integer.parseInt(timeArray[0]);
                time1.setCurrentHour(hours);


                String[] array2 = timeArray[1].split(" ");

                int minutes = Integer.parseInt(array2[0]);
                time1.setCurrentMinute(Integer.valueOf(minutes));
                String amPm = array2[1];
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hours);
                calendar.set(Calendar.MINUTE, minutes);
            }

            if (selected_time.isEmpty()) {
                real_time.setChecked(true);
                ll_clock.setEnabled(false);
            } else {
                time.setChecked(true);
                ll_clock.setEnabled(true);
                ll_clock.setClickable(true);
            }
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if(checkedId == R.id.real_time) {
                    real_time.setChecked(true);
                    time.setChecked(false);

                } else if(checkedId == R.id.time) {
                    real_time.setChecked(false);
                    time.setChecked(true);
                    SharedPreferences.Editor editor1 = sharedpreferences.edit();
                    editor1.putBoolean(isRealTimeNotification, false);
                    Log.d(TAG, "onClick: "+isRealTimeNotification);
                    SharedPreferences.Editor editor4 = sharedpreferences.edit();
                    editor4.putBoolean(String.valueOf(isDisbledNotification), true);
                    editor4.commit();
                    Log.d(TAG, "onClick: "+isDisbledNotification);
                    editor1.commit();

                    // tvw.setText("Selected Date: "+ hour +":"+ minute+" "+am_pm);
                }

            }

        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (real_time.isChecked()) {

                    SharedPreferences.Editor editor4 = sharedpreferences.edit();
                    editor4.putString(saved_time, "");
                    editor4.commit();

                    FancyToast.makeText(getApplicationContext(),"Update",FancyToast.SUCCESS).show();
                    finish();

                } else if (time.isChecked()) {

                    String min_s = "", hour_s = "";

                    int hour, minute;
                    String am_pm;
                    if (Build.VERSION.SDK_INT >= 23 ){
                        hour = time1.getHour();
                        minute = time1.getMinute();
                    } else{
                        hour = time1.getCurrentHour();
                        minute = time1.getCurrentMinute();
                    }



                    if (minute >= 0 && minute <= 9){
                        min_s = "0"+minute;
                    }else {
                        min_s = String.valueOf(minute);
                    }

                    if(hour > 12) {
                        am_pm = "PM";
                        hour = hour - 12;

                        if (hour >= 0 && hour <= 9){
                            hour_s = "0"+hour;
                        }else {
                            hour_s = String.valueOf(hour);
                        }


                    } else {
                        if (hour >= 0 && hour <= 9){
                            hour_s = "0"+hour;
                        }else {
                            hour_s = String.valueOf(hour);
                        }
                        am_pm="AM";
                    }
                    Log.d(TAG, "Selected Date: "+ hour_s +":"+ min_s+" "+am_pm);
                   // tvw.setText("Selected Date: "+ hour +":"+ minute+" "+am_pm);

                    String time = hour_s +":"+ min_s+" "+am_pm;

                    SharedPreferences.Editor editor4 = sharedpreferences.edit();
                    editor4.putString(saved_time, time);
                    editor4.commit();

                }

                FancyToast.makeText(getApplicationContext(),"Update",FancyToast.SUCCESS).show();
                finish();
            }


    });
        /*update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor1 = sharedpreferences.edit();
                editor1.putBoolean(isRealTimeNotification, true);
                editor1.commit();

                FancyToast.makeText(getApplicationContext(),"Update",FancyToast.SUCCESS).show();
                finish();
            }
        });*/
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });





      //  showCustomDialog();




    }

    private void hideAmPmLayout(TimePicker picker) {
        final int id = Resources.getSystem().getIdentifier("ampm_layout", "id", "android");
        final View amPmLayout = picker.findViewById(id);
        if(amPmLayout != null) {
            amPmLayout.setVisibility(View.GONE);
        }
    }
}
