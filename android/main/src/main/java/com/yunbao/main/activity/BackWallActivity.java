package com.yunbao.main.activity;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.ImageResultCallback;
import com.yunbao.common.interfaces.PermissionCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.DownloadUtil;
import com.yunbao.common.utils.FileUtil;
import com.yunbao.common.utils.MediaUtil;
import com.yunbao.common.utils.PermissionUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;

import java.io.File;


// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class BackWallActivity extends AbsActivity implements  View.OnClickListener {
    private String backWallThumb;
    private TextView backWallChange;
    private ImageView mBackWall;
    private ImageView downLoad;
    private Bitmap bitmap;
    private DownloadUtil mDownloadUtil;
    private String imageName= StringUtil.generateFileName() + ".png";


    private ImageResultCallback mImageResultCallback = new ImageResultCallback() {
        @Override
        public void beforeCamera() {

        }

        @Override
        public void onSuccess(File file) {
            if (file != null) {
                Intent intent = new Intent();
                //把返回数据存入Intent
                intent.putExtra("result", file.getAbsolutePath());
                //设置返回数据
                setResult(RESULT_OK, intent);
                //关闭Activity
                finish();
            }
        }

        @Override
        public void onFailure() {
        }
    };


    @Override
    protected int getLayoutId() {
        return R.layout.activity_back_wall;
    }

    @Override
    protected void main() {
        Intent intent=getIntent();
        backWallThumb=intent.getStringExtra("backWallThumb");
        mBackWall=findViewById(R.id.backWall);
        backWallChange=findViewById(R.id.backwall_change);
        downLoad=findViewById(R.id.download);
        ImgLoader.display(mContext, backWallThumb, mBackWall);
        mDownloadUtil = new DownloadUtil();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i==R.id.backwall_change){
            chooseImage();
        }else if (i==R.id.download){
            download();
        }else if (i==R.id.btn_back){
            finish();
        }else if (i==R.id.backWall){
            finish();
        }

    }

    /**
     * 选择图片
     */
    public void chooseImage() {
        DialogUitl.showStringArrayDialog(mContext, new Integer[]{
                R.string.camera, R.string.alumb}, new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int tag) {
                if (tag == R.string.camera) {
                    MediaUtil.getImageByCamera(BackWallActivity.this,false, mImageResultCallback);
                } else {
                    MediaUtil.getImageByAlumb(BackWallActivity.this, false,mImageResultCallback);
                }
            }
        });
    }

    /**
     * 保存图片到本地
     */
    private void download(){
        PermissionUtil.request((AbsActivity) mContext,
                new PermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        mDownloadUtil.download("thumb", CommonAppConfig.IMAGE_PATH, imageName, backWallThumb, new DownloadUtil.Callback() {
                            @Override
                            public void onSuccess(File file) {
                                FileUtil.saveImageToGallery(mContext,file);
                            }

                            @Override
                            public void onProgress(int progress) {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });

                    }

                },
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        );

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
