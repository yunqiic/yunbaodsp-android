package com.yunbao.common.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yunbao.common.R;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.Vh> {

    private LayoutInflater mInflater;
    private ActionListener mActionListener;
    private int mPageCount;
    private LinearLayoutManager mLayoutManager;
    private int mCurPosition;
    private View.OnLongClickListener mLongClickListener;


    public ImagePreviewAdapter(final Context context, int pageCount) {
        mPageCount = pageCount;
        mInflater = LayoutInflater.from(context);
        mLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Object tag = v.getTag(R.id.img);
                if (tag == null) {
                    return false;
                }
                int position = (int) tag;
                if (mActionListener != null) {
                    mActionListener.saveImage(position);
                }
                return true;
            }
        };
    }


    @NonNull
    @Override
    public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_preview_img, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position) {
        vh.setData(position);
    }

    @Override
    public int getItemCount() {
        return mPageCount;
    }


    class Vh extends RecyclerView.ViewHolder {

        ImageView mImg;

        public Vh(View itemView) {
            super(itemView);
            mImg = (ImageView) itemView;
            mImg.setOnLongClickListener(mLongClickListener);
        }

        void setData(int position) {
            mImg.setTag(R.id.img, position);
            if (mActionListener != null) {
                mActionListener.loadImage(mImg, position);
            }
        }
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public void setCurPosition(int curPosition) {
        mCurPosition = curPosition;
        if (mActionListener != null) {
            mActionListener.onPageChanged(curPosition);
        }
    }

    public int getCurPosition() {
        return mCurPosition;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(recyclerView);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int position = mLayoutManager.findFirstCompletelyVisibleItemPosition();
                if (position >= 0 && mCurPosition != position) {
                    mCurPosition = position;
                    if (mActionListener != null) {
                        mActionListener.onPageChanged(position);
                    }
                }
            }
        });
    }


    public interface ActionListener {
        void onPageChanged(int position);

        void loadImage(ImageView imageView, int position);

        void saveImage(int position);
    }
}
