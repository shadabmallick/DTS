package com.mobile.dts.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mobile.dts.R;
import com.mobile.dts.utills.Constants;
import com.shashank.sony.fancytoastlib.FancyToast;

import static com.mobile.dts.utills.Constants.isDisbledNotification;
import static com.mobile.dts.utills.Constants.isRealTimeNotification;


public class WidgetSchedule extends AppCompatActivity {
    TextView cancel,update;
    private SharedPreferences sharedpreferences;
    private boolean isRealtimeNotification;
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
        hideAmPmLayout(time1);
        sharedpreferences =getApplicationContext().getSharedPreferences(Constants.appPref, Activity.MODE_PRIVATE);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor1 = sharedpreferences.edit();
                editor1.putBoolean(isRealTimeNotification, true);
                editor1.commit();
                SharedPreferences.Editor editor4 = sharedpreferences.edit();
                editor4.putBoolean(isDisbledNotification, false);
                editor4.commit();
                FancyToast.makeText(getApplicationContext(),"Update",FancyToast.SUCCESS).show();
                finish();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
      //  showCustomDialog();

        boolean isDisbledNotification = sharedpreferences.getBoolean(Constants.isDisbledNotification, false);
        if (!isDisbledNotification) {
            isRealtimeNotification = sharedpreferences.getBoolean(Constants.isRealTimeNotification, false);
            if (isRealtimeNotification) {

                real_time.setChecked(true);
                ll_clock.setEnabled(false);



            } else {
                time.setChecked(true);
                ll_clock.setEnabled(true);
                ll_clock.setClickable(true);

            }
        }


    }

    private void hideAmPmLayout(TimePicker picker) {
        final int id = Resources.getSystem().getIdentifier("ampm_layout", "id", "android");
        final View amPmLayout = picker.findViewById(id);
        if(amPmLayout != null) {
            amPmLayout.setVisibility(View.GONE);
        }
    }
}
