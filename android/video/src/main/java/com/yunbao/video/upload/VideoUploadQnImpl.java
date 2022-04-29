package com.yunbao.video.upload;

import android.text.TextUtils;

import com.qiniu.android.common.FixedZone;
import com.qiniu.android.common.Zone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.yunbao.common.Constants;
import com.yunbao.common.utils.L;
import com.yunbao.video.http.VideoHttpConsts;
import com.yunbao.video.http.VideoHttpUtil;

import org.json.JSONObject;

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

public class VideoUploadQnImpl implements VideoUploadStrategy {

    private static final String TAG = "VideoUploadQnImpl";
    private VideoUploadBean mVideoUploadBean;
    private VideoUploadCallback mVideoUploadCallback;
    private String mToken;
    private String mZone;
    private String mPrefix;
    private UploadManager mUploadManager;
    private UpCompletionHandler mVideoUpCompletionHandler;//视频上传回调
    private UpCompletionHandler mVideoUpCompletionHandlerWater;//水印视频上传回调
    private UpCompletionHandler mImageUpCompletionHandler;//封面图片上传回调

    private UpProgressHandler mVideoProgessHandler;//视频上传进度回调
    private UpProgressHandler mVideoProgessHandlerWater;//水印视频上传进度回调
    private UpProgressHandler mImageProgessHandler;//封面图片上传进度回调

    private long mTotalFileLength;//全部文件大小
    private long mVideoFileLength;//视频文件大小
    private long mVideoWaterFileLength;//水印视频文件大小
    private long mImageFileLength;//封面图片文件大小


    public VideoUploadQnImpl(String uploadToken, String zone, String prefix) {
        mToken = uploadToken;
        mZone = zone;
        mPrefix = prefix + "_";
        //视频上传进度回调
        mVideoProgessHandler = new UpProgressHandler() {

            @Override
            public void progress(String key, double percent) {
                if (mVideoUploadCallback != null) {
                    double cur = mVideoFileLength * percent;
                    mVideoUploadCallback.onProgress((int) (cur / mTotalFileLength * 100));
                }
            }
        };
        //水印视频上传进度回调
        mVideoProgessHandlerWater = new UpProgressHandler() {

            @Override
            public void progress(String key, double percent) {
                if (mVideoUploadCallback != null) {
                    double cur = mVideoFileLength + mVideoWaterFileLength * percent;
                    mVideoUploadCallback.onProgress((int) (cur / mTotalFileLength * 100));
                }
            }
        };
        //封面图片上传进度回调
        mImageProgessHandler = new UpProgressHandler() {

            @Override
            public void progress(String key, double percent) {
                if (mVideoUploadCallback != null) {
                    double cur = mVideoFileLength + mVideoWaterFileLength + mImageFileLength * percent;
                    mVideoUploadCallback.onProgress((int) (cur / mTotalFileLength * 100));
                }
            }
        };
        mVideoUpCompletionHandler = new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject response) {
                if (mVideoUploadBean == null) {
                    return;
                }
                String videoResultUrl = mVideoUploadBean.getVideoFile().getName();
                L.e(TAG, "视频上传结果-------->" + videoResultUrl);
                mVideoUploadBean.setResultVideoUrl(videoResultUrl);
                File waterFile = mVideoUploadBean.getVideoWaterFile();
                if (waterFile != null && waterFile.exists()) {
                    uploadFile(waterFile, mVideoUpCompletionHandlerWater, mVideoProgessHandlerWater);
                } else {
                    uploadFile(mVideoUploadBean.getImageFile(), mImageUpCompletionHandler, mImageProgessHandler);
                }
            }
        };
        mVideoUpCompletionHandlerWater = new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject response) {
                if (mVideoUploadBean == null) {
                    return;
                }
                String videoResultWaterUrl = mVideoUploadBean.getVideoWaterFile().getName();
                L.e(TAG, "水印视频上传结果-------->" + videoResultWaterUrl);
                mVideoUploadBean.setResultWaterVideoUrl(videoResultWaterUrl);
                uploadFile(mVideoUploadBean.getImageFile(), mImageUpCompletionHandler, mImageProgessHandler);
            }
        };
        mImageUpCompletionHandler = new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject response) {
                if (mVideoUploadBean == null) {
                    return;
                }
                String imageResultUrl = mVideoUploadBean.getImageFile().getName();
                L.e(TAG, "图片上传结果-------->" + imageResultUrl);
                mVideoUploadBean.setResultImageUrl(imageResultUrl);
                if (mVideoUploadCallback != null) {
                    mVideoUploadCallback.onSuccess(mVideoUploadBean);
                }
            }
        };

    }


    @Override
    public void upload(VideoUploadBean bean, VideoUploadCallback callback) {
        if (bean == null || callback == null) {
            return;
        }
        File videoFile = bean.getVideoFile();
        if (videoFile == null || !videoFile.exists()) {
            return;
        }
        mVideoFileLength = videoFile.length();
        File videoWaterFile = bean.getVideoWaterFile();
        if (videoWaterFile != null && videoWaterFile.exists()) {
            mVideoWaterFileLength = videoWaterFile.length();
        }
        File imageFile = bean.getImageFile();
        if (imageFile != null && imageFile.exists()) {
            mImageFileLength = imageFile.length();
        }
        mTotalFileLength = mVideoFileLength + mVideoWaterFileLength + mImageFileLength;
        mVideoUploadBean = bean;
        mVideoUploadCallback = callback;
        uploadFile(mVideoUploadBean.getVideoFile(), mVideoUpCompletionHandler, mVideoProgessHandler);
    }

    /**
     * 上传文件
     */
    private void uploadFile(File file, UpCompletionHandler handler, UpProgressHandler upProgressHandler) {
        if (TextUtils.isEmpty(mToken)) {
            return;
        }
        if (mUploadManager == null) {
            Zone zone = FixedZone.zone0;//默认华东
            if (Constants.UPLOAD_QI_NIU_HB.equals(mZone)) {
                zone = FixedZone.zone1;
            } else if (Constants.UPLOAD_QI_NIU_HN.equals(mZone)) {
                zone = FixedZone.zone2;
            } else if (Constants.UPLOAD_QI_NIU_BM.equals(mZone)) {
                zone = FixedZone.zoneNa0;
            } else if (Constants.UPLOAD_QI_NIU_XJP.equals(mZone)) {
                zone = FixedZone.zoneAs0;
            }
            Configuration configuration = new Configuration.Builder().zone(zone).build();
            mUploadManager = new UploadManager(configuration);
        }
        mUploadManager.put(file, file.getName(), mToken, handler, new UploadOptions(null, null, false, upProgressHandler, null));
    }

    @Override
    public void cancel() {
        VideoHttpUtil.cancel(VideoHttpConsts.GET_QI_NIU_TOKEN);
        mVideoUploadCallback = null;
    }

}
