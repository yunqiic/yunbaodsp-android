package com.yunbao.common.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yunbao.common.R;
import com.yunbao.common.interfaces.OnItemClickListener;


// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class GiftCountAdapter extends RecyclerView.Adapter<GiftCountAdapter.Vh> {

    private String[] mArray;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private OnItemClickListener<String> mOnItemClickListener;

    public GiftCountAdapter(Context context) {
        mArray = new String[]{"1", "10", "66", "88", "100", "520", "1314"};
        mInflater = LayoutInflater.from(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick((String) tag, 0);
                }
            }
        };
    }

    public void setOnItemClickListener(OnItemClickListener<String> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_live_gift_count, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position) {
        vh.setData(mArray[position]);
    }

    @Override
    public int getItemCount() {
        return mArray.length;
    }

    class Vh extends RecyclerView.ViewHolder {

        public Vh(View itemView) {
            super(itemView);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(String text) {
            itemView.setTag(text);
            ((TextView) itemView).setText(text);
        }
    }
}
