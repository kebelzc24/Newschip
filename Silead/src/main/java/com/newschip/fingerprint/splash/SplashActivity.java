package com.newschip.fingerprint.splash;

import java.util.ArrayList;
import java.util.List;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;
import android.view.WindowManager;

import com.newschip.fingerprint.R;
import com.newschip.fingerprint.utils.SystemBarTintManager;
/***
 * @author:liuzhicang
 * data:2015-06-09
 * description:SplashActivity内容引导页
 * */

public class SplashActivity extends FragmentActivity {

    private ViewPager mVPActivity;
    private SplashFramen mFragment1;
    private SplashFramen mFragment2;
    private SplashFramen mFragment3;
    private SplashFramen mFragment4;
    private List<Fragment> mListFragment = new ArrayList<Fragment>();
    private PagerAdapter mPgAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager);
        setImmerseMode();
        initView();
    }

    private void initView() {
        mVPActivity = (ViewPager) findViewById(R.id.vp_activity);
        mFragment1 = new SplashFramen(R.layout.splash_fragment_one);
        mFragment2 = new SplashFramen(R.layout.splash_fragment_two);
        mFragment3 = new SplashFramen(R.layout.splash_fragment_three);
        mFragment4 = new SplashFramen(R.layout.splash_fragment_four);
        mListFragment.add(mFragment1);
        mListFragment.add(mFragment2);
        mListFragment.add(mFragment3);
        mListFragment.add(mFragment4);
        mPgAdapter = new ViewPagerAdapter(getSupportFragmentManager(),
                mListFragment);
        mVPActivity.setAdapter(mPgAdapter);
    }
    
    private void setImmerseMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            winParams.flags |= bits;
            win.setAttributes(winParams);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(android.R.color.white);// 通知栏所需颜色
        }
    }

}
