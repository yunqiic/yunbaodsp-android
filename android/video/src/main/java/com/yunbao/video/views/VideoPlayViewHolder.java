package com.yunbao.video.views;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.bean.VideoBean;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.video.R;
import com.yunbao.video.custom.VideoLikeHeartView;
import com.yunbao.video.http.VideoHttpConsts;
import com.yunbao.video.http.VideoHttpUtil;
import com.yunbao.video.presenter.CheckVideoPlayPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class VideoPlayViewHolder extends AbsViewHolder implements ITXVodPlayListener, View.OnTouchListener {

    private TXCloudVideoView mTXCloudVideoView;
    //    private View mVideoCover;
    private TXVodPlayer mPlayer;
    private boolean mPaused;//生命周期暂停
    private boolean mClickPaused;//点击暂停
    private boolean mPassivePaused;//被动暂停
    private ActionListener mActionListener;
    private View mPlayBtn;
    private ObjectAnimator mPlayBtnAnimator;//暂停按钮的动画
    private VideoBean mVideoBean;
    private Handler mHandler;
    private boolean mHasClicked;//是否点击过一次
    private FrameLayout mHeartContainer;
    private List<VideoLikeHeartView> mViewList;
    private Random mRandom;
    private String mCachePath;
    private TXVodPlayConfig mTXVodPlayConfig;
    private long mPlayTotalTime;
    private CheckVideoPlayPresenter mCheckVideoPlayPresenter;
    private float mDuration;
    private boolean mShowFirstFrame = false;
    private Handler mFirstFrameHander;

    public VideoPlayViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_video_play;
    }

    @Override
    public void init() {
        mRandom = new Random();
        mCachePath = mContext.getCacheDir().getAbsolutePath();
        mHeartContainer = (FrameLayout) findViewById(R.id.heart_container);
        mViewList = new ArrayList<>();
        mTXCloudVideoView = (TXCloudVideoView) findViewById(R.id.video_view);
        mTXCloudVideoView.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
        mPlayer = new TXVodPlayer(mContext);
        mTXVodPlayConfig = new TXVodPlayConfig();
        mTXVodPlayConfig.setMaxCacheItems(15);
        mTXVodPlayConfig.setProgressInterval(200);
        mTXVodPlayConfig.setHeaders(CommonAppConfig.HEADER);
        mPlayer.setConfig(mTXVodPlayConfig);
        mPlayer.setAutoPlay(true);
        mPlayer.setVodListener(this);
        mPlayer.setLoop(true);
        mPlayer.setPlayerView(mTXCloudVideoView);
        findViewById(R.id.rootView).setOnTouchListener(this);
//        mVideoCover = findViewById(R.id.video_cover);
        //mPlayBtn = findViewById(R.id.btn_play);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mHasClicked = false;
                L.e("播放器", "单击------->");
                clickCheckVideo();
            }
        };
        mFirstFrameHander = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (mTXCloudVideoView != null) {
                    mTXCloudVideoView.setTranslationX(0);
                }
                if (mActionListener != null) {
                    mActionListener.onFirstFrame();
                }
            }
        };
    }

    /**
     * 播放器事件回调
     */
    @Override
    public void onPlayEvent(TXVodPlayer txVodPlayer, int e, Bundle bundle) {
        if (e != 2005) {
            L.e("onPlayEvent------> " + e);
        }
        switch (e) {
            case TXLiveConstants.PLAY_EVT_START_VIDEO_DECODER://加载完成，开始播放的回调
            case TXLiveConstants.PLAY_EVT_PLAY_BEGIN://加载完成，开始播放的回调
                if (mActionListener != null) {
                    mActionListener.onPlayBegin();
                }
                break;
            case TXLiveConstants.PLAY_EVT_VOD_LOADING_END://获取到视频播放完毕的回调
                if (mActionListener != null) {
                    mActionListener.onPlayBegin();
                }
                if (mVideoBean != null) {
                    VideoHttpUtil.videoWatchEnd(mVideoBean.getUid(), mVideoBean.getId());

                    mPlayTotalTime = 0;
                }
                break;
            case TXLiveConstants.PLAY_EVT_PLAY_LOADING: //开始加载的回调
                if (mActionListener != null) {
                    mActionListener.onPlayLoading();
                }
                break;
//            case TXLiveConstants.PLAY_EVT_PLAY_END://获取到视频播放完毕的回调
//                if (mVideoBean != null) {
//                    VideoHttpUtil.videoWatchEnd(mVideoBean.getUid(), mVideoBean.getId());
//                }
//                break;
            case TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME://获取到视频首帧回调
                mShowFirstFrame = true;
                break;
            case TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION://获取到视频宽高回调
                onVideoSizeChanged(bundle.getInt("EVT_PARAM1", 0), bundle.getInt("EVT_PARAM2", 0));
                break;
            case TXLiveConstants.PLAY_EVT_PLAY_PROGRESS:
                if (mShowFirstFrame) {
                    mShowFirstFrame = false;
                    mFirstFrameHander.sendEmptyMessageDelayed(0, 300);
                }
                int duration = bundle.getInt(TXLiveConstants.EVT_PLAY_DURATION_MS);
                mDuration = duration / 1000f;
                int progress = bundle.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS);
                mPlayTotalTime = progress / 1000;
//                L.e("VideoPlayViewHolder----duration---> " + duration + "  ----progress---> " + progress);
                if (mActionListener != null) {
                    mActionListener.onPlayProgress(((float) progress) / duration);
                }
                break;
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer txVodPlayer, Bundle bundle) {

    }

    /**
     * 获取到视频宽高回调
     */
    public void onVideoSizeChanged(float videoWidth, float videoHeight) {
        if (mTXCloudVideoView != null && videoWidth > 0 && videoHeight > 0) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTXCloudVideoView.getLayoutParams();
            int targetH = 0;
            if (videoWidth >= videoHeight) {//横屏 9:16=0.5625  videoWidth / videoHeight > 0.5625f
                targetH = (int) (mTXCloudVideoView.getWidth() / videoWidth * videoHeight);
            } else {
                targetH = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            if (targetH != params.height) {
                params.height = targetH;
                mTXCloudVideoView.requestLayout();
            }
//            if (mVideoCover != null && mVideoCover.getVisibility() == View.VISIBLE) {
//                mVideoCover.setVisibility(View.INVISIBLE);
//            }

        }
    }

    public void setPlayBtn(View playBtn) {
        mPlayBtn = playBtn;
        if (mPlayBtnAnimator == null) {
            //暂停按钮动画
            mPlayBtnAnimator = ObjectAnimator.ofPropertyValuesHolder(mPlayBtn,
                    PropertyValuesHolder.ofFloat("scaleX", 4f, 0.8f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 4f, 0.8f, 1f),
                    PropertyValuesHolder.ofFloat("alpha", 0f, 1f));
            mPlayBtnAnimator.setDuration(150);
            mPlayBtnAnimator.setInterpolator(new AccelerateInterpolator());
        }
    }


    public void setLoginChange(boolean isLoginChange) {
        if (isLoginChange) {
            cannotPlay();
        }
    }

    /**
     * 开始播放
     */
    public void startPlay() {
        mPlayTotalTime = 0;
        mClickPaused = false;
        stopHeartAnim();
        hidePlayBtn();
        if (mVideoBean == null) {
            return;
        }
        String url = mVideoBean.getHref();
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (mTXVodPlayConfig == null) {
            mTXVodPlayConfig = new TXVodPlayConfig();
            mTXVodPlayConfig.setMaxCacheItems(15);
            mTXVodPlayConfig.setProgressInterval(200);
            mTXVodPlayConfig.setHeaders(CommonAppConfig.HEADER);
        }
        if (url.endsWith(".m3u8")) {
            mTXVodPlayConfig.setCacheFolderPath(null);
        } else {
            mTXVodPlayConfig.setCacheFolderPath(mCachePath);
        }
        if (mPlayer != null) {
            mPlayer.setConfig(mTXVodPlayConfig);
            mPlayer.setMute(false);
            int i = mPlayer.startPlay(url);
        }
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        if (mFirstFrameHander != null) {
            mFirstFrameHander.removeCallbacksAndMessages(null);
        }
        mShowFirstFrame = false;
//        if (mVideoCover != null && mVideoCover.getVisibility() != View.VISIBLE) {
//            mVideoCover.setVisibility(View.VISIBLE);
//        }
        mTXCloudVideoView.setTranslationX(30000);
        if (mPlayer != null) {
            mPlayer.stopPlay(false);
        }

    }

