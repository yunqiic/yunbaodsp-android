package com.yunbao.common.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
public class TabButton extends LinearLayout {

    private Context mContext;
    private float mScale;
    private Drawable mSelectetDrawable;
    private Drawable mUnSelectedDrawable;
    private String mTip;
    private int mIconSize;
    private int mTextSize;
    private int mCheckedTextColor;
    private int mUnCheckedTextColor;
    private boolean mChecked;
    private ImageView mImg;
    private TextView mText;

    public TabButton(Context context) {
        this(context, null);
    }

    public TabButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mScale = context.getResources().getDisplayMetrics().density;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TabButton);
        mSelectetDrawable = ta.getDrawable(R.styleable.TabButton_tbn_selected_icon);
        mUnSelectedDrawable = ta.getDrawable(R.styleable.TabButton_tbn_unselected_icon);
        mTip = ta.getString(R.styleable.TabButton_tbn_tip);
        mIconSize = (int) ta.getDimension(R.styleable.TabButton_tbn_icon_size, 0);
        mTextSize = (int) ta.getDimension(R.styleable.TabButton_tbn_text_size, 0);
        mCheckedTextColor = (int) ta.getColor(R.styleable.TabButton_tbn_text_color_checked, 0);
        mUnCheckedTextColor = (int) ta.getColor(R.styleable.TabButton_tbn_text_color_unchecked, 0);
        mChecked = ta.getBoolean(R.styleable.TabButton_tbn_checked, false);
        ta.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        mImg = new ImageView(mContext);
        LayoutParams params1 = new LayoutParams(mIconSize, mIconSize);
        params1.setMargins(0, dp2px(3), 0, 0);
        mImg.setLayoutParams(params1);
        mText = new TextView(mContext);
        LayoutParams params2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params2.setMargins(0, dp2px(-2), 0, 0);
        mText.setLayoutParams(params2);
        mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        mText.setText(mTip);
        if (mChecked) {
            mImg.setImageDrawable(mSelectetDrawable);
            mText.setTextColor(mCheckedTextColor);
        } else {
            mImg.setImageDrawable(mUnSelectedDrawable);
            mText.setTextColor(mUnCheckedTextColor);
        }
        addView(mImg);
        addView(mText);
    }


    public void setText(String text) {
        if (mText != null) {
            mText.setText(text);
        }
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        if (checked) {
            mImg.setImageDrawable(mSelectetDrawable);
            mText.setTextColor(mCheckedTextColor);
        } else {
            mImg.setImageDrawable(mUnSelectedDrawable);
            mText.setTextColor(mUnCheckedTextColor);
        }
    }

    private int dp2px(int dpVal) {
        return (int) (mScale * dpVal + 0.5f);
    }

}
