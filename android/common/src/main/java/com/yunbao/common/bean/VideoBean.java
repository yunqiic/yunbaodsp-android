package com.yunbao.common.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.alibaba.fastjson.annotation.JSONField;
import com.yunbao.common.utils.L;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class VideoBean implements Parcelable {
    public static final int NOT_CAN_PLAY = 0;
    public static final int CAN_PLAY = 1;
    private static final String KEY = "HmTPvkJ3otK5gp.COdrAi:q09Z62ash-QGn8V;FNIlbfM/D74Wj&S_E=UzYuw?1ecxXyLRB";
    private static StringBuilder sStringBuilder;
    private String id;
    private String uid;
    private String title;
    private String thumb;
    private String thumbs;
    private String href;
    private String hrefW;
    private String likeNum;
    private String viewNum;
    private String commentNum;
    private String stepNum;
    private String shareNum;
    private String addtime;
    private String lat;
    private String lng;
    private String city;
    private UserBean userBean;
    private String datetime;
    private String distance;
    private int step;//是否踩过
    private int like;//是否赞过
    private int attent;//是否关注过作者
    private int status;
    private int musicId;
    private int isAd;
    private String adUrl;
    private int mIsgoods;//是否是商品
    private int mLabelId;
    private String mLabelName;
    private MusicBean musicInfo;
    private String mGoodsId;
    private String mMoney;
    private String mPayTime;
    private String mRealHref;
    private String mRealHrefW;
    private int ispay;
    private String coin;
    private LiveBean liveinfo;
    private int mCanPlay;//1可以播放
    private int isUserad;
    private String useradUrl;


    public VideoBean() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    @JSONField(name = "thumb_s")
    public String getThumbs() {
        return this.thumb;
        //return thumbs;
    }

    @JSONField(name = "thumb_s")
    public void setThumbs(String thumbs) {
        this.thumbs = thumbs;
    }

    public String getHref() {
        if (!TextUtils.isEmpty(mRealHref)) {
            return mRealHref;
        }
        mRealHref = decryptUrl(this.href);
        if (!TextUtils.isEmpty(mRealHref)) {
            return mRealHref;
        }
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @JSONField(name = "href_w")
    public String getHrefW() {
        if (!TextUtils.isEmpty(mRealHrefW)) {
            return mRealHrefW;
        }
        mRealHrefW = decryptUrl(this.hrefW);
        if (!TextUtils.isEmpty(mRealHrefW)) {
            return mRealHrefW;
        }
        return hrefW;
    }

    @JSONField(name = "href_w")
    public void setHrefW(String hrefW) {
        this.hrefW = hrefW;
    }

    @JSONField(name = "likes")
    public String getLikeNum() {
        return likeNum;
    }

    @JSONField(name = "likes")
    public void setLikeNum(String likeNum) {
        this.likeNum = likeNum;
    }

    @JSONField(name = "views")
    public String getViewNum() {
        return viewNum;
    }

    @JSONField(name = "views")
    public void setViewNum(String viewNum) {
        this.viewNum = viewNum;
    }

    @JSONField(name = "comments")
    public String getCommentNum() {
        return commentNum;
    }

    @JSONField(name = "comments")
    public void setCommentNum(String commentNum) {
        this.commentNum = commentNum;
    }

    @JSONField(name = "steps")
    public String getStepNum() {
        return stepNum;
    }

    @JSONField(name = "steps")
    public void setStepNum(String stepNum) {
        this.stepNum = stepNum;
    }

    @JSONField(name = "shares")
    public String getShareNum() {
        return shareNum;
    }

    @JSONField(name = "shares")
    public void setShareNum(String shareNum) {
        this.shareNum = shareNum;
    }

    public String getAddtime() {
        return addtime;
    }

    public void setAddtime(String addtime) {
        this.addtime = addtime;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }


    @JSONField(name = "userinfo")
    public UserBean getUserBean() {
        return userBean;
    }

    @JSONField(name = "userinfo")
    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    @JSONField(name = "isstep")
    public int getStep() {
        return step;
    }

    @JSONField(name = "isstep")
    public void setStep(int step) {
        this.step = step;
    }

    @JSONField(name = "islike")
    public int getLike() {
        return like;
    }

    @JSONField(name = "islike")
    public void setLike(int like) {
        this.like = like;
    }

    @JSONField(name = "isattent")
    public int getAttent() {
        return attent;
    }

    @JSONField(name = "isattent")
    public void setAttent(int attent) {
        this.attent = attent;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @JSONField(name = "music_id")
    public int getMusicId() {
        return musicId;
    }

    @JSONField(name = "music_id")
    public void setMusicId(int musicId) {
        this.musicId = musicId;
    }

    @JSONField(name = "is_ad")
    public int getIsAd() {
        return isAd;
    }

    @JSONField(name = "is_ad")
    public void setIsAd(int isAd) {
        this.isAd = isAd;
    }

    @JSONField(name = "is_userad")
    public int getIsUserad() {
        return isUserad;
    }

    @JSONField(name = "is_userad")
    public void setIsUserad(int isUserad) {
        this.isUserad =isUserad;
    }

    @JSONField(name = "ad_url")
    public String getAdUrl() {
        return adUrl;
    }

    @JSONField(name = "ad_url")
    public void setAdUrl(String adUrl) {
        this.adUrl = adUrl;
    }

    @JSONField(name = "userad_url")
    public String getUseradUrl() {
        return useradUrl;
    }

    @JSONField(name = "userad_url")
    public void setUseradUrl(String useradUrl) {
        this.useradUrl = useradUrl;
    }


    @JSONField(name = "isgoods")
    public int getIsgoods() {
        return mIsgoods;
    }

    @JSONField(name = "isgoods")
    public void setIsgoods(int isgoods) {
        mIsgoods = isgoods;
    }

    @JSONField(name = "labelid")
    public int getLabelId() {
        return mLabelId;
    }

    @JSONField(name = "labelid")
    public void setLabelId(int labelId) {
        mLabelId = labelId;
    }

    @JSONField(name = "label_name")
    public String getLabelName() {
        return mLabelName;
    }

    @JSONField(name = "label_name")
    public void setLabelName(String labelName) {
        mLabelName = labelName;
    }

    @JSONField(name = "musicinfo")
    public MusicBean getMusicInfo() {
        return musicInfo;
    }

    @JSONField(name = "musicinfo")
    public void setMusicInfo(MusicBean musicInfo) {
        this.musicInfo = musicInfo;
    }

    @JSONField(name = "money")
    public String getMoney() {
        return mMoney;
    }

    @JSONField(name = "money")
    public void setMoney(String money) {
        mMoney = money;
    }

    @JSONField(name = "paytime")
    public String getPayTime() {
        return mPayTime;
    }

    @JSONField(name = "paytime")
    public void setPayTime(String payTime) {
        mPayTime = payTime;
    }


    public LiveBean getLiveinfo() {
        return liveinfo;
    }

    public void setLiveinfo(LiveBean liveinfo) {
        this.liveinfo = liveinfo;
    }

    public int getIspay() {
        return ispay;
    }

    public void setIspay(int ispay) {
        this.ispay = ispay;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public int getCanPlay() {
        return mCanPlay;
    }

    public void setCanPlay(int canPlay) {
        mCanPlay = canPlay;
    }

    @JSONField(name = "goodsid")
    public String getGoodsId() {
        return mGoodsId;
    }

    @JSONField(name = "goodsid")
    public void setGoodsId(String goodsId) {
        mGoodsId = goodsId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.uid);
        dest.writeString(this.title);
        dest.writeString(this.thumb);
        dest.writeString(this.thumbs);
        dest.writeString(this.href);
        dest.writeString(this.hrefW);
        dest.writeString(this.likeNum);
        dest.writeString(this.viewNum);
        dest.writeString(this.commentNum);
        dest.writeString(this.stepNum);
        dest.writeString(this.shareNum);
        dest.writeString(this.addtime);
        dest.writeString(this.lat);
        dest.writeString(this.lng);
        dest.writeString(this.city);
        dest.writeString(this.datetime);
        dest.writeInt(this.like);
        dest.writeInt(this.attent);
        dest.writeString(this.distance);
        dest.writeInt(this.step);
        dest.writeParcelable(this.userBean, flags);
        dest.writeInt(this.status);
        dest.writeInt(this.musicId);
        dest.writeInt(this.isAd);
        dest.writeString(this.adUrl);
        dest.writeInt(this.mIsgoods);
        dest.writeInt(this.mLabelId);
        dest.writeString(this.mLabelName);
        dest.writeParcelable(this.musicInfo, flags);
        dest.writeString(this.mMoney);
        dest.writeString(this.mPayTime);
        dest.writeString(this.mRealHref);
        dest.writeString(this.mRealHrefW);
        dest.writeInt(this.ispay);
        dest.writeString(this.coin);
        dest.writeParcelable(this.liveinfo, flags);
        dest.writeInt(this.mCanPlay);
        dest.writeString(this.mGoodsId);
    }


    public VideoBean(Parcel in) {
        this.id = in.readString();
        this.uid = in.readString();
        this.title = in.readString();
        this.thumb = in.readString();
        this.thumbs = in.readString();
        this.href = in.readString();
        this.hrefW = in.readString();
        this.likeNum = in.readString();
        this.viewNum = in.readString();
        this.commentNum = in.readString();
        this.stepNum = in.readString();
        this.shareNum = in.readString();
        this.addtime = in.readString();
        this.lat = in.readString();
        this.lng = in.readString();
        this.city = in.readString();
        this.datetime = in.readString();
        this.like = in.readInt();
        this.attent = in.readInt();
        this.distance = in.readString();
        this.step = in.readInt();
        this.userBean = in.readParcelable(UserBean.class.getClassLoader());
        this.status = in.readInt();
        this.musicId = in.readInt();
        this.isAd = in.readInt();
        this.adUrl = in.readString();
        this.mIsgoods = in.readInt();
        this.mLabelId = in.readInt();
        this.mLabelName = in.readString();
        this.musicInfo = in.readParcelable(MusicBean.class.getClassLoader());
        this.mMoney = in.readString();
        this.mPayTime = in.readString();
        this.mRealHref = in.readString();
        this.mRealHrefW = in.readString();
        this.ispay = in.readInt();
        this.coin = in.readString();
        this.liveinfo = in.readParcelable(LiveBean.class.getClassLoader());
        this.mCanPlay = in.readInt();
        this.mGoodsId = in.readString();
    }


    public static final Creator<VideoBean> CREATOR = new Creator<VideoBean>() {
        @Override
        public VideoBean[] newArray(int size) {
            return new VideoBean[size];
        }

        @Override
        public VideoBean createFromParcel(Parcel in) {
            return new VideoBean(in);
        }
    };

    @Override
    public String toString() {
        return "VideoBean{" +
                "title='" + title + '\'' +
                ",href='" + mRealHref + '\'' +
                ",id='" + id + '\'' +
                ",uid='" + uid + '\'' +
                ",userNiceName='" + userBean.getUserNiceName() + '\'' +
                ",thumb='" + thumb + '\'' +
                '}';
    }


    public String getTag() {
        return "VideoBean" + this.getId() + this.hashCode();
    }

    /**
     * 解密url
     */
    private String decryptUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        if (sStringBuilder == null) {
            sStringBuilder = new StringBuilder();
        }
        sStringBuilder.delete(0, sStringBuilder.length());
        for (int i = 0, len1 = url.length(); i < len1; i++) {
            for (int j = 0, len2 = KEY.length(); j < len2; j++) {
                if (url.charAt(i) == KEY.charAt(j)) {
                    if (j - 1 < 0) {
                        sStringBuilder.append(KEY.charAt(len2 - 1));
                    } else {
                        sStringBuilder.append(KEY.charAt(j - 1));
                    }
                }
            }
        }
        return sStringBuilder.toString();
    }


}
