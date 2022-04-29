package com.yunbao.common.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.R;
import com.yunbao.common.interfaces.ActivityResultCallback;
import com.yunbao.common.interfaces.PermissionCallback;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;

import java.util.LinkedHashMap;
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
public class ProcessFragment extends Fragment {

    private Context mContext;
    private PermissionCallback mPermissionCallback;
    private ActivityResultCallback mActivityResultCallback;
    private LinkedHashMap<String, Boolean> mMap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    /**
     * 申请权限
     */
    public void requestPermissions(PermissionCallback callback, String... permissions) {
        if (callback == null || permissions == null || permissions.length == 0) {
            return;
        }
        boolean isAllGranted = true;
        if (mMap == null) {
            mMap = new LinkedHashMap<>();
        } else {
            mMap.clear();
        }
        for (String permission : permissions) {
            boolean isGranted = ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED;
            mMap.put(permission, isGranted);
            if (!isGranted) {
                isAllGranted = false;
            }
        }
        if (isAllGranted) {
            callback.onAllGranted();
            callback.onResult(mMap);
            mPermissionCallback = null;
        } else {
            mPermissionCallback = callback;
            requestPermissions(permissions, 0);
        }
    }


    /**
     * 申请权限结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean isAllGranted = true;
        if (mMap == null) {
            mMap = new LinkedHashMap<>();
        } else {
            mMap.clear();
        }
        for (int i = 0, len = grantResults.length; i < len; i++) {
            boolean isGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            mMap.put(permissions[i], isGranted);
            if (!isGranted) {
                isAllGranted = false;
            }
        }
        if (isAllGranted) {
            if (mPermissionCallback != null) {
                mPermissionCallback.onAllGranted();
            }
        } else {
            showTip();
        }
        if (mPermissionCallback != null) {
            mPermissionCallback.onResult(mMap);
        }
        mPermissionCallback = null;
    }


    /**
     * 拒绝某项权限时候的提示
     */
    private void showTip() {
        if (mMap == null && mMap.size() == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Boolean> entry : mMap.entrySet()) {
            if (!entry.getValue()) {
                switch (entry.getKey()) {
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        String permissionStorage = WordUtil.getString(R.string.permission_storage);
                        if (sb.indexOf(permissionStorage) < 0) {
                            sb.append(permissionStorage);
                            sb.append("，");
                        }
                        break;
                    case Manifest.permission.CAMERA:
                        sb.append(WordUtil.getString(R.string.permission_camera));
                        sb.append("，");
                        break;
                    case Manifest.permission.RECORD_AUDIO:
                        sb.append(WordUtil.getString(R.string.permission_record_audio));
                        sb.append("，");
                        break;
                    case Manifest.permission.ACCESS_COARSE_LOCATION:
                        sb.append(WordUtil.getString(R.string.permission_location));
                        sb.append("，");
                        CommonAppConfig.getInstance().clearLocationInfo();
                        break;
                    case Manifest.permission.READ_PHONE_STATE:
                        sb.append(WordUtil.getString(R.string.permission_read_phone_state));
                        sb.append("，");
                        break;
                    case Manifest.permission.CALL_PHONE:
                        sb.append(WordUtil.getString(R.string.permission_call));
                        sb.append("，");
                        break;
                }
            }
        }
        String s = sb.toString();
        if (!TextUtils.isEmpty(s) && s.length() > 1) {
            s = s.substring(0, s.length() - 1);
        }
        final String tip = String.format(WordUtil.getString(R.string.permission_refused), s);
        CommonAppContext.postDelayed(new Runnable() {
            @Override
            public void run() {
                ToastUtil.show(tip);
            }
        }, 300);
    }


    public void startActivityForResult(Intent intent, ActivityResultCallback callback) {
        mActivityResultCallback = callback;
        super.startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mActivityResultCallback != null) {
            mActivityResultCallback.onResult(resultCode, data);
            if (resultCode == -1) {//RESULT_OK
                mActivityResultCallback.onSuccess(data);
            } else {
                mActivityResultCallback.onFailure();
            }
        }
    }
    public void release() {
        mPermissionCallback = null;
        mActivityResultCallback = null;
    }

}
