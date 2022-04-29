package com.yunbao.video.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.utils.BitmapUtil;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.video.R;
import com.yunbao.common.bean.MusicBean;
import com.yunbao.video.event.PicEditEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
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

public class PictureEditActivity extends AbsActivity implements TXVideoEditer.TXVideoPreviewListener, View.OnClickListener, TXVideoEditer.TXVideoGenerateListener {
    private static final String TAG = PictureEditActivity.class.getSimpleName();
    public static final int STATE_NONE = 0;
    public static final int STATE_PLAY = 1;
    public static final int STATE_RESUME = 2;
    public static final int STATE_PAUSE = 3;
    public static final int STATE_STOP = 4;
    public static final int STATE_GENERATE = 5;
    private TextView mBtnNext;
    private FrameLayout mPlayer;
    private TXVideoEditer mTXVideoEditer;
    private ArrayList<String> picPathList;
    private ArrayList<Bitmap> bitmapList;
    private int mCurrentState = STATE_STOP;// 播放器当前状态
    private String mVideoOutputPath;
    private long mVideoDuration;
    private boolean mIsDestroy;
    private Dialog mDialog;
    private MusicBean mMusicBean;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_picture_edit;
    }

    @Override
    protected void main() {
        super.main();
        Intent intent = getIntent();
        if (intent==null){
            return;
        }
        picPathList = intent.getStringArrayListExtra(Constants.INTENT_KEY_MULTI_PIC_LIST);
        mMusicBean = intent.getParcelableExtra(Constants.VIDEO_MUSIC_BEAN);
        if (picPathList == null || picPathList.size() == 0) {
            finish();
            return;
        }
        mTXVideoEditer = new TXVideoEditer(this);
        mTXVideoEditer.setTXVideoPreviewListener(this);
        decodeFileToBitmap(picPathList);
        int result = mTXVideoEditer.setPictureList(bitmapList, 20);
        if (result == TXVideoEditConstants.PICTURE_TRANSITION_FAILED) {
            Toast.makeText(this, getResources().getString(R.string.picture_join_error),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 注意：
        // 1、接口调用顺序：setPictureList在前，setPicTransferType在后，必须顺序调用
        // 1、图片转视频的时长需要设置转场类型后获取，因为不同的转场类型时长会不一样
        // 2、宽高信息sdk内部会处理成9：16比例，上层只有在加片尾水印的时候算归一化坐标用到，所以这里可以设置成720P（720 * 1280）或者540P（540 * 960）来计算。注意最终视频的分辨率是按照生成时传的参数决定的。
        // （5.0以前版本是按照第一张图片的宽高来决定最终的宽高，导致的问题是如果第一张图片有一边比较短，后面的图片会以最短边等比例缩放，显示出来就小了）
        mVideoDuration = mTXVideoEditer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_LEFT_RIGHT_SLIPPING);
        initViews();
        initPlayerLayout();
    }

    private void initPlayerLayout() {
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = mPlayer;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        mTXVideoEditer.initWithPreview(param);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPlay();
    }

    private void startPlay() {
        if (mCurrentState == STATE_NONE || mCurrentState == STATE_STOP) {
            if (mTXVideoEditer != null) {
                mTXVideoEditer.startPlayFromTime(0, mVideoDuration);
                mCurrentState = STATE_PLAY;
            }
        }
    }

    private void stopPlay() {
        if (mCurrentState == STATE_PLAY) {
            mTXVideoEditer.stopPlay();
            mCurrentState = STATE_STOP;
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


    private void initViews() {
        mBtnNext = (TextView) findViewById(R.id.btn_next);
        mPlayer = (FrameLayout) findViewById(R.id.layout_palyer);
        mBtnNext.setOnClickListener(this);
        findViewById(R.id.transition1).setOnClickListener(this);
        findViewById(R.id.transition2).setOnClickListener(this);
        findViewById(R.id.transition3).setOnClickListener(this);
        findViewById(R.id.transition4).setOnClickListener(this);
        findViewById(R.id.transition5).setOnClickListener(this);
        findViewById(R.id.transition6).setOnClickListener(this);
        findViewById(R.id.btn_music).setOnClickListener(this);
        findViewById(R.id.btn_music_volume).setOnClickListener(this);
    }

    private void decodeFileToBitmap(List<String> picPathList) {
        bitmapList = new ArrayList<>();
        for (int i = 0; i < picPathList.size(); i++) {
            String filePath = picPathList.get(i);
            Bitmap bitmap = BitmapUtil.decodeSampledBitmapFromFile(filePath, 720, 1280);
            bitmapList.add(bitmap);
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_next) {//开始预处理
            clickNext();
        } else {
            long duration = mVideoDuration;
            stopPlay();
            if (id == R.id.transition1) { //左右
                duration = mTXVideoEditer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_LEFT_RIGHT_SLIPPING);
            } else if (id == R.id.transition2) { //上下
                duration = mTXVideoEditer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_UP_DOWN_SLIPPING);
            } else if (id == R.id.transition3) { //放大
                duration = mTXVideoEditer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_ENLARGE);
            } else if (id == R.id.transition4) { //缩小
                duration = mTXVideoEditer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_NARROW);
            } else if (id == R.id.transition5) { //旋转
                duration = mTXVideoEditer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_ROTATIONAL_SCALING);
            } else if (id == R.id.transition6) { //淡入淡出
                duration = mTXVideoEditer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_FADEIN_FADEOUT);
            }
            mVideoDuration = duration;
            startPlay();
        }
    }


    /**
     * 显示处理中弹窗
     */
    private void showProcessDialog() {
        if (mDialog == null && mContext != null) {
            mDialog = DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.video_processing));
            mDialog.show();
        }
    }

    /**
     * 隐藏处理中的弹窗
     */
    private void hideProcessDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        mDialog = null;
    }

    /**
     * 点击下一步，生成视频
     */
    private void clickNext() {
        startGenerateVideo();
    }

    private void startGenerateVideo() {
        mTXVideoEditer.stopPlay();
        mCurrentState = STATE_GENERATE;
        mVideoOutputPath = StringUtil.generateVideoOutputPath();
        showProcessDialog();
        mTXVideoEditer.setVideoGenerateListener(this);
        mTXVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_720P, mVideoOutputPath);
    }

    private void stopGenerate() {
        if (mCurrentState == STATE_GENERATE) {
            hideProcessDialog();
            mCurrentState = STATE_NONE;
        }
    }


    /**
     * 生成视频结束回调
     */
    private void generateFinish() {
        hideProcessDialog();
        startPublish();
        finish();
    }

    private void release() {
        if (mIsDestroy) {
            return;
        }
        mIsDestroy = true;
        if (mTXVideoEditer != null) {
            mTXVideoEditer.deleteAllEffect();
            mTXVideoEditer.stopPlay();
            mTXVideoEditer.cancel();
            mTXVideoEditer.release();
//            mTXVideoEditer.setVideoProcessListener(null);
//            mTXVideoEditer.setThumbnailListener(null);
//            mTXVideoEditer.setTXVideoPreviewListener(null);
//            mTXVideoEditer.setVideoGenerateListener(null);

        }
        mTXVideoEditer = null;
    }

    private void exit() {
        stopGenerate();
        release();
        finish();
    }

    //生成进度回调
    @Override
    public void onGenerateProgress(final float progress) {
    }

    // 生成完成
    @Override
    public void onGenerateComplete(final TXVideoEditConstants.TXGenerateResult result) {
        if (result.retCode == TXVideoEditConstants.GENERATE_RESULT_OK) {
            generateFinish();
        } else {
            ToastUtil.show(R.string.video_generate_failed);
            hideProcessDialog();
        }
        mCurrentState = STATE_NONE;
    }

    private void startPublish() {
        mTXVideoEditer.release();
        VideoEditActivity.forward(mContext, mVideoDuration, mVideoOutputPath, false,  mMusicBean);
        finish();
        EventBus.getDefault().post(new PicEditEvent());
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

    @Override
    public void onPreviewProgress(int i) {

    }

    @Override
    public void onPreviewFinished() {
        if (mTXVideoEditer != null) {
            mTXVideoEditer.startPlayFromTime(0, mVideoDuration);
            mCurrentState = STATE_PLAY;
        }
    }
}
