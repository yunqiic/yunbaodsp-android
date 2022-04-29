package com.yunbao.video.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.custom.CommonRefreshView;
import com.yunbao.common.custom.MyLinearLayout3;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.video.R;
import com.yunbao.video.activity.AbsVideoCommentActivity;
import com.yunbao.video.adapter.VideoCommentAdapter;
import com.yunbao.video.bean.VideoCommentBean;
import com.yunbao.video.event.VideoCommentEvent;
import com.yunbao.video.http.VideoHttpConsts;
import com.yunbao.video.http.VideoHttpUtil;

import org.greenrobot.eventbus.EventBus;

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

public class VideoCommentViewHolder extends AbsViewHolder implements View.OnClickListener, VideoCommentAdapter.ActionListener {

    private View mRoot;
    private MyLinearLayout3 mBottom;
    private CommonRefreshView mRefreshView;
    private TextView mCommentNum;
    private VideoCommentAdapter mVideoCommentAdapter;
    private String mVideoId;
    private String mVideoUid;
    private String mCommentString;
    private ObjectAnimator mShowAnimator;
    private ObjectAnimator mHideAnimator;
    private boolean mAnimating;
    private boolean mShowed;
    private boolean mFullScreen;
//    private boolean mVideoIdChanged;//视频id是否发生变化了

    public VideoCommentViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    public VideoCommentViewHolder(Context context, ViewGroup parentView, boolean fullScreen) {
        super(context, parentView, fullScreen);
    }

    @Override
    protected void processArguments(Object... args) {
        if (args.length > 0) {
            mFullScreen = (boolean) args[0];
        }
    }

    @Override
    protected int getLayoutId() {
        return mFullScreen ? R.layout.view_video_comment_2 : R.layout.view_video_comment;
    }

