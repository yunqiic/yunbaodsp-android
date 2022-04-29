package com.yunbao.main.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yunbao.common.utils.SpUtil;
import com.yunbao.main.R;

import java.util.LinkedList;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class SearchHistoryAdapter extends RecyclerView.Adapter {

    private static final int HEAD = -1;
    private LinkedList<String> mList;
    private LayoutInflater mInflater;
    private View.OnClickListener mDeleteOnClickListener;
    private View.OnClickListener mItemClickListener;
    private ActionListener mActionListener;

    public SearchHistoryAdapter(Context context) {
        mList = new LinkedList<>();
        String s = SpUtil.getInstance().getStringValue(SpUtil.SEARCH_HISTORY);
        if (!TextUtils.isEmpty(s)) {
            String[] arr = s.split("/");
            for (int i = 0, length = arr.length; i < length; i++) {
                mList.add(arr[i]);
            }
        }
        mInflater = LayoutInflater.from(context);
        mDeleteOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                int position = (int) tag;
                mList.remove(position - 1);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());
                updateSearchHistory();
            }
        };
        mItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mActionListener != null) {
                    mActionListener.onItemClick((String) tag);
                }
            }
        };
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEAD;
        }
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HEAD) {
            return new HeadVh(mInflater.inflate(R.layout.item_empty_head, parent, false));
        }
        return new Vh(mInflater.inflate(R.layout.item_search_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        if (vh instanceof Vh) {
            ((Vh) vh).setData(mList.get(position - 1), position);
        }
    }


    public void insertItem(String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        if (mList.contains(key)) {
            return;
        }
        if (mList.size() >= 6) {
            mList.removeLast();
        }
        mList.addFirst(key);
        notifyDataSetChanged();
        updateSearchHistory();
    }

    public void clear() {
        if (mList == null) {
            return;
        }
        mList.clear();
        notifyDataSetChanged();
        updateSearchHistory();
    }

    /**
     * 更新历史记录
     */
    private void updateSearchHistory() {
        if (mList == null) {
            return;
        }
        int count = mList.size();
        if (mActionListener != null) {
            mActionListener.onListSizeChanged(count);
        }
        if (count > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                sb.append(mList.get(i));
                if (i < count - 1) {
                    sb.append("/");
                }
            }
            SpUtil.getInstance().setStringValue(SpUtil.SEARCH_HISTORY, sb.toString());
        } else {
            SpUtil.getInstance().setStringValue(SpUtil.SEARCH_HISTORY, "");
        }
    }

    public int getHistoryListSize() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return mList.size() + 1;
    }

    class HeadVh extends RecyclerView.ViewHolder {

        public HeadVh(View itemView) {
            super(itemView);
        }
    }

    class Vh extends RecyclerView.ViewHolder {

        TextView mText;
        View mBtnClear;

        public Vh(View itemView) {
            super(itemView);
            mText = itemView.findViewById(R.id.text);
            mBtnClear = itemView.findViewById(R.id.btn_clear);
            mBtnClear.setOnClickListener(mDeleteOnClickListener);
            itemView.setOnClickListener(mItemClickListener);
        }

        void setData(String s, int position) {
            mBtnClear.setTag(position);
            itemView.setTag(s);
            mText.setText("#" + s);
        }
    }

    public interface ActionListener {
        void onListSizeChanged(int count);

        void onItemClick(String key);
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }
}
