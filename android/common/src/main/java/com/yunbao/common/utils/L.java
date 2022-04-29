package com.yunbao.common.utils;

import android.util.Log;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class L {

    private static boolean sDeBug;

    private final static String TAG = "log--->";

    public static void e(String s) {
        e(TAG, s);
    }

    public static void e(String tag, String s) {
        if (sDeBug) {
            Log.e(tag, s);
        }
    }

    public static void setDeBug(boolean deBug){
        sDeBug=deBug;
    }
}
