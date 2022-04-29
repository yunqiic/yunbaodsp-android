package com.yunbao.main.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.adapter.ViewPagerAdapter;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.ScreenDimenUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.activity.BackWallActivity;
import com.yunbao.main.activity.BackWallOtherActivity;
import com.yunbao.main.activity.EditProfileActivity;
import com.yunbao.main.activity.FansActivity;
import com.yunbao.main.activity.FollowActivity;
import com.yunbao.main.activity.MainActivity;
import com.yunbao.main.activity.NameCardActivity;
import com.yunbao.main.activity.UserHomeActivity;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import java.util.ArrayList;
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
public class UserHomeViewHolder extends AbsMainViewHolder implements AppBarLayout.OnOffsetChangedListener, UserWorkViewHolder.ActionListener, View.OnClickListener {

    private static final int PAGE_COUNT = 2;
    private AppBarLayout mAppBarLayout;
    private View mContent;
    private ImageView mBg;
    private View mTitleViewWrap;
    private TextView mTitleView;
    private ImageView mAvatar;
    private View mGroupAvatar;
    private View mLiveTip;
    private View mLiveBorder0;
    private View mLiveBorder1;
    private ScaleAnimation mScaleAnim0;
    private ScaleAnimation mScaleAnim1;
    private TextView mName;
    private TextView mIdVal;
    private TextView mSign;
    private TextView mSex;
    private TextView mAge;
    private TextView mCity;
    private TextView mZanNum;
    private TextView mFansNum;
    private TextView mFollowNum;
    private ViewGroup mOptionGroup;
    private View mLl;
    private View mLl1;
    private View mRlFollow;
    private View mBtnProfile;
    private View mBtnFollow;
    private MagicIndicator mIndicator;
    private ViewPager mViewPager;
    private List<FrameLayout> mViewList;
    private TextView mWorkCountTextView;
    private TextView mLikeCountTextView;
    private String mToUid;
    private String mWorkString;
    private String mLikeString;
    private AbsMainViewHolder[] mViewHolders;
    private HttpCallback mGetUserHomeCallback;
    private UserBean mUserBean;
    private UserWorkViewHolder mWorkViewHolder;
    private UserLikeViewHolder mLikeViewHolder;
    private float mRate;
    private ActionListener mActionListener;

    private boolean mIsSelf;
    private boolean mHasShop;
    private ImageView mBackWall;
    private String mBackWallThumb;
    private ViewGroup mRootView;
    private int folling;

