package com.yunbao.video.activity;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.custom.ItemDecoration;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.video.R;
import com.yunbao.video.adapter.VideoChooseAdapter;
import com.yunbao.video.bean.VideoChooseBean;
import com.yunbao.video.event.VideoChooseEvent;
import com.yunbao.video.utils.VideoLocalUtil;

import org.greenrobot.eventbus.EventBus;

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

public class VideoChooseActivity extends AbsActivity implements OnItemClickListener<VideoChooseBean> {

    private long mMaxDuration = 15000;//非会员最大时长
    private long mMinDuration = 5000;
    private RecyclerView mRecyclerView;
    private View mNoData;
    private VideoLocalUtil mVideoLocalUtil;
    private boolean mIsCanSelct;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_choose;
    }


    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.video_local));
        Intent intent=getIntent();
        mMaxDuration = intent.getLongExtra(Constants.VIDEO_DURATION_MAX, 15000);
        mMinDuration = intent.getLongExtra(Constants.VIDEO_DURATION_MIN, 5000);
        mIsCanSelct = intent.getBooleanExtra(Constants.VIDEO_LOCAL_CAN_SELCT, false);
        mNoData = findViewById(R.id.no_data);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 4, GridLayoutManager.VERTICAL, false));
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 1, 1);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRecyclerView.addItemDecoration(decoration);
        mVideoLocalUtil = new VideoLocalUtil();
        mVideoLocalUtil.getLocalVideoList(new CommonCallback<List<VideoChooseBean>>() {
            @Override
            public void callback(List<VideoChooseBean> videoList) {
                if (videoList == null || videoList.size() == 0) {
                    if (mNoData != null && mNoData.getVisibility() != View.VISIBLE) {
                        mNoData.setVisibility(View.VISIBLE);
                    }
                    return;
                }
                if (mRecyclerView != null) {
                    VideoChooseAdapter adapter = new VideoChooseAdapter(mContext, videoList);
                    adapter.setOnItemClickListener(VideoChooseActivity.this);
                    mRecyclerView.setAdapter(adapter);
                }
            }
        });
    }

    @Override
    public void onItemClick(VideoChooseBean bean, int position) {
        long duration = bean.getDuration();
        if (duration < mMinDuration) {
            ToastUtil.show(WordUtil.getString(R.string.video_time_limit));
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(Constants.VIDEO_PATH, bean.getVideoPath());
        intent.putExtra(Constants.VIDEO_DURATION, bean.getDuration());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mVideoLocalUtil != null) {
            mVideoLocalUtil.release();
        }
        mVideoLocalUtil = null;
        mRecyclerView = null;
        mNoData = null;
        super.onDestroy();
    }
}
