package com.yunbao.common.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.R;
import com.yunbao.common.bean.GiftBean;


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

public class GiftPagerAdapter extends PagerAdapter {

    private Context mContext;
    private List<RecyclerView> mViewList;
    private static final int GIFT_COUNT = 10;//每页10个礼物
    private int mPage = -1;
    private ActionListener mActionListener;

    public GiftPagerAdapter(Context context, List<? extends GiftBean> giftList) {
        mContext=context;
        mViewList = new ArrayList<>();
        int fromIndex = 0;
        int size = giftList.size();
        int pageCount = size / GIFT_COUNT;
        if (size % GIFT_COUNT > 0) {
            pageCount++;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        String coinName = CommonAppConfig.getInstance().getCoinName();
        GiftAdapter.ActionListener actionListener = new GiftAdapter.ActionListener() {
            @Override
            public void onCancel() {
                if (mPage >= 0 && mPage < mViewList.size()) {
                    GiftAdapter adapter = (GiftAdapter) mViewList.get(mPage).getAdapter();
                    if (adapter != null) {
                        adapter.cancelChecked();
                    }
                }
            }

            @Override
            public void onItemChecked(GiftBean bean) {
                mPage = bean.getPage();
                if (mActionListener != null) {
                    mActionListener.onItemChecked(bean);
                }
            }
        };
        for (int i = 0; i < pageCount; i++) {
            RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.view_gift_page, null, false);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(context, 5, GridLayoutManager.VERTICAL, false));
            int endIndex = fromIndex + GIFT_COUNT;
            if (endIndex > size) {
                endIndex = size;
            }
            List<GiftBean> list = new ArrayList<>();
            for (int j = fromIndex; j < endIndex; j++) {
                GiftBean bean = giftList.get(j);
                bean.setPage(i);
                list.add(bean);
            }
            GiftAdapter adapter = new GiftAdapter(mContext,inflater, list, coinName);
            adapter.setActionListener(actionListener);
            recyclerView.setAdapter(adapter);
            mViewList.add(recyclerView);
            fromIndex = endIndex;
        }
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mViewList.get(position);
        container.addView(view);
        return view;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViewList.get(position));
    }

    public void release() {
        if (mViewList != null) {
            for (RecyclerView recyclerView : mViewList) {
                GiftAdapter adapter = (GiftAdapter) recyclerView.getAdapter();
                if (adapter != null) {
                    adapter.release();
                }
            }
        }
    }

    public interface ActionListener {
        void onItemChecked(GiftBean bean);
    }


    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public void reducePackageCount(int giftId, int count) {
        if (mViewList != null) {
            for (RecyclerView recyclerView : mViewList) {
                GiftAdapter adapter = (GiftAdapter) recyclerView.getAdapter();
                if (adapter != null && adapter.reducePackageCount(giftId, count)) {
                    return;
                }
            }
        }
    }
}
