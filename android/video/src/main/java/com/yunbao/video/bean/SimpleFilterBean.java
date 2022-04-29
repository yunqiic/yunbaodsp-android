package com.yunbao.video.bean;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class SimpleFilterBean {

    private int mImgSrc;
    private int mFilterSrc;
    private boolean mChecked;
    private int mKsyFilterType;//金山自带滤镜类型

    public SimpleFilterBean() {
    }


    public SimpleFilterBean(int imgSrc, int filterSrc) {
        mImgSrc = imgSrc;
        mFilterSrc = filterSrc;
    }


    public SimpleFilterBean(int imgSrc, int filterSrc, int ksyFilterType) {
        this(imgSrc, filterSrc);
        mKsyFilterType = ksyFilterType;
    }

    public SimpleFilterBean(int imgSrc, int filterSrc, int ksyFilterType, boolean checked) {
        this(imgSrc, filterSrc, ksyFilterType);
        mChecked = checked;
    }

    public int getImgSrc() {
        return mImgSrc;
    }

    public void setImgSrc(int imgSrc) {
        this.mImgSrc = imgSrc;
    }

    public int getFilterSrc() {
        return mFilterSrc;
    }

    public void setFilterSrc(int filterSrc) {
        mFilterSrc = filterSrc;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }

    public int getKsyFilterType() {
        return mKsyFilterType;
    }

    public void setKsyFilterType(int ksyFilterType) {
        mKsyFilterType = ksyFilterType;
    }
}
