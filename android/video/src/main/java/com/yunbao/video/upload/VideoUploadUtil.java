package com.yunbao.video.upload;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.ToastUtil;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class VideoUploadUtil {

    private static VideoUploadStrategy sStrategy;

    public static void startUpload(final CommonCallback<VideoUploadStrategy> commonCallback) {
        CommonHttpUtil.getCosInfo(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    final String cloudType = obj.getString("cloudtype");
                    if ("qiniu".equals(cloudType)) {//七牛云存储
                        JSONObject qiniuInfo = obj.getJSONObject("qiniuInfo");
                        sStrategy = new VideoUploadQnImpl(qiniuInfo.getString("qiniuToken"), qiniuInfo.getString("qiniu_zone"), cloudType);
                        if (commonCallback != null) {
                            commonCallback.callback(sStrategy);
                        }
                    } else if ("tx".equals(cloudType)) {//腾讯云存储
                        JSONObject txInfo = obj.getJSONObject("txCloudInfo");
                        final String appId = txInfo.getString("appid");
                        final String region = txInfo.getString("region");
                        final String bucketName = txInfo.getString("bucket");
                        CommonHttpUtil.getTxUploadCredential(new HttpCallback() {
                            @Override
                            public void onSuccess(int code, String msg, String[] info) {
                                if (code == 0) {
                                    if (info.length > 0) {
                                        JSONObject cls = JSON.parseObject(info[0]);
                                        sStrategy = new VideoUploadTxImpl(appId, region, bucketName,
                                                cls.getString("tmpSecretId"),
                                                cls.getString("tmpSecretKey"),
                                                cls.getString("sessionToken"),
                                                cls.getLongValue("expiredTime"),
                                                cloudType, null, null, null);
                                        if (commonCallback != null) {
                                            commonCallback.callback(sStrategy);
                                        }
                                    }
                                } else {
                                    ToastUtil.show(msg);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public static void cancelUpload() {
        CommonHttpUtil.cancel(CommonHttpConsts.GET_COS_INFO);
        CommonHttpUtil.cancel(CommonHttpConsts.GET_TX_UPLOAD_TOKEN);
        if (sStrategy != null) {
            sStrategy.cancel();
        }
    }


}
