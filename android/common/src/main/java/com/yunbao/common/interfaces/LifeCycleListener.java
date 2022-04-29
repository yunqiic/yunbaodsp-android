package com.yunbao.common.interfaces;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public interface LifeCycleListener {

    void onCreate();

    void onStart();

    void onReStart();

    void onResume();

    void onPause();

    void onStop();

    void onDestroy();
}
