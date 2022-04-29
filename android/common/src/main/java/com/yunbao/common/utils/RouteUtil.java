package com.yunbao.common.utils;

import android.content.Context;
import android.content.Intent;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.Constants;
import com.yunbao.common.bean.VideoBean;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class RouteUtil {
    /**
     * 启动页`
     */
    public static void forwardLauncher() {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.shortvideo.activity.LauncherActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        CommonAppContext.getInstance().startActivity(intent);
    }

    /**
     * 启动页
     */
    public static void forwardLauncher(String pushVideoId) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.shortvideo.activity.LauncherActivity");
        intent.putExtra(Constants.VIDEO_ID, pushVideoId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        CommonAppContext.getInstance().startActivity(intent);
    }

    /**
     * 跳转到个人主页
     */
    public static void forwardUserHome(Context context, String toUid) {
        forwardUserHome(context, toUid, true);
    }

    /**
     * 跳转到个人主页
     *
     * @param imEnable 是否使用私信
     */
    public static void forwardUserHome(Context context, String toUid, boolean imEnable) {
        forwardUserHome(context, toUid, imEnable, false);
    }


    /**
     * 跳转到个人主页
     *
     * @param imEnable 是否使用私信
     * @param fromLive 是否是直播间过来的
     */
    public static void forwardUserHome(Context context, String toUid, boolean imEnable, boolean fromLive) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.UserHomeActivity");
        intent.putExtra(Constants.TO_UID, toUid);
        intent.putExtra(Constants.IM_ENABLE, imEnable);
        intent.putExtra(Constants.FROM_LIVE, fromLive);
        context.startActivity(intent);
    }

    /**
     * 跳转到登录
     */
    public static void forwardLogin(Context context) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.LoginActivity");
        context.startActivity(intent);
    }



    /**
     * 跳转到标提现
     */
    public static void forwardCash(Context context) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.MyProfitActivity");
        context.startActivity(intent);
    }



    /**
     * 跳转到我的认证
     */
    public static void forwardAuth(Context context) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.AuthActivity");
        context.startActivity(intent);
    }




    /**
     * 跳转视频播放页
     *
     * @param videoBean
     */
    public static void forwardVideoPlay(Context context, VideoBean videoBean) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.video.activity.VideoPlayActivity");
        intent.putExtra(Constants.VIDEO_BEAN, videoBean);
        intent.putExtra(Constants.VIDEO_KEY, Constants.VIDEO_SINGLE);
        intent.putExtra(Constants.VIDEO_POSITION, 0);
        intent.putExtra(Constants.VIDEO_PAGE, 1);
        intent.putExtra(Constants.VIDEO_FROM_USER_HOME, false);
        intent.putExtra(Constants.VIDEO_PUSH, true);
        context.startActivity(intent);
    }




}
