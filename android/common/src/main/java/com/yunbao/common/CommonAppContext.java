package com.yunbao.common;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.yunbao.common.utils.L;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class CommonAppContext extends MultiDexApplication {

    private static CommonAppContext sInstance;
    private static Handler sMainThreadHandler;
    private int mCount;
    private boolean mFront;//是否前台

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        registerActivityLifecycleCallbacks();
    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(this);
        super.attachBaseContext(base);
    }

    public static CommonAppContext getInstance() {
        if (sInstance == null) {
            try {
                Class clazz = Class.forName("android.app.ActivityThread");
                Method method = clazz.getMethod("currentApplication", new Class[]{});
                Object obj = method.invoke(null, new Object[]{});
                if (obj != null && obj instanceof CommonAppContext) {
                    sInstance = (CommonAppContext) obj;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sInstance;
    }

    /**
     * 获取主线程的Handler
     */
    private static void getMainThreadHandler() {
        try {
            Class clazz = Class.forName("android.app.ActivityThread");
            Field field = clazz.getDeclaredField("sMainThreadHandler");
            field.setAccessible(true);
            Object obj = field.get(clazz);
            if (obj != null && obj instanceof Handler) {
                sMainThreadHandler = (Handler) obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void postDelayed(Runnable runnable, long delayMillis) {
        if (sMainThreadHandler == null) {
            getMainThreadHandler();
        }
        if (sMainThreadHandler != null) {
            sMainThreadHandler.postDelayed(runnable, delayMillis);
        }
    }

    public static void post(Runnable runnable) {
        if (sMainThreadHandler == null) {
            getMainThreadHandler();
        }
        if (sMainThreadHandler != null) {
            sMainThreadHandler.post(runnable);
        }
    }


    private void registerActivityLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mCount++;
                if (!mFront) {
                    mFront = true;
                    L.e("AppContext------->处于前台");
                    CommonAppConfig.getInstance().setFrontGround(true);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mCount--;
                if (mCount == 0) {
                    mFront = false;
                    L.e("AppContext------->处于后台");
                    CommonAppConfig.getInstance().setFrontGround(false);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

}
