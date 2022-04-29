package com.yunbao.video.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.upload.UploadBean;
import com.yunbao.common.upload.UploadCallback;
import com.yunbao.common.upload.UploadStrategy;
import com.yunbao.common.upload.UploadUtil;
import com.yunbao.common.utils.DateFormatUtil;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.MediaRecordUtil;
import com.yunbao.common.utils.ScreenDimenUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.video.R;
import com.yunbao.video.activity.AbsVideoCommentActivity;
import com.yunbao.video.activity.AbsVideoPlayActivity;
import com.yunbao.video.bean.VideoCommentBean;
import com.yunbao.video.event.VideoCommentEvent;
import com.yunbao.video.http.VideoHttpConsts;
import com.yunbao.video.http.VideoHttpUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
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
public class VideoCommentVoiceViewHolder extends AbsViewHolder implements View.OnClickListener {

    private static final String TAG = "VideoCommentVoiceViewHolder";
    private View mRecordTip;
    private TextView mBtnRecord;
    private String mPress;
    private String mPressString;
    private String mUnPress;
    private int mCancelHeight;
    private MediaRecordUtil mMediaRecordUtil;
    private File mRecordVoiceFile;//录音文件
    private long mRecordVoiceDuration;//录音时长
    private Handler mHandler;
    private boolean mIsRecording;
    private String mVideoId;
    private String mVideoUid;
    private VideoCommentBean mVideoCommentBean;
    private Dialog mDialog;


