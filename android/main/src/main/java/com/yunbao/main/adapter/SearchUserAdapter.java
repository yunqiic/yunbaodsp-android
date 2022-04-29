package com.yunbao.main.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.custom.MyRadioButton;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;

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

public class SearchUserAdapter extends RefreshAdapter<UserBean> {

    private View.OnClickListener mFollowClickListener;
    private View.OnClickListener mClickListener;
    private String mFollow;
    private String mFollowing;
    private String mFanString;

    public SearchUserAdapter(Context context) {
        super(context);
        mFollow = WordUtil.getString(R.string.follow);
        mFollowing = WordUtil.getString(R.string.following);
        mFanString = WordUtil.getString(R.string.fans_2);
        mFollowClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CommonAppConfig.getInstance().isLogin()){
                    RouteUtil.forwardLogin(mContext);
                    return;
                }
                if (!canClick()) {
                    return;
                }
                Object tag = v.getTag();
                if (tag != null) {
                    UserBean bean = (UserBean) tag;
                    CommonHttpUtil.setAttention(bean.getId(), null);
                }
            }
        };
        mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canClick()) {
                    return;
                }
                Object tag = v.getTag();
                if (tag != null && mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick((UserBean) tag, 0);
                }
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_search_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) vh).setData(mList.get(position), position, payload);
    }


    public void updateItem(String id, int attention) {
        if (TextUtils.isEmpty(id)) {
            return;
        }
        for (int i = 0, size = mList.size(); i < size; i++) {
            UserBean bean = mList.get(i);
            if (bean != null && id.equals(bean.getId())) {
                bean.setIsattention(attention);
                notifyItemChanged(i, Constants.PAYLOAD);
                break;
            }
        }
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mAvatar;
        TextView mName;
        TextView mID;
        TextView mFansNum;
        TextView mSign;
        MyRadioButton mBtnFollow;
        View mLine;

        public Vh(View itemView) {
            super(itemView);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mName = (TextView) itemView.findViewById(R.id.name);
            mID = (TextView) itemView.findViewById(R.id.id_val);
            mFansNum = (TextView) itemView.findViewById(R.id.fans_num);
            mSign = (TextView) itemView.findViewById(R.id.sign);
            mLine = itemView.findViewById(R.id.line);
            mBtnFollow = (MyRadioButton) itemView.findViewById(R.id.btn_follow);
            mBtnFollow.setOnClickListener(mFollowClickListener);
            itemView.setOnClickListener(mClickListener);
        }

        void setData(UserBean bean, int position, Object payload) {
            itemView.setTag(bean);
            mBtnFollow.setTag(bean);
            if (payload == null) {
                ImgLoader.displayAvatar(mContext,bean.getAvatarThumb(), mAvatar);
                mName.setText(bean.getUserNiceName());
                mID.setText("ID：" + bean.getId());
                mFansNum.setText(mFanString + bean.getFans());
                mSign.setText(bean.getSignature());
                if (position == getItemCount() - 1) {
                    if (mLine.getVisibility() == View.VISIBLE) {
                        mLine.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mLine.getVisibility() != View.VISIBLE) {
                        mLine.setVisibility(View.VISIBLE);
                    }
                }
            }
            if (bean.getIsattention() == 1) {
                mBtnFollow.doChecked(true);
                mBtnFollow.setText(mFollowing);
            } else {
                mBtnFollow.doChecked(false);
                mBtnFollow.setText(mFollow);
            }
        }

    }
}
