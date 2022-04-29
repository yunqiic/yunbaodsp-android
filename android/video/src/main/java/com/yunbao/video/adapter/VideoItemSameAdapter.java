package com.yunbao.video.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.video.R;
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
public class VideoItemSameAdapter extends RefreshAdapter<VideoBean> {


    private View.OnClickListener mOnClickListener;

    public VideoItemSameAdapter(Context context) {
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
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_same_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        if(vh instanceof Vh){
            ((Vh) vh).setData(mList.get(position), position);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder vh) {
        ((Vh) vh).recycle();
    }




    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        View mFirst;
        TextView mCharge;

        public Vh(View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mFirst = itemView.findViewById(R.id.tv_first);
            mCharge = itemView.findViewById(R.id.charge);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(VideoBean bean, int position) {
            itemView.setTag(position);
            ImgLoader.display(mContext, bean.getThumb(), mThumb);
            if (!TextUtils.isEmpty(bean.getCoin())){
                if (!"0".equals(bean.getCoin())) {
                    mCharge.setVisibility(View.VISIBLE);
                    mCharge.setText(bean.getCoin());
                } else {
                    mCharge.setVisibility(View.GONE);
                }
            }
            /*if (position==0){
                if (mFirst!=null){
                    mFirst.setVisibility(View.VISIBLE);
                }
            }else {
                if (mFirst!=null){
                    mFirst.setVisibility(View.GONE);
                }
            }*/

        }
        void recycle() {
            ImgLoader.clear(mContext, mThumb);
        }
    }

}
