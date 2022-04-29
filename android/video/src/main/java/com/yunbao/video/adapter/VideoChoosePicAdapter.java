package com.yunbao.video.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.util.bean.ChatChooseImageBean;
import com.yunbao.video.R;

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
public class VideoChoosePicAdapter extends RefreshAdapter<ChatChooseImageBean> {

    private static final int MAX_COUNT = 10;
    private static final int MIN_COUNT = 3;
    private View.OnClickListener mOnClickListener;
    private Drawable mCheckedDrawable;
    private Drawable mUnCheckedDrawable;
    private String mStringTipMax;
    private String mStringTipMin;
    private ArrayList<String> mCheckedList;
    private ActionListener mActionListener;


    public VideoChoosePicAdapter(Context context, List<ChatChooseImageBean> list) {
        super(context, list);
        mCheckedDrawable = ContextCompat.getDrawable(context, R.mipmap.icon_checked);
        mUnCheckedDrawable = ContextCompat.getDrawable(context, R.mipmap.icon_checked_none);
        mStringTipMax = WordUtil.getString(R.string.choose_pic_tip_max);
        mStringTipMin = WordUtil.getString(R.string.choose_pic_tip_min);
        mCheckedList = new ArrayList<>();
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                ChatChooseImageBean bean = mList.get(position);
                if (bean.isChecked()) {
                    bean.setChecked(false);
                    notifyItemChanged(position, Constants.PAYLOAD);
                    mCheckedList.remove(bean.getImageFile().getAbsolutePath());
                    if (mActionListener != null) {
                        mActionListener.onCheckedCountChanged(mCheckedList.size());
                    }
                } else {
                    if (mCheckedList.size() >= MAX_COUNT) {
                        ToastUtil.show(String.format(mStringTipMax, MAX_COUNT));
                        return;
                    }
                    bean.setChecked(true);
                    notifyItemChanged(position, Constants.PAYLOAD);
                    mCheckedList.add(bean.getImageFile().getAbsolutePath());
                    if (mActionListener != null) {
                        mActionListener.onCheckedCountChanged(mCheckedList.size());
                    }
                }

            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_choose_img, viewGroup, false));
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

        ImageView mCover;
        ImageView mImg;

        public Vh(final View itemView) {
            super(itemView);
            mCover = itemView.findViewById(R.id.cover);
            mImg = itemView.findViewById(R.id.img);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(ChatChooseImageBean bean, int position, Object payload) {
            if (payload == null) {
                itemView.setTag(position);
                ImgLoader.display(mContext, bean.getImageFile(), mCover);
            }
            mImg.setImageDrawable(bean.isChecked() ? mCheckedDrawable : mUnCheckedDrawable);
        }
    }

    public void refresh() {
        if (mList != null && mList.size() > 0) {
            for (ChatChooseImageBean bean : mList) {
                bean.setChecked(false);
            }
            notifyDataSetChanged();
            if (mCheckedList == null) {
                mCheckedList = new ArrayList<>();
            }
            mCheckedList.clear();
            if (mActionListener != null) {
                mActionListener.onCheckedCountChanged(0);
            }
        }
    }

    public ArrayList<String> getImagePathList() {
        if (mCheckedList.size() < MIN_COUNT) {
            ToastUtil.show(String.format(mStringTipMin, MIN_COUNT));
            return null;
        }
        return mCheckedList;
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public interface ActionListener {
        void onCheckedCountChanged(int checkedCount);
    }

}
