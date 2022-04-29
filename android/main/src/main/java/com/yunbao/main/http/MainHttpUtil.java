package com.yunbao.main.http;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.http.HttpClient;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.MD5Util;
import com.yunbao.common.utils.SpUtil;
import com.yunbao.common.utils.StringUtil;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class MainHttpUtil {

    private static final String DEVICE = "android";

    /**
     * 取消网络请求
     */
    public static void cancel(String tag) {
        HttpClient.getInstance().cancel(tag);
    }


    /**
     * 手机号 验证码登录
     */
    public static void login(String phoneNum, String code,HttpCallback callback) {
        HttpClient.getInstance().get("Login.userLogin", MainHttpConsts.LOGIN)
                .params("user_login", phoneNum)
                .params("code", code)
                .params("source", "android")
                .params("mobileid", CommonAppConfig.getInstance().getDeviceId())
                .params("country_code", 86)
                .execute(callback);
    }

    /**
     * 获取登录验证码接口
     */
    public static void getLoginCode(String phoneNum,HttpCallback callback) {
        String time = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = MD5Util.getMD5(StringUtil.contact("mobile=", phoneNum, "&time=", time, "&", CommonHttpUtil.SALT));
        HttpClient.getInstance().get("Login.getLoginCode", MainHttpConsts.GET_LOGIN_CODE)
                .params("mobile", phoneNum)
                .params("country_code", 86)
                .params("time", time)
                .params("sign", sign)
                .execute(callback);
    }




    /**
     * 获取登录信息，三方登录类型，服务和隐私条款
     */
    public static void getLoginInfo(HttpCallback callback) {
        HttpClient.getInstance().get("Home.getLogin", MainHttpConsts.GET_LOGIN_INFO)
                .execute(callback);
    }




    /**
     * 获取用户信息
     */
    public static void getBaseInfo(final HttpCallback callback) {
        HttpClient.getInstance().get("User.getBaseInfo", MainHttpConsts.GET_BASE_INFO)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            UserBean bean = JSON.parseObject(info[0], UserBean.class);
                            CommonAppConfig.getInstance().setUserBean(bean);
                            SpUtil.getInstance().setStringValue(SpUtil.USER_INFO, info[0]);
                        }
                        if (callback != null) {
                            callback.onSuccess(code, msg, info);
                        }
                    }
                });
    }

    /**
     * 获取用户信息
     */
    public static void getBaseInfo(String uid, String token, final CommonCallback<UserBean> commonCallback) {
        HttpClient.getInstance().get("User.getBaseInfo", MainHttpConsts.GET_BASE_INFO)
                .params("uid", uid)
                .params("token", token)
                .execute(new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            UserBean bean = JSON.parseObject(info[0], UserBean.class);
                            CommonAppConfig.getInstance().setUserBean(bean);
                            SpUtil.getInstance().setStringValue(SpUtil.USER_INFO, info[0]);
                            if (commonCallback != null) {
                                commonCallback.callback(bean);
                            }
                        }
                    }

                    @Override
                    public void onError() {
                        if (commonCallback != null) {
                            commonCallback.callback(null);
                        }
                    }
                });
    }

    /**
     * 获取用户信息
     */
    public static void getBaseInfo(CommonCallback<UserBean> commonCallback) {
        getBaseInfo(CommonAppConfig.getInstance().getUid(),
                CommonAppConfig.getInstance().getToken(),
                commonCallback);
    }


    /**
     * 获取推荐视频列表
     */
    public static void getRecommendVideoList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getRecommendVideos", MainHttpConsts.GET_RECOMMEND_VIDEO_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("p", p)
                .params("isstart", 0)
                .params("mobileid", CommonAppConfig.getInstance().getDeviceId())
                .execute(callback);
    }

    /**
     * 获取热门视频列表
     */
    public static void getHotVideoList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getVideoList", MainHttpConsts.GET_HOT_VIDEO_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("p", p)
                .execute(callback);
    }




    /**
     * 获取关注视频列表
     */
    public static void getFollowVideoList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getAttentionVideo", MainHttpConsts.GET_FOLLOW_VIDEO_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }



    /**
     * 用户个人主页信息
     */
    public static void getUserHome(String touid, HttpCallback callback) {
        HttpClient.getInstance().get("User.getUserHome", MainHttpConsts.GET_USER_HOME)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("touid", touid)
                .execute(callback);

    }


    /**
     * 获取某人发布的视频
     *
     * @param touid 对方的id
     */
    public static void getUserWorkList(String touid, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getHomeVideo", MainHttpConsts.GET_USER_WORK_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("touid", touid)
                .params("p", p)
                .execute(callback);
    }

    /**
     * 获取某人喜欢的视频
     *
     * @param touid 对方的id
     */
    public static void getUserLikeList(String touid, int p, HttpCallback callback) {
        HttpClient.getInstance().get("User.getLikeVideos", MainHttpConsts.GET_USER_LIKE_LIST)
                .params("touid", touid)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("p", p)
                .execute(callback);
    }




    /**
     * 获取对方的粉丝列表
     *
     * @param toUid 对方的uid
     */
    public static void getFansList(String toUid, int p, HttpCallback callback) {
        HttpClient.getInstance().get("User.getFansList", MainHttpConsts.GET_FANS_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("touid", toUid)
                .params("p", p)
                .execute(callback);
    }

    /**
     * 获取对方的关注列表
     *
     * @param toUid 对方的uid
     */
    public static void getFollowList(String toUid, int p, String key, HttpCallback callback) {
        HttpClient.getInstance().get("User.getFollowsList", MainHttpConsts.GET_FOLLOW_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("touid", toUid)
                .params("key", key)
                .params("p", p)
                .execute(callback);
    }

    /**
     * 搜索用户
     */
    public static void searchUser(String key, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Home.search", MainHttpConsts.SEARCH_USER)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("key", key)
                .params("p", p)
                .execute(callback);
    }


    /**
     * 搜索视频
     */
    public static void searchVideo(String key, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Home.videoSearch", MainHttpConsts.SEARCH_VIDEO)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("key", key)
                .params("p", p)
                .execute(callback);
    }


    /**
     * 获取 我的收益 可提现金额数
     */
    public static void getProfit(HttpCallback callback) {
        HttpClient.getInstance().get("Cash.GetProfit", MainHttpConsts.GET_PROFIT)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }

    /**
     * 获取 提现账户列表
     */
    public static void getCashAccountList(HttpCallback callback) {
        HttpClient.getInstance().get("Cash.GetAccountList", MainHttpConsts.GET_USER_ACCOUNT_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }


    /**
     * 添加 提现账户
     */
    public static void addCashAccount(String account, String name, String bank, int type, HttpCallback callback) {
        HttpClient.getInstance().get("Cash.SetAccount", MainHttpConsts.ADD_CASH_ACCOUNT)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("account", account)
                .params("name", name)
                .params("account_bank", bank)
                .params("type", type)
                .execute(callback);
    }

    /**
     * 删除 提现账户
     */
    public static void deleteCashAccount(String accountId, HttpCallback callback) {
        HttpClient.getInstance().get("Cash.delAccount", MainHttpConsts.DEL_CASH_ACCOUNT)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("id", accountId)
                .execute(callback);
    }

    /**
     * 提现
     */
    public static void doCash(String money, String accountId, HttpCallback callback) {
        HttpClient.getInstance().get("Cash.setCash", MainHttpConsts.DO_CASH)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("money", money)//提现金额
                .params("accountid", accountId)//账号ID
                .execute(callback);
    }

    /**
     * 视频观看记录
     */
    public static void getViewRecord(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.GetViewRecord", MainHttpConsts.GET_VIEW_RECORD)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 删除视频观看记录
     */
    public static void deleteViewRecord(String videoids, HttpCallback callback) {
        HttpClient.getInstance().get("Video.deltViewRecord", MainHttpConsts.DELETE_VIEW_RECORD)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoids", videoids)
                .execute(callback);
    }




    /**
     * 添加及更新认证信息
     *
     * @param name      姓名
     * @param card      身份证号
     * @param phone     手机号
     * @param cardFront 身份证正面图片
     * @param cardBack  身份证反面图片
     * @param cardHand  手持身份证图片
     */
    public static void setAuth(
            String name,
            String card,
            String phone,
            String cardFront,
            String cardBack,
            String cardHand,
            HttpCallback callback) {
        HttpClient.getInstance().get("Auth.setAuth", MainHttpConsts.SET_AUTH)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("real_name", name)
                .params("cer_no", card)
                .params("mobile", phone)
                .params("front_view", cardFront)
                .params("back_view", cardBack)
                .params("handset_view", cardHand)
                .execute(callback);
    }


    /**
     * 获取用户认证信息
     */
    public static void getAuthInfo(HttpCallback callback) {
        HttpClient.getInstance().get("Auth.getAuth", MainHttpConsts.GET_AUTH_INFO)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }

    /**
     * 店铺申请检测
     */
    public static void checkShopApply(HttpCallback callback) {
        HttpClient.getInstance().get("Shop.getCheckShop", MainHttpConsts.CHECK_SHOP_APPLY)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }



    /**
     * 个人中心获取分享名片信息
     */
    public static void getAgentCode(HttpCallback callback) {
        HttpClient.getInstance().get("Agent.getCode", MainHttpConsts.GET_AGENT_CODE)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }

}




