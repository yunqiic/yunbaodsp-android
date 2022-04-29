package com.yunbao.common.utils;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.yunbao.common.CommonAppContext;
import com.yunbao.common.R;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class ToastUtil {

    private static Toast sToast;
    private static long sLastTime;
    private static String sLastString;

    static {
        sToast = makeToast();
    }

    private static Toast makeToast() {
        Toast toast = new Toast(CommonAppContext.getInstance());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        View view = LayoutInflater.from(CommonAppContext.getInstance()).inflate(R.layout.view_toast, null);
        toast.setView(view);
        return toast;
    }


    public static void show(int res) {
        show(WordUtil.getString(res));
    }

    public static void show(String s) {
        try {
            if (TextUtils.isEmpty(s)) {
                return;
            }
            long curTime = System.currentTimeMillis();
            if (curTime - sLastTime > 2000) {
                sLastTime = curTime;
                sLastString = s;
                sToast.setText(s);
                sToast.show();
            } else {
                if (!s.equals(sLastString)) {
                    sLastTime = curTime;
                    sLastString = s;
                    sToast = makeToast();
                    sToast.setText(s);
                    sToast.show();
                }
            }

        }catch (Exception e){

        }

    }

}
