package com.yunbao.main.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.main.R;
import com.yunbao.common.bean.VideoBean;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class MainHomeHotAdapter extends RefreshAdapter<VideoBean> {

    private View.OnClickListener mOnClickListener;
    private Drawable mDrawableMall;
    private Drawable mDrawablePay;

    public MainHomeHotAdapter(Context context) {
        super(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                int position = (int) tag;
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mList.get(position), position);
                }
            }
        };
        mDrawableMall = ContextCompat.getDrawable(context, R.mipmap.icon_main_video_mall);
        mDrawablePay = ContextCompat.getDrawable(context, R.mipmap.icon_main_video_pay);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_home_hot, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        ((Vh) vh).setData(mList.get(position), position);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder vh) {
        ((Vh) vh).recycle();
    }

    /**
     * 删除视频
     */
    public void deleteVideo(String videoId) {
        if (TextUtils.isEmpty(videoId)) {
            return;
        }
        int position = -1;
        for (int i = 0, size = mList.size(); i < size; i++) {
            if (videoId.equals(mList.get(i).getId())) {
                position = i;
                break;
            }
        }
        if (position >= 0) {
            mList.remove(position);
            notifyDataSetChanged();
        }
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        ImageView mAvatar;
        TextView mName;
        TextView mTitle;
        TextView mCharge;
        ImageView mTypeImg;

        public Vh(View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mAvatar = itemView.findViewById(R.id.avatar);
            mName = itemView.findViewById(R.id.name);
            mTitle = itemView.findViewById(R.id.title);
            mCharge = itemView.findViewById(R.id.charge);
            mTypeImg = (ImageView) itemView.findViewById(R.id.type_img);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(VideoBean bean, int position) {
            itemView.setTag(position);
            ImgLoader.display(mContext, bean.getThumb(), mThumb);
            mTitle.setText(bean.getTitle());
            UserBean u = bean.getUserBean();
            if (u != null) {
                ImgLoader.display(mContext, u.getAvatarThumb(), mAvatar);
                mName.setText(u.getUserNiceName());
            }
            if (!TextUtils.isEmpty(bean.getCoin())) {
                if (!"0".equals(bean.getCoin())) {
                    mCharge.setVisibility(View.VISIBLE);
                    mCharge.setText(bean.getCoin());
                } else {
                    mCharge.setVisibility(View.GONE);
                }
            }
            if (bean.getIsgoods() == 1) {
                mTypeImg.setImageDrawable(mDrawableMall);
            } else if (bean.getIspay() == 1) {
                mTypeImg.setImageDrawable(mDrawablePay);
            } else {
                mTypeImg.setImageDrawable(null);
            }
        }

        void recycle() {
            ImgLoader.clear(mContext, mAvatar);
            ImgLoader.clear(mContext, mThumb);
        }
    }
}
