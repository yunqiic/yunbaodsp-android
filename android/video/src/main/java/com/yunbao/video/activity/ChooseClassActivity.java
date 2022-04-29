package com.yunbao.video.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.custom.CommonRefreshView;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.video.R;
import com.yunbao.video.adapter.ClassAdapter;
import com.yunbao.video.bean.ClassBean;
import com.yunbao.video.http.VideoHttpConsts;
import com.yunbao.video.http.VideoHttpUtil;

import java.util.Arrays;
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
public class ChooseClassActivity extends AbsActivity implements View.OnClickListener, OnItemClickListener<ClassBean> {
    private CommonRefreshView mCommonRefreshViewSearch;
    private View mSearchGroup;
    private EditText mEditText;
    private Handler mHandler;
    private InputMethodManager imm;
    private ClassAdapter mClassAdapter;
    private ClassAdapter mSearchLabelAdapter;
    private String mKey;
    private View mBtnClear;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_choose_class;
    }

    @Override
    protected void main() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mSearchGroup = findViewById(R.id.search_group);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                doSearch();
            }
        };
        mEditText = findViewById(R.id.edit);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    VideoHttpUtil.cancel(VideoHttpConsts.SEARCH_LABEL);
                    imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                    if (mHandler != null) {
                        mHandler.removeMessages(0);
                    }
                    doSearch();
                    return true;
                }
                return false;
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mBtnClear != null) {
                    if (TextUtils.isEmpty(s)) {
                        if (mBtnClear.getVisibility() == View.VISIBLE) {
                            mBtnClear.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        if (mBtnClear.getVisibility() != View.VISIBLE) {
                            mBtnClear.setVisibility(View.VISIBLE);
                        }
                    }
                }
                VideoHttpUtil.cancel(VideoHttpConsts.SEARCH_LABEL);
                if (mHandler != null) {
                    mHandler.removeMessages(0);
                    if (!TextUtils.isEmpty(s)) {
                        mHandler.sendEmptyMessageDelayed(0, 500);
                    } else {
                        if (mSearchGroup != null && mSearchGroup.getVisibility() == View.VISIBLE) {
                            mSearchGroup.setVisibility(View.INVISIBLE);
                        }
                        if (mSearchLabelAdapter != null) {
                            mSearchLabelAdapter.clearData();
                        }
                        mKey = null;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBtnClear = findViewById(R.id.btn_clear);
        mBtnClear.setOnClickListener(this);
        CommonRefreshView commonRefreshView = findViewById(R.id.refreshView);
        commonRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        commonRefreshView.setDataHelper(new CommonRefreshView.DataHelper<ClassBean>() {
            @Override
            public RefreshAdapter<ClassBean> getAdapter() {
                if (mClassAdapter == null) {
                    mClassAdapter = new ClassAdapter(mContext);
                    mClassAdapter.setOnItemClickListener(ChooseClassActivity.this);
                }
                return mClassAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                VideoHttpUtil.getClassLists(p, callback);
            }

            @Override
            public List<ClassBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), ClassBean.class);
            }

            @Override
            public void onRefreshSuccess(List<ClassBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<ClassBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mCommonRefreshViewSearch = findViewById(R.id.refreshView_search);
        mCommonRefreshViewSearch.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mCommonRefreshViewSearch.setDataHelper(new CommonRefreshView.DataHelper<ClassBean>() {
            @Override
            public RefreshAdapter<ClassBean> getAdapter() {
                if (mSearchLabelAdapter == null) {
                    mSearchLabelAdapter = new ClassAdapter(mContext);
                    mSearchLabelAdapter.setOnItemClickListener(ChooseClassActivity.this);
                }
                return mSearchLabelAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (!TextUtils.isEmpty(mKey)) {
                    VideoHttpUtil.searchClassLists(mKey, p, callback);
                }
            }

            @Override
            public List<ClassBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), ClassBean.class);
            }

            @Override
            public void onRefreshSuccess(List<ClassBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<ClassBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        commonRefreshView.initData();
    }


    private void doSearch() {
        String key = mEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(key)) {
            if (key.equals(mKey)) {
                return;
            }
            mKey = key;
            if (mSearchGroup != null && mSearchGroup.getVisibility() != View.VISIBLE) {
                mSearchGroup.setVisibility(View.VISIBLE);
            }
            if (mCommonRefreshViewSearch != null) {
                mCommonRefreshViewSearch.initData();
            }
        } else {
            ToastUtil.show(WordUtil.getString(R.string.content_empty));
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_clear) {
            clearEditText();
        }
    }

    private void clearEditText() {
        mKey = null;
        String s = mEditText.getText().toString();
        if (TextUtils.isEmpty(s)) {
            return;
        }
        mEditText.requestFocus();
        imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
        mEditText.setText("");
    }

    @Override
    public void onItemClick(ClassBean bean, int position) {
        Intent intent = new Intent();
        intent.putExtra(Constants.VIDEO_CLASS, bean);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (imm != null && mEditText != null) {
            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        VideoHttpUtil.cancel(VideoHttpConsts.GET_CLASS_LISTS);
        VideoHttpUtil.cancel(VideoHttpConsts.SEARCH_CLASS);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        super.onDestroy();
    }
}
