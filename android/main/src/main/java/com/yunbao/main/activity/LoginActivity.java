package com.yunbao.main.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.HtmlConfig;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.activity.WebViewActivity;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.event.LoginSuccessEvent;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;

import org.greenrobot.eventbus.EventBus;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class LoginActivity extends AbsActivity {

    public static void forward(Context context) {
        context.startActivity(new Intent(context, LoginActivity.class));
    }

    private static final int TOTAL = 60;
    private EditText mEditPhone;
    private EditText mEditCode;
    private TextView mBtnCode;
    private Handler mHandler;
    private String mLoginType = "phone";//登录方式
    private int mCount = TOTAL;
    private String mGetCodeAgain;
    private HttpCallback mGetCodeCallback;
    private boolean mHasGetCode;
    private ImageView mLoginCheckBox;
    private boolean mChecked;
    private Drawable mCheckedDrawable;
    private Drawable mUnCheckedDrawable;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void main() {
        // mGetCode = WordUtil.getString(R.string.login_get_code);
        mCheckedDrawable = ContextCompat.getDrawable(mContext, R.mipmap.bg_login_check_1);
        mUnCheckedDrawable = ContextCompat.getDrawable(mContext, R.mipmap.bg_login_check_0);
        mLoginCheckBox = findViewById(R.id.btn_login_check);
        mLoginCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChecked = !mChecked;
                mLoginCheckBox.setImageDrawable(mChecked ? mCheckedDrawable : mUnCheckedDrawable);
            }
        });
        mGetCodeAgain = WordUtil.getString(R.string.login_get_code_again);
        mEditPhone = (EditText) findViewById(R.id.edit_phone);
        mEditCode = (EditText) findViewById(R.id.edit_code);
        mBtnCode = (TextView) findViewById(R.id.btn_get_code);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mCount--;
                if (mCount > 0) {
                    mBtnCode.setText(mCount + "s");
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(0, 1000);
                    }
                } else {
                    mBtnCode.setText(mGetCodeAgain);
                    mCount = TOTAL;
                    if (mBtnCode != null) {
                        mBtnCode.setEnabled(true);
                    }
                }
            }
        };

        MainHttpUtil.getLoginInfo(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    String[] loginTypeArray = JSON.parseObject(obj.getString("login_type"), String[].class);


                    TextView loginTipTextView = findViewById(R.id.login_tip);
                    if (loginTipTextView != null) {
                        JSONObject loginInfo = obj.getJSONObject("login_alert");
                        String loginTip = loginInfo.getString("login_title");
                        SpannableString spannableString = new SpannableString(loginTip);
                        JSONArray msgArray = JSON.parseArray(loginInfo.getString("message"));
                        for (int i = 0, size = msgArray.size(); i < size; i++) {
                            final JSONObject msgItem = msgArray.getJSONObject(i);
                            String title = msgItem.getString("title");
                            int startIndex = loginTip.indexOf(title);
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
                        loginTipTextView.setText(spannableString);
                        loginTipTextView.setMovementMethod(LinkMovementMethod.getInstance());//不设置 没有点击事件
                        loginTipTextView.setHighlightColor(Color.TRANSPARENT); //设置点击后的颜色为透明
                    }


//                    LoginTipDialogFragment fragment = new LoginTipDialogFragment();
//                    fragment.setJSONObject(obj);
//                    fragment.show(getSupportFragmentManager(), "LoginTipDialogFragment");
                }
            }
        });

    }


    public void loginClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_login) {
            login();
        } else if (i == R.id.btn_get_code) {
            getLoginCode();
        }
    }


    /**
     * 手机号验证码登录
     */
    private void login() {
        if (!mChecked) {
            ToastUtil.show(R.string.login_check_tip);
            return;
        }
        final String phoneNum = mEditPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNum)) {
            ToastUtil.show(R.string.login_input_phone);
            mEditPhone.requestFocus();
            return;
        }
//        if (!ValidatePhoneUtil.validateMobileNumber(phoneNum)) {
//            mEditPhone.setError(WordUtil.getString(R.string.login_phone_error));
//            mEditPhone.requestFocus();
//            return;
//        }
        if (!mHasGetCode) {
            ToastUtil.show(R.string.login_get_code_please);
            return;
        }
        final String code = mEditCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            ToastUtil.show(R.string.login_input_code);
            mEditCode.requestFocus();
            return;
        }
        mLoginType = "phone";
        MainHttpUtil.login(phoneNum, code, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                onLoginSuccess(code, msg, info);
            }
        });
    }

    private void getLoginCode() {
        String phoneNum = mEditPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNum)) {
            ToastUtil.show(R.string.login_input_phone);
            mEditPhone.requestFocus();
            return;
        }
        mEditCode.requestFocus();
        if (mGetCodeCallback == null) {
            mGetCodeCallback = new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0) {
                        mBtnCode.setEnabled(false);
                        if (mHandler != null) {
                            mHandler.sendEmptyMessage(0);
                        }
                        if (!TextUtils.isEmpty(msg) && msg.contains("123456")) {
                            ToastUtil.show(msg);
                        } else {
                            ToastUtil.show(R.string.login_send_success);
                        }
                    } else {
                        ToastUtil.show(msg);
                    }
                }
            };
        }
        mHasGetCode = true;
        MainHttpUtil.getLoginCode(phoneNum, mGetCodeCallback);
    }


    //登录即代表同意服务和隐私条款
    private void forwardTip() {
        WebViewActivity.forward2(mContext, HtmlConfig.LOGIN_PRIVCAY);
    }

    //登录成功！
    private void onLoginSuccess(int code, String msg, String[] info) {
        if (code == 0 && info.length > 0) {
            JSONObject obj = JSON.parseObject(info[0]);
            String uid = obj.getString("id");
            String token = obj.getString("token");
            boolean isReg = obj.getIntValue("isreg") == 1;
            CommonAppConfig.getInstance().setLoginInfo(uid, token, true);
            EventBus.getDefault().post(new LoginSuccessEvent(uid, isReg));
            getBaseUserInfo();
        } else {
            ToastUtil.show(msg);
        }
    }

    /**
     * 获取用户信息
     */
    private void getBaseUserInfo() {
        MainHttpUtil.getBaseInfo(new CommonCallback<UserBean>() {
            @Override
            public void callback(UserBean bean) {
                finish();
            }
        });
    }


    @Override
    protected void onDestroy() {
        CommonHttpUtil.cancel(CommonHttpConsts.GET_CONFIG);
        MainHttpUtil.cancel(MainHttpConsts.LOGIN);
        MainHttpUtil.cancel(MainHttpConsts.GET_LOGIN_CODE);
        MainHttpUtil.cancel(MainHttpConsts.GET_BASE_INFO);
        MainHttpUtil.cancel(MainHttpConsts.GET_LOGIN_INFO);
        super.onDestroy();
    }


}
