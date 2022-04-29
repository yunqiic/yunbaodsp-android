package com.yunbao.common.utils;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.interfaces.CommonCallback;

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

public class GifCacheUtil {

    public static void getFile(String fileName, String url, final CommonCallback<File> commonCallback) {
        if (commonCallback == null) {
            return;
        }
        File dir = new File(CommonAppConfig.GIF_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        if (file.exists()) {
            commonCallback.callback(file);
        } else {
            DownloadUtil downloadUtil = new DownloadUtil();
            downloadUtil.download(CommonHttpConsts.DOWNLOAD_GIF, dir, fileName, url, new DownloadUtil.Callback() {
                @Override
                public void onSuccess(File file) {
                    commonCallback.callback(file);
                }

                @Override
                public void onProgress(int progress) {

                }

                @Override
                public void onError(Throwable e) {
                    commonCallback.callback(null);
                }
            });
        }
    }

}
