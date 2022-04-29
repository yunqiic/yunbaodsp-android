package com.yunbao.main.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yunbao.common.utils.L;
import com.yunbao.common.utils.ScreenDimenUtil;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.main.R;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class LauncherAdViewHolder extends AbsViewHolder implements View.OnClickListener {

    private ObjectAnimator mEnterAnimator;
    private ObjectAnimator mOutAnimator;
    private boolean mLoad;
    private boolean mShowed;
    private boolean mAnimating;
    private ProgressBar mProgressBar;
    private WebView mWebView;
    private TextView mTitle;
    private String mUrl;
    private ActionListener mActionListener;

    public LauncherAdViewHolder(Context context, ViewGroup parentView, String url) {
        super(context, parentView);
        mUrl = url;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_launcher_ad;
    }

    @Override
    public void init() {
        setStatusHeight();
        int screenWidth = ScreenDimenUtil.getInstance().getScreenWdith();
        Interpolator interpolator = new AccelerateDecelerateInterpolator();
        mEnterAnimator = ObjectAnimator.ofFloat(mContentView, "translationX", screenWidth, 0);
        mEnterAnimator.setDuration(200);
        mEnterAnimator.setInterpolator(interpolator);
        mEnterAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                mShowed = true;
                loadData();
            }

        });
        mOutAnimator = ObjectAnimator.ofFloat(mContentView, "translationX", 0, screenWidth);
        mOutAnimator.setDuration(200);
        mOutAnimator.setInterpolator(interpolator);
        mOutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                mShowed = false;
            }
        });
        findViewById(R.id.btn_back).setOnClickListener(this);
        mTitle = (TextView) findViewById(R.id.title);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mWebView = new WebView(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mWebView.setLayoutParams(params);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                L.e("H5-------->" + url);
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (mTitle != null) {
                    mTitle.setText(view.getTitle());
                }
            }

        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 70) {
                    if (mProgressBar.getVisibility() == View.VISIBLE) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                } else {
                    mProgressBar.setProgress(newProgress);
                }
            }
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= 21) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        ((LinearLayout) mContentView).addView(mWebView);
    }


    public void show() {
        if (!mAnimating) {
            mAnimating = true;
            mEnterAnimator.start();
        }
    }

    public void hide() {
        if (!mAnimating) {
            mAnimating = true;
            mOutAnimator.start();
        }
        if (mActionListener != null) {
            mActionListener.onHideClick();
        }
    }


    public boolean isShowed() {
        return mShowed;
    }


    public void loadData() {
        if (!mLoad) {
            mLoad = true;
            mWebView.loadUrl(mUrl);
        }
    }

    @Override
    public void release() {
        super.release();
        mActionListener = null;
        if (mWebView != null) {
            ViewGroup parent = (ViewGroup) mWebView.getParent();
            if (parent != null) {
                parent.removeView(mWebView);
            }
            mWebView.destroy();
            mWebView=null;
        }
        L.e("LauncherAdViewHolder", "----------->release");
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_back) {
            hide();
        }
    }

    public interface ActionListener {
        void onHideClick();
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }
}
