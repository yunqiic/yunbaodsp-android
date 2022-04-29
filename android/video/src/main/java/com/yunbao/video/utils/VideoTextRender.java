package com.yunbao.video.utils;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.activity.WebViewActivity;
import com.yunbao.common.custom.VerticalImageSpan;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.FaceUtil;
import com.yunbao.video.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class VideoTextRender {

    private static ForegroundColorSpan sColorSpan1;
    private static AbsoluteSizeSpan sFontSizeSpan1;
    private static final int FACE_WIDTH;
    private static final int VIDEO_FACE_WIDTH;
    private static final String REGEX = "\\[([\u4e00-\u9fa5\\w])+\\]";
    private static final Pattern PATTERN;

    static {
        sColorSpan1 = new ForegroundColorSpan(0xffc8c8c8);
        sFontSizeSpan1 = new AbsoluteSizeSpan(12, true);
        FACE_WIDTH = DpUtil.dp2px(20);
        VIDEO_FACE_WIDTH = DpUtil.dp2px(16);
        PATTERN = Pattern.compile(REGEX);
    }

    /**
     * 聊天表情
     */
    public static CharSequence getFaceImageSpan(String content, int imgRes) {
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        Drawable faceDrawable = ContextCompat.getDrawable(CommonAppContext.getInstance(), imgRes);
        faceDrawable.setBounds(0, 0, FACE_WIDTH, FACE_WIDTH);
        ImageSpan imageSpan = new ImageSpan(faceDrawable, ImageSpan.ALIGN_BOTTOM);
        builder.setSpan(imageSpan, 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    /**
     * 评论内容，解析里面的表情
     */
    public static CharSequence renderVideoComment(String content, String addTime, String atInfo) {
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        Matcher matcher = PATTERN.matcher(content);
        while (matcher.find()) {
            // 获取匹配到的具体字符
            String key = matcher.group();
            Integer imgRes = FaceUtil.getFaceImageRes(key);
            if (imgRes != null && imgRes != 0) {
                Drawable faceDrawable = ContextCompat.getDrawable(CommonAppContext.getInstance(), imgRes);
                if (faceDrawable != null) {
                    faceDrawable.setBounds(0, 0, VIDEO_FACE_WIDTH, VIDEO_FACE_WIDTH);
                    ImageSpan imageSpan = new ImageSpan(faceDrawable, ImageSpan.ALIGN_BOTTOM);
                    // 匹配字符串的开始位置
                    int startIndex = matcher.start();
                    builder.setSpan(imageSpan, startIndex, startIndex + key.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        int startIndex = builder.length();
        builder.append(addTime);
        int endIndex = startIndex + addTime.length();
        builder.setSpan(sColorSpan1, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(sFontSizeSpan1, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (!TextUtils.isEmpty(atInfo)) {
            try {
                JSONArray jsonArray = JSONArray.parseArray(atInfo);
                for (int i = 0, size = jsonArray.size(); i < size; i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String atContent = "@" + obj.getString("name");
                    int index = content.indexOf(atContent);
                    if (index >= 0) {
                        builder.setSpan(new ForegroundColorSpan(0xfff3e835), index, index + atContent.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                return builder;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return builder;
    }

    public static CharSequence renderVideoTitle(String title, boolean ad, final Runnable runnable) {
        if (ad) {
            SpannableStringBuilder builder = new SpannableStringBuilder(title);
            Drawable adDrawable = ContextCompat.getDrawable(CommonAppContext.getInstance(), R.mipmap.icon_video_ad_detail);
            if (adDrawable != null) {
                builder.append("  ");
                adDrawable.setBounds(0, 0, DpUtil.dp2px(32), DpUtil.dp2px(16));
                int length = builder.length();
                builder.setSpan(new VerticalImageSpan(adDrawable), length - 1, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ClickableSpan clickableSpan = new ClickableSpan() {

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }

                    @Override
                    public void onClick(View widget) {
                        if (runnable != null) {
                            runnable.run();
                        }
                    }
                };
                builder.setSpan(clickableSpan, length - 1, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return builder;
        }
        return title;
    }

}
