package com.yunbao.common.bean;

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
public class CoinBean implements Parcelable{

    private String id;
    private String coin;
    private String money;
    private String give;
    private boolean checked;
    private String mCoinPaypal;

    public CoinBean(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getGive() {
        return give;
    }

    public void setGive(String give) {
        this.give = give;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @JSONField(name = "coin_paypal")
    public String getCoinPaypal() {
        return mCoinPaypal;
    }
    @JSONField(name = "coin_paypal")
    public void setCoinPaypal(String coinPaypal) {
        mCoinPaypal = coinPaypal;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public CoinBean(Parcel in) {
        id = in.readString();
        coin = in.readString();
        money = in.readString();
        give = in.readString();
        checked = in.readByte() != 0;
        mCoinPaypal = in.readString();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(coin);
        dest.writeString(money);
        dest.writeString(give);
        dest.writeByte((byte) (checked ? 1 : 0));
        dest.writeString(mCoinPaypal);
    }

    public static final Creator<CoinBean> CREATOR = new Creator<CoinBean>() {
        @Override
        public CoinBean createFromParcel(Parcel in) {
            return new CoinBean(in);
        }

        @Override
        public CoinBean[] newArray(int size) {
            return new CoinBean[size];
        }
    };
}
