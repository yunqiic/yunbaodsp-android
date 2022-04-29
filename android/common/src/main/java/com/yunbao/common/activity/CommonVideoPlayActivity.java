package com.yunbao.common.activity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.R;
import com.yunbao.common.glide.ImgLoader;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class CommonVideoPlayActivity extends AbsActivity implements View.OnClickListener, ITXLivePlayListener {


    private TXCloudVideoView mTXCloudVideoView;
    private TXVodPlayer mPlayer;
    private ObjectAnimator mPlayBtnAnimator;//暂停按钮的动画
    private View mPlayBtn;
    private boolean mPlayStarted;//播放是否开始了
    private boolean mPaused;//生命周期暂停
    private boolean mClickPaused;//点击暂停
    private ImageView mVideoCover;
    private AudioManager mAudioManager;
    private float mWidth;
    private float mHeight;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_common_video_play;
    }

    @Override
    protected void main() {
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        findViewById(R.id.root).setOnClickListener(this);
        mVideoCover = findViewById(R.id.video_cover);
        mPlayBtn = findViewById(R.id.btn_play);
        //暂停按钮动画
        mPlayBtnAnimator = ObjectAnimator.ofPropertyValuesHolder(mPlayBtn,
                PropertyValuesHolder.ofFloat("scaleX", 4f, 0.8f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 4f, 0.8f, 1f),
                PropertyValuesHolder.ofFloat("alpha", 0f, 1f));
        mPlayBtnAnimator.setDuration(150);
        mPlayBtnAnimator.setInterpolator(new AccelerateInterpolator());
        mTXCloudVideoView = findViewById(R.id.video_view);
        mTXCloudVideoView.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
        mTXCloudVideoView.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        mPlayer = new TXVodPlayer(mContext);
        TXVodPlayConfig config = new TXVodPlayConfig();
        config.setMaxCacheItems(15);
        config.setProgressInterval(200);
        config.setHeaders(CommonAppConfig.HEADER);
        mPlayer.setConfig(config);
        mPlayer.setAutoPlay(true);
        mPlayer.setPlayListener(this);
        mPlayer.setPlayerView(mTXCloudVideoView);
        Intent intent = getIntent();
        String videoUrl = intent.getStringExtra(Constants.VIDEO_PATH);
        String coverImgUrl = intent.getStringExtra(Constants.URL);
        if (!TextUtils.isEmpty(coverImgUrl)) {
            setCoverImage(coverImgUrl);
        }
        if (!TextUtils.isEmpty(videoUrl)) {
            startPlay(videoUrl);
        }
    }

    @Override
    protected boolean isStatusBarWhite() {
        return true;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.root) {
            clickTogglePlay();
        }
    }

    @Override
    public void onPlayEvent(int e, Bundle bundle) {
        switch (e) {
            case TXLiveConstants.PLAY_EVT_PLAY_END://播放结束
                onReplay();
                break;
            case TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION:
                onVideoSizeChanged(mWidth,mHeight);
                break;
            case TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME://获取到视频首帧回调
                if (mVideoCover != null && mVideoCover.getVisibility() == View.VISIBLE) {
                    mVideoCover.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }

    @Override
    public void onNetStatus(Bundle bundle) {

    }

    private void startPlay(String videoUrl) {
        if (mVideoCover != null && mVideoCover.getVisibility() != View.VISIBLE) {
            mVideoCover.setVisibility(View.VISIBLE);
        }
        int result = mPlayer.startPlay(videoUrl);
        if (result == 0) {
            mPlayStarted = true;
        }
    }

    private void setCoverImage(String coverImageUrl) {
        ImgLoader.displayDrawable(mContext, coverImageUrl, new ImgLoader.DrawableCallback2() {
            @Override
            public void onLoadSuccess(Drawable drawable) {
                if (mVideoCover != null && mVideoCover.getVisibility() == View.VISIBLE && drawable != null) {
                    float w = drawable.getIntrinsicWidth();
                    float h = drawable.getIntrinsicHeight();
                    mWidth = w;
                    mHeight = h;
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mVideoCover.getLayoutParams();
                    int targetH = 0;
                    if (w / h > 0.5625f) {//横屏  9:16=0.5625
                        targetH = (int) (mVideoCover.getWidth() / w * h);
                    } else {
                        targetH = ViewGroup.LayoutParams.MATCH_PARENT;
                    }
                    if (targetH != params.height) {
                        params.height = targetH;
                        params.gravity = Gravity.CENTER;
                        mVideoCover.requestLayout();
                    }
                    mVideoCover.setImageDrawable(drawable);
                }
            }

            @Override
            public void onLoadFailed() {

            }
        });
    }


    /**
     * 循环播放
     */
    private void onReplay() {
        if (mPlayStarted && mPlayer != null) {
            mPlayer.seek(0);
            mPlayer.resume();
        }
    }


    /**
     * 获取到视频宽高回调
     */
    public void onVideoSizeChanged(float videoWidth, float videoHeight) {
        if (mTXCloudVideoView != null && videoWidth > 0 && videoHeight > 0) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mTXCloudVideoView.getLayoutParams();
            int targetH = 0;
            if (videoWidth / videoHeight > 0.5625f) {//横屏 9:16=0.5625
                targetH = (int) (mTXCloudVideoView.getWidth() / videoWidth * videoHeight);
            } else {
                targetH = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            if (targetH != params.height) {
                params.height = targetH;
                params.gravity = Gravity.CENTER;
                mTXCloudVideoView.requestLayout();
            }
        }
    }

    /**
     * 点击切换播放和暂停
     */
    private void clickTogglePlay() {
        if (!mPlayStarted) {
            return;
        }
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

    @Override
    protected void onPause() {
        mPaused = true;
        if (!mClickPaused && mPlayer != null) {
            mPlayer.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPaused) {
            if (!mClickPaused && mPlayer != null) {
                mPlayer.resume();
            }
        }
        mPaused = false;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.stopPlay(false);
            mPlayer.setPlayListener(null);
        }
        mPlayer = null;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (mAudioManager != null) {
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_RAISE,
                            AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (mAudioManager != null) {
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_LOWER,
                            AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
