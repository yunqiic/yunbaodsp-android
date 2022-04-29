package com.yunbao.main.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.main.R;
import com.yunbao.main.activity.EditProfileActivity;
import com.yunbao.main.activity.MainActivity;
import com.yunbao.main.activity.SettingActivity;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class MainMeViewHolder extends AbsMainViewHolder implements View.OnClickListener, UserHomeViewHolder.ActionListener {

    private View mNoWorkTip;
    private Animation mAnimation;
    private boolean mAnimPlaying;
    private UserHomeViewHolder mUserHomeViewHolder;
    private boolean mPaused;
    private ViewGroup mRootView;


    public MainMeViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_me;
    }

    @Override
    public void init() {
        mNoWorkTip = findViewById(R.id.no_work_tip);
        mRootView = (ViewGroup) findViewById(R.id.root);
        mNoWorkTip.setOnClickListener(this);
        mUserHomeViewHolder = new UserHomeViewHolder(mContext, (ViewGroup) findViewById(R.id.container));
        mUserHomeViewHolder.setActionListener(this);
        mUserHomeViewHolder.addToParent();
        mUserHomeViewHolder.subscribeActivityLifeCycle();
        View v = LayoutInflater.from(mContext).inflate(R.layout.view_user_option_me, mUserHomeViewHolder.getOptionContainer(), false);
        v.findViewById(R.id.btn_edit).setOnClickListener(this);
        v.findViewById(R.id.btn_more_me).setOnClickListener(this);
        mUserHomeViewHolder.setOptionView(v);

    }

    @Override
    public void loadData() {
        if (mUserHomeViewHolder != null) {
            mUserHomeViewHolder.setToUid(CommonAppConfig.getInstance().getUid());
            mUserHomeViewHolder.loadData();
        }
    }


    private void showNoWorkTip(boolean show) {
        if (mNoWorkTip == null) {
            return;
        }
        if (show) {
            if (mNoWorkTip.getVisibility() != View.VISIBLE) {
                mNoWorkTip.setVisibility(View.VISIBLE);
            }
            if (mAnimation == null) {
                mAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                        Animation.ABSOLUTE, 0, Animation.ABSOLUTE, DpUtil.dp2px(5));
                mAnimation.setRepeatCount(-1);
                mAnimation.setRepeatMode(Animation.REVERSE);
                mAnimation.setDuration(400);
            }
            if (!mAnimPlaying) {
                mAnimPlaying = true;
                mNoWorkTip.startAnimation(mAnimation);
            }
        } else {
            mAnimPlaying = false;
            mNoWorkTip.clearAnimation();
            if (mNoWorkTip.getVisibility() == View.VISIBLE) {
                mNoWorkTip.setVisibility(View.INVISIBLE);
            }
        }
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_edit) {
            EditProfileActivity.forward(mContext);
        } else if (i == R.id.btn_more_me) {
            SettingActivity.forward(mContext);
        } else if (i == R.id.no_work_tip) {
            ((MainActivity) mContext).startRecordVideo();
        }
    }

    @Override
    public void onUserInfoChanged(UserBean userBean) {

    }

    @Override
    public void onWorkCountChanged(boolean showNoWorkTip) {
        showNoWorkTip(showNoWorkTip);
    }




    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPaused) {
            if (isShowed()) {
                loadData();
            }
        }
        mPaused = false;

    }



}
