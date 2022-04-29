package com.yunbao.common.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

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

public class TabButtonGroup extends LinearLayout implements View.OnClickListener {

    private List<TabButton> mList;
    private ActionListener mActionListener;

    public TabButtonGroup(Context context) {
        this(context, null);
    }

    public TabButtonGroup(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabButtonGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mList = new ArrayList<>();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View v = getChildAt(i);
            v.setTag(i);
            v.setOnClickListener(this);
            mList.add((TabButton) v);
        }
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag != null) {
            if (mActionListener != null) {
                mActionListener.onItemClick((int) tag);
            }
        }
    }

    public void setCheck(int position, boolean checked) {
        TabButton btn = mList.get(position);
        if (btn != null) {
            btn.setChecked(checked);
        }
    }


    public interface ActionListener {
        void onItemClick(int position);
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }
}
