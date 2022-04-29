package com.yunbao.video.presenter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.opensource.svgaplayer.utils.SVGARect;
import com.yunbao.common.bean.LiveReceiveGiftBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.GifCacheUtil;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.MD5Util;
import com.yunbao.common.utils.ScreenDimenUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.video.R;
import com.yunbao.common.views.GiftDrawViewHolder;
import com.yunbao.video.views.GiftViewHolder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import pl.droidsonroids.gif.GifDrawable;
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

public class VideoGiftAnimPresenter {

    private Context mContext;
    private ViewGroup mParent2;
    private ViewGroup mDrawParent;
    private SVGAImageView mSVGAImageView;
    private GifImageView mGifImageView;
    private GifDrawable mGifDrawable;
    private View mGifGiftTipGroup;
    private TextView mGifGiftTip;
    private ObjectAnimator mGifGiftTipShowAnimator;
    private ObjectAnimator mGifGiftTipHideAnimator;
    private GiftViewHolder[] mGiftViewHolders;
    private GiftDrawViewHolder mGiftDrawViewHolder;
    private ConcurrentLinkedQueue<LiveReceiveGiftBean> mQueue;
    private ConcurrentLinkedQueue<LiveReceiveGiftBean> mGifQueue;
    private ConcurrentLinkedQueue<LiveReceiveGiftBean> mDrawGifQueue;
    private Map<String, LiveReceiveGiftBean> mMap;
    private Handler mHandler;
    private MediaController mMediaController;//koral--/android-gif-drawable 这个库用来播放gif动画的
    private static final int WHAT_GIF = -1;
    private static final int WHAT_ANIM = -2;
    private static final int WHAT_DRAW = -3;
    private static final int WHAT_DRAW_FINISH = -4;
    private static final int WHAT_DRAW_END = -5;
    private boolean mShowGif;
    private boolean mShowDrawGif;
    private CommonCallback<File> mDownloadGifCallback;
    private int mDp10;
    private int mDp500;
    private LiveReceiveGiftBean mTempGifGiftBean;
    private String mSendString;
    private SVGAParser mSVGAParser;
    private SVGAParser.ParseCompletion mParseCompletionCallback;
    private long mSvgaPlayTime;
    private Map<String, SoftReference<SVGAVideoEntity>> mSVGAMap;
    private FrameLayout mDrawGiftContainer;
    private List<ImageView> mDrawImgList;
    private Drawable mDrawGiftDrawable;
    private float mDrawGiftOffsetX;
    private float mDrawGiftOffsetY;
    private List<PointF> mDrawGiftPointList;
    private int mDrawCount;
    private int mDrawIndex;
    private ScaleAnimation mDrawImgAnim;
    private ScaleAnimation mDrawEndAnim;

