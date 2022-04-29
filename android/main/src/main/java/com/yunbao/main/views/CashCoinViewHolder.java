package com.yunbao.main.views;

import android.content.Context;
import android.view.ViewGroup;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.HtmlConfig;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class CashCoinViewHolder extends AbsCashDetailViewHolder {

    public CashCoinViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    public String getHtmlUrl() {
        CommonAppConfig appConfig = CommonAppConfig.getInstance();
        return HtmlConfig.COIN_DETAIL + "&uid=" + appConfig.getUid() + "&token=" + appConfig.getToken();
    }


}