//    /**
//     * 循环播放
//     */
//    private void replay() {
//        if (mPlayTotalTime > 0 && mVideoBean != null) {
//            VideoHttpUtil.videoWatchDuration(mVideoBean.getUid(), String.valueOf(mPlayTotalTime));
//        }
//        mPlayTotalTime = 0;
//        if (mPlayer != null) {
//            mPlayer.seek(0);
//            mPlayer.resume();
//        }
//    }


    public void seekPlay(int progress) {
        if (mPlayer != null) {
            mPlayer.seek(progress / 100f * mDuration);
            if (mClickPaused) {
                mPlayer.resume();
                mClickPaused = false;
                hidePlayBtn();
            }
        }
    }


    public void release() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        if (mFirstFrameHander != null) {
            mFirstFrameHander.removeCallbacksAndMessages(null);
        }
        mFirstFrameHander = null;
        VideoHttpUtil.cancel(VideoHttpConsts.VIDEO_WATCH_START);
        VideoHttpUtil.cancel(VideoHttpConsts.VIDEO_WATCH_END);
        VideoHttpUtil.cancel(VideoHttpConsts.VIDEO_WATCH_DURATION);
        VideoHttpUtil.cancel(VideoHttpConsts.GET_VIDEO);
        stopHeartAnim();
        if (mPlayer != null) {
            mPlayer.stopPlay(false);
            mPlayer.setPlayListener(null);
        }
        mPlayer = null;
        mActionListener = null;
    }

    /**
     * 生命周期暂停
     */
    public void pausePlay() {
        mPaused = true;
        if (!mClickPaused && !mPassivePaused && mPlayer != null) {
            mPlayer.pause();
        }
    }

    /**
     * 生命周期恢复
     */
    public void resumePlay() {
        if (mPaused) {
            if (!mClickPaused && !mPassivePaused && mPlayer != null) {
                mPlayer.resume();
            }
        }
        mPaused = false;
    }

    /**
     * 显示开始播放按钮
     */
    private void showPlayBtn() {
        if (mPlayBtn != null && mPlayBtn.getVisibility() != View.VISIBLE) {
            mPlayBtn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏开始播放按钮
     */
    private void hidePlayBtn() {
        if (mPlayBtn != null && mPlayBtn.getVisibility() == View.VISIBLE) {
            mPlayBtn.setVisibility(View.INVISIBLE);
        }
    }


    public void checkVideoPlay(final VideoBean videoBean, final CommonCallback<VideoBean> callback) {
        hidePlayBtn();
        if (videoBean == null) {
            return;
        }
        mVideoBean = videoBean;
        if (mCheckVideoPlayPresenter == null) {
            mCheckVideoPlayPresenter = new CheckVideoPlayPresenter(mContext);
            mCheckVideoPlayPresenter.setCheckVideoCallBack(new CheckVideoPlayPresenter.OnCheckVideoCallBack() {
                @Override
                public void onLoadSuccess(int code, int type, VideoBean videoBean) {
                    if (type == CheckVideoPlayPresenter.PLAY_CHECK) {
                        if (code == 0) {
                            startPlay();
                            if (callback != null) {
                                callback.callback(videoBean);
                            }
                        } else {
                            cannotPlay();
                        }
                    } else if (type == CheckVideoPlayPresenter.CLICK_CHECK) {
                        if (code == 0) {
                            clickTogglePlay();
                        }
                    }
                }

            });
        }
        mCheckVideoPlayPresenter.checkVideo(mVideoBean, CheckVideoPlayPresenter.PLAY_CHECK);
    }

    private void clickCheckVideo() {
        if (mVideoBean == null) {
            return;
        }
        if (!mClickPaused) {
            clickTogglePlay();
            return;
        }
        if (mCheckVideoPlayPresenter != null) {
            mCheckVideoPlayPresenter.checkVideo(mVideoBean, CheckVideoPlayPresenter.CLICK_CHECK);
        }
    }


    private void cannotPlay() {
        mClickPaused = true;
        showPlayBtn();
    }

    /**
     * 点击切换播放和暂停
     */
    private void clickTogglePlay() {
        if (mPlayer != null) {
            if (mClickPaused) {
                mPlayer.resume();
            } else {
                mPlayer.pause();
            }
        }
        mClickPaused = !mClickPaused;
        if (mClickPaused) {
            showPlayBtn();
            if (mPlayBtnAnimator != null) {
                mPlayBtnAnimator.start();
            }
        } else {
            hidePlayBtn();
        }
    }


    /**
     * 让播放器静音
     */
    public void setMute(boolean mute) {
//        if (mPlayer != null) {
//            mPlayer.setMute(mute);
//        }
        if (mPassivePaused || mClickPaused) {
            return;
        }
        if (mPlayer != null) {
            if (mute) {
                mPlayer.pause();
            } else {
                mPlayer.resume();
            }
        }
    }


    /**
     * 被动暂停
     */
    public void passivePause() {
        mPassivePaused = true;
        if (mPlayer != null) {
            mPlayer.pause();
        }

    }

    /**
     * 被动恢复
     */
    public void passiveResume() {
        mPassivePaused = false;
        if (mPaused) {
            return;
        }
        if (mClickPaused) {
            return;
        }
        if (mPlayer != null) {
            mPlayer.resume();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        int action = e.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
        } else if (action == MotionEvent.ACTION_UP) {
            clickRoot(e);
        } else if (action == MotionEvent.ACTION_CANCEL) {
            mHasClicked = false;
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }
        }
        return true;
    }


    private void clickRoot(MotionEvent e) {
        if (mHasClicked) {//双击
            mHasClicked = false;
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }
            if (CommonAppConfig.getInstance().isLogin()) {
                playHeartAnim(e);
                if (mActionListener != null) {
                    mActionListener.onDoubleClick();
                }
            } else {
                RouteUtil.forwardLogin(mContext);
            }
        } else {//单击
            mHasClicked = true;
            if (mHandler != null) {
                mHandler.sendEmptyMessageDelayed(0, 250);
            }
        }
    }

    /**
     * 双击
     */
    public void playHeartAnim(MotionEvent e) {
        L.e("播放器", "双击------->");
        VideoLikeHeartView heartView = null;
        for (VideoLikeHeartView v : mViewList) {
            if (v != null && !v.isShowing()) {
                heartView = v;
                break;
            }
        }
        if (heartView == null) {
            if (mViewList != null && mViewList.size() < 10) {
                heartView = new VideoLikeHeartView(mContext);
                mViewList.add(heartView);
            }
        }
        if (heartView != null) {
            heartView.show(mHeartContainer, e.getX(), e.getY(), 60 - mRandom.nextInt(120));
        }
    }

    private void stopHeartAnim() {
        for (VideoLikeHeartView v : mViewList) {
            if (v != null) {
                v.stopAnim();
            }
        }
    }


    public interface ActionListener {
        void onPlayBegin();

        void onPlayLoading();

        void onFirstFrame();

        void onDoubleClick();

        void onPlayProgress(float progressVal);
    }


    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }
}
