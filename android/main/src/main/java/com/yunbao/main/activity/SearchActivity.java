package com.yunbao.main.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.adapter.ViewPagerAdapter;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.adapter.SearchHistoryAdapter;
import com.yunbao.main.views.AbsSearchViewHolder;
import com.yunbao.main.views.SearchUserViewHolder;
import com.yunbao.main.views.SearchVideoViewHolder;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

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

public class SearchActivity extends AbsActivity implements View.OnClickListener {

    public static void forward(Context context) {
        context.startActivity(new Intent(context, SearchActivity.class));
    }

    private static final int PAGE_COUNT = 2;
    private EditText mEditText;
    private Handler mHandler;
    private InputMethodManager imm;
    private View mSearchGroup;
    private ViewPager mViewPager;
    private MagicIndicator mIndicator;
    private AbsSearchViewHolder[] mViewHolders;
    private List<FrameLayout> mViewList;
    private SearchUserViewHolder mUserViewHolder;
    private SearchVideoViewHolder mVideoViewHolder;
    private RecyclerView mHistoryRecyclerView;
    private SearchHistoryAdapter mHistoryAdapter;
    private View mBtnClear;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_search;
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
                    if (mViewHolders != null) {
                        for (AbsSearchViewHolder vh : mViewHolders) {
                            if (vh != null) {
                                vh.cancelHttpRequest();
                            }
                        }
                    }
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
                if (mViewHolders != null) {
                    for (AbsSearchViewHolder vh : mViewHolders) {
                        if (vh != null) {
                            vh.cancelHttpRequest();
                        }
                    }
                }
                if (mHandler != null) {
                    mHandler.removeMessages(0);
                    if (!TextUtils.isEmpty(s)) {
                        mHandler.sendEmptyMessageDelayed(0, 500);
                    } else {
                        if (mSearchGroup != null && mSearchGroup.getVisibility() == View.VISIBLE) {
                            mSearchGroup.setVisibility(View.INVISIBLE);
                        }
                        if (mViewHolders != null) {
                            for (AbsSearchViewHolder vh : mViewHolders) {
                                if (vh != null) {
                                    vh.clearData();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBtnClear = findViewById(R.id.btn_clear);
        mBtnClear.setOnClickListener(this);
        findViewById(R.id.btn_search_history_clear).setOnClickListener(this);
        mViewHolders = new AbsSearchViewHolder[PAGE_COUNT];
        mViewList = new ArrayList<>();
        for (int i = 0; i < PAGE_COUNT; i++) {
            FrameLayout frameLayout = new FrameLayout(mContext);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mViewList.add(frameLayout);
        }
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        if (PAGE_COUNT > 1) {
            mViewPager.setOffscreenPageLimit(PAGE_COUNT - 1);
        }
        mViewPager.setAdapter(new ViewPagerAdapter(mViewList));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                doSearch();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mIndicator = (MagicIndicator) findViewById(R.id.indicator);
        final String[] titles = new String[]{
                WordUtil.getString(R.string.user), WordUtil.getString(R.string.video)};
        CommonNavigator commonNavigator = new CommonNavigator(mContext);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {

            @Override
            public int getCount() {
                return titles.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
                simplePagerTitleView.setNormalColor(0xff73737a);
                simplePagerTitleView.setSelectedColor(ContextCompat.getColor(mContext, R.color.textColor));
                simplePagerTitleView.setText(titles[index]);
                simplePagerTitleView.setTextSize(15);
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mViewPager != null) {
                            mViewPager.setCurrentItem(index);
                        }
                    }
                });
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
                linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                linePagerIndicator.setXOffset(DpUtil.dp2px(5));
                linePagerIndicator.setLineHeight(DpUtil.dp2px(2));
                linePagerIndicator.setRoundRadius(DpUtil.dp2px(1));
                linePagerIndicator.setColors(ContextCompat.getColor(mContext, R.color.textColor));
                return linePagerIndicator;
            }

        });
        mIndicator.setNavigator(commonNavigator);
        LinearLayout titleContainer = commonNavigator.getTitleContainer();
        titleContainer.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        titleContainer.setDividerDrawable(new ColorDrawable() {
            @Override
            public int getIntrinsicWidth() {
                return DpUtil.dp2px(60);
            }
        });
        ViewPagerHelper.bind(mIndicator, mViewPager);
//        mNoHistoryTip = findViewById(R.id.no_history_tip);
        mHistoryRecyclerView = findViewById(R.id.recyclerView);
        mHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mHistoryAdapter = new SearchHistoryAdapter(mContext);
        mHistoryAdapter.setActionListener(new SearchHistoryAdapter.ActionListener() {
            @Override
            public void onListSizeChanged(int count) {
//                onHistorySizeChanged(count);
            }

            @Override
            public void onItemClick(String key) {
                if (mEditText != null) {
                    mEditText.setText(key);
                    if (!TextUtils.isEmpty(key)) {
                        mEditText.setSelection(key.length());
                    }
                    doSearch();
                }
            }
        });
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);
//        onHistorySizeChanged(mHistoryAdapter.getHistoryListSize());



    }

    private void doSearch() {
        String key = mEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(key)) {
            if (mSearchGroup != null && mSearchGroup.getVisibility() != View.VISIBLE) {
                mSearchGroup.setVisibility(View.VISIBLE);
            }
            loadPageData(mViewPager.getCurrentItem(), key);
            if (mHistoryAdapter != null) {
                mHistoryAdapter.insertItem(key);
            }
        } else {
            ToastUtil.show(WordUtil.getString(R.string.content_empty));
        }
    }


    private void loadPageData(int position, String key) {
        if (mViewHolders == null) {
            return;
        }
        AbsSearchViewHolder vh = mViewHolders[position];
        if (vh == null) {
            if (mViewList != null && position < mViewList.size()) {
                FrameLayout parent = mViewList.get(position);
                if (parent == null) {
                    return;
                }
                if (position == 0) {
                    mUserViewHolder = new SearchUserViewHolder(mContext, parent);
                    vh = mUserViewHolder;
                } else if (position == 1) {
                    mVideoViewHolder = new SearchVideoViewHolder(mContext, parent);
                    vh = mVideoViewHolder;
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
            vh.setKey(key);
            vh.loadData();
        }
    }


    @Override
    protected void onDestroy() {
        if (mHistoryAdapter != null) {
            mHistoryAdapter.setActionListener(null);
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (imm != null && mEditText != null) {
            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
        super.onBackPressed();
    }

//    private void onHistorySizeChanged(int count) {
//        if (count > 0) {
//            if (mNoHistoryTip != null && mNoHistoryTip.getVisibility() == View.VISIBLE) {
//                mNoHistoryTip.setVisibility(View.INVISIBLE);
//            }
//        } else {
//            if (mNoHistoryTip != null && mNoHistoryTip.getVisibility() != View.VISIBLE) {
//                mNoHistoryTip.setVisibility(View.VISIBLE);
//            }
//        }
//    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_clear) {
            clearEditText();
        } else if (i == R.id.btn_search_history_clear) {
            clearSearchHistory();
        }
    }

    private void clearEditText() {
        String s = mEditText.getText().toString();
        if (TextUtils.isEmpty(s)) {
            return;
        }
        mEditText.requestFocus();
        imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
        mEditText.setText("");
    }

    private void clearSearchHistory() {
        if (mHistoryAdapter != null) {
            mHistoryAdapter.clear();
        }
    }
}
