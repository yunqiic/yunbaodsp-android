package com.yunbao.video.event;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class VideoScrollPageEvent {

    private String mKey;
    private int mPage;

    public VideoScrollPageEvent(String key, int page) {
        mKey = key;
        mPage = page;
    }

    public String getKey() {
        return mKey;
    }

    public int getPage() {
        return mPage;
    }

}
