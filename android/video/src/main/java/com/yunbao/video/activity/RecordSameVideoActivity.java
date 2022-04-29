package com.yunbao.video.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.bean.MusicBean;
import com.yunbao.common.bean.VideoBean;
import com.yunbao.common.custom.CommonRefreshView;
import com.yunbao.common.custom.ItemDecoration;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.DateFormatUtil;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.DownloadUtil;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.video.R;
import com.yunbao.video.adapter.VideoItemSameAdapter;
import com.yunbao.video.http.VideoHttpConsts;
import com.yunbao.video.http.VideoHttpUtil;
import com.yunbao.video.interfaces.VideoScrollDataHelper;
import com.yunbao.video.presenter.CheckVideoRecordPresenter;
import com.yunbao.video.utils.VideoStorge;

import java.io.File;
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
public class RecordSameVideoActivity extends AbsActivity implements OnItemClickListener<VideoBean>, View.OnClickListener {
    private CommonRefreshView mRefreshView;
    private VideoItemSameAdapter mAdapter;
    private ImageView mIvBg;
    private ImageView mIvAvatar;
    private TextView mTvTitle;
    private TextView mTvName;
    private TextView mTvTitle2;
    private TextView mTvJoinNum;
    private View mBtnTakeVideo;
    private VideoBean mVideoBean;
    private MusicBean mMusicBean;
    private int mDpBot;

    private Dialog mDownloadVideoDialog;
    protected DownloadUtil mDownloadUtil;
    private VideoScrollDataHelper mVideoScrollDataHelper;
    //用来获取本地和网络media相关文件的信息
    private MediaMetadataRetriever mMetadataRetriever;

    private CheckVideoRecordPresenter mCheckVideoRecordPresenter;

    public static void forward(Context context, VideoBean bean) {
        Intent intent = new Intent(context, RecordSameVideoActivity.class);
        intent.putExtra(Constants.VIDEO_BEAN, bean);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_record_same_video;
    }

