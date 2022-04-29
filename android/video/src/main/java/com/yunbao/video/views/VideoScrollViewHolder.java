package com.yunbao.video.views;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.opensource.svgaplayer.SVGAImageView;
import com.yunbao.common.Constants;
import com.yunbao.common.bean.VideoBean;
import com.yunbao.common.event.FollowEvent;
import com.yunbao.common.event.LoginInvalidEvent;
import com.yunbao.common.event.LoginSuccessEvent;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.video.R;
import com.yunbao.video.activity.AbsVideoPlayActivity;
import com.yunbao.video.adapter.VideoScrollAdapter;
import com.yunbao.video.custom.VideoLoadingBar;
import com.yunbao.video.custom.VideoProgressBar;
import com.yunbao.video.event.VideoCommentEvent;
import com.yunbao.video.event.VideoDeleteEvent;
import com.yunbao.video.event.VideoInsertListEvent;
import com.yunbao.video.event.VideoLikeEvent;
import com.yunbao.video.event.VideoScrollPageEvent;
import com.yunbao.video.event.VideoShareEvent;
import com.yunbao.video.http.VideoHttpConsts;
import com.yunbao.video.http.VideoHttpUtil;
import com.yunbao.video.interfaces.VideoScrollDataHelper;
import com.yunbao.video.utils.VideoStorge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class VideoScrollViewHolder extends AbsViewHolder implements
        VideoScrollAdapter.ActionListener, SwipeRefreshLayout.OnRefreshListener,
        VideoPlayViewHolder.ActionListener, View.OnClickListener {

    private VideoPlayViewHolder mVideoPlayViewHolder;
    private View mPlayView;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private VideoScrollAdapter mVideoScrollAdapter;
    private int mPosition;
    private String mVideoKey;
    private VideoPlayWrapViewHolder mVideoPlayWrapViewHolder;
    private VideoLoadingBar mVideoLoadingBar;
    private VideoProgressBar mVideoProgressBar;
    private int mPage;
    private HttpCallback mRefreshCallback;//下拉刷新回调
    private HttpCallback mLoadMoreCallback;//上拉加载更多回调
    private VideoScrollDataHelper mVideoDataHelper;
    private VideoBean mVideoBean;
    private boolean mPaused;//生命周期暂停
    private boolean mHideBottom;
    private boolean mLoginChanged;//登录状态是否发生变化
    private int mLoginStatus = Constants.LOGIN_STATUS_NONE;
    private boolean mFromUserHome;//是来自个人主页


    public VideoScrollViewHolder(Context context, ViewGroup parentView, int position, String videoKey, int page, boolean hideBottom, boolean fromUserHome) {
        super(context, parentView, position, videoKey, page, hideBottom, fromUserHome);
    }

    @Override
    protected void processArguments(Object... args) {
        mPosition = (int) args[0];
        mVideoKey = (String) args[1];
        mPage = (int) args[2];
        mHideBottom = (boolean) args[3];
        mFromUserHome = (boolean) args[4];
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_video_scroll;
    }

    @Override
    public void init() {
        List<VideoBean> list = VideoStorge.getInstance().get(mVideoKey);
        if (list == null || list.size() == 0) {
            return;
        }
        mVideoPlayViewHolder = new VideoPlayViewHolder(mContext, null);
        mVideoPlayViewHolder.setActionListener(this);
        mPlayView = mVideoPlayViewHolder.getContentView();
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(R.color.global);
        mRefreshLayout.setEnabled(false);//产品不让使用刷新
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mVideoScrollAdapter = new VideoScrollAdapter(mContext, list, mPosition, mFromUserHome);
        mVideoScrollAdapter.setActionListener(this);
        mRecyclerView.setAdapter(mVideoScrollAdapter);
        ((DefaultItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mVideoLoadingBar = (VideoLoadingBar) findViewById(R.id.video_loading);
        mVideoProgressBar = (VideoProgressBar) findViewById(R.id.video_progress);
        findViewById(R.id.input_tip).setOnClickListener(this);
        findViewById(R.id.btn_face).setOnClickListener(this);
        if (mHideBottom) {
            findViewById(R.id.bottom).setVisibility(View.INVISIBLE);
        }
        EventBus.getDefault().register(this);
        mRefreshCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<VideoBean> list = JSON.parseArray(Arrays.toString(info), VideoBean.class);
                    if (mVideoScrollAdapter != null) {
                        mVideoScrollAdapter.setList(list);
                    }
                    ToastUtil.show(R.string.refresh_success);
                }
            }

            @Override
            public void onFinish() {
                if (mRefreshLayout != null) {
                    mRefreshLayout.setRefreshing(false);
                }
            }
        };
        mLoadMoreCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<VideoBean> list = JSON.parseArray(Arrays.toString(info), VideoBean.class);
                    if (list.size() > 0) {
                        if (mVideoScrollAdapter != null) {
                            mVideoScrollAdapter.insertList(list);
                        }
                        EventBus.getDefault().post(new VideoScrollPageEvent(mVideoKey, mPage));
                    } else {
                        ToastUtil.show(R.string.video_no_more_video);
                        mPage--;
                    }
                } else {
                    mPage--;
                }
            }
        };
        mVideoDataHelper = VideoStorge.getInstance().getDataHelper(mVideoKey);

    }

    @Override
    public void onPageSelected(final VideoPlayWrapViewHolder videoPlayWrapViewHolder, boolean needLoadMore) {
        if (videoPlayWrapViewHolder != null) {
            VideoBean videoBean = videoPlayWrapViewHolder.getVideoBean();
            if (videoBean != null) {
                mVideoPlayWrapViewHolder = videoPlayWrapViewHolder;
                mVideoBean = videoBean;
                if (mVideoLoadingBar != null) {
                    mVideoLoadingBar.setLoading(true);
                }
                videoPlayWrapViewHolder.addVideoView(mPlayView);
                mVideoPlayViewHolder.checkVideoPlay(mVideoBean, new CommonCallback<VideoBean>() {
                    @Override
                    public void callback(VideoBean bean) {
                        if (bean != null) {
                            bean.setCanPlay(VideoBean.CAN_PLAY);
                            mVideoPlayWrapViewHolder.setVideoInfo(bean);
                        } else {
                            mVideoBean.setCanPlay(VideoBean.NOT_CAN_PLAY);
                            mVideoPlayWrapViewHolder.setVideoInfo(mVideoBean);
                            if (mVideoLoadingBar != null) {
                                mVideoLoadingBar.setLoading(false);
                            }
                        }
                    }
                });
                mVideoPlayViewHolder.setPlayBtn(mVideoPlayWrapViewHolder.getPlayBtn());

            }
            if (needLoadMore) {
                onLoadMore();
            }
        }
    }

    @Override
    public void onPageOutWindow(VideoPlayWrapViewHolder vh) {
        if (mVideoPlayWrapViewHolder != null && mVideoPlayWrapViewHolder == vh && mVideoPlayViewHolder != null) {
            mVideoPlayViewHolder.stopPlay();

        }
        ((AbsVideoPlayActivity) mContext).hideGift();

    }


    @Override
    public void onVideoDelete(String videoId) {
        EventBus.getDefault().post(new VideoDeleteEvent(videoId, mVideoKey));
        ((AbsVideoPlayActivity) mContext).finishActivityOnVideoDelete(videoId);
    }

    @Override
    public void onVideoInsertList(int position, int count) {
        EventBus.getDefault().post(new VideoInsertListEvent(mVideoKey, position, count));
    }

    @Override
    public void release() {
        VideoHttpUtil.cancel(VideoHttpConsts.GET_HOME_VIDEO_LIST);
        VideoHttpUtil.cancel(VideoHttpConsts.GET_VIDEO);
        EventBus.getDefault().unregister(this);

        if (mVideoPlayViewHolder != null) {
            mVideoPlayViewHolder.release();
        }
        mVideoPlayWrapViewHolder = null;
        if (mVideoLoadingBar != null) {
            mVideoLoadingBar.endLoading();
        }
        mVideoLoadingBar = null;
        if (mRefreshLayout != null) {
            mRefreshLayout.setOnRefreshListener(null);
        }
        mRefreshLayout = null;
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.release();
        }
        mVideoScrollAdapter = null;
        mVideoDataHelper = null;
    }


    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {
        mPage = 1;
        if (mVideoDataHelper != null) {
            mVideoDataHelper.loadData(mPage, mRefreshCallback);
        }
    }

    /**
     * 加载更多
     */
    private void onLoadMore() {
        mPage++;
        if (mVideoDataHelper != null) {
            mVideoDataHelper.loadData(mPage, mLoadMoreCallback);
        }
    }

    @Override
    public void onPlayBegin() {
        if (mVideoLoadingBar != null) {
            mVideoLoadingBar.setLoading(false);
        }
    }

    @Override
    public void onPlayLoading() {
        if (mVideoLoadingBar != null) {
            mVideoLoadingBar.setLoading(true);
        }
    }

    @Override
    public void onFirstFrame() {
        if (mVideoPlayWrapViewHolder != null) {
            mVideoPlayWrapViewHolder.onFirstFrame();
        }
    }

    @Override
    public void onDoubleClick() {
        if (mVideoPlayWrapViewHolder != null) {
            mVideoPlayWrapViewHolder.onDoubleClick();
        }
    }

    @Override
    public void onPlayProgress(float progressVal) {
        if (mVideoProgressBar != null) {
            mVideoProgressBar.setProgressVal(progressVal);
        }
    }

    /**
     * 关注发生变化
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowEvent(FollowEvent e) {
        if (mVideoScrollAdapter != null && mVideoPlayWrapViewHolder != null) {
            VideoBean videoBean = mVideoPlayWrapViewHolder.getVideoBean();
            if (videoBean != null) {
                mVideoScrollAdapter.onFollowChanged(!mPaused, videoBean.getId(), e.getToUid(), e.getIsAttention());
            }
        }
    }

    /**
     * 点赞发生变化
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoLikeEvent(VideoLikeEvent e) {
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.onLikeChanged(!mPaused, e.getVideoId(), e.getIsLike(), e.getLikeNum());
        }
    }

    /**
     * 登录成功事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginSuccessEvent(LoginSuccessEvent e) {
        mLoginChanged = true;
        mLoginStatus = Constants.LOGIN_STATUS_SUCCESS;

    }

    /**
     * 登录失效事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginInvalidEvent(LoginInvalidEvent e) {
        mLoginChanged = true;
        mLoginStatus = Constants.LOGIN_STATUS_INVALID;

    }

    /**
     * 分享数发生变化
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoShareEvent(VideoShareEvent e) {
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.onShareChanged(e.getVideoId(), e.getShareNum());
        }
    }

    /**
     * 评论数发生变化
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoCommentEvent(VideoCommentEvent e) {
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.onCommentChanged(e.getVideoId(), e.getCommentNum());
        }
    }

    /**
     * 删除视频
     */
    public void deleteVideo(VideoBean videoBean) {
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.deleteVideo(videoBean);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.input_tip) {
            openCommentInputWindow(false);

        } else if (i == R.id.btn_face) {
            openCommentInputWindow(true);
        }
    }


    /**
     * 打开评论输入框
     */
    private void openCommentInputWindow(boolean openFace) {
        if (mVideoBean != null) {
            ((AbsVideoPlayActivity) mContext).openCommentInputWindow(openFace, mVideoBean.getId(), mVideoBean.getUid(), null, false);
        }
    }

    /**
     * VideoBean 数据发生变化
     */
    public void onVideoBeanChanged(String videoId) {
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.onVideoBeanChanged(videoId);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
        if (mVideoPlayViewHolder != null) {
            mVideoPlayViewHolder.pausePlay();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPaused) {
            if (mVideoPlayViewHolder != null) {
                mVideoPlayViewHolder.setLoginChange(mLoginChanged);
            }
            if (mLoginChanged) {
                mLoginChanged = false;
                if (mLoginStatus != Constants.LOGIN_STATUS_NONE) {
                    if (mVideoScrollAdapter != null) {
                        mVideoScrollAdapter.refreshFollowAndLike(mLoginStatus);
                    }
                    mLoginStatus = Constants.LOGIN_STATUS_NONE;
                }

            }
            mPaused = false;
            if (mVideoPlayViewHolder != null) {
                mVideoPlayViewHolder.resumePlay();
            }
        }
    }

    /**
     * 被动暂停
     */
    public void passivePause() {
        if (mVideoPlayViewHolder != null) {
            mVideoPlayViewHolder.passivePause();
        }
    }

    /**
     * 被动恢复
     */
    public void passiveResume() {
        if (mVideoPlayViewHolder != null) {
            mVideoPlayViewHolder.passiveResume();
        }
    }

    /**
     * 让播放器静音
     */
    public void setMute(boolean mute) {
        if (mVideoPlayViewHolder != null) {
            mVideoPlayViewHolder.setMute(mute);
        }
    }

    /**
     * 从播放列表中删除视频
     */
    public void deleteVideoFromPlay(String videoId) {
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.deleteVideoFromPlay(videoId);
        }
    }


    public SVGAImageView getSVGAImageView() {
        return (SVGAImageView) findViewById(R.id.gift_svga);
    }

    public GifImageView getGifImageView() {
        return (GifImageView) findViewById(R.id.gift_gif);
    }

    public View getGiftView() {
        return findViewById(R.id.gift_content);
    }

}
