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

public class VideoDeleteEvent {

    private String mVideoId;
    private String mVideoKey;

    public VideoDeleteEvent(String videoId, String videoKey) {
        mVideoId = videoId;
        mVideoKey = videoKey;
    }

    public String getVideoId() {
        return mVideoId;
    }

    public String getVideoKey() {
        return mVideoKey;
    }

}
