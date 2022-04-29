package com.yunbao.common.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.yunbao.common.R;
import com.yunbao.common.utils.WordUtil;

import java.util.List;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class ConfigBean {
    private String version;//Android apk安装包 版本号
    private String downloadApkUrl;//Android apk安装包 下载地址
    private String updateDes;//版本更新描述
    private String liveWxShareUrl;//直播间微信分享地址
    private String liveShareTitle;//直播间分享标题
    private String liveShareDes;//直播间分享描述
    private String agentShareTitle;//全民赚钱分享标题
    private String agentShareDes;//全民赚钱分享描述
    private String videoShareTitle;//短视频分享标题
    private String videoShareDes;//短视频分享描述
    private int videoAuditSwitch;//短视频审核是否开启
    private String coinName;//钻石名称
    private String votesName;//映票名称
    private String mAppName;//app名称
    private String[] liveTimeCoin;//直播间计时收费规则
    private String[] loginType;//三方登录类型
    private String[][] liveType;//直播间开播类型
    private String[] shareType;//分享类型
    private int maintainSwitch;//维护开关
    private String maintainTips;//维护提示
    private int beautyMeiBai;//萌颜美白数值
    private int beautyMoPi;//萌颜磨皮数值
    private int beautyBaoHe;//萌颜饱和数值
    private int beautyFenNen;//萌颜粉嫩数值
    private int beautyBigEye;//萌颜大眼数值
    private int beautyFace;//萌颜瘦脸数值
    private String mAdInfo;//引导页 广告信息
    private int mChatLimitSwitch;
    private int mChatLimitNum;
    private String mWaterMarkUrl;//水印

    private List<LiveClassBean> liveClass;//直播分类
    private int mAuthSwitch;
    private String mShopSystemName;//商店名称

    private String beautyKey;//萌颜鉴权码
    private int serviceSwitch;
    private String serviceUrl;

    @JSONField(name = "service_switch")
    public int getServiceSwitch() {
        return serviceSwitch;
    }
    @JSONField(name = "service_switch")
    public void setServiceSwitch(int serviceSwitch) {
        this.serviceSwitch = serviceSwitch;
    }
    @JSONField(name = "service_url")
    public String getServiceUrl() {
        return serviceUrl;
    }
    @JSONField(name = "service_url")
    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    @JSONField(name = "apk_ver")
    public String getVersion() {
        return version;
    }

    @JSONField(name = "apk_ver")
    public void setVersion(String version) {
        this.version = version;
    }

    @JSONField(name = "apk_url")
    public String getDownloadApkUrl() {
        return downloadApkUrl;
    }

    @JSONField(name = "apk_url")
    public void setDownloadApkUrl(String downloadApkUrl) {
        this.downloadApkUrl = downloadApkUrl;
    }

    @JSONField(name = "apk_des")
    public String getUpdateDes() {
        return updateDes;
    }

    @JSONField(name = "apk_des")
    public void setUpdateDes(String updateDes) {
        this.updateDes = updateDes;
    }

    @JSONField(name = "wx_siteurl")
    public void setLiveWxShareUrl(String liveWxShareUrl) {
        this.liveWxShareUrl = liveWxShareUrl;
    }

    @JSONField(name = "wx_siteurl")
    public String getLiveWxShareUrl() {
        return liveWxShareUrl;
    }

    @JSONField(name = "share_title")
    public String getLiveShareTitle() {
        return liveShareTitle;
    }

    @JSONField(name = "share_title")
    public void setLiveShareTitle(String liveShareTitle) {
        this.liveShareTitle = liveShareTitle;
    }

    @JSONField(name = "share_des")
    public String getLiveShareDes() {
        return liveShareDes;
    }

    @JSONField(name = "share_des")
    public void setLiveShareDes(String liveShareDes) {
        this.liveShareDes = liveShareDes;
    }

    @JSONField(name = "agent_share_title")
    public String getAgentShareTitle() {
        return agentShareTitle;
    }

    @JSONField(name = "agent_share_title")
    public void setAgentShareTitle(String agentShareTitle) {
        this.agentShareTitle = agentShareTitle;
    }

    @JSONField(name = "agent_share_des")
    public String getAgentShareDes() {
        return agentShareDes;
    }

    @JSONField(name = "agent_share_des")
    public void setAgentShareDes(String agentShareDes) {
        this.agentShareDes = agentShareDes;
    }


    @JSONField(name = "name_coin")
    public String getCoinName() {
        return coinName;
    }

    @JSONField(name = "name_coin")
    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    @JSONField(name = "name_votes")
    public String getVotesName() {
        return votesName;
    }

    @JSONField(name = "name_votes")
    public void setVotesName(String votesName) {
        this.votesName = votesName;
    }

    @JSONField(name = "app_name")
    public String getAppName() {
        return mAppName;
    }

    @JSONField(name = "app_name")
    public void setAppName(String appName) {
        mAppName = appName;
    }

    @JSONField(name = "live_time_coin")
    public String[] getLiveTimeCoin() {
        return liveTimeCoin;
    }

    @JSONField(name = "live_time_coin")
    public void setLiveTimeCoin(String[] liveTimeCoin) {
        this.liveTimeCoin = liveTimeCoin;
    }

    @JSONField(name = "login_type")
    public String[] getLoginType() {
        return loginType;
    }

    @JSONField(name = "login_type")
    public void setLoginType(String[] loginType) {
        this.loginType = loginType;
    }

    @JSONField(name = "live_type")
    public String[][] getLiveType() {
        return liveType;
    }

    @JSONField(name = "live_type")
    public void setLiveType(String[][] liveType) {
        this.liveType = liveType;
    }

    @JSONField(name = "share_type")
    public String[] getShareType() {
        return shareType;
    }

    @JSONField(name = "share_type")
    public void setShareType(String[] shareType) {
        this.shareType = shareType;
    }


    @JSONField(name = "maintain_switch")
    public int getMaintainSwitch() {
        return maintainSwitch;
    }

    @JSONField(name = "maintain_switch")
    public void setMaintainSwitch(int maintainSwitch) {
        this.maintainSwitch = maintainSwitch;
    }

    @JSONField(name = "maintain_tips")
    public String getMaintainTips() {
        return maintainTips;
    }

    @JSONField(name = "maintain_tips")
    public void setMaintainTips(String maintainTips) {
        this.maintainTips = maintainTips;
    }

    @JSONField(name = "sprout_key")
    public String getBeautyKey() {
        return beautyKey;
    }

    @JSONField(name = "sprout_key")
    public void setBeautyKey(String beautyKey) {
        this.beautyKey = beautyKey;
    }

    @JSONField(name = "sprout_white")
    public int getBeautyMeiBai() {
        return beautyMeiBai;
    }

    @JSONField(name = "sprout_white")
    public void setBeautyMeiBai(int beautyMeiBai) {
        this.beautyMeiBai = beautyMeiBai;
    }

    @JSONField(name = "sprout_skin")
    public int getBeautyMoPi() {
        return beautyMoPi;
    }

    @JSONField(name = "sprout_skin")
    public void setBeautyMoPi(int beautyMoPi) {
        this.beautyMoPi = beautyMoPi;
    }

    @JSONField(name = "sprout_saturated")
    public int getBeautyBaoHe() {
        return beautyBaoHe;
    }

    @JSONField(name = "sprout_saturated")
    public void setBeautyBaoHe(int beautyBaoHe) {
        this.beautyBaoHe = beautyBaoHe;
    }

    @JSONField(name = "sprout_pink")
    public int getBeautyFenNen() {
        return beautyFenNen;
    }

    @JSONField(name = "sprout_pink")
    public void setBeautyFenNen(int beautyFenNen) {
        this.beautyFenNen = beautyFenNen;
    }

    @JSONField(name = "sprout_eye")
    public int getBeautyBigEye() {
        return beautyBigEye;
    }

    @JSONField(name = "sprout_eye")
    public void setBeautyBigEye(int beautyBigEye) {
        this.beautyBigEye = beautyBigEye;
    }

    @JSONField(name = "sprout_face")
    public int getBeautyFace() {
        return beautyFace;
    }

    @JSONField(name = "sprout_face")
    public void setBeautyFace(int beautyFace) {
        this.beautyFace = beautyFace;
    }


    public String[] getVideoShareTypes() {
        return shareType;
    }

    @JSONField(name = "video_share_title")
    public String getVideoShareTitle() {
        return videoShareTitle;
    }

    @JSONField(name = "video_share_title")
    public void setVideoShareTitle(String videoShareTitle) {
        this.videoShareTitle = videoShareTitle;
    }

    @JSONField(name = "video_share_des")
    public String getVideoShareDes() {
        return videoShareDes;
    }

    @JSONField(name = "video_share_des")
    public void setVideoShareDes(String videoShareDes) {
        this.videoShareDes = videoShareDes;
    }

    @JSONField(name = "video_audit_switch")
    public int getVideoAuditSwitch() {
        return videoAuditSwitch;
    }

    @JSONField(name = "video_audit_switch")
    public void setVideoAuditSwitch(int videoAuditSwitch) {
        this.videoAuditSwitch = videoAuditSwitch;
    }


    @JSONField(name = "private_letter_switch")
    public int getChatLimitSwitch() {
        return mChatLimitSwitch;
    }

    @JSONField(name = "private_letter_switch")
    public void setChatLimitSwitch(int chatLimitSwitch) {
        mChatLimitSwitch = chatLimitSwitch;
    }

    @JSONField(name = "private_letter_nums")
    public int getChatLimitNum() {
        return mChatLimitNum;
    }

    @JSONField(name = "private_letter_nums")
    public void setChatLimitNum(int chatLimitNum) {
        mChatLimitNum = chatLimitNum;
    }

    @JSONField(name = "guide")
    public String getAdInfo() {
        return mAdInfo;
    }

    @JSONField(name = "guide")
    public void setAdInfo(String adInfo) {
        mAdInfo = adInfo;
    }

    @JSONField(name = "watermark")
    public String getWaterMarkUrl() {
        return mWaterMarkUrl;
    }

    @JSONField(name = "watermark")
    public void setWaterMarkUrl(String waterMarkUrl) {
        mWaterMarkUrl = waterMarkUrl;
    }


    @JSONField(name = "liveclass")
    public List<LiveClassBean> getLiveClass() {
        return liveClass;
    }

    @JSONField(name = "liveclass")
    public void setLiveClass(List<LiveClassBean> liveClass) {
        this.liveClass = liveClass;
    }


    @JSONField(name = "shop_system_name")
    public String getShopSystemName() {
        return mShopSystemName;
    }
    @JSONField(name = "shop_system_name")
    public void setShopSystemName(String shopSystemName) {
        mShopSystemName = shopSystemName;
    }

}
