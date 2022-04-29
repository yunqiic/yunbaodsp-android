package com.yunbao.main.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.HtmlConfig;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.activity.WebViewActivity;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.event.LoginInvalidEvent;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.GlideCatchUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.VersionUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.adapter.SettingAdapter;
import com.yunbao.main.bean.SettingBean;
import com.yunbao.main.http.MainHttpUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
public class SettingActivity extends AbsActivity implements OnItemClickListener<SettingBean> {

    private RecyclerView mRecyclerView;
    private Handler mHandler;
    private SettingAdapter mAdapter;
    private String mVersionUpdateUrl;//版本更新下载地址
    private Boolean mIsNewest;

    public static void forward(Context context) {
        context.startActivity(new Intent(context, SettingActivity.class));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void main() {
        EventBus.getDefault().register(this);
        setTitle(WordUtil.getString(R.string.setting));
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        List<SettingBean> list = new ArrayList<>();
        list.add(new SettingBean(Constants.SETTING_ABOUT_US, WordUtil.getString(R.string.setting_about_us)));
        list.add(new SettingBean(Constants.SETTING_PRIVACY_POLICY, WordUtil.getString(R.string.setting_privacy_policy)));
        list.add(new SettingBean(Constants.SETTING_UPDATE_ID, WordUtil.getString(R.string.setting_check_update)));
        list.add(new SettingBean(Constants.SETTING_CLEAR_CACHE, WordUtil.getString(R.string.setting_clear_cache_2)));
        list.add(new SettingBean(Constants.SETTING_EXIT, WordUtil.getString(R.string.setting_exit)));
        mAdapter = new SettingAdapter(mContext, list, VersionUtil.getVersion(), getCacheSize());
        mAdapter.setOnItemClickListener(SettingActivity.this);
        mRecyclerView.setAdapter(mAdapter);
        CommonAppConfig.getInstance().getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean configBean) {
                if (configBean != null) {
                    mVersionUpdateUrl = configBean.getDownloadApkUrl();
                    String tip = "";
                    if (VersionUtil.isLatest(configBean.getVersion())) {
                        mIsNewest = true;
                        tip = WordUtil.getString(R.string.version_tip_0);
                    } else {
                        mIsNewest = false;
                        tip = WordUtil.getString(R.string.version_tip_1);
                    }
                    if (mRecyclerView != null && mAdapter != null) {
                        mAdapter.setVersionTip(tip);
                    }
                }
            }
        });
    }


    @Override
    public void onItemClick(SettingBean bean, int position) {
        int id = bean.getId();
        if (id == Constants.SETTING_EXIT) {//退出登录
            DialogUitl.showStringArrayDialog(mContext, new Integer[]{R.string.a_063}, ContextCompat.getColor(mContext, R.color.global), new DialogUitl.StringArrayDialogCallback() {
                @Override
                public void onItemClick(String text, int tag) {
                    if (tag == R.string.a_063) {
                        logout();
                    }
                }
            });

        } else if (bean.getId() == Constants.SETTING_ABOUT_US) {//关于我们
            String deviceVal = StringUtil.contact(android.os.Build.MODEL, "_", android.os.Build.VERSION.RELEASE);
            try {
                deviceVal = URLEncoder.encode(deviceVal, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                deviceVal = "";
            }
            String url = StringUtil.contact(
                    HtmlConfig.ABOUT_US,
                    "?version=", CommonAppConfig.getInstance().getVersion(),
                    "&device=", deviceVal
            );
            forwardHtml(url);
        } else if (bean.getId() == Constants.SETTING_UPDATE_ID) {//检查更新
            checkVersion();
        } else if (bean.getId() == Constants.SETTING_CLEAR_CACHE) {//清除缓存
            clearCache(position);
        }  else if (bean.getId() == Constants.SETTING_PRIVACY_POLICY) {
            PrivacyPolicyActivity.forward(mContext);
        }

    }

    private void forwardHtml(String url) {
        WebViewActivity.forward(mContext, url);
    }


    /**
     * 检查更新
     */
    private void checkVersion() {
        if (mIsNewest == null) {
            return;
        }
        if (mIsNewest) {
            ToastUtil.show(R.string.version_latest);
        } else {
            if (!TextUtils.isEmpty(mVersionUpdateUrl)) {
                try {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.setData(Uri.parse(mVersionUpdateUrl));
                    startActivity(intent);
                } catch (Exception e) {
                    ToastUtil.show(R.string.version_download_url_error);
                }
            } else {
                ToastUtil.show(R.string.version_download_url_error);
            }
        }
    }

    /**
     * 退出登录
     */
    private void logout() {
        EventBus.getDefault().post(new LoginInvalidEvent());
        finish();
    }


    /**
     * 获取缓存
     */
    private String getCacheSize() {
        return GlideCatchUtil.getInstance().getCacheSize();
    }

    /**
     * 清除缓存
     */
    private void clearCache(final int position) {
        final Dialog dialog = DialogUitl.loadingDialog(mContext, getString(R.string.setting_clear_cache_ing));
        dialog.show();
        GlideCatchUtil.getInstance().clearImageAllCache();
        File gifGiftDir = new File(CommonAppConfig.GIF_PATH);
        if (gifGiftDir.exists() && gifGiftDir.length() > 0) {
            gifGiftDir.delete();
        }
        if (mHandler == null) {
            mHandler = new Handler();
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (mAdapter != null) {
                    mAdapter.setCacheString(getCacheSize());
                    mAdapter.notifyItemChanged(position);
                }
                ToastUtil.show(R.string.setting_clear_cache);
            }
        }, 2000);
    }


    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        CommonHttpUtil.cancel(CommonHttpConsts.GET_CONFIG);
        super.onDestroy();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginInvalidEvent(LoginInvalidEvent e) {
        finish();
    }
}
