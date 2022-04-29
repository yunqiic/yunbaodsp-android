package com.yunbao.common.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.alibaba.fastjson.annotation.JSONField;
import com.yunbao.common.R;
import com.yunbao.common.utils.WordUtil;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class UserBean implements Parcelable {

    protected String id;
    protected String userNiceName;
    protected String avatar;
    protected String avatarThumb;
    protected String bgImg;
    protected int sex;
    protected String signature;
    protected String province;
    protected String city;
    protected String district;
    protected String birthday;
    protected int follows;
    protected int fans;
    protected int praise;
    protected int workVideos;
    protected int likeVideos;
    protected int isattention;
    protected String age;
    protected int mIsShop;
    protected String mShopName;
    protected String mShopThumb;

    protected int level;
    protected int levelAnchor;
    protected String coin;
    protected Vip vipinfo;
//    protected Liang liang;
//    protected Car car;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JSONField(name = "user_nicename")
    public String getUserNiceName() {
        return userNiceName;
    }

    @JSONField(name = "user_nicename")
    public void setUserNiceName(String userNiceName) {
        this.userNiceName = userNiceName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @JSONField(name = "avatar_thumb")
    public String getAvatarThumb() {
        return avatarThumb;
    }

    @JSONField(name = "avatar_thumb")
    public void setAvatarThumb(String avatarThumb) {
        this.avatarThumb = avatarThumb;
    }

    @JSONField(name = "bg_img")
    public String getBgImg() {
        return bgImg;
    }

    @JSONField(name = "bg_img")
    public void setBgImg(String bgImg) {
        this.bgImg = bgImg;
    }


    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @JSONField(name = "area")
    public String getDistrict() {
        return district;
    }

    @JSONField(name = "area")
    public void setDistrict(String district) {
        this.district = district;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }


    public int getFollows() {
        return follows;
    }

    public void setFollows(int follows) {
        this.follows = follows;
    }

    public int getPraise() {
        return praise;
    }

    public void setPraise(int praise) {
        this.praise = praise;
    }

    public int getFans() {
        return fans;
    }

    public void setFans(int fans) {
        this.fans = fans;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public int getWorkVideos() {
        return workVideos;
    }

    public void setWorkVideos(int workVideos) {
        this.workVideos = workVideos;
    }

    public int getLikeVideos() {
        return likeVideos;
    }

    public void setLikeVideos(int likeVideos) {
        this.likeVideos = likeVideos;
    }

    public int getIsattention() {
        return isattention;
    }

    public void setIsattention(int isattention) {
        this.isattention = isattention;
    }

    @JSONField(name = "isshop")
    public int getIsShop() {
        return mIsShop;
    }

    @JSONField(name = "isshop")
    public void setIsShop(int isShop) {
        mIsShop = isShop;
    }

    @JSONField(name = "shop_name")
    public String getShopName() {
        return mShopName;
    }

    @JSONField(name = "shop_name")
    public void setShopName(String shopName) {
        mShopName = shopName;
    }

    @JSONField(name = "shop_thumb")
    public String getShopThumb() {
        return mShopThumb;
    }

    @JSONField(name = "shop_thumb")
    public void setShopThumb(String shopThumb) {
        mShopThumb = shopThumb;
    }


    public int getLevel() {
        if (level == 0) {
            level = 1;
        }
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @JSONField(name = "level_anchor")
    public int getLevelAnchor() {
        return levelAnchor;
    }

    @JSONField(name = "level_anchor")
    public void setLevelAnchor(int levelAnchor) {
        this.levelAnchor = levelAnchor;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    @JSONField(name = "vipinfo")
    public Vip getVipInfo() {
        return vipinfo;
    }
    @JSONField(name = "vipinfo")
    public void setVipInfo(Vip vipinfo) {
        this.vipinfo = vipinfo;
    }

//    public Liang getLiang() {
//        return liang;
//    }
//
//    public void setLiang(Liang liang) {
//        this.liang = liang;
//    }
//
//    public Car getCar() {
//        return car;
//    }
//
//    public void setCar(Car car) {
//        this.car = car;
//    }

    /**
     * 显示靓号
     */
    public String getLiangNameTip() {
//        if (this.liang != null) {
//            String liangName = this.liang.getName();
//            if (!TextUtils.isEmpty(liangName) && !"0".equals(liangName)) {
//                return WordUtil.getString(R.string.live_liang) + ":" + liangName;
//            }
//        }
        return "ID:" + this.id;
    }

    /**
     * 获取靓号
     */
    public String getGoodName() {
//        if (this.liang != null) {
//            return this.liang.getName();
//        }
        return "0";
    }

    public boolean isVip() {
        if (this.vipinfo != null) {
            return this.vipinfo.isvip==1;
        }
        return false;
    }

    public UserBean() {
    }

    protected UserBean(Parcel in) {
        this.id = in.readString();
        this.userNiceName = in.readString();
        this.avatar = in.readString();
        this.avatarThumb = in.readString();
        this.sex = in.readInt();
        this.signature = in.readString();
        this.province = in.readString();
        this.city = in.readString();
        this.district = in.readString();
        this.birthday = in.readString();
        this.follows = in.readInt();
        this.fans = in.readInt();
        this.praise = in.readInt();
        this.age = in.readString();
        this.workVideos = in.readInt();
        this.likeVideos = in.readInt();
        this.isattention = in.readInt();
        this.mIsShop = in.readInt();
        this.mShopName = in.readString();
        this.mShopThumb = in.readString();
        this.level = in.readInt();
        this.levelAnchor = in.readInt();
        this.coin = in.readString();
        this.vipinfo = in.readParcelable(Vip.class.getClassLoader());
//        this.liang = in.readParcelable(Liang.class.getClassLoader());
//        this.car = in.readParcelable(Car.class.getClassLoader());
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.userNiceName);
        dest.writeString(this.avatar);
        dest.writeString(this.avatarThumb);
        dest.writeInt(this.sex);
        dest.writeString(this.signature);
        dest.writeString(this.province);
        dest.writeString(this.city);
        dest.writeString(this.district);
        dest.writeString(this.birthday);
        dest.writeInt(this.follows);
        dest.writeInt(this.fans);
        dest.writeInt(this.praise);
        dest.writeString(this.age);
        dest.writeInt(this.workVideos);
        dest.writeInt(this.likeVideos);
        dest.writeInt(this.isattention);
        dest.writeInt(this.mIsShop);
        dest.writeString(this.mShopName);
        dest.writeString(this.mShopThumb);
        dest.writeInt(this.level);
        dest.writeInt(this.levelAnchor);
        dest.writeString(this.coin);
        dest.writeParcelable(this.vipinfo, flags);
//        dest.writeParcelable(this.liang, flags);
//        dest.writeParcelable(this.car, flags);
    }

    public static final Creator<UserBean> CREATOR = new Creator<UserBean>() {
        @Override
        public UserBean[] newArray(int size) {
            return new UserBean[size];
        }

        @Override
        public UserBean createFromParcel(Parcel in) {
            return new UserBean(in);
        }
    };

    public static class Vip implements Parcelable {
        protected int isvip;
        protected String vip_endtime;
        protected int vip_switch;

        public Vip() {
        }

        protected Vip(Parcel in) {
            isvip = in.readInt();
            vip_endtime = in.readString();
            vip_switch = in.readInt();
        }

        public int getIsvip() {
            return isvip;
        }

        public void setIsvip(int isvip) {
            this.isvip = isvip;
        }

        public String getVip_endtime() {
            return vip_endtime;
        }

        public void setVip_endtime(String vip_endtime) {
            this.vip_endtime = vip_endtime;
        }

        public int getVip_switch() {
            return vip_switch;
        }

        public void setVip_switch(int vip_switch) {
            this.vip_switch = vip_switch;
        }

        public static final Creator<Vip> CREATOR = new Creator<Vip>() {
            @Override
            public Vip createFromParcel(Parcel in) {
                return new Vip(in);
            }

            @Override
            public Vip[] newArray(int size) {
                return new Vip[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(isvip);
            parcel.writeString(vip_endtime);
            parcel.writeInt(vip_switch);
        }
    }

    public static class Liang implements Parcelable {
        protected String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Liang() {

        }

        public Liang(Parcel in) {
            this.name = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.name);
        }

        public static final Creator<Liang> CREATOR = new Creator<Liang>() {

            @Override
            public Liang createFromParcel(Parcel in) {
                return new Liang(in);
            }

            @Override
            public Liang[] newArray(int size) {
                return new Liang[size];
            }
        };

    }

    public static class Car implements Parcelable {
        protected int id;
        protected String swf;
        protected float swftime;
        protected String words;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getSwf() {
            return swf;
        }

        public void setSwf(String swf) {
            this.swf = swf;
        }

        public float getSwftime() {
            return swftime;
        }

        public void setSwftime(float swftime) {
            this.swftime = swftime;
        }

        public String getWords() {
            return words;
        }

        public void setWords(String words) {
            this.words = words;
        }

        public Car() {

        }

        public Car(Parcel in) {
            this.id = in.readInt();
            this.swf = in.readString();
            this.swftime = in.readFloat();
            this.words = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeString(this.swf);
            dest.writeFloat(this.swftime);
            dest.writeString(this.words);
        }


        public static final Creator<Car> CREATOR = new Creator<Car>() {
            @Override
            public Car[] newArray(int size) {
                return new Car[size];
            }

            @Override
            public Car createFromParcel(Parcel in) {
                return new Car(in);
            }
        };

    }
}
