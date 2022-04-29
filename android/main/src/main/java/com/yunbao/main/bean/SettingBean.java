package com.yunbao.main.bean;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class SettingBean {
    private int id;
    private String name;
    private boolean mChatMusicClose;

    public SettingBean() {
    }

    public SettingBean(int id, String name) {
        this.id = id;
        this.name = name;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChatMusicClose() {
        return mChatMusicClose;
    }

    public void setChatMusicClose(boolean chatMusicClose) {
        mChatMusicClose = chatMusicClose;
    }
}
