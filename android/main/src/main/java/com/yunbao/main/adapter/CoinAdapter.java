package com.yunbao.main.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.yunbao.common.Constants;
import com.yunbao.common.bean.CoinBean;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;

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

public class CoinAdapter extends RecyclerView.Adapter {

    private static final int HEAD = -1;
    private String mCoinName;
    private List<CoinBean> mList;
    private String mGiveString;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private OnItemClickListener<CoinBean> mOnItemClickListener;
    private View mHeadView;
    private Drawable mCheckedDrawable;
    private Drawable mUnCheckedDrawable;
    private int mCheckedPosition=-1;
    private boolean mIsPaypal;

    public CoinAdapter(Context context, String coinName) {
        mCoinName = coinName;
        mList = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
        mHeadView = mInflater.inflate(R.layout.item_coin_head, null);
        mHeadView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mGiveString = WordUtil.getString(R.string.coin_give);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                int positon = (int) tag;
                CoinBean bean = mList.get(positon);
                if (mCheckedPosition != positon) {
                    if (mCheckedPosition >= 0 && mCheckedPosition < mList.size()) {
                        mList.get(mCheckedPosition).setChecked(false);
                        notifyItemChanged(mCheckedPosition + 1, Constants.PAYLOAD);
                    }
                    bean.setChecked(true);
                    notifyItemChanged(positon + 1, Constants.PAYLOAD);
                    mCheckedPosition = positon;
                }
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(bean, positon);
                }
            }
        };
        mCheckedDrawable = ContextCompat.getDrawable(context, R.drawable.bg_coin_item_1);
        mUnCheckedDrawable = ContextCompat.getDrawable(context, R.drawable.bg_coin_item_0);
    }


    public void setList(List<CoinBean> list) {
        if (list != null && list.size() > 0) {
            mList.clear();
            if (mCheckedPosition >= 0 && mCheckedPosition < list.size()) {
                list.get(mCheckedPosition).setChecked(true);
            }
            mList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void setOnItemClickListener(OnItemClickListener<CoinBean> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
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
            ViewParent viewParent = mHeadView.getParent();
            if (viewParent != null) {
                ((ViewGroup) viewParent).removeView(mHeadView);
            }
            HeadVh headVh = new HeadVh(mHeadView);
            headVh.setIsRecyclable(false);
            return headVh;
        }
        return new Vh(mInflater.inflate(R.layout.item_coin, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        if (vh instanceof Vh) {
            ((Vh) vh).setData(mList.get(position - 1), position - 1, payload);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size() + 1;
    }

    public void setIsPaypal(boolean isPaypal) {
        if (mIsPaypal == isPaypal) {
            return;
        }
        mIsPaypal = isPaypal;
        notifyDataSetChanged();
    }

    public CoinBean getCheckedBean() {
        if (mList != null && mList.size() > 0 && mCheckedPosition >= 0) {
            return mList.get(mCheckedPosition);
        }
        return null;
    }


    class HeadVh extends RecyclerView.ViewHolder {

        public HeadVh(View itemView) {
            super(itemView);
        }
    }

    class Vh extends RecyclerView.ViewHolder {

        TextView mCoin;
        TextView mMoney;
        TextView mGive;
        View mGiveGroup;
        View mBg;

        public Vh(View itemView) {
            super(itemView);
            mCoin = itemView.findViewById(R.id.coin);
            mMoney = itemView.findViewById(R.id.money);
            mGive = itemView.findViewById(R.id.give);
            mGiveGroup = itemView.findViewById(R.id.give_group);
            mBg = itemView.findViewById(R.id.bg);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(CoinBean bean, int position, Object payload) {
            if (payload == null) {
                itemView.setTag(position);
                mCoin.setText(mIsPaypal ? bean.getCoinPaypal() : bean.getCoin());
                mMoney.setText(StringUtil.contact(mIsPaypal ? "$" : "￥", bean.getMoney()));
                if (!"0".equals(bean.getGive())) {
                    if (mGiveGroup.getVisibility() != View.VISIBLE) {
                        mGiveGroup.setVisibility(View.VISIBLE);
                    }
                    mGive.setText(mGiveString + bean.getGive() + mCoinName);
                } else {
                    if (mGiveGroup.getVisibility() == View.VISIBLE) {
                        mGiveGroup.setVisibility(View.INVISIBLE);
                    }
                }
            }
            mBg.setBackground(bean.isChecked() ? mCheckedDrawable : mUnCheckedDrawable);
        }
    }


    public View getHeadView(){
        return mHeadView;
    }

}
