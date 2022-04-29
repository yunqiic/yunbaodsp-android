package com.yunbao.common.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.Constants;
import com.yunbao.common.R;
import com.yunbao.common.bean.BackPackGiftBean;
import com.yunbao.common.bean.GiftBean;
import com.yunbao.common.custom.GiftMarkView;
import com.yunbao.common.custom.MyRadioButton;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.WordUtil;


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
public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.Vh> {

    private Context mContext;
    private List<GiftBean> mList;
    private LayoutInflater mInflater;
    private String mCoinName;
    private View.OnClickListener mOnClickListener;
    private ActionListener mActionListener;
    private int mCheckedPosition = -1;
    private ScaleAnimation mAnimation;
    private View mAnimView;
    private String mGe;
    //    private Drawable mDrawableHot;
//    private Drawable mDrawableGuard;
//    private Drawable mDrawableLuck;
    private Drawable mDrawableTu;
    private Drawable mDrawableHao;
    private Drawable mDrawableShow;

    public GiftAdapter(Context context, LayoutInflater inflater, List<GiftBean> list, String coinName) {
        mContext = context;
        mInflater = inflater;
        mList = list;
        mCoinName = coinName;
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null) {
                    int position = (int) tag;
                    GiftBean bean = mList.get(position);
                    if (!bean.isChecked()) {
                        if (!cancelChecked()) {
                            if (mActionListener != null) {
                                mActionListener.onCancel();
                            }
                        }
                        bean.setChecked(true);
                        notifyItemChanged(position, Constants.PAYLOAD);
                        View view = bean.getView();
                        if (view != null) {
                            view.startAnimation(mAnimation);
                            mAnimView = view;
                        }
                        mCheckedPosition = position;
                        if (mActionListener != null) {
                            mActionListener.onItemChecked(bean);
                        }
                    }
                }
            }
        };
        mAnimation = new ScaleAnimation(0.9f, 1.1f, 0.9f, 1.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimation.setDuration(400);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setRepeatCount(-1);
        mGe = WordUtil.getString(R.string.ge);
//        mDrawableHot = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_gift_hot);
//        mDrawableGuard = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_gift_guard);
//        mDrawableLuck = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_gift_luck);
        mDrawableTu = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_gift_tu);
        mDrawableHao = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_gift_hao);
        mDrawableShow = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_gift_shou);
    }

    @NonNull
    @Override
    public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_live_gift, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position, @NonNull List<Object> payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        vh.setData(mList.get(position), position, payload);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    /**
     * 取消选中
     */
    public boolean cancelChecked() {
        if (mCheckedPosition >= 0 && mCheckedPosition < mList.size()) {
            GiftBean bean = mList.get(mCheckedPosition);
            if (bean.isChecked()) {
                View view = bean.getView();
                if (mAnimView == view) {
                    mAnimView.clearAnimation();
                } else {
                    if (view != null) {
                        view.clearAnimation();
                    }
                }
                mAnimView = null;
                bean.setChecked(false);
                notifyItemChanged(mCheckedPosition, Constants.PAYLOAD);
            }
            mCheckedPosition = -1;
            return true;
        }
        return false;
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public void release() {
        if (mAnimView != null) {
            mAnimView.clearAnimation();
        }
        if (mList != null) {
            mList.clear();
        }
        mOnClickListener = null;
        mActionListener = null;
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mIcon;
        TextView mName;
        TextView mPrice;
        MyRadioButton mRadioButton;
        ImageView mMarkLeft;
        ImageView mMarkRight;

        public Vh(View itemView) {
            super(itemView);
            mMarkLeft = itemView.findViewById(R.id.mark_1);
            mMarkRight = itemView.findViewById(R.id.mark_2);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mName = (TextView) itemView.findViewById(R.id.name);
            mPrice = (TextView) itemView.findViewById(R.id.price);
            mRadioButton = (MyRadioButton) itemView.findViewById(R.id.radioButton);
            mRadioButton.setOnClickListener(mOnClickListener);
        }

        void setData(GiftBean bean, int position, Object payload) {
            if (payload == null) {
                ImgLoader.display(mContext, bean.getIcon(), mIcon);
                bean.setView(mIcon);
                mName.setText(bean.getName());
                int mark = bean.getMark();
                if (bean.getType() == GiftBean.TYPE_DELUXE) {
//                    if (mark == GiftBean.MARK_HOT) {
//                        mMarkLeft.setImageDrawable(mDrawableHot);
//                        mMarkRight.setImageDrawable(mDrawableHao);
//                    } else
                    if (mark == GiftBean.MARK_GUARD) {
                        mMarkLeft.setImageDrawable(mDrawableShow);
                        mMarkRight.setImageDrawable(mDrawableHao);
                    }
//                    else if (mark == GiftBean.MARK_LUCK) {
//                        mMarkLeft.setImageDrawable(mDrawableLuck);
//                        mMarkRight.setImageDrawable(mDrawableHao);
//                    }
                    else {
                        mMarkLeft.setImageDrawable(null);
                        mMarkRight.setImageDrawable(mDrawableHao);
                    }

                } else if (bean.getType() == GiftBean.TYPE_DRAW) {
//                    if (mark == GiftBean.MARK_HOT) {
//                        mMarkLeft.setImageDrawable(mDrawableHot);
//                        mMarkRight.setImageDrawable(mDrawableTu);
//                    } else


                    if (mark == GiftBean.MARK_GUARD) {
                        mMarkLeft.setImageDrawable(mDrawableShow);
                        mMarkRight.setImageDrawable(mDrawableTu);
                    }
//                    else if (mark == GiftBean.MARK_LUCK) {
//                        mMarkLeft.setImageDrawable(mDrawableLuck);
//                        mMarkRight.setImageDrawable(mDrawableTu);
//                    }
                    else {
                        mMarkLeft.setImageDrawable(null);
                        mMarkRight.setImageDrawable(mDrawableTu);
                    }

                } else {
//                    if (mark == GiftBean.MARK_HOT) {
//                        mMarkLeft.setImageDrawable(mDrawableHot);
//                        mMarkRight.setImageDrawable(null);
//                    } else
                    if (mark == GiftBean.MARK_GUARD) {
                        mMarkLeft.setImageDrawable(mDrawableShow);
                        mMarkRight.setImageDrawable(null);
                    }
//                    else if (mark == GiftBean.MARK_LUCK) {
//                        mMarkLeft.setImageDrawable(mDrawableLuck);
//                        mMarkRight.setImageDrawable(null);
//                    }
                    else {
                        mMarkLeft.setImageDrawable(null);
                        mMarkRight.setImageDrawable(null);
                    }

                }
            }
            if (bean instanceof BackPackGiftBean) {
                mPrice.setText(StringUtil.contact(String.valueOf(((BackPackGiftBean) bean).getNums()), mGe));
            } else {
                mPrice.setText(StringUtil.contact(bean.getPrice(), mCoinName));
            }
            mRadioButton.setTag(position);
            mRadioButton.doChecked(bean.isChecked());
        }
    }

    public interface ActionListener {
        void onCancel();

        void onItemChecked(GiftBean bean);
    }

    public boolean reducePackageCount(int giftId, int count) {
        for (int i = 0, size = mList.size(); i < size; i++) {
            GiftBean bean = mList.get(i);
            if (bean.getId() == giftId) {
                if (bean instanceof BackPackGiftBean) {
                    BackPackGiftBean backBean = (BackPackGiftBean) bean;
                    int num = backBean.getNums();
                    num -= count;
                    if (num < 0) {
                        num = 0;
                    }
                    backBean.setNums(num);
                    notifyItemChanged(i, Constants.PAYLOAD);
                    return true;
                }
            }
        }
        return false;
    }
}
