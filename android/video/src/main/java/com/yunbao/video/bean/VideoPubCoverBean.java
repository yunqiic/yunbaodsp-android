package com.yunbao.video.bean;

import android.graphics.Bitmap;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class VideoPubCoverBean {
//    private int mTimeStamp;
    private Bitmap mBitmap;
    private boolean mChecked;
    private boolean mUse;


    public VideoPubCoverBean( Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }

    public boolean isUse() {
        return mUse;
    }

    public void setUse(boolean use) {
        mUse = use;
    }
}
