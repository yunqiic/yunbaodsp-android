package com.yunbao.main.views;

import android.content.Context;
import android.view.ViewGroup;

import com.yunbao.common.bean.VideoBean;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.presenter.CheckVideoPresenter;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public abstract class AbsVideoMainViewHolder extends AbsMainViewHolder {
    private CheckVideoPresenter mCheckVideoPresenter;

    public AbsVideoMainViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    public AbsVideoMainViewHolder(Context context, ViewGroup parentView, Object... args) {
        super(context, parentView, args);
    }

    protected void checkVideo(final VideoBean videoBean, final CommonCallback<Integer> callback) {
        if (mCheckVideoPresenter == null) {
            mCheckVideoPresenter = new CheckVideoPresenter(mContext);
        }
        mCheckVideoPresenter.checkVideo(videoBean.getId(), callback);


    }


}
