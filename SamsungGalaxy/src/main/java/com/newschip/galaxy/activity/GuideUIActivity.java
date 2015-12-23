package com.newschip.galaxy.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.newschip.galaxy.R;
import com.newschip.galaxy.adapter.ViewPagerAdapter;
import com.newschip.galaxy.utils.CacheUtils;

import java.util.ArrayList;
import java.util.List;

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
    // 标记是否是第一次打开
    public static final String KEY_FIRST_START = "is_first_start";

    @Override
    public int getLayoutView() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        return R.layout.activity_guide;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
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
                        PasswordActivity.KEY_FIRST_START, false);
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
