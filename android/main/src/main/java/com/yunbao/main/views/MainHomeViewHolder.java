package com.yunbao.main.views;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.yunbao.common.adapter.ViewPagerAdapter;
import com.yunbao.common.custom.ScaleTransitionPagerTitleView;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.video.bean.ClassBean;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
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
public class MainHomeViewHolder extends AbsMainViewHolder {

    private ViewPager mViewPager;
    private List<FrameLayout> mViewList;
    private MagicIndicator mIndicator;
    private AbsMainViewHolder[] mViewHolders;
    private MainHomeRecommendViewHolder mRecommendViewHolder;//推荐
    private MainHomeHotViewHolder mHotViewHolder;//热门
    private List<ClassBean> mClassBeanList;
    private CommonNavigatorAdapter mNavigatorAdapter;
    private ViewPagerAdapter mViewPagerAdapter;
    private boolean mLoadSuc;

    public MainHomeViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_home;
    }

    @Override
    public void init() {
        setStatusHeight();
        mViewList = new ArrayList<>();
        mViewHolders = new AbsMainViewHolder[2];
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mIndicator = (MagicIndicator) findViewById(R.id.indicator);
        initData();
    }

    private void initData() {
        mClassBeanList = new ArrayList<>();
        mClassBeanList.add(new ClassBean(ClassBean.RECOMMEND_ID, WordUtil.getString(R.string.recommend)));
        mClassBeanList.add(new ClassBean(ClassBean.HOT_ID, WordUtil.getString(R.string.hot)));
        loadView();
        mLoadSuc = true;
    }

    private void loadView() {
        int size = mClassBeanList.size();
        for (int i = 0; i < size; i++) {
            FrameLayout frameLayout = new FrameLayout(mContext);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mViewList.add(frameLayout);
        }
        if (size > 1) {
            mViewPager.setOffscreenPageLimit(size - 1);
        }
        if (mViewPagerAdapter == null) {
            mViewPagerAdapter = new ViewPagerAdapter(mViewList);
        }
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                loadPageData(position);
                for (int i = 0, length = mViewHolders.length; i < length; i++) {
                    AbsMainViewHolder vh = mViewHolders[i];
                    if (vh != null) {
                        vh.setShowed(position == i);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (mNavigatorAdapter == null) {
            mNavigatorAdapter = new CommonNavigatorAdapter() {

                @Override
                public int getCount() {
                    return mClassBeanList.size();
                }

                @Override
                public IPagerTitleView getTitleView(Context context, final int index) {
                    SimplePagerTitleView simplePagerTitleView = new ScaleTransitionPagerTitleView(context);
                    simplePagerTitleView.setNormalColor(0xccffffff);
                    simplePagerTitleView.setSelectedColor(ContextCompat.getColor(mContext, R.color.textColor));
                    simplePagerTitleView.setText(mClassBeanList.get(index).getName());
                    simplePagerTitleView.setTextSize(17);
                    simplePagerTitleView.getPaint().setFakeBoldText(true);
                    simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mViewPager != null) {
                                mViewPager.setCurrentItem(index, false);
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
                    linePagerIndicator.setRoundRadius(DpUtil.dp2px(2));
                    linePagerIndicator.setColors(ContextCompat.getColor(mContext, R.color.white));
                    return linePagerIndicator;
                }
            };
            CommonNavigator commonNavigator = new CommonNavigator(mContext);
            commonNavigator.setAdapter(mNavigatorAdapter);
            commonNavigator.setAdjustMode(false);

            mIndicator.setNavigator(commonNavigator);
            ViewPagerHelper.bind(mIndicator, mViewPager);
        }
        loadPageData(0);
    }

    private void loadPageData(int position) {
        if (mViewHolders == null) {
            return;
        }
        AbsMainViewHolder vh = mViewHolders[position];
        if (vh == null) {
            if (mViewList != null) {
                FrameLayout parent = mViewList.get(position);
                if (parent == null) {
                    return;
                }
                if (position == 0) {
                    mRecommendViewHolder = new MainHomeRecommendViewHolder(mContext, parent);
                    vh = mRecommendViewHolder;
                } else if (position == 1) {
                    mHotViewHolder = new MainHomeHotViewHolder(mContext, parent);
                    vh = mHotViewHolder;
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
            vh.loadData();
        }
    }


    @Override
    public void loadData() {
        if (mViewPager != null && mLoadSuc) {
            loadPageData(mViewPager.getCurrentItem());
        }
    }

    @Override
    public void setShowed(boolean showed) {
        super.setShowed(showed);
        if (mViewHolders != null && mViewPager != null) {
            mViewHolders[mViewPager.getCurrentItem()].setShowed(showed);
        }
    }

    /**
     * 刷新推荐
     */
    public void refreshRecommend() {
        if (mRecommendViewHolder != null) {
            mRecommendViewHolder.refreshRecommend();
        }
    }


    /**
     * 从播放列表中删除视频
     */
    public void deleteVideoFromPlay(String videoId) {
        if (mRecommendViewHolder != null) {
            mRecommendViewHolder.deleteVideoFromPlay(videoId);
        }
    }


    public void setCurrentItem(int position) {
        if (mViewPager != null && mViewPager.getCurrentItem() != position) {
            mViewPager.setCurrentItem(position, false);
        }
    }


    public int getCurrentItem() {
        if (mViewPager != null) {
            return mViewPager.getCurrentItem();
        }
        return 0;
    }


    public boolean isVideoPub() {
        if (mRecommendViewHolder != null) {
            return mRecommendViewHolder.isVideoPub();
        }
        return false;
    }

}
