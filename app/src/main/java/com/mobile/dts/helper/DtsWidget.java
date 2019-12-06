package com.mobile.dts.helper;


import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.mobile.dts.R;
import com.mobile.dts.activity.DtsGalleryActivity;
import com.mobile.dts.activity.ImageViewLoadingActivity;
import com.mobile.dts.model.DateTimeBean;
import com.mobile.dts.model.ImageBean;
import com.mobile.dts.utills.Constants;
import com.mobile.dts.utills.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.mobile.dts.utills.Constants.appPref;
import static com.mobile.dts.utills.Constants.foregroundservicenotificationRequestCode;
import static com.mobile.dts.utills.Constants.lastViewTime;
import static com.mobile.dts.utills.Constants.widgetSize;
import static com.mobile.dts.utills.Constants.xCoordinate;
import static com.mobile.dts.utills.Constants.yCoordinate;


/*Use to show Dts Widget*/
public class DtsWidget extends Service implements View.OnClickListener {
    private int disabledistanceX;
    private int disabledistanceY;
    private RelativeLayout smallCircle; //this is the image onscreen
    private Runnable downCallback, upCallback;
    private ImageView close, image, iv_play;
    private LinearLayout layout;
    private WindowManager windowManager;
    private WindowManager windowManagerClose;
    private float screen_width;
    private float screen_height;
    private float original_width;
    private WindowManager.LayoutParams _closeParams;
    private WindowManager.LayoutParams myParams;
    private Animation shake;
    private Context context;
    private MoveAnimator animator;
    private MoveAnimatorCancelWidget animatorCancel;
    private TextView imgtext, imgtextRight;
    private boolean isFirstTime = true, isActionDown = false, isOrientation = false, isWidgetClick = false;
    private SharedPreferences sharedpreferences, settingsPref;
    private int tempNumber = 0;
    private int _widgetSize;
    private int lastX = -1, lastY = -1;
    private int widgetSizefromDimens;
    private GestureDetector mDetector;
    private boolean isOpenlandscapeMode;

