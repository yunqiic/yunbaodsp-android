package com.yunbao.main.views;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.custom.CommonRefreshView;
import com.yunbao.common.custom.ItemDecoration;
import com.yunbao.common.event.LoginSuccessEvent;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.main.R;
import com.yunbao.main.activity.MainActivity;
import com.yunbao.main.adapter.UserWorkAdapter;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;
import com.yunbao.video.activity.VideoPlayActivity;
import com.yunbao.common.bean.VideoBean;
import com.yunbao.video.event.VideoDeleteEvent;
import com.yunbao.video.event.VideoInsertListEvent;
import com.yunbao.video.event.VideoScrollPageEvent;
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

public class UserWorkViewHolder extends AbsVideoMainViewHolder implements OnItemClickListener<VideoBean> {

    private CommonRefreshView mRefreshView;
    private String mToUid;
    private String mKey;
    private VideoScrollDataHelper mVideoScrollDataHelper;
    private UserWorkAdapter mAdapter;
    private ActionListener mActionListener;

    public UserWorkViewHolder(Context context, ViewGroup parentView, String toUid) {
        super(context, parentView, toUid);
    }

    @Override
    protected void processArguments(Object... args) {
        mToUid = (String) args[0];
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_user_work;
    }

    @Override
    public void init() {
        mKey = Constants.VIDEO_USER_WORK + this.hashCode();
        mRefreshView = (CommonRefreshView) findViewById(R.id.refreshView);
        if (!TextUtils.isEmpty(mToUid) && mToUid.equals(CommonAppConfig.getInstance().getUid())) {
            if (mContext instanceof MainActivity) {
                mRefreshView.setEmptyLayoutId(0);
            } else {
                mRefreshView.setEmptyLayoutId(R.layout.view_empty_user_work_1);
            }
        } else {
            mRefreshView.setEmptyLayoutId(R.layout.view_empty_user_work_2);
        }
        mRefreshView.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false));
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 2, 0);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRefreshView.setItemDecoration(decoration);
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<VideoBean>() {
            @Override
            public RefreshAdapter<VideoBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new UserWorkAdapter(mContext);
                    mAdapter.setOnItemClickListener(UserWorkViewHolder.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (!TextUtils.isEmpty(mToUid)) {
                    MainHttpUtil.getUserWorkList(mToUid, p, callback);
                }
            }

            @Override
            public List<VideoBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), VideoBean.class);
            }

            @Override
            public void onRefreshSuccess(List<VideoBean> list, int listCount) {
                VideoStorge.getInstance().put(mKey, list);
                if (mActionListener != null) {
                    mActionListener.onWorkCountChanged(listCount);
                }
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
        EventBus.getDefault().register(UserWorkViewHolder.this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoInsertListEvent(VideoInsertListEvent e) {
        if (!TextUtils.isEmpty(mKey) && mKey.equals(e.getKey()) && mAdapter != null) {
            mAdapter.notifyItemRangeInserted(e.getStartPosition(), e.getInsertCount());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoScrollPageEvent(VideoScrollPageEvent e) {
        if (!TextUtils.isEmpty(mKey) && mKey.equals(e.getKey()) && mRefreshView != null) {
            mRefreshView.setPageCount(e.getPage());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoDeleteEvent(VideoDeleteEvent e) {
        if (!(mContext instanceof MainActivity)) {
            if (!TextUtils.isEmpty(mKey) && mKey.equals(e.getVideoKey()) && mAdapter != null) {
                mAdapter.deleteVideo(e.getVideoId());
                if (mAdapter.getItemCount() == 0 && mRefreshView != null) {
                    mRefreshView.showEmpty();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginSuccessEvent(LoginSuccessEvent e) {
        if (mContext instanceof MainActivity) {
            mToUid = e.getUid();
        }
    }


    @Override
    public void onItemClick(VideoBean bean, final int position) {
        checkVideo(bean, new CommonCallback<Integer>() {
            @Override
            public void callback(Integer code) {
                int page = 1;
                if (mRefreshView != null) {
                    page = mRefreshView.getPageCount();
                }
                if (mVideoScrollDataHelper == null) {
                    mVideoScrollDataHelper = new VideoScrollDataHelper() {

                        @Override
                        public void loadData(int p, HttpCallback callback) {
                            if (!TextUtils.isEmpty(mToUid)) {
                                MainHttpUtil.getUserWorkList(mToUid, p, callback);
                            }
                        }
                    };
                }
                VideoStorge.getInstance().putDataHelper(mKey, mVideoScrollDataHelper);
                VideoPlayActivity.forward(mContext, position, mKey, page, false);
            }
        });
    }


    @Override
    public void loadData() {
        if (mContext instanceof MainActivity) {
            if (mRefreshView != null) {
                mRefreshView.initData();
            }
        } else {
            if (isFirstLoadData() && mRefreshView != null) {
                mRefreshView.initData();
            }
        }
    }

    @Override
    public void onDestroy() {
        mVideoScrollDataHelper = null;
        mActionListener = null;
        MainHttpUtil.cancel(MainHttpConsts.GET_USER_WORK_LIST);
        EventBus.getDefault().unregister(this);
        VideoStorge.getInstance().remove(mKey);
        super.onDestroy();
    }


    public interface ActionListener {
        void onWorkCountChanged(int count);
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }
}
