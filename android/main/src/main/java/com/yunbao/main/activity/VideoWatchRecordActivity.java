package com.yunbao.main.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.custom.CommonRefreshView;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.adapter.VideoWatchRecordAdapter;
import com.yunbao.main.bean.WatchVideoBean;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;
import com.yunbao.video.activity.VideoPlayActivity;
import com.yunbao.common.bean.VideoBean;
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
public class VideoWatchRecordActivity extends AbsActivity implements OnItemClickListener<WatchVideoBean>, View.OnClickListener {

    public static void forward(Context context) {
        Intent intent = new Intent(context, VideoWatchRecordActivity.class);
        context.startActivity(intent);
    }

    private CommonRefreshView mRefreshView;
    private VideoWatchRecordAdapter mAdapter;
    private String mKey;
    private VideoScrollDataHelper mVideoScrollDataHelper;
    private TextView mBtnOption;
    private boolean mCanDelete;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_watch_record;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.watch_record));
        mBtnOption = findViewById(R.id.btn_option);
        mBtnOption.setOnClickListener(this);
        mKey = Constants.VIDEO_WATCH_RECORD + this.hashCode();
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<WatchVideoBean>() {
            @Override
            public RefreshAdapter<WatchVideoBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new VideoWatchRecordAdapter(mContext);
                    mAdapter.setOnItemClickListener(VideoWatchRecordActivity.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MainHttpUtil.getViewRecord(p, callback);
            }

            @Override
            public List<WatchVideoBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), WatchVideoBean.class);
            }

            @Override
            public void onRefreshSuccess(List<WatchVideoBean> list, int listCount) {
                VideoStorge.getInstance().put(mKey, list);
            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<WatchVideoBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        EventBus.getDefault().register(this);
        mRefreshView.initData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoScrollPageEvent(VideoScrollPageEvent e) {
        if (!TextUtils.isEmpty(mKey) && mKey.equals(e.getKey()) && mRefreshView != null) {
            mRefreshView.setPageCount(e.getPage());
        }
    }

    @Override
    public void onItemClick(WatchVideoBean bean, final int position) {
        checkVideo(bean.getId(), new CommonCallback<Integer>() {
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
                            MainHttpUtil.getViewRecord(p, callback);
                        }
                    };
                }
                VideoStorge.getInstance().putDataHelper(mKey, mVideoScrollDataHelper);
                VideoPlayActivity.forward(mContext, position, mKey, page, false);
            }
        });
    }


    public void setCanDelete(boolean canDelete) {
        mCanDelete = canDelete;
        if (mBtnOption != null) {
            mBtnOption.setText(canDelete ? R.string.delete : R.string.a_072);
        }
    }


    public void refreshData(){
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }


    @Override
    public void onClick(View v) {
        if (mAdapter != null) {
            if (mCanDelete) {

                DialogUitl.showSimpleNoTitDialog(mContext, WordUtil.getString(R.string.a_074), null, new DialogUitl.SimpleCallback2() {

                    @Override
                    public void onCancelClick() {
                        mAdapter.setShowCheck(false);
                        setCanDelete(false);
                    }

                    @Override
                    public void onConfirmClick(Dialog dialog, String content) {
                        String ids = mAdapter.getCheckedId();
                        MainHttpUtil.deleteViewRecord(ids, new HttpCallback() {
                            @Override
                            public void onSuccess(int code, String msg, String[] info) {
                                if (code == 0) {
//                                    setCanDelete(false);
                                    if (mRefreshView != null) {
                                        mRefreshView.initData();
                                    }
                                }
                                ToastUtil.show(msg);
                            }
                        });
                    }
                });

            } else {
                mAdapter.toggleShowCheck();
            }
        }
    }


    @Override
    public void onDestroy() {
        mVideoScrollDataHelper = null;
        MainHttpUtil.cancel(MainHttpConsts.GET_VIEW_RECORD);
        MainHttpUtil.cancel(MainHttpConsts.DELETE_VIEW_RECORD);
        EventBus.getDefault().unregister(this);
        VideoStorge.getInstance().remove(mKey);
        super.onDestroy();
    }


}
