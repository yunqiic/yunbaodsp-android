package com.yunbao.video.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;

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
public class VideoLikeHeartView extends AppCompatImageView {

    private ValueAnimator mAnimator;
    private float mScale;
    private int mWidth;
    private int mOffsetX;
    private int mOffsetY;
    private boolean mShowing;

    public VideoLikeHeartView(Context context) {
        this(context, null);
    }

    public VideoLikeHeartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoLikeHeartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScale = context.getResources().getDisplayMetrics().density;
        mWidth = dp2px(60);
        mOffsetX = mWidth / 2;
        mOffsetY = dp2px(90);
        mAnimator = ValueAnimator.ofFloat(1, 3.5f);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                float f = animation.getAnimatedFraction();
                setAlpha(1 - f);
                setScaleX(v);
                setScaleY(v);
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mShowing = false;
            }
        });
        mAnimator.setDuration(800);
    }


    public void show(ViewGroup container, float x, float y,int rotate) {
        if (mShowing) {
            return;
        }
        mShowing = true;
        ViewParent parent = getParent();
        if (parent == null) {
            setLayoutParams(new ViewGroup.LayoutParams(mWidth, mWidth));
            setImageResource(R.mipmap.icon_video_zan_12);
            container.addView(this);
        }
        setX(x - mOffsetX);
        setY(y - mOffsetY);
        setRotation(rotate);
        if (mAnimator != null) {
            mAnimator.start();
        }
    }

    public void stopAnim() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        setAlpha(0f);
        mShowing = false;
    }

    public boolean isShowing() {
        return mShowing;
    }

    public void release() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }

    public int dp2px(int dpVal) {
        return (int) (mScale * dpVal + 0.5f);
    }
}
