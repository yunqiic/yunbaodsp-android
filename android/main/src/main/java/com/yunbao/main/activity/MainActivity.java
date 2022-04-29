package com.yunbao.main.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.adapter.ViewPagerAdapter;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.custom.TabButtonGroup;
import com.yunbao.common.event.LoginInvalidEvent;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.upload.UploadBean;
import com.yunbao.common.upload.UploadCallback;
import com.yunbao.common.upload.UploadStrategy;
import com.yunbao.common.upload.UploadUtil;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.VersionUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.http.MainHttpUtil;
import com.yunbao.main.presenter.CheckMainStartPresenter;
import com.yunbao.main.utils.LoginUtil;
import com.yunbao.main.views.AbsMainViewHolder;
import com.yunbao.main.views.MainHomeViewHolder;
import com.yunbao.main.views.MainMeViewHolder;
import com.yunbao.video.activity.AbsVideoPlayActivity;
import com.yunbao.video.utils.VideoStorge;

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
public class MainActivity extends AbsVideoPlayActivity implements TabButtonGroup.ActionListener {
    private TabButtonGroup mTabButtonGroup;
    private ViewPager mViewPager;
    private List<FrameLayout> mViewList;
    private MainHomeViewHolder mHomeViewHolder;
    private MainMeViewHolder mMeViewHolder;
    private AbsMainViewHolder[] mViewHolders;
    private boolean mFristLoad;
    private long mLastClickBackTime;//上次点击back键的时间
    private int mCurPosition;
    private long mClickHomeBtnTime;

