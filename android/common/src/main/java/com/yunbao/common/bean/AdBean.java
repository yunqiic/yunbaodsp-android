package com.yunbao.common.bean;

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

public class AdBean {
    private String mUrl;
    private String mLink;

    @JSONField(name = "thumb")
    public String getUrl() {
        return mUrl;
    }
    @JSONField(name = "thumb")
    public void setUrl(String url) {
        mUrl = url;
    }
    @JSONField(name = "href")
    public String getLink() {
        return mLink;
    }
    @JSONField(name = "href")
    public void setLink(String link) {
        mLink = link;
    }
}
