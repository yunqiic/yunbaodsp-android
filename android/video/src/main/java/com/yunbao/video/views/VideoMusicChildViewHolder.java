package com.yunbao.video.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.yunbao.common.custom.CommonRefreshView;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.video.R;
import com.yunbao.video.adapter.MusicAdapter;
import com.yunbao.video.interfaces.VideoMusicActionListener;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public abstract class VideoMusicChildViewHolder extends AbsViewHolder {

    protected CommonRefreshView mRefreshView;
    protected MusicAdapter mAdapter;
    protected VideoMusicActionListener mActionListener;


    public VideoMusicChildViewHolder(Context context, ViewGroup parentView, VideoMusicActionListener actionListener) {
        super(context, parentView, actionListener);
    }

    @Override
    protected void processArguments(Object... args) {
        mActionListener = (VideoMusicActionListener) args[0];
    }

    @Override
    public void init() {
        mRefreshView = (CommonRefreshView) findViewById(R.id.refreshView);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
    }


    public void loadData() {
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    public void show() {
        if (mContentView != null && mContentView.getVisibility() != View.VISIBLE) {
            mContentView.setVisibility(View.VISIBLE);
        }
    }

    public void hide() {
        if (mContentView != null && mContentView.getVisibility() == View.VISIBLE) {
            mContentView.setVisibility(View.INVISIBLE);
        }
    }


    public void release() {
        mActionListener = null;
        if (mAdapter != null) {
            mAdapter.setActionListener(null);
        }
    }

    public void collectChanged(MusicAdapter adapter, String musicId, int collect) {
        if (mAdapter != null) {
            mAdapter.collectChanged(adapter, musicId, collect);
        }
    }

    public void collapse() {
        if (mAdapter != null) {
            mAdapter.collapse();
        }
    }
}
