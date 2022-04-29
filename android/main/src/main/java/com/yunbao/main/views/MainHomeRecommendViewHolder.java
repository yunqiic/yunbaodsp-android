package com.yunbao.main.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.bean.VideoBean;
import com.yunbao.common.custom.CircleProgress;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.main.R;
import com.yunbao.main.activity.MainActivity;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;
import com.yunbao.video.activity.AbsVideoPlayActivity;
import com.yunbao.video.bean.ClassBean;
import com.yunbao.video.event.VideoUploadEvent;
import com.yunbao.video.http.VideoHttpUtil;
import com.yunbao.video.interfaces.VideoScrollDataHelper;
import com.yunbao.video.upload.VideoUploadBean;
import com.yunbao.video.upload.VideoUploadCallback;
import com.yunbao.video.upload.VideoUploadStrategy;
import com.yunbao.video.upload.VideoUploadUtil;
import com.yunbao.video.utils.VideoCoverUtil;
import com.yunbao.video.utils.VideoStorge;
import com.yunbao.video.views.VideoScrollViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;
import top.zibin.luban.OnRenameListener;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class MainHomeRecommendViewHolder extends AbsMainViewHolder {

    private View mNoData;
    private VideoScrollViewHolder mVideoScrollViewHolder;
    private boolean mPassivePaused;
    private boolean mPaused;
    private boolean mIsHomeRefresh;

    private View mVideoUploadGroup;
    private ImageView mVideoUploadCover;
    private CircleProgress mVideoUploadProgress;
    private TextView mVideoUploadProgressText;
    private int mProgressVal;
    private boolean mIsVideoPub;
    private boolean mIsShowing;
    private double mVideoRatio;

    public MainHomeRecommendViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_home_recommend;
    }

    @Override
    public void init() {
        mNoData = findViewById(R.id.no_data);
        mVideoUploadGroup = findViewById(R.id.video_upload_group);
        mVideoUploadCover = (ImageView) findViewById(R.id.video_upload_cover);
        mVideoUploadProgress = (CircleProgress) findViewById(R.id.video_upload_progress);
        mVideoUploadProgressText = (TextView) findViewById(R.id.video_upload_progress_text);
        EventBus.getDefault().register(this);
        mIsShowing = true;
    }

    @Override
    public void loadData() {
        if (!isFirstLoadData()) {
            return;
        }
        getRecommendVideoList();
        onPageShowChanged();
    }

    private void getRecommendVideoList() {
        MainHttpUtil.getRecommendVideoList(1, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<VideoBean> list = JSON.parseArray(Arrays.toString(info), VideoBean.class);
                    if (list == null || list.size() == 0) {
                        if (mNoData != null && mNoData.getVisibility() != View.VISIBLE) {
                            mNoData.setVisibility(View.VISIBLE);
                        }
                        return;
                    }else{
                        if (mNoData != null && mNoData.getVisibility() != View.INVISIBLE) {
                            mNoData.setVisibility(View.INVISIBLE);
                        }
                    }
                    VideoStorge.getInstance().put(Constants.VIDEO_HOME_RECOMMEND, list);
                    initVideoPlay();
                    if (mIsHomeRefresh) {
                        mIsHomeRefresh = false;
                        ToastUtil.show(R.string.refresh_success);
                    }
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }


    private void initVideoPlay() {
        VideoStorge.getInstance().putDataHelper(Constants.VIDEO_HOME_RECOMMEND, new VideoScrollDataHelper() {

            @Override
            public void loadData(int p, HttpCallback callback) {
                MainHttpUtil.getRecommendVideoList(p, callback);
            }
        });
        mVideoScrollViewHolder = new VideoScrollViewHolder(mContext, (ViewGroup) findViewById(R.id.container), 0, Constants.VIDEO_HOME_RECOMMEND, 1, true, false);
        mVideoScrollViewHolder.addToParent();
        mVideoScrollViewHolder.subscribeActivityLifeCycle();
        if (mPassivePaused) {
            mVideoScrollViewHolder.passivePause();
        }
        if (mPaused) {
            mVideoScrollViewHolder.onPause();
        }
        ((AbsVideoPlayActivity) mContext).setVideoScrollViewHolder(mVideoScrollViewHolder);

    }

    @Override
    public void release() {
        super.release();
        MainHttpUtil.cancel(MainHttpConsts.GET_RECOMMEND_VIDEO_LIST);
        if (mVideoScrollViewHolder != null) {
            mVideoScrollViewHolder.release();
            mVideoScrollViewHolder = null;
        }
        VideoStorge.getInstance().remove(Constants.VIDEO_HOME_RECOMMEND);
        VideoCoverUtil.getInstance().release();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        release();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPaused = false;
    }

    @Override
    public void setShowed(boolean showed) {
        super.setShowed(showed);
        if (mVideoScrollViewHolder != null) {
            mPassivePaused = false;
            if (showed) {
                mVideoScrollViewHolder.passiveResume();
            } else {
                mVideoScrollViewHolder.passivePause();
            }
        } else {
            mPassivePaused = !showed;
        }
        if (mIsShowing != showed) {
            mIsShowing = showed;
            onPageShowChanged();
        }

    }


    private void onPageShowChanged() {

    }


    /**
     * 刷新推荐
     */
    public void refreshRecommend() {
        mIsHomeRefresh = true;
        if (mVideoScrollViewHolder != null) {
            mVideoScrollViewHolder.onRefresh();
        } else {
            getRecommendVideoList();
        }
    }

    /**
     * 从播放列表中删除视频
     */
    public void deleteVideoFromPlay(String videoId) {
        if (mVideoScrollViewHolder != null) {
            mVideoScrollViewHolder.deleteVideoFromPlay(videoId);
        }
    }

    public boolean isVideoPub() {
        return mIsVideoPub;
    }


    /**
     * 发布视频结束
     */
    private void videoUploadEnd() {
        if (mVideoUploadGroup != null && mVideoUploadGroup.getVisibility() == View.VISIBLE) {
            mVideoUploadGroup.setVisibility(View.INVISIBLE);
        }
        mIsVideoPub = false;
    }


    /**
     * 发布视频开始
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void videoUploadStart(final VideoUploadEvent ev) {
        if (mContext == null) {
            return;
        }
        mIsVideoPub = true;
        ((MainActivity) mContext).tabHomeRecommend();
//        L.e("发布视频", "-------生成视频封面开始------->");
        if (mVideoUploadGroup != null && mVideoUploadGroup.getVisibility() != View.VISIBLE) {
            mVideoUploadGroup.setVisibility(View.VISIBLE);
        }
        if (mVideoUploadCover != null) {
            mVideoUploadCover.setImageDrawable(null);
        }
        if (mVideoUploadProgress != null) {
            mVideoUploadProgress.setCurProgress(0);
        }
        if (mVideoUploadProgressText != null) {
            mVideoUploadProgressText.setText("0%");
        }
        mProgressVal = 0;
        mVideoRatio = 0;
        //生成视频封面图
        String videoPath = ev.getVideoPath();
        Bitmap bitmap = VideoCoverUtil.getInstance().getCoverBitmap();
        if (bitmap == null) {
            ToastUtil.show(R.string.video_cover_img_failed);
            videoUploadEnd();
            return;
        }
        mVideoRatio = ((double) bitmap.getHeight()) / bitmap.getWidth();
        if (mVideoUploadCover != null) {
            mVideoUploadCover.setImageBitmap(bitmap);
        }
        String coverImagePath = videoPath.replace(".mp4", ".jpg");
        File imageFile = new File(coverImagePath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            imageFile = null;
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (imageFile == null) {
            ToastUtil.show(R.string.video_cover_img_failed);
            videoUploadEnd();
            return;
        }
//        L.e("发布视频", "-------生成视频封面成功，开始压缩------->");
        final File finalImageFile = imageFile;
        //用鲁班压缩图片
        Luban.with(mContext)
                .load(finalImageFile)
                .setFocusAlpha(false)
                .ignoreBy(8)//8k以下不压缩
                .setTargetDir(CommonAppConfig.VIDEO_PATH)
                .setRenameListener(new OnRenameListener() {
                    @Override
                    public String rename(String filePath) {
                        filePath = filePath.substring(filePath.lastIndexOf("/") + 1);
                        return filePath.replace(".jpg", "_c.jpg");
                    }
                })
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(File file) {
                        if (!finalImageFile.getAbsolutePath().equals(file.getAbsolutePath()) && finalImageFile.exists()) {
                            finalImageFile.delete();
                        }
//                        L.e("发布视频", "-------视频封面压缩成功------->");
                        uploadVideoFile(ev, file);
                    }

                    @Override
                    public void onError(Throwable e) {
//                        L.e("发布视频", "-------视频封面压缩失败------->");
                        uploadVideoFile(ev, finalImageFile);
                    }
                }).launch();
    }


    /**
     * 上传封面图片和视频
     */
    private void uploadVideoFile(final VideoUploadEvent ev, final File imageFile) {
//        L.e("发布视频", "-------获取云存储方式------->");
        VideoUploadUtil.startUpload(new CommonCallback<VideoUploadStrategy>() {
            @Override
            public void callback(final VideoUploadStrategy videoUploadStrategy) {
                if (videoUploadStrategy == null) {
                    ToastUtil.show(R.string.video_pub_failed);
                    videoUploadEnd();
                    return;
                }
                final VideoUploadBean videoUploadBean = new VideoUploadBean(new File(ev.getVideoPath()), imageFile);
                String videoPathWater = ev.getVideoPathWater();
                if (!TextUtils.isEmpty(videoPathWater)) {
                    File waterFile = new File(videoPathWater);
                    if (waterFile.exists()) {
                        videoUploadBean.setVideoWaterFile(waterFile);
                    }
                }
                doUpload(ev, videoUploadStrategy, videoUploadBean);
            }
        });
    }


    private void doUpload(final VideoUploadEvent ev, VideoUploadStrategy videoUploadStrategy, final VideoUploadBean videoUploadBean) {
//        L.e("发布视频", "-------开始上传------->");
        videoUploadStrategy.upload(videoUploadBean, new VideoUploadCallback() {
            @Override
            public void onSuccess(VideoUploadBean bean) {
                if (ev.getSaveType() == Constants.VIDEO_SAVE_PUB) {
                    bean.deleteFile();
                }
//                L.e("发布视频", "-------上传成功------->");
                saveUploadVideoInfo(ev, videoUploadBean);
            }

            @Override
            public void onFailure() {
//                L.e("发布视频", "-------上传失败------->");
                ToastUtil.show(R.string.video_pub_failed);
                videoUploadEnd();
            }

            @Override
            public void onProgress(int progress) {
                if (mProgressVal != progress) {
                    mProgressVal = progress;
//                    L.e("发布视频", "-------上传进度-------> " + progress);
                    if (mVideoUploadProgress != null) {
                        mVideoUploadProgress.setCurProgress(progress);
                    }
                    if (mVideoUploadProgressText != null) {
                        mVideoUploadProgressText.setText(StringUtil.contact(String.valueOf(progress), "%"));
                    }
                }
            }
        });
    }


    /**
     * 把视频上传后的信息保存在服务器
     */
    private void saveUploadVideoInfo(VideoUploadEvent ev, VideoUploadBean videoUploadBean) {
        String videoTitle = ev.getVideoTitle();

        VideoHttpUtil.saveUploadVideoInfo("0", 0, videoTitle, videoUploadBean.getResultImageUrl(), videoUploadBean.getResultVideoUrl(), videoUploadBean.getResultWaterVideoUrl(), ev.getMusicId(), 0, "0", mVideoRatio,ev.getIsUserad(),ev.getUseradUrl(), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    videoUploadEnd();
                    if (info.length > 0) {

                        if (!mPaused && ((MainActivity) mContext).isTabHomeRecommend()) {
                            ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
                            if (configBean != null&&configBean.getVideoAuditSwitch() == 0) {
                                ToastUtil.show(R.string.video_pub_success);
                            }else{
                                ToastUtil.show(com.yunbao.video.R.string.video_pub_success_2);
                            }
                        }

                    }
                } else {
//                    L.e("发布视频", "-------请求接口保存数据------->"+msg);
                    ToastUtil.show(msg);
                    videoUploadEnd();
                }
            }

        });
    }




}
