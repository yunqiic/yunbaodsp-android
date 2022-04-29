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
public class VideoUploadEvent {

    private String mVideoTitle;//视频标题
    private String mVideoPath;//视频路径
    private String mVideoPathWater;//水印视频路径
    private String mMusicId;//视频背景音乐
    private int mSaveType;
    private int isUserad;
    private String useradUrl;


    public String getVideoTitle() {
        return mVideoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        mVideoTitle = videoTitle;
    }

    public String getVideoPath() {
        return mVideoPath;
    }

    public void setVideoPath(String videoPath) {
        mVideoPath = videoPath;
    }

    public String getVideoPathWater() {
        return mVideoPathWater;
    }

    public void setVideoPathWater(String videoPathWater) {
        mVideoPathWater = videoPathWater;
    }

    public String getMusicId() {
        return mMusicId;
    }

    public void setMusicId(String musicId) {
        mMusicId = musicId;
    }



    public int getSaveType() {
        return mSaveType;
    }

    public void setSaveType(int saveType) {
        mSaveType = saveType;
    }

    public int getIsUserad() {
        return isUserad;
    }

    public void setIsUserad(int isUserad) {
        this.isUserad = isUserad;
    }

    public String getUseradUrl() {
        return useradUrl;
    }

    public void setUseradUrl(String useradUrl) {
        this.useradUrl = useradUrl;
    }
}
