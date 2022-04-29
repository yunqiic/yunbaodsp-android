package com.yunbao.video.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.adapter.ImChatFacePagerAdapter;
import com.yunbao.common.interfaces.ActivityResultCallback;
import com.yunbao.common.interfaces.OnFaceClickListener;
import com.yunbao.common.interfaces.PermissionCallback;
import com.yunbao.common.utils.ActivityResultUtil;
import com.yunbao.common.utils.DownloadUtil;
import com.yunbao.common.utils.MD5Util;
import com.yunbao.common.utils.PermissionUtil;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.VoiceMediaPlayerUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.video.R;
import com.yunbao.video.bean.VideoCommentBean;
import com.yunbao.video.dialog.VideoInputDialogFragment;
import com.yunbao.video.views.VideoCommentViewHolder;
import com.yunbao.video.views.VideoCommentVoiceViewHolder;

import java.io.File;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public abstract class AbsVideoCommentActivity extends AbsActivity implements View.OnClickListener, OnFaceClickListener {

    protected DownloadUtil mDownloadUtil;
    protected VideoCommentViewHolder mVideoCommentViewHolder;
    protected VideoCommentVoiceViewHolder mVideoCommentVoiceViewHolder;
    protected VideoInputDialogFragment mVideoInputDialogFragment;
    private View mFaceView;//表情面板
    private int mFaceHeight;//表情面板高度
    private String mCurVideoId;
    private String mCurVideoUid;
    private VoiceMediaPlayerUtil mVoiceMediaPlayerUtil;

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_send) {
            if (mVideoInputDialogFragment != null) {
                mVideoInputDialogFragment.sendComment();
            }
        }
    }

    public void forwardAtFriend(String videoId, String videoUid) {
        if (!CommonAppConfig.getInstance().isLogin()) {
            RouteUtil.forwardLogin(mContext);
            return;
        }
        if (TextUtils.isEmpty(videoId) || TextUtils.isEmpty(videoUid)) {
            return;
        }
        mCurVideoId = videoId;
        mCurVideoUid = videoUid;
        if (mVideoInputDialogFragment != null) {
            mVideoInputDialogFragment.hideSoftInput();
        }
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.AtFriendActivity");
        ActivityResultUtil.startActivityForResult(this, intent, new ActivityResultCallback() {
            @Override
            public void onSuccess(Intent intent) {
                if (intent != null) {
                    String toUid = intent.getStringExtra(Constants.TO_UID);
                    String toName = intent.getStringExtra(Constants.TO_NAME);
                    if (mVideoInputDialogFragment != null) {
                        mVideoInputDialogFragment.addSpan(toUid, toName);
                        mVideoInputDialogFragment.showSoftInputDelay();
                    } else {
                        boolean dark = mVideoCommentViewHolder != null && mVideoCommentViewHolder.isShowed();
                        openCommentInputWindow(false, mCurVideoId, mCurVideoUid, null, dark, toUid, toName);
                    }
                }
            }
        });
    }


    public void showVoiceViewHolder(final String videoId, final String videoUid, final VideoCommentBean videoCommentBean) {
        if (!CommonAppConfig.getInstance().isLogin()) {
            RouteUtil.forwardLogin(mContext);
            return;
        }
        PermissionUtil.request(this, new PermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        if (mVideoInputDialogFragment != null) {
                            mVideoInputDialogFragment.hideSoftInput();
                        }
                        if (mVideoCommentVoiceViewHolder == null) {
                            mVideoCommentVoiceViewHolder = new VideoCommentVoiceViewHolder(mContext, (ViewGroup) findViewById(R.id.root));
                        }
                        mVideoCommentVoiceViewHolder.setVideoId(videoId);
                        mVideoCommentVoiceViewHolder.setVideoUid(videoUid);
                        mVideoCommentVoiceViewHolder.setVideoCommentBean(videoCommentBean);
                        mVideoCommentVoiceViewHolder.addToParent();
                        if (mVideoCommentViewHolder != null) {
                            mVideoCommentViewHolder.stopPlayVoice();
                        }
                    }
                },
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        );
    }

    /**
     * 打开评论输入框
     */
    public void openCommentInputWindow(boolean openFace, String videoId, String videoUid, VideoCommentBean bean, boolean dark, String atUid, String atName) {
        if (!CommonAppConfig.getInstance().isLogin()) {
            RouteUtil.forwardLogin(mContext);
            return;
        }
        if (mFaceView == null) {
            mFaceView = initFaceView();
        }
        VideoInputDialogFragment fragment = new VideoInputDialogFragment();
        fragment.setDarkStyle(dark);
        fragment.setVideoInfo(videoId, videoUid);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.VIDEO_FACE_OPEN, openFace);
        bundle.putInt(Constants.VIDEO_FACE_HEIGHT, mFaceHeight);
        bundle.putParcelable(Constants.VIDEO_COMMENT_BEAN, bean);
        bundle.putString(Constants.TO_UID, atUid);
        bundle.putString(Constants.TO_NAME, atName);
        fragment.setArguments(bundle);
        mVideoInputDialogFragment = fragment;
        fragment.show(getSupportFragmentManager(), "VideoInputDialogFragment");
    }

    /**
     * 打开评论输入框
     */
    public void openCommentInputWindow(boolean openFace, String videoId, String videoUid, VideoCommentBean bean, boolean dark) {
        openCommentInputWindow(openFace, videoId, videoUid, bean, dark, null, null);
    }

    public View getFaceView() {
        if (mFaceView == null) {
            mFaceView = initFaceView();
        }
        return mFaceView;
    }

    /**
     * 初始化表情控件
     */
    private View initFaceView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.view_chat_face, null);
        v.measure(0, 0);
        mFaceHeight = v.getMeasuredHeight();
        v.findViewById(R.id.btn_send).setOnClickListener(this);
        final RadioGroup radioGroup = (RadioGroup) v.findViewById(R.id.radio_group);
        ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(10);
        ImChatFacePagerAdapter adapter = new ImChatFacePagerAdapter(mContext, this);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ((RadioButton) radioGroup.getChildAt(position)).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        for (int i = 0, pageCount = adapter.getCount(); i < pageCount; i++) {
            RadioButton radioButton = (RadioButton) inflater.inflate(R.layout.view_chat_indicator, radioGroup, false);
            radioButton.setId(i + 10000);
            if (i == 0) {
                radioButton.setChecked(true);
            }
            radioGroup.addView(radioButton);
        }
        return v;
    }

    /**
     * 显示评论
     */
    public void openCommentWindow(String videoId, String videoUid, boolean fullScreen) {
        if (mVideoCommentViewHolder == null) {
            mVideoCommentViewHolder = new VideoCommentViewHolder(mContext, (ViewGroup) findViewById(R.id.root), fullScreen);
            mVideoCommentViewHolder.addToParent();
        }
        mVideoCommentViewHolder.setVideoInfo(videoId, videoUid);
        mVideoCommentViewHolder.showBottom();
    }

    /**
     * 显示评论
     */
    public void openCommentWindow(String videoId, String videoUid) {
        openCommentWindow(videoId, videoUid, false);
    }

    /**
     * 刷新评论
     */
    public void refreshComment() {
        if (mVideoCommentViewHolder != null) {
            mVideoCommentViewHolder.refreshComment();
        }
    }


    @Override
    public void onFaceClick(String str, int faceImageRes) {
        if (mVideoInputDialogFragment != null) {
            mVideoInputDialogFragment.onFaceClick(str, faceImageRes);
        }
    }

    @Override
    public void onFaceDeleteClick() {
        if (mVideoInputDialogFragment != null) {
            mVideoInputDialogFragment.onFaceDeleteClick();
        }
    }

    public void release() {
        if (mVideoCommentViewHolder != null) {
            mVideoCommentViewHolder.release();
        }
        if (mVoiceMediaPlayerUtil != null) {
            mVoiceMediaPlayerUtil.destroy();
        }
        mVideoCommentViewHolder = null;
        mVideoInputDialogFragment = null;
        mVoiceMediaPlayerUtil = null;
    }

    public void releaseVideoInputDialog() {
        mVideoInputDialogFragment = null;
    }

    /**
     * 播放语音评论
     */
    public void playCommentVoice(VideoCommentBean commentBean) {
        if (commentBean == null || !commentBean.isVoice()) {
            return;
        }
        String voiceLink = commentBean.getVoiceLink();
        if (TextUtils.isEmpty(voiceLink)) {
            return;
        }
        String fileName = MD5Util.getMD5(voiceLink);
        String path = CommonAppConfig.VOICE_PATH + fileName;
        File file = new File(path);
        if (file.exists()) {
            playVoiceFile(file);
        } else {
            if (mDownloadUtil == null) {
                mDownloadUtil = new DownloadUtil();
            }
            mDownloadUtil.download("voice", CommonAppConfig.VOICE_PATH, fileName, voiceLink, new DownloadUtil.Callback() {
                @Override
                public void onSuccess(File file) {
                    playVoiceFile(file);
                }

                @Override
                public void onProgress(int progress) {

                }

                @Override
                public void onError(Throwable e) {
                    WordUtil.getString(R.string.video_play_error);
                }
            });
        }
    }

    /**
     * 停止播放语音评论
     */
    public void stopCommentVoice(VideoCommentBean commentBean) {
        if (mVoiceMediaPlayerUtil != null) {
            mVoiceMediaPlayerUtil.stopPlay();
        }
        if (mContext instanceof AbsVideoPlayActivity) {
            ((AbsVideoPlayActivity) mContext).setMute(false);
        }
    }


    private void playVoiceFile(File file) {
        if (mContext instanceof AbsVideoPlayActivity) {
            ((AbsVideoPlayActivity) mContext).setMute(true);
        }
        if (mVoiceMediaPlayerUtil == null) {
            mVoiceMediaPlayerUtil = new VoiceMediaPlayerUtil(mContext);
            mVoiceMediaPlayerUtil.setActionListener(new VoiceMediaPlayerUtil.ActionListener() {
                @Override
                public void onPlayEnd() {
                    if (mVideoCommentViewHolder != null) {
                        mVideoCommentViewHolder.stopVoiceAnim();
                    }
                    if (mContext instanceof AbsVideoPlayActivity) {
                        ((AbsVideoPlayActivity) mContext).setMute(false);
                    }
                }
            });
        }
        mVoiceMediaPlayerUtil.startPlay(file.getAbsolutePath());
    }

    /**
     * 检查并且隐藏评论弹窗
     */
    public boolean checkAndHideCommentView() {
        if (mVideoCommentViewHolder != null && mVideoCommentViewHolder.isShowed()) {
            mVideoCommentViewHolder.hideBottom();
            return true;
        }
        return false;
    }

}
