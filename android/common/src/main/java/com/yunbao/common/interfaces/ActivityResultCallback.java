package com.yunbao.common.interfaces;

import android.content.Intent;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public abstract class ActivityResultCallback {
    public abstract void onSuccess(Intent intent);

    public void onFailure() {

    }

    public void onResult(int resultCode, Intent intent) {

    }
}
