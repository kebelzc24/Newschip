package com.newschip.galaxy.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;
import android.view.WindowManager;

import com.newschip.galaxy.R;
import com.newschip.galaxy.utils.PreferenceUtil;
import com.newschip.galaxy.widget.SplashFramen;

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

public class GuideUIActivity extends FragmentActivity {
    private ViewPager mVPActivity;
    private SplashFramen mFragment1;
    private SplashFramen mFragment2;
    private SplashFramen mFragment3;
    private List<Fragment> mListFragment = new ArrayList<Fragment>();
    private PagerAdapter mPgAdapter;
    public static final String KEY_FIRST_START = "is_first_start";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_guide);
        PreferenceUtil util = new PreferenceUtil(this);

        if(!util.getBoolean(KEY_FIRST_START,true)){
            finish();
        }
        initView();
    }

    private void initView() {
        mVPActivity = (ViewPager) findViewById(R.id.guide_pager);
        mFragment1 = new SplashFramen(GuideUIActivity.this,getResources().getDrawable(
                R.mipmap.guide_1),false);
        mFragment2 = new SplashFramen(GuideUIActivity.this,getResources().getDrawable(
                R.mipmap.guide_2),false);
        mFragment3 = new SplashFramen(GuideUIActivity.this,getResources().getDrawable(
                R.mipmap.guide_3),true);
        mListFragment.add(mFragment1);
        mListFragment.add(mFragment2);
        mListFragment.add(mFragment3);
        mPgAdapter = new ViewPagerAdapter(getSupportFragmentManager(),
                mListFragment);
        mVPActivity.setAdapter(mPgAdapter);
    }
    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragmentList = new ArrayList<Fragment>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public ViewPagerAdapter(FragmentManager fragmentManager,
                                List<Fragment> arrayList) {
            super(fragmentManager);
            this.fragmentList = arrayList;
        }

        @Override
        public Fragment getItem(int arg0) {
            return fragmentList.get(arg0);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

    }
}
