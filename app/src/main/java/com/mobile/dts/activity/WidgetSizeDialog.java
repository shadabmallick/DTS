package com.mobile.dts.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.dts.R;

import static android.content.Context.WINDOW_SERVICE;
import static com.mobile.dts.utills.Constants.appPref;
import static com.mobile.dts.utills.Constants.widgetSize;

/*Use to set Widget size*/
public class WidgetSizeDialog extends Dialog {

    private SeekBar wodgetSizeBar;
    private ImageView widgetImage;
    private RelativeLayout.LayoutParams params;
    private SharedPreferences sharedpreferences;
    private WindowManager windowManager;
    private int _widgetSize;
    private TextView updatesizebtn,cancel;
    private int _progress = 0;
    private int widgetSizeFromDimens;


    private Context context;

    public WidgetSizeDialog(@NonNull Context context) {
        super(context, R.style.MySettingsStyle);
        //super(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_widgetsizesetting);

        //getScreenSize();

        widgetImage = findViewById(R.id.widgetimage);
        sharedpreferences = context.getSharedPreferences(appPref, Activity.MODE_PRIVATE);
        wodgetSizeBar = findViewById(R.id.sizeprogress);
        cancel = findViewById(R.id.button_cancel);
        updatesizebtn = findViewById(R.id.updatesizebtn);
        widgetSizeFromDimens = (int) (context.getResources().getDimension(R.dimen.widgetsamllsize));
        if (sharedpreferences.contains(widgetSize)) {
            _widgetSize = (int) sharedpreferences.getFloat(widgetSize, widgetSizeFromDimens);
            _widgetSize = _widgetSize * 70 / 100;
        } else {
            _widgetSize = widgetSizeFromDimens;
        }
        params = new RelativeLayout.LayoutParams(_widgetSize, _widgetSize);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        widgetImage.setLayoutParams(params);
        wodgetSizeBar.setMax((2 * widgetSizeFromDimens) / 3);
        wodgetSizeBar.setProgress(_widgetSize - widgetSizeFromDimens);
        Runnable downCallback = new Runnable() {
            public void run() {
                ObjectAnimator textalpha = ObjectAnimator.ofFloat(widgetImage, "alpha", .50f);
                textalpha.setDuration(50);
                textalpha.start();
            }
        };
        Handler down = new Handler();
        down.postDelayed(downCallback, 50);
        wodgetSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { //link "seekbar" with seek bar change listener
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                params = new RelativeLayout.LayoutParams(progress + widgetSizeFromDimens, progress + widgetSizeFromDimens);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                widgetImage.setLayoutParams(params);
                _progress = (progress + widgetSizeFromDimens) * 100 / 70;

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        /*Set Widget size*/
        updatesizebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_progress != 0) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putFloat(widgetSize, _progress);
                    editor.commit();
                    Toast.makeText(context, "Size updated successfully", Toast.LENGTH_SHORT).show();
                }
                dismiss();
            }
        });
     cancel.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             dismiss();
         }
     });
    }

    private void getScreenSize() {
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
    }

}