    @Override
    public void onCreate() {
        super.onCreate();
        downCallback = new Runnable() {
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
        upCallback = new Runnable() {
            public void run() {
                ObjectAnimator text1 = ObjectAnimator.ofFloat(smallCircle, "scaleX", 1f);
                text1.setDuration(500);
                text1.start();
                ObjectAnimator text2 = ObjectAnimator.ofFloat(smallCircle, "scaleY", 1f);
                text2.setDuration(500);
                text2.start();
                ObjectAnimator textalpha = ObjectAnimator.ofFloat(smallCircle, "alpha", 1f);
                textalpha.setDuration(500);
                textalpha.start();
            }
        };
        sharedpreferences = getApplicationContext().getSharedPreferences(appPref, Activity.MODE_PRIVATE);
        settingsPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        initializeView();
        getScreenSize();
        mDetector = new GestureDetector(this, new DtsGestureListener());
        isFirstTime = true;
        if (sharedpreferences.contains(widgetSize)) {
            widgetSizefromDimens = (int) sharedpreferences.getFloat(widgetSize, (int) (getResources().getDimension(R.dimen.widgetsize)));
        } else {
            widgetSizefromDimens = (int) (getResources().getDimension(R.dimen.widgetsize));
        }
        disabledistanceX = (int) getResources().getDimension(R.dimen.disableDistanceX);
        disabledistanceY = (int) getResources().getDimension(R.dimen.disableDistanceY);
        try {
            // for moving the picture on touch and slide
            smallCircle.setOnTouchListener(new View.OnTouchListener() {
                public WindowManager.LayoutParams myParams2;
                Handler down = new Handler();
                Handler up = new Handler();
                int valueMargin = (int) getResources().getDimension(R.dimen.widgetmarginTopBottom);
                boolean isNear = true;
                long mLastClickTime = 0;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, final MotionEvent event) {
                    if (mDetector != null && mDetector.onTouchEvent(event)) {
                        // gestureDetector();
                    }
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            isActionDown = true;
                            down.removeCallbacks(downCallback);
                            initialX = myParams.x;
                            initialY = myParams.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            animator.stop();
                            animatorCancel.stop();
                            up = new Handler();
                            up.postDelayed(upCallback, 500);
                            break;
                        case MotionEvent.ACTION_UP:
                            lastX = myParams.x;
                            lastY = myParams.y;
                            isActionDown = false;
                            down.removeCallbacks(downCallback);
                            down.postDelayed(downCallback, 3500);
                            if (MathUtil.betweenExclusive(myParams.x, -disabledistanceX, disabledistanceX) && MathUtil.betweenExclusive(myParams.y, ((int) (screen_height / 3)) - disabledistanceY, ((int) screen_height / 2) + disabledistanceY)) {
                                final Runnable r = new Runnable() {
                                    public void run() {
                                        Date currentTime = Calendar.getInstance().getTime();
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putLong(lastViewTime, currentTime.getTime());
                                        editor.commit();
                                        visibility();
                                        stopSelf();
                                    }
                                };
                                new Handler().postDelayed(r, 380);
                                return true;
                            }
                            if (myParams.x > 0) {
                                imgtext.setVisibility(View.VISIBLE);
                                imgtextRight.setVisibility(View.INVISIBLE);
                            } else {
                                imgtext.setVisibility(View.INVISIBLE);
                                imgtextRight.setVisibility(View.VISIBLE);
                            }
                            if (MathUtil.betweenExclusive(myParams.x, -100, 100)
                                    && !MathUtil.betweenExclusive(myParams.y, (int) screen_height / 3, (int) screen_height / 2)) {
                                //moving to center range of screen
                                int valuex;
                                int valuey;
                                if (myParams.x > 0) {
                                    valuex = (int) screen_width / 2;
                                } else {
                                    valuex = -(int) screen_width / 2;
                                }
                                if (myParams.y > 0) {
                                    valuey = -(int) getResources().getDimension(R.dimen.widgetmarginTopBottom);
                                } else {
                                    valuey = (int) getResources().getDimension(R.dimen.widgetmarginTopBottom);
                                }
                                if (myParams.y < 0) {
                                    if (screen_height / 2 + myParams.y < (int) getResources().getDimension(R.dimen.widgetmarginTopBottom)) {
                                        animator.start(valuex, myParams.y + valuey);
                                    } else {
                                        animator.start(valuex, myParams.y);
                                    }
                                }
                                if (myParams.y > 0) {
                                    if (screen_height / 2 - myParams.y < (int) getResources().getDimension(R.dimen.widgetmarginTopBottom)) {
                                        animator.start(valuex, myParams.y + valuey);
                                    } else {
                                        animator.start(valuex, myParams.y);
                                    }
                                }
                                android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                layoutParams.width = (int) (widgetSizefromDimens);
                                layoutParams.height = (int) (widgetSizefromDimens);
                                smallCircle.setLayoutParams(layoutParams);
                                try {
                                    windowManager.updateViewLayout(v, myParams);
                                } catch (Exception e) {
                                }
                                layout.setVisibility(View.INVISIBLE);
                            } else if (MathUtil.betweenExclusive((int) event.getRawX(), 0, (int) screen_width / 5)) {
                                if (MathUtil.betweenExclusive((int) event.getRawY(), 0, (int) screen_height / 10)) {
                                    animator.start(-screen_width / 2, -(screen_height / 2) + valueMargin);
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    layoutParams.width = (int) (widgetSizefromDimens);
                                    layoutParams.height = (int) (widgetSizefromDimens);
                                    smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                    layout.setVisibility(View.INVISIBLE);
                                } else if (MathUtil.betweenExclusive((int) event.getRawY(), 9 * (int) (screen_height / 10), (int) screen_height)) {
                                    animator.start(-screen_width / 2, screen_height / 2 - valueMargin);
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    layoutParams.width = (int) (widgetSizefromDimens);
                                    layoutParams.height = (int) (widgetSizefromDimens);
                                    smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                    layout.setVisibility(View.INVISIBLE);
                                } else {
                                    int valuex;
                                    int valuey;
                                    if (myParams.x > 0) {
                                        valuex = (int) screen_width / 2;
                                    } else {
                                        valuex = -(int) screen_width / 2;
                                    }
                                    if (myParams.y > 0) {
                                        valuey = -(int) getResources().getDimension(R.dimen.widgetmarginTopBottom);
                                    } else {
                                        valuey = (int) getResources().getDimension(R.dimen.widgetmarginTopBottom);
                                    }
                                    if (myParams.y < 0) {
                                        if (screen_height / 2 + myParams.y < (int) getResources().getDimension(R.dimen.widgetmarginTopBottom)) {
                                            animator.start(valuex, myParams.y + valuey);
                                        } else {
                                            animator.start(valuex, myParams.y);
                                        }
                                    }
                                    if (myParams.y > 0) {
                                        if (screen_height / 2 - myParams.y < (int) getResources().getDimension(R.dimen.widgetmarginTopBottom)) {
                                            animator.start(valuex, myParams.y + valuey);
                                        } else {
                                            animator.start(valuex, myParams.y);
                                        }
                                    }
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    layoutParams.width = (int) (widgetSizefromDimens);
                                    layoutParams.height = (int) (widgetSizefromDimens);
                                    smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                    layout.setVisibility(View.INVISIBLE);
                                }

                            } else if (MathUtil.betweenExclusive((int) event.getRawX(), (int) screen_width - (int) (screen_width / 5), (int) screen_width)) {
                                if (MathUtil.betweenExclusive((int) event.getRawY(), 0, (int) screen_height / 10)) {
                                    animator.start(screen_width / 2, -(screen_height / 2) + valueMargin);
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    layoutParams.width = (int) (widgetSizefromDimens);
                                    layoutParams.height = (int) (widgetSizefromDimens);
                                    smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                    layout.setVisibility(View.INVISIBLE);
                                } else if (MathUtil.betweenExclusive((int) event.getRawY(), 9 * (int) (screen_height / 10), (int) screen_height)) {
                                    animator.start(screen_width / 2, screen_height / 2 - valueMargin);
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    layoutParams.width = (int) (widgetSizefromDimens);
                                    layoutParams.height = (int) (widgetSizefromDimens);
                                    smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                    layout.setVisibility(View.INVISIBLE);
                                } else {
                                    int valuex;
                                    int valuey;
                                    if (myParams.x > 0) {
                                        valuex = (int) screen_width / 2;
                                    } else {
                                        valuex = -(int) screen_width / 2;
                                    }
                                    if (myParams.y > 0) {
                                        valuey = -(int) getResources().getDimension(R.dimen.widgetmarginTopBottom);
                                    } else {
                                        valuey = (int) getResources().getDimension(R.dimen.widgetmarginTopBottom);
                                    }
                                    if (myParams.y < 0) {
                                        if (screen_height / 2 + myParams.y < (int) getResources().getDimension(R.dimen.widgetmarginTopBottom)) {
                                            animator.start(valuex, myParams.y + valuey);
                                        } else {
                                            animator.start(valuex, myParams.y);
                                        }
                                    }
                                    if (myParams.y > 0) {
                                        if (screen_height / 2 - myParams.y < (int) getResources().getDimension(R.dimen.widgetmarginTopBottom)) {
                                            animator.start(valuex, myParams.y + valuey);
                                        } else {
                                            animator.start(valuex, myParams.y);
                                        }
                                    }
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    layoutParams.width = (int) (widgetSizefromDimens);
                                    layoutParams.height = (int) (widgetSizefromDimens);
                                    smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                    layout.setVisibility(View.INVISIBLE);
                                }

                            } else if (MathUtil.betweenExclusive((int) event.getRawX(), (int) screen_width / 5, 2 * (int) (screen_width / 5))) {
                                if (MathUtil.betweenExclusive((int) event.getRawY(), 0, (int) screen_height / 10)) {
                                    animator.start(-screen_width / 2, -(screen_height / 2) + valueMargin);
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    layoutParams.width = (int) (widgetSizefromDimens);
                                    layoutParams.height = (int) (widgetSizefromDimens);
                                    smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                    ;
                                    layout.setVisibility(View.INVISIBLE);
                                } else if (MathUtil.betweenExclusive((int) event.getRawY(), 9 * (int) (screen_height / 10), (int) screen_height)) {
                                    animator.start(-screen_width / 2, screen_height / 2 - valueMargin);
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    layoutParams.width = (int) (widgetSizefromDimens);
                                    layoutParams.height = (int) (widgetSizefromDimens);
                                    smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                    layout.setVisibility(View.INVISIBLE);
                                } else {
                                    int valuex;
                                    int valuey;
                                    if (myParams.x > 0) {
                                        valuex = (int) screen_width / 2;
                                    } else {
                                        valuex = -(int) screen_width / 2;
                                    }
                                    if (myParams.y > 0) {
                                        valuey = -(int) getResources().getDimension(R.dimen.widgetmarginTopBottom);
                                    } else {
                                        valuey = (int) getResources().getDimension(R.dimen.widgetmarginTopBottom);
                                    }
                                    if (myParams.y < 0) {
                                        if (screen_height / 2 + myParams.y < (int) getResources().getDimension(R.dimen.widgetmarginTopBottom)) {
                                            animator.start(valuex, myParams.y + valuey);
                                        } else {
                                            animator.start(valuex, myParams.y);
                                        }
                                    }
                                    if (myParams.y > 0) {
                                        if (screen_height / 2 - myParams.y < (int) getResources().getDimension(R.dimen.widgetmarginTopBottom)) {
                                            animator.start(valuex, myParams.y + valuey);
                                        } else {
                                            animator.start(valuex, myParams.y);
                                        }
                                    }
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    layoutParams.width = (int) (widgetSizefromDimens);
                                    layoutParams.height = (int) (widgetSizefromDimens);
                                    smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                    layout.setVisibility(View.INVISIBLE);
                                }
                            } else if (MathUtil.betweenExclusive((int) event.getRawX(), 3 * (int) (screen_width / 5), (int) screen_width)) {
                                if (MathUtil.betweenExclusive((int) event.getRawY(), 0, (int) screen_height / 10)) {
                                    animator.start(screen_width / 2, -(screen_height / 2) + valueMargin);
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    layoutParams.width = (int) (widgetSizefromDimens);
                                    layoutParams.height = (int) (widgetSizefromDimens);
                                    smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                    layout.setVisibility(View.INVISIBLE);
                                } else if (MathUtil.betweenExclusive((int) event.getRawY(), 9 * (int) (screen_height / 10), (int) screen_height)) {
                                    animator.start(screen_width / 2, screen_height / 2 - valueMargin);
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    layoutParams.width = (int) (widgetSizefromDimens);
                                    layoutParams.height = (int) (widgetSizefromDimens);
                                    smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                    layout.setVisibility(View.INVISIBLE);
                                } else {
                                    int valuex;
                                    int valuey;
                                    if (myParams.x > 0) {
                                        valuex = (int) screen_width / 2;
                                    } else {
                                        valuex = -(int) screen_width / 2;
                                    }
                                    if (myParams.y > 0) {
                                        valuey = -(int) getResources().getDimension(R.dimen.widgetmarginTopBottom);
                                    } else {
                                        valuey = (int) getResources().getDimension(R.dimen.widgetmarginTopBottom);
                                    }
                                    if (myParams.y < 0) {
                                        if (screen_height / 2 + myParams.y < (int) getResources().getDimension(R.dimen.widgetmarginTopBottom)) {
                                            animator.start(valuex, myParams.y + valuey);
                                        } else {
                                            animator.start(valuex, myParams.y);
                                        }
                                    }
                                    if (myParams.y > 0) {
                                        if (screen_height / 2 - myParams.y < (int) getResources().getDimension(R.dimen.widgetmarginTopBottom)) {
                                            animator.start(valuex, myParams.y + valuey);
                                        } else {
                                            animator.start(valuex, myParams.y);
                                        }
                                    }
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    layoutParams.width = (int) (widgetSizefromDimens);
                                    layoutParams.height = (int) (widgetSizefromDimens);
                                    smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                    layout.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                int valuex;
                                int valuey;
                                if (myParams.x > 0) {
                                    valuex = (int) screen_width / 2;
                                } else {
                                    valuex = -(int) screen_width / 2;
                                }
                                if (myParams.y > 0) {
                                    valuey = -(int) getResources().getDimension(R.dimen.widgetmarginTopBottom);
                                } else {
                                    valuey = (int) getResources().getDimension(R.dimen.widgetmarginTopBottom);
                                }
                                if (myParams.y < 0) {
                                    if (screen_height / 2 + myParams.y < (int) getResources().getDimension(R.dimen.widgetmarginTopBottom)) {
                                        animator.start(valuex, myParams.y + valuey);
                                    } else {
                                        animator.start(valuex, myParams.y);
                                    }
                                }
                                if (myParams.y > 0) {
                                    if (screen_height / 2 - myParams.y < (int) getResources().getDimension(R.dimen.widgetmarginTopBottom)) {
                                        animator.start(valuex, myParams.y + valuey);
                                    } else {
                                        animator.start(valuex, myParams.y);
                                    }
                                }
                                android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                layoutParams.width = (int) (widgetSizefromDimens);
                                layoutParams.height = (int) (widgetSizefromDimens);
                                smallCircle.setLayoutParams(layoutParams);
                                try {
                                    windowManager.updateViewLayout(v, myParams);
                                } catch (Exception e) {
                                }
                                layout.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            layout.setVisibility(View.VISIBLE);
                            myParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                            myParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                            if (MathUtil.betweenExclusive(myParams.x, -disabledistanceX, disabledistanceX) && MathUtil.betweenExclusive(myParams.y, ((int) (screen_height / 3)) - disabledistanceY, ((int) screen_height / 2) + disabledistanceY)) {
                                if (isNear) {
                                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                        return true;
                                    }
                                    mLastClickTime = SystemClock.elapsedRealtime();
                                    isNear = false;
                                    int LAYOUT_FLAG;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

                                    } else {
                                        LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
                                    }
                                    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                                            _widgetSize,
                                            _widgetSize,
                                            LAYOUT_FLAG,
                                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                            PixelFormat.TRANSLUCENT);
                                    params.x = 0;
                                    params.y = (int) (screen_height / 2) - (int) getResources().getDimension(R.dimen.marginbottom);
                                    animatorCancel.start(params.x, params.y, myParams.x, myParams.y);
                                    ObjectAnimator text1 = ObjectAnimator.ofFloat(layout, "scaleX", 1f);
                                    text1.setDuration(50);
                                    text1.start();
                                    ObjectAnimator text2 = ObjectAnimator.ofFloat(layout, "scaleY", 1f);
                                    text2.setDuration(50);
                                    text2.start();
                                }
                            } else {
                                ObjectAnimator text1 = ObjectAnimator.ofFloat(layout, "scaleX", 0.68f);
                                text1.setDuration(50);
                                text1.start();
                                ObjectAnimator text2 = ObjectAnimator.ofFloat(layout, "scaleY", 0.68f);
                                text2.setDuration(50);
                                text2.start();
                                isNear = true;
                                windowManager.updateViewLayout(v, myParams);
                                if (MathUtil.betweenExclusive((int) event.getRawX(), 0, (int) screen_width / 5) || MathUtil.betweenExclusive((int) event.getRawX(), (int) screen_width - (int) (screen_width / 5), (int) screen_width)) {
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    layoutParams.width = (int) (widgetSizefromDimens);
                                    layoutParams.height = (int) (widgetSizefromDimens);
                                    smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                } else if (MathUtil.betweenExclusive((int) event.getRawX(), 2 * (int) (screen_width / 5), 3 * (int) (screen_width / 5))) {
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    layoutParams.width = (int) (widgetSizefromDimens);//...
                                    layoutParams.height = (int) (widgetSizefromDimens);//...
                                    smallCircle.setLayoutParams(layoutParams);
                                    try {
                                        windowManager.updateViewLayout(v, myParams);
                                    } catch (Exception e) {
                                    }
                                } else if (MathUtil.betweenExclusive((int) event.getRawX(), (int) screen_width / 5, 2 * (int) (screen_width / 5)) || MathUtil.betweenExclusive((int) event.getRawX(), 3 * (int) (screen_width / 5), (int) screen_width)) {
                                    android.view.ViewGroup.LayoutParams layoutParams = smallCircle.getLayoutParams();
                                    layoutParams.width = (int) (widgetSizefromDimens);//..
                                    layoutParams.height = (int) (widgetSizefromDimens);//..
                                    smallCircle.setLayoutParams(layoutParams);
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

    /*Use to remove widget*/
    private void visibility() {
        try {
            if (windowManager != null) {
                windowManager.removeViewImmediate(smallCircle);
                windowManagerClose.removeViewImmediate(layout);
            }
        } catch (Exception e) {
        }
    }

    private void initializeView() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        smallCircle = (RelativeLayout) inflater.inflate(R.layout.view_dts, null);
        image = smallCircle.findViewById(R.id.image);
        imgtext = smallCircle.findViewById(R.id.imgtext);
        imgtextRight = smallCircle.findViewById(R.id.imgtextright);
        iv_play = smallCircle.findViewById(R.id.iv_play);
        if (Scheduler.count != 0) {
            imgtext.setText("" + Scheduler.count);
            imgtextRight.setText("" + Scheduler.count);
        }
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManagerClose = (WindowManager) getSystemService(WINDOW_SERVICE);
        close = new ImageView(this);
        close.setImageResource(R.mipmap._close);
        close.setLayoutParams(new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.cancelbutton), (int) getResources().getDimension(R.dimen.cancelbutton)));
        context = DtsWidget.this;
        shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.wiggle);
        shake.setRepeatCount(Animation.INFINITE);
        layout = new LinearLayout(this);
        layout.addView(close);
        animator = new MoveAnimator();
        animatorCancel = new MoveAnimatorCancelWidget();
        ObjectAnimator text1 = ObjectAnimator.ofFloat(layout, "scaleX", 0.68f);
        text1.setDuration(500);
        text1.start();
        ObjectAnimator text2 = ObjectAnimator.ofFloat(layout, "scaleY", 0.68f);
        text2.setDuration(500);
        text2.start();
        startForegroundService();
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
        if (screen_width > screen_height) {
            isOpenlandscapeMode = true;
        } else {
            isOpenlandscapeMode = false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onClick(View view) {
    }

    /*use to get Media file detail*/
    private ArrayList<ImageBean> getImageArray(ArrayList<ImageBean> newImages) {
        ArrayList<ImageBean> imageBeanArrayList = new ArrayList<>();
        for (int i = 0; i < newImages.size(); i++) {
            ImageBean imageBean = new ImageBean();
            imageBean.setImagePath(newImages.get(i).getImagePath());
            imageBean.setImageSize(newImages.get(i).getImageSize());
            DateTimeBean dateTimeBean = getDateTime(newImages.get(i).getCreatedTimeStamp());
            if (dateTimeBean != null) {
                if (dateTimeBean.getDate() != null) {
                    imageBean.setCreatedDate(dateTimeBean.getDate());
                }
                if (dateTimeBean.getTime() != null) {
                    imageBean.setCreatedTime(dateTimeBean.getTime());
                }
            }
            imageBeanArrayList.add(imageBean);
        }
        return imageBeanArrayList;
    }

    /*Need to move Utility class*/
    private DateTimeBean getDateTime(long timeStamp) {
        try {
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();
            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a");
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy");
            sdfTime.setTimeZone(tz);
            sdfDate.setTimeZone(tz);
            String localTime = sdfTime.format(new Date(timeStamp).getTime());
            String localTimeDate = sdfDate.format(new Date(timeStamp).getTime());
            DateTimeBean dateTimeBean = new DateTimeBean();
            dateTimeBean.setTime(localTime);
            dateTimeBean.setDate(localTimeDate);
            return dateTimeBean;

        } catch (Exception e) {
        }
        return null;
    }

    protected void move(float deltaX, float deltaY) {
        myParams.x += deltaX;
        myParams.y += deltaY;
        windowManager.updateViewLayout(smallCircle, myParams);
    }

    protected void moveCancel(float deltaX, float deltaY) {
        myParams.x += deltaX;
        myParams.y += deltaY;
        windowManager.updateViewLayout(smallCircle, myParams);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showWidget();
        return START_STICKY;
    }

    /*use to Widget*/
    private void showWidget() {
        Handler down;
        Handler up;
        try {
            int widgetRatio = 1;
            if (isFirstTime) {
                visibility();
                isFirstTime = false;
                int LAYOUT_FLAG;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

                } else {
                    LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
                }
                if (Scheduler.count == 0) {
                    visibility();
                    stopSelf();
                    return;
                }
                _closeParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        LAYOUT_FLAG,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                _closeParams.x = 0;
                _closeParams.y = (int) (screen_height / 2) - (int) getResources().getDimension(R.dimen.marginbottom);
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
                if (layout.getParent() != null) {
                    windowManagerClose.removeView(layout);
                }
                windowManagerClose.addView(layout, _closeParams);
                layout.setVisibility(View.INVISIBLE);
                close.startAnimation(shake);
            }
            if (tempNumber != Scheduler.count) {
                up = new Handler();
                up.postDelayed(upCallback, 500);
                down = new Handler();
                down.postDelayed(downCallback, 3500);
                tempNumber = Scheduler.count;
            }
            if (Scheduler.count != 0 && imgtext != null) {
                try {
                    String medhodOption = settingsPref.getString("method", "1008");
                    switch (medhodOption) {
                        case "1008":
                            imgtext.setText("" + Scheduler.count);
                            imgtextRight.setText("" + Scheduler.count);
                            ArrayList<ImageBean> newImagesWithoutImage = new Utils().getNewImageList(context);
                            if (newImagesWithoutImage != null && newImagesWithoutImage.size() > 0) {
                                Scheduler.count = newImagesWithoutImage.size();
                                Scheduler.newImages = newImagesWithoutImage;
                                if (Scheduler.count > 99) {
                                    imgtext.setText("" + 99 + "+");
                                    imgtextRight.setText("" + 99 + "+");
                                } else {
                                    imgtext.setText("" + Scheduler.count);
                                    imgtextRight.setText("" + Scheduler.count);
                                }
                                iv_play.setVisibility(View.GONE);
                            }
                            break;
                        case "1009":
                            RequestOptions requestOptions = new RequestOptions();
                            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(_widgetSize / 2));
                            ArrayList<ImageBean> imageBeans = getImageArray(Scheduler.newImages);
                            if (imageBeans != null && imageBeans.size() > 0) {
                                GlideApp.with(context).load("file://" + imageBeans.get(0).getImagePath())
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .apply(requestOptions)
                                        .into(image);

                            }
                            if (Scheduler.count > 99) {
                                imgtext.setText("" + 99 + "+");
                                imgtextRight.setText("" + 99 + "+");

                            } else {
                                imgtext.setText("" + Scheduler.count);
                                imgtextRight.setText("" + Scheduler.count);
                            }
                            ArrayList<ImageBean> newImages = new Utils().getNewImageList(context);
                            if (newImages != null && newImages.size() > 0) {
                                Scheduler.count = newImages.size();
                                Scheduler.newImages = newImages;
                                if (Scheduler.count > 99) {
                                    imgtext.setText("" + 99 + "+");
                                    imgtextRight.setText("" + 99 + "+");

                                } else {
                                    imgtext.setText("" + Scheduler.count);
                                    imgtextRight.setText("" + Scheduler.count);
                                }

                            }
                            if (!Utils.isImageFile(imageBeans.get(0).getImagePath())) {
                                iv_play.setVisibility(View.VISIBLE);
                            } else {
                                iv_play.setVisibility(View.GONE);
                            }
                            break;
                        default:
                    }
                    float badgeRatio = _widgetSize / (getResources().getDimension(R.dimen.widgetsize));
                    if (badgeRatio != 1 || Scheduler.count > 99) {
                        int badgeSize = (int) (getResources().getDimension(R.dimen.dp17) * badgeRatio);
                        int badgeTextSize = (int) (9.0f * badgeRatio);
                        FrameLayout.LayoutParams imgtextParam1;
                        FrameLayout.LayoutParams imgtextParam2;
                        if (Scheduler.count > 99) {
                            imgtextParam1 = new FrameLayout.LayoutParams(
                                    (badgeSize + 15),
                                    badgeSize);
                            imgtextParam2 = new FrameLayout.LayoutParams(
                                    (badgeSize + 15),
                                    badgeSize);
                        } else {
                            imgtextParam1 = new FrameLayout.LayoutParams(
                                    (badgeSize),
                                    badgeSize);
                            imgtextParam2 = new FrameLayout.LayoutParams(
                                    (badgeSize),
                                    badgeSize);
                        }
                        boolean isRightToLeft = getResources().getBoolean(R.bool.is_right_to_left);
                        int valueInPixelStart = (int) (getResources().getDimension(R.dimen.widgetTextmarginstarthebrew));
                        int valueInPixelsBottom = (int) (getResources().getDimension(R.dimen.widgetTextmarginBottom));
                        if (isRightToLeft) {
                            imgtextParam1.setMargins(0, 0, 0, valueInPixelsBottom);
                            imgtextParam1.setMarginStart(valueInPixelStart);
                            imgtextParam1.gravity = Gravity.END;
                            imgtextParam2.setMargins(0, 0, 0, valueInPixelsBottom);
                            imgtextParam2.setMarginEnd(valueInPixelStart);
                            imgtextParam2.gravity = Gravity.START;

                        } else {
                            imgtextParam1.setMargins(0, 0, 0, valueInPixelsBottom);
                            imgtextParam1.setMarginEnd(valueInPixelStart);
                            imgtextParam2.setMargins(0, 0, 0, valueInPixelsBottom);
                            imgtextParam2.setMarginStart(valueInPixelStart);
                            imgtextParam2.gravity = Gravity.END;
                        }
                        imgtext.setTextSize(badgeTextSize);
                        imgtext.setLayoutParams(imgtextParam1);
                        imgtextRight.setTextSize(badgeTextSize);
                        imgtextRight.setLayoutParams(imgtextParam2);
                    }
                } catch (Exception e) {
                }
            } else {
                visibility();
                stopSelf();
            }
            if (smallCircle != null && isOpenlandscapeMode) {
                smallCircle.setVisibility(View.GONE);
            }
        } catch (Exception e) {
        }
    }

    /*use to show foreground service notification*/
    private void showNotification() {
        Intent notificationIntent = new Intent(this, DtsGalleryActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1,
                notificationIntent, 0);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.logo_launcher);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(DtsWidget.this)
                .setTicker("KeepToo Widget showing")
                .setContentText("KeepToo Widget showing")
                .setSmallIcon(R.mipmap.logo_launcher)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationManager.IMPORTANCE_NONE)
                .setOngoing(false)
                .setVisibility(View.GONE);
        startForeground(foregroundservicenotificationRequestCode,
                notification.build());

    }

