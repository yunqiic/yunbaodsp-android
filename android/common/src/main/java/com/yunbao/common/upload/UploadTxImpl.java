package com.yunbao.common.upload;

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
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.StringUtil;

import java.io.File;
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

public class UploadTxImpl implements UploadStrategy {

    private static final String TAG = "UploadTxImpl";
    private List<UploadBean> mList;
    private int mIndex;
    private boolean mNeedCompress;
    private UploadCallback mUploadCallback;
    private CosXmlService mCosXmlService;
    private String mAppId;//appId
    private String mRegion;//区域
    private String mBucketName;//桶的名字
    private CosXmlResultListener mCosXmlResultListener;//上传回调
    private Luban.Builder mLubanBuilder;
//    private String mPrefix;
    private String mSecretId;
    private String mSecretKey;
    private String mSessionToken;
    private long mExpiredTime;
    private String mImageCosPath;
    private String mVideoCosPath;
    private String mVoiceCosPath;

    public UploadTxImpl(
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
            String voiceCosPath

    ) {
//        mPrefix = prefix + "_";
        mAppId = appId;
        mRegion = region;
        mBucketName = bucketName;
        mSecretId = secretId;
        mSecretKey = secretKey;
        mSessionToken = sessionToken;
        mExpiredTime = expiredTime;
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

        mCosXmlResultListener = new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                L.e(TAG, "UploadQnImpl 上传-----ok---->");
                if (mList == null || mList.size() == 0) {
                    if (mUploadCallback != null) {
                        mUploadCallback.onFinish(mList, false);
                    }
                    return;
                }
                UploadBean uploadBean = mList.get(mIndex);
                uploadBean.setSuccess(true);
//                uploadBean.setRemoteFileName(StringUtil.contact(mPrefix, uploadBean.getRemoteFileName()));
                if (uploadBean.getType() == UploadBean.IMG && mNeedCompress) {
                    //上传完成后把 压缩后的图片 删掉
                    File compressedFile = uploadBean.getCompressFile();
                    if (compressedFile != null && compressedFile.exists()) {
                        File originFile = uploadBean.getOriginFile();
                        if (originFile != null && !compressedFile.getAbsolutePath().equals(originFile.getAbsolutePath())) {
                            compressedFile.delete();
                        }
                    }
                }
                mIndex++;
                if (mIndex < mList.size()) {
                    uploadNext();
                } else {
                    if (mUploadCallback != null) {
                        mUploadCallback.onFinish(mList, true);
                    }
                }
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException e, CosXmlServiceException e1) {
                if (mUploadCallback != null) {
                    mUploadCallback.onFinish(mList, false);
                }
            }
        };
    }

    @Override
    public void upload(List<UploadBean> list, boolean needCompress, UploadCallback callback) {
        if (callback == null) {
            return;
        }
        if (list == null || list.size() == 0) {
            callback.onFinish(list, false);
            return;
        }
        boolean hasFile = false;
        for (UploadBean bean : list) {
            if (bean.getOriginFile() != null) {
                hasFile = true;
                break;
            }
        }
        if (!hasFile) {
            callback.onFinish(list, true);
            return;
        }
        mList = list;
        mNeedCompress = needCompress;
        mUploadCallback = callback;
        mIndex = 0;
        uploadNext();

    }

    @Override
    public void cancelUpload() {
        if (mList != null) {
            mList.clear();
        }
        mUploadCallback = null;
        if (mCosXmlService != null) {
            mCosXmlService.release();
        }
        mCosXmlService = null;
    }


    private void uploadNext() {
        UploadBean bean = null;
        while (mIndex < mList.size() && (bean = mList.get(mIndex)).getOriginFile() == null) {
            mIndex++;
        }
        if (mIndex >= mList.size()) {
            if (mUploadCallback != null) {
                mUploadCallback.onFinish(mList, true);
            }
            return;
        }
        if (bean.getType() == UploadBean.IMG) {
            bean.setRemoteFileName(StringUtil.contact(StringUtil.generateFileName(), ".jpg"));
        } else if (bean.getType() == UploadBean.VIDEO) {
            bean.setRemoteFileName(StringUtil.contact(StringUtil.generateFileName(), ".mp4"));
        } else if (bean.getType() == UploadBean.VOICE) {
            bean.setRemoteFileName(StringUtil.contact(StringUtil.generateFileName(), ".m4a"));
        }
        if (bean.getType() == UploadBean.IMG && mNeedCompress) {
            if (mLubanBuilder == null) {
                mLubanBuilder = Luban.with(CommonAppContext.getInstance())
                        .ignoreBy(8)//8k以下不压缩
                        .setTargetDir(CommonAppConfig.INNER_PATH)
                        .setRenameListener(new OnRenameListener() {
                            @Override
                            public String rename(String filePath) {
                                return mList.get(mIndex).getRemoteFileName();
                            }
                        }).setCompressListener(new OnCompressListener() {
                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onSuccess(File file) {
                                UploadBean uploadBean = mList.get(mIndex);
                                uploadBean.setCompressFile(file);
                                upload(uploadBean);
                            }

                            @Override
                            public void onError(Throwable e) {
                                upload(mList.get(mIndex));
                            }
                        });
            }
            mLubanBuilder.load(bean.getOriginFile()).launch();
        } else {
            upload(bean);
        }
    }


    private void upload(UploadBean bean) {
        if (mCosXmlService == null) {
            try {
                SessionQCloudCredentials credentials = new SessionQCloudCredentials(mSecretId, mSecretKey, mSessionToken, mExpiredTime);
                QCloudCredentialProvider qCloudCredentialProvider = new StaticCredentialProvider(credentials);
                CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                        .setAppidAndRegion(mAppId, mRegion)
                        .builder();
                mCosXmlService = new CosXmlService(CommonAppContext.getInstance(), serviceConfig, qCloudCredentialProvider);
            } catch (Exception e) {
                if (mUploadCallback != null) {
                    mUploadCallback.onFinish(mList, true);
                }
                return;
            }
        }
        File uploadFile = bean.getOriginFile();
        if (bean.getType() == UploadBean.IMG && mNeedCompress) {
            File compressedFile = bean.getCompressFile();
            if (compressedFile != null && compressedFile.exists()) {
                uploadFile = compressedFile;
            }
        }
        String remotePath = "";
        if (bean.getType() == UploadBean.IMG) {
            remotePath = mImageCosPath;
        } else if (bean.getType() == UploadBean.VIDEO) {
            remotePath = mVideoCosPath;
        } else if (bean.getType() == UploadBean.VOICE) {
            remotePath = mVoiceCosPath;
        }
        String cosPath = TextUtils.isEmpty(remotePath) ? bean.getRemoteFileName() : StringUtil.contact(remotePath, bean.getRemoteFileName());
        PutObjectRequest putObjectRequest = new PutObjectRequest(mBucketName, cosPath, uploadFile.getAbsolutePath());
        putObjectRequest.setProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                L.e(TAG, "---上传进度--->" + progress * 100 / max);
            }
        });
        // 使用异步回调上传
        mCosXmlService.putObjectAsync(putObjectRequest, mCosXmlResultListener);
    }

}
