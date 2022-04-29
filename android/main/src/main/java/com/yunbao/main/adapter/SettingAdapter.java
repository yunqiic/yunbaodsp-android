package com.yunbao.main.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.Constants;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.SpUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.main.R;
import com.yunbao.main.bean.SettingBean;

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

public class SettingAdapter extends RecyclerView.Adapter {

    private static final int NORMAL = 0;
    private static final int VERSION = 1;
    private static final int LAST = 2;
    private static final int RADIO = 3;
    private List<SettingBean> mList;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private OnItemClickListener<SettingBean> mOnItemClickListener;
    private View.OnClickListener mOnRadioBtnClickListener;
    private String mVersionString;
    private String mCacheString;
    private Drawable mRadioCheckDrawable;
    private Drawable mRadioUnCheckDrawable;


    public SettingAdapter(Context context, List<SettingBean> list, String version, String cache) {
        mList = list;
        mVersionString = version;
        mCacheString = cache;
        mInflater = LayoutInflater.from(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null) {
                    int position = (int) tag;
                    SettingBean bean = mList.get(position);
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(bean, position);
                    }
                }
            }
        };
        mRadioCheckDrawable = ContextCompat.getDrawable(context, R.mipmap.icon_btn_radio_1);
        mRadioUnCheckDrawable = ContextCompat.getDrawable(context, R.mipmap.icon_btn_radio_0);
        mOnRadioBtnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null) {
                    int position = (int) tag;
                    SettingBean bean = mList.get(position);
                    boolean chatMusicClose = SpUtil.getInstance().getBooleanValue(SpUtil.CHAT_MUSIC_CLOSE);
                    SpUtil.getInstance().setBooleanValue(SpUtil.CHAT_MUSIC_CLOSE, !chatMusicClose);
                    bean.setChatMusicClose(!chatMusicClose);
                    notifyItemChanged(position,Constants.PAYLOAD);
                }
            }
        };
    }

    public void setCacheString(String cacheString) {
        mCacheString = cacheString;
    }

    public void setOnItemClickListener(OnItemClickListener<SettingBean> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        int id = mList.get(position).getId();
        if (id == Constants.SETTING_UPDATE_ID || id == Constants.SETTING_CLEAR_CACHE) {
            return VERSION;
        } else if (id == Constants.SETTING_EXIT) {
            return LAST;
        } else if (id == Constants.SETTING_RADIO) {
            return RADIO;
        } else {
            return NORMAL;
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VERSION) {
            return new Vh2(mInflater.inflate(R.layout.item_setting_1, parent, false));
        } else if (viewType == LAST) {
            return new Vh(mInflater.inflate(R.layout.item_setting_2, parent, false));
        } else if (viewType == RADIO) {
            return new RadioVh(mInflater.inflate(R.layout.item_setting_3, parent, false));
        } else {
            return new Vh(mInflater.inflate(R.layout.item_setting, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {

    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        if (vh instanceof Vh) {
            ((Vh) vh).setData(mList.get(position), position);
        } else if (vh instanceof RadioVh) {
            Object payload = payloads.size() > 0 ? payloads.get(0) : null;
            ((RadioVh) vh).setData(mList.get(position), position, payload);
        } else {
            ((Vh2) vh).setData(mList.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {

        TextView mName;

        public Vh(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.name);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(SettingBean bean, int position) {
            itemView.setTag(position);
            mName.setText(bean.getName());
        }
    }

    class Vh2 extends RecyclerView.ViewHolder {

        TextView mName;
        TextView mText;

        public Vh2(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.name);
            mText = (TextView) itemView.findViewById(R.id.text);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(SettingBean bean, int position) {
            itemView.setTag(position);
            mName.setText(bean.getName());
            if (bean.getId() == Constants.SETTING_CLEAR_CACHE) {
                mText.setText(mCacheString);
            } else {
                mText.setText(mVersionString);
            }
        }
    }


    class RadioVh extends RecyclerView.ViewHolder {

        TextView mName;
        ImageView mBtnRadio;

        public RadioVh(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.name);
            mBtnRadio = itemView.findViewById(R.id.btn_radio);
            mBtnRadio.setOnClickListener(mOnRadioBtnClickListener);
        }

        void setData(SettingBean bean, int position, Object payload) {
            if (payload == null) {
                mName.setText(bean.getName());
                mBtnRadio.setTag(position);
            }
            mBtnRadio.setImageDrawable(bean.isChatMusicClose() ? mRadioUnCheckDrawable : mRadioCheckDrawable);
        }
    }

    public void setVersionTip(String tip) {
        if (TextUtils.isEmpty(tip)) {
            return;
        }
        for (int i = 0, size = mList.size(); i < size; i++) {
            SettingBean bean = mList.get(i);
            if (bean.getId() == Constants.SETTING_UPDATE_ID) {
                mVersionString = StringUtil.contact(mVersionString, "(", tip, ")");
                notifyItemChanged(i);
                break;
            }
        }
    }


}
