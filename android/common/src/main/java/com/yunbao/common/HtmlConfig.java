package com.yunbao.common;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class HtmlConfig {
    //登录即代表同意服务和隐私条款
    public static final String LOGIN_PRIVCAY = CommonAppConfig.HOST + "/Portal/page/index?id=26";
    //关于我们
    public static final String ABOUT_US = CommonAppConfig.HOST +"/Appapi/About/index";
    //举报用户
    public static final String REPORT = CommonAppConfig.HOST +"/Appapi/Userreport/index?touid=";
    //金币明细
    public static final String COIN_DETAIL = CommonAppConfig.HOST + "/Appapi/cash/record";
    //提现明细
    public static final String CASH_DETAIL = CommonAppConfig.HOST + "/Appapi/cash/cash";

}
