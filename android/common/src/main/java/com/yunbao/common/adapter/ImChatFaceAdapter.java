package com.yunbao.common.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yunbao.common.utils.FaceUtil;
import com.yunbao.common.R;
import com.yunbao.common.interfaces.OnFaceClickListener;

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

public class ImChatFaceAdapter extends RecyclerView.Adapter<ImChatFaceAdapter.Vh> {

    private List<String> mList;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;

    public ImChatFaceAdapter(List<String> list, LayoutInflater inflater, final OnFaceClickListener onFaceClickListener) {
        mList = list;
        mInflater = inflater;
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && onFaceClickListener != null) {
                    String str = (String) v.getTag();
                    if (!TextUtils.isEmpty(str)) {
                        if ("<".equals(str)) {
                            onFaceClickListener.onFaceDeleteClick();
                        } else {
                            onFaceClickListener.onFaceClick(str, v.getId());
                        }
                    }
                }
            }
        };
    }

    @Override
    public Vh onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_list_face, parent, false));
    }

    @Override
    public void onBindViewHolder(Vh vh, int position) {
        vh.setData(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mImageView;

        public Vh(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView;
            mImageView.setOnClickListener(mOnClickListener);
        }

        void setData(String str) {
            mImageView.setTag(str);
            if (!TextUtils.isEmpty(str)) {
                if ("<".equals(str)) {
                    mImageView.setImageResource(R.mipmap.icon_face_delete);
                } else {
                    int imgRes = FaceUtil.getFaceImageRes(str);
                    mImageView.setId(imgRes);
                    mImageView.setImageResource(imgRes);
                }
            }else{
                mImageView.setClickable(false);
            }
        }
    }
}
