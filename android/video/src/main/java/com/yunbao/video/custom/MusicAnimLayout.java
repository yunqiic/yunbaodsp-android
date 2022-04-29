package com.yunbao.video.custom;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.makeramen.roundedimageview.RoundedImageView;
import com.yunbao.common.glide.ImgLoader;
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

public class MusicAnimLayout extends RelativeLayout {

    private static final int POINT_COUNT = 60;
    private static final int POINT_SETP_DELAY = 35;
    private static final int IMG_COUNT = 2;
    private Context mContext;
    private float mScale;
    private boolean mAnimStarted;
    private RoundedImageView mRoundedImageView;
    private ObjectAnimator mAnimator;
    private Drawable mDrawable1;
    private Drawable mDrawable2;
    private AnimBean[] mAnimBeanArray;
    private Handler mHandler;

    public MusicAnimLayout(Context context) {
        this(context, null);
    }

    public MusicAnimLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicAnimLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mScale = context.getResources().getDisplayMetrics().density;
        mDrawable1 = ContextCompat.getDrawable(context, R.mipmap.icon_video_music_1);
        mDrawable2 = ContextCompat.getDrawable(context, R.mipmap.icon_video_music_2);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                for (int i = 0; i < IMG_COUNT; i++) {
                    mAnimBeanArray[i].next();
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(0, POINT_SETP_DELAY);
                }
            }
        };
        init();
    }

    private void init() {
        RoundedImageView roundedImageView = new RoundedImageView(mContext);
        int dp40 = dp2px(40);
        LayoutParams params = new LayoutParams(dp40, dp40);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        int dp15 = dp2px(15);
        params.setMargins(0, 0, dp15, dp15);
        roundedImageView.setLayoutParams(params);
        roundedImageView.setBackgroundResource(R.mipmap.icon_video_music_anim_bg);
        roundedImageView.setOval(true);
        roundedImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int p = dp2px(8);
        roundedImageView.setPadding(p, p, p, p);
        addView(roundedImageView);
        mRoundedImageView = roundedImageView;

        mAnimator = ObjectAnimator.ofFloat(roundedImageView, "rotation", 0, 359f);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setDuration(5000);
        mAnimator.setRepeatCount(-1);

        Path path1 = new Path();
        path1.lineTo(0, 0);
        path1.moveTo(dp2px(65), dp2px(64));
        path1.rCubicTo(-dp2px(60), 0, -dp2px(30), -dp2px(50), -dp2px(58), -dp2px(58));
        Path path2 = new Path();
        path2.lineTo(0, 0);
        path2.moveTo(dp2px(65), dp2px(64));
        path2.rCubicTo(-dp2px(60), 0, -dp2px(40), -dp2px(50), -dp2px(30), -dp2px(58));

        PathMeasure pathMeasure1 = new PathMeasure(path1, false);
        PathMeasure pathMeasure2 = new PathMeasure(path2, false);

        mAnimBeanArray = new AnimBean[IMG_COUNT];
        for (int i = 0; i < IMG_COUNT; i++) {
            if (i % 2 == 0) {
                mAnimBeanArray[i] = new AnimBean(i, pathMeasure1, 1, mDrawable1);
            } else {
                mAnimBeanArray[i] = new AnimBean(i, pathMeasure2, -1, mDrawable2);
            }
        }
    }


    private int dp2px(int dpVal) {
        return (int) (mScale * dpVal + 0.5f);
    }


    public void startAnim() {
        if (!mAnimStarted) {
            mAnimStarted = true;
            if (mAnimator != null) {
                mAnimator.start();
            }
            if (mHandler != null) {
                mHandler.sendEmptyMessage(0);
            }
        }
    }


    public void setImageUrl(String url) {
        if (mRoundedImageView != null) {
            ImgLoader.display(mContext, url, mRoundedImageView);
        }
    }

    public void recycle() {
        cancelAnim();
        for (AnimBean bean : mAnimBeanArray) {
            bean.reset();
        }
    }

    public void release() {
        cancelAnim();
        mAnimator = null;
        mHandler = null;
        for (int i = 0; i < IMG_COUNT; i++) {
            mAnimBeanArray[i] = null;
        }
        mAnimBeanArray = null;
    }

    private void cancelAnim() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mAnimStarted = false;
    }

    class AnimBean {

        private int mIndex;
        private PathMeasure mPathMeasure;
        private float mPathLength;
        private ImageView mImageView;
        private int mDirection;
        private float mPointIndex;
        private int mPointWait;
        private float[] mPos;
        private int mOffset;

        public AnimBean(int index, PathMeasure pathMeasure, int direction, Drawable drawable) {
            mIndex = index;
            mPathMeasure = pathMeasure;
            mPathLength = pathMeasure.getLength();
            mDirection = direction;
            mPos = new float[2];
            int width = dp2px(12);
            mOffset = width / 2;
            mImageView = new ImageView(mContext);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(width, width));
            mImageView.setImageDrawable(drawable);
            addView(mImageView);
            reset();
        }


        public void next() {
            if (mPointWait > 0) {
                mPointWait--;
            } else {
                if (mPointIndex < POINT_COUNT) {
                    mPointIndex++;
                } else {
                    mPointIndex = 1;
                }
                float rate = mPointIndex / POINT_COUNT;
                mPathMeasure.getPosTan(mPathLength * rate, mPos, null);
                mImageView.setX(mPos[0] - mOffset);
                mImageView.setY(mPos[1] - mOffset);
                if (mPointIndex == 1) {
                    mImageView.setAlpha(1f);
                }
                if (mPointIndex <= 10) {
                    float r = mPointIndex / 10f;
                    mImageView.setScaleX(0.2f + r);
                    mImageView.setScaleY(0.2f + r);
                    mImageView.setRotation(mDirection * 30 * r);
                } else if (mPointIndex > 30) {
                    float r = (mPointIndex - 30) / 30f;
                    mImageView.setAlpha(1 - r);
                    mImageView.setRotation(mDirection * 30 * (1 - r));
                    float r2 = 1.2f + r / 2;
                    mImageView.setScaleX(r2);
                    mImageView.setScaleY(r2);
                }
            }

        }

        public void reset() {
            mImageView.setAlpha(0f);
            mPointWait = POINT_COUNT / IMG_COUNT * mIndex;
            mPointIndex = 0;
        }

    }


}
