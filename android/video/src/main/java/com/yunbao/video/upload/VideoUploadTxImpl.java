package com.yunbao.video.upload;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.object.PutObjectRequest;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.SessionQCloudCredentials;
import com.tencent.qcloud.core.auth.StaticCredentialProvider;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.utils.L;

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

public class VideoUploadTxImpl implements VideoUploadStrategy {

    private static final String TAG = "VideoUploadTxImpl";

    private VideoUploadBean mVideoUploadBean;
    private VideoUploadCallback mVideoUploadCallback;
    private OnSuccessCallback mVideoOnSuccessCallback;//视频上传成功回调
    private OnSuccessCallback mVideoOnSuccessWaterCallback;//水印视频上传成功回调
    private OnSuccessCallback mImageOnSuccessCallback;//封面图片上传成功回调

    private CosXmlProgressListener mVideoProgessHandler;//视频上传进度回调
    private CosXmlProgressListener mVideoProgessHandlerWater;//水印视频上传进度回调
    private CosXmlProgressListener mImageProgessHandler;//封面图片上传进度回调

    private long mTotalFileLength;//全部文件大小
    private long mVideoFileLength;//视频文件大小
    private long mVideoWaterFileLength;//水印视频文件大小
    private long mImageFileLength;//封面图片文件大小

    private CosXmlService mCosXmlService;
    private String mAppId;//appId
    private String mRegion;//区域
    private String mBucketName;//桶的名字
    private Handler mHandler;
    private String mPrefix;
    private String mSecretId;
    private String mSecretKey;
    private String mSessionToken;
    private long mExpiredTime;
    private String mImageCosPath;
    private String mVideoCosPath;
    private String mVoiceCosPath;


