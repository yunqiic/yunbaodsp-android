package com.yunbao.main.views;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.yunbao.common.views.AbsViewHolder;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public abstract class AbsSearchViewHolder extends AbsVideoMainViewHolder {

    protected boolean mFirstLoadData = true;
    private boolean mShowed;
    protected String mKey;

    public AbsSearchViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    public AbsSearchViewHolder(Context context, ViewGroup parentView, Object... args) {
        super(context, parentView, args);
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

    public void setKey(String key) {
        if (!TextUtils.isEmpty(mKey) && !mKey.equals(key)) {
            mFirstLoadData = true;
        }
        mKey = key;
    }

    public void cancelHttpRequest() {
        mFirstLoadData = true;
    }

    public void clearData() {
        mFirstLoadData = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelHttpRequest();
    }
}
