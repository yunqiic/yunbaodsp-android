package com.yunbao.common.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.yunbao.common.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class SideIndexBar extends View {

    private float mTextSpace;
    private int mTextSize;
    private List<String> mIndexList;
    private Map<String, Integer> mMap;

    private int mSelectionPosition;
    private float mIndexHeight;

    private Paint mUnCheckPaint;
    private Paint mCheckPaint;

    private ActionListener mActionListener;

    public SideIndexBar(Context context) {
        this(context, null);
    }

    public SideIndexBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SideIndexBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SideIndexBar);
        int sib_check_text_color = ta.getColor(R.styleable.SideIndexBar_sib_check_text_color, 0);
        int sib_uncheck_text_color = ta.getColor(R.styleable.SideIndexBar_sib_uncheck_text_color, 0);
        mTextSize = (int) ta.getDimension(R.styleable.SideIndexBar_sib_text_size, 0);
        mTextSpace = ta.getDimension(R.styleable.SideIndexBar_sib_text_space, 0);
        ta.recycle();

        mUnCheckPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnCheckPaint.setColor(sib_uncheck_text_color);
        mUnCheckPaint.setTextAlign(Paint.Align.CENTER);
        mUnCheckPaint.setTextSize(mTextSize);

        mCheckPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCheckPaint.setTextAlign(Paint.Align.CENTER);
        mCheckPaint.setColor(sib_check_text_color);
        mCheckPaint.setTextSize(mTextSize);

//        mIndexList = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mIndexList != null && mIndexList.size() > 0) {
            int totalHeight = (int) (mIndexList.size() * mTextSize + (mIndexList.size() + 1) * mTextSpace);
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIndexList != null && mIndexList.size() > 0) {
            mIndexHeight = ((float) getHeight()) / mIndexList.size();
            for (int i = 0, size = mIndexList.size(); i < size; i++) {
                if (mSelectionPosition == i) {
                    canvas.drawText(mIndexList.get(i), getWidth() / 2, mIndexHeight * 0.85f + mIndexHeight * i, mCheckPaint);
                } else {
                    canvas.drawText(mIndexList.get(i), getWidth() / 2, mIndexHeight * 0.85f + mIndexHeight * i, mUnCheckPaint);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchPos = getPositionForPointY(event.getY());
        if (touchPos < 0) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                setSelectionPosition(touchPos);
                if (mActionListener != null) {
                    mActionListener.onSelectionPosition(touchPos);
                }
                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//
//                break;
        }
        return true;
    }

    private int getPositionForPointY(float y) {
        if (mIndexHeight == 0) {
            return -1;
        }
        int position = (int) (y / mIndexHeight);
        if (position < 0) {
            position = 0;
        } else if (position > mIndexList.size() - 1) {
            position = mIndexList.size() - 1;
        }

        return position;
    }

    public void setSelectionIndex(String key) {
        if(mMap!=null){
            Integer pos=mMap.get(key);
            if(pos!=null){
                setSelectionPosition(pos);
            }
        }
    }

    public void setSelectionPosition(int position) {
        if (mSelectionPosition != position) {
            mSelectionPosition = position;
            invalidate();
        }
    }

    public void setData(List<String> list) {
        if (mMap == null) {
            mMap = new HashMap<>();
        } else {
            mMap.clear();
        }
        for (int i = 0, size = list.size(); i < size; i++) {
            mMap.put(list.get(i), i);
        }
        mIndexList = list;
        requestLayout();
    }


    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public interface ActionListener {
        void onSelectionPosition(int position);
    }

}
