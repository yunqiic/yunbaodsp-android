package com.yunbao.video.dialog;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.bean.VideoBean;
import com.yunbao.common.dialog.AbsDialogFragment;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.video.R;
import com.yunbao.video.activity.AbsVideoPlayActivity;
import com.yunbao.video.activity.VideoReportActivity;
import com.yunbao.video.adapter.VideoShareAdapter;
import com.yunbao.video.bean.VideoMoreBean;

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

public class VideoShareDialogFragment extends AbsDialogFragment implements OnItemClickListener<VideoMoreBean> {

    private RecyclerView mRecyclerView2;
    private VideoBean mVideoBean;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_video_share;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mVideoBean = bundle.getParcelable(Constants.VIDEO_BEAN);
        if (mVideoBean == null) {
            return;
        }

        mRecyclerView2 = (RecyclerView) findViewById(R.id.recyclerView_2);
        mRecyclerView2.setHasFixedSize(true);
        mRecyclerView2.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        loadData();
    }


    private void loadData() {

        List<VideoMoreBean> list2 = new ArrayList<>();

        //删除举报
        boolean self = mVideoBean.getUid().equals(CommonAppConfig.getInstance().getUid());
        VideoMoreBean reportBean = new VideoMoreBean();
        if (self) {//自己的视频
            reportBean.setType(Constants.DELETE);
            reportBean.setName(R.string.delete);
            reportBean.setIcon1(R.mipmap.icon_share_video_delete_dark);
        } else {
            reportBean.setType(Constants.REPORT);
            reportBean.setName(R.string.report);
            reportBean.setIcon1(R.mipmap.icon_share_video_report_dark);
        }
        list2.add(reportBean);



        //保存
        VideoMoreBean saveBean = new VideoMoreBean();
        saveBean.setType(Constants.SAVE);
        saveBean.setName(R.string.save);
        saveBean.setIcon1(R.mipmap.icon_share_video_save_dark);
        list2.add(saveBean);

        //复制链接
        VideoMoreBean linkBean = new VideoMoreBean();
        linkBean.setType(Constants.LINK);
        linkBean.setName(R.string.copy_link);
        linkBean.setIcon1(R.mipmap.icon_share_video_link_dark);
        list2.add(linkBean);

        VideoShareAdapter adapter2 = new VideoShareAdapter(mContext, list2);
        adapter2.setOnItemClickListener(this);
        mRecyclerView2.setAdapter(adapter2);
    }

    @Override
    public void onItemClick(VideoMoreBean bean, int position) {
        if (!canClick()) {
            return;
        }
        dismiss();
        switch (bean.getType()) {
            case Constants.LINK://复制链接
                ((AbsVideoPlayActivity) mContext).copyLink(mVideoBean);
                break;
            case Constants.REPORT://举报
                if (!CommonAppConfig.getInstance().isLogin()) {
                    RouteUtil.forwardLogin(mContext);
                    return;
                }
                VideoReportActivity.forward(mContext, mVideoBean.getId());
                break;
            case Constants.SAVE://保存
                ((AbsVideoPlayActivity) mContext).downloadVideo(mVideoBean);
                break;
            case Constants.DELETE://删除
                dismiss();
                ((AbsVideoPlayActivity) mContext).deleteVideo(mVideoBean);
                break;

        }
    }

}
