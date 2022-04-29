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

public class VideoCommentEvent {
    private String mVideoId;
    private String mCommentNum;

    public VideoCommentEvent(String videoId, String commentNum) {
        mVideoId = videoId;
        mCommentNum = commentNum;
    }

    public String getVideoId() {
        return mVideoId;
    }

    public void setVideoId(String videoId) {
        mVideoId = videoId;
    }

    public String getCommentNum() {
        return mCommentNum;
    }

    public void setCommentNum(String commentNum) {
        mCommentNum = commentNum;
    }
}
