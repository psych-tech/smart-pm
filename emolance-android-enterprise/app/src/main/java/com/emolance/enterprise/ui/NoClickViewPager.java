package com.emolance.enterprise.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;

/**
 * Created by user on 12/13/17.
 */

public class NoClickViewPager extends ViewPager {
    private boolean enableSwipe;

    public NoClickViewPager(Context context) {
        super(context);
        init();
    }

    private void init() {
        enableSwipe = false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return enableSwipe && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return enableSwipe && super.onTouchEvent(ev);
    }

    public void setEnableSwipe(boolean enableSwipe) {
        this.enableSwipe = enableSwipe;
    }
}
