package com.yunbao.main.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.activity.WebViewActivity;
import com.yunbao.common.dialog.AbsDialogFragment;
import com.yunbao.common.utils.DpUtil;
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
public class LoginTipDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private JSONObject mJSONObject;
    private ActionListener mActionListener;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_login_tip;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog;
    }

    @Override
    protected boolean canCancel() {
        return false;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = DpUtil.dp2px(280);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        return true;
                    }
                    return false;
                }
            });
        }
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        TextView mTitle = (TextView) findViewById(R.id.title);
        TextView mContent = (TextView) findViewById(R.id.content);
        if (mJSONObject != null) {
            JSONObject loginInfo = mJSONObject.getJSONObject("login_alert");
            if (mTitle != null) {
                mTitle.setText(loginInfo.getString("title"));
            }
            if (mContent != null) {
                String content = loginInfo.getString("content");
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                SpannableString spannableString = new SpannableString(content);
                JSONArray msgArray = JSON.parseArray(loginInfo.getString("message"));
                for (int i = 0, size = msgArray.size(); i < size; i++) {
                    final JSONObject msgItem = msgArray.getJSONObject(i);
                    String title = msgItem.getString("title");
                    int startIndex = content.indexOf(title);
                    if (startIndex >= 0) {
                        ClickableSpan clickableSpan = new ClickableSpan() {

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setColor(ContextCompat.getColor(mContext, R.color.global));
                                ds.setUnderlineText(false);
                            }

                            @Override
                            public void onClick(View widget) {
                                WebViewActivity.forward2(mContext, msgItem.getString("url"));
                            }
                        };
                        int endIndex = startIndex + title.length();
                        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                mContent.setText(spannableString);
                mContent.setMovementMethod(LinkMovementMethod.getInstance());//不设置 没有点击事件
                mContent.setHighlightColor(Color.TRANSPARENT); //设置点击后的颜色为透明
            }
        }
    }


    public void setJSONObject(JSONObject obj) {
        mJSONObject = obj;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_cancel) {
            dismiss();
            if (mContext != null) {
                ((AppCompatActivity) mContext).finish();
            }
        } else if (id == R.id.btn_confirm) {
            if (mActionListener != null) {
                mActionListener.onConfirmClick();
            }
            dismiss();
        }
    }

    @Override
    public void onDestroy() {
        mContext = null;
        mActionListener = null;
        super.onDestroy();
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public interface ActionListener {
        void onConfirmClick();
    }
}
