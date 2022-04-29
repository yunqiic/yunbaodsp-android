package com.yunbao.common;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.SparseArray;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.bean.LevelBean;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.DeviceUtils;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.SpUtil;
import com.yunbao.common.utils.StringUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
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

public class CommonAppConfig {
    public static final String PACKAGE_NAME = "com.yunbao.shortvideo";
    //Http请求头 Header
    public static final Map<String, String> HEADER = new HashMap<>();
    //域名
    public static final String HOST = getHost();
    //外部sd卡
//    public static final String DCMI_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
    public static final String DCMI_PATH = getOutPath();
    //内部存储 /data/data/<application package>/files目录
    public static final String INNER_PATH = CommonAppContext.getInstance().getFilesDir().getAbsolutePath();
    //文件夹名字
    private static final String DIR_NAME = "yunbao";
    //保存视频的时候，在sd卡存储短视频的路径DCIM下
    public static final String VIDEO_DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + DIR_NAME + "/video/";
    public static final String VIDEO_PATH = StringUtil.contact(DCMI_PATH, "/video");
    public static final String VIDEO_PATH_RECORD = StringUtil.contact(DCMI_PATH, "/record");
    public static final String VIDEO_RECORD_TEMP_PATH = StringUtil.contact(VIDEO_PATH, "/recordParts");
    //下载音乐的时候保存的路径
    public static final String MUSIC_PATH = DCMI_PATH + "/" + DIR_NAME + "/music/";
    //拍照时图片保存路径
    public static final String CAMERA_IMAGE_PATH = DCMI_PATH + "/" + DIR_NAME + "/camera/";

    public static final String GIF_PATH = INNER_PATH + "/gif/";

    public static final String VOICE_PATH = INNER_PATH + "/voice/";
    public static final String IMAGE_PATH = INNER_PATH + "/image/";

    public static final String WATER_MARK_PATH = INNER_PATH + "/water/";

    private static String getOutPath() {
        String outPath = null;
        try {
            File externalFilesDir = CommonAppContext.getInstance().getExternalFilesDir(DIR_NAME);
            if (externalFilesDir != null) {
                if (!externalFilesDir.exists()) {
                    externalFilesDir.mkdirs();
                }
                outPath = externalFilesDir.getAbsolutePath();
            }
        } catch (Exception e) {
            outPath = null;
        }
        if (TextUtils.isEmpty(outPath)) {
            outPath =  CommonAppContext.getInstance().getFilesDir().getAbsolutePath();
        }
        return outPath;
    }

    private static CommonAppConfig sInstance;

    private CommonAppConfig() {

    }

    public static CommonAppConfig getInstance() {
        if (sInstance == null) {
            synchronized (CommonAppConfig.class) {
                if (sInstance == null) {
                    sInstance = new CommonAppConfig();
                }
            }
        }
        return sInstance;
    }

    public static final String NOT_LOGIN_UID = "-1";
    private String mUid;
    private String mToken;
    private ConfigBean mConfig;
    private double mLng;
    private double mLat;
    private String mProvince;//省
    private String mCity;//市
    private String mDistrict;//区
    private UserBean mUserBean;
    private String mUserPkAvatar;
    private String mUserPkName;
    private String mVersion;
    private boolean mLoginIM;//IM是否登录了
    private boolean mLaunched;//App是否启动了
    private String mJPushAppKey;//极光推送的AppKey
    private String mTxMapAppKey;//腾讯定位，地图的AppKey
    private String mTxMapAppSecret;//腾讯地图的AppSecret
    private boolean mFrontGround;
    private int mAppIconRes;
    private Boolean mMhBeautyEnable;//是否使用美狐 true使用美狐 false 使用基础美颜

    private SparseArray<LevelBean> mLevelMap;
    private SparseArray<LevelBean> mAnchorLevelMap;


    private boolean mAgentSwitch;
    private String mDeviceId;

    public String getUid() {
        if (TextUtils.isEmpty(mUid)) {
            String[] uidAndToken = SpUtil.getInstance()
                    .getMultiStringValue(new String[]{SpUtil.UID, SpUtil.TOKEN});
            if (uidAndToken != null) {
                if (!TextUtils.isEmpty(uidAndToken[0]) && !TextUtils.isEmpty(uidAndToken[1])) {
                    mUid = uidAndToken[0];
                    mToken = uidAndToken[1];
                } else {
                    mUid = NOT_LOGIN_UID;
                    mToken = null;
                }
            } else {
                mUid = NOT_LOGIN_UID;
                mToken = null;
            }
        }
        return mUid;
    }

