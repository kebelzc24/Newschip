package com.newschip.fingerprint.activity;

import com.newschip.fingerprint.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

/**
 * @项目名: NFingers_Sync
 * @包名: com.newschip.fingerprint.activity
 * @类名: WelcomeUIActivity
 * @创建者: 邱英健
 * @创建时间: 2015-8-6
 * @描述: 欢迎界面
 */

public class WelcomeUIActivity extends BaseActivity {
    private final static long ANIMATION_DURATION = 1500;

    private static final String TAG = "welcomeUIActivity";

    // 标记是否是第一次打开
    public static final String KEY_FIRST_START = "is_first_start";
    // 根视图
    private View mRootView;
    @Override
    protected int getLayoutView() {
        return R.layout.activity_welcome;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 延迟几秒，然后跳转到相应页面
        new Handler().postDelayed(r, ANIMATION_DURATION);
    }

    Runnable r = new Runnable() {
        @Override
        public void run() {
            doNavgation();
        }
    };

    private void doNavgation() {
        // 页面跳转
        // 根据情况进行页面跳转
        // 如果是第一次打开应用程序，那么就进入引导页面，否则进入密码界面
        boolean isFirstStart = mSPUtil.getBoolean(KEY_FIRST_START,
                true);
        if (isFirstStart) {
            Log.d(TAG, "进入引导页面");
            Intent intent = new Intent(this, GuideUIActivity.class);
            startActivity(intent);
        } else {
            Log.d(TAG, "进入密码页面");
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
        }
        finish();
    }
}
