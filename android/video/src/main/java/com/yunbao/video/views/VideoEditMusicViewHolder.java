package com.yunbao.video.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yunbao.common.custom.TextSeekBar;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.video.R;
import com.yunbao.common.bean.MusicBean;
import com.yunbao.video.custom.RangeSlider;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class VideoEditMusicViewHolder extends AbsViewHolder implements View.OnClickListener {

    private View mCutGroup;
    private TextView mTip;
    private RangeSlider mRangeSlider;
    private TextSeekBar mOriginSeekBar;//原声
    private TextSeekBar mBgmSeekBar;//背景音
    private TextView mMusicName;//音乐名称
    private ActionListener mActionListener;
    private boolean mShowed;
    private long mMusicDuration;
    private String mVideoMusicTipString;
    private boolean mHasOriginBgm;//是否在录制的时候有背景音乐
    private MusicBean mCurMusicBean;
    private TextView mStartTime;
    private TextView mEndTime;
    private boolean mPic;

    public VideoEditMusicViewHolder(Context context, ViewGroup parentView, boolean hasOriginBgm, MusicBean curMusicBean,boolean isPic) {
        super(context, parentView, hasOriginBgm,curMusicBean,isPic);
    }

    @Override
    protected void processArguments(Object... args) {
        if (args[0] != null) {
            mHasOriginBgm = (boolean) args[0];
        }
        if (args[1] != null) {
            mCurMusicBean = (MusicBean) args[1];
        }
        mPic= (boolean) args[2];
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_video_edit_volume;
    }

    @Override
    public void init() {
        mVideoMusicTipString = WordUtil.getString(R.string.video_cut_tip);
        mTip = (TextView) findViewById(R.id.tip);
        mCutGroup = findViewById(R.id.cut_group);
        mStartTime = (TextView) findViewById(R.id.start_time);
        mMusicName = (TextView) findViewById(R.id.music_name);
        mEndTime = (TextView) findViewById(R.id.end_time);
        mRangeSlider = (RangeSlider) findViewById(R.id.range_slider);
        mRangeSlider.setRangeChangeListener(new RangeSlider.OnRangeChangeListener() {
            @Override
            public void onKeyDown(int type) {

            }

            @Override
            public void onKeyUp(int type, int leftPinIndex, int rightPinIndex) {
                if (mMusicDuration > 0) {
                    long startTime = mMusicDuration * leftPinIndex / 100;
                    long endTime = mMusicDuration * rightPinIndex / 100;
                    showCutTime(startTime, endTime);
                    if (mActionListener != null) {
                        mActionListener.onBgmCutTimeChanged(startTime, endTime);
                    }
                }
            }
        });
        findViewById(R.id.root).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        mOriginSeekBar = (TextSeekBar) findViewById(R.id.btn_origin);
        mBgmSeekBar = (TextSeekBar) findViewById(R.id.seek_bgm);
        TextSeekBar.OnSeekChangeListener seekChangeListener = new TextSeekBar.OnSeekChangeListener() {
            @Override
            public void onProgressChanged(View v, int progress) {
                if (mActionListener != null) {
                    int i = v.getId();
                    if (i == R.id.btn_origin) {
                        mActionListener.onOriginalVolumeChanged(progress / 100f);
                    } else if (i == R.id.seek_bgm) {
                        mActionListener.onBgmVolumeChanged(progress / 100f);
                    }
                }
            }
        };
        mOriginSeekBar.setOnSeekChangeListener(seekChangeListener);
        mBgmSeekBar.setOnSeekChangeListener(seekChangeListener);

        if (mBgmSeekBar != null) {
            mBgmSeekBar.setProgress(0);
            mBgmSeekBar.setEnabled(false);
        }

        if (mCurMusicBean != null) {
            setMusicBean(mCurMusicBean);
        }
        if (mOriginSeekBar!=null){
            mOriginSeekBar.setVisibility(mPic?View.GONE:View.VISIBLE);
        }
    }

    /**
     * 设置背景音乐
     */
    public void setMusicBean(MusicBean bean) {
        if (bean == null) {
            return;
        }
        if (mCutGroup != null && mCutGroup.getVisibility() != View.VISIBLE) {
            mCutGroup.setVisibility(View.VISIBLE);
        }
        if (mMusicName != null) {
            mMusicName.setText(StringUtil.contact(bean.getTitle()," ",bean.getAuthor()));
        }
        //如果在录制的时候有背景音，则要把原声禁用
        if (mHasOriginBgm) {
            if (mOriginSeekBar != null) {
                mOriginSeekBar.setEnabled(false);
                mOriginSeekBar.setProgress(0);
            }
        }
        if (mBgmSeekBar != null) {
            mBgmSeekBar.setProgress(80);
            mBgmSeekBar.setEnabled(true);
        }
        if (mRangeSlider != null) {
            mRangeSlider.resetRangePos();
        }
        mMusicDuration = bean.getDuration();
        showCutTime(0, mMusicDuration);
    }

    /**
     * 取消背景音乐
     */
    private void cancelMusic() {
        if (mCutGroup != null && mCutGroup.getVisibility() == View.VISIBLE) {
            mCutGroup.setVisibility(View.GONE);
        }
        if (mOriginSeekBar != null) {
            mOriginSeekBar.setProgress(80);
            mOriginSeekBar.setEnabled(true);
        }
        if (mBgmSeekBar != null) {
            mBgmSeekBar.setProgress(0);
            mBgmSeekBar.setEnabled(false);
        }
        if (mActionListener != null) {
            mActionListener.onBgmCancelClick();
        }
        hide();
    }


    private void showCutTime(long startTime, long endTime) {
        if (mStartTime != null) {
            mStartTime.setText(StringUtil.getDurationText(startTime));
        }
        if (mEndTime != null) {
            mEndTime.setText(StringUtil.getDurationText(endTime));
        }
        if (mTip != null) {
            mTip.setText(StringUtil.contact(mVideoMusicTipString , String.format("%.2f", (endTime - startTime) / 1000f),"'"));
        }

    }

    public void show() {
        mShowed = true;
        if (mContentView != null && mContentView.getVisibility() != View.VISIBLE) {
            mContentView.setVisibility(View.VISIBLE);
        }
    }

    public void hide() {
        mShowed = false;
        if (mContentView != null && mContentView.getVisibility() == View.VISIBLE) {
            mContentView.setVisibility(View.INVISIBLE);
        }
        if (mActionListener != null) {
            mActionListener.onHide();
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.root) {
            hide();

        } else if (i == R.id.btn_cancel) {
            cancelMusic();

        }
    }

    public interface ActionListener {
        void onHide();

        void onOriginalVolumeChanged(float value);

        void onBgmVolumeChanged(float value);

        void onBgmCancelClick();

        void onBgmCutTimeChanged(long startTime, long endTime);
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public void release() {
        mActionListener = null;
    }

    public boolean isShowed() {
        return mShowed;
    }
}
