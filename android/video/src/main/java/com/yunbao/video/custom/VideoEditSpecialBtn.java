package com.yunbao.video.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.video.R;

import pl.droidsonroids.gif.GifImageView;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class VideoEditSpecialBtn extends FrameLayout {

    private Context mContext;
    private float mScale;
    private Drawable mGifDrawable;
    private Drawable mCoverDrawable;
    private Drawable mCheckedDrawable;
    private ImageView mCheckedImage;
    private boolean mChecked;
    private String mText;

    public VideoEditSpecialBtn(@NonNull Context context) {
        this(context, null);
    }

    public VideoEditSpecialBtn(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoEditSpecialBtn(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mScale = context.getResources().getDisplayMetrics().density;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VideoEditSpecialBtn);
        mGifDrawable = ta.getDrawable(R.styleable.VideoEditSpecialBtn_vesb_gif_src);
        mCoverDrawable = ta.getDrawable(R.styleable.VideoEditSpecialBtn_vesb_cover_src);
        mCheckedDrawable = ta.getDrawable(R.styleable.VideoEditSpecialBtn_vesb_checked_src);
        mChecked = ta.getBoolean(R.styleable.VideoEditSpecialBtn_vesb_checked, false);
        mText = ta.getString(R.styleable.VideoEditSpecialBtn_vesb_text);
        ta.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        GifImageView gifImageView = new GifImageView(mContext);
        int dp60 = dp2px(60);
        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(dp60, dp60);
        params1.gravity = Gravity.CENTER;
        gifImageView.setLayoutParams(params1);
        gifImageView.setImageDrawable(mGifDrawable);
        addView(gifImageView);

        View cover = new View(mContext);
        int dp54 = dp2px(54);
        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(dp54, dp54);
        params2.gravity = Gravity.CENTER;
        cover.setLayoutParams(params2);
        cover.setBackground(mCoverDrawable);
        addView(cover);

        mCheckedImage = new ImageView(mContext);
        FrameLayout.LayoutParams params3 = new FrameLayout.LayoutParams(dp60, dp60);
        params3.gravity = Gravity.CENTER;
        mCheckedImage.setLayoutParams(params3);
        if (mChecked) {
            mCheckedImage.setImageDrawable(mCheckedDrawable);
        }
        addView(mCheckedImage);

        TextView textView = new TextView(mContext);
        FrameLayout.LayoutParams params4 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(20));
        params4.gravity = Gravity.BOTTOM;
        textView.setLayoutParams(params4);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(13);
        textView.setTextColor(0xffffffff);
        textView.setText(mText);
        addView(textView);
    }

    private int dp2px(int dpVal) {
        return (int) (mScale * dpVal + 0.5f);
    }

    public void setChecked(boolean checked) {
        if (mCheckedImage != null) {
            mCheckedImage.setImageDrawable(checked ? mCheckedDrawable : null);
        }
    }
}
