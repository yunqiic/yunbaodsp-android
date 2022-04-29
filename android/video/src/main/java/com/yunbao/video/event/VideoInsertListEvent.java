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

public class VideoInsertListEvent {

    private String mKey;
    private int mStartPosition;
    private int mInsertCount;

    public VideoInsertListEvent(String key, int startPosition, int count) {
        mKey = key;
        mStartPosition = startPosition;
        mInsertCount = count;
    }

    public int getStartPosition() {
        return mStartPosition;
    }

    public String getKey() {
        return mKey;
    }

    public int getInsertCount() {
        return mInsertCount;
    }
}
