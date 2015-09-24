package com.newschip.fingerprint.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.newschip.fingerprint.R;
import com.newschip.fingerprint.adapter.ViewPagerAdapter;
import com.newschip.fingerprint.utils.CacheUtils;

/**
 * @项目名: NFingers_Sync
 * @包名: com.newschip.fingerprint.activity
 * @类名: GuideUIActivity
 * @创建者: 邱英健
 * @创建时间: 2015-8-5
 * @描述: 引导界面
 */

public class GuideUIActivity extends BaseActivity implements OnPageChangeListener {
    private ViewPager vp;// 页面中的ViewPager
    private ViewPagerAdapter vpAdapter;
    private List<View> views;
    private Button mBtnStart;// 开始按钮

    @Override
    protected int getLayoutView() {
        return R.layout.activity_guide;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // 初始化View
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.activity_guide_1, null));
        views.add(inflater.inflate(R.layout.activity_guide_2, null));
        views.add(inflater.inflate(R.layout.activity_guide_3, null));

        vpAdapter = new ViewPagerAdapter(views, this);
        vp = (ViewPager) findViewById(R.id.guide_pager);

        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this);
        mBtnStart = (Button) views.get(2).findViewById(R.id.guide_btn_start);
        mBtnStart.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 设置已经开启过应用
                CacheUtils.setBoolean(GuideUIActivity.this,
                        WelcomeUIActivity.KEY_FIRST_START, false);
                startActivity(new Intent(GuideUIActivity.this,
                        MainActivity.class));
                finish();
            }
        });
    }

    // 滑动状态改变
    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub

    }

    // 页面被滑动是改变
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    // 新的页面被选中时
    @Override
    public void onPageSelected(int arg0) {

    }
}
