package com.yunbao.main.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.ImageResultCallback;
import com.yunbao.common.utils.CityUtil;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.MediaUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;

import java.io.File;
import java.util.ArrayList;

import cn.qqtheme.framework.entity.City;
import cn.qqtheme.framework.entity.County;
import cn.qqtheme.framework.entity.Province;
import cn.qqtheme.framework.picker.AddressPicker;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class EditProfileActivity extends AbsActivity {

    public static void forward(Context context) {
        context.startActivity(new Intent(context, EditProfileActivity.class));
    }

    private ImageView mAvatar;
    private EditText mName;
    private TextView mBirthday;
    private TextView mSex;
    private TextView mZone;
    private EditText mSign;
    private TextView mSignNum;
    private View mBtnSave;
    private String mChooseProvince;
    private String mChoosedCity;
    private String mChoosedDistrict;
    private UserBean mUserBean;
    private File mUpDateAvatarFile;
    private JSONObject mJSONObject;
    private ImageResultCallback mImageResultCallback = new ImageResultCallback() {
        @Override
        public void beforeCamera() {

        }

        @Override
        public void onSuccess(File file) {
            if (file != null) {
                ImgLoader.display(mContext, file, mAvatar);
                mUpDateAvatarFile = file;
//                    CommonHttpUtil.updateAvatar(file, new HttpCallback() {
//                        @Override
//                        public void onSuccess(int code, String msg, String[] info) {
//                            if (code == 0 && info.length > 0) {
//                                ToastUtil.show(R.string.edit_profile_update_avatar_success);
//                                UserBean bean = CommonAppConfig.getInstance().getUserBean();
//                                if (bean != null) {
//                                    JSONObject obj = JSON.parseObject(info[0]);
//                                    bean.setAvatar(obj.getString("avatar"));
//                                    bean.setAvatarThumb(obj.getString("avatarThumb"));
//                                }
//                            }
//                        }
//                    });
            }
        }

        @Override
        public void onFailure() {
        }
    };


    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_profile;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.edit_profile));
        mAvatar = findViewById(R.id.avatar);
        mName = findViewById(R.id.name);
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 8) {
                    s = s.subSequence(0, 8);
                    mName.setText(s);
                    mName.setSelection(8);
                    ToastUtil.show(R.string.edit_profile_name_max_2);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBirthday = findViewById(R.id.birthday);
        mSex = findViewById(R.id.sex);
        mZone = findViewById(R.id.zone);
        mSign = findViewById(R.id.sign);
        mSignNum = findViewById(R.id.sign_num);
        mSign.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mSignNum != null && s != null) {
                    mSignNum.setText(s.length() + "/20");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBtnSave = findViewById(R.id.btn_save);
        MainHttpUtil.getBaseInfo(new CommonCallback<UserBean>() {
            @Override
            public void callback(UserBean bean) {
                showUser(bean);
            }
        });
    }

    private void showUser(UserBean u) {
        if (u == null) {
            return;
        }
        mUserBean = u;
        if (mAvatar != null) {
            ImgLoader.displayAvatar(mContext, u.getAvatarThumb(), mAvatar);
        }
        if (mName != null) {
            mName.setText(u.getUserNiceName());
            String name = mName.getText().toString();
            if (!TextUtils.isEmpty(name)) {
                mName.setSelection(name.length());
            }
        }
        if (mBirthday != null) {
            mBirthday.setText(u.getBirthday());
        }
        if (mSex != null) {
            mSex.setText(u.getSex() == 1 ? R.string.sex_male : R.string.sex_female);
        }
        if (mZone != null) {
            String province = u.getProvince();
            String city = u.getCity();
            String district = u.getDistrict();
            if (!TextUtils.isEmpty(province) && !TextUtils.isEmpty(city) && !city.equals("城市未填写")) {
                String result = province + city;
                if (!TextUtils.isEmpty(district)) {
                    result += district;
                }
                mZone.setText(result);
            }
        }
        if (mSign != null) {
            mSign.setText(u.getSignature());
            String sign = mSign.getText().toString();
            if (!TextUtils.isEmpty(sign)) {
                mSign.setSelection(sign.length());
            }
        }
    }


    public void editProfileClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_avatar) {
            editAvatar();
        } else if (i == R.id.btn_birthday) {
            editBirthday();
        } else if (i == R.id.btn_sex) {
            editSex();
        } else if (i == R.id.btn_zone) {
            editZone();
        } else if (i == R.id.btn_save) {
            save();
        }

    }

    /**
     * 修改头像
     */
    private void editAvatar() {
        DialogUitl.showStringArrayDialog(mContext, new Integer[]{
                R.string.camera, R.string.alumb}, new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int tag) {
                if (tag == R.string.camera) {
                    MediaUtil.getImageByCamera(EditProfileActivity.this, mImageResultCallback);
                } else {
                    MediaUtil.getImageByAlumb(EditProfileActivity.this, mImageResultCallback);
                }
            }
        });
    }

    /**
     * 修改性别
     */
    private void editSex() {
        DialogUitl.showStringArrayDialog(mContext, new Integer[]{
                R.string.sex_male, R.string.sex_female}, new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(final String text, int tag) {
                if (mSex != null) {
                    mSex.setText(text);
                }
                if (mJSONObject == null) {
                    mJSONObject = new JSONObject();
                }
                mJSONObject.put("sex", tag == R.string.sex_male ? 1 : 0);
            }
        });
    }

    /**
     * 修改生日
     */
    private void editBirthday() {
        DialogUitl.showDatePickerDialog(mContext, new DialogUitl.DataPickerCallback() {
            @Override
            public void onConfirmClick(final String date) {
                if (mBirthday != null) {
                    mBirthday.setText(date);
                }
                if (mJSONObject == null) {
                    mJSONObject = new JSONObject();
                }
                mJSONObject.put("birthday", date);
            }
        });
    }


    /**
     * 修改地区
     */
    private void editZone() {
        final ArrayList<Province> list = CityUtil.getInstance().getCityList();
        if (list == null || list.size() == 0) {
            final Dialog loading = DialogUitl.loadingDialog(mContext);
            loading.show();
            CityUtil.getInstance().getCityListFromAssets(new CommonCallback<ArrayList<Province>>() {
                @Override
                public void callback(ArrayList<Province> newList) {
                    loading.dismiss();
                    if (newList != null) {
                        showChooseCityDialog(newList);
                    }
                }
            });
        } else {
            showChooseCityDialog(list);
        }
    }

    /**
     * 修改地址
     */
    private void showChooseCityDialog(ArrayList<Province> list) {
        String province = mChooseProvince;
        String city = mChoosedCity;
        String district = mChoosedDistrict;
        if (TextUtils.isEmpty(province)) {
            province = CommonAppConfig.getInstance().getProvince();
        }
        if (TextUtils.isEmpty(city)) {
            city = CommonAppConfig.getInstance().getCity();
        }
        if (TextUtils.isEmpty(district)) {
            district = CommonAppConfig.getInstance().getDistrict();
        }
        DialogUitl.showCityChooseDialog(this, list, province, city, district, new AddressPicker.OnAddressPickListener() {
            @Override
            public void onAddressPicked(Province province, City city, County county) {
                mChooseProvince = province.getAreaName();
                mChoosedCity = city.getAreaName();
                mChoosedDistrict = county.getAreaName();
                if (mZone != null) {
                    mZone.setText(mChooseProvince + mChoosedCity + mChoosedDistrict);
                }
                if (mJSONObject == null) {
                    mJSONObject = new JSONObject();
                }
                mJSONObject.put("province", mChooseProvince);
                mJSONObject.put("city", mChoosedCity);
                mJSONObject.put("area", mChoosedDistrict);
            }
        });
    }

    /**
     * 保存修改
     */
    private void save() {
        String nickname = mName.getText().toString().trim();
        if (TextUtils.isEmpty(nickname)) {
            ToastUtil.show(WordUtil.getString(R.string.edit_profile_input_nickname));
            return;
        } else {
            if (mUserBean != null) {
                if (!nickname.equals(mUserBean.getUserNiceName())) {
                    if (mJSONObject == null) {
                        mJSONObject = new JSONObject();
                    }
                    mJSONObject.put("user_nicename", nickname);
                }
            }
        }
        String sign = mSign.getText().toString();
        if (!sign.equals(mUserBean.getSignature())) {
            if (mJSONObject == null) {
                mJSONObject = new JSONObject();
            }
            mJSONObject.put("signature", sign);
        }
        if (mJSONObject != null) {
            if (mBtnSave != null) {
                mBtnSave.setClickable(false);
            }
            updateFields(mJSONObject.toJSONString());
        } else {
            if (mUpDateAvatarFile != null && mUpDateAvatarFile.exists()) {
                CommonHttpUtil.updateAvatar(mUpDateAvatarFile, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            ToastUtil.show(R.string.edit_profile_update_avatar_success);
                            UserBean bean = CommonAppConfig.getInstance().getUserBean();
                            if (bean != null) {
                                JSONObject obj = JSON.parseObject(info[0]);
                                bean.setAvatar(obj.getString("avatar"));
                                bean.setAvatarThumb(obj.getString("avatarThumb"));
                                finish();
                            }
                        }
                    }
                });
            } else {
                ToastUtil.show(WordUtil.getString(R.string.edit_profile_not_update));
            }
        }
    }

    private void updateFields(String fieldsJsonString) {
        CommonHttpUtil.updateFields(fieldsJsonString, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (obj != null) {
                        ToastUtil.show(obj.getString("msg"));
                    }
                    if (mUpDateAvatarFile != null && mUpDateAvatarFile.exists()) {
                        CommonHttpUtil.updateAvatar(mUpDateAvatarFile, new HttpCallback() {
                            @Override
                            public void onSuccess(int code, String msg, String[] info) {
                                if (code == 0 && info.length > 0) {
                                    ToastUtil.show(R.string.edit_profile_update_avatar_success);
                                    UserBean bean = CommonAppConfig.getInstance().getUserBean();
                                    if (bean != null) {
                                        JSONObject obj = JSON.parseObject(info[0]);
                                        bean.setAvatar(obj.getString("avatar"));
                                        bean.setAvatarThumb(obj.getString("avatarThumb"));
                                        finish();
                                    }
                                }
                            }
                        });
                    } else {
                        finish();
                    }

                } else {
                    ToastUtil.show(msg);
                    if (mBtnSave != null) {
                        mBtnSave.setClickable(true);
                    }
                }
            }

            @Override
            public void onError() {
                super.onError();
                if (mBtnSave != null) {
                    mBtnSave.setClickable(true);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        MainHttpUtil.cancel(MainHttpConsts.GET_BASE_INFO);
        CommonHttpUtil.cancel(CommonHttpConsts.UPDATE_AVATAR);
        CommonHttpUtil.cancel(CommonHttpConsts.UPDATE_FIELDS);
        super.onDestroy();
    }

}
