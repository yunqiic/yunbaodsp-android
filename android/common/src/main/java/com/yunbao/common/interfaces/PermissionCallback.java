package com.yunbao.common.interfaces;

import java.util.HashMap;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public abstract class PermissionCallback {

    public abstract void onAllGranted();

    public void onResult(HashMap<String, Boolean> resultMap) {

    }
}
