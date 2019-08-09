package com.mobile.dts.helper;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ImageViewerPager extends android.support.v4.view.ViewPager {

    public static boolean enabled = true;

    public ImageViewerPager(Context context) {
        super(context);
    }

    public ImageViewerPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static void setPagingEnabled(boolean _enabled) {
        enabled = _enabled;
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        return super.canScroll(v, checkV, dx, x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            if (this.enabled) {
                return super.onTouchEvent(ev);
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            if (this.enabled) {
                return super.onInterceptTouchEvent(ev);
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}