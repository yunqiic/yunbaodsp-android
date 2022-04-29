package com.yunbao.video.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.annotation.JSONField;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class ClassBean implements Parcelable {

    public static final int RECOMMEND_ID = -1;
    public static final int HOT_ID = -2;
    public static final int FOLLOW_ID = -3;
    private int mId;
    private String mName;
    private String mDes;
    private String mThumb;

    public ClassBean() {
    }

    public ClassBean(int id, String name) {
        mId = id;
        mName = name;
    }

    @JSONField(name = "id")
    public int getId() {
        return mId;
    }

    @JSONField(name = "id")
    public void setId(int id) {
        mId = id;
    }

    @JSONField(name = "title")
    public String getName() {
        return mName;
    }

    @JSONField(name = "title")
    public void setName(String name) {
        mName = name;
    }

    @JSONField(name = "des")
    public String getDes() {
        return mDes;
    }

    @JSONField(name = "des")
    public void setDes(String des) {
        mDes = des;
    }

    @JSONField(name = "thumb")
    public String getThumb() {
        return mThumb;
    }

    @JSONField(name = "thumb")
    public void setThumb(String thumb) {
        mThumb = thumb;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeString(mDes);
        dest.writeString(mThumb);
    }

    public ClassBean(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mDes = in.readString();
        mThumb = in.readString();
    }

    public static final Creator<ClassBean> CREATOR = new Creator<ClassBean>() {
        @Override
        public ClassBean createFromParcel(Parcel in) {
            return new ClassBean(in);
        }

        @Override
        public ClassBean[] newArray(int size) {
            return new ClassBean[size];
        }
    };
}
