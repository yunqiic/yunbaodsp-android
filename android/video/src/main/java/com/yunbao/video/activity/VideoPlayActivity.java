package com.yunbao.video.activity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.yunbao.common.Constants;
import com.yunbao.common.bean.VideoBean;
import com.yunbao.common.event.LoginInvalidEvent;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.utils.L;
import com.yunbao.video.R;
import com.yunbao.video.http.VideoHttpUtil;
import com.yunbao.video.utils.VideoStorge;
import com.yunbao.video.views.VideoScrollViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
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
public class VideoPlayActivity extends AbsVideoPlayActivity {

    public static void forward(Context context, int position, String videoKey, int page, boolean fromUserHome) {
        Intent intent = new Intent(context, VideoPlayActivity.class);
        intent.putExtra(Constants.VIDEO_POSITION, position);
        intent.putExtra(Constants.VIDEO_KEY, videoKey);
        intent.putExtra(Constants.VIDEO_PAGE, page);
        intent.putExtra(Constants.VIDEO_FROM_USER_HOME, fromUserHome);
        context.startActivity(intent);
    }

    public static void forwardSingle(Context context, VideoBean videoBean, boolean fromUserHome) {
        if (videoBean == null) {
            return;
        }
        List<VideoBean> list = new ArrayList<>();
        list.add(videoBean);
        VideoStorge.getInstance().put(Constants.VIDEO_SINGLE, list);
        Intent intent = new Intent(context, VideoPlayActivity.class);
        intent.putExtra(Constants.VIDEO_POSITION, 0);
        intent.putExtra(Constants.VIDEO_KEY, Constants.VIDEO_SINGLE);
        intent.putExtra(Constants.VIDEO_PAGE, 1);
        intent.putExtra(Constants.VIDEO_FROM_USER_HOME, fromUserHome);
        context.startActivity(intent);
    }

    private boolean mFromUserHome;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_play;
    }

    @Override
    protected boolean isStatusBarWhite() {
        return true;
    }

    @Override
    protected void main() {
        super.main();
        Intent intent = getIntent();
        mVideoKey = intent.getStringExtra(Constants.VIDEO_KEY);
        if (TextUtils.isEmpty(mVideoKey)) {
            return;
        }

        boolean isPushVideo = intent.getBooleanExtra(Constants.VIDEO_PUSH, false);
        if (isPushVideo) {
            VideoBean videoBean = intent.getParcelableExtra(Constants.VIDEO_BEAN);
            List<VideoBean> list = new ArrayList<>();
            list.add(videoBean);
            VideoStorge.getInstance().put(Constants.VIDEO_SINGLE, list);
        }

        int position = intent.getIntExtra(Constants.VIDEO_POSITION, 0);
        int page = intent.getIntExtra(Constants.VIDEO_PAGE, 1);
        mFromUserHome = intent.getBooleanExtra(Constants.VIDEO_FROM_USER_HOME, true);
        mVideoScrollViewHolder = new VideoScrollViewHolder(mContext, (ViewGroup) findViewById(R.id.container), position, mVideoKey, page, false, mFromUserHome);
        mVideoScrollViewHolder.addToParent();
        mVideoScrollViewHolder.subscribeActivityLifeCycle();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginInvalidEvent(LoginInvalidEvent e) {
        finish();
    }

    @Override
    public void onBackPressed() {
        if(checkAndHideCommentView()){
            return;
        }
        EventBus.getDefault().unregister(this);
        release();
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
        System.gc();
        ImgLoader.clearMemory(mContext);
        L.e("VideoPlayActivity------->onDestroy");
    }


}
