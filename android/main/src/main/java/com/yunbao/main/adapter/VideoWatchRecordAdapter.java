package com.yunbao.main.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.custom.ItemSlideHelper;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.activity.VideoWatchRecordActivity;
import com.yunbao.main.bean.WatchVideoBean;
import com.yunbao.main.http.MainHttpUtil;

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

public class VideoWatchRecordAdapter extends RefreshAdapter<WatchVideoBean> implements ItemSlideHelper.Callback {

    private View.OnClickListener mOnClickListener;
    private View.OnClickListener mDeleteClickListener;
    private Drawable mCheckedDrawable;
    private Drawable mUnCheckedDrawable;
    private boolean mShowCheck;

    public VideoWatchRecordAdapter(Context context) {
        super(context);
        mCheckedDrawable = ContextCompat.getDrawable(context, R.mipmap.ic_check_1);
        mUnCheckedDrawable = ContextCompat.getDrawable(context, R.mipmap.ic_check_0);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                int position = (int) tag;
                WatchVideoBean bean = mList.get(position);
                if (mShowCheck) {
                    bean.setChecked(!bean.isChecked());
                    notifyItemChanged(position, Constants.PAYLOAD);
                    if (mContext != null) {
                        ((VideoWatchRecordActivity) mContext).setCanDelete(hasChecked());
                    }
                } else {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mList.get(position), position);
                    }
                }

            }
        };

        mDeleteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = (int) v.getTag();
                final WatchVideoBean bean = mList.get(position);
                DialogUitl.showSimpleNoTitDialog(mContext, WordUtil.getString(R.string.a_074), null, new DialogUitl.SimpleCallback2() {

                    @Override
                    public void onCancelClick() {
                        setShowCheck(false);
                        ((VideoWatchRecordActivity) mContext).setCanDelete(hasChecked());
                    }

                    @Override
                    public void onConfirmClick(Dialog dialog, String content) {
                        MainHttpUtil.deleteViewRecord(bean.getId(), new HttpCallback() {
                            @Override
                            public void onSuccess(int code, String msg, String[] info) {
                                if (code == 0) {
                                    mList.remove(position);
                                    notifyDataSetChanged();
                                    if (mContext != null) {
                                        ((VideoWatchRecordActivity) mContext).setCanDelete(hasChecked());
                                    }
//                                    ((VideoWatchRecordActivity) mContext).setCanDelete(false);
//                                    ((VideoWatchRecordActivity) mContext).refreshData();

                                }
                                ToastUtil.show(msg);
                            }
                        });
                    }
                });

            }
        };
    }

    public void toggleShowCheck() {
        mShowCheck = !mShowCheck;
        for (WatchVideoBean bean : mList) {
            bean.setChecked(false);
        }
        notifyDataSetChanged();
    }


    public void setShowCheck(boolean showCheck) {
        if (mShowCheck == showCheck) {
            return;
        }
        mShowCheck = showCheck;
        for (WatchVideoBean bean : mList) {
            bean.setChecked(false);
        }
        notifyDataSetChanged();
    }

    private boolean hasChecked() {
        for (WatchVideoBean bean : mList) {
            if (bean.isChecked()) {
                return true;
            }
        }
        return false;
    }

    public String getCheckedId() {
        StringBuilder sb = null;
        for (WatchVideoBean bean : mList) {
            if (bean.isChecked()) {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append(bean.getId());
                sb.append(",");
            }
        }
        if (sb != null) {
            String s = sb.toString();
            if (s.length() > 1) {
                s = s.substring(0, s.length() - 1);
            }
            return s;
        }
        return null;
    }


    @Override
    public void refreshData(List<WatchVideoBean> list) {
        mShowCheck = false;
        super.refreshData(list);
        if (mContext != null) {
            ((VideoWatchRecordActivity) mContext).setCanDelete(hasChecked());
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_video_watch_record, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) vh).setData(mList.get(i), i, payload);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder vh) {
        ((Vh) vh).recycle();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView.addOnItemTouchListener(new ItemSlideHelper(mContext, this));
    }

    @Override
    public int getHorizontalRange(RecyclerView.ViewHolder vh) {
        return mShowCheck ? 0 : DpUtil.dp2px(60);
    }

    @Override
    public RecyclerView.ViewHolder getChildViewHolder(View childView) {
        if (mRecyclerView != null && childView != null) {
            return mRecyclerView.getChildViewHolder(childView);
        }
        return null;
    }

    @Override
    public View findTargetView(float x, float y) {
        return mRecyclerView.findChildViewUnder(x, y);
    }

    @Override
    public boolean useLeftScroll() {
        return !mShowCheck;
    }

    @Override
    public void onLeftScroll(RecyclerView.ViewHolder holder) {

    }


    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        TextView mTitle;
        TextView mName;
        View mBtnDelete;
        ImageView mImgCheck;

        public Vh(View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mTitle = itemView.findViewById(R.id.title);
            mName = itemView.findViewById(R.id.name);
            itemView.setOnClickListener(mOnClickListener);
            mBtnDelete = itemView.findViewById(R.id.btn_delete);
            mBtnDelete.setOnClickListener(mDeleteClickListener);
            mImgCheck = itemView.findViewById(R.id.img_check);
        }

        void setData(WatchVideoBean bean, int position, Object payload) {
            if (payload == null) {
                itemView.setTag(position);
                mBtnDelete.setTag(position);
                ImgLoader.display(mContext, bean.getThumbs(), mThumb);
                mTitle.setText(bean.getTitle());
                UserBean u = bean.getUserBean();
                if (u != null) {
                    mName.setText(u.getUserNiceName());
                }
                if (mShowCheck) {
                    if (mImgCheck.getVisibility() != View.VISIBLE) {
                        mImgCheck.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (mImgCheck.getVisibility() != View.GONE) {
                        mImgCheck.setVisibility(View.GONE);
                    }
                }
            }
            mImgCheck.setImageDrawable(bean.isChecked() ? mCheckedDrawable : mUnCheckedDrawable);

        }

        void recycle() {
            ImgLoader.clear(mContext, mThumb);
        }
    }
}
