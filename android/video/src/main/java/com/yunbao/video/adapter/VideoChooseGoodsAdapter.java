package com.yunbao.video.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.bean.GoodsBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.video.R;

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
public class VideoChooseGoodsAdapter extends RefreshAdapter<GoodsBean> {

    private View.OnClickListener mOnClickListener;
    private String mMoneySymbol;
    private int mCheckPosition = -1;

    public VideoChooseGoodsAdapter(Context context) {
        super(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                if (position == mCheckPosition) {
                    GoodsBean bean = mList.get(position);
                    bean.setAdded(false);
                    notifyItemChanged(position, Constants.PAYLOAD);
                    mCheckPosition = -1;
                } else {
                    if (mCheckPosition >= 0 && mCheckPosition < mList.size()) {
                        GoodsBean checkBean = mList.get(mCheckPosition);
                        checkBean.setAdded(false);
                        notifyItemChanged(mCheckPosition, Constants.PAYLOAD);
                    }
                    GoodsBean bean = mList.get(position);
                    bean.setAdded(true);
                    notifyItemChanged(position, Constants.PAYLOAD);
                    mCheckPosition = position;
                }

            }
        };
        mMoneySymbol = WordUtil.getString(R.string.money_symbol);
    }

    public GoodsBean getCheckedGoodsBean() {
        if (mCheckPosition >= 0 && mCheckPosition < mList.size()) {
            return mList.get(mCheckPosition);
        }
        return null;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_video_chooose_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {

    }


    @Override
    public void refreshData(List<GoodsBean> list) {
        if (list != null && list.size() > 0) {
            for (int i = 0, size = list.size(); i < size; i++) {
                if (list.get(i).isAdded()) {
                    mCheckPosition = i;
                    break;
                }
            }
        }
        super.refreshData(list);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) vh).setData(mList.get(position), position, payload);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder vh) {
        ((Vh) vh).recycle();
    }


    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        TextView mTitle;
        TextView mPriceNow;
        TextView mPriceOrigin;
        View mCheck;


        public Vh(View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mTitle = itemView.findViewById(R.id.title);
            mPriceNow = itemView.findViewById(R.id.price_now);
            mPriceOrigin = itemView.findViewById(R.id.price_origin);
            mPriceOrigin.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            mCheck = itemView.findViewById(R.id.check);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(GoodsBean bean, int position, Object payload) {
            if (payload == null) {
                itemView.setTag(position);
                ImgLoader.display(mContext, bean.getThumb(), mThumb);
                mTitle.setText(bean.getName());
                mPriceNow.setText(StringUtil.contact(mMoneySymbol, bean.getPriceNow()));
                mPriceOrigin.setText(StringUtil.contact(mMoneySymbol, bean.getPriceOrigin()));
            }
            if (bean.isAdded()) {
                if (mCheck.getVisibility() != View.VISIBLE) {
                    mCheck.setVisibility(View.VISIBLE);
                }
            } else {
                if (mCheck.getVisibility() == View.VISIBLE) {
                    mCheck.setVisibility(View.INVISIBLE);
                }
            }
        }

        void recycle() {
            ImgLoader.clear(mContext, mThumb);
        }
    }
}