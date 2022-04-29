package com.yunbao.main.views;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.utils.L;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.main.R;

import java.util.HashMap;
import java.util.Map;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public abstract class AbsCashDetailViewHolder extends AbsViewHolder {

    private boolean mLoadData;
    private WebView mWebView;

    public AbsCashDetailViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_cash_detail;
    }

    @Override
    public void init() {
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        mWebView = new WebView(mContext);
        mWebView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        container.addView(mWebView);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                L.e("H5-------->" + url);
                view.loadUrl(url);
                return true;
            }
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setBackgroundColor(ContextCompat.getColor(mContext, com.yunbao.common.R.color.background));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    public void loadData() {
        if (mLoadData) {
            return;
        }
        mLoadData = true;
        if (mWebView != null) {
            Map<String,String> webviewHead =new HashMap<>();
            webviewHead.put("Referer", CommonAppConfig.HOST);
            mWebView.loadUrl(getHtmlUrl(),webviewHead);
        }
    }


    @Override
    public void onDestroy() {
        if (mWebView != null) {
            ViewGroup parent = (ViewGroup) mWebView.getParent();
            if (parent != null) {
                parent.removeView(mWebView);
            }
            mWebView.destroy();
        }
        super.onDestroy();
    }

    public abstract String getHtmlUrl();
}
