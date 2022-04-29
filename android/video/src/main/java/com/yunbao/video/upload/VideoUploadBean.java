package com.yunbao.video.upload;

import java.io.File;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class VideoUploadBean {
    private File mVideoFile;
    private File mVideoWaterFile;//水印视频
    private File mImageFile;
    private String mResultVideoUrl;//视频上传结果的url
    private String mResultWaterVideoUrl;//水印视频上传结果的url
    private String mResultImageUrl;//图片上传结果的url


    public VideoUploadBean(File videoFile, File imageFile) {
        mVideoFile = videoFile;
        mImageFile = imageFile;
    }

    public File getVideoFile() {
        return mVideoFile;
    }

    public void setVideoFile(File videoFile) {
        mVideoFile = videoFile;
    }

    public File getVideoWaterFile() {
        return mVideoWaterFile;
    }

    public void setVideoWaterFile(File videoWaterFile) {
        mVideoWaterFile = videoWaterFile;
    }

    public File getImageFile() {
        return mImageFile;
    }

    public void setImageFile(File imageFile) {
        mImageFile = imageFile;
    }

    public String getResultVideoUrl() {
        return mResultVideoUrl;
    }

    public void setResultVideoUrl(String resultVideoUrl) {
        mResultVideoUrl = resultVideoUrl;
    }

    public String getResultWaterVideoUrl() {
        return mResultWaterVideoUrl;
    }

    public void setResultWaterVideoUrl(String resultWaterVideoUrl) {
        mResultWaterVideoUrl = resultWaterVideoUrl;
    }

    public String getResultImageUrl() {
        return mResultImageUrl;
    }

    public void setResultImageUrl(String resultImageUrl) {
        mResultImageUrl = resultImageUrl;
    }

    public void deleteFile() {
        if (mVideoFile != null && mVideoFile.exists()) {
            mVideoFile.delete();
        }
        if (mVideoWaterFile != null && mVideoWaterFile.exists()) {
            mVideoWaterFile.delete();
        }
        if (mImageFile != null && mImageFile.exists()) {
            mImageFile.delete();
        }
    }


}
