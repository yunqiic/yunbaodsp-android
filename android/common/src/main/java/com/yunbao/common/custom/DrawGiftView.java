package com.yunbao.common.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

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
public class DrawGiftView extends View {

    private static final String TAG = "DrawGiftView";
    private int mCount;
    private Paint mPaint;
    private List<PointF> mPointList;
    private Bitmap mBitmap;
    private Rect mSrc;
    private RectF mDst;
    private float mOffsetX;
    private float mOffsetY;
    private ActionListener mActionListener;
    private int mHeight;
    private float mLastX;
    private float mLastY;

    public DrawGiftView(Context context) {
        this(context, null);
    }

    public DrawGiftView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawGiftView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPointList = new ArrayList<>();
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mSrc = new Rect();
        mDst = new RectF();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mOffsetX = dm.widthPixels / 20;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = getMeasuredHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mBitmap == null) {
            return super.onTouchEvent(e);
        }
        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = 0;
                mLastY = 0;
//                L.e(TAG, "绘制开始-------->");
//                if (mActionListener != null) {
//                    mActionListener.onDrawStart();
//                }
                if (mPointList.size() < 100) {
                    mPointList.add(new PointF(e.getX(), e.getY()));
                    invalidate();
                    if (mActionListener != null) {
                        mActionListener.onDrawCountChanged(mPointList.size());
                    }
                }
                mCount = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mLastX != x && mLastY != y) {
                    if (e.getY() <= mHeight && mPointList.size() < 100) {
//                    L.e(TAG, "绘制中---X---> " + e.getX() + "----Y---> " + e.getY());
                        mCount++;
                        if (mCount % 2 == 0) {
                            mPointList.add(new PointF(e.getX(), e.getY()));
                            invalidate();
                            if (mActionListener != null) {
                                mActionListener.onDrawCountChanged(mPointList.size());
                            }
                        }
                    }
                }
                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                L.e(TAG, "绘制结束-------->");
//                break;
        }
        mLastX = x;
        mLastY = y;
        return true;
    }


    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        if (bitmap != null) {
            if (mSrc != null) {
                mSrc.left = 0;
                mSrc.top = 0;
                mSrc.right = bitmap.getWidth();
                mSrc.bottom = bitmap.getHeight();
            }
            mOffsetY = mOffsetX / bitmap.getWidth() * bitmap.getHeight();
        }
    }

    public void clear() {
        if (mPointList != null) {
            mPointList.clear();
        }
        invalidate();
        if (mActionListener != null) {
            mActionListener.onDrawCountChanged(mPointList.size());
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mPointList != null && mPointList.size() > 0 && mBitmap != null && mSrc != null && mDst != null) {
            for (PointF pointF : mPointList) {
                mDst.left = pointF.x - mOffsetX;
                mDst.right = pointF.x + mOffsetX;
                mDst.top = pointF.y - mOffsetY;
                mDst.bottom = pointF.y + mOffsetY;
                canvas.drawBitmap(mBitmap, mSrc, mDst, mPaint);
            }
        }
    }

    public List<PointF> getPointList() {
        return mPointList;
    }


    public void drawBack() {
        if (mPointList != null && mPointList.size() > 0) {
            mPointList.remove(mPointList.size() - 1);
            invalidate();
            if (mActionListener != null) {
                mActionListener.onDrawCountChanged(mPointList.size());
            }
        }
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }


    public interface ActionListener {
//        void onDrawStart();
//
//        void onDrawEnd();

        void onDrawCountChanged(int count);
    }
}
