package com.yunbao.main.activity;


import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.main.R;


// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class BackWallOtherActivity extends AbsActivity implements  View.OnClickListener {
    private String backWallThumb;
    private ImageView mBackWall;




    public static void forward(Context context,String backWallThumb) {
        Intent intent = new Intent(context,BackWallOtherActivity.class);
        intent.putExtra("backWallThumb",backWallThumb);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_back_wall_other;
    }

    @Override
    protected void main() {
        Intent intent=getIntent();
        backWallThumb=intent.getStringExtra("backWallThumb");
        mBackWall=findViewById(R.id.backWall);
        ImgLoader.display(mContext, backWallThumb, mBackWall);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i==R.id.btn_back){
            finish();
        }else if (i==R.id.backWall){
            finish();
        }

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