    private ViewGroup mRootView;
    private CheckMainStartPresenter mCheckMainStartPresenter;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void main() {
        super.main();
        Intent intent = getIntent();
        mTabButtonGroup = (TabButtonGroup) findViewById(R.id.tab_group);
        mRootView = (ViewGroup) findViewById(R.id.root);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setOffscreenPageLimit(2);
        mViewList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            FrameLayout frameLayout = new FrameLayout(mContext);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mViewList.add(frameLayout);
        }
        mViewPager.setAdapter(new ViewPagerAdapter(mViewList));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                loadPageData(position);
                if (mViewHolders != null) {
                    for (int i = 0, length = mViewHolders.length; i < length; i++) {
                        AbsMainViewHolder vh = mViewHolders[i];
                        if (vh != null) {
                            vh.setShowed(position == i);
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabButtonGroup.setActionListener(this);
        mViewHolders = new AbsMainViewHolder[2];
        EventBus.getDefault().register(this);
        checkVersion();
        CommonAppConfig.getInstance().setLaunched(true);
        mFristLoad = true;

    }

    public void mainClick(View v) {
        if (!canClick()) {
            return;
        }
        int i = v.getId();
        if (i == R.id.btn_start) {
            if (mHomeViewHolder != null && mHomeViewHolder.isVideoPub()) {
                ToastUtil.show(R.string.video_pub_tip);
            } else {
                startRecordVideo();
            }
        } else if (i == R.id.btn_search) {
            SearchActivity.forward(mContext);
        }
    }




    public void startRecordVideo() {
        if (mCheckMainStartPresenter == null) {
            mCheckMainStartPresenter = new CheckMainStartPresenter(mContext);
        }
        mCheckMainStartPresenter.checkStatusStartRecord();
    }

    /**
     * 检查版本更新
     */
    private void checkVersion() {
        CommonAppConfig.getInstance().getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean configBean) {
                if (configBean != null) {
                    mConfigBean = configBean;
                    if (configBean.getMaintainSwitch() == 1) {//开启维护
                        DialogUitl.showSimpleTipDialog(mContext, WordUtil.getString(R.string.main_maintain_notice), configBean.getMaintainTips());
                    }
                    if (!VersionUtil.isLatest(configBean.getVersion())) {
                        VersionUtil.showDialog(mContext, configBean.getUpdateDes(), configBean.getDownloadApkUrl());
                    }
                }
            }
        });
    }








    @Override
    protected void onResume() {
        super.onResume();
        if (mFristLoad) {
            mFristLoad = false;
            loadPageData(0);
        }

    }



    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        MainHttpUtil.cancel(CommonHttpConsts.GET_CONFIG);
        if (mCheckMainStartPresenter != null) {
            mCheckMainStartPresenter.cancel();
        }
        CommonAppConfig.getInstance().setLaunched(false);
        VideoStorge.getInstance().clear();
        release();
        super.onDestroy();
    }


    public static void forward(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }



    @Override
    public void onBackPressed() {
        if (checkAndHideCommentView()) {
            return;
        }
        long curTime = System.currentTimeMillis();
        if (curTime - mLastClickBackTime > 2000) {
            mLastClickBackTime = curTime;
            ToastUtil.show(R.string.main_click_next_exit);
            return;
        }
        release();
        super.onBackPressed();
    }


    private void loadPageData(int position) {
        if (mViewHolders == null) {
            return;
        }
        AbsMainViewHolder vh = mViewHolders[position];
        if (vh == null) {
            if (mViewList != null && position < mViewList.size()) {
                FrameLayout parent = mViewList.get(position);
                if (parent == null) {
                    return;
                }
                if (position == 0) {
                    mHomeViewHolder = new MainHomeViewHolder(mContext, parent);
                    vh = mHomeViewHolder;
                }else if (position == 1) {
                    mMeViewHolder = new MainMeViewHolder(mContext, parent);
                    vh = mMeViewHolder;
                }
                if (vh == null) {
                    return;
                }
                mViewHolders[position] = vh;
                vh.addToParent();
                vh.subscribeActivityLifeCycle();
            }
        }
        if (vh != null) {
            vh.loadData();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginInvalidEvent(LoginInvalidEvent e) {
        LoginUtil.logout();
        if (mTabButtonGroup != null) {
            mTabButtonGroup.setCheck(mCurPosition, false);
            mTabButtonGroup.setCheck(0, true);
            mCurPosition = 0;
        }
        if (mViewPager != null) {
            mViewPager.setCurrentItem(0, false);
        }
    }

    @Override
    public void onItemClick(int position) {
        if (position == mCurPosition) {
            if (isTabHomeRecommend()) {
                long curTime = System.currentTimeMillis();
                if (curTime - mClickHomeBtnTime < 500) {
                    mClickHomeBtnTime = 0;
                    //双击刷新推荐
                    refreshRecommend();
                } else {
                    mClickHomeBtnTime = curTime;
                }
            }
            return;
        }
        mClickHomeBtnTime = 0;
        if (position == 1 && !CommonAppConfig.getInstance().isLogin()) {
            LoginActivity.forward(mContext);
            return;
        }
        if (mTabButtonGroup != null) {
            mTabButtonGroup.setCheck(mCurPosition, false);
            mTabButtonGroup.setCheck(position, true);
        }

        if (mViewPager != null) {
            mViewPager.setCurrentItem(position, false);
        }
        mCurPosition = position;
    }

    /**
     * 切换到首页推荐
     */
    public void tabHomeRecommend() {
        if (mCurPosition != 0) {
            if (mTabButtonGroup != null) {
                mTabButtonGroup.setCheck(mCurPosition, false);
                mTabButtonGroup.setCheck(0, true);
            }
            mCurPosition = 0;
        }
        if (mViewPager != null) {
            mViewPager.setCurrentItem(0, false);
        }
        if (mHomeViewHolder != null) {
            mHomeViewHolder.setCurrentItem(0);
        }

    }


    /**
     * 是否切换到首页推荐
     */
    public boolean isTabHomeRecommend() {
        if (mCurPosition != 0) {
            return false;
        }
        if (mHomeViewHolder != null && mHomeViewHolder.getCurrentItem() != 0) {
            return false;
        }
        return true;
    }


    /**
     * 刷新推荐
     */
    private void refreshRecommend() {
        if (mHomeViewHolder != null) {
            mHomeViewHolder.refreshRecommend();
        }
    }

    @Override
    public void finishActivityOnVideoDelete(String videoId) {
        if (mHomeViewHolder != null) {
            mHomeViewHolder.deleteVideoFromPlay(videoId);
        }
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
                            mMeViewHolder.loadData();
                        }
                    }

                    @Override
                    public void onFinish() {

                    }
                }
        );



    }
}
