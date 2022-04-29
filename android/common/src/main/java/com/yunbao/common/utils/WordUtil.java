package com.yunbao.common.utils;

import android.content.res.Resources;

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
public class WordUtil {

    private static Resources sResources;

    static {
        sResources = CommonAppContext.getInstance().getResources();
    }

    public static String getString(int res) {
        return sResources.getString(res);
    }
}
