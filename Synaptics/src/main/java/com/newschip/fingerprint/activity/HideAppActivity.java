package com.newschip.fingerprint.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.newschip.fingerprint.R;
import com.newschip.fingerprint.utils.SystemBarTintManager;
import com.newschip.fingerprint.utils.ToastUtils;

public class HideAppActivity extends BaseActivity implements OnClickListener {

    private Button BtnAddHideApp;

    @Override
    protected int getLayoutView() {
        return R.layout.activity_hide_app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        initViews();

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        loadHideApp();
    }

    private void loadHideApp() {

    }

    private void initViews() {
        BtnAddHideApp = (Button) findViewById(R.id.btn_add_hide_app);
        BtnAddHideApp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == BtnAddHideApp) {
            ToastUtils.show(mContext, "click");

        }

    }

}
