package com.yunbao.shortvideo.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.Constants;
import com.yunbao.common.bean.AdBean;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.custom.CircleProgress;
import com.yunbao.common.event.LoginInvalidEvent;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.DownloadUtil;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.MD5Util;
import com.yunbao.common.utils.SpUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.activity.MainActivity;
import com.yunbao.main.dialog.LoginTipDialogFragment;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;
import com.yunbao.main.utils.LoginUtil;
import com.yunbao.main.views.LauncherAdViewHolder;
import com.yunbao.shortvideo.AppContext;
import com.yunbao.shortvideo.R;

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

public class LauncherActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LauncherActivity";
    private static final int WHAT_GET_CONFIG = 0;
    private static final int WHAT_COUNT_DOWN = 1;
    private Handler mHandler;
    protected Context mContext;
    private ViewGroup mRoot;

    private ViewGroup mContainer;
    private View mCover;
    private CircleProgress mCircleProgress;
    private List<AdBean> mAdList;
    private List<ImageView> mImageViewList;
    private int mMaxProgressVal;
    private int mCurProgressVal;
    private int mAdIndex;
    private int mInterval = 2000;
    private View mBtnSkipImage;
    private View mBtnSkipVideo;
    private TXCloudVideoView mTXCloudVideoView;
    private TXLivePlayer mPlayer;
    private LauncherAdViewHolder mLauncherAdViewHolder;
    private boolean mPaused;
    private int mVideoLastProgress;
    private View mAdTip;
    private boolean mGetConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //下面的代码是为了防止一个bug:
        // 收到极光通知后，点击通知，如果没有启动app,则启动app。然后切后台，再次点击桌面图标，app会重新启动，而不是回到前台。
        Intent intent = getIntent();
        if (!isTaskRoot()
                && intent != null
                && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
                && intent.getAction() != null
                && intent.getAction().equals(Intent.ACTION_MAIN)) {
            finish();
            return;
        }
        setStatusBar();
        setContentView(R.layout.activity_launcher);
        if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mContext = this;
        mRoot = findViewById(R.id.root);
        mCover = findViewById(R.id.cover);
        mContainer = findViewById(R.id.container);
        mCircleProgress = findViewById(R.id.progress);
        mBtnSkipImage = findViewById(R.id.btn_skip_img);
        mBtnSkipVideo = findViewById(R.id.btn_skip_video);
        mBtnSkipImage.setOnClickListener(this);
        mBtnSkipVideo.setOnClickListener(this);
        mAdTip = findViewById(R.id.ad_tip);
