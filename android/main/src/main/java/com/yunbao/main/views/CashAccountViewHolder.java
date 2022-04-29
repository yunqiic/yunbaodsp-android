package com.yunbao.main.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yunbao.common.Constants;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.CommonIconUtil;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.main.R;
import com.yunbao.main.activity.CashActivity;
import com.yunbao.main.http.MainHttpUtil;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class CashAccountViewHolder extends AbsViewHolder implements View.OnClickListener {

    private boolean mShowed;
    private LayoutInflater mInflater;
    private SparseArray<AccountType> mSparseArray;
    private int mKey;
    private View mBtnChooseType;
    private ImageView mAccountTypeImage;
    private TextView mAccountTypeName;
    private View mAccountTypeArrow;
    private View mGroup1;
    private View mGroup2;
    private View mGroup3;
    private EditText mEditAliAccount;
    private EditText mEditAliName;
    private EditText mEditWxAccount;
    private EditText mEditBankName;
    private EditText mEditBankAccount;
    private EditText mEditBankUserName;
    private HttpCallback mAddAccountCallback;

    public CashAccountViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_add_cash_account;
    }

    @Override
    public void init() {
        mSparseArray = new SparseArray<AccountType>();
        Drawable drawableAli = ContextCompat.getDrawable(mContext, CommonIconUtil.getCashTypeIcon(Constants.CASH_ACCOUNT_ALI));
        Drawable drawableWx = ContextCompat.getDrawable(mContext, CommonIconUtil.getCashTypeIcon(Constants.CASH_ACCOUNT_WX));
        Drawable drawableBank = ContextCompat.getDrawable(mContext, CommonIconUtil.getCashTypeIcon(Constants.CASH_ACCOUNT_BANK));
        mSparseArray.put(Constants.CASH_ACCOUNT_ALI, new AccountType(drawableAli, R.string.cash_type_ali));
        mSparseArray.put(Constants.CASH_ACCOUNT_WX, new AccountType(drawableWx, R.string.cash_type_wx));
        mSparseArray.put(Constants.CASH_ACCOUNT_BANK, new AccountType(drawableBank, R.string.cash_type_bank));
        mKey = Constants.CASH_ACCOUNT_ALI;
        mInflater = LayoutInflater.from(mContext);
        mAccountTypeImage = (ImageView) findViewById(R.id.account_type_img);
        mAccountTypeName = (TextView) findViewById(R.id.account_type_name);
        mAccountTypeArrow = findViewById(R.id.account_type_arrow);
        findViewById(R.id.root).setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        mBtnChooseType = findViewById(R.id.btn_choose_type);
        mBtnChooseType.setOnClickListener(this);
        mGroup1 = findViewById(R.id.input_group_1);
        mGroup2 = findViewById(R.id.input_group_2);
        mGroup3 = findViewById(R.id.input_group_3);
        mEditAliAccount = (EditText) findViewById(R.id.input_ali_account);
        mEditAliName = (EditText) findViewById(R.id.input_ali_name);
        mEditWxAccount = (EditText) findViewById(R.id.input_wx_account);
        mEditBankName = (EditText) findViewById(R.id.input_bank_name);
        mEditBankAccount = (EditText) findViewById(R.id.input_bank_account);
        mEditBankUserName = (EditText) findViewById(R.id.input_bank_user_name);
        mAddAccountCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
//                if (code == 0 && info.length > 0) {
//                    CashAccountBean bean = JSON.parseObject(info[0], CashAccountBean.class);
//                    ((CashActivity) mContext).insertAccount(bean);
//                }
                if (code == 0) {
                    if (mContext != null) {
                        ((CashActivity) mContext).loadData();
                    }
                }
                ToastUtil.show(msg);
            }
        };
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.root) {
            removeFromParent();

        } else if (i == R.id.btn_choose_type) {
            chooseType();

        } else if (i == R.id.btn_confirm) {
            addCashAccount();

        }
    }

    @Override
    public void addToParent() {
        super.addToParent();
        mShowed = true;
    }

    @Override
    public void removeFromParent() {
        super.removeFromParent();
        mShowed = false;
    }

    public boolean isShowed() {
        return mShowed;
    }

    private void addCashAccount() {
        String account = null;
        String name = null;
        String bank = null;
        if (mKey == Constants.CASH_ACCOUNT_ALI) {
            account = mEditAliAccount.getText().toString().trim();
            if (TextUtils.isEmpty(account)) {
                ToastUtil.show(R.string.cash_input_ali_account);
                return;
            }
            name = mEditAliName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                ToastUtil.show(R.string.cash_input_ali_name);
                return;
            }
            mEditAliAccount.setText("");
            mEditAliName.setText("");
        } else if (mKey == Constants.CASH_ACCOUNT_WX) {
            account = mEditWxAccount.getText().toString().trim();
            if (TextUtils.isEmpty(account)) {
                ToastUtil.show(R.string.cash_input_wx_account);
                return;
            }
            mEditWxAccount.setText("");
        } else {
            bank = mEditBankName.getText().toString().trim();
            if (TextUtils.isEmpty(bank)) {
                ToastUtil.show(R.string.cash_input_bank_name);
                return;
            }
            account = mEditBankAccount.getText().toString().trim();
            if (TextUtils.isEmpty(account)) {
                ToastUtil.show(R.string.cash_input_bank_account);
                return;
            }
            name = mEditBankUserName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                ToastUtil.show(R.string.cash_input_bank_user_name);
                return;
            }
            mEditBankName.setText("");
            mEditBankAccount.setText("");
            mEditBankUserName.setText("");
        }
        removeFromParent();
        MainHttpUtil.addCashAccount(account, name, bank, mKey, mAddAccountCallback);
    }

    /**
     * 选择账户类型
     */
    private void chooseType() {
        View v = mInflater.inflate(R.layout.view_cash_type_pop, null);
        final PopupWindow popupWindow = new PopupWindow(v, DpUtil.dp2px(90), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.bg_pop_cash));

        View btn1 = v.findViewById(R.id.btn_1);
        ImageView img1 = v.findViewById(R.id.img_1);
        TextView name1 = v.findViewById(R.id.name_1);

        View btn2 = v.findViewById(R.id.btn_2);
        ImageView img2 = v.findViewById(R.id.img_2);
        TextView name2 = v.findViewById(R.id.name_2);

        if (mKey == Constants.CASH_ACCOUNT_ALI) {
            AccountType accountType1 = mSparseArray.valueAt(1);
            AccountType accountType2 = mSparseArray.valueAt(2);
            btn1.setTag(mSparseArray.keyAt(1));
            img1.setImageDrawable(accountType1.getImgDrawable());
            name1.setText(accountType1.getName());
            btn2.setTag(mSparseArray.keyAt(2));
            img2.setImageDrawable(accountType2.getImgDrawable());
            name2.setText(accountType2.getName());
        } else if (mKey == Constants.CASH_ACCOUNT_WX) {
            AccountType accountType0 = mSparseArray.valueAt(0);
            AccountType accountType2 = mSparseArray.valueAt(2);
            btn1.setTag(mSparseArray.keyAt(0));
            img1.setImageDrawable(accountType0.getImgDrawable());
            name1.setText(accountType0.getName());
            btn2.setTag(mSparseArray.keyAt(2));
            img2.setImageDrawable(accountType2.getImgDrawable());
            name2.setText(accountType2.getName());
        } else {
            AccountType accountType0 = mSparseArray.valueAt(0);
            AccountType accountType1 = mSparseArray.valueAt(1);
            btn1.setTag(mSparseArray.keyAt(0));
            img1.setImageDrawable(accountType0.getImgDrawable());
            name1.setText(accountType0.getName());
            btn2.setTag(mSparseArray.keyAt(1));
            img2.setImageDrawable(accountType1.getImgDrawable());
            name2.setText(accountType1.getName());
        }
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Object tag = v.getTag();
                if (tag != null) {
                    int key = (int) tag;
                    AccountType accountType = mSparseArray.get(key);
                    mAccountTypeImage.setImageDrawable(accountType.mImgDrawable);
                    mAccountTypeName.setText(accountType.getName());
                    mKey = key;
                    switch (key) {
                        case Constants.CASH_ACCOUNT_ALI:
                            if (mGroup1.getVisibility() != View.VISIBLE) {
                                mGroup1.setVisibility(View.VISIBLE);
                            }
                            if (mGroup2.getVisibility() == View.VISIBLE) {
                                mGroup2.setVisibility(View.GONE);
                            }
                            if (mGroup3.getVisibility() == View.VISIBLE) {
                                mGroup3.setVisibility(View.GONE);
                            }
                            break;
                        case Constants.CASH_ACCOUNT_WX:
                            if (mGroup1.getVisibility() == View.VISIBLE) {
                                mGroup1.setVisibility(View.GONE);
                            }
                            if (mGroup2.getVisibility() != View.VISIBLE) {
                                mGroup2.setVisibility(View.VISIBLE);
                            }
                            if (mGroup3.getVisibility() == View.VISIBLE) {
                                mGroup3.setVisibility(View.GONE);
                            }
                            break;
                        case Constants.CASH_ACCOUNT_BANK:
                            if (mGroup1.getVisibility() == View.VISIBLE) {
                                mGroup1.setVisibility(View.GONE);
                            }
                            if (mGroup2.getVisibility() == View.VISIBLE) {
                                mGroup2.setVisibility(View.GONE);
                            }
                            if (mGroup3.getVisibility() != View.VISIBLE) {
                                mGroup3.setVisibility(View.VISIBLE);
                            }
                            break;
                    }
                }
            }
        };
        btn1.setOnClickListener(onClickListener);
        btn2.setOnClickListener(onClickListener);
        popupWindow.showAsDropDown(mBtnChooseType);
    }


    class AccountType {
        Drawable mImgDrawable;
        int mName;

        AccountType(Drawable drawable, int name) {
            mImgDrawable = drawable;
            mName = name;
        }

        Drawable getImgDrawable() {
            return mImgDrawable;
        }

        int getName() {
            return mName;
        }

    }


}
