package com.yunbao.main.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;

import java.io.File;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class NameCardActivity extends AbsActivity implements View.OnClickListener {

    public static void forward(Context context) {
        context.startActivity(new Intent(context, NameCardActivity.class));
    }

    private ImageView mImgQrCode;
    private TextView mInviteCode;
    private View mGroupShare;
    private File mShareImageFile;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_name_card;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.a_075));
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        TextView name = findViewById(R.id.name);
        TextView idVal = findViewById(R.id.id_val);
        name.setText(u.getUserNiceName());
        idVal.setText("ID:" + u.getId());
        ImageView appIcon = findViewById(R.id.icon_app);
        appIcon.setImageResource(CommonAppConfig.getInstance().getAppIconRes());
        mImgQrCode = findViewById(R.id.img_qr_code);
        mInviteCode = findViewById(R.id.invite_code);
        mGroupShare = findViewById(R.id.group_share);
        findViewById(R.id.btn_save_album).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);
        MainHttpUtil.getAgentCode(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (mImgQrCode != null) {
                        ImgLoader.display(mContext, obj.getString("qr"), mImgQrCode);
                    }
                    if (mInviteCode != null) {
                        mInviteCode.setText(obj.getString("code"));
                    }
                }
            }
        });

    }


    @Override
    protected void onDestroy() {
        MainHttpUtil.cancel(MainHttpConsts.GET_AGENT_CODE);
        super.onDestroy();
    }


    private Bitmap getCardBitmap() {
        if (mGroupShare == null) {
            return null;
        }
        mGroupShare.setDrawingCacheEnabled(true);
        Bitmap bitmap = mGroupShare.getDrawingCache();
        bitmap = Bitmap.createBitmap(bitmap);
        mGroupShare.setDrawingCacheEnabled(false);
        return bitmap;
    }

    /**
     * 保存和分享图片
     */
    private void save(final boolean share) {
    }




    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_save_album) {
            save(false);
        } else if (i == R.id.btn_share) {
            save(true);
        }
    }

}
