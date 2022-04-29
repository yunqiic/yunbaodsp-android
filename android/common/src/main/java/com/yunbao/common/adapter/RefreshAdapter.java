package com.yunbao.common.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.ClickUtil;

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
public abstract class RefreshAdapter<T> extends RecyclerView.Adapter {

    protected Context mContext;
    protected List<T> mList;
    protected LayoutInflater mInflater;
    protected RecyclerView mRecyclerView;
    protected OnItemClickListener<T> mOnItemClickListener;

    public RefreshAdapter(Context context) {
        this(context, new ArrayList<T>());
    }

    public RefreshAdapter(Context context, List<T> list) {
        mList = list;
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }


    public void setList(List<T> list) {
        if (mList != null) {
            mList.clear();
            mList.addAll(list);
        }
    }

    public void refreshData(List<T> list) {
        if (mRecyclerView != null && list != null) {
            mList.clear();
            mList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void insertList(List<T> list) {
        if (mRecyclerView != null && mList != null && list != null && list.size() > 0) {
            int p = mList.size();
            mList.addAll(list);
            notifyItemRangeInserted(p, list.size());
        }
    }

    public void clearData() {
        if (mRecyclerView != null && mList != null) {
            mList.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    protected boolean canClick() {
        return ClickUtil.canClick();
    }

    public List<T> getList() {
        return mList;
    }
}
