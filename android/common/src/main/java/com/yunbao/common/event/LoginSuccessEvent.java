package com.yunbao.common.event;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class LoginSuccessEvent {

    private String mUid;
    private boolean mIsReg;

    public LoginSuccessEvent(String uid,boolean isReg) {
        mUid = uid;
        mIsReg=isReg;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public boolean isReg() {
        return mIsReg;
    }

    public void setReg(boolean reg) {
        mIsReg = reg;
    }
}
