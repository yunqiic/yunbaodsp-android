package com.yunbao.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import com.yunbao.common.CommonAppContext;

import java.lang.reflect.Method;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class CutoutUtil {
    private static Boolean sAllowDisplayToCutout;

    /**
     * 是否为允许全屏界面显示内容到刘海区域的刘海屏机型（与AndroidManifest中配置对应）
     */
    public static boolean allowDisplayToCutout() {

        if (sAllowDisplayToCutout == null) {
            Context context = CommonAppContext.getInstance();
            if (hasCutout_Huawei(context)) {
                return sAllowDisplayToCutout = true;
            }
            if (hasCutout_OPPO(context)) {
                return sAllowDisplayToCutout = true;
            }
            if (hasCutout_VIVO(context)) {
                return sAllowDisplayToCutout = true;
            }
            if (hasCutout_XIAOMI(context)) {
                return sAllowDisplayToCutout = true;
            }
            return sAllowDisplayToCutout = false;
        } else {
            return sAllowDisplayToCutout;
        }
    }


    /**
     * 是否是华为刘海屏机型
     */
    @SuppressWarnings("unchecked")
    private static boolean hasCutout_Huawei(Context context) {
        if (!Build.MANUFACTURER.equalsIgnoreCase("HUAWEI")) {
            return false;
        }
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            if (HwNotchSizeUtil != null) {
                Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
                return (boolean) get.invoke(HwNotchSizeUtil);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否是oppo刘海屏机型
     */
    @SuppressWarnings("unchecked")
    private static boolean hasCutout_OPPO(Context context) {
        if (!Build.MANUFACTURER.equalsIgnoreCase("oppo")) {
            return false;
        }
        return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    /**
     * 是否是vivo刘海屏机型
     */
    @SuppressWarnings("unchecked")
    private static boolean hasCutout_VIVO(Context context) {
        if (!Build.MANUFACTURER.equalsIgnoreCase("vivo")) {
            return false;
        }
        try {
            ClassLoader cl = context.getClassLoader();
            @SuppressLint("PrivateApi")
            Class ftFeatureUtil = cl.loadClass("android.util.FtFeature");
            if (ftFeatureUtil != null) {
                Method get = ftFeatureUtil.getMethod("isFeatureSupport", int.class);
                return (boolean) get.invoke(ftFeatureUtil, 0x00000020);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否是小米刘海屏机型
     */
    @SuppressWarnings("unchecked")
    private static boolean hasCutout_XIAOMI(Context context) {
        if (!Build.MANUFACTURER.equalsIgnoreCase("xiaomi")) {
            return false;
        }
        try {
            ClassLoader cl = context.getClassLoader();
            @SuppressLint("PrivateApi")
            Class SystemProperties = cl.loadClass("android.os.SystemProperties");
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = int.class;
            Method getInt = SystemProperties.getMethod("getInt", paramTypes);
            //参数
            Object[] params = new Object[2];
            params[0] = "ro.miui.notch";
            params[1] = 0;
            return (Integer) getInt.invoke(SystemProperties, params) == 1;
        } catch (Exception e) {
            return false;
        }
    }


}
