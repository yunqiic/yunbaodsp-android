package com.yunbao.main.presenter;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.PermissionCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.PermissionUtil;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.video.activity.VideoRecordActivity;
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
public class CheckMainStartPresenter {
    private Context mContext;

    public CheckMainStartPresenter(Context context) {
        mContext = context;
    }



    public void checkStatusStartRecord() {
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
                        VideoRecordActivity.forward(mContext, null, Constants.VIDEO_RECORD_TYPE_RECORD);
                    }
                },
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        );
    }


    public void cancel() {
        VideoHttpUtil.cancel(VideoHttpConsts.CHECK_LIVE_VIP);
    }

}