//        ImageView imageView = (ImageView) findViewById(R.id.img);
//        ImgLoader.display(mContext, R.mipmap.screen, imageView);
        EventBus.getDefault().register(this);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WHAT_GET_CONFIG:
                        checkLogin();
                        break;
                    case WHAT_COUNT_DOWN:
                        updateCountDown();
                        break;
                }
            }
        };
        mHandler.sendEmptyMessageDelayed(WHAT_GET_CONFIG, 1000);
    }

    /**
     * 已登录，显示引导页,未登录，显示隐私条款
     */
    private void checkLogin() {
        String[] uidAndToken = SpUtil.getInstance().getMultiStringValue(
                new String[]{SpUtil.UID, SpUtil.TOKEN});
        //已登录，显示引导页
        if (!TextUtils.isEmpty(uidAndToken[0]) && !TextUtils.isEmpty(uidAndToken[1])
                && CommonAppConfig.getInstance().isLogin()
        ) {
            getConfig();
        } else {
            //未登录，显示隐私条款
            MainHttpUtil.getLoginInfo(new HttpCallback() {

                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0 && info.length > 0) {
                        JSONObject obj = JSON.parseObject(info[0]);
                        LoginTipDialogFragment fragment = new LoginTipDialogFragment();
                        fragment.setJSONObject(obj);
                        fragment.setActionListener(new LoginTipDialogFragment.ActionListener() {
                            @Override
                            public void onConfirmClick() {
                                getConfig();
                            }
                        });
                        fragment.show(getSupportFragmentManager(), "LoginTipDialogFragment");
                    }
                }
            });
        }
    }


    /**
     * 获取Config信息
     */
    private void getConfig() {
        CommonHttpUtil.getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean bean) {
                if (mGetConfig) {
                    return;
                }
                mGetConfig = true;
                if (bean != null) {
                    String adInfo = bean.getAdInfo();
                    if (!TextUtils.isEmpty(adInfo)) {
                        JSONObject obj = JSON.parseObject(adInfo);
                        if (obj.getIntValue("switch") == 1) {
                            List<AdBean> list = JSON.parseArray(obj.getString("list"), AdBean.class);
                            if (list != null && list.size() > 0) {
                                mAdList = list;
                                mInterval = obj.getIntValue("time") * 1000;
                                if (mContainer != null) {
                                    mContainer.setOnClickListener(LauncherActivity.this);
                                }
                                playAD(obj.getIntValue("type") == 0);
                            } else {
                                checkUidAndToken();
                            }
                        } else {
                            checkUidAndToken();
                        }
                    } else {
                        checkUidAndToken();
                    }
                }
            }
        });
    }


    /**
     * 检查uid和token是否存在
     */
    public void checkUidAndToken() {
        String[] uidAndToken = SpUtil.getInstance().getMultiStringValue(
                new String[]{SpUtil.UID, SpUtil.TOKEN});
        final String uid = uidAndToken[0];
        final String token = uidAndToken[1];
        if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(token)) {
            MainHttpUtil.getBaseInfo(uid, token, new CommonCallback<UserBean>() {
                @Override
                public void callback(UserBean bean) {
                    if (bean != null) {
                        CommonAppConfig.getInstance().setLoginInfo(uid, token, false);
                        forwardMainActivity();
                    }
                }
            });
        } else {
            releaseVideo();
            loginInvalid();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginInvalidEvent(LoginInvalidEvent e) {
        loginInvalid();
    }

    private void loginInvalid() {
        LoginUtil.logout();
        forwardMainActivity();
    }

    /**
     * 跳转到首页
     */
    private void forwardMainActivity() {
        MainActivity.forward(mContext);
        finish();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        MainHttpUtil.cancel(MainHttpConsts.GET_LOGIN_INFO);
        MainHttpUtil.cancel(MainHttpConsts.GET_BASE_INFO);
        CommonHttpUtil.cancel(CommonHttpConsts.GET_CONFIG);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        releaseVideo();
        super.onDestroy();
    }

    /**
     * 设置透明状态栏
     */
    private void setStatusBar() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_skip_img || i == R.id.btn_skip_video) {
            if (mBtnSkipImage != null) {
                mBtnSkipImage.setClickable(false);
            }
            if (mBtnSkipVideo != null) {
                mBtnSkipVideo.setClickable(false);
            }
            checkUidAndToken();
        } else if (i == R.id.container) {
            clickAD();
        }
    }

    private void checkHasAdLink(int index) {
        if (index >= 0 && index < mAdList.size()) {
            AdBean adBean = mAdList.get(index);
            if (adBean != null && !TextUtils.isEmpty(adBean.getLink())) {
                if (mAdTip.getVisibility() != View.VISIBLE) {
                    mAdTip.setVisibility(View.VISIBLE);
                }
            } else {
                if (mAdTip.getVisibility() == View.VISIBLE) {
                    mAdTip.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    /**
     * 图片倒计时
     */
    private void updateCountDown() {
        mCurProgressVal += 100;
        if (mCurProgressVal > mMaxProgressVal) {
            return;
        }
        if (mCircleProgress != null) {
            mCircleProgress.setCurProgress(mCurProgressVal);
        }
        int index = mCurProgressVal / mInterval;
        if (index < mAdList.size() && mAdIndex != index) {
            View v = mImageViewList.get(mAdIndex);
            if (v != null && v.getVisibility() == View.VISIBLE) {
                v.setVisibility(View.INVISIBLE);
            }
            mAdIndex = index;
            checkHasAdLink(index);
        }
        if (mCurProgressVal < mMaxProgressVal) {
            if (mHandler != null) {
                mHandler.sendEmptyMessageDelayed(WHAT_COUNT_DOWN, 100);
            }
        } else if (mCurProgressVal == mMaxProgressVal) {
            checkUidAndToken();
        }
    }

    /**
     * 点击广告
     */
    private void clickAD() {
        if (mAdList != null && mAdList.size() > mAdIndex) {
            AdBean adBean = mAdList.get(mAdIndex);
            if (adBean != null) {
                String link = adBean.getLink();
                if (!TextUtils.isEmpty(link)) {
                    if (mHandler != null) {
                        mHandler.removeCallbacksAndMessages(null);
                    }
                    if (mContainer != null) {
                        mContainer.setClickable(false);
                    }
                    releaseVideo();
                    if (mLauncherAdViewHolder == null) {
                        mLauncherAdViewHolder = new LauncherAdViewHolder(mContext, mRoot, link);
                        mLauncherAdViewHolder.addToParent();
                        mLauncherAdViewHolder.loadData();
                        mLauncherAdViewHolder.show();
                        mLauncherAdViewHolder.setActionListener(new LauncherAdViewHolder.ActionListener() {
                            @Override
                            public void onHideClick() {
                                checkUidAndToken();
                            }
                        });
                    }
                }
            }
        }
    }

    private void releaseVideo() {
        if (mPlayer != null) {
            mPlayer.stopPlay(false);
            mPlayer.setPlayListener(null);
        }
        mPlayer = null;
    }

    /**
     * 播放广告
     */
    private void playAD(boolean isImage) {
        if (mContainer == null) {
            return;
        }
        if (isImage) {
            int imgSize = mAdList.size();
            if (imgSize > 0) {
                mImageViewList = new ArrayList<>();
                for (int i = 0; i < imgSize; i++) {
                    final ImageView imageView = new ImageView(mContext);
                    imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setBackgroundColor(0xffffffff);
                    mImageViewList.add(imageView);
                    ImgLoader.displayDrawable(mContext, mAdList.get(i).getUrl(), new ImgLoader.DrawableCallback() {
                        @Override
                        public void callback(Drawable drawable) {
                            imageView.setImageDrawable(drawable);
                            if (mCover != null && mCover.getVisibility() == View.VISIBLE) {
                                mCover.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
                for (int i = imgSize - 1; i >= 0; i--) {
                    mContainer.addView(mImageViewList.get(i));
                }
                if (mBtnSkipImage != null && mBtnSkipImage.getVisibility() != View.VISIBLE) {
                    mBtnSkipImage.setVisibility(View.VISIBLE);
                }
                mMaxProgressVal = imgSize * mInterval;
                if (mCircleProgress != null) {
                    mCircleProgress.setMaxProgress(mMaxProgressVal);
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(WHAT_COUNT_DOWN, 100);
                }
//                if (mCover != null && mCover.getVisibility() == View.VISIBLE) {
//                    mCover.setVisibility(View.INVISIBLE);
//                }
                checkHasAdLink(0);
            } else {
                checkUidAndToken();
            }
        } else {
            if (mAdList == null || mAdList.size() == 0) {
                checkUidAndToken();
                return;
            }
            String videoUrl = mAdList.get(0).getUrl();
            if (TextUtils.isEmpty(videoUrl)) {
                checkUidAndToken();
                return;
            }
            String videoFileName = MD5Util.getMD5(videoUrl);
            if (TextUtils.isEmpty(videoFileName)) {
                checkUidAndToken();
                return;
            }
            File file = new File(getCacheDir(), videoFileName);
            if (file.exists()) {
                playAdVideo(file);
            } else {
                downloadAdFile(videoUrl, videoFileName);
            }
        }
    }

    /**
     * 下载视频
     */
    private void downloadAdFile(String url, String fileName) {
        DownloadUtil downloadUtil = new DownloadUtil();
        downloadUtil.download("ad_video", getCacheDir(), fileName, url, new DownloadUtil.Callback() {
            @Override
            public void onSuccess(File file) {
                playAdVideo(file);
            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onError(Throwable e) {
                checkUidAndToken();
            }
        });
    }


    @Override
    protected void onPause() {
        mPaused = true;
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.setMute(true);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPaused) {
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.setMute(false);
            }
        }
        mPaused = false;
    }


    /**
     * 播放视频
     */
    private void playAdVideo(File videoFile) {
        if (mBtnSkipVideo != null && mBtnSkipVideo.getVisibility() != View.VISIBLE) {
            mBtnSkipVideo.setVisibility(View.VISIBLE);
        }
        mTXCloudVideoView = new TXCloudVideoView(mContext);
        mTXCloudVideoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mTXCloudVideoView.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
        mContainer.addView(mTXCloudVideoView);
        mPlayer = new TXLivePlayer(mContext);
        mPlayer.setPlayerView(mTXCloudVideoView);
        mPlayer.setAutoPlay(true);
        mPlayer.setPlayListener(new ITXLivePlayListener() {
            @Override
            public void onPlayEvent(int e, Bundle bundle) {
                if (e == TXLiveConstants.PLAY_EVT_PLAY_END) {//获取到视频播放完毕的回调
                    checkUidAndToken();
                    L.e(TAG, "视频播放结束------>");
                } else if (e == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {////获取到视频宽高回调
                    float videoWidth = bundle.getInt("EVT_PARAM1", 0);
                    float videoHeight = bundle.getInt("EVT_PARAM2", 0);
                    if (mTXCloudVideoView != null && videoWidth > 0 && videoHeight > 0) {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mTXCloudVideoView.getLayoutParams();
                        int targetH = 0;
                        if (videoWidth >= videoHeight) {//横屏
                            params.gravity = Gravity.CENTER_VERTICAL;
                            targetH = (int) (mTXCloudVideoView.getWidth() / videoWidth * videoHeight);
                        } else {
                            targetH = ViewGroup.LayoutParams.MATCH_PARENT;
                        }
                        if (targetH != params.height) {
                            params.height = targetH;
                            mTXCloudVideoView.requestLayout();
                        }
                    }
                } else if (e == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
                    if (mCover != null && mCover.getVisibility() == View.VISIBLE) {
                        mCover.setVisibility(View.INVISIBLE);
                    }
                } else if (e == TXLiveConstants.PLAY_ERR_NET_DISCONNECT ||
                        e == TXLiveConstants.PLAY_ERR_FILE_NOT_FOUND) {
                    ToastUtil.show(WordUtil.getString(R.string.video_play_error));
                    checkUidAndToken();
                } else if (e == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
                    int progress = bundle.getInt("EVT_PLAY_PROGRESS_MS");
                    if (mVideoLastProgress == progress) {
                        L.e(TAG, "视频播放结束------>");
                        checkUidAndToken();
                    } else {
                        mVideoLastProgress = progress;
                    }
                }
            }

            @Override
            public void onNetStatus(Bundle bundle) {

            }
        });
        mPlayer.startPlay(videoFile.getAbsolutePath(), TXLivePlayer.PLAY_TYPE_LOCAL_VIDEO);
        checkHasAdLink(0);
    }

}
