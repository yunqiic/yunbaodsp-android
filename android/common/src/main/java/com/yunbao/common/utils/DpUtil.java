package com.yunbao.common.utils;

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
public class DpUtil {

    private static float scale;

    static {
        scale = CommonAppContext.getInstance().getResources().getDisplayMetrics().density;
    }

    public static int dp2px(int dpVal) {
        return (int) (scale * dpVal + 0.5f);
    }
}
