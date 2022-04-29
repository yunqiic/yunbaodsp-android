package com.yunbao.video.activity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;

import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoInfoReader;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.bean.MusicBean;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.BitmapUtil;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.DownloadUtil;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.MD5Util;
import com.yunbao.common.utils.MediaUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.video.R;
import com.yunbao.video.utils.VideoCoverUtil;
import com.yunbao.video.views.VideoEditFilterViewHolder;
import com.yunbao.video.views.VideoEditMusicViewHolder;
import com.yunbao.video.views.VideoMusicViewHolder;
import com.yunbao.video.views.VideoProcessViewHolder;
import com.yunbao.video.views.VideoSpecialViewHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
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

public class VideoEditActivity extends AbsActivity implements
        VideoProcessViewHolder.ActionListener,//预处理控件点击取消回调
        TXVideoEditer.TXVideoProcessListener, //视频编辑前预处理进度回调
        TXVideoEditer.TXThumbnailListener, //视频编辑前预处理中生成每一帧缩略图回调
        TXVideoEditer.TXVideoPreviewListener,//编辑视频预览回调
        TXVideoEditer.TXVideoGenerateListener //生成视频预览回调
{

    private static final String TAG = "VideoEditActivity";
    private static final int STATUS_NONE = 0;
    private static final int STATUS_PLAY = 1;
    private static final int STATUS_PAUSE = 2;
    private static final int STATUS_PREVIEW_AT_TIME = 3;


    public static void forward(Context context, long videoDuration, String videoPath, boolean fromRecord, MusicBean musicBean) {
        Intent intent = new Intent(context, VideoEditActivity.class);
        intent.putExtra(Constants.VIDEO_DURATION, videoDuration);
        intent.putExtra(Constants.VIDEO_PATH, videoPath);
        intent.putExtra(Constants.VIDEO_FROM_RECORD, fromRecord);
        intent.putExtra(Constants.VIDEO_MUSIC_BEAN, musicBean);
        context.startActivity(intent);
    }

    public static void forward(Context context, long videoDuration, String videoPath, boolean fromRecord, MusicBean musicBean, boolean isHideMusic, boolean videoRecordJoin) {
        Intent intent = new Intent(context, VideoEditActivity.class);
        intent.putExtra(Constants.VIDEO_DURATION, videoDuration);
        intent.putExtra(Constants.VIDEO_PATH, videoPath);
        intent.putExtra(Constants.VIDEO_FROM_RECORD, fromRecord);
        intent.putExtra(Constants.VIDEO_MUSIC_BEAN, musicBean);
        intent.putExtra(Constants.VIDEO_IS_HIDE_MUSIC, isHideMusic);
        intent.putExtra(Constants.VIDEO_RECORD_JOIN, videoRecordJoin);
        context.startActivity(intent);
    }

    private ViewGroup mRoot;
    private View mGroup;
    private View mBtnNext;
    private View mBtnPlay;
    private ObjectAnimator mPlayBtnAnimator;//暂停按钮的动画
    private TXVideoEditer mVideoEditer;
    private List<Bitmap> mBitmapList;//视频每一帧的缩略图
    private LongSparseArray<Bitmap> mBitmapMap;//视频每一帧的缩略图
    private long mVideoDuration;//视频总长度
    private String mOriginVideoPath;//原视频路径
    private boolean mFromRecord;
    private long mCutStartTime;//裁剪的起始时间
    private long mCutEndTime;//裁剪的结束时间
    private MusicBean mMusicBean;//背景音乐
    private boolean mHasOriginBgm;//是否在录制的时候有背景音乐
    private VideoMusicViewHolder mMusicViewHolder;//音乐
    private VideoEditFilterViewHolder mFilterViewHolder;//滤镜
    private VideoEditMusicViewHolder mVolumeViewHolder;//音量
    private VideoSpecialViewHolder mSpecialViewHolder;//特效
    private String mGenerateVideoPath;//生成视频的路径
    private String mGenerateVideoPathWater;//生成视频的路径
    private VideoProcessViewHolder mVideoProcessViewHolder;//视频预处理进度条
    private VideoProcessViewHolder mVideoGenerateViewHolder;//视频生成进度条
    private int mSaveType;
    private boolean mPaused;//生命周期暂停
    private long mPreviewAtTime;
    private int mPLayStatus = STATUS_NONE;
    private MyHandler mHandler;
    private int mSdkErrorWaitCount;//sdk预处理回调异常的时候，设置30秒等待时间
    private static final int MAX_WAIT_COUNT = 120;
    private boolean mSdkError;
    private boolean mUseWaterMark;
    private int mGenerateProgress;//生成视频的进度
    private boolean mIsDestroy;
    private boolean mIsHideMusic;
    private boolean mVideoRecordJoin;//是否是合拍的
    private View mBtnMusicVolume;
    private View mBtnMusic;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_edit;
    }

    @Override
    protected boolean isStatusBarWhite() {
        return true;
    }

    @Override
    protected void main() {
        mRoot = (ViewGroup) findViewById(R.id.root);
        mGroup = findViewById(R.id.group);
        mBtnNext = findViewById(R.id.btn_next);
        mBtnPlay = findViewById(R.id.btn_play);
        mBtnMusicVolume = findViewById(R.id.btn_music_volume);
        mBtnMusic = findViewById(R.id.btn_music);
        //暂停按钮动画
        mPlayBtnAnimator = ObjectAnimator.ofPropertyValuesHolder(mBtnPlay,
                PropertyValuesHolder.ofFloat("scaleX", 4f, 0.8f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 4f, 0.8f, 1f),
                PropertyValuesHolder.ofFloat("alpha", 0f, 1f));
        mPlayBtnAnimator.setDuration(150);
        mPlayBtnAnimator.setInterpolator(new AccelerateInterpolator());
        mSaveType = Constants.VIDEO_SAVE_SAVE_AND_PUB;
        Intent intent = getIntent();
        mVideoDuration = intent.getLongExtra(Constants.VIDEO_DURATION, 0);
        mOriginVideoPath = intent.getStringExtra(Constants.VIDEO_PATH);
        mFromRecord = intent.getBooleanExtra(Constants.VIDEO_FROM_RECORD, false);
        mMusicBean = intent.getParcelableExtra(Constants.VIDEO_MUSIC_BEAN);
        mHasOriginBgm = mMusicBean != null;
        mIsHideMusic = intent.getBooleanExtra(Constants.VIDEO_IS_HIDE_MUSIC, false);
        mVideoRecordJoin = intent.getBooleanExtra(Constants.VIDEO_RECORD_JOIN, false);
        if (mVideoDuration <= 0 || TextUtils.isEmpty(mOriginVideoPath)) {
            ToastUtil.show(WordUtil.getString(R.string.video_edit_status_error));
            deleteOriginVideoFile();
            finish();
            return;
        }
        mVideoEditer = new TXVideoEditer(mContext);
        mVideoEditer.setVideoPath(mOriginVideoPath);

        mVideoEditer.setVideoProcessListener(this);
        mVideoEditer.setThumbnailListener(this);
        mVideoEditer.setTXVideoPreviewListener(this);
        mVideoEditer.setVideoGenerateListener(this);
        mCutStartTime = 0;
        mCutEndTime = mVideoDuration;
        startPreProcess();
        if (mIsHideMusic) {
            mBtnMusicVolume.setVisibility(View.GONE);
            mBtnMusic.setVisibility(View.GONE);
        }

    }

    /**
     * 设置水印
     */
    private void getWaterMark(final CommonCallback<Bitmap> commonCallback) {
        if (commonCallback == null) {
            return;
        }
        ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
        if (configBean == null) {
            commonCallback.callback(null);
            return;
        }
        String waterMarkUrl = configBean.getWaterMarkUrl();
        if (TextUtils.isEmpty(waterMarkUrl)) {
            commonCallback.callback(null);
            return;
        }
        File dir = new File(CommonAppConfig.WATER_MARK_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = MD5Util.getMD5(waterMarkUrl);
        File waterMarkFile = new File(dir, fileName);
        if (waterMarkFile.exists()) {
            commonCallback.callback(BitmapUtil.getInstance().decodeBitmap(waterMarkFile));
            return;
        }
        DownloadUtil downloadUtil = new DownloadUtil();
        downloadUtil.download("waterMark", dir, fileName, waterMarkUrl, new DownloadUtil.Callback() {
            @Override
            public void onSuccess(File resultFile) {
                if (resultFile != null && resultFile.exists()) {
                    commonCallback.callback(BitmapUtil.getInstance().decodeBitmap(resultFile));
                }
            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }

    private void deleteOriginVideoFile() {
        if (mFromRecord && !TextUtils.isEmpty(mOriginVideoPath)) {
            File file = new File(mOriginVideoPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 显示开始播放按钮
     */
    private void showPlayBtn() {
        if (mBtnPlay != null && mBtnPlay.getVisibility() != View.VISIBLE) {
            mBtnPlay.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏开始播放按钮
     */
    private void hidePlayBtn() {
        if (mBtnPlay != null && mBtnPlay.getVisibility() == View.VISIBLE) {
            mBtnPlay.setVisibility(View.INVISIBLE);
        }
    }


    /**
     * 点击切换播放和暂停
     */
    private void clickTogglePlay() {
        switch (mPLayStatus) {
            case STATUS_PLAY:
                mPLayStatus = STATUS_PAUSE;
                if (mVideoEditer != null) {
                    mVideoEditer.pausePlay();
                }
                break;
            case STATUS_PAUSE:
                mPLayStatus = STATUS_PLAY;
                if (mVideoEditer != null) {
                    mVideoEditer.resumePlay();
                }
                break;
            case STATUS_PREVIEW_AT_TIME:
                mPLayStatus = STATUS_PLAY;
                if (mVideoEditer != null) {
                    if (mPreviewAtTime > mCutStartTime && mPreviewAtTime < mCutEndTime) {
                        mVideoEditer.startPlayFromTime(mPreviewAtTime, mCutEndTime);
                    } else {
                        mVideoEditer.startPlayFromTime(mCutStartTime, mCutEndTime);
                    }
                }
                break;
        }
        if (mPLayStatus == STATUS_PAUSE) {
            showPlayBtn();
            if (mPlayBtnAnimator != null) {
                mPlayBtnAnimator.start();
            }
        } else {
            hidePlayBtn();
        }
    }


    /**
     * 开启视频预览
     */
    private void startVideoPreview() {
        if (mVideoEditer == null) {
            return;
        }
        FrameLayout layout = (FrameLayout) findViewById(R.id.video_container);
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = layout;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        mVideoEditer.initWithPreview(param);
        if (mMusicBean != null) {
            String bgmPath = mMusicBean.getLocalPath();
            if (!TextUtils.isEmpty(bgmPath)) {
                mVideoEditer.setBGM(bgmPath);
                mVideoEditer.setBGMVolume(0.8f);
                mVideoEditer.setVideoVolume(0);
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                try {
                    mmr.setDataSource(bgmPath);
                    String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    long bgmDuration = Long.parseLong(duration);
                    mMusicBean.setDuration(bgmDuration);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (mmr != null) {
                        mmr.release();
                    }
                }
            }
        }
        startPlay();
    }

    /**
     * 开始播放
     */
    private void startPlay() {
        if (mVideoEditer != null) {
            mPLayStatus = STATUS_PLAY;
            mVideoEditer.startPlayFromTime(mCutStartTime, mCutEndTime);
            hidePlayBtn();
        }
    }

    /**
     * 预览播放回调
     */
    @Override
    public void onPreviewProgress(int time) {
        if (mPLayStatus == STATUS_PLAY && mSpecialViewHolder != null) {
            mSpecialViewHolder.onVideoProgressChanged(time);
        }
    }

    /**
     * 预览播放回调
     */
    @Override
    public void onPreviewFinished() {
        if (mPLayStatus == STATUS_PLAY) {
            startPlay();//播放结束后，重新开始播放
        }
    }

    /**
     * 生成视频进度回调
     */
    @Override
    public void onGenerateProgress(float progress) {
        if (mUseWaterMark) {
            mGenerateProgress = 50 + (int) (progress * 50);
        } else {
            mGenerateProgress = (int) (progress * 50);
        }
        if (mVideoGenerateViewHolder != null) {
            mVideoGenerateViewHolder.setProgress(mGenerateProgress);
        }
    }


    /**
     * 生成视频结束回调
     */
    private void generateFinish() {
        L.e(TAG, "onGenerateComplete------->生成视频结束");
        if (mVideoGenerateViewHolder != null) {
            mVideoGenerateViewHolder.setProgress(100);
        }
//        ToastUtil.show(R.string.video_generate_success);
        switch (mSaveType) {
            case Constants.VIDEO_SAVE_SAVE://仅保存
                ToastUtil.show(R.string.video_save_success);
                saveLocalVideo();
                break;
            case Constants.VIDEO_SAVE_PUB://仅发布
                VideoCoverUtil.getInstance().addBitmap(mBitmapMap, mCutStartTime, mCutEndTime);
                VideoPublishActivity.forward(mContext, mGenerateVideoPath, mGenerateVideoPathWater, mSaveType, mMusicBean == null ? "0" : mMusicBean.getId());
                break;
            case Constants.VIDEO_SAVE_SAVE_AND_PUB://保存并发布
                saveLocalVideo();
                VideoCoverUtil.getInstance().addBitmap(mBitmapMap, mCutStartTime, mCutEndTime);
                VideoPublishActivity.forward(mContext, mGenerateVideoPath, mGenerateVideoPathWater, mSaveType, mMusicBean == null ? "0" : mMusicBean.getId());
                break;
        }
        finish();
    }

    /**
     * 保存本地
     */
    private void saveLocalVideo(){
        Dialog dialog=DialogUitl.loadingDialog(mContext);
        dialog.show();
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        File  copyFile =null;
        try {
            inputStream = new FileInputStream(mGenerateVideoPath);
            File dir = new File(CommonAppConfig.VIDEO_DOWNLOAD_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            copyFile = new File(dir, StringUtil.generateFileName() + ".mp4");
            outputStream = new FileOutputStream(copyFile);
            byte[] buf = new byte[4096];
            int len = 0;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
        } catch (Exception e) {
            copyFile=null;
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(copyFile!=null){
            MediaUtil.saveVideoInfo(mContext, copyFile.getAbsolutePath(), mCutEndTime - mCutStartTime);
            ToastUtil.show(R.string.video_save_success);

        }
        dialog.dismiss();
    }

    /**
     * 生成视频结束回调
     */
    @Override
    public void onGenerateComplete(TXVideoEditConstants.TXGenerateResult result) {
        L.e(TAG, "onGenerateComplete------->");
        if (result.retCode == TXVideoEditConstants.GENERATE_RESULT_OK) {
            if (mUseWaterMark) {
                L.e(TAG, "水印视频生成结束------->");
                generateFinish();
            } else {
                L.e(TAG, "正常视频生成结束------->");
                mUseWaterMark = true;
                startGenerateVideo();
            }
        } else {
            ToastUtil.show(R.string.video_generate_failed);
            if (mVideoGenerateViewHolder != null) {
                mVideoGenerateViewHolder.removeFromParent();
            }
            if (mBtnNext != null) {
                mBtnNext.setEnabled(true);
            }
        }
    }

    /**
     * 把新生成的视频保存到ContentProvider,在选择上传的时候能找到
     */
    private void saveGenerateVideoInfo() {
        MediaUtil.saveVideoInfo(mContext, mGenerateVideoPath, mCutEndTime - mCutStartTime);
    }

    public void videoEditClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_music) {
            clickMusic();

        } else if (i == R.id.btn_music_volume) {
            clickMusicVolume();

        } else if (i == R.id.btn_filter) {
            clickFilter();

        } else if (i == R.id.btn_special) {
            clickSpecial();

        } else if (i == R.id.btn_next) {
            clickNext();

        } else if (i == R.id.group) {
            clickTogglePlay();

        }
    }

    /**
     * 点击音乐
     */
    private void clickMusic() {
        hideGroup();
        if (mMusicViewHolder == null) {
            mMusicViewHolder = new VideoMusicViewHolder(mContext, mRoot);
            mMusicViewHolder.setActionListener(new VideoMusicViewHolder.ActionListener() {
                @Override
                public void onChooseMusic(MusicBean bean) {
                    if (mVideoEditer != null && bean != null) {
                        String bgmPath = bean.getLocalPath();
                        if (TextUtils.isEmpty(bgmPath)) {
                            return;
                        }
                        long bgmDuration = 0;
                        MediaMetadataRetriever  mmr = new MediaMetadataRetriever();
                        try {
                            mmr.setDataSource(bgmPath);
                            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            bgmDuration = Long.parseLong(duration);
                        } catch (Exception e) {
                            bgmDuration = 0;
                            e.printStackTrace();
                        }finally {
                            if(mmr!=null){
                                mmr.release();
                            }
                        }
                        if (bgmDuration == 0) {
                            return;
                        }
                        bean.setDuration(bgmDuration);
                        mVideoEditer.setBGM(bgmPath);
                        mVideoEditer.setBGMVolume(0.8f);
                        mVideoEditer.setBGMLoop(false);
                        if (mHasOriginBgm) {
                            mVideoEditer.setVideoVolume(0);
                        }
                        mMusicBean = bean;
                        if (mVolumeViewHolder != null) {
                            mVolumeViewHolder.setMusicBean(bean);
                        }
                        mVideoEditer.stopPlay();
                        startPlay();
                    }
                }

                @Override
                public void onHide() {
                    showGroup();
                }
            });
            mMusicViewHolder.addToParent();
            mMusicViewHolder.subscribeActivityLifeCycle();
        }
        mMusicViewHolder.show();
    }

    /**
     * 点击音量
     */
    private void clickMusicVolume() {
        hideGroup();
        if (mVolumeViewHolder == null) {
            mVolumeViewHolder = new VideoEditMusicViewHolder(mContext, mRoot, mHasOriginBgm, mMusicBean, false);
            mVolumeViewHolder.setActionListener(new VideoEditMusicViewHolder.ActionListener() {
                @Override
                public void onHide() {
                    showGroup();
                }

                @Override
                public void onOriginalVolumeChanged(float value) {
                    if (mVideoEditer != null) {
                        mVideoEditer.setVideoVolume(value);
                    }
                }

                @Override
                public void onBgmVolumeChanged(float value) {
                    if (mVideoEditer != null) {
                        mVideoEditer.setBGMVolume(value);
                    }
                }

                @Override
                public void onBgmCancelClick() {
                    if (mVideoEditer != null) {
                        mVideoEditer.setVideoVolume(0.8f);
                        mVideoEditer.setBGM(null);
                        mVideoEditer.stopPlay();
                        startPlay();
                    }
                    mMusicBean = null;
                }

                @Override
                public void onBgmCutTimeChanged(long startTime, long endTime) {
                    if (mVideoEditer != null) {
                        mVideoEditer.setBGMStartTime(startTime, endTime);
                    }
                }
            });
            mVolumeViewHolder.addToParent();
            if (mMusicBean != null) {
                mVolumeViewHolder.setMusicBean(mMusicBean);
            }
        }
        mVolumeViewHolder.show();
    }

    /**
     * 点击滤镜
     */
    private void clickFilter() {
        hideGroup();
        if (mFilterViewHolder == null) {
            mFilterViewHolder = new VideoEditFilterViewHolder(mContext, mRoot);
            mFilterViewHolder.setActionListener(new VideoEditFilterViewHolder.ActionListener() {
                @Override
                public void onHide() {
                    showGroup();
                }

                @Override
                public void onFilterChanged(Bitmap bitmap) {
                    if (mVideoEditer != null) {
                        mVideoEditer.setFilter(bitmap);
                    }
                }
            });
            mFilterViewHolder.addToParent();
        }
        mFilterViewHolder.show();
    }

    private void showSpecialViewHolder() {
        if (mSpecialViewHolder == null) {
            mSpecialViewHolder = new VideoSpecialViewHolder(mContext, mRoot, mVideoDuration);
            mSpecialViewHolder.setActionListener(new VideoSpecialViewHolder.ActionListener() {
                @Override
                public void onHide() {
                    showGroup();
                    if (mPLayStatus != STATUS_PLAY) {
                        clickTogglePlay();
                    }
                }

                @Override
                public void onSeekChanged(long currentTimeMs) {
                    previewAtTime(currentTimeMs);
                }

                @Override
                public void onCutTimeChanged(long startTime, long endTime) {
                    mCutStartTime = startTime;
                    mCutEndTime = endTime;
                    if (mVideoEditer != null) {
                        mVideoEditer.setCutFromTime(startTime, endTime);
                    }
                }

                @Override
                public void onSpecialStart(int effect, long currentTimeMs) {
                    if (mVideoEditer != null) {
                        if (mPLayStatus == STATUS_NONE || mPLayStatus == STATUS_PREVIEW_AT_TIME) {
                            mVideoEditer.startPlayFromTime(mPreviewAtTime, mCutEndTime);
                        } else if (mPLayStatus == STATUS_PAUSE) {
                            mVideoEditer.resumePlay();
                        }
                        mPLayStatus = STATUS_PLAY;
                        mVideoEditer.startEffect(effect, currentTimeMs);
                    }
                    hidePlayBtn();
                }

                @Override
                public void onSpecialEnd(int effect, long currentTimeMs) {
                    if (mVideoEditer != null) {
                        mVideoEditer.pausePlay();
                        mPLayStatus = STATUS_PAUSE;
                        mVideoEditer.stopEffect(effect, currentTimeMs);
                    }
                    showPlayBtn();
                }

                @Override
                public void onSpecialCancel(long currentTimeMs) {
                    if (mVideoEditer != null) {
                        mVideoEditer.deleteLastEffect();
                        previewAtTime(currentTimeMs);
                    }

                }

                @Override
                public void onTimeSpecialCancel() {
                    startPlay();
                }

                @Override
                public void onTimeSpecialDf(boolean use) {
                    if (mVideoEditer != null) {
                        try {
                            if (use) {
                                mVideoEditer.setReverse(true);
                                mVideoEditer.stopPlay();
                                startPlay();
                            } else {
                                mVideoEditer.setReverse(false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onTimeSpecialFf(boolean use, long startTime) {
                    if (mVideoEditer != null) {
                        if (use) {
                            int fanfuRepeat = 500;
                            int count = 2;
                            TXVideoEditConstants.TXRepeat repeat = new TXVideoEditConstants.TXRepeat();
                            repeat.startTime = startTime;
                            repeat.endTime = startTime + fanfuRepeat;
                            repeat.repeatTimes = count;
                            mVideoEditer.setRepeatPlay(Arrays.asList(repeat));
                            previewAtTime(startTime);
                        } else {
                            mVideoEditer.stopPlay();
                            mVideoEditer.setRepeatPlay(null);
                            mVideoEditer.startPlayFromTime(mCutStartTime, mCutEndTime);
                        }
                    }
                }

                @Override
                public void onTimeSpecialMdz(boolean use, long startTime) {
                    if (mVideoEditer != null) {
                        if (use) {
                            TXVideoEditConstants.TXSpeed speed = new TXVideoEditConstants.TXSpeed();
                            speed.startTime = startTime;
                            speed.endTime = startTime + 500;
                            speed.speedLevel = TXVideoEditConstants.SPEED_LEVEL_SLOWEST;
                            mVideoEditer.setSpeedList(Arrays.asList(speed));
                            previewAtTime(startTime);
                        } else {
                            mVideoEditer.setSpeedList(null);
                        }
                    }
                }
            });
            mSpecialViewHolder.addToParent();
        }
        mSpecialViewHolder.show();
    }

    private void previewAtTime(long currentTimeMs) {
        if (mVideoEditer != null) {
            mVideoEditer.pausePlay();
            mVideoEditer.previewAtTime(currentTimeMs);
        }
        mPLayStatus = STATUS_PREVIEW_AT_TIME;
        mPreviewAtTime = currentTimeMs;
        showPlayBtn();
    }


    /**
     * 点击特效
     */
    private void clickSpecial() {
        hideGroup();
        showSpecialViewHolder();
    }

    /**
     * 点击下一步，生成视频
     */
    private void clickNext() {
        //产品要求把选择上传类型去掉
        DialogUitl.showStringArrayDialog(mContext, new Integer[]{
                R.string.video_save_pub,
                R.string.video_save_save,
                R.string.video_save_save_and_pub,
        }, new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int tag) {
                if (tag == R.string.video_save_save) {
                    mSaveType = Constants.VIDEO_SAVE_SAVE;
                } else if (tag == R.string.video_save_pub) {
                    mSaveType = Constants.VIDEO_SAVE_PUB;
                } else if (tag == R.string.video_save_save_and_pub) {
                    mSaveType = Constants.VIDEO_SAVE_SAVE_AND_PUB;
                }
                startGenerateVideo();
            }
        });
    }

    /**
     * 开始生成视频
     */
    private void startGenerateVideo() {
        L.e(TAG, "startGenerateVideo------->生成视频---水印----->" + mUseWaterMark);
        if (mVideoEditer == null) {
            return;
        }
        if (mUseWaterMark) {
            getWaterMark(new CommonCallback<Bitmap>() {
                @Override
                public void callback(Bitmap bitmap) {
                    if (mVideoEditer != null) {
                        if (bitmap != null) {
                            try {
                                TXVideoEditConstants.TXRect rect = new TXVideoEditConstants.TXRect();
                                rect.x = 0.05f;
                                rect.y = 0.04f;
                                rect.width = 0.25f;
                                mVideoEditer.setWaterMark(bitmap, rect);
                                mGenerateVideoPathWater = mGenerateVideoPath.replace(".mp4", "_water.mp4");
                                if (mFromRecord) {
                                    mVideoEditer.setVideoBitrate(6500);
                                    mVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, mGenerateVideoPathWater);
                                } else {
                                    mVideoEditer.setVideoBitrate(9600);
                                    mVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_720P, mGenerateVideoPathWater);
                                }

                            } catch (Exception e) {
                                generateFinish();
                            }
                        } else {
                            generateFinish();
                        }
                    }
                }
            });
        } else {
            mBtnNext.setEnabled(false);
            mVideoEditer.stopPlay();
            mVideoEditer.cancel();
            mVideoGenerateViewHolder = new VideoProcessViewHolder(mContext, mRoot, WordUtil.getString(R.string.video_process_2));
            mVideoGenerateViewHolder.setActionListener(new VideoProcessViewHolder.ActionListener() {
                @Override
                public void onCancelProcessClick() {
                    exit();
                }
            });
            mVideoGenerateViewHolder.addToParent();
            mVideoEditer.setCutFromTime(mCutStartTime, mCutEndTime);
            mGenerateVideoPath = StringUtil.generateVideoOutputPath();
            if (mFromRecord) {
                mVideoEditer.setVideoBitrate(6500);
                mVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, mGenerateVideoPath);
            } else {
                mVideoEditer.setVideoBitrate(9600);
                mVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_720P, mGenerateVideoPath);
            }
        }
    }

    private void showGroup() {
        if (mGroup != null && mGroup.getVisibility() != View.VISIBLE) {
            mGroup.setVisibility(View.VISIBLE);
        }
    }

    private void hideGroup() {
        if (mGroup != null && mGroup.getVisibility() == View.VISIBLE) {
            mGroup.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onBackPressed() {
        DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.video_edit_exit), new DialogUitl.SimpleCallback() {
            @Override
            public void onConfirmClick(Dialog dialog, String content) {
                exit();
            }
        });
    }

    private void exit() {
        if (mSpecialViewHolder != null && mSpecialViewHolder.isShowed()) {
            mSpecialViewHolder.hide();
            return;
        }
        if (mVolumeViewHolder != null && mVolumeViewHolder.isShowed()) {
            mVolumeViewHolder.hide();
            return;
        }
        if (mMusicViewHolder != null && mMusicViewHolder.isShowed()) {
            mMusicViewHolder.hide();
            return;
        }
        if (mFilterViewHolder != null && mFilterViewHolder.isShowed()) {
            mFilterViewHolder.hide();
            return;
        }
        release();
        deleteOriginVideoFile();
        VideoEditActivity.super.onBackPressed();
    }

    private void release() {
        if (mIsDestroy) {
            return;
        }
        mIsDestroy = true;
        if (mHandler != null) {
            mHandler.release();
        }
        if (mFilterViewHolder != null) {
            mFilterViewHolder.release();
        }
        if (mMusicViewHolder != null) {
            mMusicViewHolder.release();
        }
        if (mVolumeViewHolder != null) {
            mVolumeViewHolder.release();
        }
        if (mSpecialViewHolder != null) {
            mSpecialViewHolder.release();
        }
        if (mVideoEditer != null) {
            mVideoEditer.deleteAllEffect();
            mVideoEditer.stopPlay();
            mVideoEditer.cancel();
//            mVideoEditer.setFilter(null);
            mVideoEditer.release();
//            mVideoEditer.setVideoProcessListener(null);
//            mVideoEditer.setThumbnailListener(null);
//            mVideoEditer.setTXVideoPreviewListener(null);
//            mVideoEditer.setVideoGenerateListener(null);
        }
        if (mVideoProcessViewHolder != null) {
            mVideoProcessViewHolder.setActionListener(null);
        }
        if (mVideoGenerateViewHolder != null) {
            mVideoGenerateViewHolder.setActionListener(null);
        }
        if (mBitmapList != null) {
//            for (Bitmap bitmap : mBitmapList) {
//                if (bitmap != null && !bitmap.isRecycled()) {
//                    bitmap.recycle();
//                }
//            }
            mBitmapList.clear();
        }
        if (mBitmapMap != null) {
            mBitmapMap.clear();
        }

        mHandler = null;
        mFilterViewHolder = null;
        mVideoEditer = null;
        mMusicViewHolder = null;
        mVolumeViewHolder = null;
        mSpecialViewHolder = null;
        mVideoProcessViewHolder = null;
        mVideoGenerateViewHolder = null;
        mBitmapList = null;
        mBitmapMap = null;
    }


    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
        if (mVideoEditer != null && mPLayStatus == STATUS_PLAY) {
            mVideoEditer.pausePlay();
        }
        if (isFinishing()) {
            release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPaused) {
            if (mVideoEditer != null && mPLayStatus == STATUS_PLAY) {
                mVideoEditer.resumePlay();
            }
        }
        mPaused = false;
    }

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
        L.e(TAG, "VideoEditActivity------->onDestroy");
    }


    /**
     * 开始预处理
     */
    private void startPreProcess() {
        mVideoProcessViewHolder = new VideoProcessViewHolder(mContext, mRoot, WordUtil.getString(R.string.video_process_1));
        mVideoProcessViewHolder.addToParent();
        mVideoProcessViewHolder.setActionListener(this);
        if (mHandler == null) {
            mHandler = new MyHandler(this);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TXVideoEditConstants.TXVideoInfo info = TXVideoInfoReader.getInstance().getVideoFileInfo(mOriginVideoPath);
                    if (mHandler != null) {
                        if (info == null) {
                            mHandler.sendEmptyMessage(MyHandler.ERROR);
                        } else {
                            mHandler.sendEmptyMessage(MyHandler.SUCCESS);
                        }
                    }
                } catch (Exception e) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MyHandler.ERROR);
                    }
                }
            }
        }).start();
    }

    /**
     * 执行预处理
     */
    private void doPreProcess() {
        try {
            if (mVideoEditer != null) {
                mBitmapList = new ArrayList<>();
                mBitmapMap = new LongSparseArray<>();
                int thumbnailCount = (int) Math.floor(mVideoDuration / 1000f);
                TXVideoEditConstants.TXThumbnail thumbnail = new TXVideoEditConstants.TXThumbnail();
                thumbnail.count = thumbnailCount;
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(mOriginVideoPath);
                String sw = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                String sh = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                String orientation = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                if (mVideoRecordJoin) {//合拍的宽高除以二
                    if ("0".equals(orientation)) {
                        thumbnail.width = (int) Double.parseDouble(sw) / 2;
                        thumbnail.height = (int) Double.parseDouble(sh) / 2;
                    } else {
                        thumbnail.width = (int) Double.parseDouble(sh) / 2;
                        thumbnail.height = (int) Double.parseDouble(sw) / 2;
                    }
                } else {
                    if ("0".equals(orientation)) {
                        thumbnail.width = (int) Double.parseDouble(sw);
                        thumbnail.height = (int) Double.parseDouble(sh);
                    } else {
                        thumbnail.width = (int) Double.parseDouble(sh);
                        thumbnail.height = (int) Double.parseDouble(sw);
                    }
                }
                mmr.release();
                double ratio = thumbnail.width / ((double) thumbnail.height);
                if (thumbnail.width > 720) {
                    thumbnail.width = 720;
                }
                if (thumbnail.height > 1280) {
                    thumbnail.height = 1280;
                }
                if (ratio != 0.5625d) {
                    if (ratio < 0.5625d) {
                        thumbnail.width = (int) (thumbnail.height * ratio);
                    } else {
                        thumbnail.height = (int) (thumbnail.width / ratio);
                    }
                }
                L.e("视频缩略图", "-----宽--------> " + thumbnail.width);
                L.e("视频缩略图", "-----高--------> " + thumbnail.height);
                mVideoEditer.setThumbnail(thumbnail);
                mVideoEditer.processVideo();
            }
        } catch (Exception e) {

            e.printStackTrace();
            processFailed();
        }

    }


    /**
     * 录制结束后，视频预处理进度回调
     */
    @Override
    public void onProcessProgress(float progress) {
        //L.e(TAG, "视频预处----onProcessProgress--->" + progress);
        int p = (int) (progress * 100);
        if (p > 0 && p <= 100) {
            if (mVideoProcessViewHolder != null) {
                mVideoProcessViewHolder.setProgress(p);
            }
        }
    }

    /**
     * 录制结束后，视频预处理的回调
     */
    @Override
    public void onProcessComplete(TXVideoEditConstants.TXGenerateResult result) {
        L.e(TAG, "视频预处理result------->" + result.descMsg);
        if (result.retCode == TXVideoEditConstants.GENERATE_RESULT_OK) {
            if (mVideoProcessViewHolder != null && mVideoProcessViewHolder.getProgress() == 0) {
                if (!mSdkError) {
                    mSdkError = true;
                    sdkProgressError();
                }
            } else {
                processCompleted();
            }
        } else {
            L.e(TAG, "视频预处理错误------->" + result.descMsg);
            processFailed();
        }
    }

    private void processCompleted() {
        L.e(TAG, "视频预处理----->完成");
        if (mVideoProcessViewHolder != null) {
            mVideoProcessViewHolder.setProgress(100);
        }
        if (mHandler != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startVideoPreview();
                    if (mVideoProcessViewHolder != null) {
                        mVideoProcessViewHolder.removeFromParent();
                        mVideoProcessViewHolder.setActionListener(null);
                    }
                    mVideoProcessViewHolder = null;
                }
            }, 500);
        }
    }


    /**
     * 制结束后，获取缩略图的回调
     */
    @Override
    public void onThumbnail(int i, long l, Bitmap bitmap) {
        if (mBitmapList != null) {
            mBitmapList.add(new SoftReference<>(bitmap).get());
        }
        if (mBitmapMap != null) {
            mBitmapMap.put(l, bitmap);
        }
    }


    /**
     * 点击取消预处理的按钮
     */
    @Override
    public void onCancelProcessClick() {
        ToastUtil.show(WordUtil.getString(R.string.video_process_cancel));
        deleteOriginVideoFile();
        release();
        finish();
    }


    /**
     * 预处理失败
     */
    private void processFailed() {
        deleteOriginVideoFile();
        ToastUtil.show(R.string.video_process_failed);
        release();
        finish();
    }

    /**
     * sdk 预处理回调异常
     */
    private void sdkProgressError() {
        mSdkErrorWaitCount++;
        int p = (int) (mSdkErrorWaitCount * 100f / MAX_WAIT_COUNT);
        if (p > 0 && p <= 100) {
            if (mVideoProcessViewHolder != null) {
                mVideoProcessViewHolder.setProgress(p);
            }
        }
        if (p >= 100) {
            processCompleted();
        } else {
            if (mHandler != null) {
                mHandler.sendEmptyMessageDelayed(MyHandler.SDK_PROGRESS_ERROR, 250);
            }
        }
    }

    public List<Bitmap> getBitmapList() {
        return mBitmapList;
    }


    private static class MyHandler extends Handler {

        private static final int SUCCESS = 1;
        private static final int ERROR = 0;
        private static final int SDK_PROGRESS_ERROR = 2;//sdk 预处理回调异常

        private VideoEditActivity mVideoEditActivity;

        public MyHandler(VideoEditActivity recordActivity) {
            mVideoEditActivity = new WeakReference<>(recordActivity).get();
        }

        @Override
        public void handleMessage(Message msg) {
            if (mVideoEditActivity != null) {
                switch (msg.what) {
                    case SUCCESS:
                        mVideoEditActivity.doPreProcess();
                        break;
                    case ERROR:
                        mVideoEditActivity.processFailed();
                        break;
                    case SDK_PROGRESS_ERROR:
                        mVideoEditActivity.sdkProgressError();
                        break;
                }
            }
        }

        void release() {
            removeCallbacksAndMessages(null);
            mVideoEditActivity = null;
        }
    }


}
