package com.yunbao.main.utils;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class MainTextRender {

    private static ForegroundColorSpan sColorSpan1;

    static {
        sColorSpan1 = new ForegroundColorSpan(0xff969696);
    }

    public static CharSequence renderImMsg(String name, String content) {
        SpannableStringBuilder builder = new SpannableStringBuilder(name + " " + content);
        builder.setSpan(sColorSpan1, name.length(), builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }


}
