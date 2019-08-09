package com.mobile.dts.helper;


import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobile.dts.R;

import static com.mobile.dts.utills.Constants.appPref;
import static com.mobile.dts.utills.Constants.stopServiceBroadcast;
import static com.mobile.dts.utills.Constants.widgetSize;
import static com.mobile.dts.utills.Constants.xCoordinate;
import static com.mobile.dts.utills.Constants.yCoordinate;

/*Use to set Widget size*/
public class LocationWidget extends Service {
    public static boolean isMoved = false;
    public static float _xCoordinate;
    public static float _yCoordinate;
    private RelativeLayout smallCircle;
    private float screenWidthhalf, screenHeightHalf;
    Runnable downCallback = new Runnable() {
        public void run() {
            if (!isActionDown) {
                ObjectAnimator text1 = ObjectAnimator.ofFloat(smallCircle, "scaleX", .70f);
                text1.setDuration(500);
                text1.start();
                ObjectAnimator text2 = ObjectAnimator.ofFloat(smallCircle, "scaleY", .70f);
                text2.setDuration(500);
                text2.start();
                ObjectAnimator textalpha = ObjectAnimator.ofFloat(smallCircle, "alpha", .50f);
                textalpha.setDuration(500);
                textalpha.start();
            }
        }
    };

