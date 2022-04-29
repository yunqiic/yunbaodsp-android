package com.yunbao.common.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.util.FileUtils;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.R;
import com.yunbao.common.interfaces.ActivityResultCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.ImageResultCallback;
import com.yunbao.common.interfaces.VideoResultCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class ProcessImageUtil extends ProcessResultUtil {

    private static final String FILE_PROVIDER="com.yunbao.shortvideo.fileprovider";
    private Context mContext;
    private String[] mCameraPermissions;
    private String[] mAlumbPermissions;
    private String[] mVideoPermissions;
    private CommonCallback<Boolean> mCameraPermissionCallback;
    private CommonCallback<Boolean> mAlumbPermissionCallback;
    private CommonCallback<Boolean> mVideoPermissionCallback;
    private CommonCallback<Boolean> mRecordVideoPermissionCallback;
    private ActivityResultCallback mCameraResultCallback;
    private ActivityResultCallback mAlumbResultCallback;
    private ActivityResultCallback mVideoResultCallback;
    private ActivityResultCallback mCropResultCallback;
    private ActivityResultCallback mRecordVideoResultCallback;
    private File mCameraResult;//拍照后得到的图片
    private File mCorpResult;//裁剪后得到的图片
    private File mVideoResult;//裁剪后得到的图片
    private ImageResultCallback mResultCallback;
    private VideoResultCallback mResultCallbackVideo;
    private boolean mNeedCrop;//是否需要裁剪

    public ProcessImageUtil(FragmentActivity activity) {
        super(activity);
        mContext = activity;
        mCameraPermissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };
        mAlumbPermissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        mVideoPermissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };
        mCameraPermissionCallback = new CommonCallback<Boolean>() {
            @Override
            public void callback(Boolean result) {
                if (result) {
                    takePhoto();
                }
            }
        };
        mAlumbPermissionCallback = new CommonCallback<Boolean>() {
            @Override
            public void callback(Boolean result) {
                if (result) {
                    chooseFile();
                }
            }
        };
        mVideoPermissionCallback = new CommonCallback<Boolean>() {
            @Override
            public void callback(Boolean result) {
                if (result) {
                    chooseVideoFile();
                }
            }
        };
        mRecordVideoPermissionCallback = new CommonCallback<Boolean>() {
            @Override
            public void callback(Boolean result) {
                if (result) {
                    record();
                }
            }
        };
        mCameraResultCallback = new ActivityResultCallback() {
            @Override
            public void onSuccess(Intent intent) {
                if (mNeedCrop) {
                    Uri uri = null;
                    if (Build.VERSION.SDK_INT >= 24) {
                        uri = FileProvider.getUriForFile(mContext, FILE_PROVIDER, mCameraResult);
                    } else {
                        uri = Uri.fromFile(mCameraResult);
                    }
                    if (uri != null) {
                        crop(uri);
                    }
                } else {
                    if (mResultCallback != null) {
                        mResultCallback.onSuccess(mCameraResult);
                    }
                }
            }

            @Override
            public void onFailure() {
                ToastUtil.show(R.string.img_camera_cancel);
            }
        };
        mAlumbResultCallback = new ActivityResultCallback() {
            @Override
            public void onSuccess(Intent intent) {
                if (mNeedCrop) {
                    crop(intent.getData());
                } else {
                    String path = FileUtils.getPath(mContext, intent.getData());
                    if (!TextUtils.isEmpty(path) && mResultCallback != null) {
                        mResultCallback.onSuccess(new File(path));
                    }
                }
            }

            @Override
            public void onFailure() {
                ToastUtil.show(R.string.img_alumb_cancel);
            }
        };
        mVideoResultCallback = new ActivityResultCallback() {
            @Override
            public void onSuccess(Intent intent) {
                String path = FileUtils.getPath(mContext, intent.getData());
                if (!TextUtils.isEmpty(path) && mResultCallbackVideo != null) {
                    mResultCallbackVideo.onSuccess(new File(path),0);
                }
            }

            @Override
            public void onFailure() {
                ToastUtil.show(R.string.img_alumb_cancel);
            }
        };

        mCropResultCallback = new ActivityResultCallback() {
            @Override
            public void onSuccess(Intent intent) {
                if (mResultCallback != null) {
                    mResultCallback.onSuccess(mCorpResult);
                }
            }

            @Override
            public void onFailure() {
                ToastUtil.show(R.string.img_crop_cancel);
            }
        };

        mRecordVideoResultCallback = new ActivityResultCallback() {
            @Override
            public void onSuccess(final Intent intent) {
                L.e("--onSuccess----" + intent + "----" + mVideoResult);
                if (mVideoResult != null && mResultCallbackVideo != null) {
                    mResultCallbackVideo.onSuccess(mVideoResult,0);
                }
            }
        };
    }

    /**
     * 拍照获取图片
     */
    public void getImageByCamera(boolean needCrop) {
        mNeedCrop = needCrop;
        requestPermissions(mCameraPermissions, mCameraPermissionCallback);
    }

    /**
     * 拍照获取图片
     */
    public void getImageByCamera() {
        getImageByCamera(true);
    }

    /**
     * 相册获取图片
     */
    public void getImageByAlumb(boolean needCrop) {
        mNeedCrop = needCrop;
        requestPermissions(mAlumbPermissions, mAlumbPermissionCallback);
    }

    /**
     * 相册获取图片
     */
    public void getImageByAlumb() {
        getImageByAlumb(true);
    }


    /**
     * 相册获取视频
     */
    public void getVideoByAlumb() {
        requestPermissions(mAlumbPermissions, mVideoPermissionCallback);
    }


    /**
     * 录制视频
     */
    public void getVideoRecord() {
        requestPermissions(mVideoPermissions, mRecordVideoPermissionCallback);
    }


    public void record() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);// 表示跳转至相机的录视频界面
        mVideoResult = getNewVideoFile();
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(mContext, FILE_PROVIDER, mVideoResult);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(mVideoResult);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        //好使
        //intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        //intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 60 * 1048576L);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15);
        startActivityForResult(intent, mRecordVideoResultCallback);
    }

    /**
     * 开启摄像头，执行照相
     */
    private void takePhoto() {
        if (mResultCallback != null) {
            mResultCallback.beforeCamera();
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mCameraResult = getNewFile();
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(mContext, FILE_PROVIDER, mCameraResult);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(mCameraResult);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, mCameraResultCallback);
    }

    private File getNewFile() {
        // 裁剪头像的绝对路径
        File dir = new File(CommonAppConfig.CAMERA_IMAGE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, DateFormatUtil.getCurTimeString() + ".png");
    }

    private File getNewFile2() {
        // 裁剪头像的绝对路径
        File dir = new File(CommonAppConfig.INNER_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, DateFormatUtil.getCurTimeString() + ".png");
    }

    private File getNewVideoFile() {
        // 裁剪头像的绝对路径
        File dir = new File(CommonAppConfig.VIDEO_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, DateFormatUtil.getCurTimeString() + ".mp4");
    }


    /**
     * 打开相册，选择文件
     */
    private void chooseFile() {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }
        startActivityForResult(intent, mAlumbResultCallback);
    }


    /**
     * 打开相册，选择视频文件
     */
    private void chooseVideoFile() {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }
        startActivityForResult(intent, mVideoResultCallback);
    }

    /**
     * 裁剪图片
     */
    public void cropImage(File file) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(mContext, FILE_PROVIDER, file);
        } else {
            uri = Uri.fromFile(file);
        }
        if (uri != null) {
            crop(uri);
        }
    }

    /**
     * 裁剪
     */
    private void crop(Uri inputUri) {

        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        File copyFile = null;
        try {
            inputStream = mContext.getContentResolver().openInputStream(inputUri);
            File dir = new File(CommonAppConfig.INNER_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            copyFile = new File(dir, StringUtil.generateFileName() + ".png");
            outputStream = new FileOutputStream(copyFile);
            byte[] buf = new byte[4096];
            int len = 0;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            copyFile = null;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (copyFile == null || copyFile.length() == 0) {
            return;
        }
//        L.e("ProcessImageUtil------>复制成功");

        mCorpResult = getNewFile2();
        try {
            Uri resultUri = Uri.fromFile(mCorpResult);
            if (resultUri == null || mFragment == null || mContext == null) {
                return;
            }
            UCrop uCrop = UCrop.of(Uri.fromFile(copyFile), resultUri)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(400, 400);
            Intent intent = uCrop.getIntent(mContext);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, mCropResultCallback);
        } catch (Exception e) {
            try {
                Uri resultUri = FileProvider.getUriForFile(mContext, FILE_PROVIDER, mCorpResult);
                if (resultUri == null || mFragment == null || mContext == null) {
                    return;
                }
                UCrop uCrop = UCrop.of(Uri.fromFile(copyFile), resultUri)
                        .withAspectRatio(1, 1)
                        .withMaxResultSize(400, 400);
                Intent intent = uCrop.getIntent(mContext);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(intent, mCropResultCallback);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    


    public void setImageResultCallback(ImageResultCallback resultCallback) {
        mResultCallback = resultCallback;
    }

    public void setVideosultCallback(VideoResultCallback resultCallback) {
        mResultCallbackVideo = resultCallback;
    }


    @Override
    public void release() {
        super.release();
        mResultCallback = null;
    }
}
