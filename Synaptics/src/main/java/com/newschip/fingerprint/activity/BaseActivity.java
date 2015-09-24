package com.newschip.fingerprint.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

import com.newschip.fingerprint.R;
import com.newschip.fingerprint.provider.ProviderHelper;
import com.newschip.fingerprint.service.WatchDogService;
import com.newschip.fingerprint.tools.PreferenceUtil;
import com.newschip.fingerprint.utils.PackageUtils;
import com.newschip.fingerprint.utils.SystemBarTintManager;
import com.newschip.fingerprint.view.CommonTitleLayout;

public abstract class BaseActivity extends AppCompatActivity {
    protected Context mContext;

    public CommonTitleLayout mCommonTitleLayout;

    public PackageUtils mPackageUtils;
    public PreferenceUtil mSPUtil;

    public static final int NORMAL_TYPE = 0;

    public ProviderHelper mSwitchStateHelper;
    public final String TABLE_SWITCH_STATE = "switch_state";
    public ProviderHelper mProtectStateHelper;
    public final String TABLE_PROTECT_STATE = "protect_state";

    protected abstract int getLayoutView();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = this;
        if (getClassName().equals("GuideUIActivity")) {
            setStatusBarTranslucent();
        } else {
            setSystemBarImmerse();
        }
        mPackageUtils = new PackageUtils();
        mSPUtil = new PreferenceUtil(mContext);
        mSwitchStateHelper = new ProviderHelper(mContext, TABLE_SWITCH_STATE);
        mProtectStateHelper = new ProviderHelper(mContext, TABLE_PROTECT_STATE);
        initWindow();
        setContentView(getLayoutView());
    }

    @TargetApi(19)
    private void initWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintColor(getStatusBarColor());
            tintManager.setStatusBarTintEnabled(true);
        }
    }

    protected void initToolbar(Toolbar toolbar) {
        if (toolbar == null)
            return;
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(getColors(R.color.action_bar_title_color));
        toolbar.collapseActionView();
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected String getStringRes(int res){
        if (res <= 0)
            throw new IllegalArgumentException("resource id can not be less 0");
        return getResources().getString(res);
    }
    protected int getColors(int res) {
        if (res <= 0)
            throw new IllegalArgumentException("resource id can not be less 0");
        return getResources().getColor(res);
    }
    public int getStatusBarColor() {
        return getColorPrimary();
    }

    public int getColorPrimary() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }
    public void startActivitySafely(String action) {
        try {
            startActivity(new Intent(action));
            overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void showAlertDialog(Context context, int type) {
    }

    public void setCommonTitleBarTitle(int strId) {
        mCommonTitleLayout = (CommonTitleLayout) findViewById(R.id.title_bar);
        if (mCommonTitleLayout != null) {
            mCommonTitleLayout.setTitle(strId);
        }

    }

    /**
     * 获取子类的类名
     */
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    public void setStatusBarTranslucent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明状态栏
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 透明导航栏 一些手机如果有虚拟键盘的话,虚拟键盘就会变成透明的,挡住底部按钮点击事件所以,最后不要用
            // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    public void setSystemBarImmerse() {
        // TODO Auto-generated method stub
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            winParams.flags |= bits;
            win.setAttributes(winParams);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.title_bar_bg);// 通知栏所需颜色
        }

    }

    public void startOrStopWatchDogService() {
        if (mProtectStateHelper.getProtectState() || mSwitchStateHelper.getSwitchState()) {
            startService(new Intent(mContext, WatchDogService.class));
        } else {
            stopService(new Intent(mContext, WatchDogService.class));
        }
    }

}
