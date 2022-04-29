package com.yunbao.common.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.AttributeSet;

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

public class StarCountView extends AppCompatTextView {

    private Drawable mCheckDrawable;
    private Drawable mUnCheckDrawable;
    private int mDrawableSize;
    private int mTotalCount;
    private int mFillCount;
    private String mContent;
    private SpannableStringBuilder mBuilder;

    public StarCountView(Context context) {
        this(context, null);
    }

    public StarCountView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarCountView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StarCountView);
        mCheckDrawable = ta.getDrawable(R.styleable.StarCountView_scv_check_drawable);
        mUnCheckDrawable = ta.getDrawable(R.styleable.StarCountView_scv_uncheck_drawable);
        mDrawableSize = (int) ta.getDimension(R.styleable.StarCountView_scv_drawable_size, 0);
        mTotalCount = ta.getInt(R.styleable.StarCountView_scv_total_count, 5);
        mFillCount = ta.getInt(R.styleable.StarCountView_scv_fill_count, 0);
        ta.recycle();
        if (mDrawableSize > 0) {
            mCheckDrawable.setBounds(0, 0, mDrawableSize, mDrawableSize);
            mUnCheckDrawable.setBounds(0, 0, mDrawableSize, mDrawableSize);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mTotalCount; i++) {
            sb.append(String.valueOf(i));
            if (i < mTotalCount - 1) {
                sb.append(" ");
            }
        }
        mContent = sb.toString();
        setFillCount(mFillCount);
    }


    public void setFillCount(int fillCount) {
        if (mBuilder == null) {
            mBuilder = new SpannableStringBuilder();
        }
        mBuilder.clear();
        mBuilder.append(mContent);
        for (int i = 0; i < mTotalCount; i++) {
            int index = mContent.indexOf(String.valueOf(i));
            if (i < fillCount) {
                mBuilder.setSpan(new ImageSpan(mCheckDrawable), index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                mBuilder.setSpan(new ImageSpan(mUnCheckDrawable), index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        setText(mBuilder);
    }

}
