package com.yunbao.common.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

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


public class MyLinearLayout4 extends LinearLayout {

    private int mScreenWidth;
    private float mCount;

    public MyLinearLayout4(Context context) {
        this(context, null);
    }

    public MyLinearLayout4(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyLinearLayout4(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyLinearLayout4);
        mCount = ta.getFloat(R.styleable.MyLinearLayout4_mll4_count, 1);
        ta.recycle();
        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (mScreenWidth / mCount), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
