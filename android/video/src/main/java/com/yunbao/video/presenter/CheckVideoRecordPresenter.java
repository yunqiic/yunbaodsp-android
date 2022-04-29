package com.yunbao.video.presenter;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.bean.VideoBean;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.PermissionCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.PermissionUtil;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.video.R;
import com.yunbao.video.http.VideoHttpConsts;
import com.yunbao.video.http.VideoHttpUtil;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class CheckVideoRecordPresenter {
    private boolean mIsCanRecordVideo;//是否能录制视频 合拍 拍同款。
    private String mVideoStr;
    private Context mContext;
    private VideoBean mVideoBean;
    private RecordCallBack mRecordCallBack;

    public CheckVideoRecordPresenter(Context context) {
        mContext = context;
    }

    public void setRecordCallBack(RecordCallBack recordCallBack) {
        mRecordCallBack = recordCallBack;
    }

    public void checkStatusStartRecord(VideoBean bean) {
        mVideoBean = bean;
        startRecordVideo();
    }


    //去认证
    private void authDialog(String content) {
        DialogUitl.showSimpleNoTitDialog(mContext, content, WordUtil.getString(R.string.to_auth), new DialogUitl.SimpleCallback() {
            @Override
            public void onConfirmClick(Dialog dialog, String content) {
                RouteUtil.forwardAuth(mContext);
            }
        });
    }

    private void startRecordVideo() {
        PermissionUtil.request((AbsActivity) mContext, new PermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        if (mRecordCallBack != null) {
                            mRecordCallBack.onRecord(mVideoBean);
                        }
                    }
                },
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        );
    }


    public interface RecordCallBack {
        void onRecord(VideoBean bean);
    }

    public void destroy() {
        VideoHttpUtil.cancel(VideoHttpConsts.CHECK_LIVE_VIP);
    }
}