    private WindowManager windowManager;
    BroadcastReceiver stopServiceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                visibility();
                stopSelf();

            } catch (Exception e) {
            }
        }
    };
    private float screen_width;
    private float screen_height;
    private float original_width;
    private WindowManager.LayoutParams _closeParams;
    private WindowManager.LayoutParams myParams;
    private Animation shake;
    private MoveAnimator animator;
    private TextView imgtext;
    private boolean isFirstTime = true, isActionDown = false, isOrientation = false;
    private SharedPreferences sharedpreferences;
    private int _widgetSize;
    private int lastX = -1, lastY = -1;
    private int widgetSizefromDimens;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedpreferences = getApplicationContext().getSharedPreferences(appPref, Activity.MODE_PRIVATE);
        initializeView();
        getScreenSize();
        isMoved = false;
        isFirstTime = true;
        widgetSizefromDimens = (int) (getResources().getDimension(R.dimen.widgetsize));
        registerReceiver(stopServiceBroadcastReceiver, new IntentFilter(stopServiceBroadcast));
        try {
            // for moving the picture on touch and slide
            smallCircle.setOnTouchListener(new View.OnTouchListener() {
                public WindowManager.LayoutParams myParams2;
                Handler down = new Handler();
                Handler up = new Handler();
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, final MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            isActionDown = true;

                                initialX = myParams.x;
                                initialY = myParams.y;
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();

                            animator.stop();
                            break;
                        case MotionEvent.ACTION_UP:

                                lastX = myParams.x;
                                lastY = myParams.y;
                                _xCoordinate = myParams.x;
                                _yCoordinate = myParams.y;

                            isActionDown = false;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            isMoved = true;

                            int xp =  myParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                            int yp =  myParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                            Log.e("ACTION_MOVE", "myParams.x " + myParams.x + " myParams.y" + myParams.y + " screen_width" + screen_width / 2 + " screen_height" + screen_height / 2);

                            if(screenWidthhalf+43> Math.abs(xp) &&  screenHeightHalf-86 >  Math.abs(yp)) {
                                myParams.x = xp;
                                myParams.y = yp;
                                windowManager.updateViewLayout(v, myParams);
                                if (MathUtil.betweenExclusive((int) event.getRawX(), 0, (int) screen_width / 5) || MathUtil.betweenExclusive((int) event.getRawX(), (int) screen_width - (int) (screen_width / 5), (int) screen_width)) {
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    //  layoutParams.width = (int) (widgetSizefromDimens);
                                    //  layoutParams.height = (int) (widgetSizefromDimens);
                                    //  smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                } else if (MathUtil.betweenExclusive((int) event.getRawX(), 2 * (int) (screen_width / 5), 3 * (int) (screen_width / 5))) {
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    //  layoutParams.width = (int) (widgetSizefromDimens);//...
                                    // layoutParams.height = (int) (widgetSizefromDimens);//...
                                    //  smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                } else if (MathUtil.betweenExclusive((int) event.getRawX(), (int) screen_width / 5, 2 * (int) (screen_width / 5)) || MathUtil.betweenExclusive((int) event.getRawX(), 3 * (int) (screen_width / 5), (int) screen_width)) {
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    // layoutParams.width = (int) (widgetSizefromDimens);//..
                                    // layoutParams.height = (int) (widgetSizefromDimens);//..
                                    // smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                }
                            }
                            break;
                    }
                    return true;

                }

            });

        } catch (Exception e) {
        }
    }

    private void visibility() {
        try {
            if (windowManager != null) {
                windowManager.removeViewImmediate(smallCircle);
            }
        } catch (Exception e) {
        }
    }

    private void initializeView() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        smallCircle = (RelativeLayout) inflater.inflate(R.layout.view_dts, null);
        imgtext = smallCircle.findViewById(R.id.imgtext);
        imgtext.setVisibility(View.INVISIBLE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.wiggle);
        shake.setRepeatCount(Animation.INFINITE);
        animator = new MoveAnimator();
    }

    private void getScreenSize() {
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screen_width = size.x;
        screen_height = size.y;
        original_width = size.x;
        int marginValue = (int) getResources().getDimension(R.dimen.widgetmargin);
        screen_width = screen_width - marginValue;
        screenWidthhalf = screen_width/2;
        screenHeightHalf = screen_height/2;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void move(float deltaX, float deltaY) {
        myParams.x += deltaX;
        myParams.y += deltaY;
        windowManager.updateViewLayout(smallCircle, myParams);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showWidget();
        return START_STICKY;
    }

    private void showWidget() {
        Handler down;
        try {
            if (isFirstTime) {
                isFirstTime = false;
                int LAYOUT_FLAG;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

                } else {
                    LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
                }
                _closeParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        LAYOUT_FLAG,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                _closeParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
                _closeParams.x = 0;
                _closeParams.y = 100;
                if (sharedpreferences.contains(widgetSize)) {
                    _widgetSize = (int) sharedpreferences.getFloat(widgetSize, (int) (widgetSizefromDimens));
                } else {
                    _widgetSize = (int) (widgetSizefromDimens);
                }
                myParams = new WindowManager.LayoutParams(
                        _widgetSize,
                        _widgetSize,
                        LAYOUT_FLAG,
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );
                myParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
                float valueX = 0.0f;
                if (lastX != -1) {
                    if (lastX > 0 && isOrientation) {
                        if (screen_width > screen_height) {
                            valueX = (original_width / 2.1f);
                        } else {
                            valueX = (screen_width / 2.0f);
                        }
                    } else if (lastX < 0 && isOrientation) {
                        if (screen_width > screen_height) {
                            valueX = -(original_width / 2.1f);
                        } else {
                            valueX = -(screen_width / 2.0f);
                        }
                    }
                    lastX = (int) valueX;
                } else {
                    if (screen_width > screen_height) {
                        valueX = (original_width / 2.1f);
                    } else {
                        valueX = (screen_width / 2.0f);
                    }
                }
                if (sharedpreferences.contains(xCoordinate)) {
                    myParams.x = (int) sharedpreferences.getFloat(xCoordinate, valueX);
                } else {
                    myParams.x = (int) valueX;
                }
                float valueY = 0.0f;
                if (lastY != -1) {
                    if (lastY > 0 && isOrientation) {
                        isOrientation = false;
                        if (screen_width > screen_height) {
                            valueY = lastY * (screen_height / original_width);

                        } else {
                            valueY = lastY * (screen_height / original_width);

                        }


                    } else if (lastY < 0 && isOrientation) {
                        isOrientation = false;
                        if (screen_width > screen_height) {
                            valueY = lastY * (screen_height / original_width);

                        } else {
                            valueY = lastY * (screen_height / original_width);

                        }
                    }
                    lastY = (int) valueY;

                }
                if (valueY > 0) {
                    if (((screen_height / 2) - 150) < valueY) {
                        valueY = screen_height / 2 - (int) getResources().getDimension(R.dimen.widgetmarginTopBottom);

                    }


                } else {
                    if (-((screen_height / 2) - 150) > valueY) {
                        valueY = -screen_height / 2 + (int) getResources().getDimension(R.dimen.widgetmarginTopBottom);
                    }

                }
                if (sharedpreferences.contains(yCoordinate)) {
                    myParams.y = (int) sharedpreferences.getFloat(yCoordinate, valueY);

                } else {
                    myParams.y = (int) valueY;
                }
                if (smallCircle.getParent() == null) {
                    windowManager.addView(smallCircle, myParams);

                }
                down = new Handler();
                down.postDelayed(downCallback, 500);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public boolean stopService(Intent name) {
        unregisterReceiver(stopServiceBroadcastReceiver);
        return super.stopService(name);
    }

    private static class MathUtil {
        public static boolean betweenExclusive(int x, int min, int max) {
            return x > min && x < max;
        }
    }

    //this class is written to move the image to either corners if touch_up
    private class MoveAnimator implements Runnable {

        private Handler handler = new Handler(Looper.getMainLooper());
        private float destinationX;
        private float destinationY;
        private long startingTime;

        private void start(float x, float y) {
            this.destinationX = x;
            this.destinationY = y;
            startingTime = System.currentTimeMillis();
            handler.post(this);
        }

        @Override
        public void run() {
            if (smallCircle != null && smallCircle.getParent() != null) {
                float progress = Math.min(1, (System.currentTimeMillis() - startingTime) / 400f);
                float deltaX = (destinationX - myParams.x) * progress;
                float deltaY = (destinationY - myParams.y) * progress;
                move(deltaX, deltaY);
                if (progress < 1) {
                    handler.post(this);
                }
            }
        }

        private void stop() {
            handler.removeCallbacks(this);
        }
    }


}

