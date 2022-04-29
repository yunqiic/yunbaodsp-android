package com.yunbao.common.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.yunbao.common.R;
import com.yunbao.common.dialog.AbsDialogFragment;
import com.yunbao.common.event.LoginInvalidEvent;
import com.yunbao.common.event.MyEvent;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.LifeCycleListener;
import com.yunbao.common.presenter.CheckVideoPresenter;
import com.yunbao.common.utils.ClickUtil;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.ScreenDimenUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
public abstract class AbsActivity extends AppCompatActivity {

    protected String mTag;
    protected Context mContext;
    protected List<LifeCycleListener> mLifeCycleListeners;
    private boolean mIsRegistered;
    private CheckVideoPresenter mCheckVideoPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = this.getClass().getSimpleName();
        getInentParams();
        setStatusBar();
        setContentView(getLayoutId());
        setStatusHeight();
        mContext = this;
        mLifeCycleListeners = new ArrayList<>();
        main(savedInstanceState);
        if (mLifeCycleListeners != null) {
            for (LifeCycleListener listener : mLifeCycleListeners) {
                listener.onCreate();
            }
        }
        mIsRegistered = EventBus.getDefault().isRegistered(this);
        if (!mIsRegistered) {
            EventBus.getDefault().register(this);
        }
    }


    protected void getInentParams() {

    }


    private boolean isTopActivity(Activity activity) {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        return cn.getClassName().contains(activity.getLocalClassName());
    }


    protected void checkVideo(final String videoId, final CommonCallback<Integer> callback) {
        if (mCheckVideoPresenter==null){
            mCheckVideoPresenter=new CheckVideoPresenter(mContext);
        }
        mCheckVideoPresenter.checkVideo(videoId,callback);
    }


    protected abstract int getLayoutId();

    protected void main(Bundle savedInstanceState) {
        main();
    }

    protected void main() {

    }

    protected boolean isStatusBarWhite() {
        return true;
    }


    protected void setTitle(String title) {
        TextView titleView = (TextView) findViewById(R.id.titleView);
        if (titleView != null) {
            titleView.setText(title);
        }
    }


    public void backClick(View v) {
        if (v.getId() == R.id.btn_back) {
            onBackPressed();
        }
    }

    protected boolean canClick() {
        return ClickUtil.canClick();
    }


    /**
     * 设置透明状态栏
     */
    protected void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (isStatusBarWhite()) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            } else {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0);
        }
    }


    @Override
    protected void onDestroy() {
        if (mLifeCycleListeners != null) {
            for (LifeCycleListener listener : mLifeCycleListeners) {
                listener.onDestroy();
            }
            mLifeCycleListeners.clear();
            mLifeCycleListeners = null;
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mLifeCycleListeners != null) {
            for (LifeCycleListener listener : mLifeCycleListeners) {
                listener.onStart();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mLifeCycleListeners != null) {
            for (LifeCycleListener listener : mLifeCycleListeners) {
                listener.onReStart();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLifeCycleListeners != null) {
            for (LifeCycleListener listener : mLifeCycleListeners) {
                listener.onResume();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLifeCycleListeners != null) {
            for (LifeCycleListener listener : mLifeCycleListeners) {
                listener.onPause();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLifeCycleListeners != null) {
            for (LifeCycleListener listener : mLifeCycleListeners) {
                listener.onStop();
            }
        }
    }

    public void addLifeCycleListener(LifeCycleListener listener) {
        if (mLifeCycleListeners != null && listener != null) {
            mLifeCycleListeners.add(listener);
        }
    }

    public void addAllLifeCycleListener(List<LifeCycleListener> listeners) {
        if (mLifeCycleListeners != null && listeners != null) {
            mLifeCycleListeners.addAll(listeners);
        }
    }

    public void removeLifeCycleListener(LifeCycleListener listener) {
        if (mLifeCycleListeners != null) {
            mLifeCycleListeners.remove(listener);
        }
    }

    public void onDialogFragmentShow(AbsDialogFragment dialogFragment) {
    }

    public void onDialogFragmentHide(AbsDialogFragment dialogFragment) {
    }



    /**
     * 根据不同手机的状态栏设置高度
     */
    private void setStatusHeight() {
        View flTop = findViewById(R.id.fl_top);
        if (flTop == null) {
            return;
        }
        int statusBarHeight = ScreenDimenUtil.getInstance().getStatusBarHeight();
        if (statusBarHeight > DpUtil.dp2px(19)) {
            flTop.setPadding(0, statusBarHeight, 0, 0);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginInvalidEvent(MyEvent e){

    }

}