    public VideoCommentVoiceViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_comment_voice;
    }

    @Override
    public void init() {
        mCancelHeight = ScreenDimenUtil.getInstance().getScreenHeight() - DpUtil.dp2px(41);
        mPressString = WordUtil.getString(R.string.im_press_say);
        mUnPress = WordUtil.getString(R.string.im_unpress_stop);
        findViewById(R.id.btn_hide).setOnClickListener(this);
        mRecordTip = findViewById(R.id.record_tip);
        mBtnRecord = (TextView) findViewById(R.id.btn_record);
        mBtnRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        recordStart();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (e.getRawY() < mCancelHeight) {
                            recordCancel();
                        } else {
                            recordEnd();
                        }
                        break;
                }
                return true;
            }
        });
    }


    @Override
    public void addToParent() {
        super.addToParent();
        if (mBtnRecord != null) {
            mBtnRecord.setText(mPress);
        }
        if (mRecordTip != null && mRecordTip.getVisibility() == View.VISIBLE) {
            mRecordTip.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_hide) {
            removeFromParent();
        }
    }

    /**
     * 开始录制
     */
    private void recordStart() {
        if (mIsRecording) {
            return;
        }
        L.e(TAG, "开始录制----------->");
        if (mBtnRecord != null) {
            mBtnRecord.setText(mUnPress);
        }
        if (mRecordTip != null && mRecordTip.getVisibility() != View.VISIBLE) {
            mRecordTip.setVisibility(View.VISIBLE);
        }
        if (mContext instanceof AbsVideoPlayActivity) {
            ((AbsVideoPlayActivity) mContext).setMute(true);
        }
        if (mMediaRecordUtil == null) {
            mMediaRecordUtil = new MediaRecordUtil();
        }
        File dir = new File(CommonAppConfig.MUSIC_PATH + "comment");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        mRecordVoiceFile = new File(dir, DateFormatUtil.getCurTimeString() + ".m4a");
        mMediaRecordUtil.startRecord(mRecordVoiceFile.getAbsolutePath());
        if (mHandler == null) {
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    recordEnd();
                }
            };
        }
        mHandler.sendEmptyMessageDelayed(0, 60000);
        mIsRecording = true;
    }

    /**
     * 结束录制
     */
    private void recordEnd() {
        if (!mIsRecording) {
            return;
        }
        L.e(TAG, "结束录制----------->");
        if (mBtnRecord != null) {
            mBtnRecord.setText(mPress);
        }
        if (mRecordTip != null && mRecordTip.getVisibility() == View.VISIBLE) {
            mRecordTip.setVisibility(View.INVISIBLE);
        }
        mRecordVoiceDuration = mMediaRecordUtil.stopRecord();
        if (mRecordVoiceDuration < 2000) {
            ToastUtil.show(WordUtil.getString(R.string.im_record_audio_too_short));
            deleteVoiceFile();
        } else {
            if (mRecordVoiceFile != null && mRecordVoiceFile.length() > 0) {
                L.e(TAG, "录制成功----------->");
                uploadVoiceFile();
            }
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mIsRecording = false;
        if (mContext instanceof AbsVideoPlayActivity) {
            ((AbsVideoPlayActivity) mContext).setMute(false);
        }
    }

    /**
     * 取消录制
     */
    private void recordCancel() {
        if (!mIsRecording) {
            return;
        }
        L.e(TAG, "取消录制----------->");
        if (mBtnRecord != null) {
            mBtnRecord.setText(mPress);
        }
        if (mRecordTip != null && mRecordTip.getVisibility() == View.VISIBLE) {
            mRecordTip.setVisibility(View.INVISIBLE);
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mMediaRecordUtil.stopRecord();
        deleteVoiceFile();
        ToastUtil.show(R.string.video_comment_voice_tip_1);
        mIsRecording = false;
        if (mContext instanceof AbsVideoPlayActivity) {
            ((AbsVideoPlayActivity) mContext).setMute(false);
        }
    }

    /**
     * 删除录音文件
     */
    private void deleteVoiceFile() {
        if (mRecordVoiceFile != null && mRecordVoiceFile.exists()) {
            mRecordVoiceFile.delete();
        }
        mRecordVoiceFile = null;
        mRecordVoiceDuration = 0;
    }

    public void release() {
        VideoHttpUtil.cancel(VideoHttpConsts.SET_COMMENT);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        if (mMediaRecordUtil != null) {
            mMediaRecordUtil.release();
        }
        mMediaRecordUtil = null;
    }


    /**
     * 上传语音文件
     */
    private void uploadVoiceFile() {
        if (mDialog == null) {
            mDialog = DialogUitl.loadingDialog(mContext);
        }
        mDialog.show();

        UploadUtil.startUpload(new CommonCallback<UploadStrategy>() {
            @Override
            public void callback(UploadStrategy strategy) {
                if (strategy != null) {
                    List<UploadBean> list = new ArrayList<>();
                    list.add(new UploadBean(mRecordVoiceFile, UploadBean.VOICE));
                    strategy.upload(list, false, new UploadCallback() {
                        @Override
                        public void onFinish(List<UploadBean> list, boolean success) {
                            if(success){
                                String voiceLink = list.get(0).getRemoteFileName();
                                sendComment(voiceLink);
                            }else{
                                if (mDialog != null) {
                                    mDialog.dismiss();
                                }
                                ToastUtil.show(WordUtil.getString(R.string.comment_failed));
                            }
                        }
                    });
                }
            }
        });
    }


    /**
     * 发表评论
     */
    public void sendComment(String voiceLink) {
        if (TextUtils.isEmpty(mVideoId) || TextUtils.isEmpty(mVideoUid) || mRecordVoiceDuration == 0) {
            return;
        }
        if (mDialog != null) {
            mDialog.dismiss();
        }
        String toUid = mVideoUid;
        String commentId = "0";
        String parentId = "0";
        if (mVideoCommentBean != null) {
            toUid = mVideoCommentBean.getUid();
            commentId = mVideoCommentBean.getCommentId();
            parentId = mVideoCommentBean.getId();
        }
        VideoHttpUtil.setVoiceComment(toUid, mVideoId, commentId, parentId, voiceLink, (int) (mRecordVoiceDuration / 1000), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    String commentNum = obj.getString("comments");
                    EventBus.getDefault().post(new VideoCommentEvent(mVideoId, commentNum));
                    ToastUtil.show(msg);
                    removeFromParent();
                    ((AbsVideoCommentActivity) mContext).refreshComment();
                }
            }

            @Override
            public void onFinish() {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        });
    }

    public void setVideoId(String videoId) {
        mVideoId = videoId;
    }

    public void setVideoUid(String videoUid) {
        mVideoUid = videoUid;
    }

    public void setVideoCommentBean(VideoCommentBean videoCommentBean) {
        mVideoCommentBean = videoCommentBean;
        mPress = mPressString;
        if (videoCommentBean != null) {
            UserBean replyUserBean = videoCommentBean.getUserBean();//要回复的人
            if (replyUserBean != null) {
                mPress = WordUtil.getString(R.string.video_comment_reply_2) + replyUserBean.getUserNiceName();
            }
        }
    }

}
