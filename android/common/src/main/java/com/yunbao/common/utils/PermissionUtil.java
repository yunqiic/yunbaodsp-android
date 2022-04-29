package com.yunbao.common.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.yunbao.common.fragment.ProcessFragment;
import com.yunbao.common.interfaces.PermissionCallback;

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
public class PermissionUtil {

    public static void request(FragmentActivity activity, PermissionCallback callback, String... permission) {
        ProcessFragment processFragment = null;
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        List<Fragment> list = fragmentManager.getFragments();
        if (list != null && list.size() > 0) {
            for (Fragment fragment : list) {
                if (fragment != null && fragment instanceof ProcessFragment) {
                    processFragment = (ProcessFragment) fragment;
                    break;
                }
            }
        }
        if (processFragment == null) {
            processFragment = new ProcessFragment();
            FragmentTransaction tx = fragmentManager.beginTransaction();
            tx.add(processFragment, "ProcessFragment").commitNow();
        }
        processFragment.requestPermissions(callback, permission);
    }

}