    public UserHomeViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }



    @Override
    protected int getLayoutId() {
        return R.layout.view_user_home;
    }

    @Override
    public void init() {
        setStatusHeight();
        int paddingTop = ScreenDimenUtil.getInstance().getStatusBarHeight() + DpUtil.dp2px(25);
        mWorkString = WordUtil.getString(R.string.user_work);
        mLikeString = WordUtil.getString(R.string.user_like);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        mAppBarLayout.addOnOffsetChangedListener(this);
        View appBarChild = findViewById(R.id.app_bar_child);
        appBarChild.setMinimumHeight(paddingTop);
        mRootView = (ViewGroup) findViewById(R.id.root);

        mContent = findViewById(R.id.content);
        mBackWall = findViewById(R.id.backWall);
        mContent.setPadding(DpUtil.dp2px(10), paddingTop, DpUtil.dp2px(10), 0);
        mBg = (ImageView) findViewById(R.id.bg_img);
        mTitleViewWrap = findViewById(R.id.title_wrap);
        mTitleView = (TextView) findViewById(R.id.titleView);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mGroupAvatar = findViewById(R.id.group_avatar);
        mLiveTip = findViewById(R.id.live_tip);
        mLiveBorder0 = findViewById(R.id.live_border_0);
        mLiveBorder1 = findViewById(R.id.live_border_1);
        mName = (TextView) findViewById(R.id.name);
        mIdVal = (TextView) findViewById(R.id.id_val);
        mSign = (TextView) findViewById(R.id.sign);
        mSex = (TextView) findViewById(R.id.sex);
        mAge = (TextView) findViewById(R.id.age);
        mCity = (TextView) findViewById(R.id.city);
        mZanNum = (TextView) findViewById(R.id.zan_num);
        mFansNum = (TextView) findViewById(R.id.fans_num);
        mFollowNum = (TextView) findViewById(R.id.follow_num);
        mOptionGroup = (ViewGroup) findViewById(R.id.option_group);
        mOptionGroup.setPadding(0, 90, 0, 0);
        mLl = findViewById(R.id.ll);
        mLl1 = findViewById(R.id.ll1);
        mRlFollow = findViewById(R.id.rl_follow);
        mBtnProfile = findViewById(R.id.btn_edit_profile);
        mBtnFollow = findViewById(R.id.btn_edit_follow);
        mRlFollow.setOnClickListener(this);
        mBtnProfile.setOnClickListener(this);
        mBtnFollow.setOnClickListener(this);
        mGroupAvatar.setOnClickListener(this);
        mBackWall.setOnClickListener(this);
        findViewById(R.id.btn_fans).setOnClickListener(this);
        findViewById(R.id.btn_follow).setOnClickListener(this);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        if (PAGE_COUNT > 1) {
            mViewPager.setOffscreenPageLimit(PAGE_COUNT - 1);
        }
        mViewHolders = new AbsMainViewHolder[PAGE_COUNT];
        mViewList = new ArrayList<>();
        for (int i = 0; i < PAGE_COUNT; i++) {
            FrameLayout frameLayout = new FrameLayout(mContext);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mViewList.add(frameLayout);
        }
        mViewPager.setAdapter(new ViewPagerAdapter(mViewList));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                loadPageData(position);
                if (position != 0 && mActionListener != null) {
                    mActionListener.onWorkCountChanged(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mIndicator = (MagicIndicator) findViewById(R.id.indicator);
        final String[] titles = new String[]{mWorkString, mLikeString};
        CommonNavigator commonNavigator = new CommonNavigator(mContext);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {

            @Override
            public int getCount() {
                return titles.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
                simplePagerTitleView.setNormalColor(0xff73737a);
                simplePagerTitleView.setSelectedColor(ContextCompat.getColor(mContext, R.color.textColor));
                simplePagerTitleView.setText(titles[index]);
                simplePagerTitleView.setTextSize(15);
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mViewPager != null) {
                            mViewPager.setCurrentItem(index);
                        }
                    }
                });
                if (index == 0) {
                    mWorkCountTextView = simplePagerTitleView;
                } else if (index == 1) {
                    mLikeCountTextView = simplePagerTitleView;
                }
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
                linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                linePagerIndicator.setXOffset(DpUtil.dp2px(5));
                linePagerIndicator.setLineHeight(DpUtil.dp2px(2));
                linePagerIndicator.setRoundRadius(DpUtil.dp2px(1));
                linePagerIndicator.setColors(ContextCompat.getColor(mContext, R.color.textColor));
                return linePagerIndicator;
            }

        });
        mIndicator.setNavigator(commonNavigator);
        LinearLayout titleContainer = commonNavigator.getTitleContainer();
        titleContainer.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        titleContainer.setDividerDrawable(new ColorDrawable() {
            @Override
            public int getIntrinsicWidth() {
                return DpUtil.dp2px(60);
            }
        });
        ViewPagerHelper.bind(mIndicator, mViewPager);
        setBg();
    }

    private void setBg() {
        if (mContent != null) {
            mContent.post(new Runnable() {
                @Override
                public void run() {
                    if (mContent != null) {
                        int height = mContent.getHeight();
                        if (mBg != null) {
                            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mBg.getLayoutParams();
                            params.height = height;
                            mBg.requestLayout();
                            //ImgLoader.display(mContext, R.mipmap.bg_main_me, mBg);
                        }
                    }
                }
            });
        }
    }


    private void loadPageData(int position) {
        if (mViewHolders == null) {
            return;
        }
        AbsMainViewHolder vh = mViewHolders[position];
        if (vh == null) {
            if (mViewList != null && position < mViewList.size()) {
                FrameLayout parent = mViewList.get(position);
                if (parent == null) {
                    return;
                }
                if (position == 0) {
                    mWorkViewHolder = new UserWorkViewHolder(mContext, parent, mToUid);
                    mWorkViewHolder.setActionListener(this);
                    vh = mWorkViewHolder;
                } else if (position == 1) {
                    mLikeViewHolder = new UserLikeViewHolder(mContext, parent, mToUid);
                    vh = mLikeViewHolder;
                }
                if (vh == null) {
                    return;
                }
                mViewHolders[position] = vh;
                vh.addToParent();
                vh.subscribeActivityLifeCycle();
            }
        }
        if (vh != null) {
            vh.loadData();
        }
    }


    @Override
    public void loadData() {
        mHasShop = false;
        if (TextUtils.isEmpty(mToUid)) {
            return;
        }
        if (mGetUserHomeCallback == null) {
            mGetUserHomeCallback = new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0 && info.length > 0) {
                        JSONObject obj = JSON.parseObject(info[0]);
                        mHasShop = obj.getIntValue("isshop") == 1;
                        UserBean u = JSON.toJavaObject(obj, UserBean.class);
                        String vipInfo = obj.getString("vipinfo");
                        folling=u.getIsattention();
                        if (!TextUtils.isEmpty(vipInfo)) {
                            UserBean.Vip vip = new UserBean.Vip();
                            JSONObject vipObject = JSON.parseObject(vipInfo);
                            vip.setVip_switch(vipObject.getIntValue("vip_switch"));
                            vip.setIsvip(vipObject.getIntValue("isvip"));
                            vip.setVip_endtime(vipObject.getString("vip_endtime"));
                            u.setVipInfo(vip);
                        }
                        showData(u);
                        if (mActionListener != null) {
                            mActionListener.onUserInfoChanged(u);
                        }
                    }
                }
            };
        }
        if (mIsSelf) {
            MainHttpUtil.getBaseInfo(mGetUserHomeCallback);
        } else {
            MainHttpUtil.getUserHome(mToUid, mGetUserHomeCallback);
        }
        if (mViewPager != null) {
            loadPageData(mViewPager.getCurrentItem());
        }
    }


    /**
     * 显示数据
     */
    private void showData(UserBean u) {
        mUserBean = u;
        if (mTitleView != null) {
            mTitleView.setText(u.getUserNiceName());
        }
        if (mAvatar != null) {
            ImgLoader.display(mContext, u.getAvatarThumb(), mAvatar);
        }
        if (mBackWall != null) {
            if (TextUtils.isEmpty(u.getBgImg())){
                ImgLoader.display(mContext, u.getAvatarThumb(), mBackWall);
                mBackWallThumb=u.getAvatarThumb();
            }else {
                ImgLoader.display(mContext, u.getBgImg(), mBackWall);
                mBackWallThumb=u.getBgImg();
            }
        }
        if (mName != null) {
            mName.setText(u.getUserNiceName());
        }
        if (mIdVal != null) {
            mIdVal.setText("ID:" + u.getId());
        }

        if (mSign != null) {
            mSign.setText(u.getSignature());
        }
        if (mSex != null) {
            mSex.setText(u.getSex() == 1 ? R.string.sex_male : R.string.sex_female);
        }
        if (mAge != null) {
            mAge.setText(u.getAge());
        }
        if (mCity != null) {
            mCity.setText(u.getCity());
        }
        if (mZanNum != null) {
            mZanNum.setText(String.valueOf(u.getPraise()));
        }
        if (mFansNum != null) {
            mFansNum.setText(String.valueOf(u.getFans()));
        }
        if (mFollowNum != null) {
            mFollowNum.setText(String.valueOf(u.getFollows()));
        }
        if (mWorkCountTextView != null) {
            mWorkCountTextView.setText(mWorkString + " " + u.getWorkVideos());
        }
        if (mLikeCountTextView != null) {
            mLikeCountTextView.setText(mLikeString + " " + u.getLikeVideos());
        }
        if (mIsSelf){
            mLl.setVisibility(View.VISIBLE);
            mLl1.setVisibility(View.GONE);
            mRlFollow.setVisibility(View.GONE);

        }else {
            mLl.setVisibility(View.GONE);
            if (u.getIsattention()==1){
                mRlFollow.setVisibility(View.GONE);
                mLl1.setVisibility(View.VISIBLE);
            }else{
                mRlFollow.setVisibility(View.VISIBLE);
                mLl1.setVisibility(View.GONE);
            }

        }


    }

    public void setToUid(String toUid) {
        mToUid = toUid;
        mIsSelf = mToUid.equals(CommonAppConfig.getInstance().getUid());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    @Override
    public void release() {
        MainHttpUtil.cancel(MainHttpConsts.GET_USER_HOME);
        mGetUserHomeCallback = null;
        mActionListener = null;
        if (mGroupAvatar != null) {
            mGroupAvatar.clearAnimation();
            mGroupAvatar = null;
        }
        if (mLiveBorder1 != null) {
            mLiveBorder1.clearAnimation();
            mLiveBorder1 = null;
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        float totalScrollRange = appBarLayout.getTotalScrollRange();
        float rate = -1 * verticalOffset / totalScrollRange * 2;
        if (rate >= 1) {
            rate = 1;
        }
        if (mRate != rate) {
            mRate = rate;
            mTitleViewWrap.setAlpha(rate);
        }
    }

    public ViewGroup getOptionContainer() {
        return mOptionGroup;
    }

    public void setOptionView(View v) {
        if (mOptionGroup != null) {
            mOptionGroup.addView(v);
        }
    }


    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    @Override
    public void onWorkCountChanged(int count) {
        if (mActionListener != null) {
            mActionListener.onWorkCountChanged(count == 0);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_fans) {
            FansActivity.forward(mContext, mToUid);
        } else if (i == R.id.btn_follow) {
            FollowActivity.forward(mContext, mToUid);
        } else if (i==R.id.backWall){
            if (mIsSelf&&mContext instanceof MainActivity) {
                Intent intent = new Intent(mContext, BackWallActivity.class);
                intent.putExtra("backWallThumb", mBackWallThumb);
                ((MainActivity) mContext).startActivityForResult(intent, 1);
            }else if (mIsSelf&&mContext instanceof UserHomeActivity){
                Intent intent = new Intent(mContext, BackWallActivity.class);
                intent.putExtra("backWallThumb", mBackWallThumb);
                ((UserHomeActivity) mContext).startActivityForResult(intent, 1);
           }else{
                BackWallOtherActivity.forward(mContext,mBackWallThumb);
            }

        }else if (i==R.id.btn_edit_profile){
            EditProfileActivity.forward(mContext);
        }else if (i==R.id.btn_edit_follow){
            follow();
        }else if (i==R.id.rl_follow){
           follow();
        }
    }

    public interface ActionListener {
        void onUserInfoChanged(UserBean userBean);

        void onWorkCountChanged(boolean showNoWorkTip);
    }

    /**
     * 点击关注
     */
    private void follow(){
        CommonHttpUtil.setAttention(mToUid, new CommonCallback<Integer>() {
            @Override
            public void callback(Integer isAttention) {
                if (isAttention==1){
                    mRlFollow.setVisibility(View.GONE);
                    mLl1.setVisibility(View.VISIBLE);
                }else{
                    mLl1.setVisibility(View.GONE);
                    mRlFollow.setVisibility(View.VISIBLE);
                }

            }
        });
    }





}
