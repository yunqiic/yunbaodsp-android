package com.yunbao.video.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.view.WindowManager;

import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoInfoReader;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.bean.LiveReceiveGiftBean;
import com.yunbao.common.bean.VideoBean;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.PermissionCallback;
import com.yunbao.common.utils.DateFormatUtil;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.DownloadUtil;
import com.yunbao.common.utils.MD5Util;
import com.yunbao.common.utils.MediaUtil;
import com.yunbao.common.utils.PermissionUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.video.R;
import com.yunbao.video.http.VideoHttpConsts;
import com.yunbao.video.http.VideoHttpUtil;
import com.yunbao.video.presenter.CheckVideoRecordPresenter;
import com.yunbao.video.presenter.VideoGiftAnimPresenter;
import com.yunbao.video.utils.VideoStorge;
import com.yunbao.video.views.VideoScrollViewHolder;

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

public abstract class AbsVideoPlayActivity extends AbsVideoCommentActivity {

    protected VideoScrollViewHolder mVideoScrollViewHolder;
    private Dialog mDownloadVideoDialog;
    private ClipboardManager mClipboardManager;
    protected ConfigBean mConfigBean;
    private VideoBean mShareVideoBean;
    protected String mVideoKey;
    private boolean mPaused;
    private VideoGiftAnimPresenter mGiftAnimPresenter;
    private String mGiftListJson;
    private CheckVideoRecordPresenter mCheckVideoRecordPresenter;

    @Override
    protected void main() {
        super.main();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        CommonAppConfig.getInstance().getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean bean) {
                mConfigBean = bean;
            }
        });
    }


    /**
     * 复制视频链接
     */
    public void copyLink(VideoBean videoBean) {
        if (videoBean == null) {
            return;
        }
        if (mClipboardManager == null) {
            mClipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        }
        ClipData clipData = ClipData.newPlainText("text", videoBean.getHrefW());
        mClipboardManager.setPrimaryClip(clipData);
        ToastUtil.show(WordUtil.getString(R.string.copy_success));
    }



    /**
     * 下载视频
     */
    public void downloadVideo(final VideoBean videoBean) {
        if (videoBean == null || TextUtils.isEmpty(videoBean.getHrefW())) {
            return;
        }
        PermissionUtil.request(this, new PermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        mDownloadVideoDialog = DialogUitl.loadingDialog(mContext);
                        mDownloadVideoDialog.show();
                        if (mDownloadUtil == null) {
                            mDownloadUtil = new DownloadUtil();
                        }
                        String fileName = "YB_VIDEO_" + videoBean.getTitle() + "_" + DateFormatUtil.getCurTimeString() + ".mp4";
                        mDownloadUtil.download(videoBean.getTag(), CommonAppConfig.VIDEO_DOWNLOAD_PATH, fileName, videoBean.getHrefW(), new DownloadUtil.Callback() {
                            @Override
                            public void onSuccess(File file) {
                                ToastUtil.show(R.string.video_download_success);
                                if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
                                    mDownloadVideoDialog.dismiss();
                                }
                                mDownloadVideoDialog = null;
                                String path = file.getAbsolutePath();
                                try {
                                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                                    mmr.setDataSource(path);
                                    String d = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                    if (StringUtil.isInt(d)) {
                                        long duration = Long.parseLong(d);
                                        MediaUtil.saveVideoInfo(mContext, path, duration);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onProgress(int progress) {

                            }

                            @Override
                            public void onError(Throwable e) {
                                ToastUtil.show(R.string.video_download_failed);
                                if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
                                    mDownloadVideoDialog.dismiss();
                                }
                                mDownloadVideoDialog = null;
                            }
                        });
                    }
                },
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        );
    }

    /**
     * 删除视频
     */
    public void deleteVideo(final VideoBean videoBean) {
        DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.video_del_tip), new DialogUitl.SimpleCallback() {
            @Override
            public void onConfirmClick(Dialog dialog, String content) {
                VideoHttpUtil.videoDelete(videoBean.getId(), new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            if (mVideoScrollViewHolder != null) {
                                mVideoScrollViewHolder.deleteVideo(videoBean);
                            }
                        }
                        ToastUtil.show(msg);
                    }
                });
            }
        });

    }


    public void checkIsCanTogetherRecord(final VideoBean videoBean) {
        if (videoBean == null) {
            return;
        }
        if (mCheckVideoRecordPresenter == null) {
            mCheckVideoRecordPresenter = new CheckVideoRecordPresenter(mContext);
            mCheckVideoRecordPresenter.setRecordCallBack(new CheckVideoRecordPresenter.RecordCallBack() {
                @Override
                public void onRecord(VideoBean bean) {
                    togetherRecordVideo(bean);
                }
            });
        }
        mCheckVideoRecordPresenter.checkStatusStartRecord(videoBean);
    }

    /**
     * 合拍
     */
    public void togetherRecordVideo(final VideoBean videoBean) {
        if (TextUtils.isEmpty(videoBean.getHref())) {
            return;
        }
        mDownloadVideoDialog = DialogUitl.loadingDialog(mContext);
        mDownloadVideoDialog.show();
        if (mDownloadUtil == null) {
            mDownloadUtil = new DownloadUtil();
        }
        String fileName = StringUtil.contact(MD5Util.getMD5(videoBean.getHref()), ".mp4");
        mDownloadUtil.download(videoBean.getTag(), CommonAppConfig.INNER_PATH, fileName, videoBean.getHref(), new DownloadUtil.Callback() {
            @Override
            public void onSuccess(File file) {
                if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
                    mDownloadVideoDialog.dismiss();
                }
                mDownloadVideoDialog = null;
                String path = file.getAbsolutePath();
                TXVideoEditConstants.TXVideoInfo txVideoInfo = TXVideoInfoReader.getInstance().getVideoFileInfo(path);
                int fps = (int) txVideoInfo.fps;
                int audioSampleRate = txVideoInfo.audioSampleRate;
                if (fps <= 0) {
                    fps = 30;
                }
                int audioSampleRateType;
                if (audioSampleRate == 8000) {
                    audioSampleRateType = TXRecordCommon.AUDIO_SAMPLERATE_8000;
                } else if (audioSampleRate == 16000) {
                    audioSampleRateType = TXRecordCommon.AUDIO_SAMPLERATE_16000;
                } else if (audioSampleRate == 32000) {
                    audioSampleRateType = TXRecordCommon.AUDIO_SAMPLERATE_32000;
                } else if (audioSampleRate == 44100) {
                    audioSampleRateType = TXRecordCommon.AUDIO_SAMPLERATE_44100;
                } else {
                    audioSampleRateType = TXRecordCommon.AUDIO_SAMPLERATE_48000;
                }
                VideoRecordActivity.forward(mContext, path, videoBean.getThumb(), Constants.VIDEO_RECORD_TYPE_FOLLOW_SHOT, txVideoInfo.duration, fps, audioSampleRateType);
            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.show(R.string.video_download_failed);
                if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
                    mDownloadVideoDialog.dismiss();
                }
                mDownloadVideoDialog = null;
            }
        });