    public VideoGiftAnimPresenter(Context context, View v, GifImageView gifImageView, SVGAImageView svgaImageView) {
        mContext = context;
        mParent2 = (ViewGroup) v.findViewById(R.id.gift_group_1);
        mDrawParent = (ViewGroup) v.findViewById(R.id.gift_group_draw);
        mGifImageView = gifImageView;
        mSVGAImageView = svgaImageView;
        mSVGAImageView.setCallback(new SVGACallback() {
            @Override
            public void onPause() {
            }

            @Override
            public void onFinished() {
                long diffTime = 4000 - (System.currentTimeMillis() - mSvgaPlayTime);
                if (diffTime < 0) {
                    diffTime = 0;
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(WHAT_GIF, diffTime);
                }
            }

            @Override
            public void onRepeat() {

            }

            @Override
            public void onStep(int i, double v) {

            }
        });
        mGifGiftTipGroup = v.findViewById(R.id.gif_gift_tip_group);
        mGifGiftTip = (TextView) v.findViewById(R.id.gif_gift_tip);
        mDrawGiftContainer = v.findViewById(R.id.draw_gift_container);
        mDrawGiftOffsetX = ScreenDimenUtil.getInstance().getScreenWdith() / 20f;
        mDrawImgAnim = new ScaleAnimation(1f, 1.5f, 1f, 1.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mDrawImgAnim.setDuration(100);
        mDrawEndAnim = new ScaleAnimation(1f, 2.5f, 1f, 2.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mDrawEndAnim.setDuration(300);

        mDp500 = DpUtil.dp2px(500);
        mGifGiftTipShowAnimator = ObjectAnimator.ofFloat(mGifGiftTipGroup, "translationX", mDp500, 0);
        mGifGiftTipShowAnimator.setDuration(1000);
        mGifGiftTipShowAnimator.setInterpolator(new LinearInterpolator());
        mGifGiftTipShowAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(WHAT_ANIM, 2000);
                }
            }
        });
        mDp10 = DpUtil.dp2px(10);
        mGifGiftTipHideAnimator = ObjectAnimator.ofFloat(mGifGiftTipGroup, "translationX", 0);
        mGifGiftTipHideAnimator.setDuration(800);
        mGifGiftTipHideAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mGifGiftTipHideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mGifGiftTipGroup.setAlpha(1 - animation.getAnimatedFraction());
            }
        });
        mSendString = WordUtil.getString(R.string.live_send_gift_3);
        mGiftViewHolders = new GiftViewHolder[2];
        mGiftViewHolders[0] = new GiftViewHolder(context, (ViewGroup) v.findViewById(R.id.gift_group_2));
        mGiftViewHolders[0].addToParent();
        mQueue = new ConcurrentLinkedQueue<>();
        mGifQueue = new ConcurrentLinkedQueue<>();
        mDrawGifQueue = new ConcurrentLinkedQueue<>();
        mMap = new HashMap<>();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == WHAT_GIF) {
                    mShowGif = false;
                    if (mGifImageView != null) {
                        mGifImageView.setImageDrawable(null);
                    }
                    if (mGifDrawable != null && !mGifDrawable.isRecycled()) {
                        mGifDrawable.stop();
                        mGifDrawable.recycle();
                    }
                    LiveReceiveGiftBean bean = mGifQueue.poll();
                    if (bean != null) {
                        showGifGift(bean);
                    }
                } else if (msg.what == WHAT_ANIM) {
                    mGifGiftTipHideAnimator.setFloatValues(0, -mDp10 - mGifGiftTipGroup.getWidth());
                    mGifGiftTipHideAnimator.start();
                } else if (msg.what == WHAT_DRAW) {
                    nextDrawGift();
                } else if (msg.what == WHAT_DRAW_FINISH) {
                    if (mDrawGiftContainer != null) {
                        mDrawGiftContainer.startAnimation(mDrawEndAnim);
                    }
                    if (mGiftDrawViewHolder != null) {
                        mGiftDrawViewHolder.hide();
                    }
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(WHAT_DRAW_END, 500);
                    }
                } else if (msg.what == WHAT_DRAW_END) {
                    clearDrawGift();
                    mShowDrawGif = false;
                    if (mDrawGifQueue != null) {
                        LiveReceiveGiftBean bean = mDrawGifQueue.poll();
                        if (bean != null) {
                            showDrawGift(bean);
                        }
                    }
                } else {
                    GiftViewHolder vh = mGiftViewHolders[msg.what];
                    if (vh != null) {
                        LiveReceiveGiftBean bean = mQueue.poll();
                        if (bean != null) {
                            mMap.remove(bean.getKey());
                            vh.show(bean, false);
                            resetTimeCountDown(msg.what);
                        } else {
                            vh.hide();
                        }
                    }
                }
            }
        };
        mDownloadGifCallback = new CommonCallback<File>() {
            @Override
            public void callback(File file) {
                if (file != null) {
                    playHaoHuaGift(file);
                } else {
                    mShowGif = false;
                }
            }
        };
    }

    public void showGiftAnim(LiveReceiveGiftBean bean) {
        if (bean.getType() == 1) {//豪华礼物
            showGifGift(bean);
        } else if (bean.getType() == 2) {//手绘礼物
            showDrawGift(bean);
        } else {
            showNormalGift(bean);//普通礼物
        }
    }

    /**
     * 显示gif礼物
     */
    private void showGifGift(LiveReceiveGiftBean bean) {
        String url = bean.getGifUrl();
        L.e("gif礼物----->" + bean.getGiftName() + "----->" + url);
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (mShowGif) {
            if (mGifQueue != null) {
                mGifQueue.offer(bean);
            }
        } else {
            mShowGif = true;
            mTempGifGiftBean = bean;
            if (!url.endsWith(".gif") && !url.endsWith(".svga")) {
                ImgLoader.displayDrawable(mContext, url, new ImgLoader.DrawableCallback2() {
                    @Override
                    public void onLoadSuccess(Drawable drawable) {
                        resizeGifImageView(drawable);
                        mGifImageView.setImageDrawable(drawable);
                        mGifGiftTip.setText(mTempGifGiftBean.getUserNiceName() + "  " + mSendString + mTempGifGiftBean.getGiftName());
                        mGifGiftTipGroup.setAlpha(1f);
                        mGifGiftTipShowAnimator.start();
                        if (mHandler != null) {
                            mHandler.sendEmptyMessageDelayed(WHAT_GIF, 4000);
                        }
                    }

                    @Override
                    public void onLoadFailed() {
                        if (mHandler != null) {
                            mHandler.sendEmptyMessage(WHAT_GIF);
                        }
                    }
                });
            } else {
                GifCacheUtil.getFile(MD5Util.getMD5(url), url, mDownloadGifCallback);
            }
        }
    }

    /**
     * 调整mGifImageView的大小
     */
    private void resizeGifImageView(Drawable drawable) {
        float w = drawable.getIntrinsicWidth();
        float h = drawable.getIntrinsicHeight();
        ViewGroup.LayoutParams params = mGifImageView.getLayoutParams();
        params.height = (int) (mGifImageView.getWidth() * h / w);
        mGifImageView.setLayoutParams(params);
    }

    /**
     * 调整mSVGAImageView的大小
     */
    private void resizeSvgaImageView(double w, double h) {
        ViewGroup.LayoutParams params = mSVGAImageView.getLayoutParams();
        params.height = (int) (mSVGAImageView.getWidth() * h / w);
        mSVGAImageView.setLayoutParams(params);
    }

    /**
     * 播放豪华礼物
     */
    private void playHaoHuaGift(File file) {
        if (mTempGifGiftBean.getGifType() == 0) {//豪华礼物类型 0是gif  1是svga
            if (mTempGifGiftBean != null) {
                mGifGiftTip.setText(mTempGifGiftBean.getUserNiceName() + "  " + mSendString + mTempGifGiftBean.getGiftName());
                mGifGiftTipGroup.setAlpha(1f);
                mGifGiftTipShowAnimator.start();
            }
            playGift(file);
        } else {
            SVGAVideoEntity svgaVideoEntity = null;
            if (mSVGAMap != null) {
                SoftReference<SVGAVideoEntity> reference = mSVGAMap.get(mTempGifGiftBean.getGiftId());
                if (reference != null) {
                    svgaVideoEntity = reference.get();
                }
            }
            if (svgaVideoEntity != null) {
                playSVGA(svgaVideoEntity);
            } else {
                decodeSvga(file);
            }
        }
    }

    /**
     * 播放gif
     */
    private void playGift(File file) {
        try {
            mGifDrawable = new GifDrawable(file);
            mGifDrawable.setLoopCount(1);
            resizeGifImageView(mGifDrawable);
            mGifImageView.setImageDrawable(mGifDrawable);
            if (mMediaController == null) {
                mMediaController = new MediaController(mContext);
                mMediaController.setVisibility(View.GONE);
            }
            mMediaController.setMediaPlayer((GifDrawable) mGifImageView.getDrawable());
            mMediaController.setAnchorView(mGifImageView);
            int duration = mGifDrawable.getDuration();
            mMediaController.show(duration);
            if (duration < 4000) {
                duration = 4000;
            }
            if (mHandler != null) {
                mHandler.sendEmptyMessageDelayed(WHAT_GIF, duration);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mShowGif = false;
        }
    }

    /**
     * 播放svga
     */
    private void playSVGA(SVGAVideoEntity svgaVideoEntity) {
        if (mSVGAImageView != null) {
            SVGARect rect = svgaVideoEntity.getVideoSize();
            resizeSvgaImageView(rect.getWidth(), rect.getHeight());
            //SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
            //mSVGAImageView.setImageDrawable(drawable);
            mSVGAImageView.setVideoItem(svgaVideoEntity);
            mSvgaPlayTime = System.currentTimeMillis();
            mSVGAImageView.startAnimation();
            if (mTempGifGiftBean != null) {
                mGifGiftTip.setText(StringUtil.contact(mTempGifGiftBean.getUserNiceName(), "  ", mSendString, mTempGifGiftBean.getGiftName()));
                mGifGiftTipGroup.setAlpha(1f);
                mGifGiftTipShowAnimator.start();
            }
        }
    }

    /**
     * 播放svga
     */
    private void decodeSvga(File file) {
        if (mSVGAParser == null) {
            mSVGAParser = new SVGAParser(mContext);
        }
        if (mParseCompletionCallback == null) {
            mParseCompletionCallback = new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(SVGAVideoEntity svgaVideoEntity) {
                    if (mSVGAMap == null) {
                        mSVGAMap = new HashMap<>();
                    }
                    if (mTempGifGiftBean != null) {
                        mSVGAMap.put(mTempGifGiftBean.getGiftId(), new SoftReference<>(svgaVideoEntity));
                    }
                    playSVGA(svgaVideoEntity);
                }

                @Override
                public void onError() {
                    mShowGif = false;
                }
            };
        }
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            mSVGAParser.decodeFromInputStream(bis, file.getAbsolutePath(), mParseCompletionCallback, true);
        } catch (Exception e) {
            e.printStackTrace();
            mShowGif = false;
        }
    }

    /**
     * 显示普通礼物
     */
    private void showNormalGift(LiveReceiveGiftBean bean) {
        if (mGiftViewHolders[0].isIdle()) {
            if (mGiftViewHolders[1] != null && mGiftViewHolders[1].isSameGift(bean)) {
                mGiftViewHolders[1].show(bean, true);
                resetTimeCountDown(1);
                return;
            }
            mGiftViewHolders[0].show(bean, false);
            resetTimeCountDown(0);
            return;
        }
        if (mGiftViewHolders[0].isSameGift(bean)) {
            mGiftViewHolders[0].show(bean, true);
            resetTimeCountDown(0);
            return;
        }
        if (mGiftViewHolders[1] == null) {
            mGiftViewHolders[1] = new GiftViewHolder(mContext, mParent2);
            mGiftViewHolders[1].addToParent();
        }
        if (mGiftViewHolders[1].isIdle()) {
            mGiftViewHolders[1].show(bean, false);
            resetTimeCountDown(1);
            return;
        }
        if (mGiftViewHolders[1].isSameGift(bean)) {
            mGiftViewHolders[1].show(bean, true);
            resetTimeCountDown(1);
            return;
        }
        String key = bean.getKey();
        if (!mMap.containsKey(key)) {
            mMap.put(key, bean);
            mQueue.offer(bean);
        } else {
            LiveReceiveGiftBean bean1 = mMap.get(key);
            bean1.setLianCount(bean1.getLianCount() + 1);
        }
    }

    private void resetTimeCountDown(int index) {
        if (mHandler != null) {
            mHandler.removeMessages(index);
            mHandler.sendEmptyMessageDelayed(index, 5000);
        }
    }


    public void cancelAllAnim() {
        clearAnim();
        cancelNormalGiftAnim();
        if (mGifGiftTipGroup != null && mGifGiftTipGroup.getTranslationX() != mDp500) {
            mGifGiftTipGroup.setTranslationX(mDp500);
        }
    }

    private void cancelNormalGiftAnim() {
        if (mGiftViewHolders[0] != null) {
            mGiftViewHolders[0].cancelAnimAndHide();
        }
        if (mGiftViewHolders[1] != null) {
            mGiftViewHolders[1].cancelAnimAndHide();
        }
        if (mGiftDrawViewHolder != null) {
            mGiftDrawViewHolder.cancelAnimAndHide();
        }
    }


    private void clearAnim() {
        mShowGif = false;
        mShowDrawGif = false;
        CommonHttpUtil.cancel(CommonHttpConsts.DOWNLOAD_GIF);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mGifGiftTipShowAnimator != null) {
            mGifGiftTipShowAnimator.cancel();
        }
        if (mGifGiftTipHideAnimator != null) {
            mGifGiftTipHideAnimator.cancel();
        }
        if (mQueue != null) {
            mQueue.clear();
        }
        if (mGifQueue != null) {
            mGifQueue.clear();
        }
        if (mDrawGifQueue != null) {
            mDrawGifQueue.clear();
        }
        if (mMap != null) {
            mMap.clear();
        }
        if (mMediaController != null) {
            mMediaController.hide();
            mMediaController.setAnchorView(null);
        }
        if (mGifImageView != null) {
            mGifImageView.setImageDrawable(null);
        }
        if (mGifDrawable != null && !mGifDrawable.isRecycled()) {
            mGifDrawable.stop();
            mGifDrawable.recycle();
            mGifDrawable = null;
        }
        if (mSVGAImageView != null) {
            mSVGAImageView.stopAnimation(true);
        }
        if (mSVGAMap != null) {
            mSVGAMap.clear();
        }
        if (mDrawGiftContainer != null) {
            mDrawGiftContainer.clearAnimation();
        }
        clearDrawGift();

    }

    public void release() {
        clearAnim();
        if (mGiftViewHolders[0] != null) {
            mGiftViewHolders[0].release();
        }
        if (mGiftViewHolders[1] != null) {
            mGiftViewHolders[1].release();
        }
        if (mGiftDrawViewHolder != null) {
            mGiftDrawViewHolder.release();
        }
        if (mSVGAImageView != null) {
            mSVGAImageView.setCallback(null);
        }

        mSVGAImageView = null;
        mDownloadGifCallback = null;
        mHandler = null;

    }


    private void showDrawGift(LiveReceiveGiftBean bean) {
        if (bean == null) {
            return;
        }
        if (mShowDrawGif) {
            if (mDrawGifQueue != null) {
                mDrawGifQueue.offer(bean);
            }
            return;
        }
        mShowDrawGif = true;
        mDrawGiftPointList = bean.getPointList();
        if (mDrawGiftPointList == null || mDrawGiftPointList.size() <= 0) {
            return;
        }
        if (mGiftDrawViewHolder == null) {
            mGiftDrawViewHolder = new GiftDrawViewHolder(mContext, mDrawParent);
            mGiftDrawViewHolder.addToParent();
        }
        mGiftDrawViewHolder.show(bean);
        if (mDrawGiftContainer != null) {
            ViewGroup.LayoutParams lp = mDrawGiftContainer.getLayoutParams();
            lp.width = (int) bean.getDrawWidth();
            lp.height = (int) bean.getDrawHeight();
            mDrawGiftContainer.requestLayout();
        }
        ImgLoader.displayDrawable(mContext, bean.getGiftIcon(), new ImgLoader.DrawableCallback() {
            @Override
            public void callback(Drawable drawable) {
                if (drawable == null) {
                    return;
                }
                mDrawGiftDrawable = drawable;
                if (mDrawImgList == null) {
                    mDrawImgList = new ArrayList<>();
                }
                mDrawCount = mDrawGiftPointList.size();
                int cha = mDrawCount - mDrawImgList.size();
                if (cha > 0) {
                    for (int i = 0; i < cha; i++) {
                        ImageView imageView = new ImageView(mContext);
                        mDrawGiftContainer.addView(imageView);
                        mDrawImgList.add(imageView);
                    }
                }
                mDrawGiftOffsetY = mDrawGiftOffsetX * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
                for (int i = 0; i < mDrawCount; i++) {
                    ImageView imageView = mDrawImgList.get(i);
                    PointF pointF = mDrawGiftPointList.get(i);
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) imageView.getLayoutParams();
                    if (layoutParams == null) {
                        layoutParams = new FrameLayout.LayoutParams((int) (mDrawGiftOffsetX * 2), (int) (mDrawGiftOffsetY * 2));
                        layoutParams.leftMargin = (int) (pointF.x - mDrawGiftOffsetX);
                        layoutParams.topMargin = (int) (pointF.y - mDrawGiftOffsetY);
                        imageView.setLayoutParams(layoutParams);
                    } else {
                        layoutParams.width = (int) (mDrawGiftOffsetX * 2);
                        layoutParams.height = (int) (mDrawGiftOffsetY * 2);
                        layoutParams.leftMargin = (int) (pointF.x - mDrawGiftOffsetX);
                        layoutParams.topMargin = (int) (pointF.y - mDrawGiftOffsetY);
                        imageView.requestLayout();
                    }
                }
                mDrawIndex = 0;
                nextDrawGift();
            }
        });
    }

    private void nextDrawGift() {
        if (mDrawIndex < mDrawCount) {
            ImageView imageView = mDrawImgList.get(mDrawIndex);
            imageView.setImageDrawable(mDrawGiftDrawable);
            imageView.startAnimation(mDrawImgAnim);
            mDrawIndex++;
            if (mDrawIndex < mDrawCount) {
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(WHAT_DRAW, 200);
                }
            } else {
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(WHAT_DRAW_FINISH, 500);
                }
            }
        }
    }

    private void clearDrawGift() {
        if (mDrawImgList != null && mDrawImgList.size() > 0) {
            for (ImageView imageView : mDrawImgList) {
                imageView.clearAnimation();
                imageView.setImageDrawable(null);
            }
        }
    }
}
