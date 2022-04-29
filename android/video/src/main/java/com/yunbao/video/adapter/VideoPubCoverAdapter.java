package com.yunbao.video.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.video.R;
import com.yunbao.video.bean.VideoPubCoverBean;

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
public class VideoPubCoverAdapter extends RefreshAdapter<VideoPubCoverBean> {

    private View.OnClickListener mOnClickListener;
    private int mCheckedPosition;

    public VideoPubCoverAdapter(Context context, List<VideoPubCoverBean> list) {
        super(context, list);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                if (mCheckedPosition == position) {
                    return;
                }
                mList.get(position).setChecked(true);
                notifyItemChanged(position, Constants.PAYLOAD);
                mList.get(mCheckedPosition).setChecked(false);
                notifyItemChanged(mCheckedPosition, Constants.PAYLOAD);
                mCheckedPosition = position;
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mList.get(position), position);
                }
            }
        };
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_video_pub_cover, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) vh).setData(mList.get(position), position, payload);
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mImg;
        View mCheck;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mImg = itemView.findViewById(R.id.img);
            mCheck = itemView.findViewById(R.id.check);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(VideoPubCoverBean bean, int position, Object payload) {
            if (payload == null) {
                itemView.setTag(position);
                mImg.setImageBitmap(bean.getBitmap());
            }
            if (bean.isChecked()) {
                if (mCheck.getVisibility() != View.VISIBLE) {
                    mCheck.setVisibility(View.VISIBLE);
                }
            } else {
                if (mCheck.getVisibility() == View.VISIBLE) {
                    mCheck.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}
