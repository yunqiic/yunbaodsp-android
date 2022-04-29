package com.yunbao.video.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yunbao.common.Constants;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.video.R;
import com.yunbao.video.bean.SimpleFilterBean;

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

public class SimpleFilterAdapter extends RecyclerView.Adapter<SimpleFilterAdapter.Vh> {

    private List<SimpleFilterBean> mList;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private OnItemClickListener<SimpleFilterBean> mOnItemClickListener;
    private int mCheckedPosition;

    public SimpleFilterAdapter(Context context) {
        mList = new ArrayList<>();
        mList.add(new SimpleFilterBean(R.mipmap.icon_filter_orginal, 0, 0, true));
        mList.add(new SimpleFilterBean(R.mipmap.icon_filter_langman, R.mipmap.filter_langman, 1));
        mList.add(new SimpleFilterBean(R.mipmap.icon_filter_qingxin, R.mipmap.filter_qingxin, 2));
        mList.add(new SimpleFilterBean(R.mipmap.icon_filter_weimei, R.mipmap.filter_weimei, 3));
        mList.add(new SimpleFilterBean(R.mipmap.icon_filter_fennen, R.mipmap.filter_fennen, 4));
        mList.add(new SimpleFilterBean(R.mipmap.icon_filter_huaijiu, R.mipmap.filter_huaijiu, 5));
        mList.add(new SimpleFilterBean(R.mipmap.icon_filter_qingliang, R.mipmap.filter_qingliang, 6));
        mList.add(new SimpleFilterBean(R.mipmap.icon_filter_landiao, R.mipmap.filter_landiao, 7));
        mList.add(new SimpleFilterBean(R.mipmap.icon_filter_rixi, R.mipmap.filter_rixi, 8));
        mInflater = LayoutInflater.from(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null) {
                    int position = (int) tag;
                    if (mCheckedPosition == position) {
                        return;
                    }
                    if (mCheckedPosition >= 0 && mCheckedPosition < mList.size()) {
                        mList.get(mCheckedPosition).setChecked(false);
                        notifyItemChanged(mCheckedPosition, Constants.PAYLOAD);
                    }
                    mList.get(position).setChecked(true);
                    notifyItemChanged(position, Constants.PAYLOAD);
                    mCheckedPosition = position;
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mList.get(position), position);
                    }
                }
            }
        };
    }

    public void setOnItemClickListener(OnItemClickListener<SimpleFilterBean> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


    @Override
    public Vh onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_list_filter, parent, false));
    }

    @Override
    public void onBindViewHolder(Vh holder, int position) {

    }

    @Override
    public void onBindViewHolder(Vh vh, int position, List<Object> payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        vh.setData(mList.get(position), position, payload);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    class Vh extends RecyclerView.ViewHolder {

        ImageView mImg;
        ImageView mCheckImg;

        public Vh(View itemView) {
            super(itemView);
            mImg = (ImageView) itemView.findViewById(R.id.img);
            mCheckImg = (ImageView) itemView.findViewById(R.id.check_img);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(SimpleFilterBean bean, int position, Object payload) {
            itemView.setTag(position);
            if (payload == null) {
                mImg.setImageResource(bean.getImgSrc());
            }
            if (bean.isChecked()) {
                if (mCheckImg.getVisibility() != View.VISIBLE) {
                    mCheckImg.setVisibility(View.VISIBLE);
                }
            } else {
                if (mCheckImg.getVisibility() == View.VISIBLE) {
                    mCheckImg.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}
