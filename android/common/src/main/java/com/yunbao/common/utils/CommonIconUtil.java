package com.yunbao.common.utils;

import android.util.SparseIntArray;

import com.yunbao.common.Constants;
import com.yunbao.common.R;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class CommonIconUtil {

    public static int getSexIcon(int key) {
        return key == 1 ? R.mipmap.icon_sex_male_1 : R.mipmap.icon_sex_female_1;
    }

    private static SparseIntArray sCashTypeMap;//提现图片

    static {
        sCashTypeMap = new SparseIntArray();
        sCashTypeMap.put(Constants.CASH_ACCOUNT_ALI, R.mipmap.icon_cash_ali);
        sCashTypeMap.put(Constants.CASH_ACCOUNT_WX, R.mipmap.icon_cash_wx);
        sCashTypeMap.put(Constants.CASH_ACCOUNT_BANK, R.mipmap.icon_cash_bank);
    }

    public static int getCashTypeIcon(int key) {
        return sCashTypeMap.get(key);
    }

}
