package com.yunbao.video.presenter;

import android.app.Dialog;
import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.bean.VideoBean;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.video.R;
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
public class CheckVideoPlayPresenter {
    public static final int PLAY_CHECK=1;
    public static final int CLICK_CHECK=2;
    private Context mContext;
    private Dialog mTipDialog;
    private VideoBean mVideoBean;
    private OnCheckVideoCallBack mCheckVideoCallBack;
    public CheckVideoPlayPresenter(Context context) {
        mContext = context;
    }

    public void setCheckVideoCallBack(OnCheckVideoCallBack checkVideoCallBack) {
        mCheckVideoCallBack = checkVideoCallBack;
    }

    public void checkVideo(VideoBean videoBean, final int type) {
        mVideoBean = videoBean;
        if (mTipDialog != null) {
            if (mTipDialog.isShowing()) {
                mTipDialog.dismiss();
            }
        }
        VideoHttpUtil.getVideo(videoBean.getId(), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 &&info!=null&& info.length > 0) {
                    mCheckVideoCallBack.onLoadSuccess(code,type, JSON.parseObject(info[0], VideoBean.class));
                } else {
                    if (mCheckVideoCallBack != null) {
                        mCheckVideoCallBack.onLoadSuccess(code,type, null);
                    }
                    if (code == 1001) {
                        mTipDialog = DialogUitl.showSimpleNoTitDialog(mContext, msg, WordUtil.getString(R.string.login_to), new DialogUitl.SimpleCallback() {
                            @Override
                            public void onConfirmClick(Dialog dialog, String content) {
                                RouteUtil.forwardLogin(mContext);
                            }
                        });
                    }  else {
                        ToastUtil.show(msg);
                    }
                }
            }
        });
    }


    public interface OnCheckVideoCallBack {
        void onLoadSuccess(int code,int type, VideoBean videoBean);
    }



}
