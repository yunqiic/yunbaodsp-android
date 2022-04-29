package com.yunbao.common.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.yunbao.common.CommonAppContext;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class DeviceUtils {

    public static String getDeviceId() {
        String s = StringUtil.contact(
                Settings.System.getString(CommonAppContext.getInstance().getContentResolver(), Settings.System.ANDROID_ID),
                Build.SERIAL,
                Build.FINGERPRINT,
                String.valueOf(Build.TIME),
                Build.USER,
                Build.HOST,
                Build.getRadioVersion(),
                Build.HARDWARE
        );
        return MD5Util.getMD5(s);
    }


}
