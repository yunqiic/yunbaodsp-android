package com.yunbao.video.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.video.R;
import com.yunbao.video.adapter.VideoReportAdapter;
import com.yunbao.video.bean.VideoReportBean;
import com.yunbao.video.http.VideoHttpConsts;
import com.yunbao.video.http.VideoHttpUtil;

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

public class VideoReportActivity extends AbsActivity implements VideoReportAdapter.ActionListener {

    public static void forward(Context context, String videoId) {
        Intent intent = new Intent(context, VideoReportActivity.class);
        intent.putExtra(Constants.VIDEO_ID, videoId);
        context.startActivity(intent);
    }

    private String mVideoId;
    private RecyclerView mRecyclerView;
    private VideoReportAdapter mAdapter;
    //    private KeyBoardHeightUtil mKeyBoardHeightUtil;
    private InputMethodManager imm;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_report;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.report));
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mVideoId = getIntent().getStringExtra(Constants.VIDEO_ID);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        VideoHttpUtil.getVideoReportList(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<VideoReportBean> list = JSON.parseArray(Arrays.toString(info), VideoReportBean.class);
                    if (list.size() > 0) {
                        list.get(0).setChecked(true);
                    }
                    mAdapter = new VideoReportAdapter(mContext, list);
                    mAdapter.setActionListener(VideoReportActivity.this);
                    if (mRecyclerView != null) {
                        mRecyclerView.setAdapter(mAdapter);
                    }
                }
            }
        });
    }

    @Override
    public void onReportClick(VideoReportBean bean, String text) {
        if (TextUtils.isEmpty(mVideoId)) {
            return;
        }
        if (bean == null) {
            ToastUtil.show(R.string.video_report_tip_3);
            return;
        }
        String content = bean.getName();
        if (bean.getId() == -1 && !TextUtils.isEmpty(text)) {
            content = StringUtil.contact(content, " ", text);
        }
        VideoHttpUtil.videoReport(mVideoId, bean.getId(), content, mReportCallback);
    }

    @Override
    public void onEditShow(EditText editText, boolean isShow) {
        if (isShow) {
            //软键盘弹出
            editText.requestFocus();
            if(imm!=null){
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        } else {
            if (imm != null) {
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        }
    }

    private HttpCallback mReportCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                ToastUtil.show(R.string.video_report_tip_4);
                onBackPressed();
            } else {
                ToastUtil.show(msg);
            }
        }
    };

//    @Override
//    public void onKeyBoardHeightChanged(int visibleHeight, int keyboardHeight) {
//        if (mRecyclerView != null) {
//            mRecyclerView.setTranslationY(-keyboardHeight);
//        }
//        if (keyboardHeight > 0 && mAdapter != null && mRecyclerView != null) {
//            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
//        }
//    }
//
//    @Override
//    public boolean isSoftInputShowed() {
//        return false;
//    }


    private void release() {
        VideoHttpUtil.cancel(VideoHttpConsts.GET_VIDEO_REPORT_LIST);
        VideoHttpUtil.cancel(VideoHttpConsts.VIDEO_REPORT);
        if (mAdapter != null) {
            mAdapter.setActionListener(null);
        }
        mAdapter = null;
    }

    @Override
    public void onBackPressed() {
        release();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }
}
