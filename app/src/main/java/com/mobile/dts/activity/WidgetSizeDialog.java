package com.mobile.dts.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.mobile.dts.R;

import static com.mobile.dts.utills.Constants.appPref;
import static com.mobile.dts.utills.Constants.widgetSize;

/*Use to set Widget size*/
public class WidgetSizeDialog extends AppCompatActivity {

    private SeekBar wodgetSizeBar;
    private ImageView widgetImage;
    private RelativeLayout.LayoutParams params;
    private SharedPreferences sharedpreferences;
    private WindowManager windowManager;
    private int _widgetSize;
    private Button updatesizebtn;
    private int _progress = 0;
    private int widgetSizeFromDimens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_widgetsizesetting);
        getScreenSize();
        widgetImage = findViewById(R.id.widgetimage);
        sharedpreferences = this.getSharedPreferences(appPref, Activity.MODE_PRIVATE);
        wodgetSizeBar = findViewById(R.id.sizeprogress);
        updatesizebtn = findViewById(R.id.updatesizebtn);
        widgetSizeFromDimens = (int) (getResources().getDimension(R.dimen.widgetsamllsize));
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
                    Toast.makeText(WidgetSizeDialog.this, "Size updated successfully", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

    }

    private void getScreenSize() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
    }

}