package com.yunbao.video.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.yunbao.video.R;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class VideoProgressBar extends View {

    private int mWidth;
    private RectF mBgRectF;
    private Paint mBgPaint;
    private Paint mFgPaint;
    private RectF mFgRectF;
    private int mBgColor;//背景色
    private int mFgColor;//前景色
    private float mProgressVal;

    public VideoProgressBar(Context context) {
        this(context, null);
    }

    public VideoProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LoadingBar);
        mBgColor = ta.getColor(R.styleable.LoadingBar_lb_bg_color, 0xff000000);
        mFgColor = ta.getColor(R.styleable.LoadingBar_lb_fg_color, 0xffffffff);
        ta.recycle();
        initPaint();
    }

    private void initPaint() {
        mBgPaint = new Paint();
        mBgPaint.setAntiAlias(true);
        mBgPaint.setDither(true);
        mBgPaint.setColor(mBgColor);
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgRectF = new RectF();

        mFgPaint = new Paint();
        mFgPaint.setAntiAlias(true);
        mFgPaint.setDither(true);
        mFgPaint.setColor(mFgColor);
        mFgPaint.setStyle(Paint.Style.FILL);
        mFgRectF = new RectF();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getMeasuredWidth();
        int height = getMeasuredHeight();
        mBgRectF.top = 0;
        mBgRectF.bottom = height;
        mBgRectF.left = 0;
        mBgRectF.right = mWidth;

        mFgRectF.top = 0;
        mFgRectF.bottom = height;
        mBgRectF.left = 0;
        mBgRectF.right = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mFgRectF.right = mProgressVal * mWidth;
        canvas.drawRect(mBgRectF, mBgPaint);
        canvas.drawRect(mFgRectF, mFgPaint);
    }


    public void setProgressVal(float progressVal) {
        mProgressVal = progressVal;
        invalidate();
    }

}
