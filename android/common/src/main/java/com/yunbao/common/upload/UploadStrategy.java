package com.yunbao.common.upload;

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

public interface UploadStrategy {

    /**
     * 执行上传
     *
     * @param list         被上传的文件列表
     * @param needCompress 是否需要压缩
     * @param callback     上传回调
     */
    void upload(List<UploadBean> list, boolean needCompress, UploadCallback callback);

    /**
     * 取消上传
     */
    void cancelUpload();
}
