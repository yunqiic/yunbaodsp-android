package com.yunbao.shortvideo;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.ugc.TXUGCBase;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.utils.L;


// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class AppContext extends CommonAppContext {


    @Override
    public void onCreate() {
        super.onCreate();
        initSdk();
    }


    public static void initSdk() {
        CommonAppContext context = CommonAppContext.getInstance();
        //初始化Http
        CommonHttpUtil.init();
        //腾讯云视频鉴权url
        String ugcLicenceUrl = "http://license.vod2.myqcloud.com/license/v1/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx/TXUgcSDK.licence";
        //腾讯云视频鉴权key
        String ugcKey = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
        TXUGCBase.getInstance().setLicence(context, ugcLicenceUrl, ugcKey);
        L.setDeBug(BuildConfig.DEBUG);
        TXCLog.setConsoleEnabled(true);
    }


}
