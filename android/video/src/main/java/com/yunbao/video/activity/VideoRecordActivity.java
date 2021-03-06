package com.yunbao.video.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXUGCPartsManager;
import com.tencent.ugc.TXUGCRecord;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoInfoReader;
import com.tencent.ugc.TXVideoJoiner;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.bean.MusicBean;
import com.yunbao.common.custom.DrawableRadioButton2;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.MediaUtil;
import com.yunbao.common.utils.ScreenDimenUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.video.R;
import com.yunbao.video.custom.RecordProgressView;
import com.yunbao.video.custom.VideoRecordBtnView;
import com.yunbao.video.event.PicEditEvent;
import com.yunbao.video.event.VideoChooseEvent;
import com.yunbao.video.views.VideoChoosePicViewHolder;
import com.yunbao.video.views.VideoMusicViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

// +?????????????????????????????????????????????????????????????????????????????????????????????????????????
// | Created by Yunbao
// +?????????????????????????????????????????????????????????????????????????????????????????????????????????
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +?????????????????????????????????????????????????????????????????????????????????????????????????????????
// | Author: https://gitee.com/yunbaokeji
// +?????????????????????????????????????????????????????????????????????????????????????????????????????????
// | Date: 2022-04-30
// +?????????????????????????????????????????????????????????????????????????????????????????????????????????
public class VideoRecordActivity extends AbsActivity implements
        TXRecordCommon.ITXVideoRecordListener, //????????????????????????
        TXVideoJoiner.TXVideoJoinerListener//??????
{

    public static void forward(Context context, String path, String thumb, int type, long duration, int fps, int audioSampleRateType) {
        Intent intent = new Intent(context, VideoRecordActivity.class);
        intent.putExtra(Constants.VIDEO_RECORD_TYPE, type);
        intent.putExtra(Constants.VIDEO_PATH, path);
        intent.putExtra(Constants.VIDEO_THUMB, thumb);
        intent.putExtra(Constants.VIDEO_DURATION, duration);
        intent.putExtra(Constants.VIDEO_RECORD_CONFIG_FPS, fps);
        intent.putExtra(Constants.VIDEO_RECORD_AUDIO_SAMPLE_RATE_TYPE, audioSampleRateType);
        context.startActivity(intent);
    }

    public static void forward(Context context, MusicBean musicBean, int type) {
        Intent intent = new Intent(context, VideoRecordActivity.class);
        intent.putExtra(Constants.VIDEO_MUSIC_BEAN, musicBean);
        intent.putExtra(Constants.VIDEO_RECORD_TYPE, type);
        context.startActivity(intent);
    }

    private static final String TAG = "VideoRecordActivity";
    private static final int MIN_DURATION = 5000;//??????????????????5s
    private static final int MAX_DURATION = 15000;//??????????????????15s
    private static final int MAX_DURATION_VIP = 60000;//vip??????????????????60s
    private final int MSG_LOAD_VIDEO_INFO = 1000;

    //??????????????????
    private VideoRecordBtnView mVideoRecordBtnView;
    private View mRecordView;
    private View mRecordTip;
    private ValueAnimator mRecordBtnAnimator;//????????????????????????????????????
    private Drawable mRecordDrawable;
    private Drawable mUnRecordDrawable;

    /****************************/
    private boolean mRecordStarted;//????????????????????????true ????????? false ????????????
    private boolean mRecordStoped;//?????????????????????
    private boolean mRecording;//????????????????????????true ????????? false ????????????
    private ViewGroup mRoot;
    private TXCloudVideoView mVideoView;//????????????
    private RecordProgressView mRecordProgressView;//???????????????
    private TextView mTime;//????????????
    private DrawableRadioButton2 mBtnFlash;//???????????????
    private TXUGCRecord mRecorder;//?????????
    private TXRecordCommon.TXUGCCustomConfig mCustomConfig;//??????????????????
    private boolean mFrontCamera = true;//????????????????????????
    private String mVideoPath;//?????????????????????
    private int mRecordSpeed;//????????????
    private View mGroup1;
    private View mGroup2;
    private View mGroup3;//????????????
    private View mGroup4;//????????????
    private View mBtnDelete;//??????
    private View mBtnNext;//????????????????????????
    private Dialog mStopRecordDialog;//????????????????????????dialog
    private boolean mIsReachMaxRecordDuration;//??????????????????????????????
    private long mDuration;//?????????????????????
    private VideoMusicViewHolder mVideoMusicViewHolder;
    private MusicBean mMusicBean;//????????????
    private boolean mBgmPlayStarted;//???????????????????????????????????????

    private int mRecordType = Constants.VIDEO_RECORD_TYPE_RECORD;//????????????
    //???????????? ???????????????????????????????????????????????????????????????
    private TextView mBtnFifS;//15s
    private TextView mBtnSixS;//60s
    private View mVTime;//vip???????????? ??????????????????
    private long mRecordTime;
    private boolean mIsCanRecord60;//??????????????????60s??????
    //??????
    private View mFollowShotLayout;
    private TXCloudVideoView mVideoViewFollowShotRecord;
    private FrameLayout mVideoViewPlay;
    private long mFollowShotVideoDuration; // ?????????????????????ms
    // ?????????????????????TXEditer????????????????????????TXVodPlayer??????
    private TXVideoEditer mTXVideoEditer;
    private String mFollowShotVideoPath;
    // ????????????
    private TXVideoJoiner mTXVideoJoiner;
    private int mFollowShotVideoFps; // ???????????????fps
    private int mFollowShotAudioSampleRateType;// ??????????????????????????????
    private BackGroundHandler mBgHandler;
    private HandlerThread mHandlerThread;
    private TXVideoEditConstants.TXVideoInfo mRecordVideoInfo;
    private TXVideoEditConstants.TXVideoInfo mFollowVideoInfo;
    private String mFollowShotVideoOutputPath;
    private boolean isReadyJoin = false;
    private int mMaxDuration;
    private View mBtnMusic;//????????????
    private View mViewSpeed;//????????????view
    private ImageView mIvCover;//?????????????????????
    private String mVideoThumb;//?????????????????????url
    private int mVipSwitch;//vip??????
    private boolean isDestroyed = false;

    private VideoChoosePicViewHolder mVideoChoosePicViewHolder;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_record;
    }

    @Override
    protected boolean isStatusBarWhite() {
        return true;
    }

    @Override
    protected void main() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //??????????????????
        mVideoRecordBtnView = (VideoRecordBtnView) findViewById(R.id.record_btn_view);
        mRecordView = findViewById(R.id.record_view);
        mRecordTip = findViewById(R.id.record_tip);
        mUnRecordDrawable = ContextCompat.getDrawable(mContext, R.drawable.bg_btn_record_1);
        mRecordDrawable = ContextCompat.getDrawable(mContext, R.drawable.bg_btn_record_2);
        mRecordBtnAnimator = ValueAnimator.ofFloat(100, 0);
        mRecordBtnAnimator.setDuration(500);
        mRecordBtnAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                if (mVideoRecordBtnView != null) {
                    mVideoRecordBtnView.setRate((int) v);
                }
            }
        });
        mRecordBtnAnimator.setRepeatCount(-1);
        mRecordBtnAnimator.setRepeatMode(ValueAnimator.REVERSE);

        /****************************/
        mRoot = (ViewGroup) findViewById(R.id.root);
        mGroup1 = findViewById(R.id.group_1);
        mGroup2 = findViewById(R.id.group_2);
        mGroup3 = findViewById(R.id.group_3);
        mBtnDelete = findViewById(R.id.btn_delete);
        mGroup4 = findViewById(R.id.group_4);
        mVideoView = (TXCloudVideoView) findViewById(R.id.video_view);
        mTime = findViewById(R.id.time);
        mRecordProgressView = (RecordProgressView) findViewById(R.id.record_progress_view);
        mBtnFlash = (DrawableRadioButton2) findViewById(R.id.btn_flash);
        mBtnNext = findViewById(R.id.btn_next);
        //??????
        mBtnFifS = findViewById(R.id.text_15);
        mBtnSixS = findViewById(R.id.text_60);
        mVTime = findViewById(R.id.layout_time);
        //??????
        mFollowShotLayout = findViewById(R.id.follow_shot_layout);
        mVideoViewPlay = (FrameLayout) findViewById(R.id.video_view_follow_shot_play);
        mVideoViewFollowShotRecord = (TXCloudVideoView) findViewById(R.id.video_view_follow_shot_record);
        //????????????
        mBtnFifS.setOnClickListener(mRecordTimeSelectOnClickListener);
        mBtnSixS.setOnClickListener(mRecordTimeSelectOnClickListener);
        mBtnFifS.setSelected(true);

        mBtnMusic = findViewById(R.id.btn_music);
        mViewSpeed = findViewById(R.id.rg_speed);
        mIvCover = findViewById(R.id.cover);
        getData();
        updateView();

        initCameraRecord();
        startCameraPreview();
        if (mRecordType == Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT) {
            initPlayer();
        }
        EventBus.getDefault().register(this);
    }

    public MusicBean getMusicBean() {
        return mMusicBean;
    }

    public int getRecordType() {
        return mRecordType;
    }

    private void getData() {
        Intent intent = getIntent();
        if (intent != null) {
            mRecordType = intent.getIntExtra(Constants.VIDEO_RECORD_TYPE, Constants.VIDEO_RECORD_TYPE_RECORD);
            if (mRecordType == Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT) {
                //?????????
                mFollowShotVideoPath = intent.getStringExtra(Constants.VIDEO_PATH);
                mFollowShotVideoDuration = intent.getLongExtra(Constants.VIDEO_DURATION, 0);
                mVideoThumb = intent.getStringExtra(Constants.VIDEO_THUMB);
                //initPlayer();
                // ?????????????????????????????????????????????????????????fps??????????????????fps??????
                mMaxDuration = (int) mFollowShotVideoDuration;
                mFollowShotVideoFps = intent.getIntExtra(Constants.VIDEO_RECORD_CONFIG_FPS, 30);
                mFollowShotAudioSampleRateType = intent.getIntExtra(Constants.VIDEO_RECORD_AUDIO_SAMPLE_RATE_TYPE, 20);

                // ???????????????
                mFollowShotLayout.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams layoutParams = mFollowShotLayout.getLayoutParams();
                layoutParams.height = ScreenDimenUtil.getInstance().getScreenWdith() / 2 * 16 / 9;
                mFollowShotLayout.setLayoutParams(layoutParams);
                mVideoView = mVideoViewFollowShotRecord;

                ViewGroup.LayoutParams lpCover = mIvCover.getLayoutParams();
                lpCover.width = ScreenDimenUtil.getInstance().getScreenWdith() / 2;
                lpCover.height = ScreenDimenUtil.getInstance().getScreenWdith() / 2 * 16 / 9;
                mIvCover.setLayoutParams(lpCover);
                if (!TextUtils.isEmpty(mVideoThumb)) {
                    ImgLoader.display(mContext, mVideoThumb, mIvCover);
                }
                // ????????????????????????
                mTXVideoJoiner = new TXVideoJoiner(this);
                mTXVideoJoiner.setVideoJoinerListener(this);
                // ??????????????????
                mHandlerThread = new HandlerThread("FollowShotThread");
                mHandlerThread.start();
                mBgHandler = new BackGroundHandler(mHandlerThread.getLooper());
            } else {
                mMaxDuration = MAX_DURATION;
                mMusicBean = intent.getParcelableExtra(Constants.VIDEO_MUSIC_BEAN);

            }
        } else {
            mMaxDuration = MAX_DURATION;
        }
    }

    private void updateView() {
        if (mRecordType == Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT) {
            if (mVTime != null) {
                mVTime.setVisibility(View.INVISIBLE);
            }
            if (mViewSpeed != null) {
                mViewSpeed.setVisibility(View.INVISIBLE);
            }
            if (mBtnMusic != null) {
                mBtnMusic.setVisibility(View.INVISIBLE);
            }
            if (mGroup3 != null) {
                mGroup3.setVisibility(View.INVISIBLE);
            }
            if (mGroup4 != null) {
                mGroup4.setVisibility(View.INVISIBLE);
            }
            if (mIvCover != null) {
                mIvCover.setVisibility(View.VISIBLE);
            }
        } else if (mRecordType == Constants.VIDEO_RECORD_TYPE_RECORD_SAME) {
            if (mBtnMusic != null) {
                mBtnMusic.setVisibility(View.INVISIBLE);
            }
        }
        mRecordProgressView.setMaxDuration(mMaxDuration);
        mRecordProgressView.setMinDuration(MIN_DURATION);
    }

    private void setBtnMusicEnable(boolean enable) {
        if (mBtnMusic != null) {
            if (mBtnMusic.isEnabled() != enable) {
                mBtnMusic.setEnabled(enable);
                mBtnMusic.setAlpha(enable ? 1f : 0.5f);
            }
        }
    }

    private void changeRecordTime() {
        mRecordProgressView.setMaxDuration(mMaxDuration);
        mRecorder.stopCameraPreview();
        mVideoView.removeVideoView();
        mRecorder.stopBGM();
        mRecorder.setFilter(null);
        mRecorder.setVideoProcessListener(null);
        mRecorder.setVideoRecordListener(null);
        mRecorder.release();
        mRecorder = null;
        initCameraRecord();
        startCameraPreview();
    }

    /**
     * ??????????????????
     */
    private void initPlayer() {
        if (mTXVideoEditer != null) {
            return;
        }
        mTXVideoEditer = new TXVideoEditer(this);
        mTXVideoEditer.setVideoPath(mFollowShotVideoPath);
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = mVideoViewPlay;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        mTXVideoEditer.initWithPreview(param);

    }


    /**
     * ??????????????????
     */
    private void initCameraRecord() {
        mRecorder = TXUGCRecord.getInstance(CommonAppContext.getInstance());
        mRecorder.setVideoRecordListener(this);
        mRecorder.setHomeOrientation(TXLiveConstants.VIDEO_ANGLE_HOME_DOWN);
        mRecorder.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        mRecordSpeed = TXRecordCommon.RECORD_SPEED_NORMAL;
        mRecorder.setRecordSpeed(mRecordSpeed);
        mRecorder.setAspectRatio(TXRecordCommon.VIDEO_ASPECT_RATIO_9_16);
        mCustomConfig = new TXRecordCommon.TXUGCCustomConfig();
        mCustomConfig.videoResolution = TXRecordCommon.VIDEO_RESOLUTION_540_960;
        mCustomConfig.minDuration = MIN_DURATION;
        mCustomConfig.maxDuration = mMaxDuration;
        mCustomConfig.videoBitrate = 6500;
        mCustomConfig.videoFps = 30;
        mCustomConfig.videoGop = 3;
        mCustomConfig.isFront = mFrontCamera;
        if (mRecordType == Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT) {
            mCustomConfig.videoFps = mFollowShotVideoFps;
            mCustomConfig.audioSampleRate = mFollowShotAudioSampleRateType;
            mCustomConfig.needEdit = false;
            //mRecorder.setVideoRenderMode(TXRecordCommon.VIDEO_RENDER_MODE_ADJUST_RESOLUTION); //????????????????????????????????????
            mRecorder.setMute(true); // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????"?????????"???
        } else {
            mCustomConfig.videoFps = 30;
            mCustomConfig.needEdit = true;
            mRecorder.setMute(false);
        }
    }


    /**
     * ????????????
     */
    @Override
    public void onRecordEvent(int event, Bundle bundle) {
        if (event == TXRecordCommon.EVT_ID_PAUSE) {
            if (mRecordProgressView != null) {
                mRecordProgressView.clipComplete();
            }
        } else if (event == TXRecordCommon.EVT_CAMERA_CANNOT_USE) {
            ToastUtil.show(R.string.video_record_camera_failed);
        } else if (event == TXRecordCommon.EVT_MIC_CANNOT_USE) {
            ToastUtil.show(R.string.video_record_audio_failed);
        }
    }

    /**
     * ???????????? ????????????
     */
    @Override
    public void onRecordProgress(long milliSecond) {
        if (mRecordProgressView != null) {
            mRecordProgressView.setProgress((int) milliSecond);
        }
        if (mTime != null) {
            mTime.setText(StringUtil.getDurationText(milliSecond));
        }
        mRecordTime = milliSecond;
        setBtnMusicEnable(false);
        if (milliSecond >= MIN_DURATION) {//&& mRecordType != Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT
            if (mBtnNext != null && mBtnNext.getVisibility() != View.VISIBLE) {
                mBtnNext.setVisibility(View.VISIBLE);
            }
        }
        if (milliSecond >= mMaxDuration) {
            if (!mIsReachMaxRecordDuration) {
                mIsReachMaxRecordDuration = true;
                if (mRecordBtnAnimator != null) {
                    mRecordBtnAnimator.cancel();
                }
                showProcessDialog();
            }
        }
    }

    /**
     * ????????????
     */
    @Override
    public void onRecordComplete(final TXRecordCommon.TXRecordResult result) {
        mRecordStarted = false;
        mRecordStoped = true;
        if (mRecorder != null) {
            //mRecorder.toggleTorch(false);
            //mRecorder.stopBGM();
            mDuration = mRecorder.getPartsManager().getDuration();
        }
        if (result.retCode < 0) {
            hideProccessDialog();
            release();
            ToastUtil.show(R.string.video_record_failed);
            finish();
        } else {
            pauseRecord();
            if (mRecordType == Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT) {
                mBgHandler.sendEmptyMessage(MSG_LOAD_VIDEO_INFO);
            } else {
                hideProccessDialog();
                VideoEditActivity.forward(mContext, mDuration, mVideoPath, true, mMusicBean
                        , mRecordType == Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT, false);
                finish();
            }
        }
    }


    public void recordClick(View v) {
        if (mRecordStoped || !canClick()) {
            return;
        }
        int i = v.getId();
        if (i == R.id.btn_start_record) {
            clickRecord();

        } else if (i == R.id.btn_camera) {
            clickCamera();

        } else if (i == R.id.btn_flash) {
            clickFlash();

        } else if (i == R.id.btn_music) {
            clickMusic();

        } else if (i == R.id.btn_speed_1) {
            changeRecordSpeed(TXRecordCommon.RECORD_SPEED_SLOWEST);

        } else if (i == R.id.btn_speed_2) {
            changeRecordSpeed(TXRecordCommon.RECORD_SPEED_SLOW);

        } else if (i == R.id.btn_speed_3) {
            changeRecordSpeed(TXRecordCommon.RECORD_SPEED_NORMAL);

        } else if (i == R.id.btn_speed_4) {
            changeRecordSpeed(TXRecordCommon.RECORD_SPEED_FAST);

        } else if (i == R.id.btn_speed_5) {
            changeRecordSpeed(TXRecordCommon.RECORD_SPEED_FASTEST);

        } else if (i == R.id.btn_upload) {
            clickUpload();

        } else if (i == R.id.btn_delete) {
            clickDelete();

        } else if (i == R.id.btn_next) {
            clickNext();

        } else if (i == R.id.btn_pic) {
            clickPic();
        }
    }


    private View.OnClickListener mRecordTimeSelectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.isSelected()) {
                return;
            }
            float xGap = 0;
            int id = v.getId();
            if (id == R.id.text_15) {
                if (mBtnSixS.isSelected()) {
                    xGap = 1.0f / 3;
                }
                mBtnFifS.setSelected(true);
                mBtnSixS.setSelected(false);
                mMaxDuration = MAX_DURATION;
            } else if (id == R.id.text_60) {
                if (mBtnFifS.isSelected()) {
                    xGap = -1.0f / 3;
                }
                mBtnFifS.setSelected(false);
                mBtnSixS.setSelected(true);
                mMaxDuration = MAX_DURATION_VIP;
            }
            float x1 = mVTime.getTranslationX();
            float x2 = x1 + mVTime.getWidth() * xGap;
            ObjectAnimator animator = ObjectAnimator.ofFloat(mVTime, "translationX", x1, x2);
            animator.setDuration(400);
            animator.start();
            changeRecordTime();
        }
    };





    /**
     * ????????????
     */
    private void clickPic() {
        if (mVideoChoosePicViewHolder == null) {
            mVideoChoosePicViewHolder = new VideoChoosePicViewHolder(mContext, mRoot);
            mVideoChoosePicViewHolder.subscribeActivityLifeCycle();
            mVideoChoosePicViewHolder.addToParent();
        }
        mVideoChoosePicViewHolder.show();

    }

    /**
     * ???????????????
     */
    private void clickCamera() {
        if (mRecorder != null) {
            if (mBtnFlash != null && mBtnFlash.isChecked()) {
                mBtnFlash.doToggle();
                mRecorder.toggleTorch(mBtnFlash.isChecked());
            }
            mFrontCamera = !mFrontCamera;
            mRecorder.switchCamera(mFrontCamera);
        }
    }

    /**
     * ???????????????
     */
    private void clickFlash() {
        if (mFrontCamera) {
            ToastUtil.show(R.string.live_open_flash);
            return;
        }
        if (mBtnFlash != null) {
            mBtnFlash.doToggle();
            if (mRecorder != null) {
                mRecorder.toggleTorch(mBtnFlash.isChecked());
            }
        }
    }



    /**
     * ????????????
     */
    private void clickMusic() {
        if (mVideoMusicViewHolder == null) {
            mVideoMusicViewHolder = new VideoMusicViewHolder(mContext, mRoot);
            mVideoMusicViewHolder.setActionListener(new VideoMusicViewHolder.ActionListener() {
                @Override
                public void onChooseMusic(MusicBean musicBean) {
                    mMusicBean = musicBean;
                    mBgmPlayStarted = false;
                }
            });
            mVideoMusicViewHolder.addToParent();
            mVideoMusicViewHolder.subscribeActivityLifeCycle();
        }
        mVideoMusicViewHolder.show();
    }

    /**
     * ?????????????????????????????????
     */
    private void clickUpload() {
        Intent intent = new Intent(mContext, VideoChooseActivity.class);
        intent.putExtra(Constants.VIDEO_LOCAL_CAN_SELCT, mVipSwitch == 0 || mIsCanRecord60);
        startActivityForResult(intent, 0);
    }


    /**
     * ???????????????????????????
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            String videoPath = data.getStringExtra(Constants.VIDEO_PATH);
            Dialog dialog = DialogUitl.loadingDialog(mContext);
            dialog.show();
            File videoFile = new File(videoPath);
            File dir = new File(CommonAppConfig.INNER_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File copyFile = new File(dir, videoFile.getName());
            Uri inputUri = FileProvider.getUriForFile(VideoRecordActivity.this, MediaUtil.FILE_PROVIDER, videoFile);
            Uri outputUri = FileProvider.getUriForFile(VideoRecordActivity.this, MediaUtil.FILE_PROVIDER, copyFile);
            ContentResolver contentResolver = VideoRecordActivity.this.getContentResolver();
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = contentResolver.openInputStream(inputUri);
                outputStream = contentResolver.openOutputStream(outputUri);
                if (inputStream != null && outputStream != null) {
                    byte[] buf = new byte[4096];
                    int len = 0;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                }
            } catch (Exception e) {
                copyFile = null;
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
            dialog.dismiss();
            if (copyFile != null && copyFile.exists() && copyFile.length() > 0) {
                mVideoPath = copyFile.getAbsolutePath();
            } else {
                return;
            }
            mDuration = data.getLongExtra(Constants.VIDEO_DURATION, 0);
            if (mRecordType != Constants.VIDEO_RECORD_TYPE_RECORD_SAME) {
                mMusicBean = null;
            }
            VideoEditActivity.forward(mContext, mDuration, mVideoPath, false, mMusicBean,
                    mRecordType == Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT, false);
            finish();
        }
    }


    /**
     * ????????????
     */
    private void startCameraPreview() {
        if (mRecorder != null && mCustomConfig != null && mVideoView != null) {
            mRecorder.startCameraCustomPreview(mCustomConfig, mVideoView);
            if (!mFrontCamera) {
                mRecorder.switchCamera(false);
            }
        }

    }

    /**
     * ????????????
     */
    private void stopCameraPreview() {
        if (mRecorder != null) {
            if (mRecording) {
                pauseRecord();
            }
            mRecorder.stopCameraPreview();
        }
    }

    /**
     * ????????????
     */
    private void clickRecord() {
        if (mRecordStarted) {
            if (mRecording) {
                pauseRecord();
            } else {
                resumeRecord();
            }
        } else {
            startRecord();
        }
    }

    /**
     * ????????????
     */
    private void startRecord() {
        if (mRecordType == Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT && isReadyJoin) {
            // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            mRecorder.stopRecord();
            return;
        }
        if (mRecorder != null) {
            mVideoPath = StringUtil.generateVideoOutputPath();//?????????????????????
            int result = mRecorder.startRecord(mVideoPath, CommonAppConfig.VIDEO_RECORD_TEMP_PATH, null);//???????????????????????????????????????
            if (result != TXRecordCommon.START_RECORD_OK) {
                if (result == TXRecordCommon.START_RECORD_ERR_NOT_INIT) {
                    ToastUtil.show(R.string.video_record_tip_1);
                } else if (result == TXRecordCommon.START_RECORD_ERR_IS_IN_RECORDING) {
                    ToastUtil.show(R.string.video_record_tip_2);
                } else if (result == TXRecordCommon.START_RECORD_ERR_VIDEO_PATH_IS_EMPTY) {
                    ToastUtil.show(R.string.video_record_tip_3);
                } else if (result == TXRecordCommon.START_RECORD_ERR_API_IS_LOWER_THAN_18) {
                    ToastUtil.show(R.string.video_record_tip_4);
                } else if (result == TXRecordCommon.START_RECORD_ERR_LICENCE_VERIFICATION_FAILED) {
                    ToastUtil.show(R.string.video_record_tip_5);
                }
                return;
            }
        }
        mRecordStarted = true;
        mRecording = true;
        resumeBgm();
        startRecordBtnAnim();
        if (mGroup2 != null && mGroup2.getVisibility() == View.VISIBLE) {
            mGroup2.setVisibility(View.INVISIBLE);
        }

        if (mVTime != null && mVTime.getVisibility() == View.VISIBLE) {
            mVTime.setVisibility(View.INVISIBLE);
        }

        if (mRecordType == Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT) {
            if (mTXVideoEditer != null) {
                mTXVideoEditer.stopPlay();
                mTXVideoEditer.startPlayFromTime(0, mFollowShotVideoDuration);
            }

            if (mIvCover != null && mIvCover.getVisibility() == View.VISIBLE) {
                mIvCover.setVisibility(View.GONE);
            }
        }
    }


    /**
     * ????????????
     */
    private void pauseRecord() {
        if (mRecorder == null) {
            return;
        }
        pauseBgm();
        mRecorder.pauseRecord();
        mRecording = false;
        stopRecordBtnAnim();
        if (mGroup2 != null && mGroup2.getVisibility() != View.VISIBLE) {
            mGroup2.setVisibility(View.VISIBLE);
        }
        TXUGCPartsManager partsManager = mRecorder.getPartsManager();
        if (partsManager != null) {
            List<String> partList = partsManager.getPartsPathList();
            if (partList != null && partList.size() > 0) {
                if (mGroup3 != null && mGroup3.getVisibility() == View.VISIBLE) {
                    mGroup3.setVisibility(View.INVISIBLE);
                }
                if (mGroup4 != null && mGroup4.getVisibility() == View.VISIBLE) {
                    mGroup4.setVisibility(View.INVISIBLE);
                }
                if (mBtnDelete != null && mBtnDelete.getVisibility() != View.VISIBLE) {
                    mBtnDelete.setVisibility(View.VISIBLE);
                }
            } else {
                if (mGroup3 != null && mGroup3.getVisibility() != View.VISIBLE) {
                    mGroup3.setVisibility(View.VISIBLE);
                }
                if (mGroup4 != null && mGroup4.getVisibility() != View.VISIBLE) {
                    mGroup4.setVisibility(View.VISIBLE);
                }
                if (mBtnDelete != null && mBtnDelete.getVisibility() == View.VISIBLE) {
                    mBtnDelete.setVisibility(View.INVISIBLE);
                }
            }
        }

        if (mRecordType == Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT) {
            mTXVideoEditer.pausePlay();
        }
    }

    /**
     * ????????????
     */
    private void resumeRecord() {
        if (mRecorder != null) {
            int startResult = mRecorder.resumeRecord();
            if (startResult != TXRecordCommon.START_RECORD_OK) {
                ToastUtil.show(WordUtil.getString(R.string.video_record_failed));
                return;
            }
        }
        mRecording = true;
        resumeBgm();
        startRecordBtnAnim();
        if (mGroup2 != null && mGroup2.getVisibility() == View.VISIBLE) {
            mGroup2.setVisibility(View.INVISIBLE);
        }
        if (mBtnDelete != null && mBtnDelete.getVisibility() == View.VISIBLE) {
            mBtnDelete.setVisibility(View.INVISIBLE);
        }

        if (mVTime != null && mVTime.getVisibility() == View.VISIBLE) {
            mVTime.setVisibility(View.INVISIBLE);
        }

        if (mRecordType == Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT) {
            if (mTXVideoEditer != null) {
                int recordPostion = mRecorder.getPartsManager().getDuration();
                mTXVideoEditer.startPlayFromTime(recordPostion, mFollowShotVideoDuration);
            }
        }
    }

    /**
     * ??????????????????
     */
    private void pauseBgm() {
        if (mRecorder != null) {
            mRecorder.pauseBGM();
        }
    }

    /**
     * ??????????????????
     */
    private void resumeBgm() {
        if (mRecorder == null) {
            return;
        }
        if (!mBgmPlayStarted) {
            if (mMusicBean == null) {
                return;
            }
            int bgmDuration = mRecorder.setBGM(mMusicBean.getLocalPath());
            mRecorder.playBGMFromTime(0, bgmDuration);
            mRecorder.setBGMVolume(1);//????????????1??????
            mRecorder.setMicVolume(0);//????????????0
            mBgmPlayStarted = true;
        } else {
            mRecorder.resumeBGM();
        }
    }

    /**
     * ????????????????????????
     */
    private void startRecordBtnAnim() {
        if (mRecordView != null) {
            mRecordView.setBackground(mRecordDrawable);
        }
        if (mRecordBtnAnimator != null) {
            mRecordBtnAnimator.start();
        }
        if (mRecordTip != null && mRecordTip.getVisibility() == View.VISIBLE) {
            mRecordTip.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * ????????????????????????
     */
    private void stopRecordBtnAnim() {
        if (mRecordView != null) {
            mRecordView.setBackground(mUnRecordDrawable);
        }
        if (mRecordBtnAnimator != null) {
            mRecordBtnAnimator.cancel();
        }
        if (mVideoRecordBtnView != null) {
            mVideoRecordBtnView.reset();
        }
        if (mRecordTip != null && mRecordTip.getVisibility() != View.VISIBLE) {
            mRecordTip.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ??????????????????
     */
    private void changeRecordSpeed(int speed) {
        if (mRecordSpeed == speed) {
            return;
        }
        mRecordSpeed = speed;
        if (mRecorder != null) {
            mRecorder.setRecordSpeed(speed);
        }
    }

    /**
     * ??????????????????
     */
    private void clickDelete() {
        DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.video_record_delete_last), new DialogUitl.SimpleCallback() {
            @Override
            public void onConfirmClick(Dialog dialog, String content) {
                doClickDelete();
            }
        });
    }

    /**
     * ??????????????????
     */
    private void doClickDelete() {
        if (!mRecordStarted || mRecording || mRecorder == null) {
            return;
        }
        TXUGCPartsManager partsManager = mRecorder.getPartsManager();
        if (partsManager == null) {
            return;
        }
        List<String> partList = partsManager.getPartsPathList();
        if (partList == null || partList.size() == 0) {
            return;
        }
        partsManager.deleteLastPart();
        int time = partsManager.getDuration();
        if (mTime != null) {
            mTime.setText(StringUtil.getDurationText(time));
        }
        mRecordTime = time;
        setBtnMusicEnable(time == 0);
        if (time < MIN_DURATION && mBtnNext != null && mBtnNext.getVisibility() == View.VISIBLE) {
            mBtnNext.setVisibility(View.INVISIBLE);
        }
        if (mRecordProgressView != null) {
            mRecordProgressView.deleteLast();
        }
        partList = partsManager.getPartsPathList();
        if (partList != null && partList.size() == 0) {
            if (mGroup2 != null && mGroup2.getVisibility() != View.VISIBLE) {
                mGroup2.setVisibility(View.VISIBLE);
            }
            if (mBtnDelete != null && mBtnDelete.getVisibility() == View.VISIBLE) {
                mBtnDelete.setVisibility(View.INVISIBLE);
            }
            if (mRecordType != Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT) {
                if (mGroup3 != null && mGroup3.getVisibility() != View.VISIBLE) {
                    mGroup3.setVisibility(View.VISIBLE);
                }
                if (mGroup4 != null && mGroup4.getVisibility() != View.VISIBLE) {
                    mGroup4.setVisibility(View.VISIBLE);
                }
                if (mVipSwitch == 1 && mVTime != null && mVTime.getVisibility() != View.VISIBLE) {
                    mVTime.setVisibility(View.VISIBLE);
                }
            }
        }
        isReadyJoin = false;
    }

    /**
     * ???????????????????????? onRecordComplete
     */
    private void clickNext() {
        stopRecordBtnAnim();
        if (mRecorder != null) {
            mRecorder.stopBGM();
            mRecorder.stopRecord();
            showProcessDialog();
        }
    }


    /**
     * ??????????????????????????????????????????
     */
    private void showProcessDialog() {
        if (mStopRecordDialog == null && mContext != null) {
            mStopRecordDialog = DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.video_processing));
            mStopRecordDialog.show();
        }
    }

    /**
     * ????????????????????????
     */
    private void hideProccessDialog() {
        if (mStopRecordDialog != null) {
            mStopRecordDialog.dismiss();
        }
        mStopRecordDialog = null;
    }


    @Override
    public void onBackPressed() {
        if (!canClick()) {
            return;
        }
        if (mVideoMusicViewHolder != null && mVideoMusicViewHolder.isShowed()) {
            mVideoMusicViewHolder.hide();
            return;
        }
        if (mVideoChoosePicViewHolder != null && mVideoChoosePicViewHolder.isShowed()) {
            mVideoChoosePicViewHolder.hide();
            return;
        }
        List<Integer> list = new ArrayList<>();
        if (mRecordTime > 0) {
            list.add(R.string.video_re_record);
        }
        list.add(R.string.video_exit);
        DialogUitl.showStringArrayDialog(mContext, list.toArray(new Integer[list.size()]), new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int tag) {
                if (tag == R.string.video_re_record) {
                    reRecord();
                } else if (tag == R.string.video_exit) {
                    finish();
                }
            }
        });
    }

    /**
     * ????????????
     */
    private void reRecord() {
        if (mRecorder == null) {
            return;
        }
        mRecorder.pauseBGM();
        mMusicBean = null;
        mBgmPlayStarted = false;
        mRecorder.pauseRecord();
        mRecording = false;
        stopRecordBtnAnim();
        if (mGroup2 != null && mGroup2.getVisibility() != View.VISIBLE) {
            mGroup2.setVisibility(View.VISIBLE);
        }
        TXUGCPartsManager partsManager = mRecorder.getPartsManager();
        if (partsManager != null) {
            partsManager.deleteAllParts();
        }
        mRecorder.onDeleteAllParts();
        if (mTime != null) {
            mTime.setText("00:00");
        }
        mRecordTime = 0;
        setBtnMusicEnable(true);
        if (mBtnNext != null && mBtnNext.getVisibility() == View.VISIBLE) {
            mBtnNext.setVisibility(View.INVISIBLE);
        }
        if (mRecordProgressView != null) {
            mRecordProgressView.deleteAll();
        }
        if (mRecordType != Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT) {
            if (mGroup3 != null && mGroup3.getVisibility() != View.VISIBLE) {
                mGroup3.setVisibility(View.VISIBLE);
            }
            if (mGroup4 != null && mGroup4.getVisibility() != View.VISIBLE) {
                mGroup4.setVisibility(View.VISIBLE);
            }
        }
        if (mBtnDelete != null && mBtnDelete.getVisibility() == View.VISIBLE) {
            mBtnDelete.setVisibility(View.INVISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPicEdit(PicEditEvent e) {
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPicEdit(VideoChooseEvent e) {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //startCameraPreview();
    }


    @Override
    protected void onStop() {
        super.onStop();
        //stopCameraPreview();
        if (mRecorder != null && mBtnFlash != null && mBtnFlash.isChecked()) {
            mBtnFlash.doToggle();
            mRecorder.toggleTorch(mBtnFlash.isChecked());
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            release();
        }
    }


    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }

    private void release() {
        if (isDestroyed) {
            return;
        }
        stopCameraPreview();
        isDestroyed = true;
        EventBus.getDefault().unregister(this);
        if (mStopRecordDialog != null && mStopRecordDialog.isShowing()) {
            mStopRecordDialog.dismiss();
        }
        if (mVideoMusicViewHolder != null) {
            mVideoMusicViewHolder.release();
        }
        if (mRecordProgressView != null) {
            mRecordProgressView.release();
        }
        if (mRecordBtnAnimator != null) {
            mRecordBtnAnimator.cancel();
        }
        if (mVideoChoosePicViewHolder != null) {
            mVideoChoosePicViewHolder.release();
        }
        if (mHandlerThread != null) {
            if (mHandlerThread.getLooper() != null) {
                mHandlerThread.getLooper().quit();
            }
        }

        if (mBgHandler != null) {
            mBgHandler.removeCallbacksAndMessages(null);
            mBgHandler = null;
        }

        if (mRecorder != null) {
            mRecorder.toggleTorch(false);
            mRecorder.stopBGM();
            if (mRecordStarted) {
                mRecorder.stopRecord();
            }
            mRecorder.release();
            mRecorder.setFilter(null);
            mRecorder.stopCameraPreview();
            mRecorder.setVideoProcessListener(null);
            mRecorder.setVideoRecordListener(null);
            TXUGCPartsManager getPartsManager = mRecorder.getPartsManager();
            if (getPartsManager != null) {
                getPartsManager.deleteAllParts();
            }

        }


        if (mTXVideoEditer != null) {
            mTXVideoEditer.stopPlay();
            mTXVideoEditer.cancel();
            mTXVideoEditer.release();
            mTXVideoEditer.setTXVideoPreviewListener(null);
        }

        if (mTXVideoJoiner != null) {
            mTXVideoJoiner.setVideoJoinerListener(null);
            mTXVideoJoiner.cancel();
        }
        mTXVideoEditer = null;
        mTXVideoJoiner = null;


        mStopRecordDialog = null;
        mVideoMusicViewHolder = null;
        mRecordProgressView = null;
        mRecordBtnAnimator = null;
        mRecorder = null;
        mVideoChoosePicViewHolder = null;
        L.e(TAG, "-------->release");
    }


    //????????????
    @Override
    public void onJoinProgress(float v) {
    }


    @Override
    public void onJoinComplete(TXVideoEditConstants.TXJoinerResult result) {
        hideProccessDialog();
        if (result.retCode == TXVideoEditConstants.JOIN_RESULT_OK) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isReadyJoin = true;
                    VideoEditActivity.forward(mContext, mDuration, mFollowShotVideoOutputPath, true, mMusicBean
                            , mRecordType == Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT, true);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.show(WordUtil.getString(R.string.video_record_join_complete_failed));
                }
            });
        }
        finish();
    }


    class BackGroundHandler extends Handler {
        private BackGroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_LOAD_VIDEO_INFO) {
                mRecordVideoInfo = TXVideoInfoReader.getInstance().getVideoFileInfo(mVideoPath);
                mFollowVideoInfo = TXVideoInfoReader.getInstance().getVideoFileInfo(mFollowShotVideoPath);
                prepareToJoiner();
            }
        }
    }


    private void prepareToJoiner() {
        L.e("---prepareToJoiner--");
        try {
            List<String> videoSourceList = new ArrayList<>();
            videoSourceList.add(mVideoPath);
            videoSourceList.add(mFollowShotVideoPath);
            mTXVideoJoiner.setVideoPathList(videoSourceList);
            mFollowShotVideoOutputPath = StringUtil.generateVideoOutputPath();
            // ?????????????????????????????????????????????????????????????????????
            int followVideoWidth;
            int followVideoHeight;
            if ((float) mFollowVideoInfo.width / mFollowVideoInfo.height >= (float) mRecordVideoInfo.width / mRecordVideoInfo.height) {
                followVideoWidth = mRecordVideoInfo.width;
                followVideoHeight = (int) ((float) mRecordVideoInfo.width * mFollowVideoInfo.height / mFollowVideoInfo.width);
            } else {
                followVideoWidth = (int) ((float) mRecordVideoInfo.height * mFollowVideoInfo.width / mFollowVideoInfo.height);
                followVideoHeight = mRecordVideoInfo.height;
            }

            TXVideoEditConstants.TXAbsoluteRect rect1 = new TXVideoEditConstants.TXAbsoluteRect();
            rect1.x = 0;                     //?????????????????????????????????
            rect1.y = 0;
            rect1.width = mRecordVideoInfo.width;   //????????????????????????
            rect1.height = mRecordVideoInfo.height;

            TXVideoEditConstants.TXAbsoluteRect rect2 = new TXVideoEditConstants.TXAbsoluteRect();
            rect2.x = rect1.x + rect1.width; //???2???????????????????????????
            rect2.y = (mRecordVideoInfo.height - followVideoHeight) / 2;
            rect2.width = followVideoWidth;  //???2??????????????????
            rect2.height = followVideoHeight;

            List<TXVideoEditConstants.TXAbsoluteRect> list = new ArrayList<>();
            list.add(rect1);
            list.add(rect2);
            mTXVideoJoiner.setSplitScreenList(list, mRecordVideoInfo.width + followVideoWidth, mRecordVideoInfo.height); //???2???3???param????????????????????????????????????
            mTXVideoJoiner.splitJoinVideo(TXVideoEditConstants.VIDEO_COMPRESSED_480P, mFollowShotVideoOutputPath);
        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.show(WordUtil.getString(R.string.video_record_join_complete_failed));
                    finish();
                }
            });
            L.e("---Exception--" + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRecorder != null) {
            mRecorder.switchCamera(mFrontCamera);
        }
    }
}