    public String getToken() {
        return mToken;
    }

    public boolean isLogin() {
        if (NOT_LOGIN_UID.equals(getUid()) || TextUtils.isEmpty(mToken)) {
            return false;
        }
        return true;
    }

    public ConfigBean getConfig() {
        if (mConfig == null) {
            String configString = SpUtil.getInstance().getStringValue(SpUtil.CONFIG);
            if (!TextUtils.isEmpty(configString)) {
                mConfig = JSON.parseObject(configString, ConfigBean.class);
            }
        }
        return mConfig;
    }

    public void getConfig(CommonCallback<ConfigBean> callback) {
        if (callback == null) {
            return;
        }
        ConfigBean configBean = getConfig();
        if (configBean != null) {
            callback.callback(configBean);
        } else {
            CommonHttpUtil.getConfig(callback);
        }
    }

    public void setConfig(ConfigBean config) {
        mConfig = config;
    }

    /**
     * 经度
     */
    public double getLng() {
        if (mLng == 0) {
            String lng = SpUtil.getInstance().getStringValue(SpUtil.LOCATION_LNG);
            if (!TextUtils.isEmpty(lng)) {
                try {
                    mLng = Double.parseDouble(lng);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return mLng;
    }

    /**
     * 纬度
     */
    public double getLat() {
        if (mLat == 0) {
            String lat = SpUtil.getInstance().getStringValue(SpUtil.LOCATION_LAT);
            if (!TextUtils.isEmpty(lat)) {
                try {
                    mLat = Double.parseDouble(lat);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return mLat;
    }

    /**
     * 省
     */
    public String getProvince() {
        if (TextUtils.isEmpty(mProvince)) {
            mProvince = SpUtil.getInstance().getStringValue(SpUtil.LOCATION_PROVINCE);
        }
        return mProvince == null ? "" : mProvince;
    }

    /**
     * 市
     */
    public String getCity() {
        if (TextUtils.isEmpty(mCity)) {
            mCity = SpUtil.getInstance().getStringValue(SpUtil.LOCATION_CITY);
        }
        return mCity == null ? "" : mCity;
    }

    /**
     * 区
     */
    public String getDistrict() {
        if (TextUtils.isEmpty(mDistrict)) {
            mDistrict = SpUtil.getInstance().getStringValue(SpUtil.LOCATION_DISTRICT);
        }
        return mDistrict == null ? "" : mDistrict;
    }

    public void setUserBean(UserBean bean) {
        mUserBean = bean;
    }

    public UserBean getUserBean() {
        if (mUserBean == null) {
            String userBeanJson = SpUtil.getInstance().getStringValue(SpUtil.USER_INFO);
            if (!TextUtils.isEmpty(userBeanJson)) {
                mUserBean = JSON.parseObject(userBeanJson, UserBean.class);
            }
        }
        return mUserBean;
    }

    public void setUserPkAvatar(String avatar) {
        mUserPkAvatar = avatar;
        SpUtil.getInstance().setStringValue(SpUtil.PK_AVATAR, mUserPkAvatar);
    }

    public String getUserPkAvatar() {
        if (mUserPkAvatar == null) {
            mUserPkAvatar = SpUtil.getInstance().getStringValue(SpUtil.PK_AVATAR);
        }
        return mUserPkAvatar;
    }

    public void setUserPkName(String name) {
        mUserPkName = name;
        SpUtil.getInstance().setStringValue(SpUtil.PK_NAME,mUserPkName);
    }

    public String getUserPkName() {
        if (mUserPkName == null) {
            mUserPkName = SpUtil.getInstance().getStringValue(SpUtil.PK_NAME);
    }

        return mUserPkName;
    }

    /**
     * 设置萌颜是否可用
     */
    public void setMhBeautyEnable(boolean mhBeautyEnable) {
        mMhBeautyEnable = mhBeautyEnable;
        SpUtil.getInstance().setBooleanValue(SpUtil.TI_BEAUTY_ENABLE, mhBeautyEnable);
    }

    public boolean isMhBeautyEnable() {
        if (mMhBeautyEnable == null) {
            mMhBeautyEnable = SpUtil.getInstance().getBooleanValue(SpUtil.TI_BEAUTY_ENABLE);
        }
        return mMhBeautyEnable;
    }

    /**
     * 设置登录信息
     */
    public void setLoginInfo(String uid, String token, boolean save) {
        L.e("登录成功", "uid------>" + uid);
        L.e("登录成功", "token------>" + token);
        mUid = uid;
        mToken = token;
        if (save) {
            Map<String, String> map = new HashMap<>();
            map.put(SpUtil.UID, uid);
            map.put(SpUtil.TOKEN, token);
            SpUtil.getInstance().setMultiStringValue(map);
        }
    }

    /**
     * 清除登录信息
     */
    public void clearLoginInfo() {
        SpUtil.getInstance().removeValue(
                SpUtil.UID, SpUtil.TOKEN, SpUtil.USER_INFO, SpUtil.IM_LOGIN,
                Constants.CASH_ACCOUNT_ID, Constants.CASH_ACCOUNT, Constants.CASH_ACCOUNT_TYPE, Constants.CASH_ACCOUNT_NAME
        );
        mUid = null;
        mToken = null;
        mLoginIM = false;
    }


    /**
     * 设置位置信息
     *
     * @param lng      经度
     * @param lat      纬度
     * @param province 省
     * @param city     市
     */
    public void setLocationInfo(double lng, double lat, String province, String city, String district) {
        mLng = lng;
        mLat = lat;
        mProvince = province;
        mCity = city;
        mDistrict = district;
        Map<String, String> map = new HashMap<>();
        map.put(SpUtil.LOCATION_LNG, String.valueOf(lng));
        map.put(SpUtil.LOCATION_LAT, String.valueOf(lat));
        map.put(SpUtil.LOCATION_PROVINCE, province);
        map.put(SpUtil.LOCATION_CITY, city);
        map.put(SpUtil.LOCATION_DISTRICT, district);
        SpUtil.getInstance().setMultiStringValue(map);
    }

    /**
     * 清除定位信息
     */
    public void clearLocationInfo() {
        mLng = 0;
        mLat = 0;
        mProvince = null;
        mCity = null;
        mDistrict = null;
        SpUtil.getInstance().removeValue(
                SpUtil.LOCATION_LNG,
                SpUtil.LOCATION_LAT,
                SpUtil.LOCATION_PROVINCE,
                SpUtil.LOCATION_CITY,
                SpUtil.LOCATION_DISTRICT);

    }


    public boolean isLoginIM() {
        return mLoginIM;
    }

    public void setLoginIM(boolean loginIM) {
        mLoginIM = loginIM;
    }

    /**
     * 获取版本号
     */
    public String getVersion() {
        if (TextUtils.isEmpty(mVersion)) {
            try {
                PackageManager manager = CommonAppContext.getInstance().getPackageManager();
                PackageInfo info = manager.getPackageInfo(CommonAppContext.getInstance().getPackageName(), 0);
                mVersion = info.versionName;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mVersion;
    }


    /**
     * 获取App图标的资源id
     */
    public int getAppIconRes() {
        if (mAppIconRes == 0) {
            mAppIconRes = CommonAppContext.getInstance().getResources().getIdentifier("icon_app", "mipmap", "com.yunbao.shortvideo");
        }
        return mAppIconRes;
    }

    /**
     * 获取MetaData中的极光AppKey
     *
     * @return
     */
    public String getJPushAppKey() {
        if (mJPushAppKey == null) {
            mJPushAppKey = getMetaDataString("JPUSH_APPKEY");
        }
        return mJPushAppKey;
    }


    /**
     * 获取MetaData中的腾讯定位，地图的AppKey
     *
     * @return
     */
    public String getTxMapAppKey() {
        if (mTxMapAppKey == null) {
            mTxMapAppKey = getMetaDataString("TencentMapSDK");
        }
        return mTxMapAppKey;
    }


    /**
     * 获取MetaData中的腾讯定位，地图的AppSecret
     *
     * @return
     */
    public String getTxMapAppSecret() {
        if (mTxMapAppSecret == null) {
            mTxMapAppSecret = getMetaDataString("TencentMapAppSecret");
        }
        return mTxMapAppSecret;
    }

    public static String getMetaDataString(String key) {
        String res = null;
        try {
            ApplicationInfo appInfo = CommonAppContext.getInstance().getPackageManager().getApplicationInfo(CommonAppContext.getInstance().getPackageName(), PackageManager.GET_META_DATA);
            res = appInfo.metaData.getString(key);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static boolean getMetaDataBoolean(String key) {
        boolean res = false;
        try {
            ApplicationInfo appInfo = CommonAppContext.getInstance().getPackageManager().getApplicationInfo(PACKAGE_NAME, PackageManager.GET_META_DATA);
            res = appInfo.metaData.getBoolean(key);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static int getMetaDataInt(String key) {
        int res = 0;
        try {
            ApplicationInfo appInfo = CommonAppContext.getInstance().getPackageManager().getApplicationInfo(PACKAGE_NAME, PackageManager.GET_META_DATA);
            res = appInfo.metaData.getInt(key, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 判断某APP是否安装
     */
    public static boolean isAppExist(String packageName) {
        if (!TextUtils.isEmpty(packageName)) {
            PackageManager manager = CommonAppContext.getInstance().getPackageManager();
            List<PackageInfo> list = manager.getInstalledPackages(0);
            for (PackageInfo info : list) {
                if (packageName.equalsIgnoreCase(info.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isLaunched() {
        return mLaunched;
    }

    public void setLaunched(boolean launched) {
        mLaunched = launched;
    }

    //app是否在前台
    public boolean isFrontGround() {
        return mFrontGround;
    }

    //app是否在前台
    public void setFrontGround(boolean frontGround) {
        mFrontGround = frontGround;
    }

    private static String getHost() {
        String host = getMetaDataString("SERVER_HOST");
        HEADER.put("referer", host);
        return host;
    }


    public String getVotesName() {
        ConfigBean configBean = getConfig();
        if (configBean != null) {
            return configBean.getVotesName();
        }
        return Constants.VOTES;
    }

    public String getCoinName() {
        ConfigBean configBean = getConfig();
        if (configBean != null) {
            return configBean.getCoinName();
        }
        return Constants.DIAMONDS;
    }

    /**
     * 保存用户等级信息
     */
    public void setLevel(String levelJson) {
        if (TextUtils.isEmpty(levelJson)) {
            return;
        }
        List<LevelBean> list = JSON.parseArray(levelJson, LevelBean.class);
        if (list == null || list.size() == 0) {
            return;
        }
        if (mLevelMap == null) {
            mLevelMap = new SparseArray<>();
        }
        mLevelMap.clear();
        for (LevelBean bean : list) {
            mLevelMap.put(bean.getLevel(), bean);
        }
    }

    /**
     * 保存主播等级信息
     */
    public void setAnchorLevel(String anchorLevelJson) {
        if (TextUtils.isEmpty(anchorLevelJson)) {
            return;
        }
        List<LevelBean> list = JSON.parseArray(anchorLevelJson, LevelBean.class);
        if (list == null || list.size() == 0) {
            return;
        }
        if (mAnchorLevelMap == null) {
            mAnchorLevelMap = new SparseArray<>();
        }
        mAnchorLevelMap.clear();
        for (LevelBean bean : list) {
            mAnchorLevelMap.put(bean.getLevel(), bean);
        }
    }

    /**
     * 获取用户等级
     */
    public LevelBean getLevel(int level) {
        if (mLevelMap == null) {
            String configString = SpUtil.getInstance().getStringValue(SpUtil.CONFIG);
            if (!TextUtils.isEmpty(configString)) {
                JSONObject obj = JSON.parseObject(configString);
                setLevel(obj.getString("level"));
            }
        }
        if (mLevelMap == null || mLevelMap.size() == 0) {
            return null;
        }
        return mLevelMap.get(level);
    }

    /**
     * 获取主播等级
     */
    public LevelBean getAnchorLevel(int level) {
        if (mAnchorLevelMap == null) {
            String configString = SpUtil.getInstance().getStringValue(SpUtil.CONFIG);
            if (!TextUtils.isEmpty(configString)) {
                JSONObject obj = JSON.parseObject(configString);
                setAnchorLevel(obj.getString("levelanchor"));
            }
        }
        if (mAnchorLevelMap == null || mAnchorLevelMap.size() == 0) {
            return null;
        }
        return mAnchorLevelMap.get(level);
    }


    public String getDeviceId() {
        if (TextUtils.isEmpty(mDeviceId)) {
            String deviceId = SpUtil.getInstance().getStringValue(SpUtil.DEVICE_ID);
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = DeviceUtils.getDeviceId();
                SpUtil.getInstance().setStringValue(SpUtil.DEVICE_ID, deviceId);
            }
            mDeviceId = deviceId;
        }
        L.e("getDeviceId---mDeviceId-----> " + mDeviceId);
        return mDeviceId;
    }

    public boolean isAgentSwitch() {
        return mAgentSwitch;
    }

    public void setAgentSwitch(boolean agentSwitch) {
        mAgentSwitch = agentSwitch;
    }


    public static boolean isYunBaoApp() {
        String packageName = CommonAppContext.getInstance().getPackageName();
        if (!TextUtils.isEmpty(packageName)) {
            return packageName.contains("com.yunbao.short");
        }
        return false;
    }
}
