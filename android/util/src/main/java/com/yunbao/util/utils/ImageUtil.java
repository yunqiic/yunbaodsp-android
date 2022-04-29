package com.yunbao.util.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;

import com.yunbao.common.CommonAppContext;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.util.bean.ChatChooseImageBean;

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

public class ImageUtil {

    private ContentResolver mContentResolver;
    private CommonCallback<List<ChatChooseImageBean>> mCallback;
    private boolean mStop;

    public ImageUtil() {
        mContentResolver = CommonAppContext.getInstance().getContentResolver();
    }

    public void getLocalImageList(CommonCallback<List<ChatChooseImageBean>> callback) {
        if (callback == null) {
            return;
        }
        mCallback = callback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    final List<ChatChooseImageBean> imageList = getAllImage();
                    CommonAppContext.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCallback != null) {
                                mCallback.callback(imageList);
                            }
                        }
                    });
                }
            }
        }).start();
    }

    public void imageCallback(List<ChatChooseImageBean> imageList) {
        if (!mStop && mCallback != null) {
            mCallback.callback(imageList);
        }
    }

    private List<ChatChooseImageBean> getAllImage() {
        List<ChatChooseImageBean> imageList = new ArrayList<>();
        Cursor cursor = null;
        try {
            //只查询jpeg和png的图片
            cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                    "mime_type=? or mime_type=?",
                    new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED+" desc");
            if (cursor != null) {
                while (!mStop && cursor.moveToNext()) {
                    String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
//                    if (!imagePath.contains("/DCIM/")) {
//                        continue;
//                    }
                    File file = new File(imagePath);
                    if (!file.exists()) {
                        continue;
                    }
                    boolean canRead = file.canRead();
                    long length = file.length();
                    if (!canRead || length == 0) {
                        continue;
                    }
                    imageList.add(new ChatChooseImageBean(file));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return imageList;
    }

    public void release() {
        mStop = true;
        mCallback = null;
    }

}
