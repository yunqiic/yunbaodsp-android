package com.yunbao.common.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.EncodeStrategy;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.R;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Map;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;


// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class ImgLoader {

    private static final boolean SKIP_MEMORY_CACHE = false;
    private static Headers sHeaders;
    private static BlurTransformation sBlurTransformation;

    static {
        sHeaders = new Headers() {
            @Override
            public Map<String, String> getHeaders() {
                return CommonAppConfig.HEADER;
            }
        };
        sBlurTransformation = new BlurTransformation(25);
    }

    public static void display(Context context, String url, ImageView imageView) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Glide.with(context).asDrawable().load(new GlideUrl(url, sHeaders)).skipMemoryCache(SKIP_MEMORY_CACHE).into(imageView);
    }

    public static void displayWithError(Context context, String url, ImageView imageView, int errorRes) {
        if (context == null) {
            return;
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Glide.with(context).asDrawable().load(new GlideUrl(url, sHeaders)).skipMemoryCache(SKIP_MEMORY_CACHE).error(errorRes).into(new SoftReference<ImageView>(imageView).get());
    }

    public static void displayAvatar(Context context, String url, ImageView imageView) {
        if (context == null) {
            return;
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        displayWithError(context, url, imageView, R.mipmap.icon_avatar_placeholder);
    }

    public static void display(Context context, File file, ImageView imageView) {
        if (context == null || file == null || !file.exists()) {
            return;
        }
        Glide.with(context).asDrawable().load(file).skipMemoryCache(SKIP_MEMORY_CACHE).into(imageView);
    }

    public static void display(Context context, int res, ImageView imageView) {
        if (context == null) {
            return;
        }
        Glide.with(context).asDrawable().load(res).skipMemoryCache(SKIP_MEMORY_CACHE).into(imageView);
    }

    /**
     * 显示视频封面缩略图
     */
    public static void displayVideoThumb(Context context, String videoPath, ImageView imageView) {
        if (context == null) {
            return;
        }

        Glide.with(context).asDrawable().load(Uri.fromFile(new File(videoPath))).skipMemoryCache(SKIP_MEMORY_CACHE).into(imageView);
    }


    /**
     * 显示视频封面缩略图
     */
    public static void displayVideoThumb(Context context, File file, ImageView imageView) {
        if (context == null) {
            return;
        }
        Glide.with(context).asDrawable().load(Uri.fromFile(file)).skipMemoryCache(SKIP_MEMORY_CACHE).into(imageView);
    }

    public static void displayDrawable(Context context, String url, final DrawableCallback callback) {
        if (context == null) {
            return;
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Glide.with(context).asDrawable().load(new GlideUrl(url, sHeaders)).skipMemoryCache(SKIP_MEMORY_CACHE).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                if (callback != null) {
                    callback.callback(resource);
                }
            }
        });
    }


    public static void clear(Context context, ImageView imageView) {
        Glide.with(context).clear(imageView);
    }


    public static void clearMemory(Context context) {
        Glide.get(context).clearMemory();
    }


    /**
     * 显示模糊的毛玻璃图片
     */
    public static void displayBlur(Context context, String url, ImageView imageView) {
        if (context == null) {
            return;
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Glide.with(context).asDrawable().load(new GlideUrl(url, sHeaders))
                .skipMemoryCache(SKIP_MEMORY_CACHE)
                .diskCacheStrategy(new DiskCacheStrategy() {
                    @Override
                    public boolean isDataCacheable(DataSource dataSource) {
                        return true;
                    }

                    @Override
                    public boolean isResourceCacheable(boolean isFromAlternateCacheKey, DataSource dataSource, EncodeStrategy encodeStrategy) {
                        return true;
                    }

                    @Override
                    public boolean decodeCachedResource() {
                        return true;
                    }

                    @Override
                    public boolean decodeCachedData() {
                        return true;
                    }
                })
                .apply(RequestOptions.bitmapTransform(sBlurTransformation))
                .into(imageView);

    }


    public static void displayDrawable(Context context, String url, final DrawableCallback2 callback) {
        if (context == null) {
            return;
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Glide.with(context).asDrawable().load(new GlideUrl(url, sHeaders)).skipMemoryCache(SKIP_MEMORY_CACHE).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                if (callback != null) {
                    callback.onLoadSuccess(resource);
                }
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                if (callback != null) {
                    callback.onLoadFailed();
                }
            }

        });
    }


    public interface DrawableCallback {
        void callback(Drawable drawable);
    }

    public interface DrawableCallback2 {
        void onLoadSuccess(Drawable drawable);

        void onLoadFailed();
    }

}
