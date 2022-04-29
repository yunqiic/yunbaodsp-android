package com.yunbao.main.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.Constants;
import com.yunbao.common.dialog.AbsDialogFragment;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.http.MainHttpUtil;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class UserMoreDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private ActionListener mActionListener;
    private String mToUid;
    private TextView mName,mId;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_user_more_2;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mToUid = bundle.getString(Constants.TO_UID);
        if (TextUtils.isEmpty(mToUid)) {
            return;
        }
        findViewById(R.id.btn_report).setOnClickListener(this);
        mName =findViewById(R.id.anchor_name);
        mId=findViewById(R.id.anchor_id);
        findViewById(R.id.btn_cancel).setOnClickListener(this);



        MainHttpUtil.getUserHome(mToUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj=JSON.parseObject(info[0]);
                    mName.setText(obj.getString("user_nicename"));
                    mId.setText(String.format(WordUtil.getString(R.string.user_home_id),obj.getString("id")));

                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_report) {
            if (mActionListener != null) {
                mActionListener.onReportClick();
            }
        }
        dismiss();
    }

    @Override
    public void onDestroy() {
        mActionListener = null;
        super.onDestroy();
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public interface ActionListener {
        void onReportClick();
    }
}
