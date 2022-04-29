package com.yunbao.main.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.HtmlConfig;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.activity.WebViewActivity;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.event.FollowEvent;
import com.yunbao.common.event.LoginInvalidEvent;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.upload.UploadBean;
import com.yunbao.common.upload.UploadCallback;
import com.yunbao.common.upload.UploadStrategy;
import com.yunbao.common.upload.UploadUtil;
import com.yunbao.main.R;
import com.yunbao.main.dialog.UserMoreDialogFragment;
import com.yunbao.main.views.UserHomeViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
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
public class UserHomeActivity extends AbsActivity implements View.OnClickListener, UserMoreDialogFragment.ActionListener, UserHomeViewHolder.ActionListener {

    private UserHomeViewHolder mUserHomeViewHolder;
    private String mToUid;
    private UserBean mUserBean;
    private boolean mPaused;
    private ImageView mMore;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_home;
    }

    @Override
    protected void main() {
        Intent intent = getIntent();
        mToUid = intent.getStringExtra(Constants.TO_UID);
        boolean fromLive = intent.getBooleanExtra(Constants.FROM_LIVE, false);
        if (TextUtils.isEmpty(mToUid)) {
            return;
        }
        mUserHomeViewHolder = new UserHomeViewHolder(mContext, (ViewGroup) findViewById(R.id.container));
        mUserHomeViewHolder.setActionListener(this);
        mUserHomeViewHolder.addToParent();
        mUserHomeViewHolder.subscribeActivityLifeCycle();
        mMore =findViewById(R.id.btn_more);
        mMore.setOnClickListener(this);
        mUserHomeViewHolder.setToUid(mToUid);
        mUserHomeViewHolder.loadData();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_more) {
            if (mToUid.equals(CommonAppConfig.getInstance().getUid())) {
                SettingActivity.forward(mContext);
            }else{
             clickMore();
            }
        } else if (i == R.id.btn_follow || i == R.id.btn_follow_cancel) {
            clickFollow();
        } else if (i == R.id.no_work_tip) {
            ((MainActivity) mContext).startRecordVideo();
        }
    }


    private void clickMore() {
        if (CommonAppConfig.getInstance().isLogin()) {
            UserMoreDialogFragment fragment = new UserMoreDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.TO_UID, mToUid);
            fragment.setArguments(bundle);
            fragment.setActionListener(this);
            fragment.show(getSupportFragmentManager(), "UserMoreDialogFragment");
        } else {
            LoginActivity.forward(mContext);
        }
    }

    public void clickFollow() {
        if (!CommonAppConfig.getInstance().isLogin()) {
            LoginActivity.forward(mContext);
            return;
        }
        if (!TextUtils.isEmpty(mToUid)) {
            CommonHttpUtil.setAttention(mToUid, null);
        }
    }

    @Override
    public void onReportClick() {
        WebViewActivity.forward(mContext, HtmlConfig.REPORT + mToUid);
    }




    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowEvent(FollowEvent e) {
        if (!TextUtils.isEmpty(mToUid) && mToUid.equals(e.getToUid())) {
            if (mUserBean != null) {
                mUserBean.setIsattention(e.getIsAttention());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginInvalidEvent(LoginInvalidEvent e) {
        finish();
    }



    @Override
    protected void onDestroy() {
        mUserHomeViewHolder = null;
        EventBus.getDefault().unregister(this);
        CommonHttpUtil.cancel(CommonHttpConsts.SET_ATTENTION);
        super.onDestroy();
        System.gc();
        ImgLoader.clearMemory(mContext);
    }

    @Override
    public void onUserInfoChanged(UserBean userBean) {
        if (userBean == null) {
            return;
        }
        mUserBean = userBean;

    }

    @Override
    public void onWorkCountChanged(boolean showNoWorkTip) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPaused) {
            if (mUserHomeViewHolder != null) {
                mUserHomeViewHolder.loadData();
            }
        }
        mPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null) {
            String result = data.getExtras().getString("result");
            if (!TextUtils.isEmpty(result)) {
                File corpResult = new File(result);
                final List<UploadBean> mUploadList = new ArrayList<>();
                UploadBean uploadBean = new UploadBean();
                uploadBean.setOriginFile(corpResult);
                mUploadList.add(uploadBean);
                UploadUtil.startUpload(new CommonCallback<UploadStrategy>() {
                    @Override
                    public void callback(UploadStrategy strategy) {
                        if (strategy != null) {
                            strategy.upload(mUploadList, true, new UploadCallback() {
                                @Override
                                public void onFinish(List<UploadBean> list, boolean success) {
                                    if (success) {
                                        if (list != null && list.size() > 0) {
                                            final String fileName = list.get(0).getRemoteFileName();
                                            doSubmit(fileName);
                                        }

                                    }
                                }
                            });

                        }
                    }
                });
            }
        }


    }
    public  void doSubmit(String img) {
        CommonHttpUtil.updateBgImg(img,new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            mUserHomeViewHolder.loadData();
                        }
                    }

                    @Override
                    public void onFinish() {

                    }
                }
        );

    }

}
