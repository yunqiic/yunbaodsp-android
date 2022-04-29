package com.yunbao.common.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.R;
import com.yunbao.common.activity.ErrorActivity;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.event.FollowEvent;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.MD5Util;
import com.yunbao.common.utils.SpUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;

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
public class CommonHttpUtil {

    public static final String SALT = "562d4226cb2a2b4f74b3ef4340828b5d";

    /**
     * 初始化
     */
    public static void init() {
        HttpClient.getInstance().init();
    }

    /**
     * 取消网络请求
     */
    public static void cancel(String tag) {
        HttpClient.getInstance().cancel(tag);
    }


    /**
     * 获取config
     */
    public static void getConfig(final CommonCallback<ConfigBean> commonCallback) {
        HttpClient.getInstance().get("Home.getConfig", CommonHttpConsts.GET_CONFIG)
                .execute(new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            try {
                                JSONObject obj = JSON.parseObject(info[0]);
                                ConfigBean bean = JSON.toJavaObject(obj, ConfigBean.class);
                                CommonAppConfig.getInstance().setConfig(bean);
                                SpUtil.getInstance().setStringValue(SpUtil.CONFIG, info[0]);
                                if (commonCallback != null) {
                                    commonCallback.callback(bean);
                                }
                            } catch (Exception e) {
                                String error = "info[0]:" + info[0] + "\n\n\n" + "Exception:" + e.getClass() + "---message--->" + e.getMessage();
                                ErrorActivity.forward("GetConfig接口返回数据异常", error);
                            }
                        }
                    }

                    @Override
                    public void onError() {
                        if (commonCallback != null) {
                            commonCallback.callback(null);
                        }
                    }
                });
    }





    /**
     * 关注别人 或 取消对别人的关注的接口
     */
    public static void setAttention(String touid, CommonCallback<Integer> callback) {
        setAttention(CommonHttpConsts.SET_ATTENTION, touid, callback);
    }

    /**
     * 关注别人 或 取消对别人的关注的接口
     */
    public static void setAttention(String tag, final String touid, final CommonCallback<Integer> callback) {
        if (touid.equals(CommonAppConfig.getInstance().getUid())) {
            ToastUtil.show(WordUtil.getString(R.string.cannot_follow_self));
            return;
        }
        HttpClient.getInstance().get("User.setAttent", tag)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("touid", touid)
                .execute(new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            int isAttention = JSON.parseObject(info[0]).getIntValue("isattent");//1是 关注  0是未关注
                            EventBus.getDefault().post(new FollowEvent(touid, isAttention));
                            if (callback != null) {
                                callback.callback(isAttention);
                            }
                        }
                    }
                });
    }


    /**
     * 上传头像，用post
     */
    public static void updateAvatar(File file, HttpCallback callback) {
        HttpClient.getInstance().post("User.updateAvatar", CommonHttpConsts.UPDATE_AVATAR)
                .isMultipart(true)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("file", file)
                .execute(callback);
    }

    /**
     * 更新用户资料
     *
     * @param fields 用户资料 ,以json形式出现
     */
    public static void updateFields(String fields, HttpCallback callback) {
        HttpClient.getInstance().get("User.updateFields", CommonHttpConsts.UPDATE_FIELDS)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("fields", fields)
                .execute(callback);
    }


    /**
     * 充值页面，我的钻石
     */
    public static void getBalance(HttpCallback callback) {
        HttpClient.getInstance().get("User.getBalance", CommonHttpConsts.GET_BALANCE)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("type", 0)
                .execute(callback);
    }




    //不做任何操作的HttpCallback
    public static final HttpCallback NO_CALLBACK = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {

        }
    };

    /**
     * 获取用户余额
     */
    public static void getCoin(HttpCallback callback) {
        HttpClient.getInstance().get("Live.getCoin", CommonHttpConsts.GET_COIN)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }



    /**
     * 获取单个视频信息，主要是该视频关于自己的信息 ，如是否关注，是否点赞等
     *
     * @param videoId 视频的id
     */
    public static void getVideo(String videoId, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getVideo", CommonHttpConsts.GET_VIDEO)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("videoid", videoId)
                .params("mobileid", CommonAppConfig.getInstance().getDeviceId())
                .execute(callback);
    }

    /**
     * 视频扣费
     *
     * @param videoid
     * @param callback
     */
    public static void setVideoPay(String videoid, HttpCallback callback) {
        HttpClient.getInstance().get("Video.setVideoPay", CommonHttpConsts.SET_VIDEO_PAY)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoid", videoid)
                .execute(callback);
    }


    /**
     * 获取腾讯云储存上传签名
     */
    public static void getTxUploadCredential(HttpCallback callback) {
        HttpClient.getInstance().get("Video.getTxCosFederationToken", CommonHttpConsts.GET_TX_UPLOAD_TOKEN)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }

    /**
     * 视频上传 类型 以及 七牛 腾讯 相关信息
     *
     * @param callback
     */
    public static void getCosInfo(HttpCallback callback) {
        HttpClient.getInstance().get("Video.getCosInfo", CommonHttpConsts.GET_COS_INFO)
                .execute(callback);
    }




    /**
     * 用户更换个人中心背景图
     */
    public static void updateBgImg(String img,HttpCallback callback) {
        HttpClient.getInstance().get("User.updateBgImg", CommonHttpConsts.GET_COS_INFO)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("img",img)
                .execute(callback);
    }

}




