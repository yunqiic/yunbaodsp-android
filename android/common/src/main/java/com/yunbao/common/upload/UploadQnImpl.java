package com.yunbao.common.upload;

import android.text.TextUtils;

import com.qiniu.android.common.FixedZone;
import com.qiniu.android.common.Zone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.StringUtil;

import org.json.JSONObject;

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
public class UploadQnImpl implements UploadStrategy {

    private static final String TAG = "UploadQnImpl";
    private List<UploadBean> mList;
    private int mIndex;
    private boolean mNeedCompress;
    private UploadCallback mUploadCallback;
    private String mToken;
    private String mZone;
    private UploadManager mUploadManager;
    private UpCompletionHandler mCompletionHandler;//上传回调
    private Luban.Builder mLubanBuilder;
//    private String mPrefix;

    public UploadQnImpl(String uploadToken, String zone, String prefix) {
        mToken = uploadToken;
        mZone = zone;
//        mPrefix = prefix + "_";
        mCompletionHandler = new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject response) {
                L.e("UploadQnImpl 上传-----ok----> " + info.isOK() + "--key---> " + "---response---> " + (response != null ? response.toString() : null));
                if (mList == null || mList.size() == 0) {
                    if (mUploadCallback != null) {
                        mUploadCallback.onFinish(mList, false);
                    }
                    return;
                }
                UploadBean uploadBean = mList.get(mIndex);
                if (info.isOK()) {
                    uploadBean.setSuccess(true);
                    uploadBean.setRemoteFileName(key);
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
                } else {
                    upload(mList.get(mIndex));//上传失败后 重新上传
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
        if (bean != null && !TextUtils.isEmpty(mToken) && mCompletionHandler != null) {
            if (mUploadManager == null) {
                //存储区域
                Zone zone = null;
                if ("qiniu_hd".equals(mZone)) {//华东
                    zone = FixedZone.zone0;
                } else if ("qiniu_hb".equals(mZone)) {//华北
                    zone = FixedZone.zone1;
                } else if ("qiniu_hn".equals(mZone)) {//华南
                    zone = FixedZone.zone2;
                } else if ("qiniu_bm".equals(mZone)) {//北美
                    zone = FixedZone.zoneNa0;
                } else if ("qiniu_xjp".equals(mZone)) {//东南亚
                    zone = FixedZone.zoneAs0;
                }
                if(zone==null){
                    return;
                }
                Configuration configuration = new Configuration.Builder().zone(zone).build();
                mUploadManager = new UploadManager(configuration);
            }
            File uploadFile = bean.getOriginFile();
            if (bean.getType() == UploadBean.IMG && mNeedCompress) {
                File compressedFile = bean.getCompressFile();
                if (compressedFile != null && compressedFile.exists()) {
                    uploadFile = compressedFile;
                }
            }
            mUploadManager.put(uploadFile, bean.getRemoteFileName(), mToken, mCompletionHandler, null);
        } else {
            if (mUploadCallback != null) {
                mUploadCallback.onFinish(mList, false);
            }
        }
    }

}
