package com.yunbao.util.custom;

import android.content.Context;
import android.util.AttributeSet;

import com.yunbao.common.custom.ZoomView;

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

public class MyImageView2 extends ZoomView {

    private File mFile;
    private int mMsgId;

    public MyImageView2(Context context) {
        super(context);
    }

    public MyImageView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImageView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public File getFile() {
        return mFile;
    }

    public void setFile(File file) {
        mFile = file;
    }

    public int getMsgId() {
        return mMsgId;
    }

    public void setMsgId(int msgId) {
        mMsgId = msgId;
    }
}