    /*use to start foreground service */
    private void startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationShow();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            naugatNotification();
        }else {
            showNotification();
        }
    }


    /*use to show foreground service notification*/
    private void naugatNotification() {
        String CHANNEL_ONE_ID = "com.mobile.dts";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setVibrationPattern(new long[]{0});
            notificationChannel.enableVibration(true);
            notificationChannel.setSound(null, null);
            notificationChannel.setBypassDnd(false);
            notificationChannel.setImportance(NotificationManager.IMPORTANCE_NONE);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }
        Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.logo_launcher);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(DtsWidget.this,CHANNEL_ONE_ID)
                .setTicker("KeepToo Widget showing")
                .setContentText("KeepToo Widget showing")
                .setSmallIcon(R.mipmap.logo_launcher)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationManager.IMPORTANCE_NONE)
                .setOngoing(false)
                .setVisibility(View.GONE);
        startForeground(foregroundservicenotificationRequestCode,
                notification.build());

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void notificationShow() {
        String CHANNEL_ONE_ID = "com.mobile.dts";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setVibrationPattern(new long[]{0});
            notificationChannel.enableVibration(true);
            notificationChannel.setSound(null, null);
            notificationChannel.setBypassDnd(false);
            notificationChannel.setImportance(NotificationManager.IMPORTANCE_NONE);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }
       /* Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setChannelId(CHANNEL_ONE_ID)
                // .setContentTitle("Dts Widget")
                .setContentText("Dts Widget showing")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(icon)
                .build();
        startForeground(foregroundservicenotificationRequestCode, notification);*/


        Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.logo_launcher);
        Notification notification = new Notification.Builder(this, CHANNEL_ONE_ID)
                .setContentText("KeepToo Widget showing")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.logo_launcher)
                .setLargeIcon(icon)
                .setPriority(Notification.PRIORITY_MIN)
                .getNotification();
        startForeground(foregroundservicenotificationRequestCode, notification);


      /*  NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ONE_ID,
                CHANNEL_ONE_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        notifManager.createNotificationChannel(serviceChannel);

        Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification = new Notification.Builder(this, "serviceNotificationChannelId")
                .setContentTitle("Hide Notification Example")
                .setContentText("To hide me, click and uncheck \"Hidden Notification Service\"")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .getNotification();
        startForeground(foregroundservicenotificationRequestCode, notification);*/
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (smallCircle != null) {
                smallCircle.setVisibility(View.GONE);
            }
        } else {
            if (smallCircle != null) {
                smallCircle.setVisibility(View.VISIBLE);
                if (!isWidgetClick && isOpenlandscapeMode) {
                    isOpenlandscapeMode = false;
                    isOrientation = true;
                    getScreenSize();
                    isFirstTime = true;
                    visibility();
                    showWidget();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        visibility();
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

    private class MoveAnimatorCancelWidget implements Runnable {
        private Handler handler = new Handler(Looper.getMainLooper());
        private float destinationX;
        private float destinationY;

        private long startingTime;

        private void start(float x, float y, float paramsx, float paramsy) {
            this.destinationX = x;
            this.destinationY = y;
            startingTime = System.currentTimeMillis();
            handler.post(this);
        }

        @Override
        public void run() {
            if (smallCircle != null && smallCircle.getParent() != null) {
                float progress = Math.min(1, (System.currentTimeMillis() - startingTime) / 300f);
                float deltaX = (destinationX - myParams.x) * progress;
                float deltaY = (destinationY - myParams.y) * progress;
                moveCancel(deltaX, deltaY);
                if (progress < 1) {
                    handler.post(this);
                }
            }
        }

        private void stop() {
            handler.removeCallbacks(this);
        }
    }

    class DtsGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (Scheduler.newImages != null && Scheduler.newImages.size() > 0) {
                visibility();
                isWidgetClick = true;
                Intent overlayIntent = new Intent(context.getApplicationContext(), ImageViewLoadingActivity.class);
                overlayIntent.putExtra(Constants.KEY_IS_FROM_NOTIFICATION, true); //Need to the remove
                overlayIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent =
                        PendingIntent.getActivity(context, 0, overlayIntent, 0);
                try {
                    pendingIntent.send();
                } catch (Exception e1) {
                    context.startActivity(overlayIntent);
                }
                stopSelf();
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            return true;
        }
    }
}
