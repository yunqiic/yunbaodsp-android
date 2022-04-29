package com.yunbao.video.http;

import android.text.TextUtils;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.http.HttpClient;
import com.yunbao.common.utils.MD5Util;
import com.yunbao.common.utils.StringUtil;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class VideoHttpUtil {

    private static final String VIDEO_SALT = "#2hgfk85cm23mk58vncsark";

    /**
     * 取消网络请求
     */
    public static void cancel(String tag) {
        HttpClient.getInstance().cancel(tag);
    }

    /**
     * 获取首页视频列表
     */
    public static void getHomeVideoList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.GetVideoList", VideoHttpConsts.GET_HOME_VIDEO_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }


    /**
     * 视频点赞
     */
    public static void setVideoLike(String tag, String videoid, HttpCallback callback) {
        HttpClient.getInstance().get("Video.AddLike", tag)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoid", videoid)
                .execute(callback);
    }

    /**
     * 获取视频评论
     */
    public static void getVideoCommentList(String videoid, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.GetComments", VideoHttpConsts.GET_VIDEO_COMMENT_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoid", videoid)
                .params("p", p)
                .execute(callback);
    }

    /**
     * 评论点赞
     */
    public static void setCommentLike(String commentid, HttpCallback callback) {
        HttpClient.getInstance().get("Video.addCommentLike", VideoHttpConsts.SET_COMMENT_LIKE)
                .params("commentid", commentid)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }

    /**
     * 发表评论
     */
    public static void setComment(String toUid, String videoId, String content, String commentId, String parentId, String atInfo, HttpCallback callback) {
        HttpClient.getInstance().get("Video.setComment", VideoHttpConsts.SET_COMMENT)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("touid", toUid)
                .params("videoid", videoId)
                .params("commentid", commentId)
                .params("parentid", parentId)
                .params("content", content)
                .params("at_info", atInfo)
                .params("type", 0)
                .params("voice", "")
                .params("length", 0)
                .execute(callback);
    }


    /**
     * 发表语音评论
     */
    public static void setVoiceComment(String toUid, String videoId, String commentId, String parentId, String voiceLink, int voiceDuration, HttpCallback callback) {
        HttpClient.getInstance().get("Video.setComment", VideoHttpConsts.SET_COMMENT)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("touid", toUid)
                .params("videoid", videoId)
                .params("commentid", commentId)
                .params("parentid", parentId)
                .params("content", "")
                .params("at_info", "")
                .params("type", 1)
                .params("voice", voiceLink)
                .params("length", voiceDuration)
                .execute(callback);
    }


    /**
     * 获取评论回复
     */
    public static void getCommentReply(String commentid, String lastReplyId, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getReplys", VideoHttpConsts.GET_COMMENT_REPLY)
                .params("commentid", commentid)
                .params("last_replyid", lastReplyId)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }


    /**
     * 删除评论
     */
    public static void deleteComment(String videoId, String commentid, String commentUid, HttpCallback callback) {
        HttpClient.getInstance().get("Video.delComments", VideoHttpConsts.DELETE_COMMENT)
                .params("videoid", videoId)
                .params("commentid", commentid)
                .params("commentuid", commentUid)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }


    /**
     * 获取视频音乐分类列表
     */
    public static void getMusicClassList(HttpCallback callback) {
        HttpClient.getInstance().get("Music.classify_list", VideoHttpConsts.GET_MUSIC_CLASS_LIST)
                .execute(callback);
    }

    /**
     * 获取热门视频音乐列表
     */
    public static void getHotMusicList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Music.hotLists", VideoHttpConsts.GET_HOT_MUSIC_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 音乐收藏
     */
    public static void setMusicCollect(String muiscId, HttpCallback callback) {
        HttpClient.getInstance().get("Music.collectMusic", VideoHttpConsts.SET_MUSIC_COLLECT)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("musicid", muiscId)
                .execute(callback);
    }

    /**
     * 音乐收藏列表
     */
    public static void getMusicCollectList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Music.getCollectMusicLists", VideoHttpConsts.GET_MUSIC_COLLECT_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 获取具体分类下的音乐列表
     */
    public static void getMusicList(String classId, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Music.music_list", VideoHttpConsts.GET_MUSIC_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("classify", classId)
                .params("p", p)
                .execute(callback);
    }


    /**
     * 搜索音乐
     */
    public static void videoSearchMusic(String key, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Music.searchMusic", VideoHttpConsts.VIDEO_SEARCH_MUSIC)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("key", key)
                .params("p", p)
                .execute(callback);
    }





    /**
     * 短视频上传信息
     *
     * @param title   短视频标题
     * @param thumb   短视频封面图url
     * @param href    短视频视频url
     * @param musicId 背景音乐Id
     */
    public static void saveUploadVideoInfo(String coin, int classId, String title, String thumb, String href, String waterHref, String musicId, int labelId, String goodsId, double videoRatio,int is_userad,String userad_url, HttpCallback callback) {
        HttpClient.getInstance().get("Video.setVideo", VideoHttpConsts.SAVE_UPLOAD_VIDEO_INFO)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("lat", CommonAppConfig.getInstance().getLat())
                .params("lng", CommonAppConfig.getInstance().getLng())
                .params("city", CommonAppConfig.getInstance().getCity())
                .params("title", title)
                .params("thumb", thumb)
                .params("href", href)
                .params("href_w", TextUtils.isEmpty(waterHref) ? href : waterHref)
                .params("music_id", musicId)
                .params("labelid", labelId)
                .params("goodsid", TextUtils.isEmpty(goodsId) ? "" : goodsId)
                .params("classid", classId)
                .params("coin", TextUtils.isEmpty(coin) ? "0" : coin)
                .params("anyway", videoRatio == 0 ? "1.778" : String.format("%.3f", videoRatio))
                .params("is_userad", is_userad)
                .params("userad_url", userad_url)
                .execute(callback);
    }





    /**
     * 获取举报内容列表
     */
    public static void getVideoReportList(HttpCallback callback) {
        HttpClient.getInstance().get("Video.getReportContentlist", VideoHttpConsts.GET_VIDEO_REPORT_LIST)
                .execute(callback);
    }

    /**
     * 举报视频接口
     */
    public static void videoReport(String videoId, int reportId, String content, HttpCallback callback) {
        HttpClient.getInstance().get("Video.report", VideoHttpConsts.VIDEO_REPORT)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoid", videoId)
                .params("type", reportId)
                .params("content", content)
                .execute(callback);
    }

    /**
     * 删除自己的视频
     */
    public static void videoDelete(String videoid, HttpCallback callback) {
        HttpClient.getInstance().get("Video.del", VideoHttpConsts.VIDEO_DELETE)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoid", videoid)
                .execute(callback);
    }




    /**
     * 完整观看完视频后请求这个接口
     */
    public static void videoWatchEnd(String videoUid, String videoId) {
        String uid = CommonAppConfig.getInstance().getUid();
        if (TextUtils.isEmpty(uid) || uid.equals(videoUid)) {
            return;
        }
        VideoHttpUtil.cancel(VideoHttpConsts.VIDEO_WATCH_END);
        String s = MD5Util.getMD5(uid + "-" + videoId + "-" + VIDEO_SALT);
        HttpClient.getInstance().get("Video.setConversion", VideoHttpConsts.VIDEO_WATCH_END)
                .params("uid", uid)
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoid", videoId)
                .params("random_str", s)
                .execute(NO_CALLBACK);
    }




    //不做任何操作的HttpCallback
    private static final HttpCallback NO_CALLBACK = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {

        }
    };

    /**
     * 获取单个视频信息，主要是该视频关于自己的信息 ，如是否关注，是否点赞等
     *
     * @param videoId 视频的id
     */
    public static void getVideo(String videoId, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getVideo", VideoHttpConsts.GET_VIDEO)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("videoid", videoId)
                .params("mobileid", CommonAppConfig.getInstance().getDeviceId())
                .execute(callback);
    }



    /**
     * 视频分类
     */
    public static void getClassLists(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getClassLists", VideoHttpConsts.GET_CLASS_LISTS)
                .params("p", p)
                .execute(callback);
    }

    /**
     * 搜索视频分类
     */
    public static void searchClassLists(String key, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.searchClassLists", VideoHttpConsts.SEARCH_CLASS)
                .params("keywords", key)
                .params("p", p)
                .execute(callback);
    }


    /**
     * 根据视频分类获取视频列表
     *
     * @param classId
     * @param p
     * @param callback
     */
    public static void getVideoListByClass(int classId, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getVideoListByClass", VideoHttpConsts.GET_CLASS_VIDEO_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("classid", classId)
                .params("p", p)
                .execute(callback);
    }



    /**
     * 视频扣费
     *
     * @param videoid
     * @param callback
     */
    public static void setVideoPay(String videoid, HttpCallback callback) {
        HttpClient.getInstance().get("Video.setVideoPay", VideoHttpConsts.SET_VIDEO_PAY)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoid", videoid)
                .execute(callback);
    }

    /**
     * 根据音乐id获取视频列表
     *
     * @param videoid
     * @param musicId
     * @param p
     * @param callback
     */
    public static void getVideoListByMusic(String videoid, int musicId, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getVideoListByMusic", VideoHttpConsts.GET_VIDEO_LIST_BY_MUSIC)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("videoid", videoid)
                .params("musicid", musicId)
                .params("p", p)
                .execute(callback);
    }

}




