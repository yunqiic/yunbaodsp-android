package com.yunbao.main.views;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.custom.CommonRefreshView;
import com.yunbao.common.custom.ItemDecoration;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.main.R;
import com.yunbao.main.adapter.MainHomeHotAdapter;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;
import com.yunbao.video.activity.VideoPlayActivity;
import com.yunbao.common.bean.VideoBean;
import com.yunbao.video.event.VideoDeleteEvent;
import com.yunbao.video.event.VideoInsertListEvent;
import com.yunbao.video.event.VideoScrollPageEvent;
import com.yunbao.video.http.VideoHttpConsts;
import com.yunbao.video.http.VideoHttpUtil;
import com.yunbao.video.interfaces.VideoScrollDataHelper;
import com.yunbao.video.utils.VideoStorge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
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
public class MainHomeClassViewHolder extends AbsVideoMainViewHolder implements OnItemClickListener<VideoBean> {

    private CommonRefreshView mRefreshView;
    private MainHomeHotAdapter mAdapter;
    private VideoScrollDataHelper mVideoScrollDataHelper;
    private int mClassId;
    private String mKey;

    public MainHomeClassViewHolder(Context context, ViewGroup parentView, int classId) {
        super(context, parentView, classId);
    }

    @Override
    protected void processArguments(Object... args) {
        super.processArguments(args);
        mClassId = (Integer) args[0];
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_home_hot;
    }

    @Override
    public void init() {
        mKey = Constants.VIDEO_HOME_CLASS + mClassId;
        mRefreshView = (CommonRefreshView) findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_empty_video_class);
        mRefreshView.setLayoutManager(new GridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false));
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 2, 0);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRefreshView.setItemDecoration(decoration);
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<VideoBean>() {
            @Override
            public RefreshAdapter<VideoBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new MainHomeHotAdapter(mContext);
                    mAdapter.setOnItemClickListener(MainHomeClassViewHolder.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                VideoHttpUtil.getVideoListByClass(mClassId, p, callback);
            }

            @Override
            public List<VideoBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), VideoBean.class);
            }

            @Override
            public void onRefreshSuccess(List<VideoBean> list, int listCount) {
                VideoStorge.getInstance().put(mKey, list);
            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<VideoBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoInsertListEvent(VideoInsertListEvent e) {
        if (mKey.equals(e.getKey()) && mAdapter != null) {
            mAdapter.notifyItemRangeInserted(e.getStartPosition(), e.getInsertCount());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoScrollPageEvent(VideoScrollPageEvent e) {
        if (mKey.equals(e.getKey()) && mRefreshView != null) {
            mRefreshView.setPageCount(e.getPage());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoDelete(VideoDeleteEvent e) {
        if (mKey.equals(e.getVideoKey()) && mAdapter != null) {
            mAdapter.deleteVideo(e.getVideoId());
            if (mAdapter.getItemCount() == 0 && mRefreshView != null) {
                mRefreshView.showEmpty();
            }
        }
    }

    @Override
    public void loadData() {
        if (!isFirstLoadData()) {
            return;
        }
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    @Override
    public void onItemClick(VideoBean bean, final int position) {
        checkVideo(bean, new CommonCallback<Integer>() {
            @Override
            public void callback(Integer bean) {
                int page = 1;
                if (mRefreshView != null) {
                    page = mRefreshView.getPageCount();
                }
                if (mVideoScrollDataHelper == null) {
                    mVideoScrollDataHelper = new VideoScrollDataHelper() {

                        @Override
                        public void loadData(int p, HttpCallback callback) {
                            MainHttpUtil.getHotVideoList(p, callback);
                        }
                    };
                }
                VideoStorge.getInstance().putDataHelper(mKey, mVideoScrollDataHelper);
                VideoPlayActivity.forward(mContext, position, mKey, page, false);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainHttpUtil.cancel(VideoHttpConsts.GET_CLASS_VIDEO_LIST);
        VideoStorge.getInstance().remove(mKey);
        EventBus.getDefault().unregister(this);
        mVideoScrollDataHelper = null;

    }
}
