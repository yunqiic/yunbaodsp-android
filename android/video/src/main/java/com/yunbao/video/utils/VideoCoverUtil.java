package com.yunbao.video.utils;

import android.graphics.Bitmap;
import android.util.LongSparseArray;

import com.yunbao.common.utils.L;
import com.yunbao.video.bean.VideoPubCoverBean;

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
public class VideoCoverUtil {

    private static final String TAG = "视频帧";
    private static VideoCoverUtil sInstance;
    private List<VideoPubCoverBean> mList;

    private VideoCoverUtil() {

    }

    public static VideoCoverUtil getInstance() {
        if (sInstance == null) {
            synchronized (VideoCoverUtil.class) {
                if (sInstance == null) {
                    sInstance = new VideoCoverUtil();
                }
            }
        }
        return sInstance;
    }


    public void ready() {

    }

    public void addBitmap(LongSparseArray<Bitmap> sparseArray, long startTime, long endTime) {
        L.e(TAG, "开始---------->");
        if (mList == null) {
            mList = new ArrayList<>();
        } else {
            if (mList.size() > 0) {
                for (VideoPubCoverBean bean : mList) {
                    Bitmap bitmap = bean.getBitmap();
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
                mList.clear();
            }
        }
        if (sparseArray != null && sparseArray.size() > 0) {
            for (int i = 0, size = sparseArray.size(); i < size; i++) {
                long time = sparseArray.keyAt(i);
                if (time <= endTime && time >= startTime) {
                    mList.add(new VideoPubCoverBean(sparseArray.valueAt(i)));
                    L.e(TAG, "获取到---------->");
                }
            }
        }
        if (mList.size() > 0) {
            VideoPubCoverBean bean = mList.get(0);
            bean.setChecked(true);
            bean.setUse(true);
        }
        L.e(TAG, "结束---------->");

    }


//    public void setVideoFile(String videoPath) {
//        MediaMetadataRetriever mmr = null;
//        try {
//            if (mList == null) {
//                mList = new ArrayList<>();
//            } else {
//                if (mList.size() > 0) {
//                    for (VideoPubCoverBean bean : mList) {
//                        Bitmap bitmap = bean.getBitmap();
//                        if (bitmap != null && !bitmap.isRecycled()) {
//                            bitmap.recycle();
//                        }
//                    }
//                    mList.clear();
//                }
//            }
//            mmr = new MediaMetadataRetriever();
//            mmr.setDataSource(videoPath);
//
//            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//            L.e(TAG, "duration = " + duration);
//            int durationMs = Integer.parseInt(duration);
//
//            //每1秒取一次
//            for (int i = 0; i < durationMs; i += 1000) {
////                long start = System.nanoTime();
//                L.e(TAG, "getFrameAtTime time = " + i);
//                //这里传入的是微秒
//                Bitmap bitmap = mmr.getFrameAtTime(i * 1000);
////                long end = System.nanoTime();
////                long cost = end - start;
////                L.e(TAG, "cost time in millis = " + (cost * 1f / 1000000));
//                VideoPubCoverBean bean = new VideoPubCoverBean(bitmap);
//                if (i == 0) {
//                    bean.setChecked(true);
//                }
//                mList.add(bean);
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (mmr != null) {
//                mmr.release();
//            }
//        }
//
//    }

    public Bitmap getCoverBitmap() {
        if (mList != null && mList.size() > 0) {
            for (VideoPubCoverBean bean : mList) {
                if (bean.isUse()) {
                    return bean.getBitmap();
                }
            }
        }
        return null;
    }

    public List<VideoPubCoverBean> getList() {
        return mList;
    }

    public void release() {
        if (mList != null && mList.size() > 0) {
            for (VideoPubCoverBean bean : mList) {
                Bitmap bitmap = bean.getBitmap();
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
            mList.clear();
        }
    }


}