    public VideoUploadTxImpl(
            String appId,
            String region,
            String bucketName,
            String secretId,
            String secretKey,
            String sessionToken,
            long expiredTime,
            String prefix,
            String imageCosPath,
            String videoCosPath,
            String voiceCosPath) {
        mAppId = appId;
        mRegion = region;
        mBucketName = bucketName;
        mSecretId = secretId;
        mSecretKey = secretKey;
        mSessionToken = sessionToken;
        mExpiredTime = expiredTime;
        mPrefix = prefix + "_";
        if (imageCosPath == null) {
            imageCosPath = "";
        }
        if (!TextUtils.isEmpty(imageCosPath) && !imageCosPath.endsWith("/")) {
            imageCosPath += "/";
        }
        mImageCosPath = imageCosPath;

        if (videoCosPath == null) {
            videoCosPath = "";
        }
        if (!TextUtils.isEmpty(videoCosPath) && !videoCosPath.endsWith("/")) {
            videoCosPath += "/";
        }
        mVideoCosPath = videoCosPath;

        if (voiceCosPath == null) {
            voiceCosPath = "";
        }
        if (!TextUtils.isEmpty(voiceCosPath) && !voiceCosPath.endsWith("/")) {
            voiceCosPath += "/";
        }
        mVoiceCosPath = voiceCosPath;

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (mVideoUploadCallback != null) {
                    mVideoUploadCallback.onProgress(msg.what);
                }
            }
        };

        //视频上传进度回调
        mVideoProgessHandler = new CosXmlProgressListener() {
            @Override
            public void onProgress(long complete, long target) {
                if (mHandler != null) {
                    int progress = (int) (((double) (complete)) / mTotalFileLength * 100);
                    mHandler.sendEmptyMessage(progress);
                }
            }

        };
        //水印视频上传进度回调
        mVideoProgessHandlerWater = new CosXmlProgressListener() {
            @Override
            public void onProgress(long complete, long target) {
                if (mHandler != null) {
                    double cur = mVideoFileLength + ((double) (complete));
                    int progress = (int) (cur / mTotalFileLength * 100);
                    mHandler.sendEmptyMessage(progress);
                }
            }

        };
        //封面图片上传进度回调
        mImageProgessHandler = new CosXmlProgressListener() {
            @Override
            public void onProgress(long complete, long target) {
                if (mHandler != null) {
                    double cur = mVideoFileLength + mVideoWaterFileLength + ((double) (complete));
                    int progress = (int) (cur / mTotalFileLength * 100);
                    mHandler.sendEmptyMessage(progress);
                }
            }

        };

        mVideoOnSuccessCallback = new OnSuccessCallback() {
            @Override
            public void onUploadSuccess(String url) {
                if (mVideoUploadBean == null) {
                    return;
                }
                L.e(TAG, "视频上传结果-------->" + url);
                mVideoUploadBean.setResultVideoUrl(url);
                File waterFile = mVideoUploadBean.getVideoWaterFile();
                if (waterFile != null && waterFile.exists()) {
                    //上传水印视频
                    uploadFile(waterFile, mVideoOnSuccessWaterCallback, mVideoProgessHandlerWater);
                } else {
                    //上传封面图片
                    uploadFile(mVideoUploadBean.getImageFile(), mImageOnSuccessCallback, mImageProgessHandler);
                }
            }
        };
        mVideoOnSuccessWaterCallback = new OnSuccessCallback() {
            @Override
            public void onUploadSuccess(String url) {
                if (mVideoUploadBean == null) {
                    return;
                }
                L.e(TAG, "水印视频上传结果-------->" + url);
                mVideoUploadBean.setResultWaterVideoUrl(url);
                //上传封面图片
                uploadFile(mVideoUploadBean.getImageFile(), mImageOnSuccessCallback, mImageProgessHandler);
            }
        };
        mImageOnSuccessCallback = new OnSuccessCallback() {
            @Override
            public void onUploadSuccess(String url) {
                if (mVideoUploadBean == null) {
                    return;
                }
                L.e(TAG, "图片上传结果-------->" + url);
                mVideoUploadBean.setResultImageUrl(url);
                if (mVideoUploadCallback != null) {
                    mVideoUploadCallback.onSuccess(mVideoUploadBean);
                }
            }
        };

    }

    @Override
    public void upload(final VideoUploadBean bean, final VideoUploadCallback callback) {
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

        try {
            SessionQCloudCredentials credentials = new SessionQCloudCredentials(mSecretId, mSecretKey, mSessionToken, mExpiredTime);
            QCloudCredentialProvider qCloudCredentialProvider = new StaticCredentialProvider(credentials);
            CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                    .setAppidAndRegion(mAppId, mRegion)
                    .builder();
            mCosXmlService = new CosXmlService(CommonAppContext.getInstance(), serviceConfig, qCloudCredentialProvider);
        } catch (Exception e) {
            if (mVideoUploadCallback != null) {
                mVideoUploadCallback.onFailure();
            }
        }
        //上传视频
        uploadFile(mVideoUploadBean.getVideoFile(), mVideoOnSuccessCallback, mVideoProgessHandler);

    }


    /**
     * 上传文件
     */
    private void uploadFile(File file, final OnSuccessCallback callback, CosXmlProgressListener progressListener) {
        if (mCosXmlService == null) {
            return;
        }
        PutObjectRequest putObjectRequest = new PutObjectRequest(mBucketName, file.getName(), file.getAbsolutePath());
        putObjectRequest.setProgressListener(progressListener);
        // 使用异步回调上传
        mCosXmlService.putObjectAsync(putObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                if (cosXmlResult != null) {
                    String resultUrl = cosXmlResult.accessUrl;
                    L.e(TAG, "---上传结果---->  " + resultUrl);
                    if (callback != null) {
                        callback.onUploadSuccess(resultUrl);
                    }
                }
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException clientException, CosXmlServiceException serviceException) {

            }
        });
    }

    @Override
    public void cancel() {
        mVideoUploadCallback = null;
        if (mCosXmlService != null) {
            mCosXmlService.release();
        }
        mCosXmlService = null;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        CommonHttpUtil.cancel(CommonHttpConsts.GET_TX_UPLOAD_TOKEN);
    }

    public interface OnSuccessCallback {
        void onUploadSuccess(String url);
    }
}
