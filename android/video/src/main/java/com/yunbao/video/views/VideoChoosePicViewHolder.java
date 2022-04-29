package com.yunbao.video.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.yunbao.common.Constants;
import com.yunbao.common.bean.MusicBean;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.ScreenDimenUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.util.bean.ChatChooseImageBean;
import com.yunbao.util.utils.ImageUtil;
import com.yunbao.video.R;
import com.yunbao.video.activity.PictureEditActivity;
import com.yunbao.video.activity.VideoRecordActivity;
import com.yunbao.video.adapter.VideoChoosePicAdapter;

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
public class VideoChoosePicViewHolder extends AbsViewHolder implements View.OnClickListener, VideoChoosePicAdapter.ActionListener {

    private ObjectAnimator mShowAnimator;
    private ObjectAnimator mHideAnimator;
    private boolean mAnimating;
    private boolean mShowed;
    private View mRoot;

    private RecyclerView mRecyclerView;
    private TextView mBtnNext;
    private VideoChoosePicAdapter mAdapter;
    private String mStringComplete;
    private ImageUtil mImageUtil;
    private View mNoData;
    private boolean mLoaded;


    public VideoChoosePicViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    public void show() {
        if (!mShowed && mShowAnimator != null) {
            mShowAnimator.start();
        }
    }

    public void hide() {
        if (mShowed && mHideAnimator != null) {
            mHideAnimator.start();
        }
    }

    public boolean isShowed() {
        return mShowed;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.view_video_choose_img;
    }

    @Override
    public void init() {
        mRoot = findViewById(R.id.root);
        View group = findViewById(R.id.group);
        int screenHeight = ScreenDimenUtil.getInstance().getScreenHeight();
        group.setTranslationY(screenHeight);
        TimeInterpolator interpolator = new AccelerateDecelerateInterpolator();
        mShowAnimator = ObjectAnimator.ofFloat(group, "translationY", 0);
        mShowAnimator.setInterpolator(interpolator);
        mShowAnimator.setDuration(300);
        mHideAnimator = ObjectAnimator.ofFloat(group, "translationY", screenHeight);
        mHideAnimator.setInterpolator(interpolator);
        mHideAnimator.setDuration(300);
        mShowAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
                if (mRoot != null && mRoot.getVisibility() != View.VISIBLE) {
                    mRoot.setVisibility(View.VISIBLE);
                }
                mShowed = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                loadData();
            }
        });
        mHideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                mShowed = false;
                if (mRoot != null && mRoot.getVisibility() == View.VISIBLE) {
                    mRoot.setVisibility(View.INVISIBLE);
                }
            }

        });
        mBtnNext = (TextView) findViewById(R.id.btn_next);
        mBtnNext.setOnClickListener(this);
        mBtnNext.setEnabled(false);
        findViewById(R.id.btn_back).setOnClickListener(this);
        mNoData = findViewById(R.id.no_data);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 4, GridLayoutManager.VERTICAL, false));
        mStringComplete = WordUtil.getString(R.string.video_pic_choose_complete);
        mImageUtil = new ImageUtil();
    }

    private void loadData() {
        if (mLoaded) {
            if (mAdapter != null) {
                mAdapter.refresh();
            }
            return;
        }
        if (mImageUtil == null) {
            return;
        }
        mLoaded = true;
        mImageUtil.getLocalImageList(new CommonCallback<List<ChatChooseImageBean>>() {
            @Override
            public void callback(List<ChatChooseImageBean> list) {
                if (list.size() == 0) {
                    if (mNoData.getVisibility() != View.VISIBLE) {
                        mNoData.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (mRecyclerView != null) {
                        if (mAdapter == null) {
                            mAdapter = new VideoChoosePicAdapter(mContext, list);
                            mAdapter.setActionListener(VideoChoosePicViewHolder.this);
                        }
                        mRecyclerView.setAdapter(mAdapter);
                    }
                }
            }
        });
    }


    @Override
    public void onCheckedCountChanged(int checkedCount) {
        if (mBtnNext != null) {
            if (checkedCount > 0) {
                mBtnNext.setEnabled(true);
                mBtnNext.setText(StringUtil.contact(mStringComplete, "(", String.valueOf(checkedCount), ")"));
            } else {
                mBtnNext.setEnabled(false);
                mBtnNext.setText(mStringComplete);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (mAnimating || !canClick()) {
            return;
        }
        int id = v.getId();
        if (id == R.id.btn_next) {
            doSelect();
        } else if (id == R.id.btn_back) {
            hide();
        }
    }

    private void doSelect() {
        if (mAdapter == null) {
            return;
        }
        ArrayList<String> picturePathList = mAdapter.getImagePathList();
        if (picturePathList == null) {
            return;
        }
        MusicBean musicBean = ((VideoRecordActivity) mContext).getMusicBean();
        int recordType = ((VideoRecordActivity) mContext).getRecordType();
        if (recordType != Constants.VIDEO_RECORD_TYPE_RECORD_SAME) {
            musicBean = null;
        }
        Intent intent = new Intent(mContext, PictureEditActivity.class);
        intent.putStringArrayListExtra(Constants.INTENT_KEY_MULTI_PIC_LIST, picturePathList);
        intent.putExtra(Constants.VIDEO_MUSIC_BEAN, musicBean);
        hide();
        mContext.startActivity(intent);
    }


    @Override
    public void release() {
        if (mImageUtil != null) {
            mImageUtil.release();
            mImageUtil = null;
        }
        if (mShowAnimator != null) {
            mShowAnimator.cancel();
            mShowAnimator.removeAllListeners();
            mShowAnimator.removeAllUpdateListeners();
            mShowAnimator = null;
        }
        if (mHideAnimator != null) {
            mHideAnimator.cancel();
            mHideAnimator.removeAllListeners();
            mHideAnimator.removeAllUpdateListeners();
            mHideAnimator = null;
        }
    }


}
