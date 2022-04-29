package com.yunbao.common.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.yunbao.common.R;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————


public class MyViewPager extends ViewPager {

    private boolean mCanScroll;

    public MyViewPager(Context context) {
        this(context, null);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyViewPager);
        mCanScroll = ta.getBoolean(R.styleable.MyViewPager_canScroll, true);
        ta.recycle();
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mCanScroll) {
            try {
                return super.onInterceptTouchEvent(ev);
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mCanScroll) {
            try {
                return super.onTouchEvent(ev);
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public void setCanScroll(boolean canScroll) {
        mCanScroll = canScroll;
    }

}