    @Override
    public void init() {
        mRoot = findViewById(R.id.root_2);
        mBottom = (MyLinearLayout3) findViewById(R.id.bottom);
        int height = mBottom.getHeight2();
        mBottom.setTranslationY(height);
        mShowAnimator = ObjectAnimator.ofFloat(mBottom, "translationY", 0);
        mHideAnimator = ObjectAnimator.ofFloat(mBottom, "translationY", height);
        mShowAnimator.setDuration(200);
        mHideAnimator.setDuration(200);
        TimeInterpolator interpolator = new AccelerateDecelerateInterpolator();
        mShowAnimator.setInterpolator(interpolator);
        mHideAnimator.setInterpolator(interpolator);
        AnimatorListenerAdapter animatorListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                if (animation == mHideAnimator) {
                    if (mRoot != null && mRoot.getVisibility() == View.VISIBLE) {
                        mRoot.setVisibility(View.INVISIBLE);
                    }
                } else if (animation == mShowAnimator) {
//                    if (mVideoIdChanged) {
//                        mVideoIdChanged = false;
//                        if (mRefreshView != null) {
//                            mRefreshView.initData();
//                        }
//                    }
                    if (mRefreshView != null) {
                        mRefreshView.initData();
                    }
                }
            }
        };
        mShowAnimator.addListener(animatorListener);
        mHideAnimator.addListener(animatorListener);

        mRoot.setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.input).setOnClickListener(this);
        findViewById(R.id.btn_face).setOnClickListener(this);
        mCommentString = WordUtil.getString(R.string.video_comment);
        mCommentNum = (TextView) findViewById(R.id.comment_num);
        mRefreshView = (CommonRefreshView) findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_comment);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (Exception e) {
                    L.e("onLayoutChildren------>" + e.getMessage());
                }
            }
        });

        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<VideoCommentBean>() {
            @Override
            public RefreshAdapter<VideoCommentBean> getAdapter() {
                if (mVideoCommentAdapter == null) {
                    mVideoCommentAdapter = new VideoCommentAdapter(mContext);
                    mVideoCommentAdapter.setVideoUid(mVideoUid);
                    mVideoCommentAdapter.setActionListener(VideoCommentViewHolder.this);
                }
                return mVideoCommentAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (!TextUtils.isEmpty(mVideoId)) {
                    VideoHttpUtil.getVideoCommentList(mVideoId, p, callback);
                }
            }

            @Override
            public List<VideoCommentBean> processData(String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                String commentNum = obj.getString("comments");
                EventBus.getDefault().post(new VideoCommentEvent(mVideoId, commentNum));
                if (mCommentNum != null) {
                    mCommentNum.setText(commentNum + " " + mCommentString);
                }
                List<VideoCommentBean> list = JSON.parseArray(obj.getString("commentlist"), VideoCommentBean.class);
                for (VideoCommentBean bean : list) {
                    if (bean != null) {
                        bean.setParentNode(true);
                    }
                }
                return list;
            }

            @Override
            public void onRefreshSuccess(List<VideoCommentBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<VideoCommentBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
    }

    public void setVideoInfo(String videoId, String videoUid) {
        if (!TextUtils.isEmpty(videoId) && !TextUtils.isEmpty(videoUid)) {
            if (!TextUtils.isEmpty(mVideoId) && !mVideoId.equals(videoId)) {
                if (mVideoCommentAdapter != null) {
                    mVideoCommentAdapter.clearData();
                    mVideoCommentAdapter.setVideoUid(videoUid);
                }
            }
//            if (!videoId.equals(mVideoId)) {
//                mVideoIdChanged = true;
//            }
            mVideoId = videoId;
            mVideoUid = videoUid;
        }

    }

    public void showBottom() {
        if (mAnimating) {
            return;
        }
        if (mRoot != null && mRoot.getVisibility() != View.VISIBLE) {
            mRoot.setVisibility(View.VISIBLE);
        }
        if (mShowAnimator != null) {
            mShowAnimator.start();
        }
        mShowed = true;
    }

    public void hideBottom() {
        if (mAnimating) {
            return;
        }
        if (mHideAnimator != null) {
            mHideAnimator.start();
        }
        mShowed = false;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.root_2 || i == R.id.btn_close) {
            hideBottom();
        } else if (i == R.id.input) {
            if (!TextUtils.isEmpty(mVideoId) && !TextUtils.isEmpty(mVideoUid)) {
                ((AbsVideoCommentActivity) mContext).openCommentInputWindow(false, mVideoId, mVideoUid, null, true);
            }
        } else if (i == R.id.btn_face) {
            if (!TextUtils.isEmpty(mVideoId) && !TextUtils.isEmpty(mVideoUid)) {
                ((AbsVideoCommentActivity) mContext).openCommentInputWindow(true, mVideoId, mVideoUid, null, true);
            }
        }
    }


    public void release() {
        if (mVideoCommentAdapter != null) {
            mVideoCommentAdapter.setOnItemClickListener(null);
            mVideoCommentAdapter.setActionListener(null);
        }
        if (mShowAnimator != null) {
            mShowAnimator.cancel();
        }
        mShowAnimator = null;
        if (mHideAnimator != null) {
            mHideAnimator.cancel();
        }
        mHideAnimator = null;
        VideoHttpUtil.cancel(VideoHttpConsts.GET_VIDEO_COMMENT_LIST);
        VideoHttpUtil.cancel(VideoHttpConsts.SET_COMMENT_LIKE);
        VideoHttpUtil.cancel(VideoHttpConsts.GET_COMMENT_REPLY);
        VideoHttpUtil.cancel(VideoHttpConsts.DELETE_COMMENT);
    }


    public boolean isShowed() {
        return mShowed;
    }

    @Override
    public void onItemClick(VideoCommentBean bean) {
        String commentUid = bean.getUid();
        if (!TextUtils.isEmpty(commentUid) && commentUid.equals(CommonAppConfig.getInstance().getUid())) {
            return;
        }
        if (!TextUtils.isEmpty(mVideoId) && !TextUtils.isEmpty(mVideoUid)) {
            ((AbsVideoCommentActivity) mContext).openCommentInputWindow(false, mVideoId, mVideoUid, bean, true);
        }
    }

    /**
     * 长按事件
     */
    @Override
    public void onItemLongClick(final VideoCommentBean bean) {
        String uid = CommonAppConfig.getInstance().getUid();
        Integer[] arr = null;
        //是否是自己的视频
        boolean isSelfVideo = !TextUtils.isEmpty(mVideoUid) && mVideoUid.equals(uid);
        //是否是自己的评论
        String commentUid = bean.getUid();
        boolean isSelfComment = !TextUtils.isEmpty(commentUid) && commentUid.equals(uid);
        if (isSelfVideo || isSelfComment) {
            if (bean.isVoice()) {
                arr = new Integer[]{R.string.delete};
            } else {
                arr = new Integer[]{R.string.copy, R.string.delete};
            }
        } else {
            if (bean.isVoice()) {
                return;
            }
            arr = new Integer[]{R.string.copy};
        }
        if (arr == null) {
            return;
        }
        DialogUitl.showStringArrayDialog(mContext, arr, new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int tag) {
                if (tag == R.string.copy) {//复制评论
                    UserBean u = bean.getUserBean();
                    if (u != null) {
                        String content = StringUtil.contact("@", u.getUserNiceName(), ": ", bean.getContent());
                        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("text", content);
                        cm.setPrimaryClip(clipData);
                        ToastUtil.show(R.string.copy_suc);
                    }
                } else if (tag == R.string.delete) {
                    //删除评论
                    VideoHttpUtil.deleteComment(bean.getVideoId(), bean.getId(), bean.getUid(), new HttpCallback() {

                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0) {
                                refreshComment();
                            }
                            ToastUtil.show(msg);
                        }
                    });

                }
            }
        });
    }


    @Override
    public void onExpandClicked(final VideoCommentBean commentBean) {
        final VideoCommentBean parentNodeBean = commentBean.getParentNodeBean();
        if (parentNodeBean == null) {
            return;
        }
        VideoHttpUtil.getCommentReply(parentNodeBean.getId(), commentBean.getId(), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    List<VideoCommentBean> list = JSON.parseArray(obj.getString("lists"), VideoCommentBean.class);
                    if (list == null || list.size() == 0) {
                        return;
                    }
                    for (VideoCommentBean bean : list) {
                        bean.setParentNodeBean(parentNodeBean);
                    }
                    List<VideoCommentBean> childList = parentNodeBean.getChildList();
                    if (childList != null) {
                        childList.addAll(list);
                        if (mVideoCommentAdapter != null) {
                            mVideoCommentAdapter.insertReplyList(commentBean, list.size());
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onCollapsedClicked(VideoCommentBean commentBean) {
        VideoCommentBean parentNodeBean = commentBean.getParentNodeBean();
        if (parentNodeBean == null) {
            return;
        }
        List<VideoCommentBean> childList = parentNodeBean.getChildList();
        VideoCommentBean node0 = childList.get(0);
        int orignSize = childList.size();
        parentNodeBean.removeChild();
        if (mVideoCommentAdapter != null) {
            mVideoCommentAdapter.removeReplyList(node0, orignSize - childList.size());
        }
    }

    @Override
    public void onVoicePlay(VideoCommentBean commentBean) {
        if (mContext != null && mContext instanceof AbsVideoCommentActivity) {
            ((AbsVideoCommentActivity) mContext).playCommentVoice(commentBean);
        }
    }

    @Override
    public void onVoiceStop(VideoCommentBean commentBean) {
        if (mContext != null && mContext instanceof AbsVideoCommentActivity) {
            ((AbsVideoCommentActivity) mContext).stopCommentVoice(commentBean);
        }
    }

    /**
     * 停止语音播放的动画
     */
    public void stopVoiceAnim() {
        if (mVideoCommentAdapter != null) {
            mVideoCommentAdapter.stopVoiceAnim();
        }
    }


    /**
     * 刷新评论
     */
    public void refreshComment() {
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    /**
     * 停止播放语音
     */
    public void stopPlayVoice() {
        if (mVideoCommentAdapter != null) {
            mVideoCommentAdapter.stopPlayVoice();
        }
    }

}