//            }
//        });
    }

    /**
     * 礼物效果
     */
    public void showGift(LiveReceiveGiftBean receiveGiftBean) {
        if (mVideoScrollViewHolder == null) {
            return;
        }
        if (mGiftAnimPresenter == null) {
            mGiftAnimPresenter = new VideoGiftAnimPresenter(mContext, mVideoScrollViewHolder.getGiftView(), mVideoScrollViewHolder.getGifImageView(), mVideoScrollViewHolder.getSVGAImageView());
        }
        mGiftAnimPresenter.showGiftAnim(receiveGiftBean);
    }


    public void hideGift() {
        if (mGiftAnimPresenter != null) {
            mGiftAnimPresenter.cancelAllAnim();
        }
    }




    public boolean isPaused() {
        return mPaused;
    }

    @Override
    protected void onPause() {
        mPaused = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPaused = false;
    }

    @Override
    public void release() {
        super.release();
        VideoHttpUtil.cancel(VideoHttpConsts.SET_VIDEO_SHARE);
        VideoHttpUtil.cancel(VideoHttpConsts.VIDEO_DELETE);
        if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
            mDownloadVideoDialog.dismiss();
        }
        if (mVideoScrollViewHolder != null) {
            mVideoScrollViewHolder.release();
        }
        if (mCheckVideoRecordPresenter != null) {
            mCheckVideoRecordPresenter.destroy();
        }
        VideoStorge.getInstance().removeDataHelper(mVideoKey);
        if (mGiftAnimPresenter != null) {
            mGiftAnimPresenter.release();
        }
        mDownloadVideoDialog = null;
        mVideoScrollViewHolder = null;
        mCheckVideoRecordPresenter = null;
        mGiftAnimPresenter = null;
    }

    public void setVideoScrollViewHolder(VideoScrollViewHolder videoScrollViewHolder) {
        mVideoScrollViewHolder = videoScrollViewHolder;
    }

    /**
     * 让播放器静音
     */
    public void setMute(boolean mute) {
        if (mVideoScrollViewHolder != null) {
            mVideoScrollViewHolder.setMute(mute);
        }
    }

    public String getGiftListJson() {
        return mGiftListJson;
    }

    public void setGiftListJson(String giftListJson) {
        mGiftListJson = giftListJson;
    }


    public void finishActivityOnVideoDelete(String videoId) {
        this.finish();
    }
}
