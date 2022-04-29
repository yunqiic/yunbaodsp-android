package com.yunbao.video.utils;

import com.yunbao.video.R;

import java.util.Arrays;
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

public class VideoIconUtil {
    private static List<Integer> sVideoLikeAnim;//视频点赞动画
    private static List<Integer> sVideoCancelLikeAnim;//视频取消点赞动画

    static {
        sVideoLikeAnim = Arrays.asList(
                R.mipmap.icon_video_zan_02,
                R.mipmap.icon_video_zan_03,
                R.mipmap.icon_video_zan_04,
                R.mipmap.icon_video_zan_05,
                R.mipmap.icon_video_zan_06,
                R.mipmap.icon_video_zan_07,
                R.mipmap.icon_video_zan_08,
                R.mipmap.icon_video_zan_09,
                R.mipmap.icon_video_zan_10,
                R.mipmap.icon_video_zan_11,
                R.mipmap.icon_video_zan_12
        );

        sVideoCancelLikeAnim = Arrays.asList(
                R.mipmap.icon_video_zan_cancel_01,
                R.mipmap.icon_video_zan_cancel_02,
                R.mipmap.icon_video_zan_cancel_03,
                R.mipmap.icon_video_zan_cancel_04,
                R.mipmap.icon_video_zan_cancel_05,
                R.mipmap.icon_video_zan_cancel_06,
                R.mipmap.icon_video_zan_01
        );
    }

    public static List<Integer> getVideoLikeAnim() {
        return sVideoLikeAnim;
    }

    public static List<Integer> getVideoCancelLikeAnim() {
        return sVideoCancelLikeAnim;
    }

}
