package com.yunbao.common.presenter;

import android.app.Dialog;
import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.R;
import com.yunbao.common.bean.VideoBean;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class CheckVideoPresenter {
    private Context mContext;

    public CheckVideoPresenter(Context context) {
        mContext = context;
    }

    public void checkVideo(final String videoId, final CommonCallback<Integer> callback) {
        CommonHttpUtil.getVideo(videoId, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info != null && info.length > 0) {
                    VideoBean videoBean = JSON.parseObject(info[0], VideoBean.class);
                    if (callback != null) {
                        callback.callback(code);
                    } else {
                        forWardVideoPlay(videoBean);
                    }
                } else if (code == 1001) {
                    DialogUitl.showSimpleNoTitDialog(mContext, msg, WordUtil.getString(R.string.login_to), new DialogUitl.SimpleCallback() {
                        @Override
                        public void onConfirmClick(Dialog dialog, String content) {
                            RouteUtil.forwardLogin(mContext);
                        }
                    });
                } else {
                    ToastUtil.show(msg);
                }
            }

            @Override
            public boolean showLoadingDialog() {
                return true;
            }

            @Override
            public Dialog createLoadingDialog() {
                return DialogUitl.loadingDialog(mContext);
            }

            @Override
            public void onError() {
                super.onError();
                ToastUtil.show(WordUtil.getString(R.string.load_failure));
            }
        });
    }

    private void videoPay(final String videoId, final CommonCallback<Integer> callback) {
        if (videoId == null) {
            return;
        }
        CommonHttpUtil.setVideoPay(videoId, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (callback != null) {
                        callback.callback(code);
                    } else {
                        CommonHttpUtil.getVideo(videoId, new HttpCallback() {
                            @Override
                            public void onSuccess(int code, String msg, String[] info) {
                                if (code == 0 && info != null && info.length > 0) {
                                    VideoBean videoBean = JSON.parseObject(info[0], VideoBean.class);
                                    forWardVideoPlay(videoBean);
                                }
                            }
                        });
                    }
                } else {
                    ToastUtil.show(msg);
                }

            }

            @Override
            public void onError() {
                super.onError();
                ToastUtil.show(WordUtil.getString(R.string.load_failure));
            }
        });
    }

    private void forWardVideoPlay(VideoBean videoBean) {
        RouteUtil.forwardVideoPlay(mContext, videoBean);
    }
}
