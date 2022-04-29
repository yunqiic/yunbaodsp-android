package com.yunbao.video.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tencent.ugc.TXVideoEditConstants;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.video.R;
import com.yunbao.video.activity.VideoEditActivity;
import com.yunbao.video.custom.ColorfulProgress;
import com.yunbao.video.custom.RangeSliderViewContainer;
import com.yunbao.video.custom.SliderViewContainer;
import com.yunbao.video.custom.VideoEditSpecialBtn;
import com.yunbao.video.custom.VideoProgressController;
import com.yunbao.video.custom.VideoProgressView;

import java.util.List;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class VideoSpecialViewHolder extends AbsViewHolder implements View.OnClickListener {

    private static final int TIME_SPECIAL_NONE = 0;
    private static final int TIME_SPECIAL_DF = 1;
    private static final int TIME_SPECIAL_FF = 2;
    private static final int TIME_SPECIAL_MDZ = 3;
    private ActionListener mActionListener;
    private boolean mShowed;
    private VideoProgressView mVideoProgressView;
    private VideoProgressController mVideoProgressController;
    private ColorfulProgress mColorfulProgress;
    private long mVideoDuration;
    private TextView mCurTimeTextView;
    private SparseArray<View> mSparseArray;
    private SparseArray<VideoEditSpecialBtn> mBtnSparseArray;
    private View mBtnOtherSpecialCancel;
    private boolean mTouching;
    private long mCurTime;
    private boolean mSpecialStartMark;
    private int mCurGroupId;
    private int mCurTimeBtnId;
    private int mTimeSpecail;
    private SliderViewContainer mRepeatSlider;//重复
    private SliderViewContainer mSpeedSlider;//慢动作

    public VideoSpecialViewHolder(Context context, ViewGroup parentView, long videoDuration) {
        super(context, parentView, videoDuration);
    }

    @Override
    protected void processArguments(Object... args) {
        mVideoDuration = (long) args[0];
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_video_edit_cut_2;
    }

    @Override
    public void init() {
        findViewById(R.id.root).setOnClickListener(this);
        mCurTimeTextView = (TextView) findViewById(R.id.time);
        mSparseArray = new SparseArray<>();
        mCurGroupId = R.id.group_cut;
        mCurTimeBtnId = R.id.btn_time_none;
        mTimeSpecail = TIME_SPECIAL_NONE;
        mSparseArray.put(R.id.btn_cut, findViewById(R.id.group_cut));
        mSparseArray.put(R.id.btn_time, findViewById(R.id.group_time));
        mSparseArray.put(R.id.btn_other, findViewById(R.id.group_other));
        findViewById(R.id.btn_cut).setOnClickListener(this);
        findViewById(R.id.btn_time).setOnClickListener(this);
        findViewById(R.id.btn_other).setOnClickListener(this);
        mBtnSparseArray = new SparseArray<>();
        VideoEditSpecialBtn editSpecialBtnNone = (VideoEditSpecialBtn) findViewById(R.id.btn_time_none);
        VideoEditSpecialBtn editSpecialBtnDf = (VideoEditSpecialBtn) findViewById(R.id.btn_time_df);
        VideoEditSpecialBtn editSpecialBtnFf = (VideoEditSpecialBtn) findViewById(R.id.btn_time_ff);
        VideoEditSpecialBtn editSpecialBtnMdz = (VideoEditSpecialBtn) findViewById(R.id.btn_time_mdz);
        mBtnSparseArray.put(R.id.btn_time_none, editSpecialBtnNone);
        mBtnSparseArray.put(R.id.btn_time_df, editSpecialBtnDf);
        mBtnSparseArray.put(R.id.btn_time_ff, editSpecialBtnFf);
        mBtnSparseArray.put(R.id.btn_time_mdz, editSpecialBtnMdz);
        editSpecialBtnNone.setOnClickListener(this);
        editSpecialBtnDf.setOnClickListener(this);
        editSpecialBtnFf.setOnClickListener(this);
        editSpecialBtnMdz.setOnClickListener(this);
        mBtnOtherSpecialCancel = findViewById(R.id.btn_other_cancel);
        mBtnOtherSpecialCancel.setOnClickListener(this);
        //长按添加特效
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                int action = e.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    if (mTouching) {//防止多个个其他特效同时按下
                        return false;
                    }
                    mTouching = true;
                    specialDown(v);
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    mTouching = false;
                    otherSpecialUp(v);
                }
                return true;
            }
        };
        findViewById(R.id.btn_other_1).setOnTouchListener(onTouchListener);
        findViewById(R.id.btn_other_2).setOnTouchListener(onTouchListener);
        findViewById(R.id.btn_other_3).setOnTouchListener(onTouchListener);
        findViewById(R.id.btn_other_4).setOnTouchListener(onTouchListener);
        mVideoProgressView = (VideoProgressView) findViewById(R.id.progress_view);
        List<Bitmap> list = ((VideoEditActivity)mContext).getBitmapList();
        if (list != null) {
            mVideoProgressView.addBitmapList(list);
        }
        mVideoProgressController = new VideoProgressController(mContext, mVideoDuration);
        mVideoProgressController.setVideoProgressView(mVideoProgressView);
        mVideoProgressController.setVideoProgressSeekListener(new VideoProgressController.VideoProgressSeekListener() {
            @Override
            public void onVideoProgressSeek(long currentTimeMs) {
                onVideoPreviewTimeChanged(currentTimeMs);
                if (mActionListener != null) {
                    mActionListener.onSeekChanged(currentTimeMs);
                }
            }

            @Override
            public void onVideoProgressSeekFinish(long currentTimeMs) {
                if (mActionListener != null) {
                    mActionListener.onSeekChanged(currentTimeMs);
                }
            }
        });
        RangeSliderViewContainer sliderViewContainer = new RangeSliderViewContainer(mContext);
        sliderViewContainer.init(mVideoProgressController, 0, mVideoDuration, mVideoDuration);
        sliderViewContainer.setDurationChangeListener(new RangeSliderViewContainer.OnDurationChangeListener() {
            @Override
            public void onDurationChange(long startTime, long endTime) {
                if (mActionListener != null) {
                    mActionListener.onCutTimeChanged(startTime, endTime);
                }
            }
        });
        mVideoProgressController.addRangeSliderView(sliderViewContainer);
        mColorfulProgress = new ColorfulProgress(mContext);
        mColorfulProgress.setWidthHeight(mVideoProgressController.getThumbnailPicListDisplayWidth(), DpUtil.dp2px(50));
        mVideoProgressController.addColorfulProgress(mColorfulProgress);

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.root) {
            hide();
        } else if (i == R.id.btn_other_cancel) {
            specialCancel();
        } else if (i == R.id.btn_cut) {
            tabGroup(i);
        } else if (i == R.id.btn_time) {
            tabGroup(i);
        } else if (i == R.id.btn_other) {
            tabGroup(i);
        } else if (i == R.id.btn_time_none) {
            tabTimeSpecial(i);
        } else if (i == R.id.btn_time_df) {
            tabTimeSpecial(i);
        } else if (i == R.id.btn_time_ff) {
            tabTimeSpecial(i);
        } else if (i == R.id.btn_time_mdz) {
            tabTimeSpecial(i);
        }
    }


    private void tabGroup(int id) {
        if (mCurGroupId == id) {
            return;
        }
        for (int i = 0, size = mSparseArray.size(); i < size; i++) {
            View v = mSparseArray.valueAt(i);
            if (id == mSparseArray.keyAt(i)) {
                if (v.getVisibility() != View.VISIBLE) {
                    v.setVisibility(View.VISIBLE);
                }
            } else {
                if (v.getVisibility() == View.VISIBLE) {
                    v.setVisibility(View.INVISIBLE);
                }
            }
        }
        mCurGroupId = id;
    }

    private void tabTimeSpecial(int id) {
        if (mCurTimeBtnId == id) {
            return;
        }
        for (int i = 0, size = mBtnSparseArray.size(); i < size; i++) {
            VideoEditSpecialBtn btn = mBtnSparseArray.valueAt(i);
            if (id == mBtnSparseArray.keyAt(i)) {
                btn.setChecked(true);
            } else {
                btn.setChecked(false);
            }
        }
        mCurTimeBtnId = id;
        if (id == R.id.btn_time_none) {
            timeNone();
        } else if (id == R.id.btn_time_df) {
            timeDf();
        } else if (id == R.id.btn_time_ff) {
            timeFf();
        } else if (id == R.id.btn_time_mdz) {
            timeMdz();
        }
    }


    /**
     * 撤销时间特效
     */
    private void cancelTimeSpecial() {
        if (mTimeSpecail == TIME_SPECIAL_NONE) {
            return;
        }
        switch (mTimeSpecail) {
            case TIME_SPECIAL_DF:
                if (mActionListener != null) {
                    mActionListener.onTimeSpecialDf(false);
                }
                break;
            case TIME_SPECIAL_FF:
                if (mRepeatSlider != null && mRepeatSlider.getVisibility() == View.VISIBLE) {
                    mRepeatSlider.setVisibility(View.GONE);
                }
                if (mActionListener != null) {
                    mActionListener.onTimeSpecialFf(false, 0);
                }
                break;
            case TIME_SPECIAL_MDZ:
                if (mSpeedSlider != null && mSpeedSlider.getVisibility() == View.VISIBLE) {
                    mSpeedSlider.setVisibility(View.GONE);
                }
                if (mActionListener != null) {
                    mActionListener.onTimeSpecialMdz(false, 0);
                }
                break;
        }
        mTimeSpecail = TIME_SPECIAL_NONE;
    }

    /**
     * 时间特效 无
     */
    private void timeNone() {
        cancelTimeSpecial();
        if (mActionListener != null) {
            mActionListener.onTimeSpecialCancel();
        }
    }

    /**
     * 时间特效 倒放
     */
    private void timeDf() {
        if (mTimeSpecail == TIME_SPECIAL_DF) {
            return;
        }
        cancelTimeSpecial();
        if (mActionListener != null) {
            mActionListener.onTimeSpecialDf(true);
        }
        mTimeSpecail = TIME_SPECIAL_DF;
    }

    /**
     * 时间特效 反复
     */
    private void timeFf() {
        if (mTimeSpecail == TIME_SPECIAL_FF) {
            return;
        }
        cancelTimeSpecial();
        long currentTimeMs = mVideoProgressController.getCurrentTimeMs();

        if (mActionListener != null) {
            mActionListener.onTimeSpecialFf(true, currentTimeMs);
        }
        if (mRepeatSlider == null) {
            mRepeatSlider = new SliderViewContainer(mContext);
            mRepeatSlider.setOnStartTimeChangedListener(new SliderViewContainer.OnStartTimeChangedListener() {
                @Override
                public void onStartTimeMsChanged(long timeMs) {
                    if (mActionListener != null) {
                        mActionListener.onTimeSpecialFf(true, timeMs);
                    }
                    mVideoProgressController.setCurrentTimeMs(timeMs);
                }
            });
            mVideoProgressController.addSliderView(mRepeatSlider);
        } else {
            if (mRepeatSlider.getVisibility() != View.VISIBLE) {
                mRepeatSlider.setVisibility(View.VISIBLE);
            }
        }
        mRepeatSlider.setStartTimeMs(currentTimeMs);
        mTimeSpecail = TIME_SPECIAL_FF;
    }

    /**
     * 时间特效 慢动作
     */
    private void timeMdz() {
        if (mTimeSpecail == TIME_SPECIAL_MDZ) {
            return;
        }
        cancelTimeSpecial();
        long currentTimeMs = mVideoProgressController.getCurrentTimeMs();
        if (mActionListener != null) {
            mActionListener.onTimeSpecialMdz(true, currentTimeMs);
        }
        if (mSpeedSlider == null) {
            mSpeedSlider = new SliderViewContainer(mContext);
            mSpeedSlider.setOnStartTimeChangedListener(new SliderViewContainer.OnStartTimeChangedListener() {
                @Override
                public void onStartTimeMsChanged(long timeMs) {
                    if (mActionListener != null) {
                        mActionListener.onTimeSpecialMdz(true, timeMs);
                    }
                    mVideoProgressController.setCurrentTimeMs(timeMs);
                }
            });
            mVideoProgressController.addSliderView(mSpeedSlider);
        } else {
            if (mSpeedSlider.getVisibility() != View.VISIBLE) {
                mSpeedSlider.setVisibility(View.VISIBLE);
            }
        }
        mSpeedSlider.setStartTimeMs(currentTimeMs);
        mTimeSpecail = TIME_SPECIAL_MDZ;
    }


    public void onVideoProgressChanged(long timeMs) {
        if (mShowed) {
            int currentTimeMs = (int) (timeMs / 1000);//转为ms值
            if (mVideoProgressController != null) {
                mVideoProgressController.setCurrentTimeMs(currentTimeMs);
            }
            onVideoPreviewTimeChanged(currentTimeMs);
        }
    }

    public void onVideoPreviewTimeChanged(long currentTimeMs) {
        mCurTime = currentTimeMs;
        if (mCurTimeTextView != null) {
            mCurTimeTextView.setText(String.format("%.2f", currentTimeMs / 1000f) + "s");
        }
    }


    /**
     * 特效的按钮被按下
     */
    private void specialDown(View v) {
        if (mCurTime >= mVideoDuration) {
            mSpecialStartMark = false;
            return;
        }
        mSpecialStartMark = true;
        int color = 0;
        int effect = 0;
        int i = v.getId();
        if (i == R.id.btn_other_1) {
            color = 0xAA1FBCB6;
            effect = TXVideoEditConstants.TXEffectType_ROCK_LIGHT;

        } else if (i == R.id.btn_other_4) {
            color = 0xAAEC8435;
            effect = TXVideoEditConstants.TXEffectType_SPLIT_SCREEN;

        } else if (i == R.id.btn_other_2) {
            color = 0xAA449FF3;
            effect = TXVideoEditConstants.TXEffectType_DARK_DRAEM;

        } else if (i == R.id.btn_other_3) {
            color = 0xAAEC5F9B;
            effect = TXVideoEditConstants.TXEffectType_SOUL_OUT;

        }
        if (mColorfulProgress != null) {
            mColorfulProgress.startMark(color);
        }
        if (mActionListener != null) {
            mActionListener.onSpecialStart(effect, mCurTime);
        }
    }

    /**
     * 特效的按钮被抬起
     */
    private void otherSpecialUp(View v) {
        if (!mSpecialStartMark) {
            return;
        }
        mSpecialStartMark = false;
        int effect = 0;
        int i = v.getId();
        if (i == R.id.btn_other_1) {
            effect = TXVideoEditConstants.TXEffectType_ROCK_LIGHT;
        } else if (i == R.id.btn_other_4) {
            effect = TXVideoEditConstants.TXEffectType_SPLIT_SCREEN;
        } else if (i == R.id.btn_other_2) {
            effect = TXVideoEditConstants.TXEffectType_DARK_DRAEM;
        } else if (i == R.id.btn_other_3) {
            effect = TXVideoEditConstants.TXEffectType_SOUL_OUT;
        }
        if (mColorfulProgress != null) {
            mColorfulProgress.endMark();
        }
        if (mActionListener != null) {
            mActionListener.onSpecialEnd(effect, mCurTime);
        }
        showBtnSpecialCancel();
    }


    /**
     * 撤销最后一次特效
     */
    private void specialCancel() {
        if (mColorfulProgress != null) {
            ColorfulProgress.MarkInfo markInfo = mColorfulProgress.deleteLastMark();
            if (markInfo != null) {
                if (mVideoProgressController != null) {
                    mVideoProgressController.setCurrentTimeMs(markInfo.startTimeMs);
                }
                if (mActionListener != null) {
                    mActionListener.onSpecialCancel(markInfo.startTimeMs);
                }
            }
        }
        showBtnSpecialCancel();
    }

    /**
     * 显示或隐藏撤销其他特效的按钮
     */
    private void showBtnSpecialCancel() {
        if (mBtnOtherSpecialCancel != null && mColorfulProgress != null) {
            if (mColorfulProgress.getMarkListSize() > 0) {
                if (mBtnOtherSpecialCancel.getVisibility() != View.VISIBLE) {
                    mBtnOtherSpecialCancel.setVisibility(View.VISIBLE);
                }
            } else {
                if (mBtnOtherSpecialCancel.getVisibility() == View.VISIBLE) {
                    mBtnOtherSpecialCancel.setVisibility(View.INVISIBLE);
                }
            }
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


    public interface ActionListener {
        void onHide();

        void onSeekChanged(long currentTimeMs);

        void onCutTimeChanged(long startTime, long endTime);

        void onSpecialStart(int effect, long currentTimeMs);

        void onSpecialEnd(int effect, long currentTimeMs);

        void onSpecialCancel(long currentTimeMs);

        void onTimeSpecialCancel();

        void onTimeSpecialDf(boolean use);

        void onTimeSpecialFf(boolean use, long startTime);

        void onTimeSpecialMdz(boolean use, long startTime);
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
