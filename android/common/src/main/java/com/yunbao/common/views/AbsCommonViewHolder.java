package com.yunbao.common.views;

import android.content.Context;
import android.view.ViewGroup;


// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public abstract class AbsCommonViewHolder extends AbsViewHolder {

    protected boolean mFirstLoadData = true;
    private boolean mShowed;

    public AbsCommonViewHolder(Context context, ViewGroup parentView, Object... args) {
        super(context, parentView, args);
    }

    public AbsCommonViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    public void loadData() {
    }

    protected boolean isFirstLoadData() {
        if (mFirstLoadData) {
            mFirstLoadData = false;
            return true;
        }
        return false;
    }


    public void setShowed(boolean showed) {
        mShowed = showed;
    }

    public boolean isShowed() {
        return mShowed;
    }
}
