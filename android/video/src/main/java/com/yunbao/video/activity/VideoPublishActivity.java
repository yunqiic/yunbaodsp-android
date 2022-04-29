package com.yunbao.video.activity;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.video.R;
import com.yunbao.video.adapter.VideoPubCoverAdapter;
import com.yunbao.video.bean.VideoPubCoverBean;
import com.yunbao.video.event.VideoUploadEvent;
import com.yunbao.video.http.VideoHttpConsts;
import com.yunbao.video.http.VideoHttpUtil;
import com.yunbao.video.upload.VideoUploadBean;
import com.yunbao.video.upload.VideoUploadCallback;
import com.yunbao.video.upload.VideoUploadStrategy;
import com.yunbao.video.upload.VideoUploadUtil;
import com.yunbao.video.utils.VideoCoverUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;
import top.zibin.luban.OnRenameListener;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class VideoPublishActivity extends AbsActivity implements View.OnClickListener {


    public static void forward(Context context, String videoPath, String videoWaterPath, int saveType, String musicId) {
        Intent intent = new Intent(context, VideoPublishActivity.class);
        intent.putExtra(Constants.VIDEO_PATH, videoPath);
        intent.putExtra(Constants.VIDEO_PATH_WATER, videoWaterPath);
        intent.putExtra(Constants.VIDEO_SAVE_TYPE, saveType);
        intent.putExtra(Constants.VIDEO_MUSIC_ID, musicId);
        context.startActivity(intent);
    }

    private static final String TAG = "VideoPublishActivity";
    private TextView mNum;
    private String mVideoPath;
    private String mVideoPathWater;
    private VideoUploadStrategy mUploadStrategy;
    private EditText mInput;
    private String mVideoTitle;//视频标题
    private Dialog mLoading;
    private int mSaveType;
    private View mBtnPub;
    private String mMusicId;
    private boolean mIsDestroyed;
    private VideoUploadBean mVideoUploadBean;
    private View mCoverContainer;
    private ObjectAnimator mShowAnimator;
    private ObjectAnimator mHideAnimator;
    private ImageView mCover;
    private ImageView mCover2;
    private RecyclerView mRecyclerViewCover;
    private VideoPubCoverAdapter mCoverAdapter;
    private double mVideoRatio;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_publish;
    }

    @Override
    protected void main() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle(WordUtil.getString(R.string.video_pub));
        Intent intent = getIntent();
        mVideoPath = intent.getStringExtra(Constants.VIDEO_PATH);
        mVideoPathWater = intent.getStringExtra(Constants.VIDEO_PATH_WATER);
        mSaveType = intent.getIntExtra(Constants.VIDEO_SAVE_TYPE, Constants.VIDEO_SAVE_SAVE_AND_PUB);

        mMusicId = intent.getStringExtra(Constants.VIDEO_MUSIC_ID);
        mBtnPub = findViewById(R.id.btn_pub);
        mBtnPub.setOnClickListener(this);
        mNum = (TextView) findViewById(R.id.num);
        mInput = (EditText) findViewById(R.id.input);

        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mNum != null) {
                    mNum.setText(s.length() + "/50");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mCover = findViewById(R.id.cover);
        mCover2 = findViewById(R.id.cover_2);
        findViewById(R.id.cover_btn).setOnClickListener(this);
        findViewById(R.id.cover_close).setOnClickListener(this);
        findViewById(R.id.cover_confirm).setOnClickListener(this);
        mRecyclerViewCover = findViewById(R.id.recyclerView_cover);
        mRecyclerViewCover.setHasFixedSize(true);
        mRecyclerViewCover.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        List<VideoPubCoverBean> coverBeanList = VideoCoverUtil.getInstance().getList();
        if (coverBeanList != null && coverBeanList.size() > 0) {
            mCover.setImageBitmap(coverBeanList.get(0).getBitmap());
            mCover2.setImageBitmap(coverBeanList.get(0).getBitmap());
            mCoverAdapter = new VideoPubCoverAdapter(mContext, coverBeanList);
            mCoverAdapter.setOnItemClickListener(new OnItemClickListener<VideoPubCoverBean>() {
                @Override
                public void onItemClick(VideoPubCoverBean bean, int position) {
                    if (mCover2 != null) {
                        mCover2.setImageBitmap(bean.getBitmap());
                    }
                }
            });
            mRecyclerViewCover.setAdapter(mCoverAdapter);
        }

        mCoverContainer = findViewById(R.id.cover_container);
        mCoverContainer.post(new Runnable() {
            @Override
            public void run() {
                mCoverContainer.setTranslationY(mCoverContainer.getHeight());
                if (mShowAnimator == null) {
                    mShowAnimator = ObjectAnimator.ofFloat(mCoverContainer, "translationY", mCoverContainer.getHeight(), 0);
                    mShowAnimator.setDuration(300);
                    mShowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                }
                if (mHideAnimator == null) {
                    mHideAnimator = ObjectAnimator.ofFloat(mCoverContainer, "translationY", 0, mCoverContainer.getHeight());
                    mHideAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    mHideAnimator.setDuration(300);
                }
            }
        });

    }


    public void release() {
        if (mIsDestroyed) {
            return;
        }
        mIsDestroyed = true;
        CommonHttpUtil.cancel(CommonHttpConsts.GET_CONFIG);
        VideoHttpUtil.cancel(VideoHttpConsts.SAVE_UPLOAD_VIDEO_INFO);
        if (mUploadStrategy != null) {
            mUploadStrategy.cancel();
        }
        if (mShowAnimator != null) {
            mShowAnimator.cancel();
            mShowAnimator.removeAllListeners();
        }
        if (mHideAnimator != null) {
            mHideAnimator.cancel();
            mHideAnimator.removeAllListeners();
        }
        mShowAnimator = null;
        mHideAnimator = null;
        mUploadStrategy = null;
    }

    @Override
    public void onBackPressed() {
        DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.video_give_up_pub), new DialogUitl.SimpleCallback() {
            @Override
            public void onConfirmClick(Dialog dialog, String content) {
                if (mSaveType == Constants.VIDEO_SAVE_PUB) {
                    if (!TextUtils.isEmpty(mVideoPath)) {
                        File file = new File(mVideoPath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
                if (!TextUtils.isEmpty(mVideoPathWater)) {
                    File file = new File(mVideoPathWater);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                release();
                finish();
            }
        });
    }


    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
        L.e(TAG, "-------->onDestroy");
    }

    /**
     * 发送发布视频事件
     */
    private void publishEvent() {
        VideoUploadEvent ev = new VideoUploadEvent();
        ev.setVideoPath(mVideoPath);
        ev.setVideoPathWater(mVideoPathWater);
        ev.setMusicId(mMusicId);
        ev.setSaveType(mSaveType);
        ev.setVideoTitle(mInput.getText().toString().trim());
        ev.setIsUserad(0);
        ev.setUseradUrl("");
        EventBus.getDefault().post(ev);
        finish();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_pub) {
            publishEvent();
        } else if (i == R.id.cover_btn) {
            showCoverContainer();
        } else if (i == R.id.cover_close) {
            hideCoverContainer();
        } else if (i == R.id.cover_confirm) {
            coverConfirm();
        }
    }


    private void showCoverContainer() {
        if (mCoverContainer != null && mCoverContainer.getVisibility() != View.VISIBLE) {
            mCoverContainer.setVisibility(View.VISIBLE);
        }
        if (mShowAnimator != null) {
            mShowAnimator.start();
        }
    }


    private void hideCoverContainer() {
//        mClickPause = true;
//        if (mPlayStarted && mPlayer != null) {
//            mPlayer.pause();
//        }
        if (mHideAnimator != null) {
            mHideAnimator.start();
        }
    }

    private void coverConfirm() {
        if (mCoverAdapter != null) {
            List<VideoPubCoverBean> list = mCoverAdapter.getList();
            if (list != null && list.size() > 0) {
                for (VideoPubCoverBean bean : list) {
                    if (bean.isChecked()) {
                        bean.setUse(true);
                        if (mCover != null) {
                            mCover.setImageBitmap(bean.getBitmap());
                        }
                    } else {
                        bean.setUse(false);
                    }
                }
            }
        }
        hideCoverContainer();
    }


    /**
     * 发布视频
     */
    private void publishVideo() {
        mBtnPub.setEnabled(false);
        if (TextUtils.isEmpty(mVideoPath)) {
            return;
        }
        mLoading = DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.video_pub_ing));
        mLoading.show();
        mVideoRatio = 0;
        Bitmap bitmap = null;
        //生成视频封面图
        MediaMetadataRetriever mmr = null;
        try {
//            mmr = new MediaMetadataRetriever();
//            mmr.setDataSource(mVideoPath);
            //bitmap = mmr.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST);
             bitmap = VideoCoverUtil.getInstance().getCoverBitmap();
            mVideoRatio = ((double) bitmap.getHeight()) / bitmap.getWidth();
        } catch (Exception e) {
            bitmap = null;
            e.printStackTrace();
        } finally {
            if (mmr != null) {
                mmr.release();
            }
        }
        if (bitmap == null) {
            ToastUtil.show(R.string.video_cover_img_failed);
            onFailed();
            return;
        }

        final String coverImagePath = mVideoPath.replace(".mp4", ".jpg");
        File imageFile = new File(coverImagePath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            imageFile = null;
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        if (imageFile == null) {
            ToastUtil.show(R.string.video_cover_img_failed);
            onFailed();
            return;
        }
        final File finalImageFile = imageFile;
        //用鲁班压缩图片
        Luban.with(this)
                .load(finalImageFile)
                .setFocusAlpha(false)
                .ignoreBy(8)//8k以下不压缩
                .setTargetDir(CommonAppConfig.INNER_PATH)
                .setRenameListener(new OnRenameListener() {
                    @Override
                    public String rename(String filePath) {
                        filePath = filePath.substring(filePath.lastIndexOf("/") + 1);
                        return filePath.replace(".jpg", "_c.jpg");
                    }
                })
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(File file) {
                        if (!finalImageFile.getAbsolutePath().equals(file.getAbsolutePath()) && finalImageFile.exists()) {
                            finalImageFile.delete();
                        }
                        uploadVideoFile(file);
                    }

                    @Override
                    public void onError(Throwable e) {
                        uploadVideoFile(finalImageFile);
                    }
                }).launch();

    }

    private void onFailed() {
        if (mLoading != null) {
            mLoading.dismiss();
        }
        if (mBtnPub != null) {
            mBtnPub.setEnabled(true);
        }
    }

    /**
     * 上传封面图片和视频
     */
    private void uploadVideoFile(final File imageFile) {
        VideoUploadUtil.startUpload(new CommonCallback<VideoUploadStrategy>() {
            @Override
            public void callback(VideoUploadStrategy videoUploadStrategy) {
                if (videoUploadStrategy == null) {
                    ToastUtil.show(R.string.video_pub_failed);
                    return;
                }
                mUploadStrategy = videoUploadStrategy;
                final VideoUploadBean videoUploadBean = new VideoUploadBean(new File(mVideoPath), imageFile);
                if (!TextUtils.isEmpty(mVideoPathWater)) {
                    File waterFile = new File(mVideoPathWater);
                    if (waterFile.exists()) {
                        videoUploadBean.setVideoWaterFile(waterFile);
                    }
                }
                doUpload(videoUploadBean);
            }
        });


    }

    private void doUpload(VideoUploadBean videoUploadBean) {
        mUploadStrategy.upload(videoUploadBean, new VideoUploadCallback() {
            @Override
            public void onSuccess(VideoUploadBean bean) {
                if (mSaveType == Constants.VIDEO_SAVE_PUB) {
                    bean.deleteFile();
                }
                mVideoUploadBean = bean;
                saveUploadVideoInfo();
            }

            @Override
            public void onFailure() {
                ToastUtil.show(R.string.video_pub_failed);
                onFailed();
            }

            @Override
            public void onProgress(int progress) {
                L.e("VideoUploadTxImpl-----onProgress-----> " + progress);
            }
        });
    }


    /**
     * 把视频上传后的信息保存在服务器
     */
    private void saveUploadVideoInfo() {
        if (mVideoUploadBean == null) {
            return;
        }
        mVideoTitle = mInput.getText().toString().trim();
        VideoHttpUtil.saveUploadVideoInfo("0",  0 , mVideoTitle, mVideoUploadBean.getResultImageUrl(), mVideoUploadBean.getResultVideoUrl(), mVideoUploadBean.getResultWaterVideoUrl(), mMusicId, 0, "", mVideoRatio,0,"", new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    ToastUtil.show(R.string.video_pub_success);
                } else {
                    ToastUtil.show(msg);
                    onFailed();
                }
            }

            @Override
            public void onFinish() {
                if (mLoading != null) {
                    mLoading.dismiss();
                }
            }
        });
    }





}
