package com.yunbao.video.custom;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class AtSpan {
    private String uid;
    private String name;
    private String content;
    private int startIndex;
    private int endIndex;

    public AtSpan() {
    }

    public AtSpan(String uid, String name, String content, int startIndex) {
        this.uid = uid;
        this.name = name;
        this.content = content;
        this.startIndex = startIndex;
        this.endIndex = startIndex + content.length();
    }

    public void moveIndex(int count) {
        this.startIndex += count;
        this.endIndex += count;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public boolean validate() {
        return (endIndex > startIndex) && (endIndex - startIndex == this.content.length());
    }

    public void setName(String content) {
        this.name = content;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
