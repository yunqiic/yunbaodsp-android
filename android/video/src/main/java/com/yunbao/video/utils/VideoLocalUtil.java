package com.yunbao.video.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;

import com.yunbao.common.CommonAppContext;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.video.bean.VideoChooseBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class VideoLocalUtil {

    private ContentResolver mContentResolver;
    private CommonCallback<List<VideoChooseBean>> mCallback;
    private boolean mStop;

    public VideoLocalUtil() {
        mContentResolver = CommonAppContext.getInstance().getContentResolver();
    }

    public void getLocalVideoList(CommonCallback<List<VideoChooseBean>> callback) {
        if (callback == null) {
            return;
        }
        mCallback = callback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    final List<VideoChooseBean> videoList = getAllVideo();
                    CommonAppContext.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCallback != null) {
                                mCallback.callback(videoList);
                            }
                        }
                    });
                }
            }
        }).start();
    }

    private List<VideoChooseBean> getAllVideo() {
        List<VideoChooseBean> videoList = new ArrayList<>();
        String[] mediaColumns = new String[]{
                MediaStore.Video.VideoColumns._ID,
                MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.VideoColumns.DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DURATION
        };
        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    mediaColumns, null, null, MediaStore.Images.Media.DATE_MODIFIED + " desc");
            if (cursor != null) {
                while (!mStop && cursor.moveToNext()) {
                    String videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                    File file = new File(videoPath);
                    boolean canRead = file.canRead();
                    long length = file.length();
                    if (!canRead || length == 0) {
                        continue;
                    }
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    if (duration <= 0) {
                        continue;
                    }
                    VideoChooseBean bean = new VideoChooseBean();
                    bean.setVideoPath(videoPath);
                    bean.setDuration(duration);
                    bean.setDurationString(StringUtil.getDurationText(duration));
                    videoList.add(bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return videoList;
    }

    public void release() {
        mStop = true;
        mCallback = null;
    }

}
