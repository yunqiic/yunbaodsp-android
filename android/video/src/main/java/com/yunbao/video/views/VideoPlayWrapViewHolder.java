package com.yunbao.video.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.bean.LiveBean;
import com.yunbao.common.bean.MusicBean;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.bean.VideoBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.video.R;
import com.yunbao.video.activity.AbsVideoPlayActivity;
import com.yunbao.video.activity.RecordSameVideoActivity;
import com.yunbao.video.custom.MusicAnimLayout;
import com.yunbao.video.dialog.VideoShareDialogFragment;
import com.yunbao.video.event.VideoLikeEvent;
import com.yunbao.video.http.VideoHttpConsts;
import com.yunbao.video.http.VideoHttpUtil;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class VideoPlayWrapViewHolder extends AbsViewHolder implements View.OnClickListener {

    private static final String SPACE = "                 ";
    private ViewGroup mVideoContainer;
    private ImageView mCover;
    private ImageView mAvatar;
    private TextView mName;
    private TextView mTitle;
    private ImageView mBtnLike;//点赞按钮
    private TextView mLikeNum;//点赞数
    private TextView mCommentNum;//评论数
    private TextView mShareNum;//分享数
    private ImageView mBtnFollow;//关注按钮
    private TextView mMusicTitle;//音乐标题
    private VideoBean mVideoBean;
    private Drawable mFollowDrawable;//已关注
    private Drawable mUnFollowDrawable;//未关注
    private ValueAnimator mFollowAnimator;
    private boolean mFollowAnimatorPlaying;
    private boolean mCurPageShowed;//当前页面是否可见
    private ValueAnimator mLikeAnimtor;
    private ValueAnimator mCancelLikeAnimtor;
    private boolean mLikeAnimPlaying;
    private Drawable[] mLikeAnimDrawables;//点赞帧动画
    private Drawable[] mCancelLikeAnimDrawables;//取消点赞帧动画
    private int mLikeAnimIndex;
    private int mCancelLikeAnimIndex;
    private String mTag;
    private String mMusicSuffix;
    private MusicAnimLayout mMusicAnimView;
    private HttpCallback mGetVideoCallback;
    private boolean mFromUserHome;//是否来自个人主页
    private ImageLoadCallback mImageLoadCallback;

    private View mPlayBtn;


    public VideoPlayWrapViewHolder(Context context, ViewGroup parentView, boolean fromUserHome) {
        super(context, parentView, fromUserHome);
    }

    @Override
    protected void processArguments(Object... args) {
        mFromUserHome = (boolean) args[0];
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_video_play_wrap;
    }

    @Override
    public void init() {
        mTag = this.toString();
        mVideoContainer = (ViewGroup) findViewById(R.id.video_container);
        mCover = (ImageView) findViewById(R.id.cover);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mName = (TextView) findViewById(R.id.name);
        mTitle = (TextView) findViewById(R.id.title);

        mBtnLike = (ImageView) findViewById(R.id.btn_like);
        mLikeNum = (TextView) findViewById(R.id.like_num);
        mCommentNum = (TextView) findViewById(R.id.comment_num);
        mShareNum = (TextView) findViewById(R.id.share_num);
        mBtnFollow = (ImageView) findViewById(R.id.btn_follow);
        mMusicTitle = (TextView) findViewById(R.id.music_title);
        mMusicAnimView = (MusicAnimLayout) findViewById(R.id.music_anim_view);
        mFollowDrawable = ContextCompat.getDrawable(mContext, R.mipmap.icon_video_follow_1);
        mUnFollowDrawable = ContextCompat.getDrawable(mContext, R.mipmap.icon_video_follow_0);
//        mTitle.setOnClickListener(this);
        findViewById(R.id.rl_avatar).setOnClickListener(this);
        mBtnFollow.setOnClickListener(this);
        mBtnLike.setOnClickListener(this);
        mMusicAnimView.setOnClickListener(this);
        findViewById(R.id.btn_comment).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);
        mMusicSuffix = WordUtil.getString(R.string.music_suffix);
        mPlayBtn = findViewById(R.id.btn_play);

        mName.setOnClickListener(this);

    }

    public View getPlayBtn() {
        return mPlayBtn;
    }





    /**
     * 初始化点赞动画
     */
    private void initLikeAnimator() {
        if (mLikeAnimDrawables != null && mLikeAnimDrawables.length > 0) {
            mLikeAnimtor = ValueAnimator.ofFloat(0, mLikeAnimDrawables.length);
            mLikeAnimtor.setDuration(600);
            mLikeAnimtor.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = (float) animation.getAnimatedValue();
                    int index = (int) v;
                    if (mLikeAnimIndex != index) {
                        mLikeAnimIndex = index;
                        if (mBtnLike != null && mLikeAnimDrawables != null && index < mLikeAnimDrawables.length) {
                            mBtnLike.setImageDrawable(mLikeAnimDrawables[index]);
                        }
                    }
                }
            });
            mLikeAnimtor.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mLikeAnimPlaying = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mLikeAnimPlaying = false;
                }
            });
        }
    }

    /**
     * 初始化点赞动画
     */
    private void initCancelLikeAnimtor() {
        if (mCancelLikeAnimDrawables != null && mCancelLikeAnimDrawables.length > 0) {
            mCancelLikeAnimtor = ValueAnimator.ofFloat(0, mCancelLikeAnimDrawables.length);
            mCancelLikeAnimtor.setDuration(400);
            mCancelLikeAnimtor.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = (float) animation.getAnimatedValue();
                    int index = (int) v;
                    if (mCancelLikeAnimIndex != index) {
                        mCancelLikeAnimIndex = index;
                        if (mBtnLike != null && mCancelLikeAnimDrawables != null && index < mCancelLikeAnimDrawables.length) {
                            mBtnLike.setImageDrawable(mCancelLikeAnimDrawables[index]);
                        }
                    }
                }
            });
            mCancelLikeAnimtor.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mLikeAnimPlaying = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mLikeAnimPlaying = false;
                }
            });
        }
    }

    /**
     * 初始化关注动画
     */
    private void initFollowAnimation() {
        mFollowAnimator = ValueAnimator.ofFloat(1f, 1.4f, 0.2f);
        mFollowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                mBtnFollow.setScaleX(v);
                mBtnFollow.setScaleY(v);
            }
        });
        mFollowAnimator.setDuration(1000);
        mFollowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mFollowAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                mFollowAnimatorPlaying = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFollowAnimatorPlaying = false;
                if (mBtnFollow != null) {
                    if (mBtnFollow.getVisibility() == View.VISIBLE) {
                        mBtnFollow.setVisibility(View.INVISIBLE);
                    }
                    mBtnFollow.setScaleX(1f);
                    mBtnFollow.setScaleY(1f);
                }
            }
        });
    }

    public void setLikeAnimDrawables(Drawable[] drawables) {
        mLikeAnimDrawables = drawables;
    }

    public void setCancelLikeAnimDrawables(Drawable[] drawables) {
        mCancelLikeAnimDrawables = drawables;
    }


    public void setData(VideoBean bean, Object payload) {
        if (bean == null) {
            return;
        }
        mVideoBean = bean;
//        L.e("--setData--mVideoBean---" + this + "\n" + mVideoBean + "\n" + mVideoBean.getId());
        UserBean u = mVideoBean.getUserBean();
        if (payload == null) {
            setCoverImage();
            if (TextUtils.isEmpty(bean.getTitle())) {
                if (mTitle != null) {
                    mTitle.setVisibility(View.GONE);
                }
            } else {
                if (mTitle != null) {
                    mTitle.setVisibility(View.VISIBLE);
                }
                if (mTitle != null) {
                    mTitle.setText(bean.getTitle());
                }

            }

            if (u != null) {
                if (mAvatar != null) {
                    ImgLoader.displayAvatar(mContext, u.getAvatarThumb(), mAvatar);
                }
                if (mName != null) {
                    mName.setText("@" + u.getUserNiceName());
                }
            }
        }
        if (mBtnLike != null) {
            if (bean.getLike() == 1) {
                if (mLikeAnimDrawables != null && mLikeAnimDrawables.length > 0) {
                    mBtnLike.setImageDrawable(mLikeAnimDrawables[mLikeAnimDrawables.length - 1]);
                }
            } else {
                if (mCancelLikeAnimDrawables != null && mCancelLikeAnimDrawables.length > 0) {
                    mBtnLike.setImageDrawable(mCancelLikeAnimDrawables[mCancelLikeAnimDrawables.length - 1]);
                }
            }
        }
        if (mLikeNum != null) {
            mLikeNum.setText(bean.getLikeNum());
        }
        if (mCommentNum != null) {
            mCommentNum.setText(bean.getCommentNum());
        }
        if (mShareNum != null) {
            mShareNum.setText(bean.getShareNum());
        }
        if (u != null && mBtnFollow != null) {
            String toUid = u.getId();
            if (!TextUtils.isEmpty(toUid) && !toUid.equals(CommonAppConfig.getInstance().getUid())) {
                if (bean.getAttent() == 1) {
                    if (mBtnFollow.getVisibility() == View.VISIBLE) {
                        mBtnFollow.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mBtnFollow.getVisibility() != View.VISIBLE) {
                        mBtnFollow.setVisibility(View.VISIBLE);
                    }
                    mBtnFollow.setImageDrawable(mUnFollowDrawable);
                }
            } else {
                if (mBtnFollow.getVisibility() == View.VISIBLE) {
                    mBtnFollow.setVisibility(View.INVISIBLE);
                }
            }
        }
        if (mVideoBean.getMusicId() != 0) {
            MusicBean musicBean = mVideoBean.getMusicInfo();
            if (musicBean != null) {
                if (mMusicAnimView != null) {
                    mMusicAnimView.setImageUrl(musicBean.getImgUrl());
                }
                if (mMusicTitle != null) {
                    String title = musicBean.getTitle();
                    mMusicTitle.setText(title + SPACE + title + SPACE + title + SPACE + title + SPACE + title);
                }
            }
        } else {
            if (u != null) {
                if (mMusicAnimView != null) {
                    mMusicAnimView.setImageUrl(u.getAvatarThumb());
                }
                if (mMusicTitle != null) {
                    String title = "@" + u.getUserNiceName() + mMusicSuffix;
                    mMusicTitle.setText(title + SPACE + title + SPACE + title + SPACE + title + SPACE + title);
                }
            }
        }

    }

    private void getVideoInfo() {
        if (mVideoBean == null) {
            return;
        }
        if (mGetVideoCallback == null) {
            mGetVideoCallback = new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {

                    if (code == 0 && info.length > 0) {
                        VideoBean bean = JSON.parseObject(info[0], VideoBean.class);
                        UserBean u = bean.getUserBean();
                        if (u != null && mBtnFollow != null) {
                            String toUid = u.getId();
                            if (!TextUtils.isEmpty(toUid) && !toUid.equals(CommonAppConfig.getInstance().getUid())) {
                                if (bean.getAttent() == 1) {
                                    if (mBtnFollow.getVisibility() == View.VISIBLE) {
                                        mBtnFollow.setVisibility(View.INVISIBLE);
                                    }
                                } else {
                                    if (mBtnFollow.getVisibility() != View.VISIBLE) {
                                        mBtnFollow.setVisibility(View.VISIBLE);
                                    }
                                    mBtnFollow.setImageDrawable(mUnFollowDrawable);
                                }
                            } else {
                                if (mBtnFollow.getVisibility() == View.VISIBLE) {
                                    mBtnFollow.setVisibility(View.INVISIBLE);
                                }
                            }
                        }
                        if (mBtnLike != null) {
                            if (bean.getLike() == 1) {
                                if (mLikeAnimDrawables != null && mLikeAnimDrawables.length > 0) {
                                    mBtnLike.setImageDrawable(mLikeAnimDrawables[mLikeAnimDrawables.length - 1]);
                                }
                            } else {
                                if (mCancelLikeAnimDrawables != null && mCancelLikeAnimDrawables.length > 0) {
                                    mBtnLike.setImageDrawable(mCancelLikeAnimDrawables[mCancelLikeAnimDrawables.length - 1]);
                                }
                            }
                        }
                        if (mVideoBean != null) {
                            mVideoBean.setAttent(bean.getAttent());
                            mVideoBean.setLike(bean.getLike());
                        }
                    }
                }
            };
        }
        VideoHttpUtil.getVideo(mVideoBean.getId(), mGetVideoCallback);
    }


    public void setVideoInfo(VideoBean bean) {
//        L.e("----mVideoBean---" + this + "\n" + mVideoBean + "\n" + mVideoBean.getId());

        UserBean u = bean.getUserBean();
        if (u != null && mBtnFollow != null) {
            String toUid = u.getId();
            if (!TextUtils.isEmpty(toUid) && !toUid.equals(CommonAppConfig.getInstance().getUid())) {
                if (bean.getAttent() == 1) {
                    if (mBtnFollow.getVisibility() == View.VISIBLE) {
                        mBtnFollow.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mBtnFollow.getVisibility() != View.VISIBLE) {
                        mBtnFollow.setVisibility(View.VISIBLE);
                    }
                    mBtnFollow.setImageDrawable(mUnFollowDrawable);
                }
            } else {
                if (mBtnFollow.getVisibility() == View.VISIBLE) {
                    mBtnFollow.setVisibility(View.INVISIBLE);
                }
            }
        }
        if (mBtnLike != null) {
            if (bean.getLike() == 1) {
                if (mLikeAnimDrawables != null && mLikeAnimDrawables.length > 0) {
                    mBtnLike.setImageDrawable(mLikeAnimDrawables[mLikeAnimDrawables.length - 1]);
                }
            } else {
                if (mCancelLikeAnimDrawables != null && mCancelLikeAnimDrawables.length > 0) {
                    mBtnLike.setImageDrawable(mCancelLikeAnimDrawables[mCancelLikeAnimDrawables.length - 1]);
                }
            }
        }
        if (mVideoBean != null) {
            mVideoBean.setAttent(bean.getAttent());
            mVideoBean.setLike(bean.getLike());
            mVideoBean.setLiveinfo(bean.getLiveinfo());
            mVideoBean.setCanPlay(bean.getCanPlay());
        }

    }



    public void refreshFollowAndLike(int loginStatus) {
        if (loginStatus == Constants.LOGIN_STATUS_SUCCESS) {
            getVideoInfo();
        } else if (loginStatus == Constants.LOGIN_STATUS_INVALID) {
            if (mBtnFollow != null) {
                if (mBtnFollow.getVisibility() != View.VISIBLE) {
                    mBtnFollow.setVisibility(View.VISIBLE);
                }
                mBtnFollow.setImageDrawable(mUnFollowDrawable);
            }
            if (mBtnLike != null) {
                if (mCancelLikeAnimDrawables != null && mCancelLikeAnimDrawables.length > 0) {
                    mBtnLike.setImageDrawable(mCancelLikeAnimDrawables[mCancelLikeAnimDrawables.length - 1]);
                }
            }
            if (mVideoBean != null) {
                mVideoBean.setAttent(0);
                mVideoBean.setLike(0);
            }
        }
    }


    public static class ImageLoadCallback implements ImgLoader.DrawableCallback {

        private ImageView mCover;

        public ImageLoadCallback(ImageView cover) {
            mCover = cover;
        }

        @Override
        public void callback(Drawable drawable) {
            if (mCover != null && mCover.getVisibility() == View.VISIBLE && drawable != null) {
                float w = drawable.getIntrinsicWidth();
                float h = drawable.getIntrinsicHeight();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mCover.getLayoutParams();
                int targetH = 0;
                if (w >= h) {//横屏  9:16=0.5625 w / h > 0.5625f
                    targetH = (int) (mCover.getWidth() / w * h);
                } else {
                    targetH = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                if (targetH != params.height) {
                    params.height = targetH;
                    mCover.requestLayout();
                }
                mCover.setImageDrawable(drawable);
            }
        }

        public void release() {
            mCover = null;
        }
    }

    private void setCoverImage() {
        if (mCover == null) {
            return;
        }
        if (mImageLoadCallback == null) {
            mImageLoadCallback = new WeakReference<>(new ImageLoadCallback(mCover)).get();
        }
        ImgLoader.displayDrawable(mContext, mVideoBean.getThumb(), mImageLoadCallback);
    }


    public void addVideoView(View view) {
        if (mVideoContainer != null && view != null) {
            ViewParent parent = view.getParent();
            if (parent != null) {
                ViewGroup viewGroup = (ViewGroup) parent;
                if (viewGroup != mVideoContainer) {
                    viewGroup.removeView(view);
                    mVideoContainer.addView(view);
                }
            } else {
                mVideoContainer.addView(view);
            }
        }
    }

    public VideoBean getVideoBean() {
        return mVideoBean;
    }





    /**
     * 获取到视频首帧回调
     */
    public void onFirstFrame() {
        if (mCover != null && mCover.getVisibility() == View.VISIBLE) {
            mCover.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 双击屏幕
     */
    public void onDoubleClick() {
        if (mVideoBean != null && mVideoBean.getLike() == 0) {
            clickLike();
        }
    }

    /**
     * 滑出屏幕
     */
    public void onPageOutWindow() {
        mCurPageShowed = false;
        if (mCover != null && mCover.getVisibility() != View.VISIBLE) {
            mCover.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 滑入屏幕
     */
    public void onPageInWindow() {
        if (mCover != null) {
            if (mCover.getVisibility() != View.VISIBLE) {
                mCover.setVisibility(View.VISIBLE);
            }
            mCover.setImageDrawable(null);
            setCoverImage();
        }
        startMusicAnim();
    }

    /**
     * 滑动到这一页 准备开始播放
     */
    public void onPageSelected() {
        mCurPageShowed = true;
    }

    @Override
    public void onClick(View v) {
        if (!canClick()) {
            return;
        }
        int i = v.getId();
        if (i == R.id.btn_follow) {
            clickFollow();
        } else if (i == R.id.btn_comment) {
            clickComment();
        } else if (i == R.id.btn_share) {
            clickShare();
        } else if (i == R.id.btn_like) {
            clickLike();
        } else if (i == R.id.rl_avatar || i == R.id.name) {
            clickAvatar();
        }   else if (i == R.id.music_anim_view) {
            clickMusic();
        }
    }


    /**
     * 点击音乐
     */
    private void clickMusic() {
        if (mVideoBean == null) {
            return;
        }
        if (mVideoBean.getMusicId() == 0) {
            ToastUtil.show(WordUtil.getString(R.string.video_not_apply_take_same));
            return;
        }
        RecordSameVideoActivity.forward(mContext, mVideoBean);
    }


    /**
     * 点击礼物
     */
    private void clickGift() {
    }





    /**
     * 点击头像
     */
    private void clickAvatar() {
        if (mVideoBean != null) {
            LiveBean liveBean = mVideoBean.getLiveinfo();
            if (liveBean != null) {
                if (!mFromUserHome) {
                    RouteUtil.forwardUserHome(mContext, mVideoBean.getUid());
                }
            } else {
                if (!mFromUserHome) {
                    RouteUtil.forwardUserHome(mContext, mVideoBean.getUid());
                }
            }

        }

    }

    /**
     * 左滑进个人主页
     */
    public void scrollLeft() {
        if (!mFromUserHome) {
            RouteUtil.forwardUserHome(mContext, mVideoBean.getUid());
        }
    }

    /**
     * 点赞,取消点赞
     */
    private void clickLike() {
        if (!CommonAppConfig.getInstance().isLogin()) {
            RouteUtil.forwardLogin(mContext);
            return;
        }
        if (mVideoBean == null) {
            return;
        }
        VideoHttpUtil.setVideoLike(mTag, mVideoBean.getId(), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    String likeNum = obj.getString("likes");
                    int like = obj.getIntValue("islike");
                    if (mVideoBean != null) {
                        mVideoBean.setLikeNum(likeNum);
                        mVideoBean.setLike(like);
                        EventBus.getDefault().post(new VideoLikeEvent(mVideoBean.getId(), like, likeNum));
                    }
                    if (mLikeNum != null) {
                        mLikeNum.setText(likeNum);
                    }
                    if (mBtnLike != null) {
                        if (like == 1) {
                            if (mLikeAnimtor == null) {
                                initLikeAnimator();
                            }
                            mLikeAnimIndex = -1;
                            if (!mLikeAnimPlaying && mLikeAnimtor != null) {
                                mLikeAnimtor.start();
                            }
                        } else {
                            if (mCancelLikeAnimtor == null) {
                                initCancelLikeAnimtor();
                            }
                            mCancelLikeAnimIndex = -1;
                            if (!mLikeAnimPlaying && mCancelLikeAnimtor != null) {
                                mCancelLikeAnimtor.start();
                            }
                        }
                    }
                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }

    /**
     * 点击关注按钮
     */
    private void clickFollow() {
        if (!CommonAppConfig.getInstance().isLogin()) {
            RouteUtil.forwardLogin(mContext);
            return;
        }
        if (mVideoBean == null || mFollowAnimatorPlaying) {
            return;
        }
        final UserBean u = mVideoBean.getUserBean();
        if (u == null) {
            return;
        }
        CommonHttpUtil.setAttention(mTag, u.getId(), new CommonCallback<Integer>() {
            @Override
            public void callback(Integer attent) {
                mVideoBean.setAttent(attent);
                if (mCurPageShowed) {
//                    if (mFollowAnimation == null) {
//                        initFollowAnimation();
//                    }
                    if (mFollowAnimator == null) {
                        initFollowAnimation();
                    }
                    //mBtnFollow.startAnimation(mFollowAnimation);
                    mBtnFollow.setImageDrawable(mFollowDrawable);
                    mFollowAnimator.start();
                } else {
                    if (attent == 1) {
                        mBtnFollow.setImageDrawable(null);
                    } else {
                        mBtnFollow.setImageDrawable(mUnFollowDrawable);
                    }
                }
            }
        });
    }

    /**
     * 点击评论按钮
     */
    private void clickComment() {
        ((AbsVideoPlayActivity) mContext).openCommentWindow(mVideoBean.getId(), mVideoBean.getUid());
    }

    /**
     * 点击分享按钮
     */
    private void clickShare() {
        if (mVideoBean == null) {
            return;
        }
        VideoShareDialogFragment fragment = new VideoShareDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.VIDEO_BEAN, mVideoBean);
        fragment.setArguments(bundle);
        fragment.show(((AbsVideoPlayActivity) mContext).getSupportFragmentManager(), "VideoShareDialogFragment");
    }

    @Override
    public void release() {
        release(false);
    }

    public void release(boolean recycle) {
        VideoHttpUtil.cancel(VideoHttpConsts.GET_VIDEO);
        VideoHttpUtil.cancel(mTag);
        if (mLikeAnimtor != null) {
            mLikeAnimtor.cancel();
        }
        if (mFollowAnimator != null) {
            mFollowAnimator.cancel();
        }
        if (mImageLoadCallback != null) {
            mImageLoadCallback.release();
        }
        mImageLoadCallback = null;
        if (mMusicAnimView != null) {
            if (recycle) {
                mMusicAnimView.recycle();
            } else {
                mMusicAnimView.release();
                mMusicAnimView = null;
            }
        }

    }

    /**
     * 开始音乐播放动画
     */
    private void startMusicAnim() {
        if (mMusicAnimView != null) {
            mMusicAnimView.startAnim();
        }
    }


    public void recycleBitmap() {
        ImgLoader.clear(mContext, mAvatar);
        ImgLoader.clear(mContext, mCover);
    }


}