    @Override
    protected void main() {
        super.main();
        mVideoBean = getIntent().getParcelableExtra(Constants.VIDEO_BEAN);
        if (mVideoBean == null) {
            return;
        }
        mDpBot = DpUtil.dp2px(85);
        mTvTitle2 = findViewById(R.id.titleView);
        mBtnTakeVideo = findViewById(R.id.btn_take_same);
        mIvBg = findViewById(R.id.bg);
        mIvAvatar = findViewById(R.id.avatar);
        mTvTitle = findViewById(R.id.tv_title);
        mTvName = findViewById(R.id.tv_name);
        mTvJoinNum = findViewById(R.id.tv_joinnum);
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        mRefreshView = findViewById(R.id.refreshLayout);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false);
        mRefreshView.setLayoutManager(gridLayoutManager);
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 2, 0);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRefreshView.setItemDecoration(decoration);
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<VideoBean>() {
            @Override
            public RefreshAdapter<VideoBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new VideoItemSameAdapter(mContext);
                    mAdapter.setOnItemClickListener(RecordSameVideoActivity.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                VideoHttpUtil.getVideoListByMusic(mVideoBean.getId(), mVideoBean.getMusicId(), p, callback);
            }

            @Override
            public List<VideoBean> processData(String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                mMusicBean = JSON.parseObject(obj.getString("musicinfo"), MusicBean.class);
                mMusicBean.setId(String.valueOf(mVideoBean.getMusicId()));
                updateView();
                return JSON.parseArray(obj.getString("videolist"), VideoBean.class);
            }

            @Override
            public void onRefreshSuccess(List<VideoBean> list, int listCount) {
                VideoStorge.getInstance().put(Constants.VIDEO_MUSIC, list);
            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<VideoBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mBtnTakeVideo.setOnClickListener(this);
        mTvName.setOnClickListener(this);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float totalScrollRange = appBarLayout.getTotalScrollRange();
                float rate = -1 * verticalOffset / totalScrollRange;
                float curY = mBtnTakeVideo.getTranslationY();
                float targetY = rate * mDpBot;
                if (curY != targetY) {
                    mBtnTakeVideo.setTranslationY(targetY);
                }
                mTvTitle2.setAlpha(rate);
            }
        });
    }

    private void updateView() {
        ImgLoader.displayBlur(mContext, mMusicBean.getImgUrl(), mIvBg);
        ImgLoader.display(mContext, mMusicBean.getImgUrl(), mIvAvatar);
        mTvTitle2.setText(mMusicBean.getTitle());
        mTvTitle.setText(mMusicBean.getTitle());
        mTvName.setText(mMusicBean.getAuthor());
        mTvJoinNum.setText(mMusicBean.getUseNum());
    }

    @Override
    public void onItemClick(VideoBean bean, final int position) {
        checkVideo(bean.getId(), new CommonCallback<Integer>() {
            @Override
            public void callback(Integer bean) {
                int page = 1;
                if (mRefreshView != null) {
                    page = mRefreshView.getPageCount();
                }
                if (mVideoScrollDataHelper == null) {
                    mVideoScrollDataHelper = new VideoScrollDataHelper() {

                        @Override
                        public void loadData(int p, HttpCallback callback) {
                            VideoHttpUtil.getVideoListByMusic(mVideoBean.getId(), mVideoBean.getMusicId(), p, callback);
                        }
                    };
                }
                VideoStorge.getInstance().putDataHelper(Constants.VIDEO_MUSIC, mVideoScrollDataHelper);
                VideoPlayActivity.forward(mContext, position, Constants.VIDEO_MUSIC, page, false);
            }
        });
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_take_same) {
            clickTakeVideo();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    /**
     * 点击拍同款
     */
    private void clickTakeVideo() {
        if (!CommonAppConfig.getInstance().isLogin()) {
            RouteUtil.forwardLogin(mContext);
            return;
        }
        if ( mVideoBean == null) {
            return;
        }
        if (mCheckVideoRecordPresenter == null) {
            mCheckVideoRecordPresenter = new CheckVideoRecordPresenter(mContext);
            mCheckVideoRecordPresenter.setRecordCallBack(new CheckVideoRecordPresenter.RecordCallBack() {
                @Override
                public void onRecord(VideoBean videoBean) {
                    takeVideo();
                }
            });
        }
        mCheckVideoRecordPresenter.checkStatusStartRecord(mVideoBean);
    }

    private void takeVideo() {
        if (mMusicBean == null) {
            return;
        }
        mDownloadVideoDialog = DialogUitl.loadingDialog(mContext);
        mDownloadVideoDialog.show();
        if (mDownloadUtil == null) {
            mDownloadUtil = new DownloadUtil();
        }
        String fileName = "YB_MUSIC_" + mVideoBean.getTitle() + "_" + DateFormatUtil.getCurTimeString() + ".mp3";
        mDownloadUtil.download(mVideoBean.getTag(), CommonAppConfig.MUSIC_PATH, fileName, mMusicBean.getFileUrl(), new DownloadUtil.Callback() {
            @Override
            public void onSuccess(File file) {
                if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
                    mDownloadVideoDialog.dismiss();
                }
                mDownloadVideoDialog = null;
                String path = file.getAbsolutePath();
                mMusicBean.setLocalPath(path);
                long bgmDuration = 0;
                if (mMetadataRetriever == null) {
                    mMetadataRetriever = new MediaMetadataRetriever();
                }
                try {
                    mMetadataRetriever.setDataSource(path);
                    String duration = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    bgmDuration = Long.parseLong(duration);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mMusicBean.setDuration(bgmDuration);
                VideoRecordActivity.forward(mContext, mMusicBean, Constants.VIDEO_RECORD_TYPE_RECORD_SAME);

            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.show(R.string.download_failed);
                if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
                    mDownloadVideoDialog.dismiss();
                }
                mDownloadVideoDialog = null;
            }
        });
    }


    @Override
    protected void onDestroy() {
        mVideoScrollDataHelper = null;
        VideoHttpUtil.cancel(VideoHttpConsts.GET_VIDEO_LIST_BY_MUSIC);
        VideoStorge.getInstance().remove(Constants.VIDEO_MUSIC);
        super.onDestroy();
    }
}
